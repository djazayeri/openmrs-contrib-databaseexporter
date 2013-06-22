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
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.openmrs.contrib.databaseexporter.util.EventLog;
import org.openmrs.contrib.databaseexporter.util.Util;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ExportContext {

	public static final String TEMPORARY_TABLE_PREFIX = "temp_dbe_";
	private Map<String, String> temporaryTableSet = new HashMap<String, String>();

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
		eventLog = new EventLog(configuration.getLogFile());
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
		TableConfig config = getTableData().get(tableName);
		if (config != null) {
			return config.getTableMetadata();
		}
		return null;
	}

	public <T> T executeQuery(String sql, ResultSetHandler<T> handler, Object...params) {
		try {
			if (getConfiguration().getLogSql() == Boolean.TRUE) {
				log("SQL: " + sql + (params != null && params.length > 0 ? " [" + Util.toString(params) + "]" : ""));
			}
			QueryRunner runner = new QueryRunner();
			T result =  runner.query(connection, sql, handler, params);
			if (getConfiguration().getLogSql() == Boolean.TRUE) {
				log("RESULT: " + result);
			}
			return result;
		}
		catch (Exception e) {
			throw new RuntimeException("Unable to execute query: " + sql, e);
		}
	}

	public Set<Integer> executeIdQuery(String sql) {
		return new HashSet<Integer>(executeQuery(sql, new ColumnListHandler<Integer>()));
	}

	public void executeUpdate(String query) {
		if (getConfiguration().getLogSql() == Boolean.TRUE) {
			log("UPDATE: " + query);
		}
		QueryRunner qr = new QueryRunner();
		try {
			qr.update(connection, query);
		}
		catch (SQLException e) {
			throw new RuntimeException("Unable to execute query: " + query, e);
		}
	}

	public void registerInTemporaryTable(String sourceTable, String sourceColumn, final Collection<Integer> ids) {
		if (ids != null) {
			Set<Integer> toInsert = new HashSet<Integer>(ids);
			toInsert.remove(null);

			String tableAndColumn = sourceTable + "." + sourceColumn;
			String tempTableName = temporaryTableSet.get(tableAndColumn);
			if (tempTableName == null) {
				tempTableName = TEMPORARY_TABLE_PREFIX + sourceTable+"_"+temporaryTableSet.size();
				log("Preparing temporary table " + tempTableName);
				executeUpdate("create temporary table " + tempTableName + " (id integer not null primary key)");
				temporaryTableSet.put(tableAndColumn, tempTableName);
			}

			toInsert.removeAll(executeIdQuery("select id from " + tempTableName));

			if (!toInsert.isEmpty()) {
				log("Adding " + toInsert.size() + " values to " + tempTableName);
				StringBuilder insert = new StringBuilder("insert into " + tempTableName + " (id) values ");
				for (Iterator<Integer> i = toInsert.iterator(); i.hasNext();) {
					Integer id = i.next();
					if (id != null) {
						insert.append("(").append(id).append(")");
						if (i.hasNext()) {
							insert.append(",");
						}
					}
				}
				executeUpdate(insert.toString());
			}
		}
	}

	public String getTemporaryTableName(String tableName, String columnName) {
		return temporaryTableSet.get(tableName + "." + columnName);
	}

	public List<Integer> getTemporaryTableValues(String tableName, String columnName) {
		String tempTableName = getTemporaryTableName(tableName, columnName);
		if (tempTableName != null) {
			return executeQuery("select id from " + tempTableName, new ColumnListHandler<Integer>());
		}
		else {
			return executeQuery("select " + columnName + " from " + tableName, new ColumnListHandler<Integer>());
		}
	}

	public String buildQuery(String tableName, ExportContext context) {
		TableConfig config = context.getTableData().get(tableName);

		StringBuilder query = new StringBuilder("select ").append(tableName).append(".* from ").append(tableName);
		for (String tempTable : temporaryTableSet.keySet()) {
			String[] tableAndColumn = tempTable.split("\\.");
			if (tableAndColumn[0].equals(tableName)) {
				String tempTableName = temporaryTableSet.get(tempTable);
				query.append(" inner join ").append(tempTableName);
				query.append(" on ").append(tableName).append(".").append(tableAndColumn[1]);
				query.append(" = ").append(tempTableName).append(".id");
			}
		}

		return query.toString();
	}

	public void cleanupTemporaryTables() {
		for (String tableName : temporaryTableSet.values()) {
			if (tableName.startsWith(TEMPORARY_TABLE_PREFIX)) {
				executeUpdate("drop temporary table " + tableName);
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

