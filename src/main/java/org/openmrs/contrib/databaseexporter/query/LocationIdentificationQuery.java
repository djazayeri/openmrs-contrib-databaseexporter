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
package org.openmrs.contrib.databaseexporter.query;

import org.openmrs.contrib.databaseexporter.ExportContext;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Return the ids represented by the union of all of the passed query results
 */
public class LocationIdentificationQuery extends LocationQuery {

	//***** PROPERTIES *****

	private Set<Integer> locationIds;
	private Set<String> locationNames;

	//***** CONSTRUCTORS *****

	public LocationIdentificationQuery() {}

	public LocationIdentificationQuery(String... locationNames) {
		if (locationNames != null) {
			for (String u : locationNames) {
				getLocationNames().add(u);
			}
		}
	}

	//***** INSTANCE METHODS *****

	@Override
	public Set<Integer> getIds(ExportContext context) {
		Set<Integer> ret = new HashSet<Integer>();
		if (getLocationIds() != null) {
			ret.addAll(getLocationIds());
		}
		if (getLocationNames() != null && !getLocationNames().isEmpty()) {
			StringBuilder q = new StringBuilder();
			q.append("select location_id from location where name in (");
			for (Iterator<String> i = getLocationNames().iterator(); i.hasNext();) {
				q.append("'").append(i.next()).append("'").append(i.hasNext() ? "," : "");
			}
			q.append(")");
			ret.addAll(context.executeIdQuery(q.toString()));
		}
		return ret;
	}

	//***** ACCESSOR *****

	public Set<Integer> getLocationIds() {
		if (locationIds == null) {
			locationIds = new HashSet<Integer>();
		}
		return locationIds;
	}

	public void setLocationIds(Set<Integer> locationIds) {
		this.locationIds = locationIds;
	}

	public Set<String> getLocationNames() {
		if (locationNames == null) {
			locationNames = new HashSet<String>();
		}
		return locationNames;
	}

	public void setLocationNames(Set<String> locationNames) {
		this.locationNames = locationNames;
	}
}
