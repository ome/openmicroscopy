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

    def _checkIceConfig(self, args):
        try:
            args["--Ice.Config"]
        except KeyError:
            self.ctx.die(201, "No --Ice.Config provided")
        pre = []
        post = []
        for arg in args.args:
            if arg.startswith("-D"):
                pre.append(arg)
            else:
                post.append(arg)
        return pre,post

    def _xargsAndDebug(self, component, xargs_default):
        component = str(component)
        data = self.ctx.initData({})
        xargs = self._prop(data, component+".xargs")
        if len(xargs) == 0:
            xargs = xargs_default
        debug = self._prop(data, component+".debug")
        if debug == "true":
            debug = True
        else:
            debug = False
        return xargs, debug

    def help(self, args = None):
        self.ctx.out("Start the blitz server -- Reads properties via omero prefs")

    def blitz(self, args):
        args = Arguments(args)
        print args.args
        pre, post = self._checkIceConfig(args)
        xargs, debug = self._xargsAndDebug("blitz", ["-Xmx400M"])
        print "got args"
        blitz_jar = os.path.join("lib","server","blitz.jar")
        print "Execp"
        command = pre+["-jar",blitz_jar]+post
        print command
        omero.java.run(command, debug=debug, xargs=xargs, use_exec = True)

    def indexer(self, args):
        args = Arguments(args)
        pre, post = self._checkIceConfig(args)
        xargs, debug = self._xargsAndDebug("indexer", ["-Xmx256M"])
        blitz_jar = os.path.join("lib","server","blitz.jar")
        omero.java.run(pre+["-jar",blitz_jar,"ome.fulltext"]+post, debug=debug, xargs=xargs, use_exec = True)

    def web(self, args):
        args = Arguments(args)
        sys.stderr.write("Starting django... \n")
        omero_web = self.ctx.dir / "lib" / "omeroweb"
        subprocess.call(["python","manage.py","syncdb","--noinput"], cwd=str(omero_web), env = os.environ)
        # Now exec
        os.chdir(str(omero_web))
        django = ["python","manage.py","runserver","--noreload"]+list(args)
        os.execvpe("python", django, os.environ)
try:
    register("server", ServerControl)
except NameError:
    ServerControl()._main()
