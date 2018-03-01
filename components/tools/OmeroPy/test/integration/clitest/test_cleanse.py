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


from omero.testlib.cli import CLITest
from omero.cli import NonZeroReturnCode
from omero.cmd import Delete2

import omero.plugins.admin
import pytest
import os.path


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


class TestCleanseFullAdmin(CLITest):

    # make the user in this test a member of system group
    DEFAULT_SYSTEM = True
    # make the new member of system group to a Full Admin
    DEFAULT_PRIVILEGES = None

    def setup_method(self, method):
        super(TestCleanseFullAdmin, self).setup_method(method)
        self.cli.register("admin", omero.plugins.admin.AdminControl, "TEST")
        self.args += ["admin", "cleanse"]
        self.group_ctx = {'omero.group': str(self.group.id.val)}
        config_service = self.root.sf.getConfigService()
        self.mrepo_dir = config_service.getConfigValue("omero.managed.dir")
        self.data_dir = config_service.getConfigValue("omero.managed.dir")

    def make_path(self, path_in_mrepo, filename=None):
        if filename is None:
            path = self.mrepo_dir + '/' + path_in_mrepo
        else:
            path = self.mrepo_dir + '/' + path_in_mrepo + '/' + filename
        return path.replace("//", "/")

    def testCleanseBasic(self, capsys):
        """Test cleanse works for Full Admin with expected output"""
        self.args += [self.data_dir]
        self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        output_start = "Removing empty directories from...\n "
        output_string = output_start + self.mrepo_dir.replace("//", "/")
        assert output_string in out

    def testCleanseNonsenseName(self, capsys):
        """
        Test cleanse removes file on disk after OriginalFile
        name was changed to nonsense and its Image was deleted
        """
        # import image and retrieve the OriginalFile
        # (orig_file), its name and path
        image = self.import_fake_file()[0]
        fileset = self.get_fileset([image])
        params = omero.sys.ParametersI()
        params.addId(fileset.getId())
        q = ("select originalFile.path, originalFile.id "
             "from FilesetEntry where fileset.id = :id")
        queryService = self.root.sf.getQueryService()
        result = queryService.projection(q, params, self.group_ctx)
        path_in_mrepo = result[0][0].getValue()
        orig_file_path = self.make_path(path_in_mrepo)
        assert os.path.exists(orig_file_path)
        orig_file_id = result[0][1].getValue()
        orig_file = self.query.get("OriginalFile", orig_file_id)
        orig_file_name = orig_file.getName().getValue()
        orig_file_path_and_name = self.make_path(path_in_mrepo, orig_file_name)
        assert os.path.isfile(orig_file_path_and_name)

        # retrieve the logfile, its name and path
        q = ("select o from FilesetJobLink l "
             "join l.parent as fs join l.child as j "
             "join j.originalFileLinks l2 join l2.child as o "
             "where fs.id = :id and "
             "o.mimetype = 'application/omero-log-file'")
        logfile = queryService.findByQuery(q, params, self.group_ctx)
        logfile_name = logfile.getName().getValue()
        path_in_mrepo = logfile.getPath().getValue()
        logfile_path = self.make_path(path_in_mrepo)
        assert os.path.exists(logfile_path)
        logfile_path_and_name = self.make_path(path_in_mrepo, logfile_name)
        assert os.path.isfile(logfile_path_and_name)

        # change the names of original_file and logfile to nonsense
        name = "nonsensical"
        update_service = self.root.sf.getUpdateService()
        orig_file.setName(omero.rtypes.rstring(name))
        update_service.saveAndReturnObject(orig_file, self.group_ctx)
        logfile.setName(omero.rtypes.rstring(name))
        update_service.saveAndReturnObject(logfile, self.group_ctx)

        # run the cleanse command, which will not delete
        # the files on disk
        self.args += [self.data_dir]
        self.cli.invoke(self.args, strict=True)
        assert os.path.exists(orig_file_path)
        assert os.path.isfile(orig_file_path_and_name)
        assert os.path.exists(logfile_path)
        assert os.path.isfile(logfile_path_and_name)

        # delete the image, which will not delete
        # the files on disk because of the nonsensical name
        # of orig_file and logfile
        command = Delete2(targetObjects={"Image": [image.id.val]})
        handle = self.client.sf.submit(command)
        self.wait_on_cmd(self.client, handle)
        assert os.path.exists(orig_file_path)
        assert os.path.isfile(orig_file_path_and_name)
        assert os.path.exists(logfile_path)
        assert os.path.isfile(logfile_path_and_name)

        # run cleanse command again, which will now delete the
        # files on disk, the original file, the logfile
        # and their directories
        self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        assert not os.path.exists(orig_file_path)
        assert not os.path.isfile(orig_file_path_and_name)
        assert not os.path.exists(logfile_path)
        assert not os.path.isfile(logfile_path_and_name)


class TestCleanseRestrictedAdmin(CLITest):

    # make the user in this test a member of system group
    DEFAULT_SYSTEM = True
    # make the new member of system group to a Restricted
    # Admin with no privileges
    DEFAULT_PRIVILEGES = ()

    def setup_method(self, method):
        super(TestCleanseRestrictedAdmin, self).setup_method(method)
        self.cli.register("admin", omero.plugins.admin.AdminControl, "TEST")
        self.args += ["admin", "cleanse"]

    def test_cleanse_restricted_admin(self, capsys):
        """Test cleanse cannot be run by Restricted Admin"""
        config_service = self.root.sf.getConfigService()
        data_dir = config_service.getConfigValue("omero.data.dir")
        self.args += [data_dir]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        output_end = "SecurityViolation: Admin restrictions:"
        assert err.startswith(output_end)


class TestFixPyramids(CLITest):

    def setup_method(self, method):
        super(TestFixPyramids, self).setup_method(method)
        self.cli.register("admin", omero.plugins.admin.AdminControl, "TEST")
        self.args += ["admin", "fixpyramids"]

    def test_fixpyramids_admin_only(self, capsys):
        """Test fixpyramids is admin-only"""
        config_service = self.root.sf.getConfigService()
        data_dir = config_service.getConfigValue("omero.data.dir")
        self.args += [data_dir]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        assert err.endswith("SecurityViolation: Admins only!\n")


class TestFixPyramidsRestrictedAdmin(CLITest):

    # make the user in this test a member of system group
    DEFAULT_SYSTEM = True
    # make the new member of system group to a Restricted
    # Admin with no privileges
    DEFAULT_PRIVILEGES = ()

    def setup_method(self, method):
        super(TestFixPyramidsRestrictedAdmin, self).setup_method(method)
        self.cli.register("admin", omero.plugins.admin.AdminControl, "TEST")
        self.args += ["admin", "fixpyramids"]

    def test_fixpyramids_restricted_admin(self, capsys):
        """Test fixpyramids cannot be run by Restricted Admin"""
        config_service = self.root.sf.getConfigService()
        data_dir = config_service.getConfigValue("omero.data.dir")
        self.args += [data_dir]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        output_start = "SecurityViolation: Admin restrictions: Chgrp"
        assert err.startswith(output_start)
