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

#### sourceDatabaseCredentials ####

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

#### targetLocation ####

This allows you to specify the directory in which the tool will write it's output.  When run, two files will be produced in this target directory:  "export_yyyy_MM_dd_HH_ss.log" and "export_yyyy_MM_dd_HH_ss.sql".

Example:
```
	"targetLocation": "/home/openmrs/exports"
```

#### tableFilter ####

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
		"excludeSchema": ["temp*"],
		"includeData": ["*"],
		"excludeData": ["hl7_in_*","person_merge_log","concept_word"]
	}
```

#### rowFilters ####

Whereas tableFilters (above) allow you to specify which tables you want to export data for altogether, Row Filters are what provide the capability to export only a subset of data across your database.  You can specify 0-N filters, which will run in the order that they are listed in your configuration file.  Filters are additive, meaning that if 2 Filters operate against the same type of data (Patients, for example), then the results of these two filters will be combined and the union will be included in the export.  If a particular table is affected by at least 1 filter, then it's rows will be limited by the results of the filters.  If a table is not affected by any of the listed filters, then all of it's data will be included.

When a particular type of data is filtered, this means that only a subset of the rows in that table will be exported.  Because of this, any table that has a foreign key relationship with this table will be affected.  In this case, one of two things will happen:

1. If the table represents data that depends upon the referenced table to retain it's meaning or purpose, then this table will be filtered to match the parent table.  For example, if you filter patients, this will automatically filter patient identifiers, such that only those patient identifiers associated with patients in the export will be included.

2. If the table represents data that does not derive it's meaning from it's relationship with the referenced table, then the foreign key will be replaced.  For example, if you filter users, this will not automatically filter all data that references these users as their "creator", "changed_by", "retired_by", or "voided_by".  Rather, a randomly selected value from the group of valid, filtered users will be chosen and used as a replacement.

Currently the system supports 3 different types of Row Filters:

#### Patient Filter ####

A patient filter limits the patients that are exported based on a series of configurable queries that you can specify.  These queries include the following:

**PatientIdQuery**

Include patient data for only those patients with the specified patientIds in the export

Options:
* **ids** (required):  The list of patientIds to include

Example:  Export data for only 5 specific patients for testing
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.query.PatientIdQuery",
	"ids": [111,222,333,444,555]
}
```

**PatientAgeQuery**

Include representative data for up to the specified number of patients in each of the specified age ranges.

Options:
* **numberPerAgeRange** (optional, defaults to 10):  For each listed age range, ensure at least this number of patients are included, if possible
* **ageRanges** (required): List of age ranges that we want to ensure have adequate representation

Example:  Ensure at least 100 patients exist who are between 0-2, 3-10, 11-15, 16-30, 31-60, and 60+ years old
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.query.PatientAgeQuery",
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

**PatientEncounterQuery**

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
	"@class" : "org.openmrs.contrib.databaseexporter.query.PatientEncounterQuery",
	"numberPerType": 5,
	"numberPerForm": 0,
	"order": "NUM_OBS_DESC"
}
```

**PatientProgramEnrollmentQuery**

Include representative program enrollment data that includes at least the specified number of patients who have enrollments in the specified programs with the specified status

Options:
* **numberActivePerProgram** (optional, defaults to 10):  For each listed program (or all if none are specified), include this number of patients who are currently active in that program
* **numberCompletedPerProgram** (optional, defaults to 10):  For each listed program (or all if none are specified), include this number of patients who are currently completed in that program
* **includeRetired** (optional, defaults to false): Indicate whether retired programs should be included, if none are specifically listed
* **limitToPrograms** (optional, defaults to false): if specified, will limit the filter to only those programs

Example:  Ensure at least 20 patients with active enrollments and and 50 patients with completed enrollments are included for the programs with programId 1 and 3
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.query.PatientProgramEnrollmentQuery",
	"numberActivePerProgram": 20,
	"numberCompletedPerProgram": 50,
	"limitToPrograms": [1,3]
}
```

**PatientRelationshipQuery**

Include representative relationship data that includes at least the specified number of patients who have relationships in each of the specified type

