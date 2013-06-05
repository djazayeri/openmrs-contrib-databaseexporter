openmrs-contrib-databaseexporter
================================

This tool provides a similar capability to mysqldump, but with OpenMRS-aware configuration settings aiming to
extract a database out of a production environment for development, testing, or demonstration. To accomplish this, 
it has two primary features:

* Export a subset of the database.  As production databases grow in size, running mysqldump and then sourcing this into your development environment becomes very inefficient.  For databases containing hundreds of thousands of patients, and millions of obs, such a process can take hours (or days) to perform.  This tool aims to allow for exporting a subset of this data, ensuring that broad representation is accounted for across age ranges, program enrollments, encounters, relationships, etc, but which can be sourced in a matter of minutes.

* Transform aspects of the database during the export process.  Most commonly, this involves de-identification of data, but can involve any modification, removal, or addition of data that is needed during the export process.  Examples include obfuscation of data such as person names and addresses, associating random locations with encounter or obs data,  or removing existing users and replacing them with a set of test users.

Usage
------

* Clone this project to your workspace, and change into the project root directory
* Build an executable jar by running "mvn clean package"
* Copy the artifact at "./target/databaseexporter-XYZ-jar-with-dependencies.jar" into the directory of your choice
* Create a configuration file that specifies the parameters of the export.  See configuration section for details.
* Run the executable jar, specifying the location of the configuration file, as follows:

```$java -jar databaseexporter-XYZ-jar-with-dependencies.jar /path/to/configuration/file/created/above```

Configuration
--------------

In order to run the databaseexporter, you must specify an appropriate configuration file.  This configuration file is expected to be in JSON format, and contains the following settings:

##### sourceDatabaseCredentials #####

This is where you specify the details of how the tool should connect to the source database from which you would like to export data.  The supported attributes are:

* driver:  (optional), defaults to "com.mysql.jdbc.Driver"
* url: (required), should be the JDBC connection string to connect to your database
* user:  (required), an existing database user that has sufficient privileges to run the operation
* password:  (required), the password for this user

Example:
```
	"sourceDatabaseCredentials": {
		"url": "jdbc:mysql://localhost:3306/openmrs?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8",
		"user": "openmrs",
		"password": "openmrs"
	}
```

##### targetLocation #####

This allows you to specify the location and name of the file that the tool produces.

Example:
```
	"targetLocation": "/home/openmrs/openmrs_export.sql"
```

##### tableFilter #####

This allows you to specify which tables are included and which are excluded from the export.  This allows you fine-grained control over which tables you wish to create and which you want to export data.  

The supported attributes are:

* includeSchema:  (optional), list of table names for which a "drop table..." and "create table..." statement will be written in the export file.  By default, if not specified, all tables will be included.
* excludeSchema: (optional), list of table names for which a "drop table..." and "create table..." statement will not be written in the export file.  By default, if not specified, no tables will be included.
* includeData:  (optional), list of table names for which contents will be extracted into the export file.  By default, if not specified, all tables will be included.
* excludeData: (optional), list of table names for which contents will be extracted into the export file.  By default, if not specified, no tables will be included.

The use of the "*" wildcard is supported within all of the attributes.

Example:
```
	"tableFilter": {
		"includeSchema": ["*"],
		"excludeSchema": ["temp*],
		"includeData": ["*"],
		"excludeData": ["htmlformentry*","sync*","concept_word"]
	}
```

##### rowFilters #####

