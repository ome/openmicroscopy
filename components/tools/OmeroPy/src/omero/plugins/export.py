#!/usr/bin/env python
"""
   Startup plugin for command-line exporter

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import sys

from omero.cli import BaseControl, CLI, NewFileType

HELP="""Support for exporting data in XML and TIFF formats"""


class ExportControl(BaseControl):

    def _configure(self, parser):
        parser.add_argument("-f", "--file", type=NewFileType("wb"), required=True, help="Filename to export to or '-' for stdout. File may not exist")
        parser.add_argument("-t", "--type", default="TIFF", choices=("TIFF", "XML"), help="Type of export. Default: %(default)s")
        parser.add_argument("obj", help="Format: Image:<id>")
        parser.set_defaults(func=self.export)

    def export(self, args):

        img = args.obj
        if 0 > img.find(":"):
            self.ctx.die(5, "Format: 'Image:<id>'")

        klass, id = img.split(":")
        images = []
        if klass == "Image":
            images.append(id)
        else:
            self.ctx.die(5, "Can't add type: %s" % klass)

        e = None
        handle = args.file

        c = self.ctx.conn(args)
        e = c.getSession().createExporter()

        remove = True
        try:
            for img in images:
                e.addImage(long(img))

            import omero
            try:
                if args.type == "TIFF":
                    l = e.generateTiff()
                else:
                    l = e.generateXml()
            except omero.ServerError, se:
                self.ctx.err("%s: %s" % (se.__class__.__name__, se.message))
                return

            remove = False
            offset = 0
            while True:
                rv = e.read(offset, 1000*1000)
                if not rv:
                    break
                offset += len(rv)
                if handle == sys.stdout:
                    sys.stdout.buffer.write(rv)
                else:
                    handle.write(rv)

        finally:
            if handle != sys.stdout:
                handle.close()
            if remove:
                os.remove(handle.name)
            e.close()

try:
    register("export", ExportControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("export", ExportControl, HELP)
        cli.invoke(sys.argv[1:])
