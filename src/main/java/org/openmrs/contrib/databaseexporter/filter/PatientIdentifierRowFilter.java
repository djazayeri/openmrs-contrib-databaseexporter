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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Returns a particular number of patients that have one or more identifiers of each type
 */
public class PatientIdentifierRowFilter extends PatientFilter {

	public enum ORDER {
		RANDOM
	}

	//***** PROPERTIES *****

	private Integer numberPerType = 10; // Default to 10
	private boolean includeRetired = false; // Default to false
	private List<Integer> limitToTypes; // optional. if null or empty will include all identifier types
	private ORDER order = ORDER.RANDOM;

	//***** CONSTRUCTORS *****

	public PatientIdentifierRowFilter() {}

	//***** INSTANCE METHODS ******

	@Override
	public Set<Integer> getPatientIds(ExportContext context) {
		Set<Integer> ret = new HashSet<Integer>();

		if (limitToTypes == null || limitToTypes.isEmpty()) {
			String q = "select patient_identifier_type_id from patient_identifier_type" + (includeRetired ? "" : " where retired = 0");
			limitToTypes = context.executeQuery(q, new ColumnListHandler<Integer>());
		}

		for (Integer idTypeId : limitToTypes) {
			StringBuilder q = new StringBuilder();
			q.append("select 	distinct p.patient_id ");
			q.append("from 		patient p, patient_identifier i ");
			q.append("where 	p.patient_id = i.patient_id ");
			q.append("and		i.identifier_type = ").append(idTypeId).append(" ");
			q.append("and		p.voided = 0 and i.voided = 0 ");
			q.append("order by	rand() limit ").append(numberPerType);
			ret.addAll(context.executeQuery(q.toString(), new ColumnListHandler<Integer>()));
		}

		return ret;
	}

	//****** PROPERTY ACCESS *****

	public Integer getNumberPerType() {
		return numberPerType;
	}

	public void setNumberPerType(Integer numberPerType) {
		this.numberPerType = numberPerType;
	}

	public boolean isIncludeRetired() {
		return includeRetired;
	}

	public void setIncludeRetired(boolean includeRetired) {
		this.includeRetired = includeRetired;
	}

	public List<Integer> getLimitToTypes() {
		if (limitToTypes == null) {
			limitToTypes = new ArrayList<Integer>();
		}
		return limitToTypes;
	}

	public void setLimitToTypes(List<Integer> limitToTypes) {
		this.limitToTypes = limitToTypes;
	}

	public ORDER getOrder() {
		return order;
	}

	public void setOrder(ORDER order) {
		this.order = order;
	}
}
