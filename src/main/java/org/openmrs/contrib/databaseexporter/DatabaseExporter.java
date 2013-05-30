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
import org.apache.commons.io.IOUtils;
import org.openmrs.contrib.databaseexporter.filter.RowFilter;
import org.openmrs.contrib.databaseexporter.util.DbUtil;

import java.io.FileOutputStream;
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

	public static void export(Configuration configuration) throws Exception {

		FileOutputStream fos = null;
		Connection connection = null;
		try {
			connection = DbUtil.openConnection(configuration.getSourceDatabaseCredentials());

			fos = new FileOutputStream(configuration.getTargetLocation());
			OutputStreamWriter osWriter = new OutputStreamWriter(fos, "UTF-8");
			PrintWriter out = new PrintWriter(osWriter);

			ExportContext context = new ExportContext(configuration, connection, out);

			for (RowFilter filter : configuration.getRowFilters()) {
				filter.applyFilters(context);
			}

			DbUtil.writeExportHeader(context);

			for (final String table : context.getTableData().keySet()) {
				TableConfig tableConfig = context.getTableData().get(table);

				if (tableConfig.isExportSchema()) {
					DbUtil.writeTableSchema(table, context);
				}

				if (tableConfig.isExportData()) {

					DbUtil.writeTableExportHeader(table, context);

					StringBuilder query = new StringBuilder("select * from " + table);
					if (tableConfig.getColumnConstraints() != null && !tableConfig.getColumnConstraints().isEmpty()) {
						for (String columnName : tableConfig.getColumnConstraints().keySet()) {
							List<Object> columnValues = tableConfig.getColumnConstraints().get(columnName);
							query.append(" where ").append(columnName);
							query.append(" in (");
							for (Iterator<Object> i = columnValues.iterator(); i.hasNext();) {
								Object columnValue = i.next();
								if (columnValue instanceof String) {
									columnValue = "'" + columnValue + "'";
								}
								query.append(columnValue).append(i.hasNext() ? "," : "");
							}
							query.append(")");
						}
					}

					List<String> rows = context.executeQuery(query.toString(), new ResultSetHandler<List<String>>() {

						public List<String> handle(ResultSet rs) throws SQLException {

							List<String> results = new ArrayList<String>();
							ResultSetMetaData md = rs.getMetaData();
							int numColumns = md.getColumnCount();

							while (rs.next()) {
								StringBuilder row = new StringBuilder("(");

								for (int i = 1; i <= numColumns; i++) {
									ColumnValue value = new ColumnValue(table, md.getColumnName(i), md.getColumnType(i), rs.getObject(i));
									row.append(i != 1 ? "," : "").append(value.getValueForExport());
								}
								row.append(")");
								results.add(row.toString());
							}
							return results;
						}
					});

					System.out.println("Number retrieved: " + rows.size());

					if (rows.size() > 0) {
						out.println("INSERT INTO " + table + " VALUES ");
						for (Iterator<String> i = rows.iterator(); i.hasNext();) {
							out.println("    " + i.next() + (i.hasNext() ? "," : ""));
						}
						out.println(";");
					}

					DbUtil.writeTableExportFooter(table, context);
				}
			}


			DbUtil.writeExportFooter(context);

			out.flush();
		}
		finally {
			IOUtils.closeQuietly(fos);
			DbUtil.closeConnection(connection);
		}
	}
}

