#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   conftest.py - py.test fixtures for gatewaytest

   Copyright 2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from omero.gateway.scripts.testdb_create import TestDBHelper, dbhelpers


import pytest


class GatewayWrapper (TestDBHelper):

    def __init__(self):
        super(GatewayWrapper, self).__init__()
        self.setUp(skipTestDB=False, skipTestImages=True)

    def createTestImg_generated(self):
        ds = self.getTestDataset()
        assert ds
        testimg = self.createTestImage(dataset=ds)
        return testimg


@pytest.fixture(scope='class')
def gatewaywrapper(request):
    """
    Returns a test helper gateway object.
    """
    g = GatewayWrapper()

    def fin():
        g.tearDown()
        dbhelpers.cleanup()
    request.addfinalizer(fin)
    return g


@pytest.fixture(scope='function')
def author_testimg_generated(request, gatewaywrapper):
    """
    logs in as Author and returns the test image, creating it first if needed.
    """
    gatewaywrapper.loginAsAuthor()
    rv = gatewaywrapper.createTestImg_generated()
    return rv


@pytest.fixture(scope='function')
def author_testimg_tiny(request, gatewaywrapper):
    """
    logs in as Author and returns the test image, creating it first if needed.
    """
    gatewaywrapper.loginAsAuthor()
    rv = gatewaywrapper.getTinyTestImage(autocreate=True)
    return rv


@pytest.fixture(scope='function')
def author_testimg_tiny2(request, gatewaywrapper):
    """
    logs in as Author and returns the test image, creating it first if needed.
    """
    gatewaywrapper.loginAsAuthor()
    rv = gatewaywrapper.getTinyTestImage2(autocreate=True)
    return rv


@pytest.fixture(scope='function')
def author_testimg(request, gatewaywrapper):
    """
    logs in as Author and returns the test image, creating it first if needed.
    """
    gatewaywrapper.loginAsAuthor()
    rv = gatewaywrapper.getTestImage(autocreate=True)
    return rv


@pytest.fixture(scope='function')
def author_testimg_bad(request, gatewaywrapper):
    """
    logs in as Author and returns the test image, creating it first if needed.
    """
    gatewaywrapper.loginAsAuthor()
    rv = gatewaywrapper.getBadTestImage(autocreate=True)
    return rv


@pytest.fixture(scope='function')
def author_testimg_big(request, gatewaywrapper):
    """
    logs in as Author and returns the test image, creating it first if needed.
    """
    gatewaywrapper.loginAsAuthor()
    rv = gatewaywrapper.getBigTestImage(autocreate=True)
    return rv


@pytest.fixture(scope='function')
def author_testimg_32float(request, gatewaywrapper):
    """
    logs in as Author and returns the float image, creating it first if needed.
    """
    gatewaywrapper.loginAsAuthor()
    rv = gatewaywrapper.get32FloatTestImage(autocreate=True)
    return rv
