package com.jeltechnologies.photos.background.sftp.client;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.background.thumbs.SingleMediaProducer;
import com.jeltechnologies.photos.datatypes.usermodel.Role;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.utils.FileUtils;
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

    private record FileDifference(String reason, FileInfo remoteFile, File localFile) {
    };

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

    private DateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final boolean COPY_FILES = true;

    private void copyMissingFiles(List<FileInfo> files) throws Exception {
	SingleMediaProducer cacheProducer = new SingleMediaProducer(ROLE);
	List<FileDifference> filesThatMustBeCopied = new ArrayList<FileDifference>();
	for (FileInfo file : files) {
	    File localFile = getFileInAlbum(file);
	    if (!localFile.isFile()) {
		filesThatMustBeCopied.add(new FileDifference("Missing in destination", file, localFile));
	    } else {
		Path localPath = localFile.toPath();
		BasicFileAttributes attributes = Files.readAttributes(localPath, BasicFileAttributes.class);
		FileTime lastModifiedTime = attributes.lastModifiedTime();
		boolean sameMinute = isTheSameMinute(file.lastModifiedDate().getTime(), lastModifiedTime.toMillis());
		if (!sameMinute) {
		    StringBuilder description = new StringBuilder("Different modification time,");
		    description.append(logDateFormat.format(file.lastModifiedDate())).append(",")
			    .append(logDateFormat.format(new Date(lastModifiedTime.toMillis()))).append(",")
			    .append(file.absolutePath()).append(",").append(localFile);
		    filesThatMustBeCopied.add(new FileDifference(description.toString(), file, localFile));
		}
	    }
	}

	List<String> logLines = new ArrayList<String>();
	if (!filesThatMustBeCopied.isEmpty()) {
	    LOGGER.info(threadName + " retrieving " + StringUtils.formatNumber(filesThatMustBeCopied.size()) + " new file(s)");
	    for (FileDifference difference : filesThatMustBeCopied) {
		if (COPY_FILES) {
		    if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Diff;" + difference.reason());
		    }
		    logLines.add(difference.reason());
		    FileInfo file = difference.remoteFile();
		    File targetFile = getFileInAlbum(file);
		    InputStream in = null;
		    try {
			in = ftp.getInputStream(file);
			Path temporaryTargetPath = new File(targetFile.getParentFile(), targetFile.getName() + ".tmp").toPath();
			status = "Copying " + file.name() + " to " + targetFile;
			File parentFolder = targetFile.getParentFile();
			if (!parentFolder.exists()) {
			    boolean ok = parentFolder.mkdirs();
			    if (!ok) {
				LOGGER.error("Cannot make folder " + parentFolder);
			    }
			}
			Path targetPath = targetFile.toPath();
			Files.copy(in, temporaryTargetPath, StandardCopyOption.REPLACE_EXISTING);
			FileTime lastModified = FileTime.fromMillis(file.lastModifiedDate().getTime());
			Files.setAttribute(temporaryTargetPath, "lastModifiedTime", lastModified);
			Files.setAttribute(temporaryTargetPath, "creationTime", lastModified);
			Files.move(temporaryTargetPath, targetPath, StandardCopyOption.REPLACE_EXISTING);

			if (LOGGER.isInfoEnabled()) {
			    LOGGER.info("Copied " + file.name() + " to " + targetFile);
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

	FileUtils.writeTextFile("c:\\tmp\\synclog.csv", logLines);
    }

    private boolean isTheSameMinute(long one, long two) {
	LocalDateTime dt1 = LocalDateTime.ofInstant(Instant.ofEpochMilli(one), ZoneId.systemDefault());
	LocalDateTime dt2 = LocalDateTime.ofInstant(Instant.ofEpochMilli(two), ZoneId.systemDefault());
	boolean same = true;
	if (same) {
	    same = dt1.getYear() == dt2.getYear();
	}
	if (same) {
	    same = dt1.getMonth() == dt2.getMonth();
	}
	if (same) {
	    same = dt1.getDayOfMonth() == dt2.getDayOfMonth();
	}
	if (same) {
	    same = dt1.getHour() == dt2.getHour();
	}
	if (same) {
	    same = dt1.getMinute() == dt2.getMinute();
	}
	// ignore seconds, millisecond and nanoseconds
	return same;
    }

    private File getFileInAlbum(FileInfo file) {
	StringBuilder b = new StringBuilder();
	b.append(this.uncategorizedFolder.getAbsolutePath()).append("/");
	b.append(this.relativeDestinationRoot);
	b.append(file.relativePath());
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
