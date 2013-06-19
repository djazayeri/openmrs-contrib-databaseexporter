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

import org.apache.commons.dbutils.ResultSetHandler;
import org.openmrs.contrib.databaseexporter.ExportContext;
import org.openmrs.contrib.databaseexporter.TableRow;
import org.openmrs.contrib.databaseexporter.util.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * De-identifies the user table
 */
public class PersonAttributeTransform extends RowTransform {

	//***** PROPERTIES *****

	private String type;
	private Map<String, String> replacements;

	//***** CONSTRUCTORS *****

	public PersonAttributeTransform() {}

	//***** INSTANCE METHODS *****

	@Override
	public boolean canTransform(String tableName, ExportContext context) {
		return tableName.equals("person_attribute");
	}

	@Override
	public boolean applyTransform(TableRow row, ExportContext context) {
		if (row.getTableName().equals("person_attribute")) {
			if (matchesAttributeType(type, row, context)) {
				Object rawValue = row.getRawValue("value");
				if (Util.notEmpty(rawValue)) {
					String value = rawValue.toString();
					for (String pattern : getReplacements().keySet()) {
						if (Util.matchesPattern(value, pattern)) {
							Object replacement = Util.evaluateExpression(getReplacements().get(pattern), row);
							if ("[null]".equals(replacement)) {
								replacement = null;
							}
							row.setRawValue("value", replacement);
						}
					}
				}
			}
		}
		return true;
	}

	//***** INTERNAL CACHES *****

	private Map<String, Integer> attributeTypeCache;
	public boolean matchesAttributeType(String type, TableRow row, ExportContext context) {
		if (attributeTypeCache == null) {
			String q = "select person_attribute_type_id, name from person_attribute_type";
			attributeTypeCache = context.executeQuery(q, new ResultSetHandler<Map<String, Integer>>() {
				public Map<String, Integer> handle(ResultSet rs) throws SQLException {
					Map<String, Integer> result = new HashMap<String, Integer>();
					while (rs.next()) {
						result.put(rs.getString(2), rs.getInt(1));
					}
					return result;
				}
			});
		}
		Integer attributeTypeId = Integer.valueOf(row.getRawValue("person_attribute_type_id").toString());
		return attributeTypeCache.get(type).equals(attributeTypeId);
	}

	//***** PROPERTY ACCESS *****

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
