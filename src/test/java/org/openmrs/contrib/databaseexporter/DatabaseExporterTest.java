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

import org.junit.Test;

import java.lang.Exception;
import java.util.ArrayList;
import java.util.List;

public class DatabaseExporterTest {

	@Test
	public void shouldExportMinimumRwandaData() throws Exception {
		List<String> config = new ArrayList<String>();
		config.add("rwanda/deidentify");
		config.add("rwanda/trimArchiveData");
		config.add("removeSyncData");
		config.add("removeAllPatients");
		config.add("rwanda/trimUsers");
		config.add("rwanda/trimProviders");
		config.add("-localDbName=openmrs_rwink");
		config.add("-user=openmrs");
		config.add("-password=openmrs");
		config.add("-logSql=true");
		DatabaseExporter.main(config.toArray(new String[] {}));
	}

	@Test
	public void shouldExportSmallPatientsRwandaData() throws Exception {
		List<String> config = new ArrayList<String>();
		config.add("removeSyncData");
		config.add("rwanda/deidentify");
		config.add("rwanda/trimArchiveData");
		config.add("rwanda/trimPatientsSmall");
		config.add("-localDbName=openmrs_rwink");
		config.add("-user=openmrs");
		config.add("-password=openmrs");
		config.add("-logSql=true");
		DatabaseExporter.main(config.toArray(new String[] {}));
	}

	@Test
	public void shouldExportMaximumRwandaData() throws Exception {
		List<String> config = new ArrayList<String>();
		config.add("-localDbName=openmrs_rwink");
		config.add("rwanda/trimArchiveData");
		config.add("-user=openmrs");
		config.add("-password=openmrs");
		DatabaseExporter.main(config.toArray(new String[] {}));
	}

	@Test
	public void shouldExportDeidentifiedMirebalaisData() throws Exception {
		List<String> config = new ArrayList<String>();
		config.add("-localDbName=openmrs_mirebalais");
		config.add("mirebalais/deidentify");
		config.add("-user=openmrs");
		config.add("-password=openmrs");
		DatabaseExporter.main(config.toArray(new String[] {}));
	}
}

