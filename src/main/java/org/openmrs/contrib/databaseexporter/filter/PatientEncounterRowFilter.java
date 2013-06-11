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
 * Returns a particular number of patients that have one or more encounters of each type
 */
public class PatientEncounterRowFilter extends PatientFilter {

	public enum ORDER {
		RANDOM, DATE_ASC, DATE_DESC, NUM_OBS_DESC
	}

	//***** PROPERTIES *****

	private Integer numberPerType = 10; // Default to 10
	private Integer numberPerForm = 10; // Default to 10
	private boolean includeRetired = false; // Default to false
	private List<Integer> limitToTypes; // optional. if null or empty will include all encounter types
	private List<Integer> limitToForms; // optional, if null or empty will include all forms
	private ORDER order = ORDER.RANDOM;

	//***** CONSTRUCTORS *****

	public PatientEncounterRowFilter() {}

	//***** INSTANCE METHODS ******

	@Override
	public Set<Integer> getPatientIds(ExportContext context) {
		Set<Integer> ret = new HashSet<Integer>();

		if (limitToTypes == null || limitToTypes.isEmpty()) {
			String q = "select encounter_type_id from encounter_type" + (includeRetired ? "" : " where retired = 0");
			limitToTypes = context.executeQuery(q, new ColumnListHandler<Integer>());
		}
		if (limitToForms == null || limitToForms.isEmpty()) {
			String q = "select form_id from form" + (includeRetired ? "" : " where retired = 0");
			limitToForms = context.executeQuery(q, new ColumnListHandler<Integer>());
		}

		for (Integer encTypeId : limitToTypes) {
			String q = createQuery("encounter_type", encTypeId, numberPerType, order);
			ret.addAll(context.executeQuery(q, new ColumnListHandler<Integer>()));
		}

		for (Integer formId : limitToForms) {
			String q = createQuery("form_id", formId, numberPerForm, order);
			ret.addAll(context.executeQuery(q, new ColumnListHandler<Integer>()));
		}

		return ret;
	}

	protected String createQuery(String constraintColumn, Integer constraintValue, Integer num, ORDER order) {
		StringBuilder q = new StringBuilder();
		if (order == ORDER.NUM_OBS_DESC) {
			q.append("select	distinct p.patient_id from (");
			q.append("	select 		e.patient_id, e.encounter_id, count(*) ");
			q.append(	"from 		encounter e, obs o ");
			q.append("	where 		e.encounter_id = o.encounter_id and e.voided = 0 and o.voided = 0 ");
			q.append("	and			e." + constraintColumn + " = " + constraintValue + " ");
			q.append("	group by	e.patient_id, e.encounter_id ");
			q.append("	order by	count(*) desc ");
			q.append(")	p limit ").append(num);
		}
		else {
			q.append("select distinct patient_id from encounter where voided = 0");
			q.append(" and ").append(constraintColumn).append(" = ").append(constraintValue);
			String orderBy = "rand() asc";
			if (order == ORDER.DATE_ASC) {
				orderBy = "encounter_datetime asc";
			}
			else if (order == ORDER.DATE_DESC) {
				orderBy = "encounter_datetime desc";
			}
			q.append(" order by ").append(orderBy).append(" limit ").append(num);
		}
		return q.toString();
	}

	//****** PROPERTY ACCESS *****

	public Integer getNumberPerType() {
		return numberPerType;
	}

	public void setNumberPerType(Integer numberPerType) {
		this.numberPerType = numberPerType;
	}

	public Integer getNumberPerForm() {
		return numberPerForm;
	}

	public void setNumberPerForm(Integer numberPerForm) {
		this.numberPerForm = numberPerForm;
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

	public List<Integer> getLimitToForms() {
		if (limitToForms == null) {
			limitToForms = new ArrayList<Integer>();
		}
		return limitToForms;
	}

	public void setLimitToForms(List<Integer> limitToForms) {
		this.limitToForms = limitToForms;
	}

	public ORDER getOrder() {
		return order;
	}

	public void setOrder(ORDER order) {
		this.order = order;
	}
}
