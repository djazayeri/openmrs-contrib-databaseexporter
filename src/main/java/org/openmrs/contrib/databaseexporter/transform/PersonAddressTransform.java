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

import java.util.List;
import java.util.Map;

/**
 * This replaces all addresses in the person_address table based on a custom input file.
 */
public class PersonAddressTransform extends StructuredAddressTransform {

	//***** CONSTRUCTORS *****

	public PersonAddressTransform() {}

	//***** INSTANCE METHODS *****

	@Override
	public boolean canTransform(String tableName, ExportContext context) {
		return tableName.equals("person_address");
	}

	public boolean applyTransform(TableRow row, ExportContext context) {
		if (row.getTableName().equals("person_address")) {
			Map<String, String> newAddress = getRandomReplacementAddress(row, context);
			for (String column : addressColumns) {
				if (row.getRawValue(column) != null) {
					row.setRawValue(column, newAddress.get(column));
				}
			}
		}
		return true;
	}
}
