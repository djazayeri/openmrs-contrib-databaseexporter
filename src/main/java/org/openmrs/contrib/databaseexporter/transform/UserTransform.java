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
import org.openmrs.contrib.databaseexporter.util.DbUtil;
import org.openmrs.contrib.databaseexporter.util.Util;

import java.util.List;

/**
 * De-identifies the user table
 */
public class UserTransform extends RowTransform {

	//***** INTERNAL VARIABLES *****
	private List<String> foreignKeys = null;

	//***** PROPERTIES *****

	private List<Integer> usersToKeep;
	private String systemIdReplacement;
	private String usernameReplacement;
	private String passwordReplacement;

	//***** CONSTRUCTORS *****

	public UserTransform() {}

	//***** INSTANCE METHODS *****

	@Override
	public boolean canTransform(String tableName, ExportContext context) {
		if (tableName.equals("users") || tableName.equals("user_property") || tableName.equals("user_role")) {
			return true;
		}
		List<String> foreignKeys = getForeignKeys(context);
		for (String foreignKey : foreignKeys) {
			String[] split = foreignKey.split("\\.");
			if (tableName.equalsIgnoreCase(split[0])) {
				return true;
			}
		}
		return false;
	}

	public boolean applyTransform(TableRow row, ExportContext context) {
		if (row.getTableName().equals("users")) {
			if (keepUser(row.getRawValue("user_id"))) {
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
				return true;
			}
			else {
				return false;
			}
		}
		if (row.getTableName().equals("user_property")) {
			return false;
		}
		if (row.getTableName().equals("user_role") && !keepUser(row.getRawValue("user_id"))) {
			return false;
		}
		List<String> foreignKeys = getForeignKeys(context);
		for (String foreignKey : foreignKeys) {
			String[] split = foreignKey.split("\\.");
			if (row.getTableName().equalsIgnoreCase(split[0])) {
				row.setRawValue(split[1], getUsersToKeep().get(0));
			}
		}

		// TODO: What about tables like serialized_object and sync_record

		return true;
	}

	public boolean keepUser(Object userId) {
		return (usersToKeep == null || usersToKeep.isEmpty() || usersToKeep.contains(Integer.valueOf(userId.toString())));
	}

	public List<String> getForeignKeys(ExportContext context) {
		if (foreignKeys == null) {
			foreignKeys = context.getTableMetadata("users").getForeignKeys("user_id");
		}
		return foreignKeys;
	}

	//***** PROPERTY ACCESS *****

	public List<Integer> getUsersToKeep() {
		return usersToKeep;
	}

	public void setUsersToKeep(List<Integer> usersToKeep) {
		this.usersToKeep = usersToKeep;
	}

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
}
