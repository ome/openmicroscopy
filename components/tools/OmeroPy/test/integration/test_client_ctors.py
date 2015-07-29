#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2008-2014 Glencoe Software, Inc. All Rights Reserved.
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
   Tests of the omero.client constructors

"""

import os
import library as lib
import omero
import Ice

here = os.path.abspath(os.path.dirname(__file__))


class TestClientConstructors(lib.ITest):

    def setup_method(self, method):
        c = omero.client(pmap=['--Ice.Config='+(os.environ.get("ICE_CONFIG"))])
        try:
            self.host = c.ic.getProperties().getProperty('omero.host')
            self.port = int(c.ic.getProperties().getProperty('omero.port'))
            self.rootpasswd = c.ic.getProperties().getProperty(
                'omero.rootpass')
        finally:
            c.__del__()

    def testHostConstructor(self):
        c = omero.client(host=self.host, port=self.port)
        try:
            c.createSession("root", self.rootpasswd)
            c.closeSession()
            c.createSession("root", self.rootpasswd)
        except:
            c.__del__()

    def testEmptyInitializationDataConstructor(self):
        id = Ice.InitializationData()
        # With no argument id.properties is empty
        id.properties = Ice.createProperties()
        id.properties.setProperty("omero.host", self.host)
        id.properties.setProperty("omero.port", str(self.port))
        id.properties.setProperty("omero.user", "root")
        id.properties.setProperty("omero.pass", self.rootpasswd)
        c = omero.client(id=id)
        try:
            c.createSession()
            c.closeSession()
            c.createSession()
            c.closeSession()
        finally:
            c.__del__()

    def testInitializationDataConstructor(self):
        id = Ice.InitializationData()
        id.properties = Ice.createProperties([])
        id.properties.setProperty("omero.user", "root")
        id.properties.setProperty("omero.pass", self.rootpasswd)
        c = omero.client(id=id)
        try:
            c.createSession()
            c.closeSession()
            c.createSession()
            c.closeSession()
        finally:
            c.__del__()

    def testMainArgsConstructor(self):
        args = ["--omero.host="+self.host,
                "--omero.user=root", "--omero.pass=" + self.rootpasswd]
        c = omero.client(args)
        try:
            c.createSession()
            c.closeSession()
            c.createSession()
            c.closeSession()
        finally:
            c.__del__()

    def testMapConstructor(self):
        p = {}
        p["omero.host"] = self.host
        p["omero.user"] = "root"
        p["omero.pass"] = self.rootpasswd
        c = omero.client(pmap=p)
        try:
            c.createSession()
            c.closeSession()
            c.createSession()
            c.closeSession()
        finally:
            c.__del__()

    def testMainArgsGetsIcePrefix(self):
        args = ["--omero.host="+self.host,
                "--omero.user=root", "--omero.pass=" + self.rootpasswd]
        args.append("--Ice.MessageSizeMax=10")
        c = omero.client(args)
        try:
            c.createSession()
            assert "10" == c.getProperty("Ice.MessageSizeMax")
            c.closeSession()
        finally:
            c.__del__()

    def testMainArgsGetsIceConfig(self):
        cfg = os.path.join(here, "client_ctors.cfg")
        if not os.path.exists(cfg):
            assert False, cfg + " does not exist"
        args = ["--Ice.Config=" + cfg, "--omero.host=unimportant"]
        c = omero.client(args)
        try:
            assert "true" == c.getProperty("in.ice.config")
            # c.createSession()
            # c.closeSession()
        finally:
            c.__del__()

    def testTwoDifferentHosts(self):
        try:
            c1 = omero.client(host="foo")
            c1.createSession()
            c1.closeSession()
        except:
            print "foo failed appropriately"

        c2 = omero.client(host=self.host, port=self.port)
        try:
            user = self.new_user()
            c2.createSession(user.omeName.val, user.omeName.val)
            c2.closeSession()
        finally:
            c2.__del__()

    def testPorts(self):
        c = omero.client("localhost", 1111)
        try:
            assert "1111" == c.ic.getProperties().getProperty("omero.port")
        finally:
            c.__del__()

        c = omero.client("localhost", ["--omero.port=2222"])
        try:
            assert "2222" == c.ic.getProperties().getProperty("omero.port")
        finally:
            c.__del__()
        # c = omero.client("localhost")
        # assert str(omero.constants.GLACIER2PORT) ==\
        #     c.ic.getProperties().getProperty("omero.port")

    def testBlockSize(self):
        c = omero.client("localhost")
        try:
            assert 5000000 == c.getDefaultBlockSize()
        finally:
            c.__del__()
        c = omero.client("localhost", ["--omero.block_size=1000000"])
        try:
            assert 1000000 == c.getDefaultBlockSize()
        finally:
            c.__del__()

    def testPythonCtorRepair(self):
        # c = omero.client(self.host, omero.constants.GLACIER2PORT)
        c = omero.client(self.host, self.port)
        try:
            c.createSession("root", self.rootpasswd)
            c.closeSession()
        finally:
            c.__del__()
