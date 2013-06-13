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

import java.io.File;
import java.util.Date;

/**
 * Logger for events, that tracks time
 */
public class EventLog {

	private long firstTime;
	private long lastTime;
	private File logFile;

	public EventLog(File logFile) {
		this.logFile = logFile;
		firstTime = System.currentTimeMillis();
		lastTime = firstTime;
	}

	public void logEvent(String event) {
		long currentTime = System.currentTimeMillis();
		String s = new Date() + ": " + event;
		System.out.println(s);
		try {
			s = FileUtils.readFileToString(logFile) + System.getProperty("line.separator") + s;
		}
		catch (Exception e) {}
		try {
			FileUtils.writeStringToFile(logFile, s, "UTF-8");
		}
		catch (Exception e) {}
		lastTime = currentTime;
	}

	public long getTotalTime() {
		return lastTime - firstTime;
	}
}
