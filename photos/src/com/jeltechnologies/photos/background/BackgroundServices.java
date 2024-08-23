package com.jeltechnologies.photos.background;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.background.sftp.client.SFTPClientConnectionData;
import com.jeltechnologies.photos.background.sftp.client.SyncThread;
import com.jeltechnologies.photos.background.sftp.server.FileChangeHandler;
import com.jeltechnologies.photos.background.sftp.server.SFTPServer;
import com.jeltechnologies.photos.background.sftp.server.SFTPServer.User;
import com.jeltechnologies.photos.background.thumbs.CleanPhotosThread;
import com.jeltechnologies.photos.background.thumbs.ProduceFolder;
import com.jeltechnologies.photos.background.thumbs.Producer;
import com.jeltechnologies.photos.background.thumbs.ThumbnailsConsumer;
import com.jeltechnologies.photos.background.thumbs.UpdateCacheJob;
import com.jeltechnologies.photos.config.yaml.RefreshConfiguration;
import com.jeltechnologies.photos.config.yaml.SFTPClientAccount;
import com.jeltechnologies.photos.config.yaml.SFTPServerConfig;
import com.jeltechnologies.photos.config.yaml.SFTPServerUser;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.pictures.MediaQueue;
import com.jeltechnologies.photos.pictures.PhotosFileNameFilter;
import com.jeltechnologies.photos.servlet.ThreadService;
import com.jeltechnologies.photos.utils.JMXUtils;
import com.jeltechnologies.photos.videos.VideoConsumer;

public class BackgroundServices {
    private final boolean maintenanceJob = false;

    private static final Logger LOGGER = LoggerFactory.getLogger(BackgroundServices.class);
    private static final JMXUtils JMX = JMXUtils.getInstance();
    private static final Environment ENV = Environment.INSTANCE;

    private final ThreadService threadService = new ThreadService();

    private MediaQueue thumbsQueue;
    private MediaQueue videoQueue;
    private final static BackgroundServices INSTANCE = new BackgroundServices();
    private static final boolean START_BACKGROUND_TASKS = ENV.getConfig().isStartBackgroundTasksAtStartup();
    private static final File ROOT_ALBUM = Environment.INSTANCE.getConfig().getAlbumsFolder();
    private static final boolean MOVE_FAILED_FILES = true;
    private List<SFTPServer> sftpServers = new ArrayList<SFTPServer>();
    private Producer ongoingProducer = null;

    public static BackgroundServices getInstance() {
	return INSTANCE;
    }

    public ThreadService getThreadService() {
	return this.threadService;
    }

    private BackgroundServices() {
	createQueues();
    }

    public int getThumbsQueueSize() {
	return thumbsQueue.getSize();
    }

    public int getVideoQueueSize() {
	return videoQueue.getSize();
    }

    private void createQueues() {
	thumbsQueue = new MediaQueue();
	JMX.registerMBean("Thumbnails", "MediaQueues", thumbsQueue);
	videoQueue = new MediaQueue();
	JMX.registerMBean("Videos", "MediaQueues", videoQueue);
    }

    public void start() {
	if (maintenanceJob) {
	    startMaintenance();
	} else {
	    if (START_BACKGROUND_TASKS) {
		LOGGER.info("Starting background services");
		startConsumers();
		scheduleProducers();
		scheduleHousekeeping();
		startSFTPServer();
		scheduleSFTPClients();
	    } else {
		LOGGER.warn("Configured to not start background services");
	    }
	    // scheduleUpdateTimelineCache();
	}
    }

    public void startTask(Runnable command) {
	threadService.execute(command);
    }

    @SuppressWarnings("rawtypes")
    public Future submitTask(Runnable command) {
	return threadService.submit(command);
    }

    private void startMaintenance() {
	Runnable maintenance = new MaintenanceJob();
	threadService.execute(maintenance);
    }

