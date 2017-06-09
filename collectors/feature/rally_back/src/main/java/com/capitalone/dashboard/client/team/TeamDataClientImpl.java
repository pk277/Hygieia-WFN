package com.capitalone.dashboard.client.team;

import com.atlassian.rally.rest.client.api.domain.BasicProject;
import com.capitalone.dashboard.client.RallyClient;
import com.capitalone.dashboard.model.ScopeOwnerCollectorItem;
import com.capitalone.dashboard.repository.FeatureCollectorRepository;
import com.capitalone.dashboard.repository.ScopeOwnerRepository;
import com.capitalone.dashboard.util.ClientUtil;
import com.capitalone.dashboard.util.FeatureCollectorConstants;
import com.capitalone.dashboard.util.FeatureSettings;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This is the primary implemented/extended data collector for the feature
 * collector. This will get data from the source system, but will grab the
 * majority of needed data and aggregate it in a single, flat MongoDB collection
 * for consumption.
 * 
 * @author kfk884
 * 
 */
public class TeamDataClientImpl implements TeamDataClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(TeamDataClientImpl.class);
	private static final ClientUtil TOOLS = ClientUtil.getInstance();

	private final FeatureSettings featureSettings;
	private final ScopeOwnerRepository teamRepo;
	private final FeatureCollectorRepository featureCollectorRepository;
	private final RallyClient rallyClient;

	/**
	 * Extends the constructor from the super class.
	 * 
	 * @param teamRepository
	 */
	public TeamDataClientImpl(FeatureCollectorRepository featureCollectorRepository, FeatureSettings featureSettings, 
			ScopeOwnerRepository teamRepository, RallyClient rallyClient) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Constructing data collection for the feature widget, team-level data...");
		}

		this.featureSettings = featureSettings;
		this.featureCollectorRepository = featureCollectorRepository;
		this.teamRepo = teamRepository;
		this.rallyClient = rallyClient;
	}
	
	/**
	 * Explicitly updates queries for the source system, and initiates the
	 * update to MongoDB from those calls.
	 */
	public int updateTeamInformation() {
		int count = 0;
		
		List<BasicProject> projects = rallyClient.getProjects();
		
		if (projects != null && !projects.isEmpty()) {
			updateMongoInfo(projects);
			count += projects.size();
		}
		
		return count;
	}

	/**
	 * Updates the MongoDB with a JSONArray received from the source system
	 * back-end with story-based data.
	 * 
	 * @param currentPagedRallyRs
	 *            A list response of Rally issues from the source system
	 */
	private void updateMongoInfo(List<BasicProject> currentPagedRallyRs) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Size of paged Rally response: " + (currentPagedRallyRs == null? 0 : currentPagedRallyRs.size()));
		}
		
		if (currentPagedRallyRs != null) {
			ObjectId rallyCollectorId = featureCollectorRepository.findByName(FeatureCollectorConstants.JIRA).getId();
			
			for (BasicProject rallyTeam : currentPagedRallyRs) {
				String teamId = TOOLS.sanitizeResponse(rallyTeam.getId());
				
				/*
				 * Initialize DOMs
				 */
				ScopeOwnerCollectorItem team = findOneScopeOwnerCollectorItem(teamId);
				
				if (team == null) {
					team = new ScopeOwnerCollectorItem();
				}

				// collectorId
				team.setCollectorId(rallyCollectorId);

				// teamId
				team.setTeamId(TOOLS.sanitizeResponse(rallyTeam.getId()));

				// name
				team.setName(TOOLS.sanitizeResponse(rallyTeam.getName()));

				// changeDate - does not exist for rally
				team.setChangeDate("");

				// assetState - does not exist for rally
				team.setAssetState("Active");

				// isDeleted - does not exist for rally
				team.setIsDeleted("False");

				// Saving back to MongoDB
				teamRepo.save(team);
			}
		}
	}
	
	/**
	 * Retrieves the maximum change date for a given query.
	 * 
	 * @return A list object of the maximum change date
	 */
	public String getMaxChangeDate() {
		String data = null;

		try {
			List<ScopeOwnerCollectorItem> response = teamRepo.findTopByChangeDateDesc(
					featureCollectorRepository.findByName(FeatureCollectorConstants.JIRA).getId(),
					featureSettings.getDeltaCollectorItemStartDate());
			if ((response != null) && !response.isEmpty()) {
				data = response.get(0).getChangeDate();
			}
		} catch (Exception e) {
			LOGGER.error("There was a problem retrieving or parsing data from the local "
					+ "repository while retrieving a max change date\nReturning null");
		}

		return data;
	}
	
	/**
	 * Find the current collector item for the rally team id
	 * 
	 * @param teamId	the team id
	 * @return			the collector item if it exists or null
	 */
	private ScopeOwnerCollectorItem findOneScopeOwnerCollectorItem(String teamId) {
		List<ScopeOwnerCollectorItem> scopeOwnerCollectorItems = teamRepo.getTeamIdById(teamId);
		
		// Not sure of the state of the data
		if (scopeOwnerCollectorItems.size() > 1) {
			LOGGER.warn("More than one collector item found for teamId " + teamId);
		}
		
		if (!scopeOwnerCollectorItems.isEmpty()) {
			return scopeOwnerCollectorItems.get(0);
		}
		
		return null;
	}
}