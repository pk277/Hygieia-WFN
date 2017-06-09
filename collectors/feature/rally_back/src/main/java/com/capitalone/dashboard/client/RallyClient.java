package com.capitalone.dashboard.client;

import java.util.List;
import java.util.Map;

import com.capitalone.dashboard.model.BasicProject;
import com.capitalone.dashboard.model.Issue;

public interface RallyClient {
	List<Issue> getIssues(long startTime, int pageStart);
	
	List<BasicProject> getProjects();
	
	Issue getEpic(String epicId);
	
	int getPageSize();

	List<Issue> getEpics(List<String> epicKeys);
	
	Map<String, String> getStatusMapping();
}
