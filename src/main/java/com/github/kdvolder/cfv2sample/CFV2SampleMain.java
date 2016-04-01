package com.github.kdvolder.cfv2sample;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.serviceinstances.ListServiceInstancesRequest;
import org.cloudfoundry.client.v2.serviceinstances.ServiceInstanceResource;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.CloudFoundryOperationsBuilder;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.operations.applications.SetEnvironmentVariableApplicationRequest;
import org.cloudfoundry.operations.services.CreateServiceInstanceRequest;
import org.cloudfoundry.operations.spaces.GetSpaceRequest;
import org.cloudfoundry.spring.client.SpringCloudFoundryClient;
import org.cloudfoundry.util.PaginationUtils;

import com.google.common.collect.ImmutableList;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CFV2SampleMain {

	private static final String SPACE_NAME = "kdevolder";

	static SpringCloudFoundryClient client = SpringCloudFoundryClient.builder()
			.username("kdevolder@gopivotal.com")
			.password(System.getProperty("cf.password"))
			.host("api.run.pivotal.io")
			.build();

	static CloudFoundryOperations cfops = new CloudFoundryOperationsBuilder()
			.cloudFoundryClient(client)
			.target("FrameworksAndRuntimes", SPACE_NAME)
			.build();

	static String spaceId = getSpaceId();

	public static void main(String[] args) throws Exception {
		Map<String,String> env = new HashMap<>();
		env.put("foo", "ThisisFoo");
		env.put("bar", "ThisisBar");

		setEnvVars("dododododo", env).get();
		System.out.println("Finished settting env vars.");

//		createService("konijn", "cloudamqp", "lemur");
//		showApplicationDetails("demo456");
//		showApplications();
//		showServices();
	}

	private static Mono<Void> setEnvVars(String appName, Map<String, String> env) {
		//XXX CF V2: bug in CF V2: https://www.pivotaltracker.com/story/show/116725155
		// Also this code does not unset env vars, but it probably should.
		if (env==null) {
			return Mono.empty();
		} else {
			return Flux.fromIterable(env.entrySet())
			.flatMap((entry) -> {
				System.out.println("Set var starting: "+entry);
				return cfops.applications()
				.setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
						.name(appName)
						.variableName(entry.getKey())
						.variableValue(entry.getValue())
						.build()
				)
				.after(() -> {
					System.out.println("Set var complete: "+entry);
					return Mono.empty();
				});
			})
			.after();
		}
	}

	private static void createService(String name, String service, String plan) {
		System.out.println("============================");
		cfops.services().createInstance(CreateServiceInstanceRequest.builder()
				.serviceInstanceName(name)
				.serviceName(service)
				.planName(plan)
				.build()
		)
		.get();
		System.out.println("Created a Service!");
	}

	private static void showApplicationDetails(String name) {
		System.out.println("============================");
		ApplicationDetail app = cfops.applications()
		.get(GetApplicationRequest.builder()
			.name(name)
			.build()
		)
		.get();

		System.out.println("name = "+app.getName());
		System.out.println("requested state = "+app.getRequestedState());
		System.out.println("buildpack = " + app.getBuildpack());
	}

	private static String getSpaceId() {
		try {
			return cfops.spaces().get(spaceWithName(SPACE_NAME)).toCompletableFuture().get().getId();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static GetSpaceRequest spaceWithName(String spaceName) {
		return GetSpaceRequest.builder()
				.name(spaceName)
				.build();
	}

	protected static void showApplications() {
		System.out.println("============================");
		List<String> names = cfops
			.applications()
			.list()
			.map(ApplicationSummary::getName)
			.toList()
			.map(ImmutableList::copyOf)
			.get();
		System.out.println("Applications: "+names);
	}

    private static Flux<ServiceInstanceResource> requestServices(CloudFoundryClient cloudFoundryClient) {
        return PaginationUtils.requestResources((page) -> {
            	ListServiceInstancesRequest request = ListServiceInstancesRequest.builder()
            		.page(page)
    				.spaceId(spaceId)
    				.build();
            	return client.serviceInstances().list(request);
        });
    }

	protected static void showServices() {
		System.out.println("============================");
		ImmutableList<String> serviceNames = requestServices(client)
			.map((ServiceInstanceResource service) -> service.getEntity().getName())
			.toList()
			.map(ImmutableList::copyOf)
			.get();
		System.out.println("Services: "+serviceNames);
	}



}
