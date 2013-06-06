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
import org.openmrs.contrib.databaseexporter.TableConfig;

import java.util.List;

/**
 * Interface for a filter that can configure one or more filters
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class RowFilter  {

	private boolean exclusionFilter = false;

	public abstract void applyFilters(ExportContext context);

	protected final void applyConstraints(String tableName, String columnName, List<Object> values, ExportContext context) {
		TableConfig config = context.getTableData().get(tableName);
		if (exclusionFilter) {
			config.getExcludeConstraints().putAll(columnName, values);
		}
		else {
			config.getIncludeConstraints().putAll(columnName, values);
		}
	}

	public String toString() {
		return getClass().getSimpleName();
	}

	public boolean isExclusionFilter() {
		return exclusionFilter;
	}

	public void setExclusionFilter(boolean exclusionFilter) {
		this.exclusionFilter = exclusionFilter;
	}
}
