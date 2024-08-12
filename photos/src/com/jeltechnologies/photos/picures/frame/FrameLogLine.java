package com.jeltechnologies.photos.picures.frame;

import java.time.LocalDateTime;

public class FrameLogLine {
    private String message;
    private String program;
    private int percentage;
    private String id;
    private LocalDateTime timestamp;
    private String user;
    private String sessionId;

    public LocalDateTime getTimestamp() {
	return timestamp;
    }

    public String getUser() {
	return user;
    }

    public void setUser(String user) {
	this.user = user;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public String getMessage() {
	return message;
    }

    public void setMessage(String message) {
	this.message = message;
    }

    public String getProgram() {
	return program;
    }

    public void setProgram(String program) {
	this.program = program;
    }

    public int getPercentage() {
	return percentage;
    }

    public void setPercentage(int percentage) {
	this.percentage = percentage;
    }

    public String getSessionId() {
	return sessionId;
    }

    public void setSessionId(String sessionId) {
	this.sessionId = sessionId;
    }

    public void setTimestamp(LocalDateTime timestamp) {
	this.timestamp = timestamp;
    }

    @Override
    public String toString() {
	return "FrameLogLine [message=" + message + ", program=" + program + ", percentage=" + percentage + ", id=" + id + ", timestamp=" + timestamp
		+ ", user=" + user + ", sessionId=" + sessionId + "]";
    }

}
