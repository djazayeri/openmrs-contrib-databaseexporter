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

import org.openmrs.contrib.databaseexporter.ExportContext;
import org.openmrs.contrib.databaseexporter.TableRow;
import org.openmrs.contrib.databaseexporter.util.Util;

import java.util.List;
import java.util.Map;

/**
 * This is an abstract class that provides common framework for transforming data
 * related to structured addresses, for example person_address data and address_hierarchy data
 */
public abstract class StructuredAddressTransform extends RowTransform {

	private static String CONFIG_PACKAGE = "org/openmrs/contrib/databaseexporter/";
	public static String[] addressColumns = {
			"address1", "address2", "address3", "address4", "address5", "address6",
			"city_village", "county_district", "state_province", "postal_code", "country", "latitude", "longitude"
	};

	protected List<Map<String, String>> replacements = null;

	//***** CONFIGURABLE PROPERTIES *****

	private String addressPath = CONFIG_PACKAGE + "addresses.config";
	private String addressSeparator = ",";

	//***** INSTANCE METHODS *****

	public List<Map<String, String>> getReplacements() {
		if (replacements == null) {
			replacements = Util.getListOfMapsFromResource(addressPath, addressSeparator);
		}
		return replacements;
	}

	public Map<String, String> getRandomReplacementAddress(TableRow row, ExportContext context) {
		return Util.getRandomElementFromList(getReplacements());
	}

	//***** PROPERTIES *****

	public String getAddressPath() {
		return addressPath;
	}

	public void setAddressPath(String addressPath) {
		this.addressPath = addressPath;
	}

	public String getAddressSeparator() {
		return addressSeparator;
	}

	public void setAddressSeparator(String addressSeparator) {
		this.addressSeparator = addressSeparator;
	}
}