Whereas tableFilters (above) allow you to specify which tables you want to export data for altogether, Row Filters are what provide the capability to export only a subset of data across your database.  For example, if you wanted to export all of your metadata, but only the patient data for 3 specific patients, you can configure this with appropriate rowFilters.  Specifying rowFilters follows the format:
```
"rowFilters": [
	{
		"@class" : "org.openmrs.contrib.databaseexporter.filter.Filter1",
		"numericAttribute": numValue,
		"stringAttribute": "strValue",
		"listAttribute": ["value1", "value2"]
	},
	{
		"@class" : "org.openmrs.contrib.databaseexporter.filter.Filter2",
		"filterNumericAttribute": numValue,
		"filterStringAttribute": "strValue",
		"filterListAttribute": ["value1", "value2"]
	}
	...
]
```
You can choose to specify 0-N filters, which will run in the order that they are listed in your configuration file.  Filters are additive, meaning that if Filter 1 filters the patient table to patients 1, 2, and 3, and then Filter 2 filters the patient table to patients 4, 5, and 6, then the resulting export file will contain all 6 of these patients.  If a particular table is affected by at least 1 filter, then it's rows will be limited by the results of the filters.  If a table is not affected by any of the listed filters, then all of it's data will be included.

All filters have a single common attribute, named "exclusionFilter", which is false by default.  By configuring this attribute to true, you can essentially say "include all rows except those that the filter matches".

The list of available filters, and their configuration options, is:

###### General Purpose Filters ######

These filters can be applied broadly to constrain values in a wide array of tables to suit custom needs.

**DiscreteValueFilter**

Limits the rows in a particular table based on whether or not a column value matches one or more specified values.
	
Options:
* **tableName** (required):  The table to filter
* **columnName** (required): The column for which you want to apply the constraint
* **values** (required):  A list of values that the column must match to be included
* **exclusionFilter** (optional, defaults to false):  See above

Example:  Include only certain properties from the user_property table
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.filter.DiscreteValueFilter",
	"tableName": "user_property",
	"columnName": "property",
	"values": ["loginAttempts","emailAddress"]
}
```

**PatternValueFilter**

Limits the rows in a particular table based on whether or not a column value matches one or more specified patterns.

Options:
* **tableName** (required):  The table to filter
* **columnName** (required): The column for which you want to apply the constraint
* **patterns** (required):  A list of patterns that the column must match to be included.  This supports the database "like" syntax (%).
* **exclusionFilter** (optional, defaults to false):  See above

Example:  Exclude global properties for certain modules that are no longer in use
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.filter.PatternValueFilter",
	"tableName": "global_property",
	"columnName": "property",
	"values": ["htmlformentry%","sync%"],
	"exclusionFilter": true
}
```

###### Patient Data Filters ######

These filters are specifically designed to limit the patients that are exported (along with any of their associated data, including encounters, obs, program enrollments, drug orders, etc)

**PatientIdFilter**

Include patient data for only those patients with the specified patientIds in the export

Options:
* **patientIds** (required):  The list of patientIds to include
* **exclusionFilter** (optional, defaults to false):  See above

Example:  Export data for only 5 specific patients for testing
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.filter.PatientIdFilter",
	"patientIds": [111,222,333,444,555]
}
```

**PatientsHavingAgeFilter**

Include representative data for up to the specified number of patients in each of the specified age ranges.

Options:
* **numberPerAgeRange** (optional, defaults to 10):  For each listed age range, ensure at least this number of patients are included, if possible
* **ageRanges** (required): List of age ranges that we want to ensure have adequate representation

Example:  Ensure at least 100 patients exist who are between 0-2, 3-10, 11-15, 16-30, 31-60, and 60+ years old
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.filter.PatientsHavingAgeFilter",
	"numberPerAgeRange": 100,
	"ageRanges": [
		{"maxAge": 2},
		{"minAge": 3, "maxAge": 10},
		{"minAge": 11, "maxAge": 15},
		{"minAge": 16, "maxAge": 30},
		{"minAge": 31, "maxAge": 60},
		{"minAge": 61}
	]
}
```

**PatientsHavingEncounterFilter**

Include representative encounter data that includes at least the specified number of patients who have encounters by form and encounter type

