package com.github.kdvolder.cfv2sample;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.cloudfoundry.doppler.DopplerClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.ApplicationManifest;
import org.cloudfoundry.operations.applications.ApplicationManifestUtils;
import org.cloudfoundry.operations.applications.GetApplicationManifestRequest;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.operations.applications.PushApplicationManifestRequest;
import org.cloudfoundry.operations.routes.MapRouteRequest;
import org.cloudfoundry.operations.spaces.SpaceSummary;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.AbstractUaaTokenProvider;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.cloudfoundry.uaa.UaaClient;
import org.springframework.util.Assert;

import reactor.core.publisher.Mono;

public class CFV2SampleMain  {

//	private static final String API_HOST = "api.run.pivotal.io";
//	private static final String ORG_NAME = "FrameworksAndRuntimes";
//	private static final String SPACE_NAME = "kdevolder";
//	private static final boolean SKIP_SSL = false;
//	private static final String USER = "kdevolder@gopivotal.com";
//	private static final String PASSWORD = System.getProperty("cf.password");

	private static final String API_HOST = "api.local2.pcfdev.io";
	private static final String ORG_NAME = "pcfdev-org";
	private static final String SPACE_NAME = "pcfdev-space";
	private static final boolean SKIP_SSL = true;
	private static final String USER = "admin";
	private static final String PASSWORD = "admin";

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
			System.out.println("Using PASSWORD token for auth");
			return PasswordGrantTokenProvider.builder()
					.username(USER)
					.password(PASSWORD)
					.build();
//		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Starting...");
		new CFV2SampleMain().pushAppAndRepushWithDifferentRoute();
	}

	private void pushAppAndBindTcpRoute() {
		File mf = new File("manifest.yml")
				.getAbsoluteFile(); //Workaround bug in manifest parser related to relative path handling.
		if (!mf.isFile()) {
			throw new IllegalStateException("Not a file? "+mf);
		}
		PushApplicationManifestRequest req = PushApplicationManifestRequest.builder()
				.addAllManifests(ApplicationManifestUtils.read(mf.toPath()))
				.build();
				
		cfops.applications().pushManifest(req).block();
		
		String appName = "test-static-aaasd";
		ApplicationDetail appDetails = cfops.applications().get(GetApplicationRequest.builder()
				.name(appName)
				.build()
		).block();
		System.out.println("app deployed at: "+appDetails.getUrls());
		
		System.out.println("Binding a tcp route...");
		String tcpDomain = "tcp.local2.pcfdev.io";
		
		cfops.routes().map(MapRouteRequest.builder()
				.applicationName(appName)
				.domain(tcpDomain)
				.port(61005)
//				.randomPort(true)
				.build()
		).block();

		System.out.println("uris from 'ApplicationDetails': "+cfops.applications().get(GetApplicationRequest.builder()
				.name(appName)
				.build()
		).block().getUrls());
		
		ApplicationManifest manifest = cfops.applications().getApplicationManifest(GetApplicationManifestRequest.builder()
				.name(appName)
				.build()
		).block();
		
		System.out.println("routes from manifest: " + manifest.getRoutes());
		
	}

	private void pushAppAndRepushWithDifferentRoute() {
		String appName = "test-static-aaasd";
		
		String[] manifests = {
				"manifest-1.yml",
				"manifest-2.yml"
		};
		for (String fname : manifests) {
			File mf = new File(fname)
					.getAbsoluteFile(); //Workaround bug in manifest parser related to relative path handling.
			if (!mf.isFile()) {
				throw new IllegalStateException("Not a file? "+mf);
			}
			PushApplicationManifestRequest req = PushApplicationManifestRequest.builder()
					.addAllManifests(ApplicationManifestUtils.read(mf.toPath()))
					.build();
					
			cfops.applications().pushManifest(req).block();
			
			System.out.println("uris from 'ApplicationDetails': "+cfops.applications().get(GetApplicationRequest.builder()
					.name(appName)
					.build()
			).block().getUrls());
			
			ApplicationManifest manifest = cfops.applications().getApplicationManifest(GetApplicationManifestRequest.builder()
					.name(appName)
					.build()
			).block();
			
			System.out.println("routes from manifest: " + manifest.getRoutes());
		}
	}

	private void pushAnApp() {
		String appName = "test-static-aaasd";
		File mf = new File("manifest.yml")
				.getAbsoluteFile(); //Workaround bug in manifest parser related to relative path handling.
		if (!mf.isFile()) {
			throw new IllegalStateException("Not a file? "+mf);
		}
		PushApplicationManifestRequest req = PushApplicationManifestRequest.builder()
				.addAllManifests(ApplicationManifestUtils.read(mf.toPath()))
				.build();
				
		cfops.applications().pushManifest(req).block();
		
		System.out.println("uris from 'ApplicationDetails': "+cfops.applications().get(GetApplicationRequest.builder()
				.name(appName)
				.build()
		).block().getUrls());
		
		ApplicationManifest manifest = cfops.applications().getApplicationManifest(GetApplicationManifestRequest.builder()
				.name(appName)
				.build()
		).block();
		System.out.println("routes from manifest: " + manifest.getRoutes());
	}


}
