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
import omero
from omeroweb.webclient import webclient_gateway  # NOQA
from omero.gateway.utils import toBoolean
import library as lib


class TestClientConfig(lib.ITest):

    @pytest.mark.parametrize("enabled",
                             ["foo", "", "False", "false",
                              "True", "true", "", None])
    def testOrphansEnabledSetting(self, enabled):
        """
        Tests conn.getOrphanedContainerSettings()
        """
        if enabled is None:
            enabled = self.root.sf.getConfigService() \
                .getConfigDefaults()['omero.client.ui.tree.orphans.enabled']
        self.root.sf.getConfigService().setConfigValue(
            "omero.client.ui.tree.orphans.enabled", enabled)
        conn = omero.gateway.BlitzGateway(client_obj=self.client)
        orphans = conn.getOrphanedContainerSettings()
        assert orphans['enabled'] == toBoolean(enabled)

    @pytest.mark.parametrize("name",
                             ["Trash", "", None])
    def testOrphansNameSetting(self, name):
        """
        Tests conn.getOrphanedContainerSettings()
        """
        if name is None:
            name = self.root.sf.getConfigService() \
                .getConfigDefaults()['omero.client.ui.tree.orphans.name']
        self.root.sf.getConfigService().setConfigValue(
            "omero.client.ui.tree.orphans.name", name)
        conn = omero.gateway.BlitzGateway(client_obj=self.client)
        orphans = conn.getOrphanedContainerSettings()
        assert orphans['name'] == name

    @pytest.mark.parametrize("description",
                             ["Description", "", None])
    def testOrphansDescriptionSetting(self, description):
        """
        Tests conn.getOrphanedContainerSettings()
        """
        if description is None:
            description = self.root.sf.getConfigService() \
                .getConfigDefaults()[
                    'omero.client.ui.tree.orphans.description']
        self.root.sf.getConfigService().setConfigValue(
            "omero.client.ui.tree.orphans.description", description)

        conn = omero.gateway.BlitzGateway(client_obj=self.client)
        orphans = conn.getOrphanedContainerSettings()
        assert orphans['description'] == description
