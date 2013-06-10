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
import org.openmrs.contrib.databaseexporter.filter.query.FilterQuery;
import org.openmrs.contrib.databaseexporter.transform.RowTransform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A Row Filter is designed to remove one or more rows of a primary table from the database export.
 * It must define both the primary table to filter, as well as how any tables that foreign key
 * against it should be dealt with - whether to remove these as well, or whether to transform them
 * so that they refer to other valid rows.
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class RowFilter  {

	private List<RowFilter> dependencyFilters;
	private List<RowTransform> dependencyTransforms;

	//***** PROPERTIES *****

	private List<FilterQuery> filterQueries;

	/**
	 * The table name that this filter restricts on
	 */
	public abstract String getTableName();

	/**
	 * The primary key column that this filter restricts on
	 */
	public abstract String getPrimaryKeyColumn();

	/**
	 * When filtering the primary table, there will often be tables which foreign key to
	 * this primary table and contain associated data that also needs to be filtered.
	 * When we do not wish to preserve these rows, but to simply filter it out, they should
	 * be defined here. Although it is theoretically possible to find a way for this to be handled automatically,
	 * there are some many special cases where we would not want automatic behavior that
	 * specifying exactly what happens manually ends up being both safer and easier.  It gives us full control
	 */
	public List<RowFilter> getDependencyFilters() {
		if (dependencyFilters == null) {
			dependencyFilters = new ArrayList<RowFilter>();
		}
		return dependencyFilters;
	}

	/**
	 * Adds a referenceFilter
	 */
	public void addDependencyFilter(RowFilter filter) {
		getDependencyFilters().add(filter);
	}

	/**
	 * When filtering the primary table, there will be tables that foreign key against it that
	 * we do not want to filter, but we want to transform to some other valid row.
	 * For example, we may define a User Filter that removes most of the existing users from the
	 * system.  But we don't want to remove all of the data and metadata that these users are associated
	 * with (as creator, voided_by, changed_by, etc).  So, this gives us a mechanism for defining
	 * how we should transform those rows that foreign key against filtered out rows to ensure that these
	 * foreign key constraints can be re-applied.
	 */
	public List<RowTransform> getDependencyTransforms() {
		if (dependencyTransforms == null) {
			dependencyTransforms = new ArrayList<RowTransform>();
		}
		return dependencyTransforms;
	}

	/**
	 * Iterates across all of the defined queries and dependency filters and populates the constraints into the context
	 */
	public void applyFilters(ExportContext context) {
		Set<Integer> ids = new HashSet<Integer>();
		for (FilterQuery q : getFilterQueries()) {
			ids.addAll(q.getIds(context));
		}
		if (ids != null && !ids.isEmpty()) {
			context.registerInTemporaryTable(getTableName(), getPrimaryKeyColumn(), ids);
		}

		for (RowFilter dependencyFilter : getDependencyFilters()) {
			dependencyFilter.applyFilters(context);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	//***** PROPERTY ACCESS *****

	public List<FilterQuery> getFilterQueries() {
		if (filterQueries == null) {
			filterQueries = new ArrayList<FilterQuery>();
		}
		return filterQueries;
	}

	public void addFilterQuery(FilterQuery filterQuery) {
		getFilterQueries().add(filterQuery);
	}

	public void setFilterQueries(List<FilterQuery> filterQueries) {
		this.filterQueries = filterQueries;
	}
}
