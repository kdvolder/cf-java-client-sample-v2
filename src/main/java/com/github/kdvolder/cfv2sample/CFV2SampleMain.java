package com.github.kdvolder.cfv2sample;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.organizations.ListOrganizationsRequest;
import org.cloudfoundry.client.v2.organizations.OrganizationResource;
import org.cloudfoundry.client.v2.serviceinstances.ListServiceInstancesRequest;
import org.cloudfoundry.client.v2.serviceinstances.ServiceInstanceResource;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.CloudFoundryOperationsBuilder;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.cloudfoundry.operations.spaces.GetSpaceRequest;
import org.cloudfoundry.spring.client.SpringCloudFoundryClient;
import org.cloudfoundry.util.PaginationUtils;

import com.fasterxml.jackson.databind.deser.impl.ExternalTypeHandler.Builder;
import com.google.common.collect.ImmutableList;

import reactor.core.publisher.Flux;

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
		showApplications();
		showServices();
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
		List<String> names = ImmutableList.copyOf(
			cfops
				.applications()
				.list()
				.map(ApplicationSummary::getName)
				.toIterable()
		);
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
		ImmutableList<String> serviceNames = ImmutableList.copyOf(
			requestServices(client)
				.map((ServiceInstanceResource service) ->
					service.getEntity().getName())
				.toIterable()
		);
		System.out.println("Services: "+serviceNames);
	}



}
