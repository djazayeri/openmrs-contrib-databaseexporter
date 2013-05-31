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

import org.openmrs.contrib.databaseexporter.ExportContext;
import org.openmrs.contrib.databaseexporter.TableRow;

/**
 * This transform supports two different configurable options
 * * De-identifying location names and descriptions
 * * Scrambling the locations associated with data such as obs, encounter, patient_program, and person_attribute
 *   in a way that these are consistent for each patient
 */
public class LocationTransform extends RowTransform {

	//***** CONSTRUCTORS *****

	public LocationTransform() {}

	//***** INSTANCE METHODS *****

	public boolean applyTransform(TableRow row, ExportContext context) {
		// TODO: Implement this
		return true;
	}
}
