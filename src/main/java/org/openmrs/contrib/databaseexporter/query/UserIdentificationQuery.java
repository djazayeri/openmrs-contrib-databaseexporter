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
package org.openmrs.contrib.databaseexporter.query;

import org.openmrs.contrib.databaseexporter.ExportContext;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Return the ids represented by the union of all of the passed query results
 */
public class UserIdentificationQuery extends UserQuery {

	//***** PROPERTIES *****

	private Set<String> userNames;

	//***** CONSTRUCTORS *****

	public UserIdentificationQuery() {}

	public UserIdentificationQuery(String... userNames) {
		if (userNames != null) {
			for (String u : userNames) {
				getUserNames().add(u);
			}
		}
	}

	//***** INSTANCE METHODS *****

	@Override
	public Set<Integer> getIds(ExportContext context) {
		Set<Integer> ret = new HashSet<Integer>();
		if (getUserNames() != null && !getUserNames().isEmpty()) {
			StringBuilder q = new StringBuilder();
			q.append("select user_id from users where username in (");
			for (Iterator<String> i = getUserNames().iterator(); i.hasNext();) {
				q.append("'").append(i.next()).append("'").append(i.hasNext() ? "," : "");
			}
			q.append(")");
			ret.addAll(context.executeIdQuery(q.toString()));
		}
		return ret;
	}

	//***** ACCESSOR *****

	public Set<String> getUserNames() {
		if (userNames == null) {
			userNames = new HashSet<String>();
		}
		return userNames;
	}

	public void setUserNames(Set<String> userNames) {
		this.userNames = userNames;
	}
}
