#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Copyright (C) 2016-2018 University of Dundee & Open Microscopy Environment.
                      All Rights Reserved.
   Copyright 2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

   pytest fixtures used as defined in conftest.py:
   - gatewaywrapper

"""

import pytest
from omero.gateway.utils import toBoolean


@pytest.mark.parametrize("interpolate",
                         ["foo", "False", "false",
                          "True", "true", "", None])
def testInterpolateSetting(gatewaywrapper, interpolate):
    """
    Tests conn.getInterpolateSetting(), setting the value as Admin and
    testing the output as a regular user.
    """
    gatewaywrapper.loginAsAdmin()
    if interpolate is None:
        interpolate = gatewaywrapper.gateway.getConfigService() \
            .getConfigDefaults()['omero.client.viewer.interpolate_pixels']
    try:
        gatewaywrapper.gateway.getConfigService().setConfigValue(
            "omero.client.viewer.interpolate_pixels", interpolate)
        gatewaywrapper.loginAsAuthor()
        inter = gatewaywrapper.gateway.getInterpolateSetting()
        assert inter == toBoolean(interpolate)
    finally:
        gatewaywrapper.loginAsAdmin()
        d = gatewaywrapper.gateway.getConfigService() \
            .getConfigDefaults()['omero.client.viewer.interpolate_pixels']
        gatewaywrapper.gateway.getConfigService().setConfigValue(
            "omero.client.viewer.interpolate_pixels", d)


def testReadOnlyConsistentResults(gatewaywrapper):
    """
    Test that 'db' and 'repo' compose the queryable subsystems among the
    omero.cluster.read_only.runtime.* properties from getReadOnlyStatus and
    that isAnyReadOnly returns results consistent with their property
    values.
    """
    gatewaywrapper.loginAsAuthor()
    gateway = gatewaywrapper.gateway

    read_only_status = gateway.getReadOnlyStatus()
    assert set(read_only_status.keys()) == set(['db', 'repo'])

    is_read_only_db = read_only_status['db']
    is_read_only_repo = read_only_status['repo']
    assert gateway.isAnyReadOnly() is False
    assert gateway.isAnyReadOnly('db') == is_read_only_db
    assert gateway.isAnyReadOnly('repo') == is_read_only_repo
    assert gateway.isAnyReadOnly('db', 'repo') == \
        is_read_only_db or is_read_only_repo


def testReadOnlyBackwardCompatibility(gatewaywrapper):
    """
    Test that in the absence of corresponding
    omero.cluster.read_only.runtime.* properties as from a pre-5.4.6 server
    the queryable subsystems are reported as being read-write.
    """
    gatewaywrapper.loginAsAuthor()
    gateway = gatewaywrapper.gateway

    absent_subsystem = 'absent subsystem'
    assert absent_subsystem not in gateway.getReadOnlyStatus()
    assert gateway.isAnyReadOnly(absent_subsystem) is False
