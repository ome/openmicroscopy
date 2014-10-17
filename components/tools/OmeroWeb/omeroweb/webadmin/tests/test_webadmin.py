#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#
# Copyright (c) 2008 University of Dundee.
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


import unittest, os
import omero

from connector import Server, Connector

# Test model
class ServerModelTest (unittest.TestCase):

    def setUp(self):
        Server.reset()

    def test_constructor(self):
        # Create object with alias
        Server(host=u'example.com', port=4064, server=u'ome')

        # Create object without alias
        Server(host=u'example2.com', port=4065)

        # without any params
        try:
            Server()
        except Exception:
            pass
        else:
            self.fail('Error:Parameters required')

    def test_get_and_find(self):
        SERVER_LIST = [[u'example1.com', 4064, u'omero1'], [u'example2.com', 4064, u'omero2'], [u'example3.com', 4064], [u'example4.com', 4064]]
        for s in SERVER_LIST:
            server = (len(s) > 2) and s[2] or None
            Server(host=s[0], port=s[1], server=server)

        s1 = Server.get(1)
        self.assertEquals(s1.host, u'example1.com')
        self.assertEquals(s1.port, 4064)
        self.assertEquals(s1.server, u'omero1')

        s2 = Server.find('example2.com')[0]
        self.assertEquals(s2.host, u'example2.com')
        self.assertEquals(s2.port, 4064)
        self.assertEquals(s2.server, u'omero2')

    def test_load_server_list(self):
        SERVER_LIST = [[u'example1.com', 4064, u'omero1'], [u'example2.com', 4064, u'omero2'], [u'example3.com', 4064], [u'example4.com', 4064]]
        for s in SERVER_LIST:
            server = (len(s) > 2) and s[2] or None
            Server(host=s[0], port=s[1], server=server)
        Server.freeze()

        try:
            Server(host=u'example5.com', port=4064)
        except Exception:
            pass
        else:
            self.fail('Error:No more instances allowed')

        Server(host=u'example1.com', port=4064)