Options:
* **numberPerType** (optional, defaults to 10):  For each listed encounter type (or all if none are specified), include this number of patients who have encounters of this type
* **numberPerForm** (optional, defaults to 10):  For each listed form (or allif none are specified),  include this number of patients who have encounters for this form
* **includeRetired** (optional, defaults to false): Indicate whether retired forms and encounter types should be included, if none are specifically listed
* **limitToTypes** (optional):  if specified, will limit the filter to only those encounter types
* **limitToForms** (optional):  if specified, will limit the filter to only those forms
* **order** (optional, options are RANDOM, DATE_ASC, DATE_DESC, NUM_OBS_DESC, defaults to RANDOM).  Determines which patients are selected for the given criteria - based on encounter date created, randomly, or favoring those with the most obs

Example:  Ensure at least 5 patients with each non-retired encounter type are included, and ensure these are the encounters that have the most number of Obs associated with them for each type
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.filter.PatientsHavingEncounterFilter",
	"numberPerType": 5,
	"numberPerForm": 0,
	"order": "NUM_OBS_DESC"
}
```

**PatientsHavingProgramEnrollmentFilter**

Include representative program enrollment data that includes at least the specified number of patients who have enrollments in the specified programs with the specified status

Options:
* **numberActivePerProgram** (optional, defaults to 10):  For each listed program (or all if none are specified), include this number of patients who are currently active in that program
* **numberCompletedPerProgram** (optional, defaults to 10):  For each listed program (or all if none are specified), include this number of patients who are currently completed in that program
* **includeRetired** (optional, defaults to false): Indicate whether retired programs should be included, if none are specifically listed
* **limitToPrograms** (optional, defaults to false): if specified, will limit the filter to only those programs

Example:  Ensure at least 20 patients with active enrollments and and 50 patients with completed enrollments are included for the programs with programId 1 and 3
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.filter.PatientsHavingProgramEnrollmentFilter",
	"numberActivePerProgram": 20,
	"numberCompletedPerProgram": 50,
	"limitToPrograms": [1,3]
}
```

**PatientsHavingRelationshipFilter**

Include representative relationship data that includes at least the specified number of patients who have relationships in each of the specified type

Options:
* **numberPerType** (optional, defaults to 10):  For each listed relationshipType (or all if none are specified), include this number of patients who have this relationship
* **includeRetired** (optional, defaults to false): Indicate whether retired relationship types should be included, if none are specifically listed
* **limitToTypes** (optional, defaults to false): if specified, will limit the filter to only those relationship types

Example:  Ensure at least 5 patients with each relationship type, including retired types, is included in the export
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.filter.PatientsHavingRelationshipFilter",
	"numberPerType": 5,
	"includeRetired": true
}
```

**PatientsHavingIdentifierFilter**

Include representative identifier data that includes at least the specified number of patients who have identifiers in each of the specified type

Options:
* **numberPerType** (optional, defaults to 10):  For each listed identifierType (or all if none are specified), include this number of patients who have this identifier type
* **includeRetired** (optional, defaults to false): Indicate whether retired identifier types should be included, if none are specifically listed
* **limitToTypes** (optional, defaults to false): if specified, will limit the filter to only those identifier types

Example:  Ensure at least 100 patients with each non-retired identifier type is included in the export
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.filter.PatientsHavingIdentifierFilter",
	"numberPerType": 100,
	"includeRetired": false
}
```

**PatientsHavingVitalStatusFilter**

Include representative data for both alive and dead patients

Options:
* **numberAlive** (optional, defaults to 10):  Ensure at least this number of alive patients is included
* **numberDead** (optional, defaults to false): Ensure at least this number of dead patients is included

