package com.github.kdvolder.cfv2sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.cloudfoundry.doppler.DopplerClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
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
	private static final String SPACE_NAME = "sts-development";//"kdevolder";
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
			System.out.print("Using SSO code: '"+ssoToken+"'");
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
		new SSOExampleMain().showApplicationsWithDetails();
		//new SSOExampleMain().showRoutes();
	}

	private void showRoutes() {
		System.out.println(">>>> getting routes ...");
		List<Route> routes = cfops.routes().list(ListRoutesRequest.builder()
				.level(Level.SPACE)
				.build()
		)
		.collectList()
		.block();
		System.out.println(">>>> getting routes DONE");
		for (Route route : routes) {
			System.out.println(route);
		}
	}


	private void showApplicationsWithDetails() {
		cfops.applications()
		.list()
		.flatMap((appSummary) -> {
			return cfops.applications().get(GetApplicationRequest.builder()
				.name(appSummary.getName())
				.build()
			)
			.otherwise((error) -> {
				System.err.println("Error gettting details for app: "+appSummary.getName());
				error.printStackTrace(System.err);
				return Mono.empty();
			});
		})
		.doOnTerminate(() -> System.out.println("====done===="))
		.subscribe(System.out::println);
		while (true) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
