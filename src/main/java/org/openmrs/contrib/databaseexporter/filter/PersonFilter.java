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
 * Filters Persons and dependent objects from database
 */
public class PersonFilter extends RowFilter {

	//**** CONSTRUCTORS *****

	public PersonFilter() {
		{
			PersonNameFilter f = new PersonNameFilter();
			f.addFilterQuery(new SqlFilterQuery("select person_name_id from person_name inner join person on person_name.person_id = person.person_id"));
			addDependencyFilter(f);
		}

		/*
		addDependencyFilter(new PersonAddressFilter("inner join person on person_address.person_id = person.person_id"));
		addDependencyFilter(new PersonAttributeFilter("inner join person on person_attribute.person_id = person.person_id"));
		addDependencyFilter(new RelatioshipFilter("inner join person on (relationship.person_a = person.person_id or relationship.person_b = person_id)"));
		addDependencyFilter(new ObsFilter("inner join person on obs.person_id = person.person_id"));
		addDependencyFilter(new UserFilter("inner join person on users.person_id = person.person_id"));
		addDependencyFilter(new ProviderFilter("inner join provider on provider.person_id = person.person_id"));
		*/
	}

	//***** INSTANCE METHODS *****

	@Override
	public String getTableName() {
		return "person";
	}

	@Override
	public String getPrimaryKeyColumn() {
		return "person_id";
	}
}
