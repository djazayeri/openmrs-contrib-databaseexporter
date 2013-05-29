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

/**
 * Helper class to encapsulate a query of a single column in a table
 */
public class FilterQuery {

	private String tableName;
	private String columnToQuery;
	private String constraintClause;

	public FilterQuery() {}

	public FilterQuery(String tableName, String columnToQuery, String constraintClause) {
		this.tableName = tableName;
		this.columnToQuery = columnToQuery;
		this.constraintClause = constraintClause;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumnToQuery() {
		return columnToQuery;
	}

	public void setColumnToQuery(String columnToQuery) {
		this.columnToQuery = columnToQuery;
	}

	public String getConstraintClause() {
		return constraintClause;
	}

	public void setConstraintClause(String constraintClause) {
		this.constraintClause = constraintClause;
	}
}
