#!/bin/sh
if [ -z "$1" ]
   then
	printf "\nUsage : nohup sh startfileMonitor.sh <location of file to be monitored>.\nFor eg. - nohup sh startfileMonitor.sh ./config/test.txt\n"
        exit 1
else
	if [ -f $1 ] 
	   then
                printf "<------ Starting FileMonitor Job ------>\n"
		printf "<------ Please use stop script to stop this job ------>\n"
		java -jar filemonitor.jar $1
        else
            	printf "\nProvided File path("$1") is not valid\n"
                exit 1
        fi
fi
