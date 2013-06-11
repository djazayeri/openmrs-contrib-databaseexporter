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
import org.openmrs.contrib.databaseexporter.util.ListMap;

/**
 * Filter given a fixed set of ids
 */
public class IdRowFilter extends RowFilter {

	//***** PROPERTIES *****

	private String tableName;
	private ListMap<String, Integer> ids;

	//***** CONSTRUCTORS *****

	public IdRowFilter() {}

	//***** INSTANCE METHODS *****

	@Override
	public ListMap<String, Integer> getIds(ExportContext context) {
		return getIds();
	}

	public void addIds(String columnName, Integer... ids) {
		for (Integer id : ids) {
			getIds().putInList(columnName, id);
		}
	}

	//***** PROPERTY ACCESS *****

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public ListMap<String, Integer> getIds() {
		if (ids == null) {
			ids = new ListMap<String, Integer>();
		}
		return ids;
	}

	public void setIds(ListMap<String, Integer> ids) {
		this.ids = ids;
	}
}