Options:
* **numberPerType** (optional, defaults to 10):  For each listed relationshipType (or all if none are specified), include this number of patients who have this relationship
* **includeRetired** (optional, defaults to false): Indicate whether retired relationship types should be included, if none are specifically listed
* **limitToTypes** (optional, defaults to false): if specified, will limit the filter to only those relationship types

Example:  Ensure at least 5 patients with each relationship type, including retired types, is included in the export
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.query.PatientRelationshipQuery",
	"numberPerType": 5,
	"includeRetired": true
}
```

**PatientIdentifierQuery**

Include representative identifier data that includes at least the specified number of patients who have identifiers in each of the specified type

Options:
* **numberPerType** (optional, defaults to 10):  For each listed identifierType (or all if none are specified), include this number of patients who have this identifier type
* **includeRetired** (optional, defaults to false): Indicate whether retired identifier types should be included, if none are specifically listed
* **limitToTypes** (optional, defaults to false): if specified, will limit the filter to only those identifier types

Example:  Ensure at least 100 patients with each non-retired identifier type is included in the export
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.query.PatientIdentifierQuery",
	"numberPerType": 100,
	"includeRetired": false
}
```

**PatientVitalStatusQuery**

Include representative data for both alive and dead patients

Options:
* **numberAlive** (optional, defaults to 10):  Ensure at least this number of alive patients is included
* **numberDead** (optional, defaults to false): Ensure at least this number of dead patients is included

Example:  Ensure at least 20 alive patients and 5 dead patients are included in the export
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.query.PatientVitalStatusQuery",
	"numberAlive": 20,
	"numberDead": 5
}
```

#### User Filter ####

A user filter limits the users that are exported based on a series of configurable queries that you can specify.  These queries include the following:

**UserIdentificationQuery**

Include only those users with the given usernames (note: admin and daemon will always be included)

Options:
* **userNames** (required):  The list of users to include

Example:  Export only my account
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.query.UserIdentificationQuery",
	"userNames": ["mseaton"]
}
```

#### Provider Filter ####

A provider filter limits the providers that are exported based on a series of configurable queries that you can specify.  These queries include the following:

**ProviderIdQuery**

Include only those providers with the given primary key provider ids

Options:
* **ids** (required):  The list of provider ids to include

Example:  Export just 5 sample providers
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.query.ProviderIdQuery",
	"ids": [1,2,3,4,5]
}
```

#### rowTransforms ####

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
* Many transforms support "expressions".  When you see the word "expression" in the description below, this means that it supports free text, as well as replacement values for any other value within the row in the format "Hello ${another_column_name_in_the_row}"

The list of available filters, and their configuration options, is as follows:

**SimpleReplacementTransform**

Allows you to specify a collection of tableName.columnName -> replacement pairs, in order to apply a common replacement expression across all values in a particular column

Options:
* **replacements** (required):  This should be a map from tableName.columnName to replacement expression

Example:  Replace the obs.value_text column before exporting to ensure no sensitive data is exported that might be contained there
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.transform.SimpleReplacementTransform",
	"replacements": {
           "obs.value_text": "Value replaced during de-identification ${obs_id}"
        }
}
```

**GlobalPropertyTransform**

Allows you to specify a simple expression replacement for 1-N global properties

Options:
* **replacements** (required):  This should be a map from propertyName to propertyValue

Example:  Change the scheduler password for security reasons, and change any server specific settings
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.transform.GlobalPropertyTransform",
	"replacements": {
		"scheduler.password": "Admin123",
		"metadatasharing.urlPrefix": "http://localhost:8080/openmrs"
        }
}
```

**GlobalPropertyTransform**

Allows you to specify a simple expression replacement for 1-N global properties

Options:
* **replacements** (required):  This should be a map from propertyName to propertyValue

Example:  Change the scheduler password for security reasons, and change any server specific settings
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.transform.GlobalPropertyTransform",
	"replacements": {
		"scheduler.password": "Admin123",
		"metadatasharing.urlPrefix": "http://localhost:8080/openmrs"
        }
}
```

**IdentifierTransform**

Allows you to transform all identifiers of one or more types with a particular identifier generator.  The format for this is as follows:

