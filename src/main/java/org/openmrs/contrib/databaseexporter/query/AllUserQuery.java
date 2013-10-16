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

import java.util.Set;

/**
 * Return the ids represented by the union of all of the passed query results
 */
public class AllUserQuery extends UserQuery {

	//***** CONSTRUCTORS *****

	public AllUserQuery() {}

	//***** INSTANCE METHODS *****

	@Override
	public Set<Integer> getIds(ExportContext context) {
		return context.executeIdQuery("select user_id from users");
	}
}
