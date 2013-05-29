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

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.openmrs.contrib.databaseexporter.filter.RowFilter;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class ExportContext {

	//***** PROPERTIES *****

	private Configuration configuration;
	private Connection connection;
	private PrintWriter writer;
	private Map<String, TableConfig> tableData;

	//***** CONSTRUCTOR *****

	public ExportContext(Configuration configuration, Connection connection, PrintWriter writer) {
		this.configuration = configuration;
		this.connection = connection;
		this.writer = writer;
		tableData = configuration.getTableFilter().getTablesForExport(this);
		for (RowFilter filter : configuration.getRowFilters()) {
			filter.filter(this);
		}
	}

	//***** METHODS *****

	public void write(String s) {
		writer.println(s);
	}

	public <T> T executeQuery(String sql, ResultSetHandler<T> handler, Object...params) {
		try {
			System.out.println("Query: " + sql + (params != null ? " (" + Util.toString(params) + ")" : ""));
			QueryRunner runner = new QueryRunner();
			return runner.query(connection, sql, handler, params);
		}
		catch (Exception e) {
			throw new RuntimeException("Unable to execute query: " + sql, e);
		}
	}

	//***** PROPERTY ACCESS *****

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public PrintWriter getWriter() {
		return writer;
	}

	public void setWriter(PrintWriter writer) {
		this.writer = writer;
	}

	public Map<String, TableConfig> getTableData() {
		if (tableData == null) {
			tableData = new TreeMap<String, TableConfig>();
		}
		return tableData;
	}

	public void setTableData(Map<String, TableConfig> tableData) {
		this.tableData = tableData;
	}

	public void addTableData(String tableName, TableConfig tableData) {
		getTableData().put(tableName, tableData);
	}
}

