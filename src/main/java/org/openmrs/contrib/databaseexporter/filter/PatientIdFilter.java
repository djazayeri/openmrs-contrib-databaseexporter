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

import java.util.Collection;

/**
 * Filter patients given a fixed set of patient ids
 */
public class PatientIdFilter extends PatientFilter {

	private Collection<Integer> patientIds;

	public PatientIdFilter() {}

	@Override
	public Collection<Integer> getPatientIds(ExportContext context) {
		return getPatientIds();
	}

	public Collection<Integer> getPatientIds() {
		return patientIds;
	}

	public void setPatientIds(Collection<Integer> patientIds) {
		this.patientIds = patientIds;
	}
}