Example:  Ensure at least 20 alive patients and 5 dead patients are included in the export
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.filter.PatientsHavingVitalStatusFilter",
	"numberAlive": 20,
	"numberDead": 5
}
```

##### rowTransforms #####

Whereas row filters determine what rows of data should be exported, row transforms are able to transform the actual content of each row prior to exporting.  For example, if you wanted to replace each patient name with a randomly-selected generic alternative, you would do this by configuring the appropriate transform.  Specifying rowTransforms follows the format:
```
"rowTransforms": [
	{
		"@class" : "org.openmrs.contrib.databaseexporter.transform.Transform1",
		"numericAttribute": numValue,
		"stringAttribute": "strValue",
		"listAttribute": ["value1", "value2"]
	},
	{
		"@class" : "org.openmrs.contrib.databaseexporter.transform.Transform2",
		"numericAttribute": numValue,
		"stringAttribute": "strValue",
		"listAttribute": ["value1", "value2"]
	}
	...
]
```

* Row Transforms run after Row Filters, and only operate upon the rows that have passed through the filter criteria
* You can choose to specify 0-N transforms, which will run in the order that they are listed in your configuration file.
* Transforms can exclude rows altogether, so even if a rowFilter chooses to include a particular row, a rowTransform may later decide to exclude it.
* Transforms can add completely new rows
* Many transforms support "expressions".  Wne you see the word "expression" in the description below, this means that it supports free text, as well as replacement values for any other value within the row in the format "Hello ${another_column_name_in_the_row}"

The list of available filters, and their configuration options, is as follows:

**SimpleReplacementTransform**

Replaces the value in all of the specified table/column entries with the configured replacement value

Options:
* **tableAndColumnList** (required):  This should be a list of table/column pairs in the format ["table1.column1","table2.column2"]
* **replacement** (required): This should be an expression (see above for definition) that defines the replacement value

Example:  Replace the obs.value_text column before exporting to ensure no sensitive data is exported that might be contained there
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.transform.SimpleReplacementTransform",
	"tableAndColumnList": ["obs.value_text"],
	"replacement": "Value replaced during de-identification ${obs_id}"
}
```






PersonAddressTransform

	private String addressPath = CONFIG_PACKAGE + "addresses.config";
	private String addressSeparator = ",";

PersonNameTransform

	private boolean reproducible = false; // If true, this will randomize in a reproducible fashion
	private String maleNamePath = CONFIG_PACKAGE + "maleNames.config";
	private String femaleNamePath = CONFIG_PACKAGE + "femaleNames.config";
	private String familyNamePath = CONFIG_PACKAGE + "familyNames.config";

IdentifierTransform

	private String identifierReplacement = "${patient_identifier_id}";

LocationTransform

	private String addressPath = CONFIG_PACKAGE + "addresses.config";
	private String addressSeparator = ",";
	private String nameReplacement = "${address1} Health Center";
	private String descriptionReplacement = "A de-identified health center located at ${address1}";
	private boolean scrambleLocationsInData = true;
	private List<Integer> keepOnlyLocations;

	{
		"@class": "org.openmrs.contrib.databaseexporter.transform.LocationTransform",
		"keepOnlyLocations": [1,25,26,27,28.29]
	}

RwandaAddressHierarchyTransform

	private List<String> hierarchyLevels = Arrays.asList("country","state_province","county_district","city_village","address3","address1");

	{
		"@class": "org.openmrs.contrib.databaseexporter.transform.RwandaAddressHierarchyTransform",
	}

ScrambleStatesInWorkflowTransform

	private Integer workflowToScramble;
	private List<Integer> possibleStates;

	{
		"@class": "org.openmrs.contrib.databaseexporter.transform.ScrambleStatesInWorkflowTransform",
		"workflowToScramble": 9,
		"possibleStates": [247,248,249]
	}

UserTransform

	private List<Integer> usersToKeep;
	private String systemIdReplacement;
	private String usernameReplacement;
	private String passwordReplacement;

	{
		"usersToKeep": [1,24839,12],
		"@class": "org.openmrs.contrib.databaseexporter.transform.UserTransform",
		"systemIdReplacement": "${user_id}",
		"usernameReplacement": "user${user_id}",
		"passwordReplacement": "Test1234"
	}
