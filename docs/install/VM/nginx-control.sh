#!/bin/sh
#
# $Id$

OMERO_HOME=${OMERO_HOME:-"/home/omero/OMERO.server"}

case $1 in
  start)
    /usr/sbin/nginx -c $OMERO_HOME/omero-web-nginx.conf
    ;;
  stop)
    /usr/sbin/nginx -s stop -c $OMERO_HOME/omero-web-nginx.conf
    ;;
  restart)
    /usr/sbin/nginx -t $OMERO_HOME/omero-web-nginx.conf && \
      /usr/sbin/nginx -s reopen -c $OMERO_HOME/omero-web-nginx.conf
    ;;
  reload)
    /usr/sbin/nginx -s reload -c $OMERO_HOME/omero-web-nginx.conf
    ;;
  force-stop)
    /usr/sbin/nginx -s quit -c $OMERO_HOME/omero-web-nginx.conf
    ;;
  status)
    ps -Fw -C nginx
    ;;
  *)
    echo "$0 (start|stop|restart|reload|force-stop|status)"
    exit 1
    ;;
esac

