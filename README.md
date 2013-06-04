openmrs-contrib-databaseexporter
================================

This tool aims to provide a similar capability to mysqldump, but with OpenMRS-aware configuration settings aiming to
extract a database out of a production environment for development, testing, or demonstration.
To accomplish this, it has two primary features:

* Export a subset of the database.  As production databases grow in size, the approach of running mysqldump and then
sourcing this into your development environment becomes very inefficient.  For databases containing hundreds of
thousands of patients, and millions of obs, such a process can take hours (or days) to perform.  This tool aims to
allow for exporting a subset of this data, ensuring that broad representation is accounted for across age ranges,
program enrollments, encounters, relationships, etc, but which can be sourced in a matter of minutes.

* Transform aspects of the database during the export process.  Most commonly, this involves de-identification of data.
Depending on the type of environment being developed, this de-identification can be more or less comprehensive.
Options include replacement of data such as person names and addresses, obfuscation of free-text data
such as text observations, and scrambling of data such as what patient data is associated with what locations.
Although de-identification is the most common use case here, it is not the only one.  Transforms may be used to set
up additional user accounts for training, to reset passwords, to configure global properties, etc.

Usage
------

* Build this project by cloning this repository, navigating to the root directory, and running "mvn clean package".
This will produce an executable jar in the "target" directory: "databaseexporter-XYZ-jar-with-dependencies.jar".

* Create a configuration file that specifies the parameters of the export.  See configuration section for details.

* Navigate into the directory that contains executable jar produced above and run the command:

	$ java -jar databaseexporter-XYZ-jar-with-dependencies.jar /path/to/configuration/file/created/above

Configuration
--------------

TBD


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
			"@class" : "org.openmrs.contrib.databaseexporter.filter.DiscreteValueFilter",
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
			"@class" : "org.openmrs.contrib.databaseexporter.filter.PatternValueFilter",
			"tableName": "global_property",
			"columnName": "property",
			"patterns": ["htmlform%"]
		},
		{
			"@class" : "org.openmrs.contrib.databaseexporter.filter.PatternValueFilter",
			"exclusionFilter": true,
			"tableName": "global_property",
			"columnName": "property",
			"patterns": ["%.started","%.database_version","%.mandatory"]
		}


		"rowTransforms": [
			{
				"@class" : "org.openmrs.contrib.databaseexporter.transform.SimpleReplacementTransform",
				"tableAndColumnList": ["obs.value_text"],
				"replacement": "Value replaced during de-identification ${obs_id}"
			}
		]

				{
        			"usersToKeep": [1,24839,12],
        			"@class": "org.openmrs.contrib.databaseexporter.transform.UserTransform",
        			"systemIdReplacement": "${user_id}",
        			"usernameReplacement": "user${user_id}",
        			"passwordReplacement": "Test1234"
        		}

        				{
                			"@class": "org.openmrs.contrib.databaseexporter.transform.ScrambleStatesInWorkflowTransform",
                			"workflowToScramble": 9,
                			"possibleStates": [247,248,249]
                		}

                				{
                        			"@class": "org.openmrs.contrib.databaseexporter.transform.RwandaAddressHierarchyTransform",
                        			"hierarchyLevels": ["country","state_province","county_district","city_village","address3","address1"]
                        		}
