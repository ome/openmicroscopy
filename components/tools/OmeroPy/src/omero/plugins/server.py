#!/usr/bin/env python
"""
   Startup plugin for our various server components, called typically
   by icegridnode after parsing etc/grid/templates.xml.

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import subprocess, optparse, os, sys
from omero.cli import Arguments, BaseControl, VERSION
import omero.java

class ServerControl(BaseControl):

    def _prop(self, data, key):
        return data.properties.getProperty("omero."+key)

    def help(self, args = None):
        self.ctx.out("Start the blitz server -- Reads properties via omero prefs")

    def blitz(self, args):

        args = Arguments(args)
        first, other = args.firstOther()

        if not first or not first.startswith("--Ice.Config"):
            self.ctx.die(201, "No --Ice.Config provided")
        if len(other) > 0:
            self.ctx.err("Non --Ice.Config arguments provided: "+str(other))

        data = self.ctx.initData({})
        xargs = self._prop(data, "blitz.xargs")
        if len(xargs) == 0:
            xargs = "-Xmx400M"
        # Appending the to the end of xargs since required
        xargs = xargs + " -Djava.awt.headless=true"

        debug = self._prop(data, "blitz.debug")
        if debug == "true":
            debug = True
        else:
            debug = False

        # Run java -jar blitz/blitz.jar replacing the current process
        omero.java.run(["-jar","blitz/blitz.jar",first], debug=debug, xargs=xargs, use_exec = True)

try:
    register("server", ServerControl)
except NameError:
    ServerControl()._main()
