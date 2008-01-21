#!/usr/bin/env python
"""
   upoad plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2007 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import shlex

def do_upload(self, arg):
    """
    Syntax: upload <filename>
    Upload the given file name
    """

    files = shlex.split(arg)
    print arg + " = " + str(files)
    client = self.client()
    for f in files:
        obj = client.upload(f)
        print "Uploaded %s as " % f + str(obj.id.val)

CLI.do_upload = do_upload
