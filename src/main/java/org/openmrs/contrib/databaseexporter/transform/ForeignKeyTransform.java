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

import java.util.Arrays;
import java.util.List;

/**
 * Transform, usually instantiated internally, rather than via end-user configuration,
 * which ensures that any columns that reference a row in a table that has been filtered
 * via rowFilters are re-associated with other, valid data, if possible
 */
public class ForeignKeyTransform extends RowTransform {

	public static final List<String> KEYS_NOT_TO_TRANSFORM = Arrays.asList(
		"notification_alert_recipient.user_id"
	);

	//***** PROPERTIES *****

	private String referencedTable; // The table to check for missing references
	private String referencedColumn; // The column to check for missing references

	//***** CONSTRUCTORS *****

	public ForeignKeyTransform(String referencedTable, String referencedColumn) {
		this.referencedTable = referencedTable;
		this.referencedColumn = referencedColumn;
	}

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

	public boolean transformRow(TableRow row, ExportContext context) {
		List<String> fks = getForeignKeys(context);
		for (String columnName : row.getColumns()) {
			String tabCol = row.getTableName() + "." + columnName;
			if (fks.contains(tabCol)) {
				List<Integer> validValues =  getValidValues(context);
				if (validValues != null && !validValues.isEmpty()) {
					if (!validValues.contains(row.getRawValue(columnName))) {
						if (KEYS_NOT_TO_TRANSFORM.contains(tabCol)) {
							return false;
						}
						else {
							Object replacementValue = Util.getRandomElementFromList(validValues);
							row.setRawValue(columnName, replacementValue);
						}
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

	private List<Integer> validValues;
	private List<Integer> getValidValues(ExportContext context) {
		if (validValues == null) {
			validValues = context.getTemporaryTableValues(getReferencedTable(), getReferencedColumn());
		}
		return validValues;
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
}
