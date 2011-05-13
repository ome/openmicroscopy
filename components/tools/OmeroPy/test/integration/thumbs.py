#!/usr/bin/env python

"""
   Tests for the stateful RawPixelsStore service.

   Copyright 2011 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import omero
import unittest
import integration.library as lib

from omero.rtypes import rstring, rlong, rint
from omero.util.concurrency import get_event
from binascii import hexlify as hex

class TestThumbs(lib.ITest):

    def testRomioToPyramid(self):
        """
        Here we create a pixels that is not big,
        then modify its metadata so that it IS big,
        in order to trick the service into throwing
        us a MissingPyramidException.
        """
        pix = self.missing_pyramid()
        tb = self.client.sf.createThumbnailStore()
        tb.setPixelsId(pix.id.val)
        tb.resetDefaults()
        #tb.s
        try:
            buf = tb.getThumbnailDirect(rint(64), rint(64))
        finally:
            tb.close()

if __name__ == '__main__':
    unittest.main()
