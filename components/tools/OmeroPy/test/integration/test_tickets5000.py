#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
Integration tests for tickets between 4000 and 4999
"""

import unittest, time
import test.integration.library as lib
import Glacier2

from omero.rtypes import *


class TestTickets5000(lib.ITest):

    def test4341(self):
        """
        Delete annotation with IUpdate
        """
        tag = omero.model.TagAnnotationI()
        tag = self.update.saveAndReturnObject(tag)
        self.update.deleteObject(tag)

if __name__ == '__main__':
    unittest.main()
