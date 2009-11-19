#!/usr/bin/env python
"""
   Startup plugin for command-line deletes

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import sys
import array
import exceptions

from omero.cli import Arguments, BaseControl, VERSION, OMERODIR

class DeleteControl(BaseControl):

    def _run(self, args = []):
        args = Arguments(args)

        from getopt import getopt, GetoptError
        try:
            options, args = getopt(args.args, "s:u:k:w:p:h")
            if len(options) == 0 and len(args) == 0:
                raise GetoptError("No arguments")
        except GetoptError, (msg, opt):
            self.help("Bad arguments")
            self.ctx.die(0, "")

        server = None
        user = None
        port = 4063
        pasw = None
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
            elif option == "-k":
                key = argument
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

        import omero.clients
        images = []
        plates = []
        for arg in args:
            klass, id = arg.split(":")
            if klass == "Image":
                images.append(long(id))
            elif klass == "Plate":
                plates.append(long(id))
            else:
                self.ctx.die(5, "Can't add type: %s" % klass)

        c = self.ctx.conn({"omero.host":server, "omero.user":user,"omero.pass":pasw})
        e = None

        def action(klass, method, *args):
            self.ctx.out("Deleting %s %s..." % (klass, args), newline = False)
            try:
                method(*args)
                self.ctx.out("ok.")
            except exceptions.Exception, e:
                self.ctx.out("failed (%s)" % e)

        try:
            deleteSrv = c.getSession().getDeleteService()
            for image in images: action("Image", deleteSrv.deleteImage, image, True)
            for plate in plates: action("Plate", deleteSrv.deletePlate, plate)
        finally:
            c.closeSession() # FIXME

    def help(self, args = None):
        self.ctx.out("""
Usage: %s delete [OPTION]... Image:<id> Plate:<id> ...
Delete OMERO data

Queried arguments: (if not provided, requires user input)
  -s    OMERO server hostname
  -u    OMERO experimenter name (username)
  -k    OMERO session key (can be used in place of -u and -w)
  -w    OMERO experimenter password (Requested if not provided)

Optional arguments:
  -p    OMERO server port [defaults to 4063]
  -h    Display this help and exit

ex. %s delete -s localhost -u bart -w simpson Image:50

Report bugs to <ome-users@openmicroscopy.org.uk>""" % (sys.argv[0], sys.argv[0]))

    def __call__(self, *args):
        args = Arguments(*args)
        self._run(args)

try:
    register("delete", DeleteControl)
except NameError:
    DeleteControl()._main()
