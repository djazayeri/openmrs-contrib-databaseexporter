openmrs-contrib-databaseexporter
================================

	{
		"@class" : "org.openmrs.contrib.databaseexporter.filter.PatientsHavingAgeFilter",
		"numberPerAgeRange": 100,
		"ageRanges": {
			{"maxAge": 2},
			{"minAge": 3, "maxAge": 10},
			{"minAge": 11, "maxAge": 15},
			{"minAge": 16, "maxAge": 30},
			{"minAge": 31, "maxAge": 60},
			{"minAge": 61}
		}
	},
	{
		"@class" : "org.openmrs.contrib.databaseexporter.filter.PatientsHavingEncounterFilter",
		"numberPerType": 50,
		"numberPerForm", 50,
		"limitToTypes": [optional],
		"limitToForms": [optional]
	},
	{
		"@class" : "org.openmrs.contrib.databaseexporter.filter.PatientsHavingProgramEnrollmentFilter",
		"numberActivePerProgram": 100,
		"numberCompletedPerProgram": 50,
		"limitToPrograms": [optional]
	},
	{
		"@class" : "org.openmrs.contrib.databaseexporter.filter.PatientsHavingRelationshipFilter",
		"numberPerType": 50,
		"limitToTypes": [optional]
	},
	{
		"@class" : "org.openmrs.contrib.databaseexporter.filter.PatientsHavingIdentifierFilter",
		"numberPerType": 100,
		"limitToTypes": [optional]
	},
	{
		"@class" : "org.openmrs.contrib.databaseexporter.filter.PatientsHavingVitalStatusFilter",
		"numberDead": 100
	}



		{
			"@class" : "org.openmrs.contrib.databaseexporter.filter.ColumnValueFilter",
			"tableName": "patient_identifier_type",
			"columnName": "patient_identifier_type_id",
			"values": [5,2]
		},
		{
			"@class" : "org.openmrs.contrib.databaseexporter.filter.PatientIdFilter",
			"patientIds": [1243,1386,1425]
		},
		{
			"@class" : "org.openmrs.contrib.databaseexporter.filter.PatientsHavingAgeFilter",
			"numberPerAgeRange": 3,
			"ageRanges": [
				{"maxAge": 2},
				{"minAge": 3, "maxAge": 10},
				{"minAge": 11, "maxAge": 15},
				{"minAge": 16, "maxAge": 30},
				{"minAge": 31, "maxAge": 60},
				{"minAge": 61}
			]
		},
		{
			"@class" : "org.openmrs.contrib.databaseexporter.filter.PatientsHavingEncounterFilter",
			"numberPerType": 5,
			"numberPerForm": 5
		},
		{
			"@class" : "org.openmrs.contrib.databaseexporter.filter.PatientsHavingProgramEnrollmentFilter",
			"numberActivePerProgram": 5,
			"numberCompletedPerProgram": 5
		},
		{
			"@class" : "org.openmrs.contrib.databaseexporter.filter.PatientsHavingRelationshipFilter",
			"numberPerType": 5
		},
		{
			"@class" : "org.openmrs.contrib.databaseexporter.filter.PatientsHavingIdentifierFilter",
			"numberPerType": 5
		},
		{
			"@class" : "org.openmrs.contrib.databaseexporter.filter.PatientsHavingVitalStatusFilter",
			"numberDead": 5
		}

				{
        			"@class" : "org.openmrs.contrib.databaseexporter.filter.GlobalPropertyFilter",
        			"patterns": ["htmlform%"]
        		},
        		{
        			"@class" : "org.openmrs.contrib.databaseexporter.filter.GlobalPropertyFilter",
        			"exclusionFilter": true,
        			"patterns": ["%.started","%.database_version","%.mandatory"]
        		}