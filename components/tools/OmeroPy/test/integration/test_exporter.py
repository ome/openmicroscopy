#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
   Tests for the stateful Exporter service.
"""

import omero
import unittest
import test.integration.library as lib

from omero.rtypes import rstring, rlong, rint
from omero.util.concurrency import get_event
from omero.util.tiles import *
from binascii import hexlify as hex


class TestExporter(lib.ITest):

    def bigimage(self):
        pix = self.pix(x=4000, y=4000, z=1, t=1, c=1)
        rps = self.client.sf.createRawPixelsStore()
        try:
            rps.setPixelsId(pix.id.val, True)
            self.write(pix, rps)
            return pix
        finally:
            rps.close()

    def testBasic(self):
        """
        Runs a simple export through to completion
        as a smoke test.
        """
        pix_ids = self.import_image()
        image_id = self.client.sf.getQueryService().projection(\
                "select i.id from Image i join i.pixels p where p.id = :id",
                omero.sys.ParametersI().addId(pix_ids[0]))[0][0].val
        exporter = self.client.sf.createExporter()
        exporter.addImage(image_id)
        length = exporter.generateTiff()
        offset = 0
        while True:
            rv = exporter.read(offset, 1000*1000)
            if not rv:
                break
            rv = rv[:min(1000*1000, length - offset)]
            offset += len(rv)

    def test6713(self):
        """
        Tests that a big image will not be exportable.
        """
        pix = self.bigimage()
        exporter = self.client.sf.createExporter()
        exporter.addImage(pix.getImage().id.val)
        self.assertRaises(omero.ApiUsageException, exporter.generateTiff)

if __name__ == '__main__':
    unittest.main()
