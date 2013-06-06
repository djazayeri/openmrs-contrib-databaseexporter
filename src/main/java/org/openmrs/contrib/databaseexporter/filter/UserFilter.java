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
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.openmrs.contrib.databaseexporter.ExportContext;
import org.openmrs.contrib.databaseexporter.transform.SimpleReplacementTransform;
import org.openmrs.contrib.databaseexporter.util.DbUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Row filter which filters the users included in the export
 */
public class UserFilter extends RowFilter {

	//***** PROPERTIES *****

	private List<Integer> limitToUsers;

	//***** CONSTRUCTORS *****

	public UserFilter() { }

	//***** INSTANCE METHODS *****

	@Override
	public void applyFilters(ExportContext context) {
		if (!getLimitToUsers().isEmpty()) {

			// If we are limiting users, we still need to ensure we keep admin and daemon
			Set<Integer> l = new HashSet<Integer>(getLimitToUsers());
			l.addAll(context.executeQuery("select user_id from users where username in ('admin','daemon')", new ColumnListHandler<Integer>()));

			// Limit the known tables that are really user data and should be removed if we are not keeping certain users
			applyConstraints("users", "user_id", l, context);
			applyConstraints("user_property", "user_id", l, context);
			applyConstraints("user_role", "user_id", l, context);
			applyConstraints("notification_alert_recipient", "user_id", l, context);
			applyConstraints("usagestatistics_daily", "user_id", l, context);
			applyConstraints("usagestatistics_usage", "user_id", l, context);

			// Since we are limiting the users, add a transform that sets the person on the user
			Integer adminPerson = context.executeQuery("select person_id from users where username = 'admin'", new ScalarHandler<Integer>());

			SimpleReplacementTransform userPersonTransform = new SimpleReplacementTransform();
			userPersonTransform.setTableAndColumnList(Arrays.asList("users.person_id"));
			userPersonTransform.setReplacement(adminPerson);
			context.getConfiguration().getRowTransforms().add(userPersonTransform);
		}
	}

	public List<Integer> getLimitToUsers() {
		if (limitToUsers == null) {
			limitToUsers = new ArrayList<Integer>();
		}
		return limitToUsers;
	}

	public void setLimitToUsers(List<Integer> limitToUsers) {
		this.limitToUsers = limitToUsers;
	}
}