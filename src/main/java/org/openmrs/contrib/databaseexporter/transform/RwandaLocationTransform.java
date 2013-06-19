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

import java.util.Iterator;

/**
 * This transform does everything that the standard LocationTransform does,
 * as well as replacing any global property values as necessary
 */
public class RwandaLocationTransform extends LocationTransform {

	public RwandaLocationTransform() {}

	//***** INSTANCE METHODS *****

	@Override
	public boolean canTransform(String tableName, ExportContext context) {
		boolean ret = super.canTransform(tableName, context);
		ret = ret || tableName.equals("global_property");
		return ret;
	}

	@Override
	public boolean applyTransform(TableRow row, ExportContext context) {
		boolean ret = super.applyTransform(row, context);
		if (ret) {
			// At this point, all of the caches should be populated

			if (row.getTableName().equals("global_property")) {
				String propertyName = row.getRawValue("property").toString();

				if (propertyName.equals("reports.currentlocation")) {
					String replacement = getUsedNames().isEmpty() ? "" : getUsedNames().iterator().next();
					row.setRawValue("property_value", replacement);
				}
				else if (propertyName.equals("registration.defaultLocationCode")) {
					row.setRawValue("property_value", "1111");
				}

				if (propertyName.equals("registration.rwandaLocationCodes") ||
					propertyName.equals("dataqualitytools.sitesToList") ||
					propertyName.equals("dataqualitytools.sitesToTally")) {
					StringBuilder sb = new StringBuilder();
					int num=0;
					for (Iterator<String> i = getUsedNames().iterator(); i.hasNext();) {
						num++;
						String name = i.next();
						if (propertyName.equals("registration.rwandaLocationCodes")) {
							sb.append(name + ":" + num + (i.hasNext() ? "|" : ""));
						}
						else if (propertyName.equals("dataqualitytools.sitesToList")) {
							sb.append(name + ":" + name + (i.hasNext() ? "|" : ""));
						}
						else if (propertyName.equals("dataqualitytools.sitesToTally")) {
							sb.append(name + (i.hasNext() ? "|" : ""));
						}
					}
					row.setRawValue("property_value", sb.toString());
				}
			}
		}
		return ret;
	}
}
