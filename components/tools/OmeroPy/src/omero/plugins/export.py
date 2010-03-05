#!/usr/bin/env python
"""
   Startup plugin for command-line exporter

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import sys
import array
from getopt import getopt, GetoptError

from omero.cli import Arguments, BaseControl, VERSION, OMERODIR
import omero.java

class ExportControl(BaseControl):

    def _run(self, args = []):
        args = Arguments(args, shortopts="f:t:", longopts=["file=","type="])
        c = self.ctx.conn(args)
        f = args.get_arg("file", "f")
        t = args.get_arg("type", "t")
        if not t:
            t = "TIFF"

        if t not in ("TIFF","XML"):
            self.ctx.die(1, "Only TIFF and XML supported")
        if f is None:
            self.ctx.die(7, "No file specified")
        if os.path.exists(str(f)):
            self.ctx.die(2, "%s already exists" % f)

        if len(args) != 1:
            self.ctx.die(4, "Currently only a single image supported")

        img = args.args[0]
        if 0 > img.find(":"):
            self.ctx.die(5, "Format: 'Image:<id>'")

        klass, id = img.split(":")
        images = []
        if klass == "Image":
            images.append(id)
        else:
            self.ctx.die(5, "Can't add type: %s" % klass)

        e = None
        handle = None
        if f:
            handle = open(f, "wb")

        try:
            e = c.getSession().createExporter()
            try:
                for img in images:
                    e.addImage(long(img))
                if t == "TIFF":
                    l = e.generateTiff()
                else:
                    l = e.generateXml()

                offset = 0
                while True:
                    rv = e.read(offset, 1000*1000)
                    if not rv:
                        break
                    offset += len(rv)
                    if handle:
                        handle.write(rv)
                    else:
                        sys.stdout.buffer.write(rv)

            finally:
                if handle:
                    handle.close()
                e.close()
        finally:
                c.closeSession() # FIXME


    def help(self, args = None):
        self.ctx.out("""
Usage: %s export [OPTION]... Image:<id>
Export OMERO image data.

Mandatory arguments:
  -f    Output file

Queried arguments: (if not provided, requires user input)
  -s    OMERO server hostname
  -u    OMERO experimenter name (username)
  -k    OMERO session key (can be used in place of -u and -w)
  -w    OMERO experimenter password (Requested if not provided)

Optional arguments:
  -p    OMERO server port [defaults to 4063]
  -t    Format: XML or TIFF Default: TIFF
  -h    Display this help and exit

ex. %s export -s localhost -u bart -w simpson -f image_50.ome.tiff Image:50

Report bugs to <ome-users@openmicroscopy.org.uk>""" % (sys.argv[0], sys.argv[0]))

    def __call__(self, *args):
        args = Arguments(args)
        self._run(args)

try:
    register("export", ExportControl)
except NameError:
    ExportControl()._main()
