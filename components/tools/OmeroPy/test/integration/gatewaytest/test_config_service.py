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


@pytest.mark.parametrize("enabled",
                         ["foo", "", "False", "false",
                          "True", "true", "", None])
def testOrphansEnabledSetting(gatewaywrapper, enabled):
    """
    Tests getOrphanedImagesSettings()
    """
    gatewaywrapper.loginAsAdmin()
    if enabled is None:
        enabled = gatewaywrapper.gateway.getConfigService() \
            .getConfigDefaults()['omero.client.ui.tree.orphans.enabled']
    gatewaywrapper.gateway.getConfigService().setConfigValue(
        "omero.client.ui.tree.orphans.enabled", enabled)

    gatewaywrapper.loginAsAuthor()
    orphans = gatewaywrapper.gateway.getOrphanedImagesSettings()
    assert orphans['enabled'] == toBoolean(enabled)


@pytest.mark.parametrize("name",
                         ["Trash", "", None])
def testOrphansNameSetting(gatewaywrapper, name):
    """
    Tests conn.getOrphanedImagesSettings()
    """
    gatewaywrapper.loginAsAdmin()
    if name is None:
        name = gatewaywrapper.gateway.getConfigService() \
            .getConfigDefaults()['omero.client.ui.tree.orphans.name']
    gatewaywrapper.gateway.getConfigService().setConfigValue(
        "omero.client.ui.tree.orphans.name", name)

    gatewaywrapper.loginAsAuthor()
    orphans = gatewaywrapper.gateway.getOrphanedImagesSettings()
    assert orphans['name'] == name


@pytest.mark.parametrize("description",
                         ["Description", "", None])
def testOrphansDescriptionSetting(gatewaywrapper, description):
    """
    Tests conn.getOrphanedImagesSettings()
    """
    gatewaywrapper.loginAsAdmin()
    if description is None:
        description = gatewaywrapper.gateway.getConfigService() \
            .getConfigDefaults()[
                'omero.client.ui.tree.orphans.description']
    gatewaywrapper.gateway.getConfigService().setConfigValue(
        "omero.client.ui.tree.orphans.description", description)

    gatewaywrapper.loginAsAuthor()
    orphans = gatewaywrapper.gateway.getOrphanedImagesSettings()
    assert orphans['description'] == description


def testInterpolateSetting(gatewaywrapper):
    """
    Tests conn.getInterpolateSetting(), setting the value as Admin and
    testing the output as a regular user.
    """
    gatewaywrapper.loginAsAdmin()
    configService = gatewaywrapper.gateway.getConfigService()

    defaluts = configService.getConfigDefaults()
    defaultValue = defaluts["omero.client.viewer.interpolate_pixels"]

    try:
        configService.setConfigValue(
            "omero.client.viewer.interpolate_pixels", "false")

        gatewaywrapper.loginAsAuthor()
        assert not gatewaywrapper.gateway.getInterpolateSetting()

        gatewaywrapper.loginAsAdmin()
        configService = gatewaywrapper.gateway.getConfigService()
        configService.setConfigValue(
            "omero.client.viewer.interpolate_pixels", "true")

        gatewaywrapper.loginAsAuthor()
        assert gatewaywrapper.gateway.getInterpolateSetting()

    finally:
        # try/finally to make sure that we
        # set back to default value to clean up
        gatewaywrapper.loginAsAdmin()
        configService = gatewaywrapper.gateway.getConfigService()
        configService.setConfigValue(
            "omero.client.viewer.interpolate_pixels", defaultValue)
