package com.capitalone.dashboard.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;

import com.capitalone.dashboard.model.UDeployApplication;

/**
 * Repository for {@link UDeployApplication}s.
 */
public interface UDeployApplicationRepository extends BaseCollectorItemRepository<UDeployApplication> {

    /**
     * Find a {@link UDeployApplication} by UDeploy instance URL and UDeploy application id.
     *
     * @param collectorId ID of the {@link com.UDeployCollector.wfn.devops.dashboard.model.HPOODeployCollector}
     * @param string UDeploy application ID
     * @return a {@link UDeployApplication} instance
     */
    @Query(value="{ 'collectorId' : ?0, executionId : ?1}")
    UDeployApplication findHPOODeployApplication(ObjectId collectorId, String string);

    /**
     * Finds all {@link UDeployApplication}s for the given instance URL.
     *
     * @param collectorId ID of the {@link com.UDeployCollector.wfn.devops.dashboard.model.HPOODeployCollector}
     * @param instanceUrl UDeploy instance URl
     * @return list of {@link UDeployApplication}s
     */
    @Query(value="{ 'collectorId' : ?0, enabled: true}")
    List<UDeployApplication> findEnabledApplications(ObjectId collectorId);
}
