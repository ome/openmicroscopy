#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Simple unit test which makes various calls on the code
   generated model.

   Copyright 2007-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import pytest
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
from omero.rtypes import rbool
from omero.rtypes import rlong
from omero.rtypes import rstring
from omero.rtypes import rtime


class TestProxyString(object):

    @pytest.mark.parametrize("data", (
        ("", None, None, None),
        ("1", None, None, None),
        ("Image", None, None, None),
        ("ImageI", None, None, None),
        ("Image:1", None, ImageI, 1),
        ("ImageI:1", None, ImageI, 1),
        ("ImageI:1", "ImageI", ImageI, 1),
        ("Image:1", "ImageI", ImageI, 1),
        ("1", "ImageI", ImageI, 1),
        ("1", "Image", ImageI, 1),
    ))
    def testAll(self, data):
        source = data[0]
        default = data[1]
        type = data[2]
        id = data[3]
        err = (type is None and id is None)
        try:
            obj = omero.proxy_to_instance(source, default)
            assert isinstance(obj, type)
            assert obj.id == id
            assert not obj.loaded
            if err:
                assert False, "should have raised"
        except Exception, e:
            if not err:
                assert "should not have raised", e


class TestModel(object):

    def testVirtual(self):
        img = ImageI()
        imgI = ImageI()
        img.unload()
        imgI.unload()

    def testUnloadCollections(self):
        pix = PixelsI()
        assert pix.sizeOfSettings() >= 0
        pix.unloadCollections()
        assert pix.sizeOfSettings() < 0

    def testSimpleCtor(self):
        img = ImageI()
        assert img.isLoaded()
        assert img.sizeOfPixels() >= 0

    def testUnloadedCtor(self):
        img = ImageI(rlong(1), False)
        assert not img.isLoaded()
        try:
            assert img.sizeOfDatasetLinks() < 0
            assert False, "Should throw"
        except:
            # Is true, but can't test it.
            pass

    def testUnloadCheckPtr(self):
        img = ImageI()
        assert img.isLoaded()
        assert img.getDetails()  # details are auto instantiated
        assert not img.getName()  # no other single-valued field is
        img.unload()
        assert not img.isLoaded()
        pytest.raises(omero.UnloadedEntityException, img.getDetails)

    def testUnloadField(self):
        img = ImageI()
        assert img.getDetails()
        img.unloadDetails()
        assert not img.getDetails()

    def testSequences(self):
        img = ImageI()
        assert img.sizeOfAnnotationLinks() >= 0
        img.linkAnnotation(None)
        img.unload()
        try:
            assert not img.sizeOfAnnotationLinks() >= 0
            assert len(img.copyAnnotationLinks()) == 0
            assert False, "can't reach here"
        except:
            # These are true, but can't be tested
            pass

    def testAccessors(self):
        name = rstring("name")
        img = ImageI()
        assert not img.getName()
        img.setName(name)
        assert img.getName()
        name = img.getName()
        assert name.val == "name"
        assert name == name

        img.setName(rstring("name2"))
        assert img.getName().val == "name2"
        assert img.getName()

        img.unload()
        try:
            assert not img.getName()
            assert False, "should fail"
        except:
            # Is true, but cannot test
            pass

    def testUnloadedAccessThrows(self):
        unloaded = ImageI(rlong(1), False)
        pytest.raises(omero.UnloadedEntityException, unloaded.getName)

    def testIterators(self):
        d = DatasetI()
        image = ImageI()
        image.linkDataset(d)
        it = image.iterateDatasetLinks()
        count = 0
        for i in it:
            count += 1
        assert count == 1

    def testClearSet(self):
        img = ImageI()
        assert img.sizeOfPixels() >= 0
        img.addPixels(PixelsI())
        assert 1 == img.sizeOfPixels()
        img.clearPixels()
        assert img.sizeOfPixels() >= 0
        assert 0 == img.sizeOfPixels()

    def testUnloadSet(self):
        img = ImageI()
        assert img.sizeOfPixels() >= 0
        img.addPixels(PixelsI())
        assert 1 == img.sizeOfPixels()
        img.unloadPixels()
        assert img.sizeOfPixels() < 0
        # Can't check size assert 0==img.sizeOfPixels()

    def testRemoveFromSet(self):
        pix = PixelsI()
        img = ImageI()
        assert img.sizeOfPixels() >= 0

        img.addPixels(pix)
        assert 1 == img.sizeOfPixels()

        img.removePixels(pix)
        assert 0 == img.sizeOfPixels()

    def testLinkGroupAndUser(self):
        user = ExperimenterI()
        group = ExperimenterGroupI()
        link = GroupExperimenterMapI()

        link.id = rlong(1)
        link.link(group, user)
        user.addGroupExperimenterMap(link, False)
        group.addGroupExperimenterMap(link, False)

        count = 0
        for i in user.iterateGroupExperimenterMap():
            count += 1

        assert count == 1

    def testLinkViaLink(self):
        user = ExperimenterI()
        user.setFirstName(rstring("test"))
        user.setLastName(rstring("user"))
        user.setOmeName(rstring("UUID"))
        user.setLdap(rbool(False))

        # possibly setOmeName() and setOmeName(string) ??
        # and then don't need omero/types.h

        group = ExperimenterGroupI()
        # TODOuser.linkExperimenterGroup(group)
        link = GroupExperimenterMapI()
        link.parent = group
        link.child = user

    def testLinkingAndUnlinking(self):
        d = DatasetI()
        i = ImageI()

        d.linkImage(i)
        assert d.sizeOfImageLinks() == 1
        d.unlinkImage(i)
        assert d.sizeOfImageLinks() == 0

        d = DatasetI()
        i = ImageI()
        d.linkImage(i)
        assert i.sizeOfDatasetLinks() == 1
        i.unlinkDataset(d)
        assert d.sizeOfImageLinks() == 0

        d = DatasetI()
        i = ImageI()
        dil = DatasetImageLinkI()
        dil.link(d, i)
        d.addDatasetImageLink(dil, False)
        assert d.sizeOfImageLinks() == 1
        assert i.sizeOfDatasetLinks() == 0

    def testScriptJobHasLoadedCollections(self):
        s = ScriptJobI()
        assert s.sizeOfOriginalFileLinks() >= 0

    #
    # Python specific
    #

    def testGetAttrGood(self):
        i = ImageI()
        assert i.loaded
        assert i.isLoaded()
        assert not i.name
        i.name = rstring("name")
        assert i.name
        i.setName(None)
        assert not i.getName()
        i.copyAnnotationLinks()
        i.linkAnnotation(omero.model.BooleanAnnotationI())

    def testGetAttrBad(self):
        i = ImageI()

        def assign_loaded():
            i.loaded = False
        pytest.raises(AttributeError, assign_loaded)
        pytest.raises(AttributeError, lambda: i.foo)

        def assign_foo():
            i.foo = 1
        pytest.raises(AttributeError, assign_foo)
        pytest.raises(AttributeError, lambda: i.annotationLinks)
        pytest.raises(AttributeError, lambda: i.getAnnotationLinks())

        def assign_links():
            i.annotationLinks = []
        pytest.raises(AttributeError, assign_links)

    def testGetAttrSetAttrDetails(self):
        d = DetailsI()
        assert None == d.owner
        d.owner = ExperimenterI()
        assert d.owner
        d.owner = None
        assert None == d.owner
        d.ice_preMarshal()

    def testProxy(self):
        i = ImageI()
        pytest.raises(omero.ClientError, i.proxy)
        i = ImageI(5, False)
        i.proxy()

    def testId(self):
        i = ImageI(4)
        assert 4 == i.id.val

    def testOrderedCollectionsTicket2547(self):
        pixels = PixelsI()
        channels = [ChannelI() for x in range(3)]
        pixels.addChannel(channels[0])
        assert 1 == pixels.sizeOfChannels()
        old = pixels.setChannel(0, channels[1])
        assert old == channels[0]
        assert 1 == pixels.sizeOfChannels()

    def testWrappers(self):
        image = ImageI()
        info = image._field_info

        assert info.name.wrapper == rstring
        assert info.acquisitionDate.wrapper == rtime

        assert not info.name.nullable
        assert info.acquisitionDate.nullable

        image.setAcquisitionDate("1", wrap=True)
        assert type(image.acquisitionDate) == type(rtime(0))

        # The following causes pytest to fail!
        # image.setAcquisitionDate("", wrap=True)

        # Note: collections still don't work well
        if False:
            image.addPixels(PixelsI())
            image.setPixels(0, "Pixels:1", wrap=True)
            assert type(image.pixels) == omero.model.PixelsI

            image.setPixels(0, "Pixels", wrap=True)
            image.setPixels(0, "Unknown", wrap=True)

        link = DatasetImageLinkI()
        link.setParent("Dataset:1")
        link.setChild("Image:1")
