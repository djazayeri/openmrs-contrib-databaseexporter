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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Configurable class which can return all tables that are included for an Export
 */
public class TableConfig {

	//***** PROPERTIES *****

	private String tableName;
	private boolean exportSchema = true;
	private boolean exportData = true;
	private ListMap<String, Object> includeConstraints;
	private ListMap<String, Object> excludeConstraints;

	//***** CONSTRUCTORS *****

	public TableConfig(String tableName) {
		this.tableName = tableName;
	}

	//***** METHODS *****

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public boolean isExportSchema() {
		return exportSchema;
	}

	public void setExportSchema(boolean exportSchema) {
		this.exportSchema = exportSchema;
	}

	public boolean isExportData() {
		return exportData;
	}

	public void setExportData(boolean exportData) {
		this.exportData = exportData;
	}

	public ListMap<String, Object> getIncludeConstraints() {
		if (includeConstraints == null) {
			includeConstraints = new ListMap<String, Object>();
		}
		return includeConstraints;
	}

	public ListMap<String, Object> getExcludeConstraints() {
		if (excludeConstraints == null) {
			excludeConstraints = new ListMap<String, Object>();
		}
		return excludeConstraints;
	}

	public Set<String> getAllConstrainedColumns() {
		Set<String> constrainedColumns = new HashSet();
		constrainedColumns.addAll(getIncludeConstraints().keySet());
		constrainedColumns.addAll(getExcludeConstraints().keySet());
		return constrainedColumns;
	}
}
