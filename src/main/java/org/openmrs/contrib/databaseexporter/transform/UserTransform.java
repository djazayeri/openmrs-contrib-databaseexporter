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

import java.util.HashSet;
import java.util.Set;

/**
 * De-identifies the user table
 */
public class UserTransform extends RowTransform {

	//***** PROPERTIES *****

	private String systemIdReplacement;
	private String usernameReplacement;
	private String passwordReplacement;
	private Set<String> usersToPreserve;

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
			Object user = Util.nvl(row.getRawValue("username"), row.getRawValue("system_id"));
			if (!getUsersToPreserve().contains(user)) {
				if (systemIdReplacement != null) {
					row.setRawValue("system_id", Util.evaluateExpression(systemIdReplacement, row));
				}
				if (usernameReplacement != null) {
					row.setRawValue("username", Util.evaluateExpression(usernameReplacement, row));
				}
				if (passwordReplacement != null) {
					String pwAndSalt = Util.evaluateExpression(passwordReplacement, row).toString() + row.getRawValue("salt");
					row.setRawValue("password", Util.encodeString(pwAndSalt));
				}
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

	public Set<String> getUsersToPreserve() {
		if (usersToPreserve == null) {
			usersToPreserve = new HashSet<String>();
			usersToPreserve.add("admin");
			usersToPreserve.add("daemon");
		}
		return usersToPreserve;
	}

	public void setUsersToPreserve(Set<String> usersToPreserve) {
		this.usersToPreserve = usersToPreserve;
	}
}
