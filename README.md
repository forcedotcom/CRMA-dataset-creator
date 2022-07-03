

#  DatasetCreator

DatasetCreator is lightweight RESTFul client implementation of the  CRM Analytics External Data API. This tool is free to use, but it is not officially supported by Salesforce. It has been deliberately developed with no 3rd party jars with the goal of being a lean, reliable  and scalable solution. 
This is a community project that have not been officially tested or documented. Please do not contact Salesforce for support when using this application.


## Log4j2 Issues (CVE-2021-44228 and CVE-2021-45046)

DatasetCreator uses no third party jars and is  therefore free from Log4j issues   


## Downloading DatasetCreator

Download the latest version from [releases](https://github.com/forcedotcom/CRMA-DatasetCreator/releases) and follow the examples below:

## Running DatasetCreator

## Prerequisite

Download and install Java JDK (not JRE) from Zulu Open JDK

* [Zulu Open JDK](https://www.azul.com/downloads/zulu-community/?&architecture=x86-64-bit&package=jdk)

After installation is complete. Different versions of DatasetUtils require different versions of JDK, the latest release API 54 requires JDK 11. Open a console and check that the java version is correct for your DatasetCreator version  by running the following command:


``java -version``


**Windows**: 

Download exectuable jar.

**Mac**: 

Download executable jar.
	 	 

### Command Line

Open a terminal and type in the following command and follow the prompts on the console: 

 
      java -jar dataset-creator-0.54.jar --inputFile  <filename>.csv|.zip --schemaFile  <metafile>.json  --operation Overwrite|Append|Upsert  --u <username@yourorg.com>  --p <passowrd> --dataset <dsname> --label <dslabel> --app <appname>   --endpoint <https://yoursfdcorg> --cSecret <secretkey> --cKey <longclientkey>


--u       : Salesforce.com login

--p       : Salesforce.com password,if omitted you will be prompted

--endpoint:  The Salesforce soap api endpoint (test/prod) Default: https://login.salesforce.com

--dataset : the dataset alias. required 

--label : (Optional) the dataset label.

--app   : (Optional) the app/folder name for the dataset.

--operation   : the operation for load (Overwrite/Upsert/Append/Delete).

--inputFile :  the input csv or gzip file. 

--schemaFile : the dataset meta json file.

--cKey : the client key.

--cSecret : the client secret.



## Usage Example 1: Upload a local csv to a dataset in production

    java -jar dataset-creator-0.54.jar --inputFile  Complaints.csv --schemaFile  Complaints_schema.json  --operation Overwrite  --u twilson@olympus.crma.com  --p AppassWd --dataset Complaints --label Complaints --app SharedApp   --endpoint https://login.salesforce.com --cSecret 844383111F0F755E420B23E1EA0B4AEDB --cKey 3MVG9F0F755E420B23E1EA9BC10B4AEDBmV9T7ZMnfw4C
    
## Usage Example 2: Append a local csv to a dataset

	    java -jar dataset-creator-0.54.jar --inputFile  Complaints.csv --schemaFile  Complaints_schema.json  --operation Append  --u twilson@olympus.crma.com  --p AppassWd --dataset Complaints --label Complaints --app SharedApp   --endpoint https://login.salesforce.com --cSecret 844383111F0F755E420B23E1EA0B4AEDB --cKey 3MVG9F0F755E420B23E1EA9BC10B4AEDBmV9T7ZMnfw4C
	
## Usage Example 3: Upload a local csv to a dataset in sandbox

    java -jar dataset-creator-0.54.jar --inputFile  Complaints.csv --schemaFile  Complaints_schema.json  --operation Overwrite  --u twilson@olympus.crma.com  --p AppassWd --dataset Complaints --label Complaints --app SharedApp   --endpoint https://test.salesforce.com --cSecret 844383111F0F755E420B23E1EA0B4AEDB --cKey 3MVG9F0F755E420B23E1EA9BC10B4AEDBmV9T7ZMnfw4C
	


