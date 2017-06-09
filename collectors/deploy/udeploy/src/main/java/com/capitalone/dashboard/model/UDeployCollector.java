package com.capitalone.dashboard.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

public class UDeployCollector extends Collector {
    private List<String> uDeployServers = new ArrayList<>();
    private List<String> niceNames = new ArrayList<>();

    public List<String> getUDeployServers() {
        return uDeployServers;
    }
    
    public List<String> getNiceNames() {
    	return niceNames;
    }

    public static UDeployCollector prototype(List<String> servers, List<String> niceNames) {
        UDeployCollector protoType = new UDeployCollector();
        protoType.setName("UDeploy");
        protoType.setCollectorType(CollectorType.Deployment);
        protoType.setOnline(true);
        protoType.setEnabled(true);
        protoType.getUDeployServers().addAll(servers);
        if (!CollectionUtils.isEmpty(niceNames)) {
            protoType.getNiceNames().addAll(niceNames);
        }
        return protoType;
    }
}
