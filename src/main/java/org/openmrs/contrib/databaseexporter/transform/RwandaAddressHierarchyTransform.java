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

import org.openmrs.contrib.databaseexporter.ColumnValue;
import org.openmrs.contrib.databaseexporter.ExportContext;
import org.openmrs.contrib.databaseexporter.TableRow;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This replaces the address_hierarchy table with contents based on the configured addresses file
 */
public class RwandaAddressHierarchyTransform extends StructuredAddressTransform implements TableTransform {

	//***** CONSTRUCTORS *****

	public RwandaAddressHierarchyTransform() {}

	//***** INSTANCE METHODS *****

	// Remove all existing rows from the address_hierarchy_entry table
	public boolean applyTransform(TableRow row, ExportContext context) {
		if (row.getTableName().equals("address_hierarchy")) {
			return false;
		}
		return true;
	}

	@Override
	public List<TableRow> getNewRows(String tableName, ExportContext context) {
		List<TableRow> rows = new ArrayList<TableRow>();

		List<Map<String, String>> replacements = getReplacements();
		for (int i=0; i<replacements.size(); i++) {
			Map<String, String> replacement = replacements.get(i);
			Integer num = i+1;
			if (num == 1) {
				TableRow row = new TableRow("address_hierarchy");
				row.addColumnValue("address_hierarchy_id", new ColumnValue("address_hierarchy", "address_hierarchy_id", Types.INTEGER, num));
				row.addColumnValue("name", new ColumnValue("address_hierarchy", "name", Types.VARCHAR, "Foo"));
				row.addColumnValue("type_id", new ColumnValue("address_hierarchy", "type_id", Types.INTEGER, 1));
				row.addColumnValue("parent_id", new ColumnValue("address_hierarchy", "parent_id", Types.INTEGER, null));
				row.addColumnValue("user_generated_id", new ColumnValue("address_hierarchy", "user_generated_id", Types.VARCHAR, Integer.toString(num)));
				rows.add(row);
			}
		}

		return rows;

	}
}
