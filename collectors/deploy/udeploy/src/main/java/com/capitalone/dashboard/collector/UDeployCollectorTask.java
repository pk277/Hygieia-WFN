package com.capitalone.dashboard.collector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.capitalone.dashboard.model.CollectorItem;
import com.capitalone.dashboard.model.CollectorType;
import com.capitalone.dashboard.model.Environment;
import com.capitalone.dashboard.model.EnvironmentComponent;
import com.capitalone.dashboard.model.EnvironmentStatus;
import com.capitalone.dashboard.model.UDeployApplication;
import com.capitalone.dashboard.model.UDeployCollector;
import com.capitalone.dashboard.model.UDeployEnvResCompData;
import com.capitalone.dashboard.repository.BaseCollectorRepository;
import com.capitalone.dashboard.repository.ComponentRepository;
import com.capitalone.dashboard.repository.EnvironmentComponentRepository;
import com.capitalone.dashboard.repository.EnvironmentStatusRepository;
import com.capitalone.dashboard.repository.UDeployApplicationRepository;
import com.capitalone.dashboard.repository.UDeployCollectorRepository;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Collects {@link EnvironmentComponent} and {@link EnvironmentStatus} data from
 * {@link UDeployApplication}s.
 */
@Component
public class UDeployCollectorTask extends CollectorTask<UDeployCollector> {
    @SuppressWarnings({"unused", "PMD.UnusedPrivateField"})
    private static final Logger LOGGER = LoggerFactory.getLogger(UDeployCollectorTask.class);

    private final UDeployCollectorRepository uDeployCollectorRepository;
    private final UDeployApplicationRepository uDeployApplicationRepository;
    private final UDeployClient uDeployClient;
    private final UDeploySettings uDeploySettings;

    private final EnvironmentComponentRepository envComponentRepository;
    private final EnvironmentStatusRepository environmentStatusRepository;

    private final ComponentRepository dbComponentRepository;

    @Autowired
    public UDeployCollectorTask(TaskScheduler taskScheduler,
                                UDeployCollectorRepository uDeployCollectorRepository,
                                UDeployApplicationRepository uDeployApplicationRepository,
                                EnvironmentComponentRepository envComponentRepository,
                                EnvironmentStatusRepository environmentStatusRepository,
                                UDeploySettings uDeploySettings, UDeployClient uDeployClient,
                                ComponentRepository dbComponentRepository) {
        super(taskScheduler, "HPOO");
        this.uDeployCollectorRepository = uDeployCollectorRepository;
        this.uDeployApplicationRepository = uDeployApplicationRepository;
        this.uDeploySettings = uDeploySettings;
        this.uDeployClient = uDeployClient;
        this.envComponentRepository = envComponentRepository;
        this.environmentStatusRepository = environmentStatusRepository;
        this.dbComponentRepository = dbComponentRepository;
    }

    @Override
    public UDeployCollector getCollector() {
        return UDeployCollector.prototype(uDeploySettings.getServers());
    }

    @Override
    public BaseCollectorRepository<UDeployCollector> getCollectorRepository() {
        return uDeployCollectorRepository;
    }

    @Override
    public String getCron() {
        return uDeploySettings.getCron();
    }

    @Override
    public void collect(UDeployCollector collector) {
        for (String instanceUrl : collector.getUDeployServers()) {

            logBanner(instanceUrl);

            long start = System.currentTimeMillis();

            clean(collector);

            addNewApplications(uDeployClient.getExecutions(instanceUrl),
                    collector);
            
            List<UDeployApplication> list  = enabledApplications(collector, instanceUrl);
            
            if(!list.isEmpty()){
            	updateData(list);
            }
            log("Finished", start);
        }
    }

    /**
     * Clean up unused deployment collector items
     *
     * @param collector the {@link UDeployCollector}
     */
    @SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
    private void clean(UDeployCollector collector) {
        deleteUnwantedJobs(collector);
    	//deleteJobs();
        Set<ObjectId> uniqueIDs = new HashSet<>();
        for (com.capitalone.dashboard.model.Component comp : dbComponentRepository
                .findAll()) {
            if (comp.getCollectorItems() == null || comp.getCollectorItems().isEmpty()) continue;
            List<CollectorItem> itemList = comp.getCollectorItems().get(
                    CollectorType.Deployment);
            if (itemList == null) continue;
            for (CollectorItem ci : itemList) {
                if (ci == null) continue;
                uniqueIDs.add(ci.getId());
            }
        }
        List<UDeployApplication> appList = new ArrayList<>();
        Set<ObjectId> udId = new HashSet< >();
        udId.add(collector.getId());
        for (UDeployApplication app : uDeployApplicationRepository.findByCollectorIdIn(udId)) {
            if (app != null) {
                app.setEnabled(uniqueIDs.contains(app.getId()));
                appList.add(app);
            }
        }
        uDeployApplicationRepository.save(appList);
    }

