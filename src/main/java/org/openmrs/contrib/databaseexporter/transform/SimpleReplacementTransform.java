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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * De-identifies the user table
 */
public class SimpleReplacementTransform extends RowTransform {

	//***** PROPERTIES *****

	// This is in the format "tableName.columnName" -> "replacementExpression"
	private Map<String, String> replacements;

	//***** CONSTRUCTORS *****

	public SimpleReplacementTransform() {}

	//***** INSTANCE METHODS *****

	@Override
	public boolean canTransform(String tableName, ExportContext context) {
		return getIncludedTables().contains(tableName);
	}

	@Override
	public boolean transformRow(TableRow row, ExportContext context) {
		if (getIncludedTables().contains(row.getTableName())) {
			for (String s : getReplacements().keySet()) {
				String[] split = s.split("\\.");
				if (split[0].equals(row.getTableName())) {
					Object currentValue = row.getRawValue(split[1]);
					if (Util.notEmpty(currentValue)) {
						row.setRawValue(split[1], Util.evaluateExpression(getReplacements().get(s), row));
					}
				}
			}
		}
		return true;
	}

	//***** INTERNAL CACHES *****
	private Set<String> includedTables;
	public Set<String> getIncludedTables() {
		if (includedTables == null) {
			includedTables = new HashSet<String>();
			for (String tableAndColumn : getReplacements().keySet()) {
				includedTables.add(tableAndColumn.split("\\.")[0]);
			}
		}
		return includedTables;
	}

	//***** PROPERTY ACCESS *****

	public Map<String, String> getReplacements() {
		if (replacements == null) {
			replacements = new HashMap<String, String>();
		}
		return replacements;
	}

	public void setReplacements(Map<String, String> replacements) {
		this.replacements = replacements;
	}
}
