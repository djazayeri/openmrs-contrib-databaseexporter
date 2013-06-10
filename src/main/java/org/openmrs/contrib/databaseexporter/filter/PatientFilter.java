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
package org.openmrs.contrib.databaseexporter.filter;

import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.openmrs.contrib.databaseexporter.ExportContext;
import org.openmrs.contrib.databaseexporter.filter.query.FilterQuery;
import org.openmrs.contrib.databaseexporter.filter.query.SqlFilterQuery;
import org.openmrs.contrib.databaseexporter.util.Util;

import java.util.HashSet;
import java.util.Set;

/**
 * The Patient Filter is applied first of all of the filters, and retrieves all patients that are
 * configured to be included, as well as the super-set of all persons needed for these patients,
 * their immediate relationships, and all providers and users defined in the system.
 * Filtering down these users and providers can come during either a later filtering phase or the
 * transform phase, at which point the associated person records can be chosen to be removed as well.
 */
public class PatientFilter extends RowFilter {

	//**** CONSTRUCTORS *****

	public PatientFilter() {

		{
			PersonFilter f = new PersonFilter();
			f.addFilterQuery(new SqlFilterQuery("select person_id from person inner join patient on person.person_id = patient.patient_id"));
			f.addFilterQuery(new SqlFilterQuery("select person_a from relationship inner join patient on relationship.person_b = patient.patient_id"));
			f.addFilterQuery(new SqlFilterQuery("select person_b from relationship inner join patient on relationship.person_a = patient.patient_id"));
			f.addFilterQuery(new SqlFilterQuery("select person_id from users where users.username in ('admin','daemon') and person_id is not null"));
			f.addFilterQuery(new SqlFilterQuery("select person_id from provider where person_id is not null"));
			addDependencyFilter(f);
		}
		{
			PatientIdentifierFilter f = new PatientIdentifierFilter();
			f.addFilterQuery(new SqlFilterQuery("select patient_identifier_id from patient_identifier inner join patient on patient_identifier.patient_id = patient.patient_id"));
			addDependencyFilter(f);
		}
		{
			EncounterFilter f = new EncounterFilter();
			f.addFilterQuery(new SqlFilterQuery("select encounter_id from encounter inner join patient on encounter.patient_id = patient.patient_id"));
			addDependencyFilter(f);
		}
		{
			OrderFilter f = new OrderFilter();
			f.addFilterQuery(new SqlFilterQuery("select order_id from orders inner join patient on orders.patient_id = patient.patient_id"));
			addDependencyFilter(f);
		}
		{
			PatientProgramFilter f = new PatientProgramFilter();
			f.addFilterQuery(new SqlFilterQuery("select patient_program_id from patient_program inner join patient on patient_program.patient_id = patient.patient_id"));
			addDependencyFilter(f);
		}
	}

	//***** INSTANCE METHODS *****

	@Override
	public String getTableName() {
		return "patient";
	}

	@Override
	public String getPrimaryKeyColumn() {
		return "patient_id";
	}
}
