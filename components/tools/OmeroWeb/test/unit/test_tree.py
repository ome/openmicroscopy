#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2014 Glencoe Software, Inc.
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

"""
Simple unit tests for the "tree" module.
"""

import pytest

from omero.rtypes import rlong, rstring, rtime
from omeroweb.webclient.tree import marshal_plate_acquisition, \
    marshal_dataset, marshal_plate, parse_permissions_css


class MockConnection(object):

    def getUserId(self):
        return 1L

    def isAdmin(self):
        return False


@pytest.fixture(scope='module')
def mock_conn():
    return MockConnection()


@pytest.fixture(scope='module')
def owner_permissions():
    return {
        'canEdit': True,
        'canAnnotate': True,
        'canLink': True,
        'canDelete': True
    }


@pytest.fixture(scope='module')
def start_time():
    # 2014-05-08 10:37:02 UTC; server timestamps contain ms
    return rtime(1399545422L * 1000)


@pytest.fixture(scope='module')
def end_time():
    # 2014-05-08 10:38:30 UTC; server timestamps contain ms
    return rtime(1399545510L * 1000)


class TestTree(object):
    """
    Tests to ensure that OMERO.web "tree" infrastructure is working
    correctly.  Order and type of columns in row is:
      * id (rlong)
      * name (rstring)
      * details.owner.id (rlong)
      * details.permissions (dict)
      * startTime (rtime)
      * endTime (rtime)
    """

    def test_marshal_plate_acquisition_no_name_no_start_no_end(
            self, mock_conn, owner_permissions):
        row = [
            rlong(1L),
            None,
            rlong(1L),
            owner_permissions,
            None,
            None
        ]
        expected = {
            'id': 1L,
            'name': 'Run 1',
            'permsCss':
                'canEdit canAnnotate canLink canDelete isOwned canChgrp'
        }

        marshaled = marshal_plate_acquisition(mock_conn, row)
        assert marshaled == expected

    def test_marshal_plate_acquisition_name_no_start_no_end(
            self, mock_conn, owner_permissions):
        row = [
            rlong(1L),
            rstring('name'),
            rlong(1L),
            owner_permissions,
            None,
            None
        ]
        expected = {
            'id': 1L,
            'name': 'name',
            'permsCss':
                'canEdit canAnnotate canLink canDelete isOwned canChgrp'
        }

        marshaled = marshal_plate_acquisition(mock_conn, row)
        assert marshaled == expected

    def test_marshal_plate_acquisition_no_name_start_end(
            self, mock_conn, owner_permissions, start_time, end_time):
        row = [
            rlong(1L),
            None,
            rlong(1L),
            owner_permissions,
            start_time,
            end_time
        ]
        expected = {
            'id': 1L,
            'name': '2014-05-08 10:37:02 - 2014-05-08 10:38:30',
            'permsCss':
                'canEdit canAnnotate canLink canDelete isOwned canChgrp'
        }

        marshaled = marshal_plate_acquisition(mock_conn, row)
        assert marshaled == expected

    def test_marshal_plate_acquisition_not_owner(
            self, mock_conn, owner_permissions):
        row = [
            rlong(1L),
            None,
            rlong(2L),
            owner_permissions,
            None,
            None
        ]
        expected = {
            'id': 1L,
            'name': 'Run 1',
            'permsCss': 'canEdit canAnnotate canLink canDelete'
        }

        marshaled = marshal_plate_acquisition(mock_conn, row)
        assert marshaled == expected

    def test_parse_permissions_css(
            self, mock_conn):
        restrictions = ('canEdit', 'canAnnotate', 'canLink', 'canDelete')
        # Iterate through every combination of the restrictions' flags,
        # checking each with and without expected canChgrp
        for i in range(2**len(restrictions)):
            expected = []
            permissions_dict = {'perm': '------'}
            for j in range(len(restrictions)):
                if i & 2**j != 0:
                    expected.append(restrictions[j])
                    permissions_dict[restrictions[j]] = True
                else:
                    permissions_dict[restrictions[j]] = False
            expected.sort()
            owner_id = mock_conn.getUserId()
            # Test with different owner_ids, which means canChgrp is False
            received = parse_permissions_css(permissions_dict,
                                             owner_id+1,
                                             mock_conn)
            received = filter(None, received.split(' '))
            received.sort()
            assert expected == received
            # Test with matching owner_ids, which means
            # isOwned and canChgrp is True
            expected.append('isOwned')
            expected.append('canChgrp')
            expected.sort()
            received = parse_permissions_css(permissions_dict,
                                             owner_id,
                                             mock_conn)
            received = filter(None, received.split(' '))
            received.sort()
            assert expected == received

    def test_marshal_dataset(self, mock_conn, owner_permissions):
        row = [
            rlong(1L),
            rstring('name'),
            rlong(1L),
            owner_permissions,
            rlong(1L)
        ]
        expected = {
            'id': 1L,
            'name': 'name',
            'permsCss':
                'canEdit canAnnotate canLink canDelete isOwned canChgrp',
            'childCount': 1
        }

        marshaled = marshal_dataset(mock_conn, row)
        assert marshaled == expected

    def test_marshal_dataset_not_owner(self, mock_conn, owner_permissions):
        row = [
            rlong(1L),
            rstring('name'),
            rlong(2L),
            owner_permissions,
            rlong(1L)
        ]
        expected = {
            'id': 1L,
            'name': 'name',
            'permsCss': 'canEdit canAnnotate canLink canDelete',
            'childCount': 1
        }

        marshaled = marshal_dataset(mock_conn, row)
        assert marshaled == expected

    def test_marshal_plate(self, mock_conn, owner_permissions):
        row = [
            rlong(1L),
            rstring('name'),
            rlong(1L),
            owner_permissions,
        ]
        expected = {
            'id': 1L,
            'name': 'name',
            'permsCss':
                'canEdit canAnnotate canLink canDelete isOwned canChgrp',
            'plateAcquisitions': list()
        }

        marshaled = marshal_plate(mock_conn, row)
        assert marshaled == expected

    def test_marshal_plate_not_owner(self, mock_conn, owner_permissions):
        row = [
            rlong(1L),
            rstring('name'),
            rlong(2L),
            owner_permissions,
        ]
        expected = {
            'id': 1L,
            'name': 'name',
            'permsCss': 'canEdit canAnnotate canLink canDelete',
            'plateAcquisitions': list()
        }

        marshaled = marshal_plate(mock_conn, row)
        assert marshaled == expected
