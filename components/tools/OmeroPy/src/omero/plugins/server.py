#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   Startup plugin for our various server components, called typically
   by icegridnode after parsing etc/grid/templates.xml.

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2008, 2016 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import sys
import platform
from omero.cli import BaseControl, CLI
import omero.java

from omero.install.windows_warning import windows_warning, WINDOWS_WARNING

HELP = """Start commands for server components"""

if platform.system() == 'Windows':
    HELP += ("\n\n%s" % WINDOWS_WARNING)


class ServerControl(BaseControl):

    def _configure(self, parser):
        sub = parser.sub()
        parser.add(sub, self.blitz, help="Start OMERO.blitz")
        parser.add(sub, self.indexer, help="Start OMERO.indexer")
        # web = parser.add(sub, self.web, help = "Start OMERO.web")
        # web.add_argument("arg", nargs="*")

    def _prop(self, data, key):
        return data.properties.getProperty("omero."+key)

    @windows_warning
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
        return pre, post

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

    @windows_warning
    def help(self, args=None):
        self.ctx.out(
            "Start the blitz server -- Reads properties via omero prefs")

    def blitz(self, args):
        pre, post = self._checkIceConfig(args)
        xargs, debug = self._xargsAndDebug("blitz", ["-Xmx400M"])
        blitz_jar = os.path.join("lib", "server", "blitz.jar")
        command = pre+["-jar", blitz_jar]+post
        omero.java.run(command, debug=debug, xargs=xargs, use_exec=True)

    def indexer(self, args):
        pre, post = self._checkIceConfig(args)
        xargs, debug = self._xargsAndDebug("indexer", ["-Xmx256M"])
        blitz_jar = os.path.join("lib", "server", "blitz.jar")
        omero.java.run(pre+["-jar", blitz_jar, "ome.fulltext"]+post,
                       debug=debug, xargs=xargs, use_exec=True)

    # def web(self, args):
    #    sys.stderr.write("Starting django... \n")
    #    omero_web = self.ctx.dir / "lib" / "python" / "omeroweb"
    #    os.chdir(str(omero_web))
    #    import omeroweb.settings as settings
    #    deploy = getattr(settings, 'APPLICATION_SERVER', 'default')
    #    if deploy == 'fastcgi':
    #        cmd = "python manage.py runfcgi workdir=./"
    #        cmd += " method=prefork socket=%(base)s/var/django_fcgi.sock"
    #        cmd += " pidfile=%(base)s/var/django.pid daemonize=false"
    #        cmd += " maxchildren=5 minspare=1 maxspare=5 maxrequests=400"
    #        django = (cmd % {'base': self.ctx.dir}).split()+list(args.arg)
    #    else:
    #        django = [sys.executable,
    # "manage.py","runserver","--noreload"]+list(args.arg)
    #    sys.stderr.write(str(django) + '\n')
    #    os.execvpe(sys.executable, django, os.environ)

try:
    register("server", ServerControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("server", ServerControl, HELP)
        cli.invoke(sys.argv[1:])
