/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2;

import java.io.IOException;
import java.util.List;

import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.organizations.OrganizationSummary;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Kris De Volder
 */
public class DefaultClientRequestsV2 {

	private CloudFoundryOperations _operations;

	private Mono<CloudFoundryOperations> operationsFor(OrganizationSummary org) {
		return null;
	}

	public List<CFSpace> getSpaces() throws Exception {
		return get(
			log("operations.organizations().list()",
				_operations.organizations()
				.list()
			)
			.flatMap((OrganizationSummary org) -> {
				return operationsFor(org).flatMap((operations) ->
					log("operations.spaces.list(org="+org.getId()+")",
						operations
						.spaces()
						.list()
						.map((space) -> CFWrappingV2.wrap(org, space))
					)
				);
			})
			.collectList()
		);
	}

	private <T> Flux<T> log(String msg, Flux<T> flux) {
		return flux;
	}

	public static <T> T get(Mono<T> mono) throws Exception {
		try {
			return mono.block();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

}
