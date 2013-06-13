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
package org.openmrs.contrib.databaseexporter.filter;

import org.openmrs.contrib.databaseexporter.ExportContext;
import org.openmrs.contrib.databaseexporter.util.Util;

import java.util.List;

/**
 * Defines how to handle dependencies of filtered objects
 */
public class DependencyFilter {

	//***** PROPERTIES *****

	private String tableName;
	private List<String> idColumns;
	private List<DependencyFilterQuery> subQueries;
	private List<DependencyFilter> dependencies;

	//***** CONSTRUCTORS *****

	public DependencyFilter() {}

	//***** INSTANCE METHODS *****

	public void filter(ExportContext context) {
		if (getSubQueries() != null) {
			for (DependencyFilterQuery q : getSubQueries()) {
				if (context.getTableMetadata(q.getTableName()) != null) {
					for (String idCol : q.getIdColumns()) {
						StringBuilder sql = new StringBuilder();
						sql.append("select ").append(q.getTableName()).append(".").append(idCol);
						sql.append(" from ").append(q.getTableName());

						String joinTable = context.getTemporaryTableName(q.getJoinTable(), q.getParentColumn());
						String joinTableColumn = "id";
						if (joinTable == null) {
							joinTable = q.getJoinTable();
							joinTableColumn = q.getParentColumn();
						}

						if (q.getTableName().equals(joinTable) && idCol.equals(joinTableColumn)) {
							context.log("Skipping joining " + q.getTableName() + "." + idCol + " against itself");
						}
						else {
							sql.append(" inner join ").append(joinTable);
							sql.append(" on ").append(q.getTableName()).append(".").append(q.getChildColumn());
							sql.append(" = ").append(joinTable).append(".").append(joinTableColumn);
						}
						context.registerInTemporaryTable(q.getTableName(), idCol, context.executeIdQuery(sql.toString()));
					}
				}
				else {
					context.log("**** Skipping " + q.getTableName() + " from dependent tables, as this is not part of the configured export");
				}
			}
		}
		if (getDependencies() != null) {
			for (DependencyFilter df : getDependencies()) {
				DependencyFilter template = context.getConfiguration().getDependencyFilters().get(df.getTableName());
				DependencyFilter mergedFilter = new DependencyFilter();
				mergedFilter.setTableName(df.getTableName());
				mergedFilter.setIdColumns(Util.nvl(df.getIdColumns(), template.getIdColumns()));
				mergedFilter.setSubQueries(Util.nvl(df.getSubQueries(), template.getSubQueries()));
				mergedFilter.setDependencies(Util.nvl(df.getDependencies(), template.getDependencies()));
				mergedFilter.filter(context);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(tableName).append(" dependency filter");
		if (subQueries != null) {
			sb.append(" with ").append(subQueries.size()).append(" subqueries");
		}
		if (dependencies != null) {
			sb.append(" with ").append(dependencies.size()).append(" dependencies");
		}
		return sb.toString();
	}

	//***** PROPERTY ACCESS *****

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public List<String> getIdColumns() {
		return idColumns;
	}

	public void setIdColumns(List<String> idColumns) {
		this.idColumns = idColumns;
	}

	public List<DependencyFilterQuery> getSubQueries() {
		return subQueries;
	}

	public void setSubQueries(List<DependencyFilterQuery> subQueries) {
		this.subQueries = subQueries;
	}

	public List<DependencyFilter> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<DependencyFilter> dependencies) {
		this.dependencies = dependencies;
	}
}
