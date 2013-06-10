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
package org.openmrs.contrib.databaseexporter.filter.query;

import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.openmrs.contrib.databaseexporter.ExportContext;
import org.openmrs.contrib.databaseexporter.TableConfig;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Return the ids represented by the union of all of the passed query results
 */
public class SqlFilterQuery extends FilterQuery {

	//***** PROPERTIES *****

	private Set<String> sqlQueries;

	//***** CONSTRUCTORS *****

	public SqlFilterQuery() {}

	public SqlFilterQuery(Collection<String> queries) {
		getSqlQueries().addAll(queries);
	}

	public SqlFilterQuery(String query) {
		getSqlQueries().add(query);
	}

	//***** INSTANCE METHODS *****

	@Override
	public Set<Integer> getIds(ExportContext context) {
		Set<Integer> ret = new HashSet<Integer>();
		for (String sql : getSqlQueries()) {
			for (String table : context.getTableData().keySet()) {
				TableConfig tableConfig = context.getTableData().get(table);
				String tn = tableConfig.getTableMetadata().getTableName();
				if (tableConfig.getTemporaryTableName() != null) {
					sql = sql.replace(" " + tn + " ", " " + tableConfig.getTemporaryTableName() + " ");
					sql = sql.replace(" " + tn + "." + tableConfig.getPrimaryKeyName(), " " + tableConfig.getTemporaryTableName() + "." + tableConfig.getPrimaryKeyName());
				}
			}
			ret.addAll(context.executeQuery(sql, new ColumnListHandler<Integer>()));
		}
		return ret;
	}

	//***** ACCESSOR *****

	public Set<String> getSqlQueries() {
		if (sqlQueries == null) {
			sqlQueries = new LinkedHashSet<String>();
		}
		return sqlQueries;
	}

	public void setSqlQueries(Set<String> sqlQueries) {
		this.sqlQueries = sqlQueries;
	}
}
