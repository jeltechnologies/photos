package com.jeltechnologies.photos.background.sftp.client;

import java.util.Date;

import com.jeltechnologies.photos.pictures.MediaType;

public record FileInfo(String name, String absolutePath, String relativePath, Date lastModifiedDate, long size, MediaType type) {
}
