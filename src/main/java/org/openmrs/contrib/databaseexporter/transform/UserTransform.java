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

import java.util.ArrayList;
import java.util.List;

/**
 * De-identifies the user table
 */
public class UserTransform extends RowTransform {

	//***** PROPERTIES *****

	private List<Integer> limitToUsers;
	private String systemIdReplacement;
	private String usernameReplacement;
	private String passwordReplacement;

	//***** CONSTRUCTORS *****

	public UserTransform() {}

	//***** INSTANCE METHODS *****

	@Override
	public boolean canTransform(String tableName, ExportContext context) {
		return tableName.equals("users");
	}

	public boolean applyTransform(TableRow row, ExportContext context) {

		// If the row will be kept, de-identify user data if specified
		if (row.getTableName().equals("users")) {
			Object systemId = row.getRawValue("system_id");
			if (systemIdReplacement != null && !systemId.equals("admin") && !systemId.equals("daemon")) {
				row.setRawValue("system_id", Util.evaluateExpression(systemIdReplacement, row));
			}
			Object username = row.getRawValue("username");
			if (usernameReplacement != null && !username.equals("admin") && !username.equals("daemon")) {
				row.setRawValue("username", Util.evaluateExpression(usernameReplacement, row));
			}
			if (passwordReplacement != null) {
				String pwAndSalt = Util.evaluateExpression(passwordReplacement, row).toString() + row.getRawValue("salt");
				row.setRawValue("password", Util.encodeString(pwAndSalt));
			}
			row.setRawValue("secret_question", null);
			row.setRawValue("secret_answer", null);
		}

		return true;
	}

	//***** PROPERTY ACCESS *****

	public String getSystemIdReplacement() {
		return systemIdReplacement;
	}

	public void setSystemIdReplacement(String systemIdReplacement) {
		this.systemIdReplacement = systemIdReplacement;
	}

	public String getUsernameReplacement() {
		return usernameReplacement;
	}

	public void setUsernameReplacement(String usernameReplacement) {
		this.usernameReplacement = usernameReplacement;
	}

	public String getPasswordReplacement() {
		return passwordReplacement;
	}

	public void setPasswordReplacement(String passwordReplacement) {
		this.passwordReplacement = passwordReplacement;
	}

	public List<Integer> getLimitToUsers() {
		if (limitToUsers == null) {
			limitToUsers = new ArrayList<Integer>();
		}
		return limitToUsers;
	}

	public void setLimitToUsers(List<Integer> limitToUsers) {
		this.limitToUsers = limitToUsers;
	}
}
