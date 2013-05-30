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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

	public static boolean isEmpty(Object o) {
		return o == null || o.equals("");
	}

	public static String nvlStr(Object o, String valueIfNull) {
		if (isEmpty(o)) {
			return valueIfNull;
		}
		return o.toString();
	}

	public static String loadResource(String path) {
		// First try to load from file
		String contents = null;
		try {
			contents = FileUtils.readFileToString(new File(path), "UTF-8");
		}
		catch (Exception e) {}

		// If that didn't work, try loading from classpath
		if (contents == null) {
			InputStream is = null;
			try {
				is = Util.class.getClassLoader().getResourceAsStream(path);
				contents = IOUtils.toString(is, "UTF-8");
			}
			catch (Exception e) {
				System.out.println("ERROR: " + e);
			}
			finally {
				IOUtils.closeQuietly(is);
			}
		}

		if (contents == null) {
			throw new IllegalArgumentException("Unable to load String from resource: " + path);
		}
		return contents;
	}

	public static List<String> getListFromResource(String path) {
		List<String> ret = new ArrayList<String>();
		String s = loadResource(path);
		for (String line : s.split(System.getProperty("line.separator"))) {
			ret.add(line);
		}
		return ret;
	}
}

