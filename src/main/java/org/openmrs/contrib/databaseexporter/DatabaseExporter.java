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

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.io.IOUtils;
import org.openmrs.contrib.databaseexporter.filter.DependencyFilter;
import org.openmrs.contrib.databaseexporter.filter.PatientFilter;
import org.openmrs.contrib.databaseexporter.filter.ProviderFilter;
import org.openmrs.contrib.databaseexporter.filter.RowFilter;
import org.openmrs.contrib.databaseexporter.filter.UserFilter;
import org.openmrs.contrib.databaseexporter.transform.RowTransform;
import org.openmrs.contrib.databaseexporter.util.DbUtil;
import org.openmrs.contrib.databaseexporter.util.ListMap;
import org.openmrs.contrib.databaseexporter.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseExporter {

	public DatabaseExporter() { }

	public static void main(String[] args) throws Exception {
		// For now, we simply support the specification of a file path on the command line for the configuration file
		if (args.length == 0) {
			System.out.println("This command expects 1-N arguments referencing the configuration files it should load");
			return;
		}
		List<String> resourceNames = new ArrayList<String>();
		resourceNames.add("defaults");

		Map<String, String> overrides = new HashMap<String, String>();
		for (String arg : args) {
			if (arg.startsWith("-")) {
				String[] split = arg.substring(1).split("\\=");
				overrides.put(split[0].trim(), split[1].trim());
			}
			else {
				resourceNames.add(arg);
			}
		}

		Configuration config = Util.loadConfiguration(resourceNames);

		for (String key : overrides.keySet()) {
			String val = overrides.get(key);
			if (key.equals("user")) {
				config.getSourceDatabaseCredentials().setUser(val);
			}
			else if (key.equals("password")) {
				config.getSourceDatabaseCredentials().setPassword(val);
			}
			else if (key.equals("url")) {
				config.getSourceDatabaseCredentials().setUrl(val);
			}
			else if (key.equals("localDbName")) {
				config.getSourceDatabaseCredentials().setUrl("jdbc:mysql://localhost:3306/" + val + "?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8");
			}
			else if (key.equals("targetDirectory")) {
				config.setTargetDirectory(val);
			}
			else if (key.equals("logSql")) {
				config.setLogSql("true".equals(val));
			}
			else {
				throw new RuntimeException("Unable to set property <" + key + "> outside of configuration files.");
			}
		}

		export(config);
	}

	public static void export(final Configuration configuration) throws Exception {

		FileOutputStream fos = null;
		Connection connection = null;
		try {
			connection = DbUtil.openConnection(configuration);

			if (configuration.getTargetDirectory() != null) {
				File f = new File(configuration.getTargetDirectory());
				if (!f.exists()) {
					f.mkdirs();
				}
			}
			File outputFile = configuration.getOutputFile();

			fos = new FileOutputStream(outputFile);
			OutputStreamWriter osWriter = new OutputStreamWriter(fos, "UTF-8");
			PrintWriter out = new PrintWriter(osWriter);

			final ExportContext context = new ExportContext(configuration, out);
			context.log("Context initialized");

			DbUtil.writeExportHeader(context);

			try {
				List<RowFilter> rowFilters = new ArrayList<RowFilter>();
				rowFilters.add(Util.nvl(configuration.getPatientFilter(), new PatientFilter()));
				rowFilters.add(Util.nvl(configuration.getUserFilter(), new UserFilter()));
				rowFilters.add(Util.nvl(configuration.getProviderFilter(), new ProviderFilter()));

				for (RowFilter filter : rowFilters) {
					context.log("Applying filter: " + filter.getClass().getSimpleName());
					filter.filter(context);
				}
				for (RowFilter filter : rowFilters) {
					for (DependencyFilter df : filter.getDependencyFilters()) {
						df.filter(context);
					}
				}

				ListMap<String, RowTransform> tableTransforms = new ListMap<String, RowTransform>(true);

				for (final String table : context.getTableData().keySet()) {
					TableConfig tableConfig = context.getTableData().get(table);

					if (tableConfig.isExportSchema()) {
						DbUtil.writeTableSchema(table, context);
						context.log(table + " schema exported");
					}

					if (tableConfig.isExportData()) {
						context.log("Starting " + table + " data export");

						DbUtil.writeTableExportHeader(table, context);

						context.log("Constructing query");
						String query = context.buildQuery(table, context);

						context.log("Determining applicable transforms for table");
						for (RowFilter filter : rowFilters) {
							tableTransforms.putAll(table, filter.getTransforms());
						}

						for (RowTransform transform : configuration.getRowTransforms()) {
							if (transform.canTransform(table, context)) {
								tableTransforms.putInList(table, transform);
							}
						}

						final List<RowTransform> transforms = Util.nvl(tableTransforms.get(table), new ArrayList<RowTransform>());

						context.log("Determining number of rows for table");
						String rowNumQuery = query.replace(table+".*", "count(*)");
						final Long totalRows = context.executeQuery(rowNumQuery, new ScalarHandler<Long>());
						final int batchSize = context.getConfiguration().getBatchSize();

						context.log("***************************** Executing query **************************");
						context.log("Query: " + query);
						context.log("Transforms: " + transforms);
						context.log("Total Rows: " + totalRows);

						Integer rowsAdded = context.executeQuery(query, new ResultSetHandler<Integer>() {

							public Integer handle(ResultSet rs) throws SQLException {

								List<TableRow> results = new ArrayList<TableRow>();
								ResultSetMetaData md = rs.getMetaData();
								int numColumns = md.getColumnCount();

								int rowsChecked = 0;
								int rowsAdded = 0;
								int rowIndex = 0;

								while (rs.next()) {
									rowsChecked++;

									TableRow row = new TableRow(table);
									for (int i = 1; i <= numColumns; i++) {
										String columnName = md.getColumnName(i);
										ColumnValue value = new ColumnValue(table, columnName, md.getColumnType(i), rs.getObject(i));
										row.addColumnValue(columnName, value);
									}
									boolean includeRow = true;
									for (RowTransform transform : transforms) {
										includeRow = includeRow && transform.transformRow(row, context);
									}
									if (includeRow) {
										rowsAdded++;
										rowIndex = (rowIndex >= batchSize ? 0 : rowIndex) + 1;
										DbUtil.writeInsertRow(row, rowIndex, rowsAdded, context);
									}
									if (rowsChecked % 1000 == 0) {
										context.log("Processed " + table + " rows " + (rowsChecked - 1000) + " to " + rowsChecked + " (" + Util.toPercent(rowsChecked, totalRows, 0) + "%)");
									}
								}
								return rowsAdded;
							}
						});
						if (rowsAdded % batchSize != 0) {
							context.write(";");
							context.write("");
						}
						context.log(rowsAdded + " rows retrieved and transformed from initial queries");
						context.log("********************************************************************");

						DbUtil.writeTableExportFooter(table, context);
					}
				}

				// Handle any post-processing transforms that have been defined
				for (String table : tableTransforms.keySet()) {
					List<TableRow> rows = new ArrayList<TableRow>();
					for (RowTransform transform : tableTransforms.get(table)) {
						rows.addAll(transform.postProcess(table, context));
					}
					if (rows != null && !rows.isEmpty()) {
						DbUtil.writeTableExportHeader(table, context);
						for (int i=1; i<=rows.size(); i++) {
							TableRow row = rows.get(i-1);
							DbUtil.writeInsertRow(row, i, i, context);
						}
						context.write(";");
						context.write("");
						DbUtil.writeTableExportFooter(table, context);
					}
				}
			}
			catch (Exception e) {
				context.log("An error occurred during export: " + e.getMessage());
				e.printStackTrace(System.out);
			}
			finally {
				context.log("Cleaning up temporary tables");
				context.cleanupTemporaryTables();
			}

			DbUtil.writeExportFooter(context);

			context.log("Exporting Database Completed");

			context.log("***** Summary Data *****");
			for (final String table : context.getTableData().keySet()) {
				TableConfig tableConfig = context.getTableData().get(table);
				context.log(tableConfig.getTableMetadata().getTableName() + ": " + tableConfig.getNumRowsExported());
			}
			context.log("**************************");
			context.log("Export completed in: " + Util.formatTimeDifference(context.getEventLog().getTotalTime()));

			out.flush();
		}
		finally {
			IOUtils.closeQuietly(fos);
			DbUtil.closeConnection(connection);
		}
	}
}

