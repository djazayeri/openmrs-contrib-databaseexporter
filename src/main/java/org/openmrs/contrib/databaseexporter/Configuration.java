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

import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.contrib.databaseexporter.filter.RowFilter;
import org.openmrs.contrib.databaseexporter.filter.TableFilter;
import org.openmrs.contrib.databaseexporter.transform.RowTransform;

import java.util.ArrayList;
import java.util.List;

public class Configuration {

	//***** PROPERTIES *****

	private DatabaseCredentials sourceDatabaseCredentials;
	private String targetLocation;
	private Integer batchSize = 10000;

	private TableFilter tableFilter;
	private List<RowFilter> rowFilters;
	private List<RowTransform> rowTransforms;

	//***** CONSTRUCTORS *****

	public Configuration() {}

	public static Configuration loadFromJson(String json) {
		Configuration config = new Configuration();
		try {
			ObjectMapper mapper = new ObjectMapper();
			config = mapper.readValue(json, Configuration.class);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unable to load configuration from JSON: " + json, e);
		}
		return config;
	}

	//***** PROPERTY ACCESS *****

	public String getTargetLocation() {
		return targetLocation;
	}

	public void setTargetLocation(String targetLocation) {
		this.targetLocation = targetLocation;
	}

	public Integer getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(Integer batchSize) {
		this.batchSize = batchSize;
	}

	public DatabaseCredentials getSourceDatabaseCredentials() {
		if (sourceDatabaseCredentials == null) {
			sourceDatabaseCredentials = new DatabaseCredentials();
		}
		return sourceDatabaseCredentials;
	}

	public void setSourceDatabaseCredentials(DatabaseCredentials sourceDatabaseCredentials) {
		this.sourceDatabaseCredentials = sourceDatabaseCredentials;
	}

	public TableFilter getTableFilter() {
		if (tableFilter == null) {
			tableFilter = new TableFilter();
		}
		return tableFilter;
	}

	public void setTableFilter(TableFilter tableFilter) {
		this.tableFilter = tableFilter;
	}

	public List<RowFilter> getRowFilters() {
		if (rowFilters == null) {
			rowFilters = new ArrayList<RowFilter>();
		}
		return rowFilters;
	}

	public void setRowFilters(List<RowFilter> rowFilters) {
		this.rowFilters = rowFilters;
	}

	public List<RowTransform> getRowTransforms() {
		if (rowTransforms == null) {
			rowTransforms = new ArrayList<RowTransform>();
		}
		return rowTransforms;
	}

	public void setRowTransforms(List<RowTransform> rowTransforms) {
		this.rowTransforms = rowTransforms;
	}
}
