#!/usr/bin/env python

"""
   Integration test for delete testing

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import integration.library as lib
import omero
import omero.callbacks

class TestDelete(lib.ITest):

    def testBasicUsage(self):
        img = omero.model.ImageI()
        img.name = omero.rtypes.rstring("delete test")
        img.acquisitionDate = omero.rtypes.rtime(0)
        tag = omero.model.TagAnnotationI()
        img.linkAnnotation( tag )

        img = self.client.sf.getUpdateService().saveAndReturnObject( img )

        command = omero.api.delete.DeleteCommand("/Image", img.id.val, None)
        handle = self.client.sf.getDeleteService().queueDelete([command])
        callback = omero.callbacks.DeleteCallbackI(self.client, handle)
        errors = None
        count = 10
        while errors is None:
            errors = callback.block(500)
            count -= 1
            self.assert_( count != 0 )
        self.assertEquals(0, errors)

if __name__ == '__main__':
    unittest.main()
