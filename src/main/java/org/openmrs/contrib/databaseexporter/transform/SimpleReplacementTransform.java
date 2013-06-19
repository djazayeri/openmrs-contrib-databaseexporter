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
package org.openmrs.contrib.databaseexporter.transform;

import org.openmrs.contrib.databaseexporter.ExportContext;
import org.openmrs.contrib.databaseexporter.TableRow;
import org.openmrs.contrib.databaseexporter.util.Util;

import java.util.HashSet;
import java.util.Set;

/**
 * De-identifies the user table
 */
public class SimpleReplacementTransform extends RowTransform {

	//***** PROPERTIES *****

	private String tableName;
	private Set<String> columnNames;
	private String replacement;

	//***** CONSTRUCTORS *****

	public SimpleReplacementTransform() {}

	//***** INSTANCE METHODS *****

	@Override
	public boolean canTransform(String tableName, ExportContext context) {
		return tableName.equals(getTableName());
	}

	@Override
	public boolean transformRow(TableRow row, ExportContext context) {
		if (row.getTableName().equals(getTableName())) {
			for (String columnName : getColumnNames()) {
				Object currentValue = row.getRawValue(columnName);
				if (Util.notEmpty(currentValue)) {
					row.setRawValue(columnName, Util.evaluateExpression(getReplacement(), row));
				}
			}
		}
		return true;
	}

	//***** PROPERTY ACCESS *****

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Set<String> getColumnNames() {
		if (columnNames == null) {
			columnNames = new HashSet<String>();
		}
		return columnNames;
	}

	public void setColumnNames(Set<String> columnNames) {
		this.columnNames = columnNames;
	}

	public String getReplacement() {
		return replacement;
	}

	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}
}
