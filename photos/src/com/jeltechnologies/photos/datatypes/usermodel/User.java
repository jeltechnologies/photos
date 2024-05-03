package com.jeltechnologies.photos.datatypes.usermodel;

import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.pictures.Photo;

public record User(String name, @JsonIgnore List<Role> roles) {
    
    public User {
	roles = List.copyOf(roles);
    }
    
    @JsonIgnore
    public boolean isAdmin() {
	boolean admin = false;
	Iterator<Role> iterator = roles.iterator();
	while (!admin && iterator.hasNext()) {
	    Role current = iterator.next();
	    admin = current.admin();
	}
	return admin;
    }
    
    public static boolean inUserAlbum(Photo photo) {
	String relativeFolderName = photo.getRelativeFolderName();
	boolean inAlbums = relativeFolderName != null && relativeFolderName.startsWith(Environment.INSTANCE.getRelativeRootAlbums());
	return inAlbums;
    }
    
    public static boolean inUserAlbum(String relativeFolderName) {
	boolean inAlbums = relativeFolderName != null && relativeFolderName.startsWith(Environment.INSTANCE.getRelativeRootAlbums());
	return inAlbums;
    }
}
