#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2012 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""
Basic usage of the "fileset" model objects.
These tests don't do anything functional, but
just show creation / linkage scenarios.
"""

import time
import unittest
import omero
import omero.all

from omero.rtypes import rstring as _
from omero.rtypes import rtime


class TestModel(unittest.TestCase):

    def mkentry(self, clientPath):
        originalFile = omero.model.OriginalFileI()
        parts = clientPath.split("/")
        path = "/".join(parts[:-1])
        name = parts[-1]
        originalFile.path = _(path)
        originalFile.name = _(name)
        # etc.
        entry = omero.model.FilesetEntryI()
        entry.clientPath = _(clientPath)
        entry.originalFile = originalFile
        return entry

    def testBasicImport(self):
        """
        basic server-side import steps

        Once a list of file paths have been passed to the server,
        an omero.model.Fileset object will be created which captures
        the state of the import.
        """

        # This should be passed in by the client
        clientInfo = omero.model.FilesetVersionInfoI()

        # This will be created server-side
        serverInfo = omero.model.FilesetVersionInfoI()
        serverInfo.bioformatsReader = _("ExampleReader")
        serverInfo.bioformatsVersion = _("v4.4.5 git: abc123")
        serverInfo.omeroVersion = _("v.4.4.4 git: def456")
        serverInfo.osName = _("Linux")
        serverInfo.osArchitecture = _("amd64")
        serverInfo.osVersion = _("2.6.38-8-generic")
        # Something returned by Locale.getDefault().toString()
        serverInfo.locale = "en_US"

        # Now that the basics are setup, we
        # need to link to all of the original files.
        fs = omero.model.FilesetI()
        fs.addFilesetEntry(self.mkentry("main_file.txt")) # First!
        fs.addFilesetEntry(self.mkentry("uf1.data"))
        fs.addFilesetEntry(self.mkentry("uf2.data"))

        # Now that the files are all added, we
        # add the "activities" that will be
        # performed on them.

        # Uploading is almost always the first
        # step, and must be completed by the clients
        # before any other activity.
        act1 = omero.model.FilesetActivityI()
        act1.name = _("upload")
        act1.job = omero.model.ImportJobI() # TODO: better types
        act1.job.scheduledFor = rtime(time.time() * 1000) # Now
        # Set this "started" since we're expecting
        # upload to be in process.

        # Import is a server-side activity which
        # causes the files to be parsed and their
        # metadata to be stored.
        act2 = omero.model.FilesetActivityI()
        act2.name = _("import")

        # Most files will also have thumbnails generated
        # Some viewing can occur during this process
        act3 = omero.model.FilesetActivityI()
        act3.name = _("thumbnailing")

        # Other possible activities include "pyramids"
        # and "re-import"

        fs.addFilesetActivity(act1)
        fs.addFilesetActivity(act2)
        fs.addFilesetActivity(act3)


if __name__ == '__main__':
    unittest.main()
