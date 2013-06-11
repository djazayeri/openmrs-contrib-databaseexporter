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

import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.openmrs.contrib.databaseexporter.ExportContext;
import org.openmrs.contrib.databaseexporter.util.AgeRange;
import org.openmrs.contrib.databaseexporter.util.ListMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Returns a particular number of patients in configured set of age ranges
 */
public abstract class PatientFilter extends RowFilter {

	@Override
	public String getTableName() {
		return "patient";
	}

	@Override
	public ListMap<String, Integer> getIds(ExportContext context) {
		ListMap<String, Integer> ret = new ListMap<String, Integer>();
		ret.putAll("patient_id", getPatientIds(context));
		return ret;
	}

	public abstract Collection<Integer> getPatientIds(ExportContext context);
}
