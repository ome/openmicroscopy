#!/bin/sh

export OME=/home/josh/code/ome3
export BASE=$OME/components/jars/srv
export CLASSPATH=$BASE/target/classes:$BASE/../model/target/classes

rmic -d $BASE/target/classes \
	org.ome.srv.net.rmi.RMIServiceFactory \
	org.ome.srv.net.rmi.RMIAdministrationFacade \
	org.ome.srv.net.rmi.RMIContainerFacade 

java  \
	-Djava.rmi.server.codebase="file://$BASE/target/classes/ file://$BASE/../model/target/ome3-model-1.0.jar" \
	-Djava.security.policy=$OME/policy  \
	org.ome.srv.net.rmi.RMIServer

exit
#=cpu=times,thread=y \
#-agentlib:hprof=cpu=samples,interval=20,depth=3 \
