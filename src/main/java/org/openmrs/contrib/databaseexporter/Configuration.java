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
import org.openmrs.contrib.databaseexporter.filter.DependencyFilter;
import org.openmrs.contrib.databaseexporter.filter.PatientFilter;
import org.openmrs.contrib.databaseexporter.filter.ProviderFilter;
import org.openmrs.contrib.databaseexporter.filter.TableFilter;
import org.openmrs.contrib.databaseexporter.filter.UserFilter;
import org.openmrs.contrib.databaseexporter.transform.RowTransform;
import org.openmrs.contrib.databaseexporter.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration {

	public static final String CONFIG_PATH = "org/openmrs/contrib/databaseexporter/configuration/";

	//***** PROPERTIES *****

	private DatabaseCredentials sourceDatabaseCredentials;
	private String targetDirectory;
	private Integer batchSize = 10000;
	private Boolean logSql;

	private TableFilter tableFilter;
	private PatientFilter patientFilter;
	private UserFilter userFilter;
	private ProviderFilter providerFilter;

	private List<RowTransform> rowTransforms;
	private Map<String, DependencyFilter> dependencyFilters;

	//***** CONSTRUCTORS *****

	public Configuration() {}

	public File getOutputFile() {
		String fileSuffix = Util.formatDate(new Date(), "yyyy_MM_dd_HH_mm");
		String dir = getTargetDirectory();
		if (Util.isEmpty(dir)) {
			dir = System.getProperty("user.dir");
		}
		return new File(dir, "export_"+ fileSuffix + ".sql");
	}

	public File getLogFile() {
		String fileSuffix = Util.formatDate(new Date(), "yyyy_MM_dd_HH_mm");
		String dir = getTargetDirectory();
		if (Util.isEmpty(dir)) {
			dir = System.getProperty("user.dir");
		}
		return new File(dir, "export_"+ fileSuffix + ".log");
	}

	public String toString() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(this);
		}
		catch (Exception e) {
			return super.toString();
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

	public Boolean getLogSql() {
		return logSql;
	}

	public void setLogSql(Boolean logSql) {
		this.logSql = logSql;
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

	public PatientFilter getPatientFilter() {
		return patientFilter;
	}

	public void setPatientFilter(PatientFilter patientFilter) {
		this.patientFilter = patientFilter;
	}

	public UserFilter getUserFilter() {
		return userFilter;
	}

	public void setUserFilter(UserFilter userFilter) {
		this.userFilter = userFilter;
	}

	public ProviderFilter getProviderFilter() {
		return providerFilter;
	}

	public void setProviderFilter(ProviderFilter providerFilter) {
		this.providerFilter = providerFilter;
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
