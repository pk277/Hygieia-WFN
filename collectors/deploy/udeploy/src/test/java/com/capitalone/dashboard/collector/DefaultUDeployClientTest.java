package com.capitalone.dashboard.collector;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import com.capitalone.dashboard.model.Environment;
import com.capitalone.dashboard.model.UDeployApplication;
import com.capitalone.dashboard.model.UDeployEnvResCompData;
import com.capitalone.dashboard.util.Supplier;

@RunWith(MockitoJUnitRunner.class)
public class DefaultUDeployClientTest {
    @Mock private Supplier<RestOperations> restOperationsSupplier;
    @Mock private RestOperations rest;
    @Mock private UDeploySettings settings;

    private DefaultUDeployClient defaultUDeployClient;

//    private static final String URL = "URL";


    @Before
    public void init() {
        when(restOperationsSupplier.get()).thenReturn(rest);
        settings = new UDeploySettings();
        defaultUDeployClient = new DefaultUDeployClient(settings, restOperationsSupplier);
    }
    
    @Test
    public void testGetApplications() throws Exception {
        String appJson = getJson("application.json");

        String instanceUrl = "http://cdlorchestration.nj.adp.com/oo/";
        String appListUrl = "http://cdlorchestration.nj.adp.com/oo/rest/v1/executions/";

        when(rest.exchange(eq(appListUrl), eq(HttpMethod.GET), Matchers.any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(appJson, HttpStatus.OK));
        List<UDeployApplication> apps = defaultUDeployClient.getExecutions(instanceUrl);
        System.out.println("apps.size"+apps.size());
     //   assertThat(apps.size(), is(1));
     //   assertThat(apps.get(0).getExecutionId(), is("12566602968"));
      //  assertThat(apps.get(0).getExecutionName(), is("WFN-MEGAFLOW"));
    }

    @Test
    public void testGetEnvironments() throws Exception {
        String appJson = getJson("application.json");

        String instanceUrl = "http://cdlorchestration.nj.adp.com/oo/";
        String appListUrl = "http://cdlorchestration.nj.adp.com/oo/rest/v1/executions/";

        when(rest.exchange(eq(appListUrl), eq(HttpMethod.GET), Matchers.any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(appJson, HttpStatus.OK));
        List<UDeployApplication> apps = defaultUDeployClient.getExecutions(instanceUrl);

        String environments = getJson("environments.json");
        String envUrl = "http://cdlorchestration.nj.adp.com/oo/rest/v1/executions/"+apps.get(0).getExecutionId()+"/execution-log/";

        when(rest.exchange(eq(envUrl), eq(HttpMethod.GET), Matchers.any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(environments, HttpStatus.OK));

        List<Environment> envs = defaultUDeployClient.getEnvironments(apps.get(0));
        System.out.println("envs.size"+envs.size());
        //assertThat(envs.size(), is(1));
      //  assertThat(envs.get(0).getVersion(), is("WFN13"));
      //  assertThat(envs.get(0).getEnvName(), is("AUT1"));
    }



    @Test
    public void testGetEnvironmentResourceStatusData() throws Exception {
        String appJson = getJson("application.json");
        
      //  String instanceUrl = "http://cdlorchestration.nj.adp.com/oo/";
        String appListUrl = "http://cdlorchestration.nj.adp.com/oo/rest/v1/executions/";

        when(rest.exchange(eq(appListUrl), eq(HttpMethod.GET), Matchers.any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(appJson, HttpStatus.OK));
      //  List<HPOODeployApplication> apps = defaultUDeployClient.getExecutions(instanceUrl);

        String resourceUrl = "http://cdlorchestration.nj.adp.com/oo/rest/v1/executions/12564238548/execution-log/";


        String environments = getJson("environments.json");
        String envUrl = "http://cdlorchestration.nj.adp.com/oo/rest/v1/executions/12564238548/execution-log/";

        when(rest.exchange(eq(envUrl), eq(HttpMethod.GET), Matchers.any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(environments, HttpStatus.OK));

     //   List<Environment> envs = defaultUDeployClient.getEnvironments(apps.get(0));


        when(rest.exchange(eq(resourceUrl), eq(HttpMethod.GET), Matchers.any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(environments, HttpStatus.OK));

     //   List<HPOODeployEnvResCompData> data = defaultUDeployClient.getEnvironmentResourceStatusData(apps.get(0), envs.get(0));


       // assertThat(data.size(), is(1));
       // assertThat(data.get(0).getComponentName(), is("WFN-MEGAFLOW"));
        //assertThat(data.get(0).getResourceName(), is("WFN-MEGAFLOW"));
        //assertThat(data.get(0).isDeployed(), is(true));
      //  assertThat(data.get(0).isOnline(), is(false));
      //  assertThat(data.get(0).getEnvironmentName(), is("AUT1"));

    }


    private String getJson(String fileName) throws IOException {
        InputStream inputStream = DefaultUDeployClientTest.class.getResourceAsStream(fileName);
        return IOUtils.toString(inputStream);
    }
}