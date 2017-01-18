package com.github.kdvolder.cfv2sample;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.cloudfoundry.doppler.DopplerClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.operations.applications.PushApplicationRequest;
import org.cloudfoundry.operations.routes.Level;
import org.cloudfoundry.operations.routes.ListRoutesRequest;
import org.cloudfoundry.operations.routes.Route;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.AbstractUaaTokenProvider;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.cloudfoundry.reactor.tokenprovider.RefreshTokenGrantTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.cloudfoundry.uaa.UaaClient;

import reactor.core.publisher.Mono;

public class CFV2SampleMain  {

	private static final String tokenPath = ".cf_java_client.json";

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

	private void getSshCode() {
		System.out.println("ssh code = '"+cfops.advanced().sshCode().block()+"'");
	}

	protected AbstractUaaTokenProvider createTokenProvider() {
		TokenFile tokenFile = TokenFile.read(new File(tokenPath));
		if (tokenFile!=null) {
			System.out.println("Using stored REFRESH token for auth");
			return RefreshTokenGrantTokenProvider.builder()
					.token(tokenFile.getRefreshToken())
					.build();
		} else {
			System.out.println("Using PASSWORD token for auth");
			return PasswordGrantTokenProvider.builder()
					.username(USER)
					.password(System.getProperty("cf.password"))
					.build();
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Starting...");
		new CFV2SampleMain().showApplicationsWithDetails();
//		new CFV2SampleMain().showRoutes();
//		new CFV2SampleMain().push();
	}

	private void push() {
		Path path = new File("/home/kdvolder/git/kdvolder/chatter/chatter-banner-service/target/chatter-banner-service-0.0.1-SNAPSHOT.jar")
				.toPath();
		cfops.applications().push(PushApplicationRequest.builder()
				.application(path)
				.name("chatter-banner-service")
				.buildpack("java_buildpack")
				.build()
		)
		.block();
	}

	private void saveRefreshToken() {
		try {
			String refreshToken = tokenProvider.getRefreshToken();
			TokenFile tokenFile = new TokenFile();
			tokenFile.setRefreshToken(refreshToken);
			tokenFile.write(new File(tokenPath));
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		saveRefreshToken();
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
		.doOnComplete(this::saveRefreshToken)
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
