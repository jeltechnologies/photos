package com.jeltechnologies.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.util.OperatingSystemCommandArgument.Quotes;

public class OperatingSystemCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(OperatingSystemCommand.class);
    private final File exe;
    private List<OperatingSystemCommandArgument> arguments = new ArrayList<>();
    private List<String> responseLines;
    private String description;
    private int exitValue = -1;
    private final List<OperatingSystemCommandListener> listeners = new ArrayList<OperatingSystemCommandListener>();
    private Map<String, String> environmentVariables = new HashMap<String, String>();
    private File folder = null;

    public OperatingSystemCommand(File exe) {
	this.exe = exe;
    }

    public OperatingSystemCommand(File exe, Quotes quotes) {
	this.exe = exe;
    }

    public void addArgument(String argument) {
	this.arguments.add(new OperatingSystemCommandArgument(argument));
    }

    public void addArgument(String argument, Quotes quotes) {
	this.arguments.add(new OperatingSystemCommandArgument(argument, quotes));
    }

    public void addListener(OperatingSystemCommandListener listener) {
	this.listeners.add(listener);
    }

    public void setEnvironmentVariable(String variable, String value) {
	environmentVariables.put(variable, value);
    }

    public void setFolder(File folder) {
	this.folder = folder;
    }

    public void execute() throws IOException, InterruptedException {
	responseLines = new ArrayList<>();
	ProcessBuilder processBuilder = new ProcessBuilder();

	List<String> commands = new ArrayList<String>();
	commands.add(exe.getAbsolutePath());
	for (OperatingSystemCommandArgument argument : arguments) {
	    commands.add(argument.toString());
	}

	StringBuilder b = new StringBuilder();
	for (int i = 0; i < commands.size(); i++) {
	    if (i > 0) {
		b.append(" ");
	    }
	    b.append(commands.get(i));
	}
	description = b.toString();

	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Executing " + description);
	}

	if (environmentVariables != null && !environmentVariables.isEmpty()) {
	    Map<String, String> environment = processBuilder.environment();
	    for (String name : environmentVariables.keySet()) {
		environment.put(name, environmentVariables.get(name));
	    }
	}

	if (folder != null) {
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("Executing in folder " + folder);
	    }
	    processBuilder.directory(folder);
	}

	processBuilder.command(commands);

	processBuilder.redirectErrorStream(true);
	Process process;
	process = processBuilder.start();
	BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	String line;
	while ((line = reader.readLine()) != null) {
	    if (Thread.interrupted()) {
		throw new InterruptedException();
	    }
	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace(line);
	    }
	    for (OperatingSystemCommandListener listener : listeners) {
		listener.receivedLine(line);
	    }
	    responseLines.add(line);
	}
	exitValue = process.waitFor();

	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Exit value: " + exitValue);
	}

	if (exitValue != 0) {
	    StringBuilder errorMessage = new StringBuilder();
	    if (responseLines.isEmpty()) {
		errorMessage.append("Exit value: " + exitValue);
	    } else {
		for (String responseLine : responseLines) {
		    errorMessage.append(responseLine).append(" ");
		}
	    }
	    throw new IOException(description + " => " + errorMessage.toString());
	}
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Successfully executed " + description);
	}
    }

    public List<String> getOutput() {
	return responseLines;
    }

    public String getDescription() {
	return description;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("OperatingSystemCommand [description=");
	builder.append(description);
	if (exitValue != -1) {
	    builder.append(", exitValue=");
	    builder.append(exitValue);
	}
	builder.append("]");
	return builder.toString();
    }

}
