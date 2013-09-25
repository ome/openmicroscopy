#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test focused on the omero.api.IQuery interface
   a running server.

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest, time
import test.integration.library as lib
import omero
from omero.rtypes import *

class TestQuery(lib.ITest):

    # ticket:1849
    def testGetPixelsCount(self):
        q = self.root.sf.getQueryService()
        a = self.root.sf.getAdminService()
        groups = a.lookupGroups()
        for group in groups:
            rtypeseqseq = q.projection("""
            select p.pixelsType.value, sum(cast(p.sizeX as long) * p.sizeY * p.sizeZ * p.sizeT * p.sizeC)
            from Pixels p group by p.pixelsType.value
            """, None, {"omero.group":str(group.id.val)})
            rv = unwrap(rtypeseqseq)
            as_map = dict()
            for obj_array in rv:
                as_map[obj_array[0]] = obj_array[1]
            if len(as_map) > 0:
                print "Group %s: %s" % (group.id.val, as_map)

if __name__ == '__main__':
    unittest.main()
