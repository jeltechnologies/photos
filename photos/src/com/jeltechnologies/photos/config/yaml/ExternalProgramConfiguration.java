package com.jeltechnologies.photos.config.yaml;

import java.io.File;
import java.io.Serializable;

public class ExternalProgramConfiguration implements Serializable {
    private static final long serialVersionUID = 412250593532972652L;
    private File executable;

    public File getExecutable() {
        return executable;
    }

    public void setExecutable(File executable) {
        this.executable = executable;
    }
    
    public boolean executableExists() {
	return executable != null && executable.isFile();
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("ExternalProgramConfiguration [executable=");
	builder.append(executable);
	builder.append("]");
	return builder.toString();
    }
    
}
