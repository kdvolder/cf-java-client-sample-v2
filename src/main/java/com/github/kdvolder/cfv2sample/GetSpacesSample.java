package com.github.kdvolder.cfv2sample;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.CloudFoundryOperationsBuilder;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.cloudfoundry.operations.spaces.SpaceSummary;
import org.cloudfoundry.spring.client.SpringCloudFoundryClient;

import reactor.core.publisher.Mono;
import reactor.core.publisher.SchedulerGroup;

public class GetSpacesSample {

	SpringCloudFoundryClient client = SpringCloudFoundryClient.builder()
			.username(System.getProperty("cf.username"))
			.password(System.getProperty("cf.password"))
			.host("api.run.pivotal.io")
			.build();

	CloudFoundryOperations operations = new CloudFoundryOperationsBuilder()
			.cloudFoundryClient(client)
//			.target("FrameworksAndRuntimes", SPACE_NAME)
			.build();

	public static void main(String[] args) throws Exception {
		new GetSpacesSample().getSpacesInALoop();
	}

	private void getSpacesInALoop() throws Exception {
		int success = 0;
		int failed  = 0;

		Exception error = null;
		for (int i = 0; i < 100; i++) {
			try {
				long start = System.currentTimeMillis();
				List<CFSpace> spaces = getSpaces();
				long duration = System.currentTimeMillis() - start;
				System.out.println("getSpaces -> "+spaces.size()+" spaces in "+ duration + " ms");
				success++;
			} catch (Exception e) {
				error = e;
				failed++;
				System.out.println("getSpaces -> "+ExceptionUtil.getMessage(e));
			}
		}
		System.out.println("getSpaces failure rate = "+failed + "/" +(success+failed));
		if (failed>0) {
			throw new IOException("getSpaces failure rate = "+failed + "/" +(success+failed), error);
		}
	}


	public List<CFSpace> getSpaces() throws Exception {
		return operations.organizations()
			.list()
			.flatMap((OrganizationSummary org) -> {
				return operationsFor(org).flatMap((operations) ->
					operations
					.spaces()
					.list()
					.map((space) -> new CFSpace(org.getName(), space.getName()))
				);
			})
			.toList()
			.get(Duration.ofMinutes(1));
	}

	private Mono<CloudFoundryOperations> operationsFor(OrganizationSummary org) {
		return Mono.fromCallable(() -> new CloudFoundryOperationsBuilder()
				.cloudFoundryClient(client)
				.target(org.getName())
				.build()
		)
		//This fixes it:
		//.subscribeOn(SchedulerGroup.single())
		;
	}

	public static CFSpace wrap(OrganizationSummary org, SpaceSummary space) {
		return new CFSpace(org.getName(), space.getName());
	}

}
