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
package org.openmrs.contrib.databaseexporter;

import org.openmrs.contrib.databaseexporter.util.ListMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates relevant metadata for a table
 */
public class TableMetadata {

	private String tableName;
	private Set<String> primaryKeys;
	private ListMap<String, String> foreignKeyMap;

	public TableMetadata(String tableName) {
		this.tableName = tableName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public boolean isPrimaryKey(String columnName) {
		return getPrimaryKeys().contains(columnName);
	}

	public Set<String> getPrimaryKeys() {
		if (primaryKeys == null) {
			primaryKeys = new HashSet<String>();
		}
		return primaryKeys;
	}

	public void setPrimaryKeys(Set<String> primaryKeys) {
		this.primaryKeys = primaryKeys;
	}

	public List<String> getForeignKeys(String columnName) {
		List<String> ret = getForeignKeyMap().get(columnName);
		return (ret == null ? new ArrayList<String>() : ret);
	}

	public ListMap<String, String> getForeignKeyMap() {
		if (foreignKeyMap == null) {
			foreignKeyMap = new ListMap<String, String>();
		}
		return foreignKeyMap;
	}

	public void setForeignKeyMap(ListMap<String, String> foreignKeyMap) {
		this.foreignKeyMap = foreignKeyMap;
	}
}
