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

from django.urls import reverse


def get_all_privileges(client):
    """Get the full list of Privileges as strings."""
    types = client.sf.getTypesService()
    return [e.getValue().val for e in types.allEnumerations("AdminPrivilege")]


class TestUserSettings(IWebTest):

    def validate_settings_page(self, django_client, ome_name, first_name,
                               last_name, default_group_id, admin=False):
        request_url = reverse("wamyaccount", args=['edit'])
        rsp = get(django_client, request_url)
        html = rsp.content.decode("utf-8")
        print('html', html)
        admin_link = ("""<a href="/webadmin/" title="Web-Admin:"""
                      """ Edit users and groups">Admin</a>""")
        assert "Username:" in html
        assert (admin_link in html) == admin
        assert ("name=\"omename\" value=\"%s\"" % ome_name) in html
        assert ("name=\"first_name\" value=\"%s\"" % first_name) in html
        assert ("name=\"last_name\" value=\"%s\"" % last_name) in html
        assert ("<option value=\"%s\" selected>" % default_group_id) in html

    def test_user_settings_page(self):
        # regular user
        client, exp = self.new_client_and_user()
        gid = client.sf.getAdminService().getEventContext().groupId
        ome_name = exp.omeName.val
        first_name = exp.firstName.val
        last_name = exp.lastName.val
        django_client = self.new_django_client(ome_name, ome_name)
        self.validate_settings_page(django_client, ome_name, first_name,
                                    last_name, gid)
        # check experimenter -1. See https://github.com/ome/omero-web/pull/285
        get(django_client, reverse("userdata"), {'experimenter': '-1'})
        self.validate_settings_page(django_client, ome_name, first_name,
                                    last_name, gid)
        # admin
        gid = self.root.sf.getAdminService().getEventContext().groupId
        self.validate_settings_page(self.django_root_client, "root", "root",
                                    "root", gid, admin=True)

    def test_edit_settings(self):
        request_url = reverse("wamyaccount", args=['save'])
        exp = self.new_user()
        ome_name = exp.omeName.val
        first = "Ben"
        last = "Nevis"
        # Add user to a 2nd group
        groupid = self.new_group(experimenters=[exp]).id.val
        django_client = self.new_django_client(ome_name, ome_name)
        data = {
            "omename": ome_name,
            "first_name": first,
            "middle_name": "J",
            "last_name": last,
            "email": "",
            "institution": "UoD",
            "default_group": groupid,
        }
        rsp = post(django_client, request_url, data, status_code=302)
        assert rsp.get('Location').endswith(reverse("wamyaccount"))
        # Check fields and default group have been saved
        self.validate_settings_page(django_client, ome_name, first, last,
                                    groupid)


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
        groupid2 = self.new_group().id.val
        request_url = reverse('wamanageexperimenterid', args=["create"])
        data = {
            "omename": uuid,
            "first_name": uuid,
            "last_name": uuid,
            "active": "on",
            "default_group": groupid,
            "other_groups": [groupid, groupid2],
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
        exp_gids = [m.parent.id.val for m in exp.copyGroupExperimenterMap()]
        # In new groups
        assert groupid in exp_gids
        assert groupid2 in exp_gids

        # Check Role & Privileges
        privileges = [p.getValue().val for p in admin.getAdminPrivileges(exp)]
        sid = admin.getSecurityRoles().systemGroupId
        is_admin = sid in exp_gids

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
        assert error in rsp.content.decode("utf-8")

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
        exp = self.new_user(privileges=privileges)
        p = [p.getValue().val for p in admin.getAdminPrivileges(exp)]
        assert set(privileges) == set(p)

        # Restricted admin Django client... (password is same as ome_name)
        ome_name = exp.omeName.val
        django_client = self.new_django_client(ome_name, ome_name)
        # loads the new experimenter form
        request_url = reverse('wamanageexperimenterid', args=["new"])
        rsp = get(django_client, request_url)
        form_html = rsp.content.decode("utf-8")
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
        admin_option = [line for line in form_lines
                        if 'value="administrator"' in line]
        assert 'disabled' in admin_option[0]

    def test_restricted_admin_create_edit_user(self):
        """Test create & edit user doesn't allow privilege escalation."""
        exp = self.new_user(privileges=['Chown', 'ModifyGroup', 'ModifyUser',
                                        'ModifyGroupMembership'])
        ome_name = exp.omeName.val
        django_client = self.new_django_client(ome_name, ome_name)

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
            "role": "restricted_administrator",
            "Sudo": 'on',   # Sudo and Chgrp should be ignored (escalation)
            "Chgrp": 'on',
            "ModifyGroup": 'on',   # Should be applied
        }
        post(django_client, request_url, data, status_code=302)
        # Check that user was created
        admin = self.client.sf.getAdminService()
        exp = admin.lookupExperimenter(uuid)
        # Check Privileges
        privileges = [p.getValue().val for p in admin.getAdminPrivileges(exp)]
        assert privileges == ['ModifyGroup']

        # Edit user
        request_url = reverse('wamanageexperimenterid',
                              args=["save", exp.id.val])
        # Add & remove privileges
        data['Chown'] = 'on'
        del data['ModifyGroup']
        post(django_client, request_url, data, status_code=302)

        privileges = [p.getValue().val for p in admin.getAdminPrivileges(exp)]
        assert privileges == ['Chown']