Options:
* **defaultGenerator** (optional):  If specified, all identifiers not configured with a specific generator will use this on
* **replacementGenerators** (optional):  If specified, is a Map from Identifier Type name to Generator configuration

Example:  Configure all identifiers to be replaced by default by their primary key in the patient_identifier table for de-identification purposes.  Configure the "OpenMRS ID" to use a Verhoeff-based generator to produce a random replacement that meets validation requirements.
```
{
	"@class": "org.openmrs.contrib.databaseexporter.transform.IdentifierTransform",
	"defaultGenerator": {
		"@class" : "org.openmrs.contrib.databaseexporter.generator.SimpleReplacementGenerator",
		"replacement": "${patient_identifier_id}"
	},
	"replacementGenerators": {
		"OpenMRS ID": {
			"@class" : "org.openmrs.contrib.databaseexporter.generator.VerhoeffGenerator",
			"firstIdentifierBase": "90100000",
			"baseCharacterSet": "0123456789"
		}
	}
}
```

The following Generators are available:

** SimpleReplacementGenerator: **

Replaces the identifier with the configured expression replacment value

Options:
* **replacement** (required):  The expression to use for the replacement

Example:
```
"defaultGenerator": {
	"@class" : "org.openmrs.contrib.databaseexporter.generator.SimpleReplacementGenerator",
	"replacement": "${patient_identifier_id}"
}
```

** SequentialGenerator: **

Replaces the identifier with a sequentially allocated value in the configured base

Options:
* **baseCharacterSet** (required):  The character set to use
* **firstIdentifierBase** (optional):  The first identifierBase to start at
* **prefix** (optional):  Any prefix to put on the identifier
* **suffix** (optional):  Any suffix to put on the identifier

Example:  Generate a sequential numeric identifier, starting at 5001
```
"defaultGenerator": {
	"@class" : "org.openmrs.contrib.databaseexporter.generator.SequentialGenerator",
	"baseCharacterSet": "0123456789",
	"firstIdentifierBase": "5001"
}
```

** VerhoeffGenerator: **

Extension of the SequentialGenerator that appends a Verhoeff-based checkdigit, with a dash separator, to the end of the identifier

Options:
* See Sequential Generator

Note:  You should make sure you configure this generator use use a baseCharacterSet of "0123456789"

** LuhnGenerator: **

Extension of the SequentialGenerator that appends a Luhn-based checkdigit, with a configurable separator, to the end of the identifier

Options:
* See Sequential Generator
* **checkDigitSeparator** (optional):  If specified, will use this to divide the undecorated identifier from the check-digit.  Defaults to ""


**LocationTransform**

For each location in the system, this will automatically choose a random address from the configured address resource, and replace the location address components with it.  It also allows you to replace the name and description of the locations with configured expressions and optionally scramble what locations are associated with what patients in the underlying data

Options:
* **nameReplacement** (optional):  An expression for replacing the name
* **descriptionReplacement** (optional):  An expression for replacing the description
* **addressPath** (optional): The location of a resource file containing replacement addresses.  There is a resource that is used by default if no custom file specified.
* **addressSeparator** (optional): The character used to separate address fields in the address replacement resource.  Defaults to ","
* **scrambleLocationsInData** (optional): Defaults to true.  If true, this will randomize the locations that are associated with all patient data in the system, to ensure that location patterns cannot be used to identify patient populations in the exported dataset


Example:  De-identify the names and addresses of all Locations in the system, but do not scramble how these locations are distributed across the exported patients
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.transform.LocationTransform",
	"nameReplacement": "${address1} Health Center",
	"descriptionReplacement": "A de-identified health center located at ${address1}",
	"scrambleLocationsInData:: false
}
```

**PersonAddressTransform**

For each person address in the system, this will automatically choose a random replacement address from the configured address resource

Options:
* **addressPath** (optional): The location of a resource file containing replacement addresses.  There is a resource that is used by default if no custom file specified.
* **addressSeparator** (optional): The character used to separate address fields in the address replacement resource.  Defaults to ","

Example:  De-identify all person addresses with the default address resource
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.transform.PersonAddressTransform"
}
```

