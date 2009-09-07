#!/usr/bin/env python
"""
   Startup plugin for command-line exporter

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import subprocess, optparse, os, sys, signal, time
from omero.cli import Arguments, BaseControl, VERSION, OMERODIR
import omero.java

class ExportControl(BaseControl):

    def _run(self, args = []):
        args = Arguments(args)

        first, other = args.firstOther()
        klass, id = first.split(":")
        images = []
        if klass == "Image":
            images.append(id)
        else:
            self.ctx.die(300, "Can't add type: %s" % klass)

        c = self.ctx.conn({"omero.host":"localhost", "omero.user":"root","omero.pass":"ome"})
        e = None
        buf = []
        try:
            e = c.getSession().createExporter()
            try:
                for img in images:
                    e.addImage(long(img))
                while True:
                    rv = e.getBytes(50000)
                    import time
                    buf.extend(rv)
                    if len(rv) < 50000:
                        break
            finally:
                # e.close()
                pass
        finally:
                c.closeSession() # FIXME
        print "".join(buf)

    def help(self, args = None):
        self.ctx.out("""
Usage: %(program) export [OPTION]... Image:<id> ...
Export OMERO data in various OME files.

Mandatory arguments:
  -s    OMERO server hostname
  -u    OMERO experimenter name (username)
  -w    OMERO experimenter password
  -k    OMERO session key (can be used in place of -u and -w)

Optional arguments: (UNSUPPORTED)
  -p    OMERO server port [defaults to 4063]
  -f    Output file rather than standard out
  -a    Include annotations
  -h    Display this help and exit
  -t    Format (XML, TIFF, ...)

ex. %(program) export -s localhost -u bart -w simpson Image:50 > image_50.ome.xml

Report bugs to <ome-users@openmicroscopy.org.uk>""")

    def __call__(self, *args):
        args = Arguments(*args)
        self._run(args)


try:
    register("export", ExportControl)
except NameError:
    ImportControl()._main()
