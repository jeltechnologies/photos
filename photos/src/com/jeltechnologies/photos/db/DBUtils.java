package com.jeltechnologies.photos.db;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DBUtils.class);
    
    public static void close(Statement statement) {
	if (statement != null) {
	    try {
		statement.close();
	    } catch (SQLException e) {
		LOGGER.error("Cannot close Statemement", e);
	    }
	}
    }

    public static void close(ResultSet rs) {
	if (rs != null) {
	    try {
		rs.close();
	    } catch (SQLException e) {
		LOGGER.error("Cannot close ResultSet", e);
	    }
	}
    }

    @Deprecated
    public static void close(Connection connection) {
	if (connection != null) {
	    try {
		connection.close();
	    } catch (SQLException e) {
		LOGGER.error("Cannot close connection", e);
	    }
	}
    }

    public static LocalDateTime getDateTime(ResultSet rs, int columnIndex) throws SQLException {
	LocalDateTime localDateTime = rs.getObject(columnIndex, LocalDateTime.class);
	return localDateTime;
    }
    
    public static LocalDate getDate(ResultSet rs, int columnIndex) throws SQLException {
	LocalDate localDate = rs.getObject(columnIndex, LocalDate.class);
	return localDate;
    }
    
    public static BigDecimal getBigDecimal(ResultSet rs, int columnIndex) throws SQLException {
	BigDecimal bigDecimal = rs.getBigDecimal(columnIndex);
	return bigDecimal;
    }

    public static void setTimestamp(PreparedStatement st, int parameterIndex, LocalDateTime localDateTime) throws SQLException {
	st.setObject(parameterIndex, localDateTime);
    }
    
    public static void setBigDecimal(PreparedStatement st, int parameterIndex, BigDecimal bigDecimal) throws SQLException {
	if (bigDecimal != null) {
	    bigDecimal.setScale(16, RoundingMode.HALF_UP);
	    st.setBigDecimal(parameterIndex, bigDecimal);
	} else {
	    st.setNull(parameterIndex, Types.NUMERIC);
	}
    }
    
    public static void setString(PreparedStatement st, int parameterIndex, String s) throws SQLException {
	if (s != null) {
	    st.setString(parameterIndex, s);
	} else {
	    st.setNull(parameterIndex, Types.VARCHAR);
	}
    }
    
    public static void setIfNotNull(PreparedStatement st, int parameterIndex, String s) throws SQLException {
	if (s != null) {
	    st.setString(parameterIndex, s);
	} 
    }
  

}
