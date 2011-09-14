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

    def testSaveAs (self):
        for u in (self.AUTHOR, self.ADMIN):
            if u == self.ADMIN:
                print "loginAsAdmin"
            else:
                print "loginAsAuthor"
            # Test image should be owned by author
            self.loginAsAuthor()
            image = self.getTestImage()

            # Now login as author or admin
            self.doLogin(u)
            self.assertEqual(image.getOwnerOmeName(), self.AUTHOR.name)
            # Create some object
            param = omero.sys.Parameters()
            param.map = {'ns': omero.rtypes.rstring('weblitz.UserTest.testSaveAs')}
            anns = self.gateway.getQueryService().findAllByQuery('from CommentAnnotation as a where a.ns=:ns', param)
            self.assertEqual(len(anns), 0)
            ann = omero.gateway.CommentAnnotationWrapper(conn=self.gateway)
            ann.setNs(param.map['ns'].val)
            ann.setValue('foo')
            ann.saveAs(image.getDetails())

            # Annotations are owned by author
            self.loginAsAuthor()
            try:
                anns = self.gateway.getQueryService().findAllByQuery('from CommentAnnotation as a where a.ns=:ns', param)
                self.assertEqual(len(anns), 1)
                self.assertEqual(omero.gateway.CommentAnnotationWrapper(self.gateway, anns[0]).getOwnerOmeName(), self.AUTHOR.name)
            finally:
                self.gateway.getUpdateService().deleteObject(ann._obj)
                anns = self.gateway.getQueryService().findAllByQuery('from CommentAnnotation as a where a.ns=:ns', param)
                self.assertEqual(len(anns), 0)

if __name__ == '__main__':
    unittest.main()
