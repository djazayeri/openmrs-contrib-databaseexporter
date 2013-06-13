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
package org.openmrs.contrib.databaseexporter.query;

import org.openmrs.contrib.databaseexporter.ExportContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Returns a particular number of patients that have one or more program enrollments of each type
 */
public class PatientProgramEnrollmentQuery extends PatientQuery {

	public enum ORDER {
		RANDOM, DATE_ASC, DATE_DESC, NUM_ENCOUNTERS_DESC
	}

	//***** PROPERTIES *****

	private Integer numberActivePerProgram = 10; // Default to 10
	private Integer numberCompletedPerProgram = 10; // Default to 10
	private boolean includeRetired = false; // Default to false
	private Set<Integer> limitToPrograms; // optional. if null or empty will include all programs
	private ORDER order = ORDER.RANDOM;

	//***** CONSTRUCTORS *****

	public PatientProgramEnrollmentQuery() {}

	//***** INSTANCE METHODS ******

	@Override
	public Set<Integer> getIds(ExportContext context) {
		Set<Integer> ret = new HashSet<Integer>();

		if (limitToPrograms == null || limitToPrograms.isEmpty()) {
			String q = "select program_id from program" + (includeRetired ? "" : " where retired = 0");
			limitToPrograms = context.executeIdQuery(q);
		}

		for (Integer programId : limitToPrograms) {
			String activeQuery = createQuery(programId, "date_completed is null", numberActivePerProgram, order);
			ret.addAll(context.executeIdQuery(activeQuery));

			String completedQuery = createQuery(programId, "date_completed is not null", numberCompletedPerProgram, order);
			ret.addAll(context.executeIdQuery(completedQuery));
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

	public Set<Integer> getLimitToPrograms() {
		if (limitToPrograms == null) {
			limitToPrograms = new HashSet<Integer>();
		}
		return limitToPrograms;
	}

	public void setLimitToPrograms(Set<Integer> limitToPrograms) {
		this.limitToPrograms = limitToPrograms;
	}

	public ORDER getOrder() {
		return order;
	}

	public void setOrder(ORDER order) {
		this.order = order;
	}
}
