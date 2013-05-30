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
import java.util.Collection;
import java.util.List;

/**
 * Returns a particular number of patients that have one or more relationships of each type
 */
public class PatientsHavingRelationshipFilter extends PatientFilter {

	public enum ORDER {
		RANDOM
	}

	//***** PROPERTIES *****

	private Integer numberPerType = 10; // Default to 10
	private boolean includeRetired = false; // Default to false
	private List<Integer> limitToTypes; // optional. if null or empty will include all relationship types
	private ORDER order = ORDER.RANDOM;

	//***** CONSTRUCTORS *****

	public PatientsHavingRelationshipFilter() {}

	//***** INSTANCE METHODS ******

	@Override
	public Collection<Integer> getPatientIds(ExportContext context) {
		List<Integer> ret = new ArrayList<Integer>();

		if (limitToTypes == null || limitToTypes.isEmpty()) {
			String q = "select relationship_type_id from relationship_type" + (includeRetired ? "" : " where retired = 0");
			limitToTypes = context.executeQuery(q, new ColumnListHandler<Integer>());
		}

		for (Integer relTypeId : limitToTypes) {
			String personA = createQuery(relTypeId, "person_a", numberPerType, order);
			ret.addAll(context.executeQuery(personA, new ColumnListHandler<Integer>()));

			String personB = createQuery(relTypeId, "person_b", numberPerType, order);
			ret.addAll(context.executeQuery(personB, new ColumnListHandler<Integer>()));
		}

		return ret;
	}

	protected String createQuery(Integer relTypeId, String sideOfRelationship, Integer num, ORDER order) {
		StringBuilder q = new StringBuilder();
		q.append("select 	distinct p.patient_id ");
		q.append("from 		patient p, relationship r ");
		q.append("where 	p.patient_id = r.").append(sideOfRelationship).append(" ");
		q.append("and		r.relationship = ").append(relTypeId).append(" ");
		q.append("and		p.voided = 0 and r.voided = 0 ");
		q.append("order by	rand() limit ").append(num);
		return q.toString();
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
