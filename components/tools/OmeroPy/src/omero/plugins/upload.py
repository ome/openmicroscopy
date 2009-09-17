#!/usr/bin/env python
"""
   upoad plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2007 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from omero.cli import BaseControl
import omero.util.originalfileutils;
class UploadControl(BaseControl):

    def help(self, args = None):
        return \
            """
Syntax: %(program_name)s upload <filename>
        Upload the given file name
    """

    def __call__(self, *args):
        import omero
        args = Arguments(*args)
        client = self.ctx.conn()
        for f in args:
            format = omero.util.originalfileutils.getFormat(f);
            omeroFormat = format[1];    
            obj = client.upload(f, f, f, omeroFormat)
            print "Uploaded %s as " % f + str(obj.id.val)

try:
    register("upload", UploadControl)
except NameError:
    UploadControl._main()
