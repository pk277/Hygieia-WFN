package com.capitalone.dashboard.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.capitalone.dashboard.model.EnvironmentStatus;

/**
 * {@link EnvironmentStatus} repository.
 */
public interface EnvironmentStatusRepository extends CrudRepository<EnvironmentStatus, ObjectId> {

    /**
     * Finds all {@link EnvironmentStatus}es for a given {@link com.capitalone.dashboard.model.CollectorItem}.
     *
     * @param collectorItemId collector item id
     * @return list of {@link EnvironmentStatus}es.
     */
    List<EnvironmentStatus> findByCollectorItemId(ObjectId collectorItemId);
    
    @Query(value="{environmentName : ?0}")
    List<EnvironmentStatus> findStatusByEnvName(String envName);
}
