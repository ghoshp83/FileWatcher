# FileWatcher
It is a utility which infinitely watches any change inside a file and report it to the calling program

Steps to Install:

1.	Please install Java package 1.7.x.x in production/test environment.
2.	Set JAVA_HOME and PATH according to java installation directory. Verify using java –version command. It should show java version as 1.7.x.x
3.	Take ‘FileWatcher.tar’ from the source environment to production/test environment.
4.	Create a directory named ‘FileWatcher’ and place ‘FileWatcher.tar’ inside it.
5.	Unzip ‘FileWatcher.tar’ using below command –
tar –xvf FileWatcher.tar
6.	Remove FileWatcher.tar from FileWatcher directory.
7.	Create config directory inside FileWatcher directory and place your file inside config directory. This is a default way to monitor file from config directory. One can place files in any place, only thing is that absolute path of the file needs to be provided in command line argument during execution.
8.	Create logs directory inside FileWatcher directory. This is the location for STDOUT and STDERR log files.

Steps to Execution:

1.	Use the below command to start the execution(inside FileWatcher folder) –

nohup sh startfileMonitor.sh <location of file to be monitored> >> <location of stdout log> 2>><location of stderr log> &

For example – 

nohup sh startfileMonitor.sh ./config/test.txt >> ./logs/monitor.log 2>>./logs/mErr.log &

2.	Use the below command to stop the execution(inside FileWatcher folder) –

sh stopfileMonitor.sh

3.	Use the below command to see the stdout/stderr log file – 

tail –f <<location of stdout_log_file>>
tail –f <<location of stderr_log_file>>

4.	A file named fileUpdateDetails.txt will be created in FileWatcher directory. This will contain the details of change in the input file. This file can be pursued by other applications for analysis of the changes happened to the input file.

Steps to Integration:
	
1.	This application is an infinite running standalone application. The input to this application is a textual file (which needs to be monitored for infinite time) and the output of this application is a text file which will store the changes made on the input file.
2.	Other application can use this application jar (filemonitor.jar) in their execution class-path and call the application class with the input file argument. 
3.	One can use the steps of execution (as mentioned above) to run this application for the first time and use the output file for their application use. This will be applicable in Unix/Linux environment.
4.	Other application can use the below java code to use the output file for further analysis and use in their application as per business requirement. 

Reusability Factor:
	
1.	This application makes use of java.nio.file.WatchService and difflib.DiffUtils open-source packages and customizes them to watch a particular file infinitely, capture any changes and stores the changes in a textual format.
2.	The output text file can be leveraged by any other application according to their dynamic business need.

