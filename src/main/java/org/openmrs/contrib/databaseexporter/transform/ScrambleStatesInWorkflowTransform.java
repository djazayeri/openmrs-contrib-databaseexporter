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

import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.openmrs.contrib.databaseexporter.ExportContext;
import org.openmrs.contrib.databaseexporter.TableRow;
import org.openmrs.contrib.databaseexporter.util.Util;

import java.util.List;

/**
 * This transform takes in a particular Program Workflow id, and will
 *   # Get all of the possible states for this workflow, or a subset of the states, if provided
 *   # Update all Patient States for this workflow with one of these values
 * The goal of this is to make it impossible to identify which patients had a possible state in a given workflow
 */
public class ScrambleStatesInWorkflowTransform extends RowTransform {

	private Integer workflowToScramble;
	private List<Integer> possibleStates;

	//***** CONSTRUCTORS *****

	public ScrambleStatesInWorkflowTransform() {}

	//***** INSTANCE METHODS *****

	@Override
	public boolean canTransform(String tableName, ExportContext context) {
		return tableName.equals("patient_state");
	}

	public boolean transformRow(TableRow row, ExportContext context) {
		if (row.getTableName().equalsIgnoreCase("patient_state")) {
			if (possibleStates == null || possibleStates.isEmpty()) {
				String stateQuery = "select program_workflow_state_id from program_workflow_state where program_workflow_id = " + workflowToScramble + " and retired = 0";
				possibleStates = context.executeQuery(stateQuery, new ColumnListHandler<Integer>());
			}
			row.setRawValue("state", Util.getRandomElementFromList(possibleStates));
		}
		return true;
	}

	public Integer getWorkflowToScramble() {
		return workflowToScramble;
	}

	public void setWorkflowToScramble(Integer workflowToScramble) {
		this.workflowToScramble = workflowToScramble;
	}

	public List<Integer> getPossibleStates() {
		return possibleStates;
	}

	public void setPossibleStates(List<Integer> possibleStates) {
		this.possibleStates = possibleStates;
	}
}
