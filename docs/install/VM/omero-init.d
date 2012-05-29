#!/bin/bash
#
# /etc/init.d/omero
# Subsystem file for "omero" server
#
### BEGIN INIT INFO
# Provides:             omero-server
# Required-Start:       $local_fs $remote_fs $network $time postgresql
# Required-Stop:        $local_fs $remote_fs $network $time
# Default-Start:        2 3 4 5
# Default-Stop:         0 1 6
# Short-Description:    OMERO.server
### END INIT INFO

RETVAL=0
prog="omero"

# Read configuration variable file if it is present
[ -r /etc/default/$prog ] && . /etc/default/$prog

OMERO_HOME=${OMERO_HOME:-"/home/omero/OMERO.server"}
OMERO_USER=${OMERO_USER:-"omero"}

start() {	
	echo -n $"Starting $prog:"
	sudo -iu ${OMERO_USER} ${OMERO_HOME}/bin/omero admin start
	RETVAL=$?
	[ "$RETVAL" = 0 ]
	echo
}

stop() {
	echo -n $"Stopping $prog:"
	sudo -iu ${OMERO_USER} ${OMERO_HOME}/bin/omero admin stop
	RETVAL=$?
	[ "$RETVAL" = 0 ]
	echo
}

status() {
	echo -n $"Status $prog:"
	sudo -iu ${OMERO_USER} ${OMERO_HOME}/bin/omero admin status && echo -n ' OMERO.server running'
	RETVAL=$?
	echo
}

diagnostics() {
	echo -n $"Diagnostics $prog:"
	sudo -iu ${OMERO_USER} ${OMERO_HOME}/bin/omero admin diagnostics
	RETVAL=$?
	echo
}

clearlogs() {
  LOGDIR=${LOGDIR:-${OMERO_HOME}/var/log}
  TARFILE=${TARFILE:-omero-logs-$(date '+%F').tar.bz2}
  echo -n $"Clearing logs $prog:"
  cd $LOGDIR && tar -caf $TARFILE *.{err,out,log} && \
	(for x in ${LOGDIR}/*.{err,out,log}; do : > $x ;done) && \
	chown $OMERO_USER ${LOGDIR}/${TARFILE} && \
	echo -n $" saved to $LOGFILE/$TARFILE:"
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
  clearlogs)
    clearlogs
    RETVAL=$?
    ;;
	*)	
		echo $"Usage: $0 {start|stop|restart|status|diagnostics|clearlogs}"
		RETVAL=1
esac
exit $RETVAL
