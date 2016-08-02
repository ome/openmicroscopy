#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
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

import pytest

from test.integration.clitest.cli import CLITest
from omero.cli import NonZeroReturnCode
from omero.plugins.obj import ObjControl
from omero.plugins.upload import UploadControl
from omero.util.temp_files import create_path


class TestUpload(CLITest):

    def setup_method(self, method):
        super(TestUpload, self).setup_method(method)
        self.cli.register("upload", UploadControl, "TEST")
        self.cli.register("obj", ObjControl, "TEST")
        self.args += ["upload"]

    def upload(self, capfd):
        self.cli.invoke(self.args, strict=True)
        return capfd.readouterr()[0]

    def check_file_name(self, originalFile, filename):
        args = self.login_args() + ["obj", "get", originalFile]
        self.cli.invoke(args + ["name"], strict=True)
        name = self.cli.get("tx.state").get_row(0)
        assert filename.name == name

    def check_file_name_via_annotation(self, fileAnnotation, filename):
        args = self.login_args() + ["obj", "get", fileAnnotation]
        self.cli.invoke(args + ["file"], strict=True)
        originalFile = self.cli.get("tx.state").get_row(0)
        self.check_file_name(originalFile, filename)

    def testUploadSingleFile(self, capfd):
        f = create_path(suffix=".txt")
        self.args += [str(f)]
        out = self.upload(capfd)
        self.check_file_name(out, f)

    def testUploadSingleFileAnnotation(self, capfd):
        f = create_path(suffix=".txt")
        self.args += ["--annotate"]
        self.args += [str(f)]
        out = self.upload(capfd)
        self.check_file_name_via_annotation(out, f)

    def testUploadMultipleFiles(self, capfd):
        f1 = create_path(suffix=".txt")
        f2 = create_path(suffix=".txt")
        self.args += [str(f1), str(f2)]
        out = self.upload(capfd)
        ids = out.split(":")[1].split(",")
        self.check_file_name("OriginalFile:%s" % ids[0], f1)
        self.check_file_name("OriginalFile:%s" % ids[1], f2)

    def testUploadMultipleFileAnnotations(self, capfd):
        f1 = create_path(suffix=".txt")
        f2 = create_path(suffix=".txt")
        self.args += ["--annotate"]
        self.args += [str(f1), str(f2)]
        out = self.upload(capfd)
        ids = out.split(":")[1].split(",")
        self.check_file_name_via_annotation("FileAnnotation:%s" % ids[0], f1)
        self.check_file_name_via_annotation("FileAnnotation:%s" % ids[1], f2)

    def testUploadBadFile(self, capfd):
        f1 = create_path(suffix=".txt")
        f2 = self.uuid() + ""
        self.args += [str(f1), str(f2)]
        with pytest.raises(NonZeroReturnCode):
            self.upload(capfd)
