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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface for a transform that can manipulate a row in one or more tables
 */
public class SimpleReplacementTransform extends RowTransform {

	private List<String> tableAndColumnList;
	private Map<String, String> constraints;
	private Object replacement;

	@Override
	public boolean canTransform(String tableName, ExportContext context) {
		for (String s : tableAndColumnList) {
			if (s.startsWith(tableName + ".")) {
				return true;
			}
		}
		return false;
	}

	public boolean applyTransform(TableRow row, ExportContext context) {
		for (String column : row.getColumns()) {
			if (tableAndColumnList.contains(row.getTableName() + "." + column)) {
				if (row.getRawValue(column) != null) {
					boolean passes = true;
					for (String constraintColumnName : getConstraints().keySet()) {
						passes = passes && Util.areEqualStr(row.getRawValue(constraintColumnName), getConstraints().get(constraintColumnName));
					}
					if (passes) {
						row.setRawValue(column, Util.evaluateExpression(replacement, row));
					}
				}
			}
		}
		return true;
	}

	public List<String> getTableAndColumnList() {
		if (tableAndColumnList == null) {
			tableAndColumnList = new ArrayList<String>();
		}
		return tableAndColumnList;
	}

	public void setTableAndColumnList(List<String> tableAndColumnList) {
		this.tableAndColumnList = tableAndColumnList;
	}

	public Map<String, String> getConstraints() {
		if (constraints == null) {
			constraints = new HashMap<String, String>();
		}
		return constraints;
	}

	public void setConstraints(Map<String, String> constraints) {
		this.constraints = constraints;
	}

	public Object getReplacement() {
		return replacement;
	}

	public void setReplacement(Object replacement) {
		this.replacement = replacement;
	}
}
