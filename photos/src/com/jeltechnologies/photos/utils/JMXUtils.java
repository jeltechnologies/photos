package com.jeltechnologies.photos.utils;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMXUtils {
    private static Logger LOGGER = LoggerFactory.getLogger(JMXUtils.class);

    private Map<String, ObjectName> registeredMBeans = new HashMap<String, ObjectName>();

    private static JMXUtils INSTANCE;

    private static final String MBEAN_NAME_PREFIX = "com.jeltechnologies.photos:type=";
    
    private JMXUtils() {
    }

    public static synchronized JMXUtils getInstance() {
	if (INSTANCE == null) {
	    INSTANCE = new JMXUtils();
	}
	return INSTANCE;
    }

    public void registerMBean(String name, String type, Object bean) {
	if (registeredMBeans.get(name) != null) {
	    throw new IllegalStateException("Already registered another MBEan with name " + name);
	}
	MBeanServer server = ManagementFactory.getPlatformMBeanServer();
	ObjectName objectName;
	try {
	    String objectNameString = MBEAN_NAME_PREFIX + type + ",name=" + name;
	    objectName = new ObjectName(objectNameString);
	    server.registerMBean(bean, objectName);
	    registeredMBeans.put(name, objectName);
	} catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException
		| NotCompliantMBeanException e) {
	    LOGGER.error("Cannot register MBean", e);
	}
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("MBean registered: " + name); 
	}
    } 

    public void unregisterMBean(String name) {
	ObjectName objectName = this.registeredMBeans.get(name);
	if (objectName != null) {
	    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
	    try {
		server.unregisterMBean(objectName);
		this.registeredMBeans.remove(name);
		if (LOGGER.isTraceEnabled()) {
		    LOGGER.trace("MBean unregistered: " + name);
		}
	    } catch (MBeanRegistrationException | InstanceNotFoundException e) {
		LOGGER.error("Cannot unregister MBean", e);
	    }
	} else {
	    LOGGER.warn("Cannot find ObjectName as registered [" + objectName + "]");
	}
    }

    public void unregisterAllMBeans() {
	MBeanServer server = ManagementFactory.getPlatformMBeanServer();
	for (String name : this.registeredMBeans.keySet()) {
	    ObjectName objectName = this.registeredMBeans.get(name);
	    try {
		server.unregisterMBean(objectName);
 	    } catch (MBeanRegistrationException | InstanceNotFoundException e) {
		LOGGER.error("Cannot unregister MBean", e);
	    }
	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace("MBean unregistered: " + objectName.toString());
	    }
	} 
    }

}

