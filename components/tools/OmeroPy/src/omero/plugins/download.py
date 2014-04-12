#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   download plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2007 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import sys
import omero
import re
from omero.cli import BaseControl, CLI
from omero.rtypes import unwrap

HELP = """Download the given file id to the given filename"""


class DownloadControl(BaseControl):

    def _configure(self, parser):
        parser.add_argument(
            "object", help="ID of the OriginalFile to download or object of"
            " form <object>:<image_id>")
        parser.add_argument(
            "filename", help="Local filename to be saved to. '-' for stdout")
        parser.set_defaults(func=self.__call__)
        parser.add_login_arguments()

    def __call__(self, args):
        from omero_model_OriginalFileI import OriginalFileI as OFile

        client = self.ctx.conn(args)
        if ':' in args.object:
            image_id = self.parse_image_id(args.object)
            query = client.sf.getQueryService()
            params = omero.sys.ParametersI()
            params.addLong('iid', image_id)
            sql = "select f from Image i" \
                " join i.fileset as fs" \
                " join fs.usedFiles as uf" \
                " join uf.originalFile as f" \
                " where i.id = :iid"
            query_out = query.projection(sql, params)
            file_id = unwrap(query_out[0])[0].id.val
        else:
            file_id = self.parse_file_id(args.object)

        orig_file = OFile(file_id)
        target_file = str(args.filename)

        try:
            if target_file == "-":
                client.download(orig_file, filehandle=sys.stdout)
                sys.stdout.flush()
            else:
                client.download(orig_file, target_file)
        except omero.ValidationException, ve:
            # Possible, though unlikely after previous check
            self.ctx.die(67, "Unknown ValidationException: %s"
                         % ve.message)

    def parse_file_id(self, value):

        try:
            return long(value)
        except ValueError:
            self.ctx.die(601, 'Invalid OriginalFile ID input')

    def parse_image_id(self, value):

        pattern = r'Image:(?P<id>\d+)'
        pattern = re.compile('^' + pattern + '$')
        m = pattern.match(value)
        if not m:
            self.ctx.die(601, 'Invalid object input')
        return long(m.group('id'))


try:
    register("download", DownloadControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("download", DownloadControl, HELP)
        cli.invoke(sys.argv[1:])
