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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openmrs.contrib.databaseexporter.filter.RowFilter;
import org.openmrs.contrib.databaseexporter.transform.RowTransform;
import org.openmrs.contrib.databaseexporter.transform.TableTransform;
import org.openmrs.contrib.databaseexporter.util.DbUtil;
import org.openmrs.contrib.databaseexporter.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DatabaseExporter {

	public DatabaseExporter() { }

	public static void main(String[] args) {
		// For now, we simply support the specification of a file path on the command line for the configuration file
		if (args.length == 0) {
			System.out.println("You must specify a single argument, which is the path to the configuration file.");
			return;
		}
		File f = new File(args[0]);
		if (!f.exists()) {
			System.out.println("The configuration file path specified is not valid.");
			return;
		}
		try {
			String json = FileUtils.readFileToString(f);
			Configuration config = Configuration.loadFromJson(json);
			export(config);
		}
		catch (IOException ioe) {
			System.out.println("There was an error reading configuration file");
			ioe.printStackTrace();
		}
		catch (Exception e) {
			System.out.println("There is an error in your configuration syntax");
			e.printStackTrace();
		}
	}

	public static void export(final Configuration configuration) throws Exception {

		FileOutputStream fos = null;
		Connection connection = null;
		try {
			connection = DbUtil.openConnection(configuration.getSourceDatabaseCredentials());

			fos = new FileOutputStream(configuration.getTargetLocation());
			OutputStreamWriter osWriter = new OutputStreamWriter(fos, "UTF-8");
			PrintWriter out = new PrintWriter(osWriter);

			final ExportContext context = new ExportContext(configuration, connection, out);
			context.log("Context initialized");

			for (RowFilter filter : configuration.getRowFilters()) {
				context.log("Applying filter: " + filter.getClass().getSimpleName());
				filter.applyFilters(context);
			}
			context.log(configuration.getRowFilters().size() + " Row filters completed");

			DbUtil.writeExportHeader(context);

			context.log("Preparing temporary tables based on the filter results");
			QueryBuilder queryBuilder = new QueryBuilder();
			try {
				queryBuilder.prepareTemporaryTablesForExport(context);

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
						String query = queryBuilder.buildQuery(table, context);

						context.log("Determining applicable transforms for table");
						List<RowTransform> transforms = new ArrayList<RowTransform>();
						List<TableTransform> tableTransforms = new ArrayList<TableTransform>();
						for (RowTransform transform : configuration.getRowTransforms()) {
							if (transform.canTransform(table, context)) {
								transforms.add(transform);
								if (transform instanceof TableTransform) {
									tableTransforms.add((TableTransform)transform);
								};
							}
						}

						context.log("Determining number of rows for table");
						String rowNumQuery = query.replace("*", "count(*)");
						final Long totalRows = context.executeQuery(rowNumQuery, new ScalarHandler<Long>());

						context.log("***************************** Executing query **************************");
						context.log("Query: " + query);
						context.log("Transforms: " + transforms);
						context.log("Total Rows: " + totalRows);
						context.log("********************************************************************");

						Integer rowsAdded = context.executeQuery(query, new ResultSetHandler<Integer>() {

							public Integer handle(ResultSet rs) throws SQLException {

								List<TableRow> results = new ArrayList<TableRow>();
								ResultSetMetaData md = rs.getMetaData();
								int numColumns = md.getColumnCount();

								int rowsChecked = 0;
								int rowsAdded = 0;
								while (rs.next()) {
									rowsChecked++;

									TableRow row = new TableRow(table);
									for (int i = 1; i <= numColumns; i++) {
										String columnName = md.getColumnName(i);
										ColumnValue value = new ColumnValue(table, columnName, md.getColumnType(i), rs.getObject(i));
										row.addColumnValue(columnName, value);
									}
									boolean includeRow = true;
									for (RowTransform transform : configuration.getRowTransforms()) {
										includeRow = includeRow && transform.applyTransform(row, context);
									}
									if (includeRow) {
										rowsAdded++;
										DbUtil.writeInsertRow(row, rowsAdded, context);
									}
									if (rowsChecked % 1000 == 0) {
										context.log("Processed " + table + " rows " + (rowsChecked - 1000) + " to " + rowsChecked + " (" + Util.toPercent(rowsChecked, totalRows, 0) + "%)");
									}
								}
								return rowsAdded;
							}
						});
						context.log(rowsAdded + " rows retrieved and transformed from initial queries");

						// Now that we have retrieved and transformed existing values, apply any whole-table transforms
						for (TableTransform transform : tableTransforms) {
							context.log("Applying table transform: " + transform);
							for (TableRow row : transform.getNewRows(table, context)) {
								rowsAdded++;
								DbUtil.writeInsertRow(row, rowsAdded, context);
							}
						}

						if (rowsAdded > 0) {
							out.println(";");
						}

						context.log(rowsAdded + " rows exported");

						DbUtil.writeTableExportFooter(table, context);
					}
				}
			}
			finally {
				context.log("Cleaning up temporary tables");
				queryBuilder.cleanupTemporaryTables(context);
			}

			DbUtil.writeExportFooter(context);

			context.log("Exporting Database Completed");

			System.out.println("Export completed in: " + Util.formatTimeDifference(context.getEventLog().getTotalTime()));

			out.flush();
		}
		finally {
			IOUtils.closeQuietly(fos);
			DbUtil.closeConnection(connection);
		}
	}
}

