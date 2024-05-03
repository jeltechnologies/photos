package com.jeltechnologies.photos.background.sftp.server;

import java.io.File;

public interface FileChangeListener { 
    void fileChanged(File file);
}
