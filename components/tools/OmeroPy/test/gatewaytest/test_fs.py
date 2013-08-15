#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
# All rights reserved.
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


import omero
import time



class TestFileset (object):

    def testFileset(self, author_testimg_tiny):
        image = author_testimg_tiny

        # Assume image is not imported pre-FS
        filesCount = image.countFilesetFiles()
        assert filesCount > 0, "Imported image should be linked to original files"

        # List the 'imported image files' (from fileset), check the number
        filesInFileset = list(image.getImportedImageFiles())
        assert filesCount ==  len(filesInFileset)


