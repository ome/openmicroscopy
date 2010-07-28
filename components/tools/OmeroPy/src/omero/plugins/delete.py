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

HELP = """Delete OMERO data.

Where available (currently: Image & Plate) special methods
are used for deleting the objects. Otherwise, IUpdate.deleteObject()
is used.


Examples:

    bin/omero delete Image:50
    bin/omero delete Plate:1

"""

class DeleteControl(BaseControl):

    def _configure(self, parser):
        parser.add_argument("obj", nargs="+", help="""Objects to be deleted in the form "<Classs>:<Id>""")
        parser.set_defaults(func=self.delete)

    def delete(self, args):

        import omero
        client = self.ctx.conn(args)

        images = []
        plates = []
        objects = []
        for arg in args.obj:
            if 0 > arg.find(":"):
                self.ctx.die(5, "Format: 'Image:<id>'")
            klass, id = arg.split(":")
            if klass == "Image":
                images.append(long(id))
            elif klass == "Plate":
                plates.append(long(id))
            else:
                ctor = getattr(omero.model, "%sI" % klass)
                if not ctor:
                    ctor = getattr(omero.model, klass)
                try:
                    objects.append(ctor(long(id), False))
                except exceptions.Exception, e:
                    self.ctx.dbg("Exception on ctor: %s" % e)
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
        updateSrv = client.getSession().getUpdateService()
        for image in images: action("Image", deleteSrv.deleteImage, image, True)
        for plate in plates: action("Plate", deleteSrv.deletePlate, plate)
        for object in objects: action(object.__class__.__name__, updateSrv.deleteObject, object)

try:
    register("delete", DeleteControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("delete", DeleteControl, HELP)
        cli.invoke(sys.argv[1:])
