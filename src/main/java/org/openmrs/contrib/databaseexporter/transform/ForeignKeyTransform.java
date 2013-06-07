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

import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.openmrs.contrib.databaseexporter.ExportContext;
import org.openmrs.contrib.databaseexporter.QueryBuilder;
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
	private boolean onlyReplaceIfInvalid = true; // Whether to replace a row with a new value only if the current value no longer exists
	private List<String> foreignKeysToRemove; // Any table.column combinations that should be removed rather than replaced, if invalid

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
				List<Object> replacements = getForeignKeyData(context);
				if (replacements.isEmpty() || getForeignKeysToRemove().contains(tabCol)) {
					return false;
				}
				else {
					boolean isStillValid = replacements.contains(row.getRawValue(columnName));
					if (!isOnlyReplaceIfInvalid() || !isStillValid) {
						row.setRawValue(columnName, Util.getRandomElementFromList(replacements));
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

	List<Object> foreignKeyData;
	public List<Object> getForeignKeyData(ExportContext context) {
		if (foreignKeyData == null) {
			QueryBuilder qb = new QueryBuilder();
			String query = qb.buildQuery(getReferencedTable(), context);
			query = query.replace("*", getReferencedColumn());
			foreignKeyData = context.executeQuery(query, new ColumnListHandler<Object>());
		}
		return foreignKeyData;
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

	public List<String> getForeignKeysToRemove() {
		if (foreignKeysToRemove == null) {
			foreignKeysToRemove = new ArrayList<String>();
		}
		return foreignKeysToRemove;
	}

	public void setForeignKeysToRemove(List<String> foreignKeysToRemove) {
		this.foreignKeysToRemove = foreignKeysToRemove;
	}

	public boolean isOnlyReplaceIfInvalid() {
		return onlyReplaceIfInvalid;
	}

	public void setOnlyReplaceIfInvalid(boolean onlyReplaceIfInvalid) {
		this.onlyReplaceIfInvalid = onlyReplaceIfInvalid;
	}
}
