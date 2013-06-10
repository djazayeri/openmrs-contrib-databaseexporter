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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * De-identifies the user table
 */
public class UserTransform extends RowTransform {

	// Limit the known tables that are really user data and should be removed if we are not keeping certain users
	private static List<String> userDataTables = Arrays.asList(
		"users", "user_property", "user_role", "notification_alert_recipient",
		"usagestatistics_daily", "usagestatistics_usage"
	);

	//***** PROPERTIES *****

	private List<Integer> limitToUsers;
	private String systemIdReplacement;
	private String usernameReplacement;
	private String passwordReplacement;
	private boolean scrambleUsersInData;

	//***** CONSTRUCTORS *****

	public UserTransform() {}

	//***** INSTANCE METHODS *****

	@Override
	public boolean canTransform(String tableName, ExportContext context) {
		if (tableName.equals("users")) {
			return true;
		}
		if (!getLimitToUsers().isEmpty() && userDataTables.contains(tableName)) {
			return true;
		}
		if (scrambleUsersInData) {
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

		// First, remove any rows altogether if configured to do so
		if (!getLimitToUsers().isEmpty()) {
			Set<Integer> l = new HashSet<Integer>(getLimitToUsers());

			if (userDataTables.contains(row.getTableName())) {
				Object userId = row.getRawValue("user_id");
				boolean requiredUser = getUserId("admin", context).equals(userId) || getUserId("daemon", context).equals(userId);
				if (l.contains(Integer.valueOf(userId.toString())) || requiredUser) {
					getUserIdTransform().addValidValue(userId);
					return true;
				}
				return false;
			}
		}

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

	@Override
	public void cleanup(TableRow row, ExportContext context) {
		getUserIdTransform().applyTransform(row, context);
	}

	//***** INTERNAL CACHES  *****

	private ForeignKeyTransform userIdTransform;
	private ForeignKeyTransform getUserIdTransform() {
		if (userIdTransform == null) {
			userIdTransform = new ForeignKeyTransform();
			userIdTransform.setReferencedTable("users");
			userIdTransform.setReferencedColumn("user_id");
			userIdTransform.setOnlyReplaceIfInvalid(!isScrambleUsersInData());
		}
		return userIdTransform;
	}

	private Map<String, Object> userIdMap;
	private Object getUserId(String username, ExportContext context) {
		if (userIdMap == null) {
			userIdMap = context.executeQuery("select user_id, username from users", new ResultSetHandler<Map<String, Object>>() {
				public Map<String, Object> handle(ResultSet rs) throws SQLException {
					Map<String, Object> ret = new HashMap<String, Object>();
					while (rs.next()) {
						ret.put(rs.getString(2), rs.getInt(1));
					}
					return ret;
				}
			});
		}
		return userIdMap.get(username);
	}

	private List<String> foreignKeys = null;
	private List<String> getForeignKeys(ExportContext context) {
		if (foreignKeys == null) {
			foreignKeys = context.getTableMetadata("users").getForeignKeys("user_id");
		}
		return foreignKeys;
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

	public boolean isScrambleUsersInData() {
		return scrambleUsersInData;
	}

	public void setScrambleUsersInData(boolean scrambleUsersInData) {
		this.scrambleUsersInData = scrambleUsersInData;
	}
}
