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


def testInterpolateSetting(gatewaywrapper):
    """
    Tests conn.getInterpolateSetting(), setting the value as Admin and
    testing the output as a regular user.
    """
    gatewaywrapper.loginAsAdmin()
    configService = gatewaywrapper.gateway.getConfigService()
    configService.setConfigValue("omero.client.viewer.interpolate_pixels", "true")

    gatewaywrapper.loginAsAuthor()
    assert gatewaywrapper.gateway.getInterpolateSetting()

    gatewaywrapper.loginAsAdmin()
    configService = gatewaywrapper.gateway.getConfigService()
    configService.setConfigValue("omero.client.viewer.interpolate_pixels", "false")

    gatewaywrapper.loginAsAuthor()
    assert not gatewaywrapper.gateway.getInterpolateSetting()
