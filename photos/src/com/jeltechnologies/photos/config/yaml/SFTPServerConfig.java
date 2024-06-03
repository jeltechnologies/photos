package com.jeltechnologies.photos.config.yaml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SFTPServerConfig implements Serializable {
    private static final long serialVersionUID = 854796427088153793L;
    private int port;
    private List<SFTPServerUser> users = new ArrayList<SFTPServerUser>();
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public List<SFTPServerUser> getUsers() {
        return users;
    }
    
    public void setUsers(List<SFTPServerUser> users) {
        this.users = users;
    }

    @Override
    public String toString() {
	return "SFTPServerConfig [port=" + port + ", users=" + users + "]";
    }
}
