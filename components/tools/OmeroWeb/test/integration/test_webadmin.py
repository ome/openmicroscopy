# -*- coding: utf-8 -*-

"""
Test webadmin editing of Groups and Experimenters.

Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
All rights reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
"""

import pytest
from omeroweb.testlib import IWebTest
from omeroweb.testlib import post, get
import re

from django.core.urlresolvers import reverse


def get_all_privileges(client):
    """Get the full list of Privileges as strings."""
    types = client.sf.getTypesService()
    return [e.getValue().val for e in types.allEnumerations("AdminPrivilege")]


class TestExperimenters(IWebTest):
    """Test creation and editing of Experimenters."""

    @pytest.mark.parametrize("role",
                             ["user",
                              "restricted_administrator",
                              "administrator"
                              ])
    def test_create_experimenter_roles(self, role):
        """Test creation of User, Restricted Admin and Full Admin."""
        uuid = self.uuid()
        groupid = self.new_group().id.val
        request_url = reverse('wamanageexperimenterid', args=["create"])
        data = {
            "omename": uuid,
            "first_name": uuid,
            "last_name": uuid,
            "active": "on",
            "default_group": groupid,
            "other_groups": groupid,
            "password": uuid,
            "confirmation": uuid,
            "role": role,
            "Sudo": 'on',   # This should be ignored for 'user' role
        }
        redirect = post(self.django_root_client, request_url, data,
                        status_code=302)
        # Redirect location should be to 'experimenters' page
        assert redirect.get('Location').endswith(reverse('waexperimenters'))

        # Check that user was created
        admin = self.client.sf.getAdminService()
        exp = admin.lookupExperimenter(uuid)

        # Check Role & Privileges
        privileges = [p.getValue().val for p in admin.getAdminPrivileges(exp)]
        sid = admin.getSecurityRoles().systemGroupId
        is_admin = sid in [m.parent.id.val
                           for m in exp.copyGroupExperimenterMap()]

        if role == 'user':
            assert len(privileges) == 0
            assert not is_admin
        else:
            assert is_admin
            if role == 'restricted_administrator':
                assert privileges == ['Sudo']
            else:
                assert set(privileges) == set(get_all_privileges(self.client))

    @pytest.mark.parametrize("required_field",
                             [["omename", "This field is required"],
                              ["first_name", "This field is required"],
                              ["last_name", "This field is required"],
                              ["password", "This field is required"],
                              ["confirmation", "This field is required"],
                              ["default_group", "No default group selected"],
                              ["other_groups",
                               "User must be a member of at least one group."]
                              ])
    def test_required_fields(self, required_field):
        """Test form validation with required fields missing."""
        uuid = self.uuid()
        groupid = self.new_group().id.val
        request_url = reverse('wamanageexperimenterid', args=["create"])
        data = {
            "omename": uuid,
            "first_name": uuid,
            "last_name": uuid,
            "active": "on",
            "default_group": groupid,
            "other_groups": groupid,
            "password": uuid,
            "confirmation": uuid,
            "role": "user",
        }
        field, error = required_field
        del data[field]
        rsp = post(self.django_root_client, request_url, data)
        assert error in rsp.content

    @pytest.mark.parametrize(
        "privilege",
        [('Sudo', ('Sudo',)),
         ('Write', ('WriteFile', 'WriteManagedRepo', 'WriteOwned')),
         ('Delete', ('DeleteFile', 'DeleteManagedRepo', 'DeleteOwned')),
         ('Chgrp', ('Chgrp',)),
         ('Chown', ('Chown',)),
         ('ModifyGroup', ('ModifyGroup',)),
         ('ModifyUser', ('ModifyUser',)),
         ('ModifyGroupMembership', ('ModifyGroupMembership',)),
         ('Script', ('DeleteScriptRepo', 'WriteScriptRepo')),
         ('unknown', [])])
    def test_create_restricted_admin(self, privilege):
        """Test creation of restricted admin."""
        uuid = self.uuid()
        groupid = self.new_group().id.val
        request_url = reverse('wamanageexperimenterid', args=["create"])
        data = {
            "omename": uuid,
            "first_name": uuid,
            "last_name": uuid,
            "active": "on",
            "default_group": groupid,
            "other_groups": groupid,
            "password": uuid,
            "confirmation": uuid,
            "role": "restricted_administrator"
        }
        name, expected = privilege
        # Enable a single Privilege for this user
        data[name] = 'on'
        post(self.django_root_client, request_url, data, status_code=302)

        admin = self.client.sf.getAdminService()
        exp = admin.lookupExperimenter(uuid)
        privileges = [p.getValue().val for p in admin.getAdminPrivileges(exp)]
        assert set(expected) == set(privileges)

    @pytest.mark.parametrize(
        "privileges",
        [['Sudo'],
         # expect 'Write' to be enabled
         ['WriteFile', 'WriteManagedRepo', 'WriteOwned', 'ModifyUser'],
         # expect 'Write' to be disabled
         ['WriteManagedRepo', 'WriteOwned', 'ModifyUser'],
         ['Chgrp', 'Chown', 'ModifyUser'],
         ['ModifyGroup', 'ModifyUser']])
    def test_create_restricted_admin_form(self, privileges):
        """
        Test experimenter form doesn't allow privilege escalation.

        Without 'ModifyUser' privilege, whole form is disabled.
        """
        admin = self.client.sf.getAdminService()

        # Create Restricted Admin and check privileges are as expected
        client, exp = self.new_client_and_user(privileges=privileges)
        p = [p.getValue().val for p in admin.getAdminPrivileges(exp)]
        assert set(privileges) == set(p)

        # Restricted admin Django client... (password is same as ome_name)
        ome_name = exp.omeName.val
        django_client = self.new_django_client(ome_name, ome_name)
        # loads the new experimenter form
        request_url = reverse('wamanageexperimenterid', args=["new"])
        rsp = get(django_client, request_url)
        form_html = rsp.content
        form_lines = form_html.split('\n')
        input_text = '<input class="privilege'
        inputs = [line for line in form_lines if input_text in line]

        form_enabled = 'ModifyUser' in privileges
        for field in inputs:
            p_name = re.search('(?<=name=\")[A-Za-z]*', field).group(0)
            # if privilege is if not in user's privileges, should be disabled
            if p_name == 'Write':
                # presence of 'WriteFile' in privileges should match enabled
                p_name = 'WriteFile'
            expected = form_enabled and p_name in privileges
            enabled = 'disabled' not in field
            assert enabled == expected, p_name

        # Shouldn't be able to create a Full Admin
        admin_option = [l for l in form_lines if 'value="administrator"' in l]
        assert 'disabled' in admin_option[0]
