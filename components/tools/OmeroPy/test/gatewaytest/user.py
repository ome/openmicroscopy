#!/usr/bin/env python

"""
   gateway tests - Users

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import omero

import gatewaytest.library as lib

class UserTest (lib.GTest):
    def testUsers (self):
        self.loginAsUser()
        # Try reconnecting without disconnect
        self._has_connected = False
        self.doConnect()
        self.loginAsAuthor()
        self.loginAsAdmin()

    def XtestSaveAs (self):
        for u in (self.AUTHOR, self.ADMIN):
            if u == self.ADMIN:
                print "loginAsAdmin"
            else:
                print "loginAsAuthor"
            self.doLogin(u)
            # Test image should be owned by author
            image = self.getTestImage()
            self.assertEqual(image.getOwnerOmeName(), self.AUTHOR.name)
            # Create some object
            param = omero.sys.Parameters()
            param.map = {'ns': omero.rtypes.rstring('weblitz.UserTest.testSaveAs')}
            ann = self.gateway.getQueryService().findAllByQuery('from CommentAnnotation as a where a.ns=:ns', param)
            self.assertEqual(len(ann), 0)
            ann = omero.gateway.CommentAnnotationWrapper(conn=self.gateway)
            ann.setNs(param.map['ns'].val)
            ann.setValue('foo')
            ann.saveAs(image.getDetails())
            try:
                ann2 = self.gateway.getQueryService().findAllByQuery('from CommentAnnotation as a where a.ns=:ns', param)
                self.assertEqual(len(ann2), 1)
                self.assertEqual(omero.gateway.CommentAnnotationWrapper(self.gateway, ann2[0]).getOwnerOmeName(), self.AUTHOR.name)
            finally:
                self.gateway.getUpdateService().deleteObject(ann._obj)

if __name__ == '__main__':
    unittest.main()
