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
package org.openmrs.contrib.databaseexporter.generator;

import org.openmrs.contrib.databaseexporter.ExportContext;
import org.openmrs.contrib.databaseexporter.TableRow;

/**
 * Returns a set of ids for a particular table column
 */
public class VerhoeffGenerator extends SequentialGenerator {

	private static final char[] CHECK_DIGIT_CHARS = {'A','B','C','D','E','F','G','H','I','J'};

	public VerhoeffGenerator() {
		F[0] = F0;
		F[1] = F1;
		for (int i = 2; i < 8; i++) {
			F[i] = new int[10];
			for (int j = 0; j < 10; j++)
				F[i][j] = F[i - 1][F[1][j]];
		}
	}

	//***** INSTANCE METHODS *****

	@Override
	public String generateIdentifier(TableRow row, ExportContext context) {
		String identifier = super.generateIdentifier(row, context);
		int[] a = getBase(Integer.parseInt(identifier), identifier.length());
		insertCheck(a);
		char checkLetter = CHECK_DIGIT_CHARS[a[0]];
		return identifier + "-" + checkLetter;
	}

	private int[] getBase(int num, int length) {
		int[] a = new int[length + 1];
		int x = 1;
		for (int i = length; i-- > 0;) {
			int y = num / x;
			a[i + 1] = y % 10;
			x = x * 10;
		}
		return a;
	}

	private int insertCheck(int[] a) {
		int check = 0;
		for (int i = 1; i < a.length; i++)
			check = op[check][F[i % 8][a[i]]];
		a[0] = inv[check];
		return a[0];
	}

	private int[][] F = new int[8][];

	private static final int[] F0 = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

	private static final int[] F1 = { 1, 5, 7, 6, 2, 8, 3, 0, 9, 4 };

	private static final int[][] op = { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, { 1, 2, 3, 4, 0, 6, 7, 8, 9, 5 },
			{ 2, 3, 4, 0, 1, 7, 8, 9, 5, 6 }, { 3, 4, 0, 1, 2, 8, 9, 5, 6, 7 }, { 4, 0, 1, 2, 3, 9, 5, 6, 7, 8 },
			{ 5, 9, 8, 7, 6, 0, 4, 3, 2, 1 }, { 6, 5, 9, 8, 7, 1, 0, 4, 3, 2 }, { 7, 6, 5, 9, 8, 2, 1, 0, 4, 3 },
			{ 8, 7, 6, 5, 9, 3, 2, 1, 0, 4 }, { 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 } };

	private static final int[] inv = { 0, 4, 3, 2, 1, 5, 6, 7, 8, 9 };
}