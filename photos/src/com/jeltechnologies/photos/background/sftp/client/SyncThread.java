package com.jeltechnologies.photos.background.sftp.client;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.background.thumbs.SingleMediaProducer;
import com.jeltechnologies.photos.datatypes.usermodel.Role;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.utils.StringUtils;

public class SyncThread implements Runnable, SyncThreadMBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncThread.class);

    private int filesOnNAS = -1;

    private String status;

    private final File uncategorizedFolder;

    private SFTPClient ftp;

    private String threadName;

    private final String relativeDestinationRoot;
    
    private final static Role ROLE = RoleModel.ROLE_ADMIN;

    public SyncThread(String threadName, SFTPClientConnectionData connectionData, File uncategorizedFolder) {
	this.threadName = threadName;
	this.relativeDestinationRoot = connectionData.getUser() + "/" + connectionData.getRootFolder() + "/";
	ftp = new SFTPClient(connectionData);
	this.uncategorizedFolder = uncategorizedFolder;
    }

    @Override
    public void run() {
	String oldThreadName = Thread.currentThread().getName();
	Thread.currentThread().setName(threadName);
	if (LOGGER.isInfoEnabled()) {
	    LOGGER.info(threadName + " started");
	}
	status = "Idle";
	try {
	    try {
		status = "Getting files from NAS by FTP";
		ftp.connect();
		List<FileInfo> files = ftp.getFiles();
		filesOnNAS = files.size();
		copyMissingFiles(files);
	    } catch (InterruptedException e) {
		LOGGER.info(threadName + " interrupted");
	    } catch (Exception e) {
		LOGGER.warn("Error sycnhronizing files: " + e.getMessage(), e);
	    }
	} finally {
	    if (ftp != null) {
		ftp.close();
	    }
	}
	status = "Idle";
	if (LOGGER.isInfoEnabled()) {
	    LOGGER.info(threadName + " ended synchronized " + StringUtils.formatNumber(filesOnNAS) + " files");
	}
	Thread.currentThread().setName(oldThreadName);
    }

    private void copyMissingFiles(List<FileInfo> files) throws Exception {
	int missing = 0;
	for (FileInfo file : files) {
	    File targetFile = getFileInAlbum(file);
	    if (!targetFile.isFile()) {
		missing++;
	    }
	}
	if (missing > 0) {
	    LOGGER.info(threadName + " retrieving " + missing + " new file(s)");
	    SingleMediaProducer cacheProducer = new SingleMediaProducer(ROLE); 
	    for (FileInfo file : files) {
		File targetFile = getFileInAlbum(file);
		if (!targetFile.isFile()) {
		    InputStream in = null;
		    try {
			in = ftp.getInputStream(file);
			status = "Copying " + file.getName() + " to " + targetFile;
			File parentFolder = targetFile.getParentFile();
			if (!parentFolder.exists()) {
			    boolean ok = parentFolder.mkdirs();
			    if (!ok) {
				LOGGER.error("Cannot make folder " + parentFolder);
			    }
			}
			Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			if (LOGGER.isDebugEnabled()) {
			    LOGGER.debug("Copied " + file.getName() + " to " + targetFile);
			}
			cacheProducer.addToQueue(targetFile);
		    } finally {
			if (in != null) {
			    in.close();
			}
		    }
		}
	    }
	} else {
	    LOGGER.info(threadName + " no new files found");
	}
    }

    private File getFileInAlbum(FileInfo file) {
	StringBuilder b = new StringBuilder();
	b.append(this.uncategorizedFolder.getAbsolutePath()).append("/");
	b.append(this.relativeDestinationRoot);
	b.append(file.getRelativePath());
	File targetFile = new File(b.toString());
	return targetFile;
    }

    public String getThreadName() {
	return threadName;
    }

    @Override
    public String getStatus() {
	return status;
    }

    @Override
    public int getFilesOnNAS() {
	return filesOnNAS;
    }

}
