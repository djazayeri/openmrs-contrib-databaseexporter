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
package org.openmrs.contrib.databaseexporter.util;

import java.util.Arrays;
import java.util.Collection;

public class Util {

	public static String toString(Collection<?> c) {
		StringBuilder ret = new StringBuilder();
		for (Object o : c) {
			ret.append(ret.length() == 0 ? "" : ",").append(o);
		}
		return ret.toString();
	}

	public static String toString(Object[] c) {
		return toString(Arrays.asList(c));
	}

}

