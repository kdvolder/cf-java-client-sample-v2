package com.github.kdvolder.cfv2sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.cloudfoundry.doppler.DopplerClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.cloudfoundry.operations.routes.Level;
import org.cloudfoundry.operations.routes.ListRoutesRequest;
import org.cloudfoundry.operations.routes.Route;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.AbstractUaaTokenProvider;
import org.cloudfoundry.reactor.tokenprovider.OneTimePasscodeTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.cloudfoundry.uaa.UaaClient;

import reactor.core.publisher.Mono;

public class SSOExampleMain  {

	private static final String API_HOST = "api.run.pivotal.io";
	private static final String ORG_NAME = "FrameworksAndRuntimes";
	private static final String SPACE_NAME = "kdevolder";
	private static final boolean SKIP_SSL = false;
	private static final String USER = "kdevolder@gopivotal.com";

	ConnectionContext connection = DefaultConnectionContext.builder()
			.apiHost(API_HOST)
			.skipSslValidation(SKIP_SSL)
			.build();

	AbstractUaaTokenProvider tokenProvider = createTokenProvider();

	ReactorCloudFoundryClient client = ReactorCloudFoundryClient.builder()
			.connectionContext(connection)
			.tokenProvider(tokenProvider)
			.build();

	UaaClient uaaClient = ReactorUaaClient.builder()
			.connectionContext(connection)
			.tokenProvider(tokenProvider)
			.build();

	DopplerClient doppler = ReactorDopplerClient.builder()
			.connectionContext(connection)
			.tokenProvider(tokenProvider)
			.build();

	CloudFoundryOperations cfops = DefaultCloudFoundryOperations.builder()
			.cloudFoundryClient(client)
			.dopplerClient(doppler)
			.uaaClient(uaaClient)
			.organization(ORG_NAME)
			.space(SPACE_NAME)
			.build();

	protected AbstractUaaTokenProvider createTokenProvider() {
		try {
			System.out.print("Enter SSO token:");
			String ssoToken = readline();
			System.out.println("Using SSO code: '"+ssoToken+"'");
			return OneTimePasscodeTokenProvider.builder()
					.passcode(ssoToken)
					.build();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String readline() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		return reader.readLine();
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Starting...");
		SSOExampleMain main = new SSOExampleMain();
		main.showOrgs();
	}

	private void showOrgs() {
		List<OrganizationSummary> orgs = cfops.organizations().list().collect(Collectors.toList()).block();
		System.out.println("====Fetching orgs====");
		for (OrganizationSummary o : orgs) {
			System.out.println(o);
		}
		System.out.println("====done====");
	}

}