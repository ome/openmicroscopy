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

from omero.grid import WellColumn, StringColumn, DoubleColumn

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
    def table_data(self):
        """Return column classes, column names & row data."""
        col_types = [
            WellColumn, StringColumn, DoubleColumn, DoubleColumn
        ]
        col_names = ["Well", "TestColumn", "SmallNumbers", "BigNumbers"]
        rows = [
            [1, 'test', 0.5, 135345.0],
            [2, 'string', 1.0, 345345.121],
            [3, 'column', 0.75, 356575.012],
            [4, 'data', 0.12345, 13579.0],
            [5, 'five', 0.01, 500.05]
        ]
        return (col_types, col_names, rows)

    @pytest.fixture()
    def omero_table_file(self, user1, table_data):
        """Create a new OMERO Table and returns the original file ID."""
        client = user1[0]
        col_types, col_names, rows = table_data

        columns = []
        for col_type, name in zip(col_types, col_names):
            if col_type == StringColumn:
                columns.append(StringColumn(name, '', 64, []))
            else:
                columns.append(col_type(name, '', []))

        tablename = "omero_table_test:%s" % str(random())
        table = client.sf.sharedResources().newTable(1, tablename)
        table.initialize(columns)

        data = []
        for col_type, name, idx in zip(col_types, col_names,
                                       range(len(col_names))):
            col_data = [row[idx] for row in rows]
            if col_type == StringColumn:
                data.append(StringColumn(name, '', 64, col_data))
            else:
                data.append(col_type(name, '', col_data))

        table.addData(data)

        orig_file = table.getOriginalFile()
        table.close()
        return orig_file.id.val

    def test_table_html(self, omero_table_file, django_client, table_data):
        """Do a GET request to query table data."""
        file_id = omero_table_file

        # expected table data
        col_types, col_names, rows = table_data

        col_types = [cls.__name__ for cls in col_types]

        # GET json
        request_url = reverse("omero_table", args=[file_id, 'json'])
        rsp = get_json(django_client, request_url)
        assert rsp['data']['rows'] == rows
        assert rsp['data']['columns'] == col_names
        assert rsp['data']['column_types'] == col_types
        assert rsp['data']['name'].startswith('omero_table_test')
        assert rsp['data']['id'] == file_id

        # GET html
        request_url = reverse("omero_table", args=[file_id])
        rsp = get(django_client, request_url)
        html = rsp.content
        for col_type, col in zip(col_types, col_names):
            assert ('<th title="%s">%s</th>' % (col_type, col)) in html
        well_col_index = col_types.index('WellColumn')
        for row in rows:
            for idx, td in enumerate(row):
                if idx != well_col_index:
                    assert ('<td>%s</td>' % td) in html
                else:
                    # link to webclient
                    link = reverse('webindex')
                    link_html = ('<a target="_blank" href="%s?show=well-%s">'
                                 % (link, td))
                    assert link_html in html

        # GET csv
        request_url = reverse("omero_table", args=[file_id, 'csv'])
        rsp = get(django_client, request_url)
        csv_data = rsp.content
        cols_csv = ','.join(col_names)
        rows_csv = '\n'.join([','.join(
            [str(td) for td in row]) for row in rows])
        assert csv_data == '%s\n%s' % (cols_csv, rows_csv)

    def test_table_pagination(self, omero_table_file, django_client,
                              table_data):
        """Test pagination of table data as JSON."""
        file_id = omero_table_file

        # expected table data
        col_types, col_names, rows = table_data

        # GET json
        limit = 2
        request_url = reverse("omero_table", args=[file_id, 'json'])
        for offset in [0, 2, 4]:
            request_url += '?limit=%s&offset=%s' % (limit, offset)
            rsp = get_json(django_client, request_url)
            assert rsp['data']['rows'] == rows[offset: offset + limit]

    def test_table_query(self, omero_table_file, django_client, table_data):
        """Test pagination of table data as JSON."""
        file_id = omero_table_file

        # expected table data
        col_types, col_names, rows = table_data
        queries = ['SmallNumbers>0.5',
                   '(SmallNumbers>=0.75)%26(BigNumbers<350000.5)',
                   'SmallNumbers==0.01']
        filtered_rows = [
            [r for r in rows if r[2] > 0.5],
            [r for r in rows if r[2] >= 0.75 and r[3] < 350000.5],
            [r for r in rows if r[2] == 0.01]]

        for query, expected in zip(queries, filtered_rows):
            request_url = reverse("omero_table", args=[file_id, 'json'])
            request_url += '?query=%s' % query
            rsp = get_json(django_client, request_url)
            assert rsp['data']['rows'] == expected
