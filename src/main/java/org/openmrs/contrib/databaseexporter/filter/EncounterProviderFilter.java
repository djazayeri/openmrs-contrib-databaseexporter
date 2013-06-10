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

import org.openmrs.contrib.databaseexporter.ExportContext;

/**
 * Filters Persons and dependent objects from database
 */
public class EncounterProviderFilter extends RowFilter {

	//**** CONSTRUCTORS *****

	public EncounterProviderFilter() {

		// Do we need something like EncounterProviderRole filter depenency too?

	}

	//***** INSTANCE METHODS *****

	@Override
	public String getTableName() {
		return "encounter_provider";
	}

	@Override
	public String getPrimaryKeyColumn() {
		return "encounter_provider_id";
	}
}
