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
import org.openmrs.contrib.databaseexporter.query.LocationIdentificationQuery;
import org.openmrs.contrib.databaseexporter.query.LocationQuery;
import org.openmrs.contrib.databaseexporter.util.ListMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Filters out locations from the system
 */
public class LocationFilter extends RowFilter {

	//***** PROPERTIES *****

	private List<LocationQuery> queries;

	//***** INSTANCE METHODS *****

	@Override
	public String getTableName() {
		return "location";
	}

	@Override
	public ListMap<String, Integer> getIds(ExportContext context) {
		ListMap<String, Integer> ret = new ListMap<String, Integer>();
		for (LocationQuery q : getQueries()) {
			context.log("Running query: " + q);
			ret.putAll(q.getColumnName(), q.getIds(context));
		}
		// Make sure that if we are filtering locations, we keep the unknown location
		if (!ret.isEmpty()) {
			LocationIdentificationQuery idq = new LocationIdentificationQuery("Unknown location");
			ret.putAll("location_id", idq.getIds(context));
		}
		return ret;
	}

	//***** PROPERTY ACCESS ****

	public List<LocationQuery> getQueries() {
		if (queries == null) {
			queries = new ArrayList<LocationQuery>();
		}
		return queries;
	}

	public void setQueries(List<LocationQuery> queries) {
		this.queries = queries;
	}
}
