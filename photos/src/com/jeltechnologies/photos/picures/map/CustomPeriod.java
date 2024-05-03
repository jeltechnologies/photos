package com.jeltechnologies.photos.picures.map;

import java.time.LocalDate;

import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.utils.JSONUtilsFactory;

import jakarta.servlet.http.HttpServletRequest;

public class CustomPeriod {
    public static String getEarliestPhoto(HttpServletRequest request) throws Exception {
	User user = RoleModel.getUser(request);
	Database db = null;
	try {
	    db = new Database();
	    LocalDate earliestDate = db.getEarliestDayTaken(user);
	    String json = JSONUtilsFactory.getInstance().toJSON(earliestDate);
	    return json;
	}
	finally {
	    if (db != null) {
		db.close();
	    }
	}
    }
}
