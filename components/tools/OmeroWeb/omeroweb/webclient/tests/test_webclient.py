#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#
# Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# Copyright 2013 Glencoe Software, Inc. All rights reserved.
# Use is subject to license terms supplied in LICENSE.txt
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
# pytest fixtures used as defined in pytest_fixtures.py:
# - gatewaywrapper
#


def testUserProxy(gatewaywrapper):
    gatewaywrapper.loginAsAuthor()
    user = gatewaywrapper.gateway.getUser()
    assert user.isAdmin() is False
    int(user.getId())
    assert user.getName() == gatewaywrapper.AUTHOR.name
    assert user.getFirstName() == gatewaywrapper.AUTHOR.firstname
