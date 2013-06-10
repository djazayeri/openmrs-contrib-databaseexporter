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

import org.openmrs.contrib.databaseexporter.ExportContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Filter given a fixed set of ids
 */
public class IdFilterQuery extends FilterQuery {

	private Set<Integer> ids;

	public IdFilterQuery() {}

	@Override
	public Set<Integer> getIds(ExportContext context) {
		return getIds();
	}

	public Set<Integer> getIds() {
		if (ids == null) {
			ids = new HashSet<Integer>();
		}
		return ids;
	}

	public void addIds(Integer... ids) {
		for (Integer id : ids) {
			getIds().add(id);
		}
	}

	public void setIds(Set<Integer> ids) {
		this.ids = ids;
	}
}
