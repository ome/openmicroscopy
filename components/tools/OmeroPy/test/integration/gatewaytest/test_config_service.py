#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
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
