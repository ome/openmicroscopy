#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   gateway tests - Object Wrappers

   Copyright 2009-2015 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

   pytest fixtures used as defined in conftest.py:
   - gatewaywrapper
   - author_testimg

"""

import omero
import pytest


class TestWrapper(object):

    def setUp(self, gatewaywrapper):
        super(TestWrapper, self).setUp()
        gatewaywrapper.loginAsAuthor()
        self.TESTIMG = gatewaywrapper.getTestImage()

    def testAllObjectsWrapped(self, author_testimg):
        """
        Blitz object wrapper should ensure that all values returned are also
        wrapped (or are primative values)
        """
        image = author_testimg
        pixels = image.getPrimaryPixels()
        instrument = image.getInstrument()
        assert isinstance(instrument, omero.gateway.BlitzObjectWrapper)
        assert not hasattr(image.getArchived(), 'val'), \
            "Shouldn't return rtype"
        assert not hasattr(image.getAcquisitionDate(), 'val'), \
            "Shouldn't return rtype"
        assert not hasattr(image.sizeOfPixels(), 'val'), \
            "Non 'get' methods shouldn't return rtype either"
        assert isinstance(
            image.getInstrument(), omero.gateway.InstrumentWrapper), \
            "Should return InstrumentWrapper"
        assert isinstance(pixels, omero.gateway.BlitzObjectWrapper), \
            "Should return a BlitzObjectWrapper"

        # 'get' methods should wrap model objects in BlitzObjectWrapper -
        # allowing lazy loading
        assert isinstance(
            image.getFormat(), omero.gateway.BlitzObjectWrapper), \
            "Should return a BlitzObjectWrapper"
        format = image.getFormat()
        assert format.value == "Deltavision", \
            "BlitzObjectWrapper should lazy-load the value"
        assert not hasattr(format.value, 'val'), "Shouldn't return rtype"
        assert not hasattr(format.getValue(), 'val'), "Shouldn't return rtype"
        # direct access of the same model object shouldn't wrap
        assert isinstance(image.format, omero.model.FormatI), \
            "Shouldn't wrap directly-accessed objects"
        assert hasattr(image.format.id, 'val'), "Model object access"
        # 'get' methods where there isn't a similarly-named attribute also
        # shouldn't wrap
        assert isinstance(image.getPixels(0), omero.model.PixelsI), \
            "Shouldn't wrap: No 'pixels' attribute"
        # Don't accidentally wrap data structures
        assert isinstance(image.copyPixels(), list), "Shouldn't wrap lists"

    def testProjectWrapper(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        p = gatewaywrapper.getTestProject()
        pid = p.getId()
        gid = p.getDetails().getGroup().getId()
        m = p.simpleMarshal()
        assert m['name'] == p.getName()
        assert m['description'] == p.getDescription()
        assert m['id'] == p.getId()
        assert m['type'] == p.OMERO_CLASS
        assert 'parents' not in m
        m = p.simpleMarshal(parents=True)
        assert m['name'] == p.getName()
        assert m['description'] == p.getDescription()
        assert m['id'] == p.getId()
        assert m['type'] == p.OMERO_CLASS
        assert m['parents'] == []
        m = p.simpleMarshal(xtra={'childCount': None})
        assert m['name'] == p.getName()
        assert m['description'] == p.getDescription()
        assert m['id'] == p.getId()
        assert m['type'] == p.OMERO_CLASS
        assert 'parents' not in m
        assert m['child_count'] == p.countChildren_cached()
        # Verify canOwnerWrite
        gatewaywrapper.loginAsAdmin()
        gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
        chmod = omero.cmd.Chmod2(targetObjects={'ExperimenterGroup': [gid]})
        p = gatewaywrapper.gateway.getObject('project', pid)
        perms = str(p.getDetails().getGroup().getDetails().permissions)

        # try making the group writable by owner
        chmod.permissions = 'rw' + perms[2:]
        gatewaywrapper.gateway.c.submit(chmod)
        p = gatewaywrapper.gateway.getObject('project', pid)
        assert p.canOwnerWrite() is True

        # try making the group not writable by owner
        chmod.permissions = 'r-' + perms[2:]
        gatewaywrapper.gateway.c.submit(chmod)
        p = gatewaywrapper.gateway.getObject('project', pid)
        assert p.canOwnerWrite() is False

        # put the group back as it was
        chmod.permissions = perms
        gatewaywrapper.gateway.c.submit(chmod)

    def testDatasetWrapper(self, gatewaywrapper, author_testimg):
        # author_testimg above is only included to populate the dataset
        gatewaywrapper.loginAsAuthor()
        d = gatewaywrapper.getTestDataset()
        # first call to count_cached should calculate and store
        assert d.countChildren_cached() == 1
        pm = d.getParent().simpleMarshal()
        m = d.simpleMarshal()
        assert m['name'] == d.getName()
        assert m['description'] == d.getDescription()
        assert m['id'] == d.getId()
        assert m['type'] == d.OMERO_CLASS
        assert 'parents' not in m
        m = d.simpleMarshal(parents=True)
        assert m['name'] == d.getName()
        assert m['description'] == d.getDescription()
        assert m['id'] == d.getId()
        assert m['type'] == d.OMERO_CLASS
        assert m['parents'] == [pm]
        m = d.simpleMarshal(xtra={'childCount': None})
        assert m['name'] == d.getName()
        assert m['description'] == d.getDescription()
        assert m['id'] == d.getId()
        assert m['type'] == d.OMERO_CLASS
        assert 'parents' not in m
        assert m['child_count'] == d.countChildren_cached()
        # Do an extra check on listParents
        pm_multi = d.getParent()
        assert d.listParents()[0] == pm_multi

    def testExperimenterWrapper(self, gatewaywrapper):
        gatewaywrapper.loginAsAdmin()
        e = gatewaywrapper.gateway.getObject(
            "Experimenter", attributes={'omeName': gatewaywrapper.USER.name})
        assert e.getName() == gatewaywrapper.USER.name

    def testDetailsWrapper(self, gatewaywrapper, author_testimg):
        img = author_testimg
        d = img.getDetails()
        assert d.getOwner().omeName == gatewaywrapper.AUTHOR.name
        assert d.getGroup().name == \
            img.getProject().getDetails().getGroup().name

    def testSetters(self, gatewaywrapper):
        """
        Verify the setters that coerce values into blitz friendly rtypes.
        """
        gatewaywrapper.loginAsAuthor()
        p = gatewaywrapper.getTestProject()
        n = p.getName()
        p.setName('some name')
        assert p.getName() == 'some name'
        # we have not saved, but just in case revert it
        p.setName(n)
        assert p.getName() == n
        # Trying for something that does not exist must raise
        pytest.raises(AttributeError, getattr, self,
                      'something_wild_that_does_not_exist')

    def testOther(self):
        p = omero.gateway.ProjectWrapper()
        assert repr(p) is not None
