#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011-2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
Integration test focused on the omero.api.IUpdate interface.
"""

import library as lib
import omero
import pytest


class TestIUpdate(lib.ITest):
    """
    Basic test of all IUpdate functionality
    """

    def tags(self, count=3):
        return [omero.model.TagAnnotationI() for x in range(count)]

    def testSaveArray(self):
        """
        See ticket:6870
        """
        tags = self.tags()
        self.update.saveArray(tags)

    def testSaveCollection(self):
        """
        See ticket:6870
        """
        tags = self.tags()
        self.update.saveCollection(tags)

    def testExternalInfoOnCreation(self):
        ds = self.mkdataset(True)
        ds = self.update.saveAndReturnObject(ds)
        assert ds.details.externalInfo
        self.assert_type(ds, "test")

    def testExternalInfoAfterCreationTransient(self):
        ds = self.mkdataset(False)
        ds = self.update.saveAndReturnObject(ds)
        assert not ds.details.externalInfo
        ds.details.externalInfo = self.mkinfo("late")
        ds = self.update.saveAndReturnObject(ds)
        assert ds.details.externalInfo
        self.assert_type(ds, "late")

    def testExternalInfoAfterCreationManaged(self):
        ds = self.mkdataset(False)
        ds = self.update.saveAndReturnObject(ds)
        assert not ds.details.externalInfo
        info = self.mkinfo("late")
        info = self.update.saveAndReturnObject(info)
        ds.details.externalInfo = info
        ds = self.update.saveAndReturnObject(ds)
        assert ds.details.externalInfo
        self.assert_type(ds, "late")

    def testExternalInfoNewInstance(self):
        ds = self.mkdataset(True)
        ds = self.update.saveAndReturnObject(ds)
        info = self.mkinfo(type="updated")
        ds.details.externalInfo = info
        ds = self.update.saveAndReturnObject(ds)
        self.assert_type(ds, "updated")

    def testExternalInfoNullInstance(self):
        ds = self.mkdataset(True)
        ds = self.update.saveAndReturnObject(ds)
        ds.details.externalInfo = None
        ds = self.update.saveAndReturnObject(ds)
        self.assert_type(ds, None)

    def testExternalInfoUpdateInstance(self):
        ds = self.mkdataset(True)
        ds = self.update.saveAndReturnObject(ds)
        self.assert_type(ds, "test")
        info = ds.details.externalInfo
        info.entityType = omero.rtypes.rstring("updated")
        ds = self.update.saveAndReturnObject(ds)
        # This is still disallowed since the ExternalInfo
        # object itself is immutable (it has no update_id
        # column).
        with pytest.raises(Exception):
            self.assert_type(ds, "updated")

    # Helpers

    def reload(self, ds):
        return self.query.findByQuery((
            "select ds from Dataset ds "
            "left outer join fetch ds.details.externalInfo "
            "where ds.id = :id"), omero.sys.ParametersI().addId(ds.id))

    def mkdataset(self, info):
        ds = omero.model.DatasetI()
        ds.name = omero.rtypes.rstring("testExternalInfo")
        if info:
            ds.details.externalInfo = self.mkinfo()
        return ds

    def mkinfo(self, type="test"):
        info = omero.model.ExternalInfoI()
        info.entityType = omero.rtypes.rstring(type)
        info.entityId = omero.rtypes.rlong(1)
        return info

    def assert_type(self, ds, value):
        if value is None:
            assert ds.details.externalInfo is None
        else:
            assert ds.details.externalInfo.entityType.val == value
        ds = self.reload(ds)
        if value is None:
            assert ds.details.externalInfo is None
        else:
            assert ds.details.externalInfo.entityType.val == value
        return ds

    def incr(self, ds):
        if ds.version is None:
            ds.version = omero.rtypes.rint(0)
        else:
            i = ds.version.val
            ds.version = omero.rtypes.rint(i+1)
