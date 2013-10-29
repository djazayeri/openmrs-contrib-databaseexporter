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

import org.openmrs.contrib.databaseexporter.ExportContext;
import org.openmrs.contrib.databaseexporter.query.AllPatientQuery;
import org.openmrs.contrib.databaseexporter.query.PatientQuery;
import org.openmrs.contrib.databaseexporter.util.ListMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Returns a particular number of patients in configured set of age ranges
 */
public class PatientFilter extends RowFilter {

	//***** PROPERTIES *****

	private List<PatientQuery> queries;

	//***** INSTANCE METHODS *****

	@Override
	public String getTableName() {
		return "patient";
	}

	@Override
	public ListMap<String, Integer> getIds(ExportContext context) {
		ListMap<String, Integer> ret = new ListMap<String, Integer>();
		List<PatientQuery> l = getQueries();
		if (l.isEmpty() && context.getTableData().get("patient").isExportData()) {
			l.add(new AllPatientQuery());
		}
		for (PatientQuery q : l) {
			context.log("Running query: " + q);
			ret.putAll(q.getColumnName(), q.getIds(context));
		}
		return ret;
	}

	//***** PROPERTY ACCESS ****

	public List<PatientQuery> getQueries() {
		if (queries == null) {
			queries = new ArrayList<PatientQuery>();
		}
		return queries;
	}

	public void setQueries(List<PatientQuery> queries) {
		this.queries = queries;
	}
}