class TestGroups(IWebTest):
    """Test creation and editing of Groups."""

    @pytest.mark.parametrize("privileges",
                             [['ModifyGroup'],
                              ['ModifyGroupMembership'],
                              ['ModifyGroup', 'ModifyGroupMembership']
                              ])
    def test_new_group_form(self, privileges):
        """Form should show correct fields for editing Group / Membership."""
        exp = self.new_user(privileges=privileges)
        ome_name = exp.omeName.val
        django_client = self.new_django_client(ome_name, ome_name)

        # Only users with "ModifyGroup" see "Create Group" btn on groups page
        can_modify = 'ModifyGroup' in privileges
        request_url = reverse('wagroups')
        rsp = get(django_client, request_url)
        groups_html = rsp.content.decode("utf-8")
        assert ('<span>Add new Group</span>' in groups_html) == can_modify

        # Check new group form has correct fields for privileges
        request_url = reverse('wamanagegroupid', args=["new"])
        rsp = get(django_client, request_url)
        form_html = rsp.content.decode("utf-8")

        assert ('name="name"' in form_html) == can_modify
        assert ('name="description"' in form_html) == can_modify
        assert ('name="permissions"' in form_html) == can_modify

        can_add_members = 'ModifyGroupMembership' in privileges
        assert ('name="owners"' in form_html) == can_add_members
        assert ('name="members"' in form_html) == can_add_members

    @pytest.mark.parametrize("permissions",
                             [["0", [0, 0, 0]],
                              ["1", [1, 0, 0]],
                              ["2", [1, 1, 0]],
                              ["3", [1, 1, 1]],
                              ])
    def test_create_group_permissions(self, permissions):
        """Test simple group creation."""
        uuid = self.uuid()
        request_url = reverse('wamanagegroupid', args=["create"])
        pvalue, perms = permissions
        data = {
            "name": uuid,
            "permissions": pvalue
        }
        redirect = post(self.django_root_client, request_url, data,
                        status_code=302)
        # Redirect location should be to 'groups' page
        assert redirect.get('Location').endswith(reverse('wagroups'))

        # Check that group was created
        admin = self.client.sf.getAdminService()
        group = admin.lookupGroup(uuid)

        # permissions are: isGroupRead, isGroupAnnotate, isGroupWrite
        group_perms = group.getDetails().getPermissions()
        attrs = ("isGroupRead", "isGroupAnnotate", "isGroupWrite")
        for (p, a) in zip(perms, attrs):
            assert p == getattr(group_perms, a)()

    @pytest.mark.parametrize("required_field",
                             [["name", "This field is required"],
                              ["permissions", "This field is required"]
                              ])
    def test_required_fields(self, required_field):
        """Test form validation with required fields missing."""
        uuid = self.uuid()
        request_url = reverse('wamanagegroupid', args=["create"])
        data = {
            "name": uuid,
            "permissions": "0"
        }
        field, error = required_field
        del data[field]
        rsp = post(self.django_root_client, request_url, data)
        assert error in rsp.content.decode("utf-8")

    def test_validation_errors(self):
        """Test creating or editing group with existing group name."""
        uuid = self.uuid()
        exp = self.new_user(privileges=['ModifyGroup',
                                        'ModifyGroupMembership'])
        ome_name = exp.omeName.val
        django_client = self.new_django_client(ome_name, ome_name)

        request_url = reverse('wamanagegroupid', args=["create"])
        data = {
            "name": "system",
            "permissions": "0"
        }
        # Try to create - check error
        rsp = post(django_client, request_url, data)
        assert "This name already exists." in rsp.content.decode("utf-8")
        # Create group
        data['name'] = uuid
        post(django_client, request_url, data, status_code=302)

        admin = self.client.sf.getAdminService()
        group = admin.lookupGroup(uuid)

        # Create a new user in this group, and in a different group
        exp = self.new_user(group=group)
        exp2 = self.new_user()

        # Try to save - check duplicate name again...
        request_url = reverse('wamanagegroupid', args=["save", group.id.val])
        data = {
            "name": "system",
            "permissions": "0"
        }
        rsp = post(django_client, request_url, data)
        assert "This name already exists." in rsp.content.decode("utf-8")

        # ...Fix name. Now we get error saving group with no members
        # (removed user from their only group)
        data['name'] = uuid
        rsp = post(django_client, request_url, data)
        assert "Can't remove user" in rsp.content.decode("utf-8")

        # Save and check we added members
        data['members'] = [exp.id.val, exp2.id.val]
        post(django_client, request_url, data, status_code=302)

        group = admin.lookupGroup(uuid)
        eids = [link.child.id.val for link in group.copyGroupExperimenterMap()]

        assert set(eids) == set(data['members'])

    def test_save_experimenter(self):
        """Test edit of User"""
        # Create User in 2 groups
        uuid = self.uuid()
        groupid = self.new_group().id.val
        groupid2 = self.new_group().id.val
        groupid3 = self.new_group().id.val
        request_url = reverse('wamanageexperimenterid', args=["create"])
        data = {
            "omename": uuid,
            "first_name": uuid,
            "last_name": uuid,
            "active": "on",
            "default_group": groupid,
            "other_groups": [groupid, groupid2],
            "password": uuid,
            "confirmation": uuid,
            "role": "user",
        }
        post(self.django_root_client, request_url, data, status_code=302)
        # Check that user was created in groups
        admin = self.root.sf.getAdminService()
        exp = admin.lookupExperimenter(uuid)
        exp_id = exp.id.val
        exp_gids = [m.parent.id.val for m in exp.copyGroupExperimenterMap()]
        # first group is default group
        assert exp_gids[0] == groupid
        assert groupid2 in exp_gids

        # Edit names, change default group, add group, remove group
        new_name = "new_name_%s" % uuid
        new_lastname = "new_lastname_%s" % uuid
        request_url = reverse('wamanageexperimenterid', args=["save", exp_id])
        data = {
            "omename": uuid,
            "first_name": new_name,
            "last_name": new_lastname,
            "active": "on",
            "default_group": groupid2,
            "other_groups": [groupid2, groupid3],
            "role": "user",
        }
        post(self.django_root_client, request_url, data, status_code=302)

        # Check
        exp = admin.lookupExperimenter(uuid)
        assert exp.firstName.val == new_name
        assert exp.lastName.val == new_lastname
        exp_id = exp.id.val
        exp_gids = [m.parent.id.val for m in exp.copyGroupExperimenterMap()]
        # first group is default group
        assert exp_gids[0] == groupid2
        assert groupid not in exp_gids
        assert groupid3 in exp_gids
