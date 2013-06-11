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

import java.util.Date;

/**
 * Logger for events, that tracks time
 */
public class EventLog {

	private long firstTime;
	private long lastTime;

	public EventLog() {
		firstTime = System.currentTimeMillis();
		lastTime = firstTime;
	}

	public void logEvent(String event) {
		long currentTime = System.currentTimeMillis();
		System.out.println(new Date() + ": " + event);
		lastTime = currentTime;
	}

	public long getTotalTime() {
		return lastTime - firstTime;
	}
}