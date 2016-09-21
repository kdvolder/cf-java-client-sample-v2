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

import org.cloudfoundry.client.v2.buildpacks.BuildpackResource;
import org.cloudfoundry.client.v2.domains.DomainResource;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.cloudfoundry.operations.applications.InstanceDetail;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.cloudfoundry.operations.services.ServiceInstance;
import org.cloudfoundry.operations.spaces.SpaceSummary;
import org.cloudfoundry.operations.stacks.Stack;

/**
 * Various helper methods to 'wrap' objects returned by CF client into
 * our own types, so that we do not directly expose library types to our
 * code.
 *
 * @author Kris De Volder
 */
public class CFWrappingV2 {

	public static CFBuildpack wrap(BuildpackResource rsrc) {
		return null;
	}

	public static CFApplicationDetail wrap(ApplicationDetail details, ApplicationExtras extras) {
		return null;
	}

	public static CFStack wrap(Stack stack) {
		return null;
	}

	public static CFApplicationDetail wrap(
			CFApplicationSummaryData summary,
			ApplicationDetail details
	) {
		return null;
	}

	public static CFCloudDomain wrap(DomainResource domainRsrc) {
		return null;
	}

	public static CFInstanceStats wrap(InstanceDetail instanceDetail) {
		return null;
	}

	private static CFApplicationSummaryData wrapSummary(ApplicationDetail app, ApplicationExtras extras) {
		return null;
	}

	public static CFApplication wrap(ApplicationSummary app, ApplicationExtras extras) {
		return null;
	}

	public static CFServiceInstance wrap(final ServiceInstance service) {
		return null;
	}

	public static CFAppState wrapAppState(String s) {
		return null;
	}

	public static CFSpace wrap(OrganizationSummary org, SpaceSummary space) {
		return null;
	}

	public static CFOrganization wrap(OrganizationSummary org) {
		return null;
	}

	public static CFBuildpack buildpack(String name) {
		return null;
	}

}
