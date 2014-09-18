#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.


"""
Test of various things under omero.gateway
"""

import Ice
import pytest

from omero.gateway import BlitzGateway


class TestBlitzGatewayUnicode(object):
    """
    Tests to ensure that unicode encoding of usernames and passwords are
    performed successfully.  `gateway.connect()` will not even attempt to
    perform a connection and just return `False` if the encoding fails.
    """

    def test_unicode_username(self):
        with pytest.raises(Ice.ConnectionRefusedException):
            gateway = BlitzGateway(
                username=u'ążźćółę', passwd='secret',
                host='localhost', port=65535
            )
            gateway.connect()

    def test_unicode_password(self):
        with pytest.raises(Ice.ConnectionRefusedException):
            gateway = BlitzGateway(
                username='user', passwd=u'ążźćółę',
                host='localhost', port=65535
            )
            gateway.connect()
