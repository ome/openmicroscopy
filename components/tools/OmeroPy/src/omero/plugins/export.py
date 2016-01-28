#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   Startup plugin for command-line exporter

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import sys

from omero.cli import BaseControl, CLI, NewFileType

HELP = """Support for exporting data in XML and TIFF formats

Example Usage:
  omero export --file new.ome.tif            Image:1
  omero export --file new.ome.xml --type XML Image:1
  omero export --file output-dir  --iterate  Dataset:2

If --iterate is used, the only supported obj is Dataset:<id>
"""


# Default block read size when downloading exported data
DEFAULT_READ_LENGTH = 1000*1000


class ExportControl(BaseControl):

    def _configure(self, parser):
        parser.add_argument(
            "-f", "--file", type=NewFileType("wb"), required=True,
            help="Filename to export to or '-' for stdout."
            " File may not exist")
        parser.add_argument(
            "-t", "--type", default="TIFF", choices=("TIFF", "XML"),
            help="Type of export. Default: %(default)s")
        parser.add_argument("obj", help="Format: Image:<id>")
        parser.add_argument(
            "--iterate", action="store_true", default=False,
            help="Iterate over an object and write individual objects to the"
            " directory named by --file (EXPERIMENTAL)")

        parser.set_defaults(func=self.export)
        parser.add_login_arguments()

    def export(self, args):

        img = args.obj
        if 0 > img.find(":"):
            self.ctx.die(5, "Format: 'Image:<id>'")

        klass, id = img.split(":")
        try:
            id = long(id)
        except:
            self.ctx.die(3, "Bad id format: %s" % id)

        images = []
        datasets = []

        if klass == "Image":
            images.append(id)
            self.handleImages(args, images)
        elif klass == "Dataset":
            if not args.iterate:
                self.ctx.die(4, "Dataset currently only supported with"
                                " --iterate")
            datasets.append(id)
            self.handleDatasets(args, datasets)
        else:
            self.ctx.die(5, "Can't add type: %s" % klass)

    def handleDatasets(self, args, datasets):

        if args.file == sys.stdout:
            self.ctx.die(950, "Can't use stdin for datasets")

        f = args.file
        dir = f.name
        f.close()
        os.remove(dir)
        os.makedirs(dir)

        c = self.ctx.conn(args)

        import omero
        p = omero.sys.ParametersI()
        p.leaves()

        ds = c.sf.getContainerService().loadContainerHierarchy("Dataset",
                                                               datasets, p)
        if not ds:
            self.ctx.die(7, "No datasets found: %s",
                         ", ".join([str(x) for x in datasets]))

        for d in ds:
            for i in d.linkedImageList():
                if i:
                    i = i.id.val
                    args.file = open(
                        os.path.join(dir, "%s.ome.%s"
                                     % (i, args.type.lower())), "wb")
                    self.handleImages(args, [i])

    def handleImages(self, args, images):
        e = None
        handle = args.file
        issysout = (handle == sys.stdout)

        c = self.ctx.conn(args)
        e = c.getSession().createExporter()

        remove = True
        try:
            for img in images:
                e.addImage(img)

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
                rv = e.read(offset, DEFAULT_READ_LENGTH)
                if not rv:
                    break
                rv = rv[:min(DEFAULT_READ_LENGTH, l - offset)]
                offset += len(rv)
                handle.write(rv)

        finally:
            try:
                if not issysout:
                    handle.close()
                    if remove:
                        os.remove(handle.name)
            except Exception, e:
                self.ctx.err("Failed to close handle: %s" % e)

            e.close()

try:
    register("export", ExportControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("export", ExportControl, HELP)
        cli.invoke(sys.argv[1:])
