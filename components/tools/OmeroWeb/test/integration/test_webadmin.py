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

from omeroweb.testlib import IWebTest
from omeroweb.testlib import post

from django.core.urlresolvers import reverse


class TestExperimenters(IWebTest):
    """Test creation and editing of Experimenters."""

    def test_create_experimenter(self):
        """Test simple Experimenter creation."""
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
        redirect = post(self.django_root_client, request_url, data,
                        status_code=302)
        # Redirect location should be to 'experimenters' page
        assert redirect.get('Location').endswith(reverse('waexperimenters'))

        # Check that user was created
        admin = self.client.sf.getAdminService()
        # Will throw if user doesn't exist
        admin.lookupExperimenter(uuid)
