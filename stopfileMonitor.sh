#!/bin/sh
echo "<------ Killing the FileMonitor Process ------>"
ps axf | grep filemonitor.jar | grep -v grep | awk '{print "kill -15 " $1}' | sh
rm -rf configcopy/
rm -rf ./logs/*.log
echo "<------ Killed the FileMonitor Process ------>"
