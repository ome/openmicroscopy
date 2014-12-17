#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011-2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
Integration test focused on the omero.api.IUpdate interface.
"""

import library as lib
import omero


class TestIUpdate(lib.ITest):
    """
    Basic test of all IUpdate functionality
    """

    def tags(self, count=3):
        return [omero.model.TagAnnotationI() for x in range(count)]

    def testSaveArray(self):
        """
        See ticket:6870
        """
        tags = self.tags()
        self.update.saveArray(tags)

    def testSaveCollection(self):
        """
        See ticket:6870
        """
        tags = self.tags()
        self.update.saveCollection(tags)
