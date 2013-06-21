/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.contrib.databaseexporter;

import junit.framework.Assert;
import org.junit.Test;
import org.openmrs.contrib.databaseexporter.filter.PatientFilter;
import org.openmrs.contrib.databaseexporter.util.Util;

public class UtilTest {

	public Configuration getConfiguration() {
		return Util.loadConfiguration(
			"org/openmrs/contrib/databaseexporter/config1.json",
			"org/openmrs/contrib/databaseexporter/config2.json",
			"org/openmrs/contrib/databaseexporter/config3.json"
		);
	}

	@Test
	public void shouldRetainTopLevelValue() throws Exception {
		Configuration configuration = getConfiguration();
		Assert.assertTrue(configuration.getLogSql());
	}

	@Test
	public void shouldRetainNestedValue() throws Exception {
		Configuration configuration = getConfiguration();
		Assert.assertEquals("openmrs", configuration.getSourceDatabaseCredentials().getUser());
	}

	@Test
	public void shouldOverrideTopLevelValues() throws Exception {
		Configuration configuration = getConfiguration();
		Assert.assertEquals("/home/mseaton/Desktop/databaseexportmod", configuration.getTargetDirectory());
	}

	@Test
	public void shouldOverrideNestedValues() throws Exception {
		Configuration configuration = getConfiguration();
		Assert.assertTrue(configuration.getSourceDatabaseCredentials().getUrl().contains("openmrs_different"));
		Assert.assertEquals(2, configuration.getTableFilter().getIncludeSchema().size());
		Assert.assertEquals("patient", configuration.getTableFilter().getIncludeSchema().get(0));
	}

	@Test
	public void shouldOverrideNestedLists() throws Exception {
		Configuration configuration = getConfiguration();
		Assert.assertEquals(2, configuration.getRowFilters().size());
		Assert.assertEquals(PatientFilter.class, configuration.getRowFilters().get(0).getClass());
		Assert.assertEquals(4, configuration.getRowTransforms().size());
	}
}

