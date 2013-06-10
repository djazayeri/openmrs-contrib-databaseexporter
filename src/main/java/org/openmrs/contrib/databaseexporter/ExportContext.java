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
import org.openmrs.contrib.databaseexporter.util.EventLog;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ExportContext {

	public static final String TEMPORARY_TABLE_PREFIX = "temp_databaseexporter_";
	private Set<String> temporaryTableSet = new HashSet<String>();

	//***** PROPERTIES *****

	private Configuration configuration;
	private Connection connection;
	private PrintWriter writer;
	private EventLog eventLog;
	private Map<String, TableConfig> tableData;

	//***** CONSTRUCTOR *****

	public ExportContext(Configuration configuration, Connection connection, PrintWriter writer) {
		this.configuration = configuration;
		this.connection = connection;
		this.writer = writer;
		eventLog = new EventLog();
		tableData = configuration.getTableFilter().getTablesForExport(this);
	}

	//***** METHODS *****

	public void write(String s) {
		writer.println(s);
	}

	public void log(String eventName) {
		eventLog.logEvent(eventName);
	}

	public TableMetadata getTableMetadata(String tableName) {
		return getTableData().get(tableName).getTableMetadata();
	}

	public <T> T executeQuery(String sql, ResultSetHandler<T> handler, Object...params) {
		try {
			QueryRunner runner = new QueryRunner();
			return runner.query(connection, sql, handler, params);
		}
		catch (Exception e) {
			throw new RuntimeException("Unable to execute query: " + sql, e);
		}
	}

	public void executeUpdate(String query) {
		QueryRunner qr = new QueryRunner();
		try {
			qr.update(connection, query);
		}
		catch (SQLException e) {
			throw new RuntimeException("Unable to execute query: " + query, e);
		}
	}

	public void registerInTemporaryTable(String sourceTable, String primaryKeyColumn, Collection<Integer> ids) {

		String tempTableName = TEMPORARY_TABLE_PREFIX + sourceTable;

		log("Preparing temporary table " + tempTableName + " by adding " + ids.size() + " values to " + primaryKeyColumn);

		executeUpdate("create temporary table if not exists " + tempTableName + " (" + primaryKeyColumn + " integer not null primary key)");

		StringBuilder insert = new StringBuilder("insert into " + tempTableName + " (" + primaryKeyColumn + ") values ");
		for (Iterator<Integer> i = ids.iterator(); i.hasNext();) {
			Integer id = i.next();
			insert.append("(").append(id).append(")");
			if (i.hasNext()) {
				insert.append(",");
			}
		}
		executeUpdate(insert.toString());
		getTableData().get(sourceTable).setTemporaryTableName(tempTableName);
		getTableData().get(sourceTable).setPrimaryKeyName(primaryKeyColumn);
	}

	public String buildQuery(String tableName, ExportContext context) {
		TableConfig config = context.getTableData().get(tableName);

		StringBuilder query = new StringBuilder("select * from " + tableName);
		if (config.getTemporaryTableName() != null) {
			query.append(" inner join ").append(config.getTemporaryTableName());
			query.append(" on ").append(tableName).append(".").append(config.getPrimaryKeyName());
			query.append(" = ").append(config.getTemporaryTableName()).append(".").append(config.getPrimaryKeyName());
		}

		return query.toString();
	}

	public void cleanupTemporaryTables() {
		for (String tableName : temporaryTableSet) {
			if (tableName.startsWith(TEMPORARY_TABLE_PREFIX)) {
				executeUpdate("drop table " + tableName);
			}
		}
		temporaryTableSet.clear();
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

	public EventLog getEventLog() {
		return eventLog;
	}

	public void setEventLog(EventLog eventLog) {
		this.eventLog = eventLog;
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

