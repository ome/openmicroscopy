#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2020 University of Dundee & Open Microscopy Environment.
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

"""Tests querying Experimenters and ExperimenterGroups."""

from omeroweb.testlib import IWebTest, get_json, \
    post_json, put_json, delete_json
from django.core.urlresolvers import reverse
from omeroweb.api import api_settings
import pytest
from test_api_projects import get_connection
from test_api_containers import assert_objects
from omero_marshal import OME_SCHEMA_URL
from omero.sys import ParametersI


class TestExperimenters(IWebTest):
    """Tests querying Experimenters."""

    @pytest.fixture()
    def user1(self):
        """Return a new user in a read-annotate group."""
        group = self.new_group(perms='rwra--')
        user = self.new_client_and_user(group=group)
        return user

    @pytest.mark.parametrize("dtype", ['Experimenter',
                                       'ExperimenterGroup'])
    @pytest.mark.parametrize("method", [(post_json, 'Creation'),
                                        (put_json, 'Update')])
    def test_create_update_unsupported(self, user1, dtype, method):
        """Test create and update are rejected for unsupported types."""
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]
        save_url = reverse('api_save', kwargs={'api_version': version})
        payload = {'Name': 'test',
                   '@type': OME_SCHEMA_URL + '#%s' % dtype}
        # Test PUT/POST
        rsp = method[0](django_client, save_url, payload,
                        status_code=405)
        assert rsp['message'] == '%s of %s not supported' % (method[1], dtype)

    @pytest.mark.parametrize("dtype", ['Experimenter',
                                       'ExperimenterGroup'])
    def test_delete_unsupported(self, user1, dtype):
        """Test delete is rejected for unsupported types."""
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]
        # Delete (fake url - object doesn't need to exist for test)
        url_name = 'api_%s' % dtype.lower()
        # NB: here the dtype and url_name do not map as they do elsewhere
        if dtype == 'ExperimenterGroup':
            url_name = 'api_experimentergroup'
        delete_url = reverse(url_name, kwargs={'api_version': version,
                                               'object_id': 1})
        rsp = delete_json(django_client, delete_url, status_code=405)
        assert rsp['message'] == 'Delete of %s not supported' % dtype

    def test_experimenters_groups(self, user1):
        """
        Test listing experimenters.

        We simply list existing Experimenters since we have no way to filter
        and show only those created in the test.
        """
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)
        request_url = reverse(
            'api_experimenters',
            kwargs={'api_version': api_settings.API_VERSIONS[-1]})
        data = {'limit': 10}
        rsp = get_json(django_client, request_url, data)
        exp_json = rsp['data']

        query = """select obj from Experimenter as obj order by
                   lower(obj.lastName), lower(obj.firstName), obj.id"""
        params = ParametersI()
        params.page(0, 10)
        exps = conn.getQueryService().findAllByQuery(query, params)

        assert_objects(conn, exp_json, exps, dtype="Experimenter")

        # Check we can follow link to Groups for first Experimenter
        groups_url = exp_json[0]["url:experimentergroups"]
        rsp = get_json(django_client, groups_url)
        groups_json = rsp['data']
        grp_ids = [g['@id'] for g in groups_json]

        # Check if gids are same for experimenter (won't be ordered)
        gids = [g.id for g in conn.getOtherGroups(exp_json[0]['@id'])]
        assert set(gids) == set(grp_ids)

        assert_objects(conn, groups_json, grp_ids, dtype="ExperimenterGroup")

    def test_groups_experimenters(self, user1):
        """
        Test listing groups.

        We simply list existing Groups since we have no way to filter
        and show only those created in the test.
        """
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        django_client = self.new_django_client(user_name, user_name)
        request_url = reverse(
            'api_experimentergroups',
            kwargs={'api_version': api_settings.API_VERSIONS[-1]})
        data = {'limit': 10}
        rsp = get_json(django_client, request_url, data)
        groups_json = rsp['data']

        query = """select obj from ExperimenterGroup as obj order by
                   lower(obj.name), obj.id"""
        params = ParametersI()
        params.page(0, 10)
        groups = conn.getQueryService().findAllByQuery(query, params)

        assert_objects(conn, groups_json, groups, dtype="ExperimenterGroup")

        # Check we can follow link to Experimenters for first Group
        expimenters_url = groups_json[0]["url:experimenters"]
        rsp = get_json(django_client, expimenters_url)
        exps_json = rsp['data']
        exp_ids = [e['@id'] for e in exps_json]

        # Check if eids are same for group (won't be ordered)
        grp = conn.getObject("ExperimenterGroup", groups_json[0]['@id'])
        eids = [link.child.id.val for link in grp.copyGroupExperimenterMap()]
        assert set(eids) == set(exp_ids)

        assert_objects(conn, exps_json, exp_ids, dtype="Experimenter")

    def test_filter_groups(self, user1):
        """
        Test filtering groups by experimenter
        """
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        user_id = conn.getUserId()
        group_id = conn.getEventContext().groupId
        django_client = self.new_django_client(user_name, user_name)
        request_url = reverse(
            'api_experimentergroups',
            kwargs={'api_version': api_settings.API_VERSIONS[-1]})
        # Test /experimentergroups/?experimenter=1
        data = {'experimenter': user_id}
        rsp = get_json(django_client, request_url, data)
        groups_json = rsp['data']
        # user1 is in a single group AND the 'user' group
        assert len(groups_json) == 2
        user_group_id = conn.getAdminService().getSecurityRoles().userGroupId
        user_group = [g for g in groups_json if g["@id"] == user_group_id]
        assert user_group[0]['Name'] == 'user'
        other_group = [g for g in groups_json if g["@id"] != user_group_id]
        assert other_group[0]['Name'] == conn.getGroupFromContext().name
        assert other_group[0]['@id'] == group_id

        # Test same result with experimenters/1/experimentergroups/
        request_url = reverse(
            'api_experimenter_experimentergroups',
            kwargs={'api_version': api_settings.API_VERSIONS[-1],
                    'experimenter_id': user_id})
        rsp = get_json(django_client, request_url)
        groups_json2 = rsp['data']
        assert groups_json == groups_json2

    def test_filter_experimenters(self, user1):
        """
        Test filtering experimenters by group
        """
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        user_id = conn.getUserId()
        group_id = conn.getEventContext().groupId
        django_client = self.new_django_client(user_name, user_name)
        request_url = reverse(
            'api_experimenters',
            kwargs={'api_version': api_settings.API_VERSIONS[-1]})
        # Test /experimenters/?experimentergroup=1
        data = {'experimentergroup': group_id}
        rsp = get_json(django_client, request_url, data)
        exps_json = rsp['data']
        # user1 is in a single group AND the 'user' group
        assert len(exps_json) == 1
        assert exps_json[0]['@id'] == user_id
        assert exps_json[0]['UserName'] == user_name

        # Test same result with experimentergroups/1/experimenters/
        request_url = reverse(
            'api_experimentergroup_experimenters',
            kwargs={'api_version': api_settings.API_VERSIONS[-1],
                    'group_id': group_id})
        rsp = get_json(django_client, request_url)
        exps_json2 = rsp['data']
        assert exps_json == exps_json2
