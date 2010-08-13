#!/usr/bin/env python

"""
   Integration test which attempts to run Projections.py

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import integration.library as lib
import omero, tempfile, unittest, os
import omero.processor
from omero.rtypes import *

class TestProjection(lib.ITest):

    def testAllParamTypes(self):
        me = os.path.abspath(__file__)
        here = os.path.dirname(me)
        proj = os.path.join(here, "Projection.py")
        file = self.root.upload(proj, type="text/x-python")

        map = {}
        map["pixelsID"] = rlong(1)
        map["channelSet"] = rlist([robject(omero.model.ChannelI())])
        map["timeSet"] = rlist([rint(0)])
        map["zSectionSet"] = rlist([rint(1)])
        map["point1"] = rinternal(omero.Point(1,2))
        map["point2"] = rinternal(omero.Point(2,2))
        map["method"] = rstring("max")

        impl = omero.processor.usermode_processor(self.root)
        try:
            p = self.root.sf.getScriptService().runScript(file.id.val, map, None)
            process.wait()
            output = p.getResults(process)
            self.assert_( -1 == output.val["newPixelsID"].val )
        finally:
            impl.cleanup()

if __name__ == '__main__':
    unittest.main()
