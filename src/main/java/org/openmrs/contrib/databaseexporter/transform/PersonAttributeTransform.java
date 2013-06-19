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
import org.openmrs.contrib.databaseexporter.util.ListMap;
import org.openmrs.contrib.databaseexporter.util.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Applies String replacement transforms on the person attribute table
 */
public class PersonAttributeTransform extends RowTransform {

	//***** PROPERTIES *****

	private ListMap<String, String> replacements;

	//***** CONSTRUCTORS *****

	public PersonAttributeTransform() {}

	//***** INSTANCE METHODS *****

	@Override
	public boolean canTransform(String tableName, ExportContext context) {
		return tableName.equals("person_attribute");
	}

	@Override
	public boolean transformRow(TableRow row, ExportContext context) {
		if (row.getTableName().equals("person_attribute")) {
			Object rawValue = row.getRawValue("value");
			if (Util.notEmpty(rawValue)) {
				List<String> replacementsForRow = getReplacementsForRow(row, context);
				if (replacementsForRow != null) {
					String s = Util.getRandomElementFromList(replacementsForRow);
					row.setRawValue("value", Util.evaluateExpression(s, row));
				}
			}
		}
		return true;
	}

	//***** INTERNAL CACHES *****

	private Map<Integer, String> attributeNameCache;
	public List<String> getReplacementsForRow(TableRow row, ExportContext context) {
		if (attributeNameCache == null) {
			String q = "select person_attribute_type_id, name from person_attribute_type";
			attributeNameCache = context.executeQuery(q, new ResultSetHandler<Map<Integer, String>>() {
				public Map<Integer, String> handle(ResultSet rs) throws SQLException {
					Map<Integer, String> result = new HashMap<Integer, String>();
					while (rs.next()) {
						result.put(rs.getInt(1), rs.getString(2));
					}
					return result;
				}
			});
			for (Iterator<Integer> i = attributeNameCache.keySet().iterator(); i.hasNext();) {
				Integer typeId = i.next();
				if (!getReplacements().containsKey(attributeNameCache.get(typeId))) {
					i.remove();
				}
			}
		}
		Object attTypeId = row.getRawValue("person_attribute_type_id");
		if (attTypeId != null) {
			String typeName = attributeNameCache.get(attTypeId);
			return getReplacements().get(typeName);
		}
		return null;
	}

	//***** PROPERTY ACCESS *****

	public ListMap<String, String> getReplacements() {
		if (replacements == null) {
			replacements = new ListMap<String, String>();
		}
		return replacements;
	}

	public void setReplacements(ListMap<String, String> replacements) {
		this.replacements = replacements;
	}
}
