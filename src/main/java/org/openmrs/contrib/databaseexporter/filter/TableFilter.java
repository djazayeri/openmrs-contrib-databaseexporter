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
import org.openmrs.contrib.databaseexporter.TableConfig;
import org.openmrs.contrib.databaseexporter.TableMetadata;
import org.openmrs.contrib.databaseexporter.util.DbUtil;
import org.openmrs.contrib.databaseexporter.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

		Map<String, TableMetadata> tableMetadataMap = DbUtil.getTableMetadata(context);
		for (String tableName : tableMetadataMap.keySet()) {
			TableConfig config = new TableConfig(tableMetadataMap.get(tableName));

			boolean exportSchema = getIncludeSchema().isEmpty() || Util.matchesAnyPattern(tableName, getIncludeSchema());
			exportSchema = exportSchema && !Util.matchesAnyPattern(tableName, getExcludeSchema());

			boolean exportData = getIncludeData().isEmpty() || Util.matchesAnyPattern(tableName, getIncludeData());
			exportData = exportData && !Util.matchesAnyPattern(tableName, getExcludeData());

			config.setExportSchema(exportSchema);
			config.setExportData(exportData);

			tablesToDump.put(tableName, config);
		}
		return tablesToDump;
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
