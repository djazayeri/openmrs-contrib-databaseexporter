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
import org.openmrs.contrib.databaseexporter.generator.IdentifierGenerator;
import org.openmrs.contrib.databaseexporter.util.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Transforms patient identifier type and patient identifier
 * so that they are fully de-identified
 */
public class IdentifierTransform extends RowTransform {

	private IdentifierGenerator defaultGenerator;
	private Map<String, IdentifierGenerator> replacementGenerators;

	//***** CONSTRUCTORS *****

	public IdentifierTransform() {}

	//***** INSTANCE METHODS *****

	@Override
	public boolean canTransform(String tableName, ExportContext context) {
		return tableName.equals("patient_identifier");
	}

	@Override
	public boolean transformRow(TableRow row, ExportContext context) {
		if (row.getTableName().equals("patient_identifier")) {
			IdentifierGenerator generator = getReplacementGenerator(row, context);
			if (generator != null && Util.notEmpty(row.getRawValue("identifier"))) {
				row.setRawValue("identifier", generator.generateIdentifier(row, context));
			}
		}
		return true;
	}

	//***** INTERNAL CACHES *****

	private Map<Integer, String> identifierTypeNameCache;
	public IdentifierGenerator getReplacementGenerator(TableRow row, ExportContext context) {
		IdentifierGenerator ret = null;
		if (identifierTypeNameCache == null) {
			String q = "select patient_identifier_type_id, name from patient_identifier_type";
			identifierTypeNameCache = context.executeQuery(q, new ResultSetHandler<Map<Integer, String>>() {
				public Map<Integer, String> handle(ResultSet rs) throws SQLException {
					Map<Integer, String> result = new HashMap<Integer, String>();
					while (rs.next()) {
						result.put(rs.getInt(1), rs.getString(2));
					}
					return result;
				}
			});
			for (Iterator<Integer> i = identifierTypeNameCache.keySet().iterator(); i.hasNext();) {
				Integer typeId = i.next();
				if (!getReplacementGenerators().containsKey(identifierTypeNameCache.get(typeId))) {
					i.remove();
				}
			}
		}
		Object attTypeId = row.getRawValue("identifier_type");
		if (attTypeId != null) {
			String typeName = identifierTypeNameCache.get(attTypeId);
			ret = getReplacementGenerators().get(typeName);
		}
		if (ret != null) {
			return ret;
		}
		return getDefaultGenerator();
	}

	//***** PROPERTY ACCESS *****


	public IdentifierGenerator getDefaultGenerator() {
		return defaultGenerator;
	}

	public void setDefaultGenerator(IdentifierGenerator defaultGenerator) {
		this.defaultGenerator = defaultGenerator;
	}

	public Map<String, IdentifierGenerator> getReplacementGenerators() {
		if (replacementGenerators == null) {
			replacementGenerators = new HashMap<String, IdentifierGenerator>();
		}
		return replacementGenerators;
	}

	public void setReplacementGenerators(Map<String, IdentifierGenerator> replacementGenerators) {
		this.replacementGenerators = replacementGenerators;
	}
}
