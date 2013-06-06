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

import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.openmrs.contrib.databaseexporter.util.DbUtil;
import org.openmrs.contrib.databaseexporter.util.ListMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class QueryBuilder {

	public static final String TEMPORARY_TABLE_PREFIX = "temp_databaseexporter_";

	private ListMap<String, String> temporaryTableSet = new ListMap<String, String>();

	public QueryBuilder() { }

	public void prepareTemporaryTablesForExport(ExportContext context) {
		for (String tableName : context.getTableData().keySet()) {
			TableConfig config = context.getTableData().get(tableName);

			for (String columnName : config.getAllConstrainedColumns()) {

				if (columnName.equals("id") || columnName.endsWith("_id")) {
					List<Object> includedValues = config.getIncludeConstraints().get(columnName);
					if (includedValues == null || includedValues.isEmpty()) {
						includedValues = context.executeQuery("select distinct " + columnName + " from " + tableName, new ColumnListHandler<Object>());
					}
					List<Object> excludedValues = config.getExcludeConstraints().get(columnName);
					if (excludedValues != null && !excludedValues.isEmpty()) {
						includedValues.removeAll(config.getExcludeConstraints().get(columnName));
					}
					Set<Object> valuesToInsert = new HashSet<Object>(includedValues);

					String tempTableName = TEMPORARY_TABLE_PREFIX + tableName;

					context.log("Preparing temporary table " + tempTableName +" by adding " + valuesToInsert.size() + " values to " + columnName);

					if (columnName.equals("id") || columnName.endsWith("_id")) {
						DbUtil.executeUpdate("create temporary table " + tempTableName + " (" + columnName + " integer not null primary key)", context);

						StringBuilder insert = new StringBuilder("insert into " + tempTableName + " (" + columnName + ") values ");
						for (Iterator<Object> i = valuesToInsert.iterator(); i.hasNext();) {
							Object o = i.next();
							insert.append("(").append(o).append(")");
							if (i.hasNext()) {
								insert.append(",");
							}
						}
						DbUtil.executeUpdate(insert.toString(), context);

						temporaryTableSet.putInList(tempTableName, columnName);
					}
				}
			}
		}
	}

	public String buildQuery(String tableName, ExportContext context) {

		TableConfig tableConfig = context.getTableData().get(tableName);

		List<String> joins = new ArrayList<String>();
		List<String> constraints = new ArrayList<String>();

		String tempTableName = TEMPORARY_TABLE_PREFIX + tableName;

		for (String column : tableConfig.getAllConstrainedColumns()) {
			if (temporaryTableSet.containsValueInList(tempTableName, column)) {
				joins.add("inner join " + tempTableName + " on " + tableName + "." + column + " = " + tempTableName + "." + column);
			}
			else {
				Set<Object> includes = new HashSet<Object>();
				if (tableConfig.getIncludeConstraints().get(column) != null) {
					includes.addAll(tableConfig.getIncludeConstraints().get(column));
				}
				Set<Object> excludes = new HashSet<Object>();
				if (tableConfig.getExcludeConstraints().get(column) != null) {
					excludes.addAll(tableConfig.getExcludeConstraints().get(column));
				}
				for (Iterator<Object> i = includes.iterator(); i.hasNext();) {
					Object o = i.next();
					if (excludes.contains(o)) {
						i.remove();
						excludes.remove(o);
					}
				}
				if (!includes.isEmpty()) {
					constraints.add(DbUtil.addInClauseToQuery(new StringBuilder(tableName + "." + column + " in "), includes).toString());
				}
				if (!excludes.isEmpty()) {
					constraints.add(DbUtil.addInClauseToQuery(new StringBuilder(tableName + "." + column + " not in "), excludes).toString());
				}
			}
		}

		StringBuilder query = new StringBuilder("select * from " + tableName);
		for (String join : joins) {
			query.append(" ").append(join);
		}
		for (String constraint : constraints) {
			DbUtil.addConstraintToQuery(query, constraint);
		}

		return query.toString();
	}

	public void cleanupTemporaryTables(ExportContext context) {
		for (String tableName : temporaryTableSet.keySet()) {
			if (tableName.startsWith(TEMPORARY_TABLE_PREFIX)) {
				DbUtil.executeUpdate("drop table " + tableName, context);
			}
		}
		temporaryTableSet.clear();
	}
}

