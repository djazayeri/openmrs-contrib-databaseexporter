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
import org.openmrs.contrib.databaseexporter.filter.RowFilter;
import org.openmrs.contrib.databaseexporter.filter.UserFilter;
import org.openmrs.contrib.databaseexporter.query.PatientQuery;
import org.openmrs.contrib.databaseexporter.query.UserIdentificationQuery;
import org.openmrs.contrib.databaseexporter.query.UserQuery;
import org.openmrs.contrib.databaseexporter.util.Util;

import java.util.List;

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
	}

	@Test
	public void shouldMergeLists() throws Exception {
		Configuration configuration = getConfiguration();

		List<String> excludeData = configuration.getTableFilter().getExcludeData();
		Assert.assertEquals(2, excludeData.size());
		Assert.assertEquals("hl7_in_*", excludeData.get(0));
		Assert.assertEquals("sync_*", excludeData.get(1));

		List<UserQuery> queries = configuration.getUserFilter().getQueries();
		Assert.assertEquals(2, queries.size());
		UserIdentificationQuery q1 = (UserIdentificationQuery) queries.get(0);
		UserIdentificationQuery q2 = (UserIdentificationQuery) queries.get(1);
		Assert.assertEquals(1, q1.getUserNames().size());
		Assert.assertEquals("mseaton", q1.getUserNames().iterator().next());
		Assert.assertEquals(1, q2.getUserNames().size());
		Assert.assertEquals("test", q2.getUserNames().iterator().next());
		Assert.assertEquals(5, configuration.getRowTransforms().size());
	}
}

