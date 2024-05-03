package com.jeltechnologies.photos.config.yaml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FeedsConfiguration implements Serializable {
    private static final long serialVersionUID = -5590766557149587307L;
    
    private List<SFTPClientAccount> sftpclients = new ArrayList<SFTPClientAccount>();
    
    private List<SFTPServerAccount> sftpservers = new ArrayList<SFTPServerAccount>();
    
    public List<SFTPClientAccount> getSftpclients() {
        return sftpclients;
    }

    public void setSftpclients(List<SFTPClientAccount> sftpaccounts) {
        this.sftpclients = sftpaccounts;
    }
    
    public List<SFTPServerAccount> getSftpservers() {
        return sftpservers;
    }

    public void setSftpservers(List<SFTPServerAccount> sftservers) {
        this.sftpservers = sftservers;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("FeedsConfiguration [sftpclients=");
	builder.append(sftpclients);
	builder.append(", sftpservers=");
	builder.append(sftpservers);
	builder.append("]");
	return builder.toString();
    }


}
