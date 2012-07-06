#! /bin/sh
### BEGIN INIT INFO
# Provides:          virtualbox-network-fix
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Example initscript
# Description:       Fix a bug in virtualbox where it does not immediately give out an address
### END INIT INFO

# Author: Chris MacLeod <ckm@glencoesoftware.com>

# Do NOT "set -e"

# PATH should only include /usr/* if it runs after the mountnfs.sh script
PATH=/sbin:/usr/sbin:/bin:/usr/bin
DESC="Networking fix for virtualbox"
NAME=virtualbox-network-fix
PIDFILE=/var/run/$NAME.pid
SCRIPTNAME=/etc/init.d/$NAME

# Load the VERBOSE setting and other rcS variables
. /lib/init/vars.sh

# Define LSB log_* functions.
# Depend on lsb-base (>= 3.2-14) to ensure that this file is present
# and status_of_proc is working.
. /lib/lsb/init-functions

#
# Function that starts the daemon/service
#
do_start()
{
	# Return
	#   0 if daemon has been started
	#   1 if daemon was already running
	#   2 if daemon could not be started

  # we attempt and return success regardless

  # check up against a prefix, if the interface has an address in this prefix it will return data
  INTERFACE_CONFIGURED=$( ip addr show to 10/8 )

  if [ -z "$INTERFACE_CONFIGURED" ]; then
    # check for link status
    if [ -e "/sbin/ethtool" ]; then
      LINK_Y=$( ethtool $IFACE 2>/dev/null |grep "Link detected: yes" )
    elif [ -e "/sbin/mii-tool" ]; then
      LINK_Y=$( mii-tool eth0 2>/dev/null |grep "link ok" )
    else
      LINK_Y=$( ip addr show eth0 | grep "state UP" )
    fi
    if [ -n "$LINK_Y" ]; then
      dhclient -r
      dhclient eth0
    fi
  fi
  return 0
}

#
# Function that stops the daemon/service
#
do_stop()
{
	# Return
	#   0 if daemon has been stopped
	#   1 if daemon was already stopped
	#   2 if daemon could not be stopped
	#   other if a failure occurred
  return 0
}

#
# Function that sends a SIGHUP to the daemon/service
#
do_reload() {
	#
	return 0
}

case "$1" in
  start)
	[ "$VERBOSE" != no ] && log_daemon_msg "Starting $DESC" "$NAME"
	do_start
	case "$?" in
		0|1) [ "$VERBOSE" != no ] && log_end_msg 0 ;;
		2) [ "$VERBOSE" != no ] && log_end_msg 1 ;;
	esac
	;;
  stop)
	[ "$VERBOSE" != no ] && log_daemon_msg "Stopping $DESC" "$NAME"
	do_stop
	case "$?" in
		0|1) [ "$VERBOSE" != no ] && log_end_msg 0 ;;
		2) [ "$VERBOSE" != no ] && log_end_msg 1 ;;
	esac
	;;
  status)
    IFACE_DETAILS=$( ip addr show to 10/8 )
    if [ -n "$IFACE_DETAILS" ]; then
      echo "$IFACE_DETAILS"
    else
      echo "Interfaced unconfigured."
    fi
       ;;
  #reload|force-reload)
	#
	# If do_reload() is not implemented then leave this commented out
	# and leave 'force-reload' as an alias for 'restart'.
	#
	#log_daemon_msg "Reloading $DESC" "$NAME"
	#do_reload
	#log_end_msg $?
	#;;
  restart|force-reload)
	#
	# If the "reload" option is implemented then remove the
	# 'force-reload' alias
	#
	log_daemon_msg "Restarting $DESC" "$NAME"
	do_stop
	case "$?" in
	  0|1)
		do_start
		case "$?" in
			0) log_end_msg 0 ;;
			1) log_end_msg 1 ;; # Old process is still running
			*) log_end_msg 1 ;; # Failed to start
		esac
		;;
	  *)
	  	# Failed to stop
		log_end_msg 1
		;;
	esac
	;;
  *)
	#echo "Usage: $SCRIPTNAME {start|stop|restart|reload|force-reload}" >&2
	echo "Usage: $SCRIPTNAME {start|stop|status|restart|force-reload}" >&2
	exit 3
	;;
esac

:
