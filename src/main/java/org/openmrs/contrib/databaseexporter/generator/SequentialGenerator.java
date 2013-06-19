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
public class SequentialGenerator extends IdentifierGenerator {

	//***** PROPERTIES *****

	private String prefix; // Optional prefix
	private String suffix; // Optional suffix
	private String firstIdentifierBase; // First identifier to start at
	private String baseCharacterSet; // Enables configuration in appropriate Base

	//***** INTERNAL CACHES *****
	private long sequenceValue = -1;

	//***** INSTANCE METHODS *****

	@Override
	public String generateIdentifier(TableRow row, ExportContext context) {

		int minLength = firstIdentifierBase == null ? 1 : firstIdentifierBase.length();

		if (sequenceValue < 0) {
			if (firstIdentifierBase != null) {
				sequenceValue = convertFromBase(firstIdentifierBase, baseCharacterSet.toCharArray());
			}
			else {
				sequenceValue = 1;
			}
		}

		String identifier = convertToBase(sequenceValue, baseCharacterSet.toCharArray(), minLength);

		// Add optional prefix and suffix
		identifier = (prefix == null ? identifier : prefix + identifier);
		identifier = (suffix == null ? identifier : identifier + suffix);

		sequenceValue++;

		return identifier;
	}

	public Long getNextSequenceValue() {
		return sequenceValue;
	}

	/**
	 * Converts a long to a String given the passed base characters
	 * @should convert from long to string in base character set
	 */
	public static String convertToBase(long n, char[] baseCharacters, int padToLength) {
		StringBuilder base = new StringBuilder();
		long numInBase = (long)baseCharacters.length;
		while (n > 0) {
			int index = (int)(n % numInBase);
			base.insert(0, baseCharacters[index]);
			n = (long)(n / numInBase);
		}
		while (base.length() < padToLength) {
			base.insert(0, baseCharacters[0]);
		}
		return base.toString();
	}

	/**
	 * Converts a String back to an long based on the passed base characters
	 * @should convert from string in base character set to long
	 */
	public static long convertFromBase(String s, char[] baseCharacters) {
		long ret = 0;
		char[] inputChars = s.toCharArray();
		long multiplier = 1;
		for (int i = inputChars.length-1; i>=0; i--) {
			int index = -1;
			for (int j=0; j<baseCharacters.length; j++) {
				if (baseCharacters[j] == inputChars[i]) {
					index = j;
				}
			}
			if (index == -1) {
				throw new RuntimeException("Invalid character " + inputChars[i] + " found in " + s);
			}
			ret = ret + multiplier * index;
			multiplier *= baseCharacters.length;
		}
		return ret;
	}

	//***** PROPERTY ACCESS *****


	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public String getFirstIdentifierBase() {
		return firstIdentifierBase;
	}

	public void setFirstIdentifierBase(String firstIdentifierBase) {
		this.firstIdentifierBase = firstIdentifierBase;
	}

	public String getBaseCharacterSet() {
		return baseCharacterSet;
	}

	public void setBaseCharacterSet(String baseCharacterSet) {
		this.baseCharacterSet = baseCharacterSet;
	}

	public long getSequenceValue() {
		return sequenceValue;
	}

	public void setSequenceValue(long sequenceValue) {
		this.sequenceValue = sequenceValue;
	}
}