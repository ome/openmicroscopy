#!/usr/bin/env python
"""
   upoad plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2007 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from omero.cli import BaseControl

class UploadControl(BaseControl):

    def help(self):
        return \
            """
Syntax: %(program_name)s upload <filename>
        Upload the given file name
    """

    def __call__(self, *args):
        args = Arguments(*args)
        client = self.client()
        for f in args:
            obj = client.upload(f)
            print "Uploaded %s as " % f + str(obj.id.val)

try:
    register("upload", UploadControl)
except NameError:
    UploadControl._main()
