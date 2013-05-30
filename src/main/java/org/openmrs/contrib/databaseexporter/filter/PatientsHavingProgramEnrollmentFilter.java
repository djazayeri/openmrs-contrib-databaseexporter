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
 * Returns a particular number of patients that have one or more program enrollments of each type
 */
public class PatientsHavingProgramEnrollmentFilter extends PatientFilter {

	public enum ORDER {
		RANDOM, DATE_ASC, DATE_DESC, NUM_ENCOUNTERS_DESC
	}

	//***** PROPERTIES *****

	private Integer numberActivePerProgram = 10; // Default to 10
	private Integer numberCompletedPerProgram = 10; // Default to 10
	private boolean includeRetired = false; // Default to false
	private List<Integer> limitToPrograms; // optional. if null or empty will include all programs
	private ORDER order = ORDER.RANDOM;

	//***** CONSTRUCTORS *****

	public PatientsHavingProgramEnrollmentFilter() {}

	//***** INSTANCE METHODS ******

	@Override
	public Collection<Integer> getPatientIds(ExportContext context) {
		List<Integer> ret = new ArrayList<Integer>();

		if (limitToPrograms == null || limitToPrograms.isEmpty()) {
			String q = "select program_id from program" + (includeRetired ? "" : " where retired = 0");
			limitToPrograms = context.executeQuery(q, new ColumnListHandler<Integer>());
		}

		for (Integer programId : limitToPrograms) {
			String activeQuery = createQuery(programId, "date_completed is null", numberActivePerProgram, order);
			ret.addAll(context.executeQuery(activeQuery, new ColumnListHandler<Integer>()));

			String completedQuery = createQuery(programId, "date_completed is not null", numberCompletedPerProgram, order);
			ret.addAll(context.executeQuery(completedQuery, new ColumnListHandler<Integer>()));
		}

		return ret;
	}

	protected String createQuery(Integer programId, String constraints, Integer num, ORDER order) {
		StringBuilder q = new StringBuilder();
		if (order == ORDER.NUM_ENCOUNTERS_DESC) {
			q.append("select	distinct patient_id from (");
			q.append("	select 		p.patient_id, count(*) ");
			q.append("	from 		patient_program p, encounter e ");
			q.append("	where 		p.patient_id = e.patient_id and p.voided = 0 and e.voided = 0 ");
			q.append("	and			p.program_id = ").append(programId).append(" ");
			q.append("	and	").append(constraints).append(" ");
			q.append("	group by	p.patient_id ");
			q.append("	order by	count(*) desc ");
			q.append(")");
		}
		else {
			q.append("select distinct patient_id from patient_program where voided = 0 ");
			q.append("and program_id = ").append(programId).append(" ");
			q.append("and ").append(constraints).append(" ");
			String orderBy = "rand() asc";
			if (order == ORDER.DATE_ASC) {
				orderBy = "date_enrolled asc";
			}
			else if (order == ORDER.DATE_DESC) {
				orderBy = "date_enrolled desc";
			}
		}
		q.append(" limit ").append(num);
		return q.toString();
	}

	//****** PROPERTY ACCESS *****

	public Integer getNumberActivePerProgram() {
		return numberActivePerProgram;
	}

	public void setNumberActivePerProgram(Integer numberActivePerProgram) {
		this.numberActivePerProgram = numberActivePerProgram;
	}

	public Integer getNumberCompletedPerProgram() {
		return numberCompletedPerProgram;
	}

	public void setNumberCompletedPerProgram(Integer numberCompletedPerProgram) {
		this.numberCompletedPerProgram = numberCompletedPerProgram;
	}

	public boolean isIncludeRetired() {
		return includeRetired;
	}

	public void setIncludeRetired(boolean includeRetired) {
		this.includeRetired = includeRetired;
	}

	public List<Integer> getLimitToPrograms() {
		if (limitToPrograms == null) {
			limitToPrograms = new ArrayList<Integer>();
		}
		return limitToPrograms;
	}

	public void setLimitToPrograms(List<Integer> limitToPrograms) {
		this.limitToPrograms = limitToPrograms;
	}

	public ORDER getOrder() {
		return order;
	}

	public void setOrder(ORDER order) {
		this.order = order;
	}
}
