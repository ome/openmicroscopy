#!/bin/bash
#
# /etc/init.d/omero
# Subsystem file for "omero" server
#

RETVAL=0
prog="omero"

start() {	
	echo -n $"Starting $prog:"
	sudo -u omero /Server/omero/dist/bin/omero admin start
	sudo -u omero /Server/omero/dist/bin/omero web start localhost 8080 &
	RETVAL=$?
	[ "$RETVAL" = 0 ] 
	echo
}

stop() {
	echo -n $"Stopping $prog:"
	sudo -u omero /Server/omero/dist/bin/omero admin stop
	RETVAL=$?
	[ "$RETVAL" = 0 ]
	echo
}

status() {
	echo -n $"Status $prog:"
	sudo -u omero /Server/omero/dist/bin/omero admin status
	RETVAL=$?
	echo
}

diagnostics() {
	echo -n $"Diagnostics $prog:"
	sudo -u omero /Server/omero/dist/bin/omero admin diagnostics
	RETVAL=$?
	echo
}

case "$1" in
	start)
		start
		;;
	stop)
		stop
		;;
	restart)
		stop
		start
		;;
	status)
		status
		RETVAL=$?
		;;
	diagnostics)
		diagnostics
		RETVAL=$?
		;;
	*)	
		echo $"Usage: $0 {start|stop|restart|status|diagnostics}"
		RETVAL=1
esac
exit $RETVAL
