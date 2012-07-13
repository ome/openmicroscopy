#!/bin/bash
#
# /etc/init.d/omero
# Subsystem file for "omero" server
#
### BEGIN INIT INFO
# Provides:             omero-web
# Required-Start:       $local_fs $remote_fs $network $time omero postgresql
# Required-Stop:        $local_fs $remote_fs $network $time omero postgresql
# Default-Start:        2 3 4 5
# Default-Stop:         0 1 6
# Short-Description:    OMERO.web
### END INIT INFO

RETVAL=0
prog="omero-web"

# Read configuration variable file if it is present
[ -r /etc/default/$prog ] && . /etc/default/$prog
# also read the omero config
[ -r /etc/default/omero ] && . /etc/default/omero

OMERO_HOME=${OMERO_HOME:-"/home/omero/OMERO.server"}
OMERO_USER=${OMERO_USER:-"omero"}

start() {	
	echo -n $"Starting $prog:"
	sudo -iu ${OMERO_USER} ${OMERO_HOME}/bin/omero web start &> /dev/null && echo -n ' OMERO.web'
  sudo -iu ${OMERO_USER} bash ${OMERO_HOME%OMERO.server}/nginx-control.sh start
	RETVAL=$?
	[ "$RETVAL" = 0 ]
}

stop() {
	echo -n $"Stopping $prog:"
	sudo -iu ${OMERO_USER} ${OMERO_HOME}/bin/omero web stop &> /dev/null && echo -n ' OMERO.web'
  sudo -iu ${OMERO_USER} bash ${OMERO_HOME%OMERO.server}/nginx-control.sh stop
	RETVAL=$?
	[ "$RETVAL" = 0 ]
}

status() {
	echo -n $"Status $prog:"
	sudo -iu ${OMERO_USER} ${OMERO_HOME}/bin/omero web status
  sudo -iu ${OMERO_USER} bash ${OMERO_HOME%OMERO.server}/nginx-control.sh status
	RETVAL=$?
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
	*)	
		echo $"Usage: $0 {start|stop|restart|status}"
		RETVAL=1
esac
exit $RETVAL
