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
import org.openmrs.contrib.databaseexporter.util.Util;

/**
 * Returns a set of ids for a particular table column
 */
public class LuhnGenerator extends SequentialGenerator {

	private String checkDigitSeparator;

	//***** INSTANCE METHODS *****

	@Override
	public String generateIdentifier(TableRow row, ExportContext context) {
		String identifier = super.generateIdentifier(row, context);
		String standardized = standardizeValidIdentifier(identifier);
		return standardized + Util.nvl(checkDigitSeparator, "") + computeCheckDigit(standardized);
	}

	/**
	 * Computes the check digit for the passed undecorated identifier
	 * @should compute a valid check digit
	 */
	public char computeCheckDigit(String undecoratedIdentifier) {
		int factor = 2;
		int sum = 0;
		char[] inputChars = standardizeValidIdentifier(undecoratedIdentifier).toCharArray();
		char[] baseChars = getBaseCharacterSet().toCharArray();
		int mod = baseChars.length;

		// Starting from the right and working leftwards is easier since the initial "factor" will always be "2"
		for (int i = inputChars.length - 1; i >= 0; i--) {
			int codePoint = -1;
			for (int j=0; j<baseChars.length; j++) {
				if (baseChars[j] == inputChars[i]) {
					codePoint = j;
				}
			}
			if (codePoint == -1) {
				throw new RuntimeException("Invalid character specified for Luhn Generator");
			}
			int addend = factor * codePoint;

			// Alternate the "factor" that each "codePoint" is multiplied by
			factor = (factor == 2) ? 1 : 2;

			// Sum the digits as expressed in base "n"
			addend = (addend / mod) + (addend % mod);
			sum += addend;
		}

		// Calculate the number that must be added to the "sum" to make it divisible by "n"
		int remainder = sum % mod;
		int checkCodePoint = mod - remainder;
		checkCodePoint %= mod;

		return baseChars[checkCodePoint];
	}

	public String standardizeValidIdentifier(String validIdentifier) {
		if (validIdentifier != null) {
			validIdentifier = validIdentifier.toUpperCase().trim();
		}
		return validIdentifier;
	}

	public String getCheckDigitSeparator() {
		return checkDigitSeparator;
	}

	public void setCheckDigitSeparator(String checkDigitSeparator) {
		this.checkDigitSeparator = checkDigitSeparator;
	}
}