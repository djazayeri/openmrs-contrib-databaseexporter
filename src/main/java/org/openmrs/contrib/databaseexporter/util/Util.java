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
import org.openmrs.contrib.databaseexporter.TableRow;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

	public static List<String> toList(String s, String separator) {
		List<String> ret = new ArrayList<String>();
		for (String element : s.split(separator)) {
			ret.add(element);
		}
		return ret;
	}

	public static boolean isEmpty(Object o) {
		return o == null || o.equals("");
	}

	public static boolean notEmpty(Object o) {
		return !isEmpty(o);
	}

	public static <T> T firstNotNull(T... values) {
		for (T val : values) {
			if (notEmpty(val)) {
				return val;
			}
		}
		return null;
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

	public static List<Map<String, String>> getListOfMapsFromResource(String path, String elementSeparator) {
		List<Map<String, String>> ret = new ArrayList<Map<String, String>>();
		String s = loadResource(path).trim();
		List<String> headers = null;
		for (String line : s.split(System.getProperty("line.separator"))) {
			if (headers == null) {
				headers = toList(line, elementSeparator);
			}
			else {
				Map<String, String> row = new LinkedHashMap<String, String>();
				List<String> elements = toList(line, elementSeparator);
				for (int i=0; i<headers.size(); i++) {
					row.put(headers.get(i), elements.get(i));
				}
				ret.add(row);
			}
		}
		return ret;
	}

	public static String encodeString(String strToEncode) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			byte[] input = strToEncode.getBytes("UTF-8");
			return hexString(md.digest(input));
		}
		catch (Exception e) {
			throw new RuntimeException("Unable to encode string " + strToEncode, e);
		}
	}

	public static String hexString(byte[] block) {
		StringBuffer buf = new StringBuffer();
		char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		int len = block.length;
		int high = 0;
		int low = 0;
		for (int i = 0; i < len; i++) {
			high = ((block[i] & 0xf0) >> 4);
			low = (block[i] & 0x0f);
			buf.append(hexChars[high]);
			buf.append(hexChars[low]);
		}
		return buf.toString();
	}

	public static Object evaluateExpression(Object expression, TableRow row) {
		Object ret = expression;
		if (ret != null && ret instanceof String && ret.toString().contains("${")) {
			String s = (String)ret;
			for (String c : row.getColumns()) {
				s = s.replace("${"+c+"}", Util.nvlStr(row.getRawValue(c), ""));
			}
			ret = s;
		}
		return ret;
	}

	public static <T> T getRandomElementFromList(List<T> l) {
		return l.get((int) (Math.random() * l.size()));
	}

	public static String formatTimeDifference(long ms) {
		if (ms < 1000) {
			return "< 1 second";
		}
		long seconds = ms/1000;
		if (seconds < 60) {
			return ((int)seconds) + " seconds";
		}
		int minutes = (int)seconds/60;
		return minutes + " minutes";
	}

	public static String toPercent(Number numerator, Number denominator, int decimals) {
		BigDecimal bd = new BigDecimal(100 * numerator.doubleValue() / denominator.doubleValue());
		bd = bd.setScale(decimals, BigDecimal.ROUND_HALF_UP);
		return bd.toString();
	}
}

