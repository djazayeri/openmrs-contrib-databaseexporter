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
import org.openmrs.contrib.databaseexporter.filter.PatientFilter;
import org.openmrs.contrib.databaseexporter.query.PatientIdQuery;
import org.openmrs.contrib.databaseexporter.filter.TableFilter;
import org.openmrs.contrib.databaseexporter.util.DbUtil;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QueryBuilderTest {

	private Configuration getConfiguration() {
		Configuration c = Configuration.getDefaultConfiguration();
		c.setSourceDatabaseCredentials(new DatabaseCredentials("jdbc:mysql://localhost:3306/openmrs_rwink?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8", "root", "root"));
		c.setTargetDirectory("/home/mseaton/Desktop");

		TableFilter tableFilter = new TableFilter();
		List<String> tablesToInclude = Arrays.asList("patient", "person", "patient_identifier");
		tableFilter.getIncludeSchema().addAll(tablesToInclude);
		tableFilter.getIncludeData().addAll(tablesToInclude);
		c.setTableFilter(tableFilter);

		PatientFilter patientFilter = new PatientFilter();
		PatientIdQuery idFilter = new PatientIdQuery();
		idFilter.addIds(82494, 81712);
		patientFilter.getQueries().add(idFilter);
		c.getRowFilters().add(patientFilter);

		return c;
	}

	@Test
	public void shouldTestExport() throws Exception {
		DatabaseExporter.export(getConfiguration());
	}

	@Test
	public void shouldTestForeignKeys() throws Exception {
		Configuration configuration = getConfiguration();
		Connection connection = null;
		try {
			connection = DbUtil.openConnection(configuration.getSourceDatabaseCredentials());
			OutputStreamWriter osWriter = new OutputStreamWriter(System.out, "UTF-8");
			PrintWriter out = new PrintWriter(osWriter);
			final ExportContext context = new ExportContext(configuration, connection, out);

			Set<String> excludePatterns = new HashSet<String>();
			excludePatterns.add("*/users");

			Set<String> allTables = context.getTableData().keySet();
			for (String outer : allTables) {
				for (String inner : allTables) {
					if (!outer.equals(inner) && inner.equals("person") || inner.equals("patient")) {
						List<String> joins = DbUtil.getJoins(outer, inner, excludePatterns, context);
						for (String s : joins) {
							System.out.println(outer + "->" + inner + ": " + s);
						}
					}
				}
			}



		}
		finally {
			DbUtil.closeConnection(connection);
		}
	}

	@Test
	public void shouldTest() throws Exception {



				/*

		encounter_provider.encounter_provider_id no
		check foreign keys:
		encounter_provider.encounter_id / encounter.encounter_id no
		check foreign keys
		encounter.patient_id / patient.patient_id yes

		select *
		from encounter_provider
		inner join encounter on encounter_provider.encounter_id = encounter.encounter_id
		inner join patient on encounter.patient_id = patient.patient_id
		where patient.patient_id in (82494,81712)

		"patient_identifier", "patient_id", "patient", "patient_id"

		 */
	}
}

