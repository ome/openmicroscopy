#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011-2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
Integration tests for tickets between 4000 and 4999
"""
import omero
import library as lib


class TestTickets5000(lib.ITest):

    def test4341(self):
        """
        Delete annotation with IUpdate
        """
        tag = omero.model.TagAnnotationI()
        tag = self.update.saveAndReturnObject(tag)
        self.update.deleteObject(tag)
