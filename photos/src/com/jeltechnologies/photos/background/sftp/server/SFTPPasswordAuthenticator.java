package com.jeltechnologies.photos.background.sftp.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;

import com.jeltechnologies.photos.background.sftp.server.SFTPServer.User;

public class SFTPPasswordAuthenticator implements PasswordAuthenticator {
    private final List<User> accounts =  new ArrayList<User>();
    
    public SFTPPasswordAuthenticator(List<User> accounts) {
	for (User account : accounts) {
	    this.accounts.add(account);
	}
    }
    
    @Override
    public boolean authenticate(String username, String password, ServerSession session) throws PasswordChangeRequiredException {
	User found = null;
	Iterator<User> iterator = accounts.iterator();
	while (found == null && iterator.hasNext()) {
	    User current = iterator.next();
	    if (current.user().equals(username) && current.password().equals(password)) {
		found = current;
	    }
	}
	return found != null;
    }
}
