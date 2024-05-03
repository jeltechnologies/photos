package com.jeltechnologies.photos.background.sftp.client;

public interface SyncThreadMBean {
    String getStatus();
    int getFilesOnNAS();
}
