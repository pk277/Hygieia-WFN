package com.capitalone.dashboard.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.capitalone.dashboard.util.FeatureSettings;
import com.capitalone.dashboard.util.Supplier;
import com.rallydev.rest.RallyRestApi;

/**
 * Separate RallyRestClient supplier to make unit testing easier
 * 
 * @author <a href="mailto:MarkRx@users.noreply.github.com">MarkRx</a>
 */
@Component
public class RallyRestClientSupplier implements Supplier<RallyRestApi> {
	private static final Logger LOGGER = LoggerFactory.getLogger(RallyRestClientSupplier.class);
	
	@Autowired
	private FeatureSettings featureSettings;
	
	@Override
	public RallyRestApi get() {
		RallyRestApi client = null;
		
		String rallyCredentials = featureSettings.getRallyCredentials();
		String rallyBaseUrl = featureSettings.getRallyBaseUrl();
		String proxyUri = null;
		String proxyPort = null;
		
		URI rallyUri = null;
		
		try {
			if (featureSettings.getRallyProxyUrl() != null && !featureSettings.getRallyProxyUrl().isEmpty() && (featureSettings.getRallyProxyPort() != null)) {
				proxyUri = this.featureSettings.getRallyProxyUrl();
				proxyPort = this.featureSettings.getRallyProxyPort();
				
				rallyUri = this.createRallyConnection(rallyBaseUrl,
						proxyUri + ":" + proxyPort, 
						this.decodeCredentials(rallyCredentials).get("rallyKey"));
			} else {
				rallyUri = new URI(rallyBaseUrl);
			}
			
			InetAddress.getByName(rallyUri.getHost());
			client = new RallyRestApi(new URI(proxyUri+":"+proxyPort), decodeCredentials(rallyCredentials).get("rallyKey"));
			
		} catch (UnknownHostException | URISyntaxException e) {
			LOGGER.error("The Rally host name is invalid. Further rally collection cannot proceed.");
			
			LOGGER.debug("Exception", e);
		}
		
		return client;
	}
	
	/**
	 * Converts rally basic authentication credentials from Base 64 string to a
	 * username/password map
	 * 
	 * @param rallyBasicAuthCredentialsInBase64
	 *            Base64-encoded single string in the following format:
	 *            <em>username:password</em><br/>
	 * <br/>
	 *            A null parameter value will result in an empty hash map
	 *            response (e.g., nothing gets decoded)
	 * @return Decoded username/password map of strings
	 */
	private Map<String, String> decodeCredentials(String rallyBasicAuthCredentialsInBase64) {
		Map<String, String> credMap = new LinkedHashMap<String, String>();
		if (rallyBasicAuthCredentialsInBase64 != null) {
				//the tokenize includes a \n to ensure we trim those off the end (mac base64 adds these!)
			StringTokenizer tokenizer = new StringTokenizer(new String(
					Base64.decodeBase64(rallyBasicAuthCredentialsInBase64)), ":\n");
			for (int i = 0; tokenizer.hasMoreTokens(); i++) {
				if (i == 0) {
					credMap.put("username", tokenizer.nextToken());
				} else {
					credMap.put("password", tokenizer.nextToken());
				}
			}
		}
		
		return credMap;

	}

	/**
	 * Generates an authenticated proxy connection URI and rally URI for use in
	 * talking to rally.
	 * 
	 * @param rallyBaseUri
	 *            A string representation of a rally URI
	 * @param fullProxyUrl
	 *            A string representation of a completed proxy URL:
	 *            http://your.proxy.com:8080
	 * @param username
	 *            A string representation of a username to be authenticated
	 * @param password
	 *            A string representation of a password to be used in
	 *            authentication
	 * @return A fully configured rally URI with authenticated proxy connection
	 */
	private URI createRallyConnection(String rallyBaseUri, String fullProxyUrl, String key) {
		Proxy proxy = null;
		URLConnection connection = null;
		try {
			if (!StringUtils.isEmpty(rallyBaseUri)) {
				URL baseUrl = new URL(rallyBaseUri);
				if (!StringUtils.isEmpty(fullProxyUrl)) {
					URL proxyUrl = new URL(fullProxyUrl);
					URI proxyUri = new URI(proxyUrl.getProtocol(), proxyUrl.getUserInfo(),
							proxyUrl.getHost(), proxyUrl.getPort(), proxyUrl.getPath(),
							proxyUrl.getQuery(), null);
					proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUri.getHost(),
							proxyUri.getPort()));
					connection = baseUrl.openConnection(proxy);

				} else {
					connection = baseUrl.openConnection();
				}
			} else {
				LOGGER.error("The response from Rally was blank or non existant - please check your property configurations");
				return null;
			}

			return connection.getURL().toURI();

		} catch (URISyntaxException | IOException e) {
			try {
				LOGGER.error("There was a problem parsing or reading the proxy configuration settings during openning a Rally connection. Defaulting to a non-proxy URI.");
				return new URI(rallyBaseUri);
			} catch (URISyntaxException e1) {
				LOGGER.error("Correction:  The Rally connection base URI cannot be read!");
				return null;
			}
		}
	}
}
