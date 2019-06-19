#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2019 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

"""Tests for querying OMERO.tables."""

import pytest
from omeroweb.testlib import IWebTest, get, get_json
from test_api_projects import get_connection

from omero.grid import WellColumn, StringColumn

from django.core.urlresolvers import reverse
from random import random


class TestOmeroTables(IWebTest):
    """Tests querying of OMERO.table."""

    @pytest.fixture()
    def user1(self):
        """Return a new user in a read-annotate group."""
        group = self.new_group(perms='rwra--')
        return self.new_client_and_user(group=group)

    @pytest.fixture()
    def django_client(self, user1):
        """Return new Django client."""
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        return self.new_django_client(user_name, user_name)

    @pytest.fixture()
    def omero_table_file(self, user1):
        """Create a new OMERO Table and returns the original file ID."""
        client = user1[0]
        col1 = WellColumn('Well', '', [])
        col2 = StringColumn('TestColumn', '', 64, [])

        columns = [col1, col2]
        tablename = "plate_well_table_test:%s" % str(random())
        table = client.sf.sharedResources().newTable(1, tablename)
        table.initialize(columns)

        wellIds = [1]

        data1 = WellColumn('Well', '', wellIds)
        data2 = StringColumn('TestColumn', '', 64, ["foobar"])
        data = [data1, data2]
        table.addData(data)
        table.close()

        orig_file = table.getOriginalFile()
        return orig_file.id.val

    def test_table_html(self, omero_table_file, django_client):
        """Do a GET request to query table data."""
        file_id = omero_table_file
        wellId = 1

        # expected table data
        cols = ['Well', 'TestColumn']
        rows = [[wellId, 'foobar']]

        # GET json
        request_url = reverse("omero_table", args=[file_id, 'json'])
        rsp = get_json(django_client, request_url)
        assert rsp['data']['rows'] == rows
        assert rsp['data']['columns'] == cols
        assert rsp['data']['name'].startswith('plate_well_table_test')
        assert rsp['data']['id'] == file_id

        # GET html
        request_url = reverse("omero_table", args=[file_id])
        rsp = get(django_client, request_url)
        html = rsp.content
        for col in cols:
            assert ('<th>%s</th>' % col) in html
        for row in rows:
            for td in row:
                assert ('<td>%s</td>' % td) in html

        # GET csv
        request_url = reverse("omero_table", args=[file_id, 'csv'])
        rsp = get(django_client, request_url)
        csv_data = rsp.content
        cols_csv = ','.join(cols)
        rows_csv = '/n'.join([','.join(
            [str(td) for td in row]) for row in rows])
        assert csv_data == '%s\n%s' % (cols_csv, rows_csv)
