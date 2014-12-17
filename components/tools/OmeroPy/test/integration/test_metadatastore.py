#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test focused on the omero.api.MetadataStore interface

   Copyright 2008-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import library as lib
import omero


class TestMetdataStore(lib.ITest):

    def testBasicUsage(self):

        ms = self.client.sf.createByName(omero.constants.METADATASTORE)
        ms = omero.api.MetadataStorePrx.checkedCast(ms)

        ms.createRoot()
        # Needs work

    def testMetadataService(self):

        self.client.sf.getMetadataService()
