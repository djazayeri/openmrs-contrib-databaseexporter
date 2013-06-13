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
import org.openmrs.contrib.databaseexporter.query.UserIdentificationQuery;
import org.openmrs.contrib.databaseexporter.query.UserQuery;
import org.openmrs.contrib.databaseexporter.util.ListMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Returns a particular number of patients in configured set of age ranges
 */
public class UserFilter extends RowFilter {

	//***** PROPERTIES *****

	private List<UserQuery> queries;

	//***** INSTANCE METHODS *****

	@Override
	public String getTableName() {
		return "users";
	}

	@Override
	public ListMap<String, Integer> getIds(ExportContext context) {
		ListMap<String, Integer> ret = new ListMap<String, Integer>();
		for (UserQuery q : getQueries()) {
			ret.putAll(q.getColumnName(), q.getIds(context));
		}
		// Make sure that if we are filtering users, that we keep the admin and daemon users
		if (!ret.isEmpty()) {
			UserIdentificationQuery idq = new UserIdentificationQuery("admin", "daemon");
			ret.putAll("user_id", idq.getIds(context));
		}
		return ret;
	}

	//***** PROPERTY ACCESS ****

	public List<UserQuery> getQueries() {
		if (queries == null) {
			queries = new ArrayList<UserQuery>();
		}
		return queries;
	}

	public void setQueries(List<UserQuery> queries) {
		this.queries = queries;
	}
}
