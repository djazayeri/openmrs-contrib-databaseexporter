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
import org.openmrs.contrib.databaseexporter.TableConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Row filter which returns those rows that match the passed values
 */
public class ColumnValueFilter extends RowFilter {

	private String tableName;
	private String columnName;
	private List<Object> values;

	public ColumnValueFilter() {}

	@Override
	public void filter(ExportContext context) {
		TableConfig config = context.getTableData().get(tableName);
		config.addColumnConstraints(columnName, values);
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

	public List<Object> getValues() {
		if (values == null) {
			values = new ArrayList<Object>();
		}
		return values;
	}

	public void setValues(List<Object> values) {
		this.values = values;
	}

	public void addValue(Object value) {
		getValues().add(value);
	}
}
