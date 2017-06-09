package com.capitalone.dashboard.model;

public class UDeployApplication extends CollectorItem {
    private static final String INSTANCE_URL = "instanceUrl";
    private String executionId = "executionId";
    private String executionName = "executionName";

    public String getInstanceUrl() {
        return (String) getOptions().get(INSTANCE_URL);
    }

    public void setInstanceUrl(String instanceUrl) {
        getOptions().put(INSTANCE_URL, instanceUrl);
    }
  
	public String getExecutionId() {
		return executionId;
	}

	public void setExecutionId(String runId) {
		executionId = runId;
	}

	public String getExecutionName() {
		return executionName;
	}

	public void setExecutionName(String runName) {
		executionName = runName;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UDeployApplication that = (UDeployApplication) o;
        return getExecutionId().equals(that.getExecutionId()) && getInstanceUrl().equals(that.getInstanceUrl());
    }

    @Override
    public int hashCode() {
        int result = getInstanceUrl().hashCode();
        result = 31 * result + getExecutionId().hashCode();
        return result;
    }
}
