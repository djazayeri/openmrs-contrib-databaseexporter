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
package org.openmrs.contrib.databaseexporter.filter;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.openmrs.contrib.databaseexporter.ExportContext;
import org.openmrs.contrib.databaseexporter.transform.ForeignKeyTransform;
import org.openmrs.contrib.databaseexporter.transform.RowTransform;
import org.openmrs.contrib.databaseexporter.util.ListMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract class the defines the patients that should be included in a PatientFilter
 */
public abstract class RowFilter {

	//***** PROPERTIES *****

	private List<RowTransform> transforms;
	private List<DependencyFilter> dependencyFilters;

	//***** INSTANCE METHODS *****

	public abstract String getTableName();

	public abstract ListMap<String, Integer> getIds(ExportContext context);

	/**
	 * Primary method responsible for filtering out data during the export process
	 * This method does the following:
	 *  - Executes any configured queries to retrieve references to the rows to keep
	 *  - Constructs temporary tables that contain these references for later joining
	 *  - Iterates across all dependent tables (those with a many-to-one relationship) and detemines which of these to keep by adding these to temporary tables
	 *  - Adds in a transform for associated tables (those with a many-to-many relationship) to replace the foreign key with an alternative
	 */
	public void filter(ExportContext context) {
		ListMap<String, Integer> ids = getIds(context);
		for (String column : ids.keySet()) {
			List<Integer> l = ids.get(column);
			if (l != null) {
				Set<Integer> s = new HashSet<Integer>(l);
				s.remove(null);
				context.registerInTemporaryTable(getTableName(), column, s);
				getTransforms().add(new ForeignKeyTransform(getTableName(), column));
				DependencyFilter df = context.getConfiguration().getDependencyFilters().get(getTableName());
				if (df == null) {
					throw new RuntimeException("You must specify a dependency filter for any tables that you are applying row filters to");
				}
				getDependencyFilters().add(df);
			}
		}
	}

	public String toString() {
		return getClass().getSimpleName();
	}

	//***** PROPERTY ACCESS

	public List<RowTransform> getTransforms() {
		if (transforms == null) {
			transforms = new ArrayList<RowTransform>();
		}
		return transforms;
	}

	public void setTransforms(List<RowTransform> transforms) {
		this.transforms = transforms;
	}

	public List<DependencyFilter> getDependencyFilters() {
		if (dependencyFilters == null) {
			dependencyFilters = new ArrayList<DependencyFilter>();
		}
		return dependencyFilters;
	}

	public void setDependencyFilters(List<DependencyFilter> dependencyFilters) {
		this.dependencyFilters = dependencyFilters;
	}
}
