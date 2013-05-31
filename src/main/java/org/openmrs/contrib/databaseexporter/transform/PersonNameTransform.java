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
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.openmrs.contrib.databaseexporter.ExportContext;
import org.openmrs.contrib.databaseexporter.TableRow;
import org.openmrs.contrib.databaseexporter.util.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * De-identifies a Person Name row
 */
public class PersonNameTransform extends RowTransform {

	//**** INNER VARIABLES *****

	private static String CONFIG_PACKAGE = "org/openmrs/contrib/databaseexporter/";
	private Map<String, List<String>> replacements = null;
	private Map<Integer, String> genders = null;

	//***** CONFIGURABLE PROPERTIES *****

	private boolean reproducible = false; // If true, this will randomize in a reproducible fashion
	private String maleNamePath = CONFIG_PACKAGE + "maleNames.config";
	private String femaleNamePath = CONFIG_PACKAGE + "femaleNames.config";
	private String familyNamePath = CONFIG_PACKAGE + "familyNames.config";

	//***** CONSTRUCTORS *****

	public PersonNameTransform() {}

	//***** INSTANCE METHODS *****

	public boolean applyTransform(TableRow row, ExportContext context) {
		if (row.getTableName().equals("person_name")) {

			String givenNameStart = (String)row.getRawValue("given_name");
			String middleNameStart = (String)row.getRawValue("middle_name");
			List<String> givenNameList = getReplacements("given", true, row, context);

			String familyNameStart = (String)row.getRawValue("family_name");
			List<String> familyNameList = getReplacements("family", false, row, context);

			row.setRawValue("prefix", null);
			row.setRawValue("given_name", getReplacement(givenNameStart, givenNameList));
			row.setRawValue("middle_name", getReplacement(middleNameStart, givenNameList));
			row.setRawValue("family_name_prefix", null);
			row.setRawValue("family_name", getReplacement(familyNameStart, familyNameList));
			row.setRawValue("family_name2", null);
			row.setRawValue("family_name_suffix", null);
			row.setRawValue("degree", null);
		}
		return true;
	}

	/**
	 * @return a suitable replacement value given the passed startingValue and list of possible replacements.
	 * If startingValue is null, then null is returned back.  Otherwise
	 * If this transform is not configured to be reproducible, then the startingValue is ignored and a random replacement
	 * value from the list is returned.  If the transform is reproducible, then a replacement value based off of the
	 * starting value, is returned, but not in a way that will allow for the startingValue to be re-derived
	 */
	public String getReplacement(String startingValue, List<String> replacementList) {
		if (Util.isEmpty(startingValue)) {
			return startingValue;
		}
		if (!isReproducible()) {
			int randomNum = (int)(Math.random() * replacementList.size());
			return replacementList.get(randomNum);
		}
		int remainder = (Math.abs(startingValue.hashCode()) % replacementList.size());
		return replacementList.get(remainder);
	}

	public List<String> getReplacements(String which, boolean genderSpecific, TableRow row, ExportContext context) {
		if (replacements == null) {
			replacements = new HashMap<String, List<String>>();
			replacements.put("givenNamesM", Util.getListFromResource(maleNamePath));
			replacements.put("givenNamesF", Util.getListFromResource(femaleNamePath));
			replacements.put("familyNames", Util.getListFromResource(familyNamePath));
		}
		if (genders == null) {
			genders = getGenderMap(context);
		}
		String key = which + "Names" + (genderSpecific ? Util.nvlStr(genders.get(row.getRawValue("person_id")), "F") : "");
		return replacements.get(key);
	}

	public Map<Integer, String> getGenderMap(ExportContext context) {
		String q = "select person_id, gender from person";
		Map<Integer, String> m = context.executeQuery(q, new ResultSetHandler<Map<Integer, String>>() {
			public Map<Integer, String> handle(ResultSet rs) throws SQLException {
				Map<Integer, String> ret = new HashMap<Integer, String>();
				while (rs.next()) {
					ret.put(rs.getInt(1), rs.getString(2));
				}
				return ret;
			}
		});
		return m;
	}

	//***** PROPERTY ACCESS *****

	public boolean isReproducible() {
		return reproducible;
	}

	public void setReproducible(boolean reproducible) {
		this.reproducible = reproducible;
	}

	public String getMaleNamePath() {
		return maleNamePath;
	}

	public void setMaleNamePath(String maleNamePath) {
		this.maleNamePath = maleNamePath;
	}

	public String getFemaleNamePath() {
		return femaleNamePath;
	}

	public void setFemaleNamePath(String femaleNamePath) {
		this.femaleNamePath = femaleNamePath;
	}

	public String getFamilyNamePath() {
		return familyNamePath;
	}

	public void setFamilyNamePath(String familyNamePath) {
		this.familyNamePath = familyNamePath;
	}
}
