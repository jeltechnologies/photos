package com.jeltechnologies.photos.db;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.pictures.Photo;

public interface QuerySupport {
    public List<Photo> query(Query query) throws SQLException;
    public LocalDate getEarliestDayTaken(User user) throws SQLException;
    public void close();
}
