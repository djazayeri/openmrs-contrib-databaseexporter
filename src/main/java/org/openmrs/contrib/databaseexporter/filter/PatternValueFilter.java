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

import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.openmrs.contrib.databaseexporter.ExportContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Row filter which returns those rows that match the passed values
 */
public class PatternValueFilter extends RowFilter {

	private String tableName;
	private String columnName;
	private List<String> patterns; // This is not a regular expression, but is in the format supported by the "like" function in mysql

	public PatternValueFilter() {}

	@Override
	public void applyFilters(ExportContext context) {
		StringBuilder q = new StringBuilder();
		q.append("select " + columnName + " from " + tableName + " where ");
		for (Iterator<String> i = patterns.iterator(); i.hasNext();) {
			String p = i.next();
			q.append(columnName + " like '" + p + "'").append(i.hasNext() ? " or " : "");
		}
		List<Object> properties = context.executeQuery(q.toString(), new ColumnListHandler<Object>());
		applyConstraints(tableName, columnName, properties, context);
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public List<String> getPatterns() {
		if (patterns == null) {
			patterns = new ArrayList<String>();
		}
		return patterns;
	}

	public void setPatterns(List<String> patterns) {
		this.patterns = patterns;
	}

	public void addPattern(String patterns) {
		getPatterns().add(patterns);
	}
}
