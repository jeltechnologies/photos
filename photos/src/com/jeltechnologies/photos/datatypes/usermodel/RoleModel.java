package com.jeltechnologies.photos.datatypes.usermodel;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.PageContext;

public class RoleModel {
    public static final Role ROLE_USER = new Role("photos-user", false);
    public static final Role ROLE_ADMIN = new Role("photos-admin", true);
    public static final Role[] ROLES = { ROLE_USER, ROLE_ADMIN };
    
    private static final List<Role> USER_ROLES;
    static {
	USER_ROLES = new ArrayList<Role>();
	USER_ROLES.add(ROLE_USER);
    }
    
    private static final List<Role> ADMIN_ROLES;
    static {
	ADMIN_ROLES = new ArrayList<Role>();
	ADMIN_ROLES.add(ROLE_ADMIN);
	ADMIN_ROLES.add(ROLE_USER);
    }
    
    private static final User USER_SYSADMIN = new User("user", ADMIN_ROLES);
    private static final User USER_NORMAL = new User("admin", USER_ROLES);
    
    public static User getSystemAdmin() {
	return USER_SYSADMIN;
    }
    
    public static User getSystemUser() {
	return USER_NORMAL;
    }
    
    public static Role getRole(String name) {
	Role found = null;
	for (Role current : ROLES) {
	    if (current.name().equals(name)) {
		found = current;
		break;
	    }
	}
	return found;
    }

    public static User getUser(HttpServletRequest request) {
	List<Role> photoAppRoles = new ArrayList<Role>();
	for (Role role : ROLES) {
	    if (request.isUserInRole(role.name())) {
		photoAppRoles.add(role);
	    }
	}
	String name = request.getUserPrincipal().getName();
	User user = new User(name, photoAppRoles);
	return user;
    }

    public static User getUser(PageContext pageContext) {
	return getUser((HttpServletRequest) pageContext.getRequest());
    }
}
