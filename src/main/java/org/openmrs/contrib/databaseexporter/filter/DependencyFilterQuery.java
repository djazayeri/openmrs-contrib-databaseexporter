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

import java.util.List;

/**
 * Contains information about how to construct a subquery
 */
public class DependencyFilterQuery {

	//***** PROPERTIES *****

	private String tableName;
	private List<String> idColumns;
	private String joinTable;
	private String childColumn;
	private String parentColumn;

	//***** CONSTRUCTORS *****

	public DependencyFilterQuery() {}

	public DependencyFilterQuery(String tableName, List<String> idColumns, String joinTable, String childColumn, String parentColumn) {
		this.tableName = tableName;
		this.idColumns = idColumns;
		this.joinTable = joinTable;
		this.childColumn = childColumn;
		this.parentColumn = parentColumn;
	}

	//***** METHODS *****

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

	public String getJoinTable() {
		return joinTable;
	}

	public void setJoinTable(String joinTable) {
		this.joinTable = joinTable;
	}

	public String getChildColumn() {
		return childColumn;
	}

	public void setChildColumn(String childColumn) {
		this.childColumn = childColumn;
	}

	public String getParentColumn() {
		return parentColumn;
	}

	public void setParentColumn(String parentColumn) {
		this.parentColumn = parentColumn;
	}
}
