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

    def _name(self):
        return "upload"

    def help(self):
        return \
            """
    Syntax: upload <filename>
    Upload the given file name
    """

    def _run(self, *args):
        files = shlex.split(arg)
        print arg + " = " + str(files)
        client = self.client()
        for f in files:
            obj = client.upload(f)
            print "Uploaded %s as " % f + str(obj.id.val)

c = UploadControl()
try:
    register(c)
except NameError:
    c._main()
