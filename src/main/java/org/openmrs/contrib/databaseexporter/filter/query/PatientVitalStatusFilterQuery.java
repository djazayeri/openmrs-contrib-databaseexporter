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

import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.openmrs.contrib.databaseexporter.ExportContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Returns a particular number of patients in configured set of age ranges
 */
public class PatientVitalStatusFilterQuery extends FilterQuery {

	//***** PROPERTIES *****

	private Integer numberAlive = 10; // Default to 10
	private Integer numberDead = 10; // Default to 10

	//***** CONSTRUCTORS *****

	public PatientVitalStatusFilterQuery() {}

	//***** INSTANCE METHODS ******

	@Override
	public Set<Integer> getIds(ExportContext context) {
		Set<Integer> ret = new HashSet<Integer>();

		StringBuilder aliveQuery = new StringBuilder();
		aliveQuery.append("select p.patient_id from patient p, person n ");
		aliveQuery.append("where  p.voided = 0 and n.voided = 0 and p.patient_id = n.person_id ");
		aliveQuery.append("and	 n.dead = 0 ");
		aliveQuery.append("order by rand() limit " + numberAlive);
		ret.addAll(context.executeQuery(aliveQuery.toString(), new ColumnListHandler<Integer>()));

		StringBuilder deadQuery = new StringBuilder();
		deadQuery.append("select p.patient_id from patient p, person n ");
		deadQuery.append("where  p.voided = 0 and n.voided = 0 and p.patient_id = n.person_id ");
		deadQuery.append("and	 n.dead = 1 ");
		deadQuery.append("order by rand() limit " + numberDead);
		ret.addAll(context.executeQuery(deadQuery.toString(), new ColumnListHandler<Integer>()));

		return ret;
	}

	//****** PROPERTY ACCESS *****


	public Integer getNumberAlive() {
		return numberAlive;
	}

	public void setNumberAlive(Integer numberAlive) {
		this.numberAlive = numberAlive;
	}

	public Integer getNumberDead() {
		return numberDead;
	}

	public void setNumberDead(Integer numberDead) {
		this.numberDead = numberDead;
	}
}
