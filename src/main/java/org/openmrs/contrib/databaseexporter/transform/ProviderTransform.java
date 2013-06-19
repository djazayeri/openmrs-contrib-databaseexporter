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

/**
 * De-identifies the user table
 */
public class ProviderTransform extends PersonNameTransform {

	//***** PROPERTIES *****

	private boolean scrambleName = false;
	// TODO: private IdentifierGenerator = null;

	//***** CONSTRUCTORS *****

	public ProviderTransform() {}

	//***** INSTANCE METHODS *****

	@Override
	public boolean canTransform(String tableName, ExportContext context) {
		return tableName.equals("provider");
	}

	public boolean transformRow(TableRow row, ExportContext context) {

		// If the row will be kept, de-identify user data if specified
		if (row.getTableName().equals("provider")) {
			if (Util.notEmpty(row.getRawValue("name"))) {
				List<String> givenNameList = getReplacements("given", true, row, context);
				List<String> familyNameList = getReplacements("family", false, row, context);
				String s = Util.getRandomElementFromList(givenNameList) + " " + Util.getRandomElementFromList(familyNameList);
				row.setRawValue("name", s);
			}
		}

		return true;
	}

	//***** PROPERTY ACCESS *****

	public boolean isScrambleName() {
		return scrambleName;
	}

	public void setScrambleName(boolean scrambleName) {
		this.scrambleName = scrambleName;
	}
}
