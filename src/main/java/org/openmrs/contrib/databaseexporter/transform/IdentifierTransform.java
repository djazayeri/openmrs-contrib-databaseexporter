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
import java.util.List;
import java.util.Map;

/**
 * Transforms patient identifier type and patient identifier
 * so that they are fully de-identified
 */
public class IdentifierTransform extends RowTransform {

	private String identifierReplacement = "${patient_identifier_id}";

	//***** CONSTRUCTORS *****

	public IdentifierTransform() {}

	//***** INSTANCE METHODS *****

	/**
	 * TODO: Improve this
	 * This probably isn't a sufficient solution, as we will want to be able to demonstrate
	 * and test the identifier validators and use them with idgen.  For now, we will leave it
	 * like this because it accomplishes the first most important task, which is de-identification
	 */
	public boolean applyTransform(TableRow row, ExportContext context) {
		if (row.getTableName().equals("patient_identifier_type")) {
			row.setRawValue("check_digit", 0);
			row.setRawValue("validator", null);
			row.setRawValue("format", null);
			row.setRawValue("format_description", null);
		}
		if (row.getTableName().equals("patient_identifier")) {
			row.setRawValue("identifier", Util.evaluateExpression(getIdentifierReplacement(), row));
		}
		return true;
	}

	//***** PROPERTY ACCESS *****

	public String getIdentifierReplacement() {
		return identifierReplacement;
	}

	public void setIdentifierReplacement(String identifierReplacement) {
		this.identifierReplacement = identifierReplacement;
	}
}
