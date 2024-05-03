package com.jeltechnologies.photos.manage.add;

import java.util.List;

import com.jeltechnologies.photos.pictures.Photo;

public record NewPhotosResponse(int month, int year, List<Photo> photos) {};
