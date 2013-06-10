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
import java.util.List;

/**
 * Transform, usually instantiated internally, rather than via end-user configuration,
 * which ensures that any columns that reference a row in a table that has been filtered
 * via rowFilters are re-associated with other, valid data, if possible
 */
public class ForeignKeyTransform extends RowTransform {

	//***** PROPERTIES *****

	private String referencedTable; // The table to check for missing references
	private String referencedColumn; // The column to check for missing references
	private List<Object> validValues; // The values to use as valid replacements
	private boolean onlyReplaceIfInvalid = true; // Whether to replace a row with a new value only if the current value no longer exists

	//***** INSTANCE METHODS *****

	@Override
	public boolean canTransform(String tableName, ExportContext context) {
		List<String> fks = getForeignKeys(context);
		for (String fk : fks) {
			if (fk.startsWith(tableName + ".")) {
				return true;
			}
		}
		return false;
	}

	public boolean applyTransform(TableRow row, ExportContext context) {
		List<String> fks = getForeignKeys(context);
		for (String columnName : row.getColumns()) {
			String tabCol = row.getTableName() + "." + columnName;
			if (fks.contains(tabCol)) {
				if (getValidValues().isEmpty()) {
					return false;
				}
				else {
					boolean isStillValid = getValidValues().contains(row.getRawValue(columnName));
					if (!isOnlyReplaceIfInvalid() || !isStillValid) {
						row.setRawValue(columnName, Util.getRandomElementFromList(getValidValues()));
					}
				}
			}
		}
		return true;
	}

	//***** INTERNAL CACHES *****

	private List<String> foreignKeys;
	private List<String> getForeignKeys(ExportContext context) {
		if (foreignKeys == null) {
			foreignKeys = context.getTableMetadata(getReferencedTable()).getForeignKeys(getReferencedColumn());
		}
		return foreignKeys;
	}

	//***** PROPERTY ACCESS *****

	public String getReferencedColumn() {
		return referencedColumn;
	}

	public void setReferencedColumn(String referencedColumn) {
		this.referencedColumn = referencedColumn;
	}

	public String getReferencedTable() {
		return referencedTable;
	}

	public void setReferencedTable(String referencedTable) {
		this.referencedTable = referencedTable;
	}

	public List<Object> getValidValues() {
		if (validValues == null) {
			validValues = new ArrayList<Object>();
		}
		return validValues;
	}

	public void addValidValue(Object value) {
		getValidValues().add(value);
	}

	public void setValidValues(List<Object> validValues) {
		this.validValues = validValues;
	}

	public boolean isOnlyReplaceIfInvalid() {
		return onlyReplaceIfInvalid;
	}

	public void setOnlyReplaceIfInvalid(boolean onlyReplaceIfInvalid) {
		this.onlyReplaceIfInvalid = onlyReplaceIfInvalid;
	}
}
