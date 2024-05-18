package com.jeltechnologies.photos.picures.share;

import java.time.LocalDateTime;

public record SharedFile(String uuid, String photoId, LocalDateTime creationDate, LocalDateTime expirationDate, String username) {
}