**RwandaAddressHierarchyTransform**

Update the address hierarchy entry table used by the rwandaaddresshierarchy module with the values from the configured address replacement file

Options:
* See PersonAddressTransform for valid options

Example:  Set up the address hierarchy entry tables with the default values from the included address replacement configuration
{
	"@class": "org.openmrs.contrib.databaseexporter.transform.RwandaAddressHierarchyTransform"
}
```

**PersonNameTransform**

For each person name in the system, this will automatically choose a random replacement from the configured name resources

Options:
* **maleNamePath** (optional): The location of a resource file containing replacement male names.  There is a resource that is used by default if no custom file specified.
* **femaleNamePath** (optional): The location of a resource file containing replacement female names.  There is a resource that is used by default if no custom file specified.
* **familyNamePath** (optional): The location of a resource file containing replacement family names.  There is a resource that is used by default if no custom file specified.
* **reproducible** (optional): Defaults to false.  If true, this will pick replacement names in a way that is consistent every time this tool is run on the same database.  The names will still be de-identified, but in a predictable way.

Resource files are expected to be a text file that contains one name per line

Example:  De-identify all person names with default given names, and user-specified family names
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.transform.PersonNameTransform",
	"familyNamePath": "/home/openmrs/export/customizedFamilyNames.txt"
}
```

**ProviderTransform**

Provides exactly the same functionality as the PersonName transform, except that it replaces any non-null provider name with a random name taken from the configured name resources

Options:
* See PersonNameTransform for valid options


Example:  De-indentify each provider name with defaults from the provided configuration files
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.transform.ProviderTransform"
}
```

**PersonAttributeTransform**

Allows you to specify a List of valid replacements for one or more Person Attribute types, which will be randomly applied to all exported person attributes of those types

Options:
* **replacements** (required):  This should be a map from attribute type name to a List of possible replacement values

Example:  De-indentify a variety of person attribute values as configured below:
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.transform.PersonAttributeTransform",
	"replacements": {
		"Race": [""],
		"Birthplace": ["MA","NY","NJ","FL"],
		"Boarding School": [""],
		"Citizenship": ["USA"],
		"Father's name": ["Adams","Banks","Ford","Green","Jones","King","Murphy","Price","Smith","Thomas"],
		"Mother's Name": ["Bell","Carroll","Ellis","Faye","Kay","Madison","Pearl","Riley","Shelly","Taylor"],
		"Health District": ["Middlesex","Suffolk","New York","Burlington","Monmouth","Union","Palm Beach"]
	}
}
```

**ScrambleStatesInWorkflowTransform**

For a particular program workflow, this transform will randomize what state a user is associated with in that workflow.

Options:
* **workflowToScramble** (required):  This is the primary key id for the program_workflow to scramble for patients
* **possibleStates**: (optional): A List of valid states to use to scramble the workflow.  If not specified, all non-retired states for that workflow will be used

Example:  For the HIV Program Treatment Group workflow, scramble these values to that patients cannot be identified based on their assigned treatment group
```
{
	"@class": "org.openmrs.contrib.databaseexporter.transform.ScrambleStatesInWorkflowTransform",
	"workflowToScramble": 9,
	"possibleStates": [247,248,249]
}
```

**UserTransform**

Allows for setting the systemId, username, and password to particular expression replacement values for every user.  For any user configured in the usernamesToPreserve property, passwords will also be replaced, but systemId and username will be left intact.

Options:
* **systemIdReplacement** (optional):  An expression used to replace each systemId
* **usernameReplacement**: (optional): An expression used to replace each username
* **passwordReplacement**: (optional): An expression used to replace each password
* **usernamesToPreserve**: (optional): An list of usernames for which the systemId and username replacements should not apply

Example:  De-identify all users except for me, and replace everyone's password with a known value
```
{
	"@class": "org.openmrs.contrib.databaseexporter.transform.UserTransform",
	"systemIdReplacement": "${userId}",
	"usernameReplacement": "user${userId}",
	"passwordReplacement": "Test1234",
	"usernamesToPreserve": ["mseaton"]
}
```
