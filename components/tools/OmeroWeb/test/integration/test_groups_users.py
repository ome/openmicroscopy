# -*- coding: utf-8 -*-

"""
Test webclient listing of Groups and Experimenters.

Copyright (C) 2021 University of Dundee & Open Microscopy Environment.
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

from omeroweb.testlib import IWebTest
from omeroweb.testlib import get

from django.urls import reverse

READONLY = 'rwr---'


class TestGroupsUsers(IWebTest):

    def test_group_users_menu(self):
        request_url = reverse("group_user_content")
        my_groups = """<strong>My Groups</strong>"""
        all_groups = """<strong>ALL Groups</strong>"""
        # regular user in group
        exp = self.new_user()
        group = self.new_group(experimenters=[exp.omeName], perms=READONLY)
        ome_name = exp.omeName.val
        django_client = self.new_django_client(ome_name, ome_name)
        rsp = get(django_client, request_url)
        page_html = rsp.content.decode("utf-8")
        assert my_groups not in page_html
        assert all_groups not in page_html
        assert str(group.id.val) in page_html
        assert group.name.val in page_html
        # admin
        rsp = get(self.django_root_client, request_url)
        page_html = rsp.content.decode("utf-8")
        # Admin can see 'My Groups' and 'ALL Groups'
        assert my_groups in page_html
        assert all_groups in page_html
        assert str(group.id.val) in page_html
        assert group.name.val in page_html
