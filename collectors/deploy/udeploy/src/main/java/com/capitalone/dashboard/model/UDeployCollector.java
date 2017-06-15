package com.capitalone.dashboard.model;

import java.util.ArrayList;
import java.util.List;

public class UDeployCollector extends Collector {
    private List<String> uDeployServers = new ArrayList<>();
   // private List<String> niceNames = new ArrayList<>();

    public List<String> getUDeployServers() {
        return uDeployServers;
    }
    
  /*  public List<String> getNiceNames() {
    	return niceNames;
    }
*/
    public static UDeployCollector prototype(List<String> servers) {
        UDeployCollector protoType = new UDeployCollector();
        protoType.setName("HPOO");
        protoType.setCollectorType(CollectorType.Deployment);
        protoType.setOnline(true);
        protoType.setEnabled(true);
        protoType.getUDeployServers().addAll(servers);
       
        return protoType;
    }
}
