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
import org.openmrs.contrib.databaseexporter.filter.TableFilter;
import org.openmrs.contrib.databaseexporter.filter.RowFilter;
import org.openmrs.contrib.databaseexporter.transform.RowTransform;
import org.openmrs.contrib.databaseexporter.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration {

	//***** PROPERTIES *****

	private DatabaseCredentials sourceDatabaseCredentials;
	private String targetDirectory;
	private Integer batchSize = 10000;

	private TableFilter tableFilter;
	private List<RowFilter> rowFilters;
	private List<RowTransform> rowTransforms;
	private Map<String, DependencyFilter> dependencyFilters;

	//***** CONSTRUCTORS *****

	public Configuration() {}

	public static Configuration getDefaultConfiguration() {
		return loadFromResource("org/openmrs/contrib/databaseexporter/defaultConfiguration.json");
	}

	public static Configuration loadFromResource(String resource) {
		String json = Util.loadResource(resource);
		return loadFromJson(json);
	}

	public static Configuration loadFromJson(String json) {
		Configuration config;
		try {
			ObjectMapper mapper = new ObjectMapper();
			config = mapper.readValue(json, Configuration.class);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unable to load configuration from JSON: " + json, e);
		}
		return config;
	}

	public void merge(Configuration config) {
		if (config.getSourceDatabaseCredentials() != null) {
			setSourceDatabaseCredentials(config.getSourceDatabaseCredentials());
		}
		if (config.getTargetDirectory() != null) {
			setTargetDirectory(config.getTargetDirectory());
		}
		if (config.getTableFilter() != null) {
			setTableFilter(config.getTableFilter());
		}
		if (config.getRowFilters().size() > 0) {
			setRowFilters(config.getRowFilters());
		}
		if (config.getRowTransforms().size() > 0) {
			setRowTransforms(config.getRowTransforms());
		}
		if (config.getDependencyFilters().size() > 0) {
			setDependencyFilters(config.getDependencyFilters());
		}
	}

	//***** PROPERTY ACCESS *****

	public String getTargetDirectory() {
		return targetDirectory;
	}

	public void setTargetDirectory(String targetDirectory) {
		this.targetDirectory = targetDirectory;
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

	public Map<String, DependencyFilter> getDependencyFilters() {
		if (dependencyFilters == null) {
			dependencyFilters = new HashMap<String, DependencyFilter>();
		}
		return dependencyFilters;
	}

	public void setDependencyFilters(Map<String, DependencyFilter> dependencyFilters) {
		this.dependencyFilters = dependencyFilters;
	}
}
