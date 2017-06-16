package com.capitalone.dashboard.collector;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import com.capitalone.dashboard.model.Environment;
import com.capitalone.dashboard.model.EnvironmentComponent;
import com.capitalone.dashboard.model.UDeployApplication;
import com.capitalone.dashboard.model.UDeployEnvResCompData;
import com.capitalone.dashboard.util.Supplier;

@Component
public class DefaultUDeployClient implements UDeployClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUDeployClient.class);

	private final UDeploySettings uDeploySettings;
	private final RestOperations restOperations;

	private List<String> envList = new ArrayList<String>();

	@Autowired
	public DefaultUDeployClient(UDeploySettings uDeploySettings, Supplier<RestOperations> restOperationsSupplier) {
		this.uDeploySettings = uDeploySettings;
		this.restOperations = restOperationsSupplier.get();
	}

	@Override
	public List<UDeployApplication> getExecutions(String instanceUrl) {
		List<UDeployApplication> applications = new ArrayList<>();

		for (Object item : paresAsArray(makeRestCall(instanceUrl, "v1/executions/"))) {
			JSONObject jsonObject = (JSONObject) item;
			if (str(jsonObject, "executionName").contains("WFN-MEGAFLOW")) {
				UDeployApplication application = new UDeployApplication();
				application.setInstanceUrl(instanceUrl);
				application.setExecutionName(str(jsonObject, "executionName"));
				application.setExecutionId(str(jsonObject, "executionId"));
				applications.add(application);
			}
		}
		return applications;
	}

	@Override
	public List<Environment> getEnvironments(UDeployApplication application) {
		List<Environment> environment = new ArrayList<>();

		String url = "v1/executions/" + application.getExecutionId() + "/execution-log/";
		LOGGER.info("execution id is " + application.getExecutionId());

		ResponseEntity<String> response = makeRestCall(application.getInstanceUrl(), url);
		String newResponse = "[" + response.getBody() + "]";
		JSONArray array;
		try {
			array = (JSONArray) new JSONParser().parse(newResponse);

			JSONObject jsonObject = (JSONObject) array.get(0);
			JSONArray flowVarsArray = getLowestLevelChildren(jsonObject, new JSONArray());
			String envVer = str((JSONObject) flowVarsArray.get(0), "value");
			String envName = str((JSONObject) flowVarsArray.get(1), "value");
			if (!envList.contains(envVer+" "+envName)) {
				envList.add(envVer+" "+envName);
				environment.add(new Environment(envVer, envVer+" "+envName));
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			LOGGER.info("ParseException is " + e.getMessage());
		}
		return environment;
	}

	@Override
	public List<UDeployEnvResCompData> getEnvironmentResourceStatusData(UDeployApplication application,
			Environment environment) {

		List<UDeployEnvResCompData> environmentStatuses = new ArrayList<>();
		String url = "v1/executions/" + application.getExecutionId() + "/execution-log/";
		ResponseEntity<String> allResourceResponse = makeRestCall(application.getInstanceUrl(), url);
		String newResponse = "[" + allResourceResponse.getBody() + "]";
		JSONArray array;
		try {
			array = (JSONArray) new JSONParser().parse(newResponse);
			// JSONArray allResourceJSON = paresAsArray(allResourceResponse);
			JSONObject jsonObject = (JSONObject) array.get(0);
			environmentStatuses.add(buildUdeployEnvResCompData(environment, application,
					(JSONObject) jsonObject.get("executionSummary")));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			LOGGER.info("ParseException is " + e.getMessage());
		}
		return environmentStatuses;
	}

	@Override
	public List<EnvironmentComponent> getEnvironmentComponents(UDeployApplication application,
			Environment environment) {
		List<EnvironmentComponent> components = new ArrayList<>();
		LOGGER.info("execution id is " + application.getExecutionId());
		String url = "v1/executions/" + application.getExecutionId() + "/execution-log/";
		ResponseEntity<String> allResourceResponse = makeRestCall(application.getInstanceUrl(), url);
		String newResponse = "[" + allResourceResponse.getBody() + "]";
		JSONArray array;
		try {
			array = (JSONArray) new JSONParser().parse(newResponse);
			JSONObject jsonObject = (JSONObject) array.get(0);

			EnvironmentComponent component = new EnvironmentComponent();
			component.setEnvironmentID(environment.getVersion());
			component.setEnvironmentName(environment.getEnvName());
			component.setEnvironmentUrl("");
			component.setComponentID(application.getExecutionId());
			component.setComponentName(environment.getEnvName());
			component.setComponentVersion(environment.getVersion());
			component.setAsOfDate(date(jsonObject, "endDate"));
			JSONObject object = (JSONObject) jsonObject.get("executionSummary");
			String finalStatus = str(object, "status");
			String flowStatus = str(object, "resultStatusType");
			if ("COMPLETED".equals(finalStatus) && "RESOLVED".equals(flowStatus)) {
				component.setDeployed(true);
			} else {
				component.setDeployed(false);
			}
			components.add(component);

		} catch (Exception e) {
			LOGGER.info("No Environment data found, No components deployed");
		}

		return components;
	}

	private UDeployEnvResCompData buildUdeployEnvResCompData(Environment environment, UDeployApplication application,
			JSONObject jsonObject) {
		UDeployEnvResCompData data = new UDeployEnvResCompData();
		data.setEnvironmentName(environment.getEnvName());
		data.setCollectorItemId(application.getId());
		data.setComponentVersion(environment.getVersion());
		data.setAsOfDate(date(jsonObject, "endTime"));
		String finalStatus = str(jsonObject, "status");
		String flowStatus = str(jsonObject, "resultStatusType");

		if ("COMPLETED".equals(finalStatus) && "RESOLVED".equals(flowStatus)) {
			data.setDeployed(true);
		} else {
			data.setDeployed(false);
		}
		data.setComponentName(environment.getEnvName());
		// need to check this
		data.setOnline(data.isDeployed());
		data.setResourceName(application.getExecutionName());

		return data;
	}

	@SuppressWarnings("unchecked")
	private JSONArray getLowestLevelChildren(JSONObject topParent, JSONArray returnArray) {
		JSONArray jsonFlowVars = (JSONArray) topParent.get("flowVars");

		if (jsonFlowVars != null && jsonFlowVars.size() > 0) {
			for (Object flowVar : jsonFlowVars) {
				if (null != ((JSONObject) flowVar)) {
					returnArray.add(flowVar);
				} else {
					getLowestLevelChildren((JSONObject) flowVar, returnArray);
				}
			}
		}
		return returnArray;
	}

	private ResponseEntity<String> makeRestCall(String instanceUrl, String endpoint) {
		String url = normalizeUrl(instanceUrl, "/rest/" + endpoint);
		ResponseEntity<String> response = null;
		try {
			/*
			 * response = new ResponseEntity<>(
			 * getJson(endpoint.equals("v1/executions/") ? "application.json" :
			 * "environments.json"), HttpStatus.OK);
			 */
			response = restOperations.exchange(url, HttpMethod.GET, new HttpEntity<>(createHeaders()), String.class);

		} catch (RestClientException re) {
			LOGGER.error("Error with REST url: " + url);
			LOGGER.error(re.getMessage());
			/*
			 * } catch (IOException e) { // TODO Auto-generated catch block
			 * e.printStackTrace();
			 */
		}
		return response;
	}

	/*
	 * private String getJson(String fileName) throws IOException { InputStream
	 * inputStream =
	 * DefaultHPOODeployClientTest.class.getResourceAsStream(fileName); return
	 * IOUtils.toString(inputStream); }
	 */

	private String normalizeUrl(String instanceUrl, String remainder) {
		return StringUtils.removeEnd(instanceUrl, "/") + remainder;
	}

	protected HttpHeaders createHeaders() {
		String auth = uDeploySettings.getUsername() + ":" + uDeploySettings.getPassword();
		byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.US_ASCII));
		String authHeader = "Basic " + new String(encodedAuth);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", authHeader);
		return headers;
	}

	private JSONArray paresAsArray(ResponseEntity<String> response) {
		if (response == null)
			return new JSONArray();
		try {
			return (JSONArray) new JSONParser().parse(response.getBody());
		} catch (ParseException pe) {
			LOGGER.debug(response.getBody());
			LOGGER.error(pe.getMessage());
		}
		return new JSONArray();
	}

	private String str(JSONObject json, String key) {
		Object value = json.get(key);
		return value == null ? null : value.toString();
	}

	private long date(JSONObject jsonObject, String key) {
		Object value = jsonObject.get(key);
		return value == null ? 0 : (long) value;
	}
}
