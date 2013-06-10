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

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.openmrs.contrib.databaseexporter.ExportContext;

import java.util.Set;

/**
 * Abstract class the defines the patients that should be included in a PatientFilter
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class FilterQuery {

	public abstract Set<Integer> getIds(ExportContext context);

	public String toString() {
		return getClass().getSimpleName();
	}
}
