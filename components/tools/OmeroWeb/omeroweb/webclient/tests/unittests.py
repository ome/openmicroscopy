#!/usr/bin/env python
#
#
# Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
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
#

from webgateway.tests.unittests import WGTest

class UserProxyTest (WGTest):
    def test (self):
        self.loginAsAuthor()
        user = self.gateway.user
        self.assertEqual(user.isAdmin(), False)
        int(user.getId())
        self.assertEqual(user.getName(), self.AUTHOR.name)
        self.assertEqual(user.getFirstName(), self.AUTHOR.firstname)
