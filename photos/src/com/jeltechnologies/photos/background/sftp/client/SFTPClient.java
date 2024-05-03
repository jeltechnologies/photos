package com.jeltechnologies.photos.background.sftp.client;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jeltechnologies.photos.utils.StringUtils;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;

// https://www.baeldung.com/java-file-sftp

public class SFTPClient {

    private String rootFolder;

    private ChannelSftp sftp;

    private Session session;

    private String user;

    private String password;

    private String host;

    private int port;

    public SFTPClient() {
    }

    public SFTPClient(SFTPClientConnectionData account) {
	this.host = account.getHost();
	this.port = account.getPort();
	this.user = account.getUser();
	this.password = account.getPassword();
	this.rootFolder = account.getRootFolder();
    }

    public void connect() throws Exception {
	JSch jsch = new JSch();
	session = jsch.getSession(user, host, port);
	java.util.Properties config = new java.util.Properties();
	config.put("StrictHostKeyChecking", "no");
	session.setConfig(config);
	session.setPassword(password);
	session.connect();
	sftp = (ChannelSftp) session.openChannel("sftp");
	sftp.connect();
    }

    public void close() {
	try {
	    if (sftp != null) {
		sftp.exit();
	    }
	} catch (Exception exception) {
	    exception.printStackTrace();
	}
	if (session != null) {
	    session.disconnect();
	}
    }

    public List<FileInfo> getFiles() throws Exception {
	List<FileInfo> files = new ArrayList<FileInfo>();
	addFiles(rootFolder, files);
	return files;
    }

    private List<FileInfo> addFiles(String folder, List<FileInfo> files) throws Exception {
	// System.out.println("getFiles('" + folder + "')");

	// See
	// https://ourcodeworld.com/articles/read/29/how-to-list-a-remote-path-with-jsch-sftp-in-android
	List<String> folders = new ArrayList<String>();
	@SuppressWarnings("unchecked")
	List<LsEntry> children = sftp.ls(folder);
	
	String rootFolderSlashed = rootFolder + "/";

	for (LsEntry entry : children) {
	    String fileName = entry.getFilename();
	    SftpATTRS attr = entry.getAttrs();
	    if (attr.isDir()) {
		if (!fileName.startsWith(".")) {
		    folders.add(fileName);
		}
	    } else {
		FileInfo file = new FileInfo();
		files.add(file);
		file.setName(fileName);
		String absolutePath = folder + "/" + entry.getFilename();
		
		file.setAbsolutePath(absolutePath);
		long lastModified = attr.getMTime() * 1000L;
		
		file.setLastModifiedDate(new Date(lastModified));
		file.setSize(attr.getSize());
		
		String relativeFileName = StringUtils.findAfter(absolutePath, rootFolderSlashed);
		file.setRelativePath(relativeFileName);
	    }
	}

	for (String folderInFolder : folders) {
	    String childFolder = folder + "/" + folderInFolder;
	    addFiles(childFolder, files);
	}

	return files;
    }

    public String getRootFolder() {
	return rootFolder;
    }
    
    public InputStream getInputStream(FileInfo fileInfo) throws Exception {
	return sftp.get(fileInfo.getAbsolutePath());
    }




}
