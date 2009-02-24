#!/usr/bin/env python
"""
   Startup plugin for our various server components, called typically
   by icegridnode after parsing etc/grid/templates.xml.

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import subprocess, optparse, os, sys, signal, time
from omero.cli import Arguments, BaseControl, VERSION
import omero.java

class ServerControl(BaseControl):

    def _prop(self, data, key):
        return data.properties.getProperty("omero."+key)

    def _checkIceConfig(self, first, other):
        if not first or not first.startswith("--Ice.Config"):
            self.ctx.die(201, "No --Ice.Config provided")
        if len(other) > 0:
            self.ctx.err("Non --Ice.Config arguments provided: "+str(other))

    def _xargsAndDebug(self, component, xargs_default):
        component = str(component)
        data = self.ctx.initData({})
        xargs = self._prop(data, component+".xargs")
        if len(xargs) == 0:
            xargs = xargs_default

        debug = self._prop(data, "component.debug")
        if debug == "true":
            debug = True
        else:
            debug = False

    def help(self, args = None):
        self.ctx.out("Start the blitz server -- Reads properties via omero prefs")

    def blitz(self, args):
        args = Arguments(args)
        first, other = args.firstOther()
        self._checkIceConfig(first, other)
        xargs, debug = self._xargsAndDebug("blitz", "-Xmx400M")
        blitz_jar = os.path.join("lib","server","blitz.jar")
        omero.java.run(["-jar",blitz_jar,first], debug=debug, xargs=xargs, use_exec = True)

    def indexer(self, args):
        args = Arguments(args)
        first, other = args.firstOther()
        self._checkIceConfig(first, other)
        xargs, debug = self._xargsAndDebug("indexer", "-Xmx128M")
        server_jar = os.path.join("lib","server","server.jar")
        omero.java.run(["-jar",server_jar,first], debug=debug, xargs=xargs, use_exec = True)

    def web(self, args):
        args = Arguments(args)
        sys.stderr.write("Starting django... \n")
        omero_web = os.path.join("lib","omeroweb")
        subprocess.call(["python","manage.py","syncdb","--noinput"], cwd=omero_web, env = os.environ)
        # Now exec
        os.chdir("omeroweb")
        django = ["python","manage.py","runserver","--noreload"]+list(args)
        os.execvpe("python", django, os.environ)
try:
    register("server", ServerControl)
except NameError:
    ServerControl()._main()
