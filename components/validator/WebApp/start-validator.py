#!/usr/bin/env python
# -*- coding: utf-8 -*-
import pkg_resources
pkg_resources.require("TurboGears")

from turbogears import update_config, start_server
import cherrypy
import os
cherrypy.lowercase_api = True

from os.path import join, dirname, exists
import sys

import datetime
from datetime import timedelta
import glob


LOCAL_DIR = os.path.dirname(os.path.join(os.getcwd(), "uploads/"))
SESSION_PREFIX = 'session-'
LOCK = 'Store'


def Delete_dirs(data):
    sessionfiles = [fname for fname in os.listdir(LOCAL_DIR)
                    if (fname.startswith(SESSION_PREFIX)
                        and not fname.endswith(LOCK))]

    now = datetime.datetime.now()
    now.timetuple()
    for sfile in sessionfiles:
        for file in glob.glob(LOCAL_DIR+"/"+sfile):
            stats = os.stat(file)
            dtfile = datetime.datetime.fromtimestamp(stats[8])
            t = now-timedelta(days=2)
            if t > dtfile:
                os.remove(os.path.join((LOCAL_DIR), sfile))

    for fname in os.listdir(LOCAL_DIR):
        if (not fname.startswith(SESSION_PREFIX) and not fname.endswith(LOCK)):
            for sname in sessionfiles:
                if not fname.endswith(sname.split('-')[1]):
                    for aname in os.listdir(LOCAL_DIR+'/'+fname):
                        os.remove(os.path.join((LOCAL_DIR+'/'+fname), aname))
                    os.rmdir(os.path.join(LOCAL_DIR, fname))

# first look on the command line for a desired config file,
# if it's not on the command line, then
# look for setup.py in this directory. If it's not there, this script is
# probably installed
if len(sys.argv) > 1:
    update_config(configfile=sys.argv[1],
                  modulename="validator.config")
elif exists(join(dirname(__file__), "setup.py")):
    update_config(configfile="dev.cfg", modulename="validator.config")
else:
    update_config(configfile="prod.cfg", modulename="validator.config")

if not os.path.isdir(LOCAL_DIR):
    try:
        os.mkdir(LOCAL_DIR)
    except IOError:
        print "IOError: %s could not be created" % LOCAL_DIR

from validator.controllers import Root

cherrypy.root = Root()
cherrypy.config.update({
    'server.log_to_screen': True,
    'server.environment': 'production',
    'session_filter.on': True,
    'session_filter.storage_type': 'file',
    'session_filter.storage_path': LOCAL_DIR,
    'session_filter.timeout': 60,
    'session_filter.clean_up_delay': 60,
    'session_filter.on_create_session': Delete_dirs,
    })

start_server(Root())
