#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2018 University of Dundee & Open Microscopy Environment.
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

"""Tests display of data in History page."""

from omeroweb.testlib import IWebTest
from omeroweb.testlib import get, post
from datetime import datetime
from django.core.urlresolvers import reverse


class TestHistory(IWebTest):
    """Tests display of data in History page."""

    def test_history(self):
        """Test /webclient/history/ page."""
        request_url = reverse("load_template", args=["history"])
        response = get(self.django_client, request_url)
        assert "history_calendar" in response.content

    def test_calendar_default(self):
        """Test display of new Project in today's history page."""
        calendar_url = reverse("load_calendar")
        response = get(self.django_client, calendar_url)
        # Calendar is initially empty (no 'Project' icon)
        assert "folder16.png" not in response.content

        # Add Project
        request_url = reverse("manage_action_containers",
                              args=["addnewcontainer"])
        data = {
            'folder_type': 'project',
            'name': 'foobar'
        }
        response = post(self.django_client, request_url, data)

        # Default calendar loads calendar for current month
        response = get(self.django_client, calendar_url)
        # Now contains icon for Project
        assert "folder16.png" in response.content

    def test_calendar_month(self):
        """Test loading of calendar, specifying this month."""
        now = datetime.now()
        calendar_url = reverse("load_calendar", args=[now.year, now.month])
        print 'calendar_url', calendar_url
        response = get(self.django_client, calendar_url)
        # Calendar is initially empty (no 'Dataset' icon)
        assert "folder_image16.png" not in response.content

        # Add Dataset
        request_url = reverse("manage_action_containers",
                              args=["addnewcontainer"])
        data = {
            'folder_type': 'dataset',
            'name': 'foobar'
        }
        response = post(self.django_client, request_url, data)

        # Now contains icon for Dataset
        response = get(self.django_client, calendar_url)
        assert "folder_image16.png" in response.content
