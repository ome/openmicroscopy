#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014-2017 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.


from test.integration.clitest.cli import CLITest, RootCLITest
from omero.cli import NonZeroReturnCode
from omero.cmd import Delete2

import omero.plugins.admin
import pytest
import omero
import omero.gateway
import os.path

all_grps = {'omero.group': '-1'}


class TestCleanse(CLITest):

    def setup_method(self, method):
        super(TestCleanse, self).setup_method(method)
        self.cli.register("admin", omero.plugins.admin.AdminControl, "TEST")
        self.args += ["admin", "cleanse"]

    def testCleanseAdminOnly(self, capsys):
        """Test cleanse is admin-only"""
        config_service = self.root.sf.getConfigService()
        data_dir = config_service.getConfigValue("omero.data.dir")
        self.args += [data_dir]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        assert err.endswith("SecurityViolation: Admins only!\n")


class TestCleanseRoot(RootCLITest):

    def setup_method(self, method):
        super(TestCleanseRoot, self).setup_method(method)
        self.cli.register("admin", omero.plugins.admin.AdminControl, "TEST")
        self.import_args = self.args
        self.args += ["admin", "cleanse"]

    def testCleanseBasic(self, capsys):
        """Test cleanse works for root with expected output"""
        config_service = self.root.sf.getConfigService()
        data_dir = config_service.getConfigValue("omero.data.dir")
        self.args += [data_dir]
        self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        output_string_start = "Removing empty directories from...\n "
        mrepo_dir = config_service.getConfigValue("omero.managed.dir")
        output_string = output_string_start + mrepo_dir.replace("//", "/")
        assert output_string in out

    def testCleanseNonsenseName(self, tmpdir, capsys):
        """Test cleanse removes a file when originalFile"""
        """has nonsensical name"""
        name = "nonsensical"
        images = self.import_fake_file()
        ids = [image.id.val for image in images]
        fileset = self.get_fileset(images)
        fileset_id = fileset.getId()
        params = omero.sys.ParametersI()
        params.addId(fileset_id)
        query = ("select details.group.id, originalFile.id, "
                 "originalFile.path from FilesetEntry where fileset.id = :id")
        queryService = self.root.sf.getQueryService()
        result = queryService.projection(query, params, all_grps)
        group_id = result[0][0].getValue()
        group_ctx = {'omero.group': str(group_id)}
        orig_file_id = result[0][1].getValue()
        path_in_mrepo = result[0][2].getValue()
        query = 'from OriginalFile o where o.id = :id'
        params_file = omero.sys.ParametersI()
        params_file.addId(orig_file_id)
        orig_file = queryService.findByQuery(query, params_file, group_ctx)
        orig_file_name = orig_file.getName().getValue()
        config_service = self.root.sf.getConfigService()
        mrepo_dir = config_service.getConfigValue("omero.managed.dir")
        path = mrepo_dir + '/' + path_in_mrepo
        path = path.replace("//", "/")
        assert os.path.exists(path)
        orig_file_path = path + '/' + orig_file_name
        orig_file_path = orig_file_path.replace("//", "/")
        assert os.path.isfile(orig_file_path)
        orig_file.setName(omero.rtypes.rstring(name))
        update_service = self.root.sf.getUpdateService()
        update_service.saveAndReturnObject(orig_file, group_ctx)
        query = ("select o from FilesetJobLink l "
                 "join l.parent as fs join l.child as j "
                 "join j.originalFileLinks l2 join l2.child as o "
                 "where fs.id = :id and "
                 "o.mimetype = 'application/omero-log-file'")
        logfile = queryService.findByQuery(query, params, group_ctx)
        logfile.getName().getValue()

        command = Delete2(targetObjects={"Image": ids})
        handle = self.client.sf.submit(command)
        self.wait_on_cmd(self.client, handle)
        assert os.path.exists(path)
        assert os.path.isfile(orig_file_path)

        data_dir = config_service.getConfigValue("omero.data.dir")
        self.args += [data_dir]
        self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        assert not os.path.isfile(orig_file_path)
        assert not os.path.exists(path)
