package com.jeltechnologies.photos.timeline;

import java.time.LocalDate;

import com.jeltechnologies.photos.pictures.Photo;

public record TimelinePhoto(Photo cover,LocalDate from, LocalDate to) 
{
}
