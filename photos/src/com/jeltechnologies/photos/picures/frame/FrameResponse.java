package com.jeltechnologies.photos.picures.frame;

import java.util.List;

import com.jeltechnologies.photos.pictures.Photo;

public record FrameResponse(String program, int percentage, String description, String mapkey, List<Photo> payload, int sinceHours) {}