    private void deleteUnwantedJobs(UDeployCollector collector) {

        List<UDeployApplication> deleteAppList = new ArrayList<>();
        Set<ObjectId> udId = new HashSet<>();
        udId.add(collector.getId());
        for (UDeployApplication app : uDeployApplicationRepository.findByCollectorIdIn(udId)) {
            /*if (!collector.getUDeployServers().contains(app.getInstanceUrl()) ||
                    (!app.getCollectorId().equals(collector.getId()))) {*/
        		log(app.getCollectorId().toString());
                deleteAppList.add(app);
           // }
        }

        uDeployApplicationRepository.delete(deleteAppList);

    }
    
   /* private void deleteJobs() {

        uDeployApplicationRepository.deleteAll();

    }*/

    private List<EnvironmentComponent> getEnvironmentComponent(List<UDeployEnvResCompData> dataList, Environment environment, UDeployApplication application) {
        List<EnvironmentComponent> returnList = new ArrayList<>();
        for (UDeployEnvResCompData data : dataList) {
            EnvironmentComponent component = new EnvironmentComponent();
            component.setComponentName(data.getComponentName());
            component.setCollectorItemId(data.getCollectorItemId());
            component.setComponentVersion(data
                    .getComponentVersion());
            component.setDeployed(data.isDeployed());
            component.setEnvironmentName(data
                    .getEnvironmentName());

            component.setEnvironmentName(environment.getEnvName());
            component.setAsOfDate(data.getAsOfDate());
            //String environmentURL = application.getInstanceUrl();
            component.setEnvironmentUrl(application.getInstanceUrl());
            
            returnList.add(component);
        }
        return returnList;
    }


    private List<EnvironmentStatus> getEnvironmentStatus(List<UDeployEnvResCompData> dataList) {
        List<EnvironmentStatus> returnList = new ArrayList<>();
        for (UDeployEnvResCompData data : dataList) {
            EnvironmentStatus status = new EnvironmentStatus();
            status.setCollectorItemId(data.getCollectorItemId());
            status.setComponentID(data.getComponentID());
            status.setComponentName(data.getComponentName());
            status.setEnvironmentName(data.getEnvironmentName());
            status.setOnline(data.isOnline());
            status.setResourceName(data.getResourceName());

            returnList.add(status);
        }
        return returnList;
    }


    /**
     * For each {@link UDeployApplication}, update the current
     * {@link EnvironmentComponent}s and {@link EnvironmentStatus}.
     *
     * @param uDeployApplications list of {@link UDeployApplication}s
     */
    private void updateData(List<UDeployApplication> uDeployApplications) {
        for (UDeployApplication application : uDeployApplications) {
        	log("uDeployApplications size is "+uDeployApplications.size());
        	log("uDeployApplication is "+uDeployApplications.get(0).getExecutionId());
            List<EnvironmentComponent> compList = new ArrayList<>();
            List<EnvironmentStatus> statusList = new ArrayList<>();
            long startApp = System.currentTimeMillis();
            log("application is "+application.getExecutionId());
            for (Environment environment : uDeployClient
                    .getEnvironments(application)) {

                List<UDeployEnvResCompData> combinedDataList = uDeployClient
                        .getEnvironmentResourceStatusData(application,
                                environment);

                compList.addAll(getEnvironmentComponent(combinedDataList, environment, application));
                statusList.addAll(getEnvironmentStatus(combinedDataList));
            }
            if (!compList.isEmpty()) {
                List<EnvironmentComponent> existingComponents = envComponentRepository
                        .findByCollectorItemId(application.getId());
                envComponentRepository.delete(existingComponents);
                envComponentRepository.save(compList);
            }
            if (!statusList.isEmpty()) {
                List<EnvironmentStatus> existingStatuses = environmentStatusRepository
                        .findByCollectorItemId(application.getId());
                environmentStatusRepository.delete(existingStatuses);
                environmentStatusRepository.save(statusList);
            }

            log(" " + application.getExecutionName(), startApp);
        }
    }

