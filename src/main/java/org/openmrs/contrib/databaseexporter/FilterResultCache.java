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
package org.openmrs.contrib.databaseexporter;

import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Caches the results from applying one or more row filters across the database to be exported
 */
public class FilterResultCache {

	private Map<String, ListMap<String, Integer>> cache;

	public FilterResultCache() {
		cache = new HashMap<String, ListMap<String, Integer>>();
	}

	public void add(String tableName, String columnName, Integer value) {
		getColumnFilters(tableName).putInList(columnName, value);
	}

	public void add(String tableName, String columnName, Collection<Integer> values) {
		for (Integer value : values) {
			getColumnFilters(tableName).putInList(columnName, value);
		}
	}

	public void add(String tableName, ListMap<String, Integer> values) {
		for (String s : values.keySet()) {
			getColumnFilters(tableName).putAll(s, values.get(s));
		}
	}

	public ListMap<String, Integer> getColumnFilters(String tableName) {
		ListMap<String, Integer> lm = cache.get(tableName);
		if (lm == null) {
			lm = new ListMap<String, Integer>();
			cache.put(tableName, lm);
		}
		return lm;
	}
}
