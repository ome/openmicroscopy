#!/bin/sh

export BASE=/home/josh/workspace/OME-Generation
export CLASSPATH=target

rmic -d target org.ome.srv.net.rmi.RMIAdministrationFacade
java  -Djava.rmi.server.codebase=file://$BASE/target/ -Djava.security.policy=$BASE/policy  \
	-agentlib:hprof=cpu=samples,interval=20,depth=3 \
	org.ome.srv.net.rmi.RMIServer

exit
#=cpu=times,thread=y \
