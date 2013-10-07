#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2012 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
Integration test focused on the omero.api.IMetadata interface
a running server.
"""

import time
import datetime
import test.integration.library as lib
import pytest
import omero
from omero.rtypes import *


NAMES = ("TagAnnotation",
        "TagAnnotationI",
        "omero.model.TagAnnotation",
        "omero.model.TagAnnotationI",
        "ome.model.annotations.TagAnnotation")

class TestIMetadata(lib.ITest):

    def setup_method(self, method):
        lib.ITest.setup_method(self, method)
        self.md = self.client.sf.getMetadataService()

    def testLoadAnnotations3671(self):
        """
        See #3671. Support for less-strict class names
        """
        with pytest.raises(omero.ApiUsageException):
            self.md.loadAnnotations('Project', [0], ['X'], None, None)
        for name in NAMES:
            self.md.loadAnnotations('Project', [0], [name], None, None)
        self.md.loadAnnotations('Project', [0], NAMES, None, None)

    def testLoadAnnotationsUsedNotOwned3671(self):
        """
        See #3671. Support for less-strict class names
        """
        with pytest.raises(omero.ApiUsageException):
            self.md.loadAnnotationsUsedNotOwned('X', 0, None)
        for name in NAMES:
            self.md.loadAnnotationsUsedNotOwned(name, 0, None)

    def testCountAnnotationsUsedNotOwned3671(self):
        """
        See #3671. Support for less-strict class names
        """
        with pytest.raises(omero.ApiUsageException):
            self.md.countAnnotationsUsedNotOwned('X', 0, None)
        for name in NAMES:
            self.md.countAnnotationsUsedNotOwned(name, 0, None)

    def testCountSpecifiedAnnotations3671(self):
        """
        See #3671. Support for less-strict class names
        """
        with pytest.raises(omero.ApiUsageException):
            self.md.countSpecifiedAnnotations('X', [], [], None)
        for name in NAMES:
            self.md.countSpecifiedAnnotations(name, [], [], None)

    def testLoadSpecifiedAnnotations3671(self):
        """
        See #3671. Support for less-strict class names
        """
        with pytest.raises(omero.ApiUsageException):
            self.md.loadSpecifiedAnnotations('X', [], [], None)
        for name in NAMES:
            self.md.loadSpecifiedAnnotations(name, [], [], None)