    public void startProducer() {
	synchronized (this) {
	    if (LOGGER.isDebugEnabled()) {
		String log;
		if (ongoingProducer == null) {
		    log = "null";
		} else {
		    log = ongoingProducer.toString();
		}
		LOGGER.debug("Ongoing producer: " + log);
	    }
	    boolean ongoing = ongoingProducer != null && ongoingProducer.isRunning();
	    if (!ongoing) {
		Producer producer = createProducer(Producer.Type.ONLY_ADD_NEW_PHOTOS);
		threadService.execute(producer);
		ongoingProducer = producer;
	    } else {
		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug("Ignoring request for new producer, because already one producer is running");
		}
	    }
	}
    }

    private void scheduleProducers() {
	Producer producerCompleteRefresh = createProducer(Producer.Type.COMPLETE_REFRESH);
	// Producer producerCompleteRefresh = createTestProducer(Producer.Type.COMPLETE_REFRESH);

	RefreshConfiguration refreshConfiguration = Environment.INSTANCE.getConfig().getRefreshConfiguration();
	if (refreshConfiguration.isAllAtStarup()) {
	    threadService.execute(producerCompleteRefresh);
	    LOGGER.info("Starting a complete refresh at startup");
	} else {
	    LOGGER.info("Skipping a complete refresh at startup");
	}
	if (refreshConfiguration.isScheduled()) {
	    int hour = refreshConfiguration.getAtHour();
	    int minute = refreshConfiguration.getAtMinute();
	    threadService.scheduleDailyAt(producerCompleteRefresh, hour, minute);
	    LOGGER.info("Scheduled a daily complete refresh at " + hour + ":" + minute);
	}
    }

    private Producer createProducer(Producer.Type type) {
	Producer producer = new Producer("Photos-Thumbnails-Producer", type);
	producer.add(getAlbumsPhotosFolder());
	producer.add(getAlbumsVideosFolder());
	producer.add(getUncategorizedPhotosFolder());
	producer.add(getUncategorizedVideosFolder());
	LOGGER.info(producer.toString());
	return producer;
    }

    @SuppressWarnings("unused")
    private Producer createTestProducer(Producer.Type type) {
	File testFolder = new File("D:\\Projects\\Photos\\Originals\\Albums\\2024\\2024-07");

	ProduceFolder photosFolder = new ProduceFolder();
	photosFolder.setFilenameFilter(new PhotosFileNameFilter(Environment.PHOTO_EXTENSIONS));
	photosFolder.setRole(RoleModel.ROLE_USER);
	photosFolder.setFolder(testFolder);
	photosFolder.setQueue(thumbsQueue);

	ProduceFolder videosFolder = new ProduceFolder();
	videosFolder.setFilenameFilter(new PhotosFileNameFilter(Environment.VIDEO_EXTENSIONS));
	videosFolder.setRole(RoleModel.ROLE_USER);
	videosFolder.setFolder(testFolder);
	videosFolder.setQueue(videoQueue);

	Producer producer = new Producer("Photos-Thumbnails-Producer", type);
	producer.add(photosFolder);
	producer.add(videosFolder);
	LOGGER.info(producer.toString());
	return producer;
    }

    private ProduceFolder getAlbumsPhotosFolder() {
	ProduceFolder folder = new ProduceFolder();
	folder.setFilenameFilter(new PhotosFileNameFilter(Environment.PHOTO_EXTENSIONS));
	folder.setRole(RoleModel.ROLE_USER);
	folder.setFolder(ROOT_ALBUM);
	folder.setQueue(thumbsQueue);
	return folder;
    }

    private ProduceFolder getAlbumsVideosFolder() {
	ProduceFolder folder = new ProduceFolder();
	folder.setFilenameFilter(new PhotosFileNameFilter(Environment.VIDEO_EXTENSIONS));
	folder.setRole(RoleModel.ROLE_USER);
	folder.setFolder(ROOT_ALBUM);
	folder.setQueue(videoQueue);
	return folder;
    }

    private ProduceFolder getUncategorizedPhotosFolder() {
	ProduceFolder folder = new ProduceFolder();
	folder.setFilenameFilter(new PhotosFileNameFilter(Environment.PHOTO_EXTENSIONS));
	folder.setRole(RoleModel.ROLE_ADMIN);
	folder.setFolder(Environment.INSTANCE.getConfig().getUncategorizedFolder());
	folder.setQueue(thumbsQueue);
	return folder;
    }

    private ProduceFolder getUncategorizedVideosFolder() {
	ProduceFolder folder = new ProduceFolder();
	folder.setFilenameFilter(new PhotosFileNameFilter(Environment.VIDEO_EXTENSIONS));
	folder.setRole(RoleModel.ROLE_ADMIN);
	folder.setFolder(Environment.INSTANCE.getConfig().getUncategorizedFolder());
	folder.setQueue(videoQueue);
	return folder;
    }

    private void startConsumers() {
	int nrOfConsumers = ENV.getConfig().getNrOfThumbnailsConsumers();
	for (int i = 0; i < nrOfConsumers; i++) {
	    String threadName = "Photos-ThumbnailsConsumer-" + (i + 1);
	    ThumbnailsConsumer consumer = new ThumbnailsConsumer(thumbsQueue, threadName, MOVE_FAILED_FILES);
	    JMX.registerMBean(threadName, "MediaConsumers", consumer);
	    threadService.execute(consumer);
	}
	int nrOfVideoConsumers = ENV.getConfig().getNrOfVideoConsumers();
	for (int i = 0; i < nrOfVideoConsumers; i++) {
	    String threadName = "VideoConsumer-" + (i + 1);
	    VideoConsumer videoConsumer = new VideoConsumer(videoQueue, threadName, MOVE_FAILED_FILES);
	    JMX.registerMBean(threadName, "MediaConsumers", videoConsumer);
	    threadService.execute(videoConsumer);
	}
    }

    private void startSFTPServer() {
	SFTPServerConfig serverAccount = ENV.getConfig().getSFTPServer();
	if (serverAccount != null) {
	    FileChangeHandler listener = new FileChangeHandler();
	    SFTPServer server = null;
	    try {
		String folderName = ENV.getRootOriginalFolder() + "/" + ENV.getRelativeRootUncategorized() + "/sftp/";
		File folder = new File(folderName);
		List<User> users = new ArrayList<User>();
		for (SFTPServerUser configUser : serverAccount.getUsers()) {
		    users.add(new User(configUser.user(), configUser.password()));
		}
		server = new SFTPServer(serverAccount.getPort(), folder, users);
		server.addListener(listener);
		sftpServers.add(server);
	    } catch (Exception e) {
		String name;
		if (server == null) {
		    name = "null";
		} else {
		    name = server.getServerName();
		}
		LOGGER.error("Cannot start " + name + " because " + e.getMessage());

	    }
	}
    }

    private void scheduleSFTPClients() {
	List<SyncThread> threads = getSFTPClientThreads();
	LOGGER.info("Scheduling " + threads.size() + " SFTP accounts to download newest files");
	int threadCounter = 1;
	for (SyncThread thread : threads) {
	    JMX.registerMBean(thread.getThreadName(), "SFTP", thread);
	    int waitTime = (threadCounter * 1) - 1;
	    threadService.scheduleAtFixedRate(thread, waitTime, 60, TimeUnit.MINUTES);
	    threadCounter++;
	}
    }

    public List<SyncThread> getSFTPClientThreads() {
	List<SFTPClientAccount> accounts = ENV.getConfig().getSFTPClients();
	int threadCounter = 1;
	List<SyncThread> threads = new ArrayList<SyncThread>();
	for (SFTPClientAccount account : accounts) {
	    String userName = account.getUser();
	    String threadName = "SFTPSynchronization-" + userName + "-" + threadCounter;
	    SFTPClientConnectionData conData = new SFTPClientConnectionData();
	    conData.setHost(account.getHost());
	    conData.setPort(account.getPort());
	    conData.setUser(account.getUser());
	    conData.setPassword(account.getPassword());
	    conData.setRootFolder(account.getRootfolder());
	    SyncThread zinkThread = new SyncThread(threadName, conData, Environment.INSTANCE.getConfig().getUncategorizedFolder());
	    threads.add(zinkThread);
	}
	return threads;
    }

    private void scheduleHousekeeping() {
	Runnable houseKeeping = new CleanPhotosThread(MOVE_FAILED_FILES);
	threadService.scheduleAtFixedRate(houseKeeping, 0, 23, TimeUnit.HOURS);
    }

    public void refreshCacheAfter(long delay, TimeUnit timeUnit) {
	threadService.scheduleOnce(new UpdateCacheJob(this), delay, timeUnit);
    }

    @SuppressWarnings("unused")
    private void scheduleUpdateTimelineCache() {
	Runnable task = new UpdateCacheJob(this);
	threadService.scheduleAtFixedRate(task, 7, 7, TimeUnit.DAYS);
    }

    public MediaQueue getThumbsQueue() {
	return thumbsQueue;
    }

    public MediaQueue getVideoQueue() {
	return videoQueue;
    }

    public void shutdown() {
	for (SFTPServer server : this.sftpServers) {
	    try {
		server.stop();
	    } catch (IOException e) {
		LOGGER.error("Cannot stop " + server.getServerName());
	    }
	}
	JMX.unregisterAllMBeans();
	threadService.shutdown();
    }

}
