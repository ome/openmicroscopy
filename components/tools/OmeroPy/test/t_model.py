#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Simple unit test which makes various calls on the code
   generated model.

   Copyright 2007 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import omero
import omero.clients
from omero_model_ChannelI import ChannelI
from omero_model_PixelsI import PixelsI
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_GroupExperimenterMapI import GroupExperimenterMapI
from omero_model_DatasetImageLinkI import DatasetImageLinkI
from omero_model_ScriptJobI import ScriptJobI
from omero_model_DetailsI import DetailsI
from omero_model_BooleanAnnotationI import BooleanAnnotationI
from omero_model_ImageAnnotationLinkI import ImageAnnotationLinkI
from omero.rtypes import *


class TestModel(unittest.TestCase):

    def testVirtual(self):
        img = ImageI()
        imgI = ImageI()
        img.unload()
        imgI.unload()

    def testUnloadCollections(self):
        pix = PixelsI()
        self.assert_( pix.sizeOfSettings() >= 0 )
        pix.unloadCollections()
        self.assert_( pix.sizeOfSettings() < 0 )

    def testSimpleCtor(self):
        img = ImageI()
        self.assert_( img.isLoaded() )
        self.assert_( img.sizeOfPixels() >= 0 )

    def testUnloadedCtor(self):
        img = ImageI(rlong(1),False)
        self.assert_( not img.isLoaded() )
        try:
            self.assert_( img.sizeOfDatasetLinks() < 0 )
            self.fail("Should throw")
        except:
            # Is true, but can't test it.
            pass

    def testUnloadCheckPtr(self):
        img = ImageI()
        self.assert_( img.isLoaded() )
        self.assert_( img.getDetails() ) # details are auto instantiated
        self.assert_( not img.getName() ) # no other single-valued field is
        img.unload()
        self.assert_( not img.isLoaded() )
        self.assertRaises( omero.UnloadedEntityException, img.getDetails )

    def testUnloadField(self):
        img = ImageI()
        self.assert_( img.getDetails() )
        img.unloadDetails()
        self.assert_( not img.getDetails() )

    def testSequences(self):
        img = ImageI()
        self.assert_( img.sizeOfAnnotationLinks() >= 0 )
        img.linkAnnotation(None)
        img.unload()
        try:
            self.assert_( not img.sizeOfAnnotationLinks() >= 0 )
            self.assert_( len(img.copyAnnotationLinks()) == 0 )
            self.fail("can't reach here")
        except:
            # These are true, but can't be tested
            pass

    def testAccessors(self):
        name = rstring("name")
        img = ImageI()
        self.assert_( not img.getName() )
        img.setName( name )
        self.assert_( img.getName() )
        name = img.getName()
        self.assert_( name.val == "name" )
        self.assert_( name == name )

        img.setName(rstring("name2"))
        self.assert_( img.getName().val == "name2" )
        self.assert_( img.getName() )

        img.unload()
        try:
            self.assert_( not img.getName() )
            self.fail("should fail")
        except:
            # Is true, but cannot test
            pass

    def testUnloadedAccessThrows(self):
        unloaded = ImageI(rlong(1),False)
        self.assertRaises( omero.UnloadedEntityException, unloaded.getName )

    def testIterators(self):
        d = DatasetI()
        image = ImageI()
        image.linkDataset(d)
        it = image.iterateDatasetLinks()
        count = 0
        for i in it:
            count += 1
        self.assert_( count == 1 )

    def testClearSet(self):
        img = ImageI()
        self.assert_( img.sizeOfPixels() >= 0 )
        img.addPixels( PixelsI() )
        self.assert_( 1==img.sizeOfPixels() )
        img.clearPixels()
        self.assert_( img.sizeOfPixels() >= 0 )
        self.assert_( 0==img.sizeOfPixels() )

    def testUnloadSet(self):
        img = ImageI()
        self.assert_( img.sizeOfPixels() >= 0 )
        img.addPixels( PixelsI() )
        self.assert_( 1==img.sizeOfPixels() )
        img.unloadPixels()
        self.assert_( img.sizeOfPixels() < 0 )
        # Can't check size self.assert_( 0==img.sizeOfPixels() )


    def testRemoveFromSet(self):
        pix = PixelsI()
        img = ImageI()
        self.assert_( img.sizeOfPixels() >= 0 )

        img.addPixels( pix )
        self.assert_( 1==img.sizeOfPixels() )

        img.removePixels( pix )
        self.assert_( 0==img.sizeOfPixels() )

    def testLinkGroupAndUser(self):
        user = ExperimenterI()
        group = ExperimenterGroupI()
        link = GroupExperimenterMapI()

        link.id = rlong(1)
        link.link(group,user)
        user.addGroupExperimenterMap( link, False )
        group.addGroupExperimenterMap( link, False )

        count = 0
        for i in user.iterateGroupExperimenterMap():
            count += 1

        self.assert_( count == 1 )

    def testLinkViaLink(self):
        user = ExperimenterI()
        user.setFirstName(rstring("test"))
        user.setLastName(rstring("user"))
        user.setOmeName(rstring("UUID"))
        
        # possibly setOmeName() and setOmeName(string) ??
        # and then don't need omero/types.h
        
        group = ExperimenterGroupI()
        # TODOuser.linkExperimenterGroup(group)
        link = GroupExperimenterMapI()
        link.parent = group
        link.child  = user

    def testLinkingAndUnlinking(self):
        d = DatasetI()
        i = ImageI()

        d.linkImage(i)
        self.assert_( d.sizeOfImageLinks() == 1 )
        d.unlinkImage(i)
        self.assert_( d.sizeOfImageLinks() == 0 )

        d = DatasetI()
        i = ImageI()
        d.linkImage(i)
        self.assert_( i.sizeOfDatasetLinks() == 1 )
        i.unlinkDataset(d)
        self.assert_( d.sizeOfImageLinks() == 0 )

        d = DatasetI()
        i = ImageI()
        dil = DatasetImageLinkI()
        dil.link(d,i)
        d.addDatasetImageLink(dil, False)
        self.assert_( d.sizeOfImageLinks() == 1 )
        self.assert_( i.sizeOfDatasetLinks() == 0 )

    def testScriptJobHasLoadedCollections(self):
        s = ScriptJobI()
        self.assert_( s.sizeOfOriginalFileLinks() >= 0 )

    #
    # Python specific
    #

    def testGetAttrGood(self):
       i = ImageI()
       self.assert_( i.loaded )
       self.assert_( i.isLoaded() )
       self.assert_( not i.name )
       i.name = rstring("name")
       self.assert_( i.name )
       i.setName( None )
       self.assert_( not i.getName() )
       i.copyAnnotationLinks()
       i.linkAnnotation( omero.model.BooleanAnnotationI() )

    def testGetAttrBad(self):
       i = ImageI()
       def assign_loaded():
            i.loaded = False
       self.assertRaises( AttributeError, assign_loaded )
       self.assertRaises( AttributeError, lambda: i.foo )
       def assign_foo():
            i.foo = 1
       self.assertRaises( AttributeError, assign_foo )
       self.assertRaises( AttributeError, lambda: i.annotationLinks )
       self.assertRaises( AttributeError, lambda: i.getAnnotationLinks() )
       def assign_links():
            i.annotationLinks = []
       self.assertRaises( AttributeError, assign_links)

    def testGetAttrSetAttrDetails(self):
        d = DetailsI()
        self.assert_( None == d.owner)
        d.owner = ExperimenterI()
        self.assert_( d.owner )
        d.owner = None
        self.assert_( None == d.owner)
        d.ice_preMarshal()

    def testProxy(self):
        i = ImageI()
        self.assertRaises(omero.ClientError, i.proxy)
        i = ImageI(5, False)
        i.proxy()

    def testId(self):
        i = ImageI(4)
        self.assertEquals(4, i.id.val)

    def testOrderedCollectionsTicket2547(self):
        pixels = PixelsI()
        channels = [ChannelI() for x in range(3)]
        pixels.addChannel(channels[0])
        self.assertEquals(1, pixels.sizeOfChannels())
        old = pixels.setChannel(0, channels[1])
        self.assertEquals(old, channels[0])
        self.assertEquals(1, pixels.sizeOfChannels())

if __name__ == '__main__':
    unittest.main()
