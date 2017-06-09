package com.capitalone.dashboard.model;

/**
 * Represents a UDeploy environment by ID and name.
 */
public class Environment {
    private String version;
    private String envName;
    
	public Environment(String version, String envName) {
		super();
		this.version = version;
		this.envName = envName;
	}
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getEnvName() {
		return envName;
	}
	public void setEnvName(String envName) {
		this.envName = envName;
	}

    
}
