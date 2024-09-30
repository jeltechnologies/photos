package com.jeltechnologies.photos.servlet;

import java.util.List;

import com.jeltechnologies.photos.pictures.Photo;

public record GalleryResponse (String mapkey, List<Photo> payload) {}
