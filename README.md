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

Whereas tableFilters (above) allow you to specify which tables you want to export data for altogether, Row Filters are what provide the capability to export only a subset of data across your database.  For example, if you wanted to export all of your metadata, but only the patient data for 3 specific patients, you can configure this with appropriate rowFilters.  Specifying a rowFilter follows the format:
```
{
	"@class" : "org.openmrs.contrib.databaseexporter.filter.XYZFilter",
	"filterNumericAttribute": numValue,
	"filterStringAttribute": "strValue",
	"filterListAttribute": ["value1", "value2"]
}
```
You can choose to specify 0-N filters, which will run in the order that they are listed in your configuration file.  Filters are additive, meaning that if Filter 1 filters the patient table to patients 1, 2, and 3, and then Filter 2 filters the patient table to patients 4, 5, and 6, then the resulting export file will contain all 6 of these patients.  If a particular table is affected by at least 1 filter, then it's rows will be limited by the results of the filters.  If a table is not affected by any of the listed filters, then all of it's data will be included.

All filters have a single common attribute, named "exclusionFilter", which is false by default.  By configuring this attribute to true, you can essentially say "include all rows except those that the filter matches".

The list of available filters, and their configuration options, is:

###### General Purpose Filters ######

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
