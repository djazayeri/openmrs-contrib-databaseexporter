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
import java.util.Map;

/**
 * Interface for a transform that can replace global property values
 */
public class GlobalPropertyTransform extends RowTransform {

	private Map<String, String> replacements;

	@Override
	public boolean canTransform(String tableName, ExportContext context) {
		return tableName.equals("global_property");
	}

	public boolean transformRow(TableRow row, ExportContext context) {
		if (row.getTableName().equals("global_property")) {
			for (String propertyName : replacements.keySet()) {
				if (propertyName.equals(row.getRawValue("property"))) {
					String replacement = replacements.get(propertyName);
					if (replacement == null) {
						return false;
					}
					row.setRawValue("property_value", Util.evaluateExpression(replacement, row));
				}
			}
		}
		return true;
	}

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
