# Jenkins Job Logger Plugin
This plugin writes Jenkins Build information into a CSV formatted log file, such as Job Name, Job Number, Build Status, Build Cause, Build Nodes and a Link to the Console Log.
The csv file is distributed through Jenkins web server. You can directly work on the file, e.g. by opening it in Excel, or hook up another application like Grafana.

## How to build
To create a local version of your plugin, run:
```
gradlew build
```
You find the resulting *.hpi file* in the `build/libs/` directory.

## How to install
Released version of this Plugin are provided as GitHub Releases. Just click on the "release" link on this page and download the .hpi file.

Install the plugin in Jenkins through:
**Manage Jenkins** > **Manage Plugins** > **Advanced** > **Upload Plugin**,
or copy it into your $JENKINS_HOME/plugins directory.

## Usage
If logging is enabled, the plugin will write job status information in
Jenkins's user content directory under the name: **job_logger.csv.**

The Jenkins user content directory can be accessed in:
```
http://yourJenkinsUrl:port/userContent/
```

The format of the CSV log file is as following:
```
JobName;JobNumber;Result;StartDate;EndDate;Cause;Node;ConsoleUrl
```

* Job name: the Jenkins job name
* Job number: the build number of the logged job
* Result: the job result
* Start date: the job's start date
* End date: the job's end date
* Cause: the build cause
* Node(s): slave names used in the job
* Job console URL: the URL path to the job's console

Example:
```
JobName;JobNumber;Result;StartDate;EndDate;Cause;Node;ConsoleUrl
projectA;42;SUCCESS;Wed Mar 07 16:01:01 CET 2018;Wed Mar 07 16:01:08 CET 2018;Push event to branch master;[AWS_Slave];http://my-host/jenkins/job/projectA/42//console
```

## Configuration options:
Configure the plugin in Jenkins global configuration page, in the section **ContiFactory Job Logger Plugin**.
#### Enable logger
If checked, the plugin will start to log job information (default is false).
#### Max log file size
Specify the maximum log file size limit in megabyte (decimal number).
The default value is `1.0` MB.

If the log file exceed this specified size, the content of the log file
will be archived in Jenkins's user content directory under the name:
`job_logger_[current date].csv`.
