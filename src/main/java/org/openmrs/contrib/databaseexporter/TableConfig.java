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

/**
 * Contains all details about how a table is configured for export
 */
public class TableConfig {

	//***** PROPERTIES *****

	private TableMetadata tableMetadata;
	private boolean exportSchema = true;
	private boolean exportData = true;

	//***** CONSTRUCTORS *****

	public TableConfig(TableMetadata tableMetadata) {
		this.tableMetadata = tableMetadata;
	}

	//***** METHODS *****

	public TableMetadata getTableMetadata() {
		return tableMetadata;
	}

	public void setTableMetadata(TableMetadata tableMetadata) {
		this.tableMetadata = tableMetadata;
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
}
