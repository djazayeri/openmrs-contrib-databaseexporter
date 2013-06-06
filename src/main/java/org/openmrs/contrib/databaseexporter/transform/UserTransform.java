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

import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.openmrs.contrib.databaseexporter.ExportContext;
import org.openmrs.contrib.databaseexporter.TableRow;
import org.openmrs.contrib.databaseexporter.util.Util;

import java.util.List;

/**
 * De-identifies the user table
 */
public class UserTransform extends RowTransform {

	/**
	 * This allows us the possibility of expanding to a random distribution of user replacements
	 * For now, however, we are not supporting this since it is difficult to coordinate with user filtering
	 */
	public enum ReferenceReplacementStrategy {
		NONE, ADMIN
	}

	//***** INTERNAL VARIABLES *****
	private Integer adminUserId;
	private List<String> foreignKeys = null;

	//***** PROPERTIES *****

	private String systemIdReplacement;
	private String usernameReplacement;
	private String passwordReplacement;
	private ReferenceReplacementStrategy referenceReplacementStrategy;

	//***** CONSTRUCTORS *****

	public UserTransform() {}

	//***** INSTANCE METHODS *****

	@Override
	public boolean canTransform(String tableName, ExportContext context) {
		if (tableName.equals("users") || tableName.equals("user_property") || tableName.equals("user_role")) {
			return true;
		}
		if (referenceReplacementStrategy != null && referenceReplacementStrategy != ReferenceReplacementStrategy.NONE) {
			List<String> foreignKeys = getForeignKeys(context);
			for (String foreignKey : foreignKeys) {
				String[] split = foreignKey.split("\\.");
				if (tableName.equalsIgnoreCase(split[0])) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean applyTransform(TableRow row, ExportContext context) {

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

		// In this block we change the references to users in tables that foreign key to it, if specified
		if (referenceReplacementStrategy != null && referenceReplacementStrategy != ReferenceReplacementStrategy.NONE) {
			List<String> foreignKeys = getForeignKeys(context);
			for (String foreignKey : foreignKeys) {
				String[] split = foreignKey.split("\\.");
				if (row.getTableName().equalsIgnoreCase(split[0])) {
					row.setRawValue(split[1], getAdminUserId(context));
				}
			}
		}

		return true;
	}

	private Integer getAdminUserId(ExportContext context) {
		if (adminUserId == null) {
			adminUserId = context.executeQuery("select user_id from users where username = 'admin'", new ScalarHandler<Integer>());
		}
		return adminUserId;
	}

	private List<String> getForeignKeys(ExportContext context) {
		if (foreignKeys == null) {
			foreignKeys = context.getTableMetadata("users").getForeignKeys("user_id");
		}
		return foreignKeys;
	}

	//***** PROPERTY ACCESS *****

	public ReferenceReplacementStrategy getReferenceReplacementStrategy() {
		return referenceReplacementStrategy;
	}

	public void setReferenceReplacementStrategy(ReferenceReplacementStrategy referenceReplacementStrategy) {
		this.referenceReplacementStrategy = referenceReplacementStrategy;
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
