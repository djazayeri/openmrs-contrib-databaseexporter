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
package org.openmrs.contrib.databaseexporter.util;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.openmrs.contrib.databaseexporter.DatabaseCredentials;
import org.openmrs.contrib.databaseexporter.ExportContext;
import org.openmrs.contrib.databaseexporter.util.ListMap;
import org.openmrs.contrib.databaseexporter.util.Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class DbUtil {

	public static Connection openConnection(DatabaseCredentials credentials) {
		try {
			DbUtils.loadDriver(credentials.getDriver());
			return DriverManager.getConnection(credentials.getUrl(), credentials.getUser(), credentials.getPassword());
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Error retrieving connection to the database", e);
		}
	}

	public static ListMap<String, String> getForeignKeyMap(ExportContext context) {
		StringBuilder query = new StringBuilder();
		query.append("select	referenced_table_name, referenced_column_name, table_name, column_name ");
		query.append("from 		information_schema.key_column_usage ");
		query.append("where 	table_schema = database()");
		return context.executeQuery(query.toString(), new ResultSetHandler<ListMap<String, String>>() {
			public ListMap<String, String> handle(ResultSet rs) throws SQLException {
				ListMap<String, String> ret = new ListMap<String, String>();
				while (rs.next()) {
					ret.putInList(rs.getString(1) + "." + rs.getString(2), rs.getString(3) + "." + rs.getString(4));
				}
				return ret;
			}
		});
	}

	public static List<Integer> getPrimaryKeys(String tableName, String primaryKeyColumn, String constraintColumn, Collection<?> constraintValues, ExportContext context) {
		StringBuilder query = new StringBuilder();
		query.append("select ").append(primaryKeyColumn).append(" from ").append(tableName);
		query.append(" where ").append(constraintColumn).append(" in (").append(Util.toString(constraintValues)).append(")");
		return context.executeQuery(query.toString(), new ColumnListHandler<Integer>());
	}

	public static void closeConnection(Connection connection) {
		DbUtils.closeQuietly(connection);
	}

	/**
	 * Write the DDL Header as mysqldump does
	*/
	public static void writeExportHeader(ExportContext context) {
		context.write("-- ------------------------------------------------------");
		context.write("-- Create OpenMRS Schema");
		context.write("-- Generated: " + new Date());
		context.write("-- ------------------------------------------------------");
		context.write("");
		context.write("/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;");
		context.write("/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;");
		context.write("/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;");
		context.write("/*!40101 SET NAMES utf8 */;");
		context.write("/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;");
		context.write("/*!40103 SET TIME_ZONE='+00:00' */;");
		context.write("/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;");
		context.write("/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;");
		context.write("/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;");
		context.write("/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;");
	}

	public static void writeTableSchema(String table, ExportContext context) {
		System.out.println("Writing schema for " + table);
		context.write("--");
		context.write("-- Table structure for table `" + table + "`");
		context.write("--");
		context.write("DROP TABLE IF EXISTS `" + table + "`;");
		context.write("SET @saved_cs_client     = @@character_set_client;");
		context.write("SET character_set_client = utf8;");

		Object[] createTableStatement = context.executeQuery("SHOW CREATE TABLE " + table, new ArrayHandler());
		context.write(createTableStatement[1] + ";");
		context.write("SET character_set_client = @saved_cs_client;");
	}

	/**
	 * Write the header that precedes all table data exports
	 */
	public static void writeTableExportHeader(String table, ExportContext context) {
		context.write("");
		context.write("-- Dumping data for table `" + table + "`");
		context.write("LOCK TABLES `" + table + "` WRITE;");
		context.write("/*!40000 ALTER TABLE `" + table + "` DISABLE KEYS */;");
		context.write("");
	}

	/**
	 * Write the footer that follows all table data exports
	 */
	public static void writeTableExportFooter(String table, ExportContext context) {
		context.write("");
		context.write("/*!40000 ALTER TABLE `" + table + "` ENABLE KEYS */;");
		context.write("UNLOCK TABLES;");
		context.write("");
	}

	/**
	 * Write the DDL Footer as mysqldump does
	 */
	public static void writeExportFooter(ExportContext context) {
		context.write("");
		context.write("/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;");
		context.write("/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;");
		context.write("/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;");
		context.write("/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;");
		context.write("/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;");
		context.write("/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;");
		context.write("/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;");
		context.write("/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;");
	}
}

