#!/usr/bin/env python
"""
   download plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2007 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""


def do_download(self, arg):
    """
    Syntax: download <id> <filename>
    Download the given file id to the given file name
    """

    id = 1
    file = "foo"

    client = self.client()
    session = client.getSession()
    filePrx = session.createRawFileStore()
    filePrx.setFileId(id)
    fileSize = filePrx.getSize()

CLI.do_download = do_download
