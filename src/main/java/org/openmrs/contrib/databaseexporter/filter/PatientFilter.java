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
import org.openmrs.contrib.databaseexporter.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Row filter which filters the patient data that is included in the export
 */
public abstract class PatientFilter extends RowFilter {

	public abstract Collection<Integer> getPatientIds(ExportContext context);

	private List<FilterQuery> standardQueries = new ArrayList<FilterQuery>();  // Built-in filter queries
	private List<FilterQuery> customQueries = new ArrayList<FilterQuery>();  // Extended filter queries

	protected List<Integer> getPersonIds(ExportContext context) {
		Collection<Integer> pIds = getPatientIds(context);
		List<Integer> ret = new ArrayList<Integer>();
		ret.addAll(context.executeQuery("select person_b from relationship where person_a in (" + Util.toString(pIds) + ")", new ColumnListHandler<Integer>()));
		ret.addAll(context.executeQuery("select person_a from relationship where person_b in (" + Util.toString(pIds) + ")", new ColumnListHandler<Integer>()));
		return ret;
	}

	public PatientFilter() {
		addStandardQuery("person", "person_id", "person_id in :personIds");
		addStandardQuery("patient", "patient_id", "patient_id in :patientIds");
		addStandardQuery("patient_identifier", "patient_identifier_id", "patient_id in :patientIds");
		addStandardQuery("patient_program", "patient_program_id", "patient_id in :patientIds");
		addStandardQuery("patient_state", "patient_state_id", "patient_program_id in (select patient_program_id from patient_program where patient_id in :patientIds)");
		addStandardQuery("person_address", "person_address_id", "person_id in :personIds");
		addStandardQuery("person_attribute", "person_attribute_id", "person_id in :personIds");
		addStandardQuery("person_name", "person_name_id", "person_id in :personIds");
		addStandardQuery("relationship", "relationship_id", "person_a in :patientIds or person_b in :patientIds");
		addStandardQuery("orders", "order_id", "patient_id in :patientIds");
		addStandardQuery("drug_order", "order_id", "order_id in (select order_id from orders where patient_id in :patientIds)");
		addStandardQuery("encounter", "encounter_id", "patient_id in :patientIds");
		addStandardQuery("encounter_provider", "encounter_provider_id", "encounter_id in (select encounter_id from encounter where patient_id in :patientIds)");
		addStandardQuery("obs", "obs_id", "person_id in :personIds");
		addStandardQuery("visit", "visit_id", "visit_id in (select visit_id from encounter where patient_id in :patientIds)");
	}

	@Override
	public void applyFilters(ExportContext context) {
		Collection<Integer> patientIds = getPatientIds(context);
		if (patientIds != null && !patientIds.isEmpty()) {
			Collection<Integer> personIds = getPersonIds(context);
			String patientIdClause = "(" + Util.toString(patientIds) + ")";
			String personIdClause = "(" + Util.toString(personIds) + ")";
			List<FilterQuery> allQueries = new ArrayList<FilterQuery>(getStandardQueries());
			allQueries.addAll(getCustomQueries());
			for (FilterQuery fq : allQueries) {
				StringBuilder sb = new StringBuilder();
				sb.append("select ").append(fq.getColumnToQuery()).append(" from ").append(fq.getTableName());
				if (fq.getConstraintClause() != null) {
					String clause = fq.getConstraintClause().replace(":patientIds", patientIdClause).replace(":personIds", personIdClause);
					sb.append(" where ").append(clause);
				}
				List<Object> l = context.executeQuery(sb.toString(), new ColumnListHandler<Object>());
				applyConstraints(fq.getTableName(), fq.getColumnToQuery(), l, context);
			}
		}
	}

	public void addStandardQuery(String tableName, String columnToQuery, String constraintClause) {
		getStandardQueries().add(new FilterQuery(tableName, columnToQuery, constraintClause));
	}

	public List<FilterQuery> getStandardQueries() {
		if (standardQueries == null) {
			standardQueries = new ArrayList<FilterQuery>();
		}
		return standardQueries;
	}

	public void setStandardQueries(List<FilterQuery> standardQueries) {
		this.standardQueries = standardQueries;
	}

	public List<FilterQuery> getCustomQueries() {
		if (customQueries == null) {
			customQueries = new ArrayList<FilterQuery>();
		}
		return customQueries;
	}

	public void setCustomQueries(List<FilterQuery> customQueries) {
		this.customQueries = customQueries;
	}
}
