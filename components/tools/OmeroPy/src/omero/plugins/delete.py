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

from omero.cli import BaseControl, CLI

HELP = """Delete OMERO data

Example: bin/omero delete Image:50

"""

class DeleteControl(BaseControl):

    def _configure(self, parser):
        parser.add_argument("obj", nargs="+", help="""Objects to be deleted in the form "<Classs>:<Id>""")
        parser.set_defaults(func=self.delete)

    def delete(self, args):

        client = self.ctx.conn(args)

        images = []
        plates = []
        for arg in args.obj:
            klass, id = arg.split(":")
            if klass == "Image":
                images.append(long(id))
            elif klass == "Plate":
                plates.append(long(id))
            else:
                self.ctx.die(5, "Can't delete type: %s" % klass)

        def action(klass, method, *args):
            import omero
            self.ctx.out(("Deleting %s %s... " % (klass, args)), newline = False)
            try:
                method(*args)
                self.ctx.out("ok.")
            except omero.ApiUsageException, aue:
                self.ctx.out(aue.message)
            except exceptions.Exception, e:
                self.ctx.out("failed (%s)" % e)

        deleteSrv = client.getSession().getDeleteService()
        for image in images: action("Image", deleteSrv.deleteImage, image, True)
        for plate in plates: action("Plate", deleteSrv.deletePlate, plate)

try:
    register("delete", DeleteControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("delete", DeleteControl, HELP)
        cli.invoke(sys.argv[1:])
