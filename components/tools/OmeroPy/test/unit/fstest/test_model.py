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
import omero
import omero.all

from omero.rtypes import rstring
from omero.rtypes import rtime


class TestModel(object):

    def mkentry(self, clientPath):
        originalFile = omero.model.OriginalFileI()
        parts = clientPath.split("/")
        path = "/".join(parts[:-1])
        name = parts[-1]
        originalFile.path = rstring(path)
        originalFile.name = rstring(name)
        # etc.
        entry = omero.model.FilesetEntryI()
        entry.clientPath = rstring(clientPath)
        entry.originalFile = originalFile
        return entry

    def testBasicImport(self):
        """
        basic server-side import steps

        Once a list of file paths have been passed to the server,
        an omero.model.Fileset object will be created which captures
        the state of the import.
        """

        # This will be created server-side
        serverInfo = {}
        serverInfo['bioformatsReader'] = rstring("ExampleReader")
        serverInfo['bioformatsVersion'] = rstring("v4.4.5 git: abc123"),
        serverInfo['omeroVersion'] = rstring("v.4.4.4 git: def456"),
        serverInfo['osName'] = rstring("Linux"),
        serverInfo['osArchitecture'] = rstring("amd64"),
        serverInfo['osVersion'] = rstring("2.6.38-8-generic"),
        serverInfo['locale'] = rstring("en_US")

        # Now that the basics are setup, we
        # need to link to all of the original files.
        fs = omero.model.FilesetI()
        fs.addFilesetEntry(self.mkentry("main_file.txt"))  # First!
        fs.addFilesetEntry(self.mkentry("uf1.data"))
        fs.addFilesetEntry(self.mkentry("uf2.data"))

        # Now that the files are all added, we
        # add the "activities" that will be
        # performed on them.

        # Uploading is almost always the first
        # step, and must be completed by the clients
        # before any other activity.
        job1 = omero.model.UploadJobI()
        job1.scheduledFor = rtime(time.time() * 1000)  # Now
        # Set this "started" since we're expecting
        # upload to be in process.

        # Import is a server-side activity which
        # causes the files to be parsed and their
        # metadata to be stored.
        job2 = omero.model.MetadataImportJobI()

        # Other possible activities include "pyramids"
        # and "re-import"

        fs.linkJob(job1)
        fs.linkJob(job2)
