#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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

from datetime import date
from datetime import datetime
from datetime import timedelta

from test.integration.clitest.cli import CLITest
from omero.cli import NonZeroReturnCode
from omero.model import DatasetI
from omero.plugins.search import SearchControl
from omero.rtypes import rstring


class TestSearch(CLITest):

    def mkimage(self, with_acquisitionDate=False):
        self._uuid = self.uuid().replace("-", "")
        if with_acquisitionDate:
            filename_date = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
            self._image = self.import_mif(
                name=self._uuid, acquisitionDate=filename_date)[0]
        else:
            self._image = self.import_mif(name=self._uuid)[0]
        self.root.sf.getUpdateService().indexObject(self._image)

    def mkdataset(self):
        self._uuid_ds = self.uuid().replace("-", "")
        self._dataset = DatasetI()
        self._dataset.name = rstring(self._uuid_ds)
        update = self.client.sf.getUpdateService()
        self._dataset = update.saveAndReturnObject(self._dataset)
        self.root.sf.getUpdateService().indexObject(self._dataset)

    def short(self):
        return self._uuid[0:8]

    def days_ago(self, ago=1):
        t = date.today() - timedelta(ago)
        t = t.strftime("%Y-%m-%d")
        return t

    def setup_method(self, method):
        super(TestSearch, self).setup_method(method)
        self.cli.register("search", SearchControl, "TEST")
        self.args += ["search"]
        self.setup_mock()

    def go(self):
        self.cli.invoke(self.args, strict=True)
        return self.cli.get("search.results")

    def assertSearch(self, args, success=True, name=None):
        if name is None:
            name = self._uuid
        self.args.extend(list(args))
        if success:
            results = self.go()
            assert 1 == len(results)
            assert name in results[0].name.val
        else:
            with pytest.raises(NonZeroReturnCode):
                results = self.go()

    def test_search_basic(self):
        self.mkimage()
        self.assertSearch(("Image", self._uuid + "*"))

    def test_search_wildcard(self):
        self.mkimage()
        short = self.short()
        self.assertSearch(("Image", short + "*"))

    def test_search_name_field(self):
        self.mkimage()
        short = self.short()
        self.assertSearch(("Image", short + "*", "--field=name"))

    def test_search_description_field(self):
        self.mkimage()
        short = self.short()
        with pytest.raises(NonZeroReturnCode):
            # Not set on description
            self.assertSearch(("Image", short + "*",
                               "--field=description"))

    def test_search_style(self, capsys):
        self.mkimage()
        short = self.short()
        self.assertSearch(("Image", short + "*", "--style=plain"))
        o, e = capsys.readouterr()
        parts = o.split(",")
        assert "ImageI" == parts[1]
        assert ("%s" % self._image.id.val) == parts[2]

    def test_search_ids_only(self, capsys):
        self.mkimage()
        short = self.short()
        self.assertSearch(("Image", short + "*", "--ids-only"))
        o, e = capsys.readouterr()
        assert ("ImageI:%s" % self._image.id.val) in o

    @pytest.mark.parametrize("data", (
        (1, None, True, True),
        (1, None, False, False),
    ))
    def test_search_acquisition_date(self, data):
        from_ago, to_ago, with_acquisitionDate, success = data
        self.mkimage(with_acquisitionDate=with_acquisitionDate)
        short = self.short()
        args = ["Image", short + "*"]
        if from_ago:
            args += ["--from=%s" % self.days_ago(from_ago)]
        if to_ago:
            args += ["--to=%s" % self.days_ago(to_ago)]
        args += ["--date-type=acquisitionDate"]

        self.assertSearch(args, success=success)

    @pytest.mark.parametrize("data", (
        (1, None, None, True),
        (1, None, "import", True),
        (1, -1, None, True),
        (None, 1, None, False),
        (-1, None, None, False),
    ))
    def test_search_other_dates(self, data):
        from_ago, to_ago, date_type, success = data
        self.mkimage()
        short = self.short()
        args = ["Image", short + "*"]
        if from_ago:
            args += ["--from=%s" % self.days_ago(from_ago)]
        if to_ago:
            args += ["--to=%s" % self.days_ago(to_ago)]
        if date_type:
            args += ["--date-type=%s" % date_type]

        self.assertSearch(args, success=success)

    def test_search_no_parse(self):
        self.mkimage()
        short = self.short()
        args = ["Image", short + "*", "--no-parse"]
        self.assertSearch(args)

    def test_search_dataset_acquisition(self):
        self.mkdataset()
        txt = self._uuid_ds[0:8] + "*"
        _from = "--from=%s" % self.days_ago(1)
        _to = "--to=%s" % self.days_ago(-1)
        args = ["Dataset", txt, _from, _to]
        self.assertSearch(args, name=self._uuid_ds)
