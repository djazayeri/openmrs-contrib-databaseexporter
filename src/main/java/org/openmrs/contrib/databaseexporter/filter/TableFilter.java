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
import org.openmrs.contrib.databaseexporter.TableConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Configurable class which can return all tables that are included for an Export
 */
public class TableFilter {

	//***** PROPERTIES *****

	private List<String> includeSchema;
	private List<String> excludeSchema;
	private List<String> includeData;
	private List<String> excludeData;

	//***** CONSTRUCTORS *****

	public TableFilter() {}

	//***** METHODS *****

	public Map<String, TableConfig> getTablesForExport(ExportContext context) {
		Map<String, TableConfig> tablesToDump = new TreeMap<String, TableConfig>();

		String allTableQuery = "SELECT table_name FROM INFORMATION_SCHEMA.TABLES WHERE table_schema = database()";
		List<String> allTables = context.executeQuery(allTableQuery, new ColumnListHandler<String>());

		for (String tableName : allTables) {
			boolean includeSchema = getIncludeSchema().isEmpty() || isInList(tableName, getIncludeSchema());
			includeSchema = includeSchema && !isInList(tableName, getExcludeSchema());
			if (includeSchema) {
				TableConfig config = new TableConfig(tableName);
				boolean includeData = getIncludeData().isEmpty() || isInList(tableName, getIncludeData());
				includeData = includeData && !isInList(tableName, getExcludeData());
				config.setExportData(includeData);
				tablesToDump.put(tableName, config);
			}
		}
		return tablesToDump;
	}

	protected boolean isInList(String tableName, List<String> expressions) {
		if (expressions != null) {
			for (String t : expressions) {
				if (tableName.equalsIgnoreCase(t) || (t.endsWith("*") && tableName.startsWith(t.substring(0, t.length()-1)))) {
					return true;
				}
			}
		}
		return false;
	}

	//***** ACCESSORS *****

	public List<String> getIncludeSchema() {
		if (includeSchema == null) {
			includeSchema = new ArrayList<String>();
		}
		return includeSchema;
	}

	public void setIncludeSchema(List<String> includeSchema) {
		this.includeSchema = includeSchema;
	}

	public List<String> getExcludeSchema() {
		if (excludeSchema == null) {
			excludeSchema = new ArrayList<String>();
		}
		return excludeSchema;
	}

	public void setExcludeSchema(List<String> excludeSchema) {
		this.excludeSchema = excludeSchema;
	}

	public List<String> getIncludeData() {
		if (includeData == null) {
			includeData = new ArrayList<String>();
		}
		return includeData;
	}

	public void setIncludeData(List<String> includeData) {
		this.includeData = includeData;
	}

	public List<String> getExcludeData() {
		if (excludeData == null) {
			excludeData = new ArrayList<String>();
		}
		return excludeData;
	}

	public void setExcludeData(List<String> excludeData) {
		this.excludeData = excludeData;
	}
}
