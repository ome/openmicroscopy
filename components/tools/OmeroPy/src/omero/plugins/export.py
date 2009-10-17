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
        args = Arguments(args)

        try:
            options, args = getopt(args.args, "s:u:k:w:p:f:t:h")
            if len(options) == 0 and len(args) == 0:
                raise GetoptError("No arguments")
        except GetoptError, (msg, opt):
            self.help("Bad arguments")
            self.ctx.die(0, "")

        server = None
        user = None
        port = 4063
        pasw = None
        file = None
        tiff = "TIFF"
        key = None
        for option, argument in options:
            if option == "-u":
                user = argument
            elif option == "-w":
                pasw = argument
            elif option == "-s":
                server = argument
            elif option == "-p":
                port = int(argument)
            elif option == "-f":
                file = argument
            elif option == "-k":
                key = argument
            elif option == "-t":
                tiff = argument
            else:
                self.ctx.out("Ignoring option: %s" % option)

        if server is None:
            server = self.ctx.input("Server:")
        if key:
            user = key
            pasw = key
        else:
            if file is None and pasw is None:
                self.ctx.die(3, "Password or key must be provided to send to stdout")
            if user is None and key is None:
                user = self.ctx.input("Username:")
            if pasw is None and key is None:
                pasw = self.ctx.input("Password:", hidden = True)
        if tiff not in ["TIFF","XML"]:
            self.ctx.die(1, "Only TIFF and XML supported")
        if file is None:
            self.ctx.die(7, "No file specified")
        if os.path.exists(str(file)):
            self.ctx.die(2, "%s already exists" % file)

        if len(args) > 1:
            self.ctx.die(4, "Currently only a single image supported")

        klass, id = args[0].split(":")
        images = []
        if klass == "Image":
            images.append(id)
        else:
            self.ctx.die(5, "Can't add type: %s" % klass)

        c = self.ctx.conn({"omero.host":server, "omero.user":user,"omero.pass":pasw})
        e = None

        if file:
            f = open(file, "wb")

        try:
            e = c.getSession().createExporter()
            try:
                for img in images:
                    e.addImage(long(img))
                if tiff == "TIFF":
                    l = e.generateTiff()
                else:
                    l = e.generateXml()

                offset = 0
                while True:
                    rv = e.read(offset, 1000*1000)
                    if not rv:
                        break
                    offset += len(rv)
                    if file:
                        f.write(rv)
                    else:
                        sys.stdout.buffer.write(rv)

            finally:
                if file:
                    f.close()
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
        args = Arguments(*args)
        self._run(args)

try:
    register("export", ExportControl)
except NameError:
    ExportControl()._main()
