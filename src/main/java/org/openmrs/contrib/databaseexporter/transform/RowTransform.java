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

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.openmrs.contrib.databaseexporter.ExportContext;
import org.openmrs.contrib.databaseexporter.TableConfig;
import org.openmrs.contrib.databaseexporter.TableRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for a transform that can manipulate a row in one or more tables
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class RowTransform {

	public abstract boolean canTransform(String tableName, ExportContext context);

	/**
	 * Provides a mechanism for transforming the contents of a table row before writing it
	 * Also provides an additional mechanism for excluding a row.  If a transform returns false,
	 * this indicates to the export framework that the row should be excluded altogether
	 */
	public abstract boolean applyTransform(TableRow row, ExportContext context);

	public String toString() {
		return getClass().getSimpleName();
	}
}