    private List<UDeployApplication> enabledApplications(
            UDeployCollector collector, String instanceUrl) {
        return uDeployApplicationRepository.findEnabledApplications(
                collector.getId(), instanceUrl);
    }

    /**
     * Add any new {@link UDeployApplication}s.
     *
     * @param applications list of {@link UDeployApplication}s
     * @param collector    the {@link UDeployCollector}
     */
    private void addNewApplications(List<UDeployApplication> applications,
                                    UDeployCollector collector) {
        long start = System.currentTimeMillis();
        int count = 0;
        log("All apps", start, applications.size());
        for (UDeployApplication application : applications) {
        	UDeployApplication existing = findExistingApplication(collector, application);

        	//String niceName = getNiceName(application, collector);
            if (existing == null) {
            	log("Inside existing");
                application.setCollectorId(collector.getId());
                application.setEnabled(true);
                application.setDescription(application.getExecutionName());
                /*if (StringUtils.isNotEmpty(niceName)) {
                	application.setNiceName(niceName);
                }*/
                try {
                    uDeployApplicationRepository.save(application);
                } catch (org.springframework.dao.DuplicateKeyException ce) {
                    log("Duplicates items not allowed", 0);

                }
                count++;
            } else  {
            	log("Inside else existing");
            	existing.setEnabled(true);
            	uDeployApplicationRepository.save(existing);
            	//continue;
			//	existing.setNiceName(niceName);
            	//uDeployApplicationRepository.delete(existing);
			//	uDeployApplicationRepository.save(existing);
            }

        }
        log("New apps", start, count);
    }

    private UDeployApplication findExistingApplication(UDeployCollector collector,
                                     UDeployApplication application) {
        return uDeployApplicationRepository.findHPOODeployApplication(
                collector.getId(), application.getInstanceUrl(),
                application.getExecutionId());
    }
    
   /* private String getNiceName(UDeployApplication application, UDeployCollector collector) {
        if (CollectionUtils.isEmpty(collector.getUDeployServers())) return "";
        List<String> servers = collector.getUDeployServers();
        List<String> niceNames = collector.getNiceNames();
        if (CollectionUtils.isEmpty(niceNames)) return "";
        for (int i = 0; i < servers.size(); i++) {
            if (servers.get(i).equalsIgnoreCase(application.getInstanceUrl()) && niceNames.size() > i) {
                return niceNames.get(i);
            }
        }
        return "";
    }*/

    @SuppressWarnings("unused")
	private boolean changed(EnvironmentStatus status, EnvironmentStatus existing) {
        return existing.isOnline() != status.isOnline();
    }

    @SuppressWarnings("unused")
	private EnvironmentStatus findExistingStatus(
            final EnvironmentStatus proposed,
            List<EnvironmentStatus> existingStatuses) {

        return Iterables.tryFind(existingStatuses,
                new Predicate<EnvironmentStatus>() {
                    @Override
                    public boolean apply(EnvironmentStatus existing) {
                        return existing.getEnvironmentName().equals(
                                proposed.getEnvironmentName())
                                && existing.getComponentName().equals(
                                proposed.getComponentName())
                                && existing.getResourceName().equals(
                                proposed.getResourceName());
                    }
                }).orNull();
    }

    @SuppressWarnings("unused")
	private boolean changed(EnvironmentComponent component,
                            EnvironmentComponent existing) {
        return existing.isDeployed() != component.isDeployed()
                || existing.getAsOfDate() != component.getAsOfDate() || !existing.getComponentVersion().equalsIgnoreCase(component.getComponentVersion());
    }

    @SuppressWarnings("unused")
	private EnvironmentComponent findExistingComponent(
            final EnvironmentComponent proposed,
            List<EnvironmentComponent> existingComponents) {

        return Iterables.tryFind(existingComponents,
                new Predicate<EnvironmentComponent>() {
                    @Override
                    public boolean apply(EnvironmentComponent existing) {
                        return existing.getEnvironmentName().equals(
                                proposed.getEnvironmentName())
                                && existing.getComponentName().equals(
                                proposed.getComponentName());

                    }
                }).orNull();
    }
}
