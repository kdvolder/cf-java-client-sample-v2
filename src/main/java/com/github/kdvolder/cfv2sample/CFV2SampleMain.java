package com.github.kdvolder.cfv2sample;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.apache.commons.lang.RandomStringUtils;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.info.GetInfoRequest;
import org.cloudfoundry.client.v2.info.GetInfoResponse;
import org.cloudfoundry.client.v2.serviceinstances.DeleteServiceInstanceRequest;
import org.cloudfoundry.client.v2.serviceinstances.ListServiceInstancesRequest;
import org.cloudfoundry.client.v2.serviceinstances.ServiceInstanceResource;
import org.cloudfoundry.doppler.DopplerClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.cloudfoundry.operations.applications.GetApplicationEnvironmentsRequest;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.operations.applications.LogsRequest;
import org.cloudfoundry.operations.applications.SetEnvironmentVariableApplicationRequest;
import org.cloudfoundry.operations.applications.UnsetEnvironmentVariableApplicationRequest;
import org.cloudfoundry.operations.routes.Level;
import org.cloudfoundry.operations.routes.ListRoutesRequest;
import org.cloudfoundry.operations.routes.MapRouteRequest;
import org.cloudfoundry.operations.routes.Route;
import org.cloudfoundry.operations.routes.UnmapRouteRequest;
import org.cloudfoundry.operations.services.CreateServiceInstanceRequest;
import org.cloudfoundry.operations.services.ServiceInstance;
import org.cloudfoundry.operations.spaces.GetSpaceRequest;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.cloudfoundry.spring.client.SpringCloudFoundryClient;
import org.cloudfoundry.uaa.UaaClient;
import org.cloudfoundry.util.PaginationUtils;

import com.google.common.collect.ImmutableList;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CFV2SampleMain  {

	private static final String API_HOST = "api.run.pivotal.io";
	private static final String ORG_NAME = "FrameworksAndRuntimes";
	private static final String SPACE_NAME = "kdevolder";

	SpringCloudFoundryClient client = SpringCloudFoundryClient.builder()
			.username("kdevolder@gopivotal.com")
			.password(System.getProperty("cf.password"))
			.host(API_HOST)
			.build();
	
	UaaClient uaaClient = ReactorUaaClient.builder()
			.cloudFoundryClient(client)
			.build();

	DopplerClient doppler = ReactorDopplerClient.builder()
			.cloudFoundryClient(client)
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

	public static void main(String[] args) throws Exception {
		System.out.println("Starting...");
		new CFV2SampleMain().showApplicationsWithDetails();
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
