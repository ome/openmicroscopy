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


import unittest, time, os, datetime
import tempfile

import omero

from django.conf import settings
from request_factory import fakeRequest

from webgateway import views as webgateway_views
from webadmin import views as webadmin_views
from webadmin.webadmin_utils import toBoolean
from webadmin.forms import LoginForm, GroupForm, ExperimenterForm, \
                ContainedExperimentersForm, ChangePassword

from connector import Server, Connector
from webadmin.views import getActualPermissions, setActualPermissions, \
                        otherGroupsInitialList, prepare_experimenter, \
                        getSelectedExperimenters, getSelectedGroups, \
                        mergeLists
from django.core.urlresolvers import reverse

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
        except Exception, x:
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
        except Exception, x:
            pass
        else:
            self.fail('Error:No more instances allowed')

        Server(host=u'example1.com', port=4064)


class WebTest(unittest.TestCase):
        
    def setUp (self):
        c = omero.client(pmap=['--Ice.Config='+(os.environ.get("ICE_CONFIG"))])
        try:
            self.root_password = c.ic.getProperties().getProperty('omero.rootpass')
            self.omero_host = c.ic.getProperties().getProperty('omero.host')
            self.omero_port = c.ic.getProperties().getProperty('omero.port')
            Server.reset()
            Server(host=self.omero_host, port=self.omero_port)
        finally:
            c.__del__()

        self.server_id = 1
        connector = Connector(self.server_id, True)
        self.rootconn = connector.create_connection('TEST.webadmin', 'root', self.root_password)
        if self.rootconn is None or not self.rootconn.isConnected() or not self.rootconn.keepAlive():
            raise Exception("Cannot connect")

    def tearDown(self):
        try:
            self.rootconn.seppuku()
        except Exception,e:
            self.fail(e)
    
    def loginAsUser(self, username, password):
        blitz = Server.get(pk=self.server_id) 
        if blitz is not None:
            connector = Connector(self.server_id, True)
            conn = connector.create_connection('TEST.webadmin', username, password)
            if conn is None or not conn.isConnected() or not conn.keepAlive():
                raise Exception("Cannot connect")
            return conn
        else:
            raise Exception("'%s' is not on omero.web.server_list"  % omero_host)


class WebAdminConfigTest(unittest.TestCase):
    
    def setUp (self):
        c = omero.client(pmap=['--Ice.Config='+(os.environ.get("ICE_CONFIG"))])
        try:
            self.root_password = c.ic.getProperties().getProperty('omero.rootpass')
            self.omero_host = c.ic.getProperties().getProperty('omero.host')
            self.omero_port = c.ic.getProperties().getProperty('omero.port')
            Server.reset()
            Server(host=self.omero_host, port=self.omero_port)
        finally:
            c.__del__()
    
    def test_isServerOn(self):
        connector = Connector(1, True)
        if not connector.is_server_up('omero-webadmin-test'):
            self.fail('Server is offline')
            
    def test_checkVersion(self):
        connector = Connector(1, True)
        if not connector.check_version('omero-webadmin-test'):
            self.fail('Client version does not match server')
    
# Testing controllers, and forms
class WebAdminTest(WebTest):
        
    def test_loginFromRequest(self):
        params = {
            'username': 'root',
            'password': self.root_password,
            'server':self.server_id,
            'ssl':'on'
        }        
        request = fakeRequest(method="post", path="/webadmin/login", params=params)
        
        server_id = request.REQUEST.get('server')
        username = request.REQUEST.get('username')
        password = request.REQUEST.get('password')
        is_secure = toBoolean(request.REQUEST.get('ssl'))

        connector = Connector(server_id, is_secure)
        conn = connector.create_connection('TEST.webadmin', username, password)
        if conn is None:
            self.fail('Cannot connect')
        
        conn.seppuku()
        if conn.isConnected() and conn.keepAlive():
            self.fail('Connection was not closed')

    def test_loginFromForm(self):
        params = {
            'username': 'root',
            'password': self.root_password,
            'server':self.server_id,
            'ssl':'on'
        }        
        request = fakeRequest(method="post", params=params)
        
        server_id = request.REQUEST.get('server')
        form = LoginForm(data=request.REQUEST.copy())
        if form.is_valid():
            username = form.cleaned_data['username']
            password = form.cleaned_data['password']
            server_id = form.cleaned_data['server']
            is_secure = toBoolean(form.cleaned_data['ssl'])

            connector = Connector(server_id, is_secure)
            conn = connector.create_connection('OMERO.web', username, password)
            if conn is None:
                self.fail('Cannot connect')
            
            conn.seppuku()
            if conn.isConnected() and conn.keepAlive():
                self.fail('Connection was not closed')
            
        else:
            errors = form.errors.as_text()
            self.fail(errors)
            
    def test_loginFailure(self):
        params = {
            'username': 'notauser',
            'password': 'nonsence',
            'server':self.server_id
        }        
        request = fakeRequest(method="post", params=params)
        
        server_id = request.REQUEST.get('server')
        form = LoginForm(data=request.REQUEST.copy())
        if form.is_valid():
            username = form.cleaned_data['username']
            password = form.cleaned_data['password']
            server_id = form.cleaned_data['server']
            is_secure = toBoolean(form.cleaned_data['ssl'])

            connector = Connector(server_id, is_secure)
            conn = connector.create_connection('OMERO.web', username, password)
            if conn is not None:
                self.fail('This user does not exist. Login failure error!')
        
        else:
            errors = form.errors.as_text()
            self.fail(errors)            
    
    def test_createGroupsWithNonASCII(self):        
        conn = self.rootconn
        uuid = conn._sessionUuid
        
        # private group
        params = {
            "name":u"русский_алфавит %s" % uuid,
            "description":u"Frühstück-Śniadanie. Tschüß-Cześć",
            "owners": [0L],
            "members": [0L],
            "permissions":0
        }
        request = fakeRequest(method="post", params=params)
        gid = _createGroup(request, conn)
        
        # check if group created
        group = conn.getObject("ExperimenterGroup", gid)
        ownerIds = [e.id for e in group.getOwners()]
        membersIds = [e.id for e in group.getMembers()]
        permissions = getActualPermissions(group)
        self.assertEquals(params['name'], group.name)
        self.assertEquals(params['description'], group.description)
        self.assertEquals(sorted(params['owners']), sorted(ownerIds))
        self.assertEquals(sorted(params['members']), sorted(membersIds))
        self.assertEquals(params['permissions'], permissions)
    
    def test_createGroups(self):        
        conn = self.rootconn
        uuid = conn._sessionUuid
        
        # private group
        params = {
            "name":"webadmin_test_group_private %s" % uuid,
            "description":"test group",
            "permissions":0
        }
        request = fakeRequest(method="post", params=params)
        gid = _createGroup(request, conn)
           
        # check if group created
        group = conn.getObject("ExperimenterGroup", gid)
        permissions = getActualPermissions(group)
        self.assertEquals(params['name'], group.name)
        self.assertEquals(params['description'], group.description)
        self.assertEquals(params['permissions'], permissions)
        
        # collaborative read-only group
        params = {
            "name":"webadmin_test_group_read-only %s" % uuid,
            "description":"test group",
            "permissions":1
        }
        request = fakeRequest(method="post", params=params)
        gid = _createGroup(request, conn)
           
        # check if group created
        group = conn.getObject("ExperimenterGroup", gid)
        permissions = getActualPermissions(group)
        self.assertEquals(params['name'], group.name)
        self.assertEquals(params['description'], group.description)
        self.assertEquals(params['permissions'], permissions)
        
        # collaborative read-annotate group
        params = {
            "name":"webadmin_test_group_read-ann %s" % uuid,
            "description":"test group",
            "permissions":2
        }
        request = fakeRequest(method="post", params=params)
        gid = _createGroup(request, conn)
           
        # check if group created
        group = conn.getObject("ExperimenterGroup", gid)
        permissions = getActualPermissions(group)
        self.assertEquals(params['name'], group.name)
        self.assertEquals(params['description'], group.description)
        self.assertEquals(params['permissions'], permissions)
    
    def test_badCreateGroup(self):
        conn = self.rootconn
        uuid = conn._sessionUuid
        
        # empty fields
        params = {}
        request = fakeRequest(method="post", params=params)
        try:
            gid = _createGroup(request, conn)
        except Exception, e:
            pass
        else:
            self.fail("Can't create group with no parameters")
    
    def test_updateGroups(self):
        conn = self.rootconn
        uuid = conn._sessionUuid
        
        # default group - helper
        params = {
            "name":"webadmin_test_default %s" % uuid,
            "description":"test group default",
            "owners": [0L],
            "permissions":0
        }
        request = fakeRequest(method="post", params=params)
        default_gid = _createGroup(request, conn)
        
        # create new user
        params = {
            "omename":"webadmin_test_owner %s" % uuid,
            "first_name":uuid,
            "middle_name": uuid,
            "last_name":uuid,
            "email":"owner_%s@domain.com" % uuid,
            "institution":"Laboratory",
            "active":True,
            "default_group":default_gid,
            "other_groups":[default_gid],
            "password":"123",
            "confirmation":"123" 
        }
        request = fakeRequest(method="post", params=params)
        eid = _createExperimenter(request, conn)
        
        # create new user2
        params = {
            "omename":"webadmin_test_member %s" % uuid,
            "first_name":uuid,
            "middle_name": uuid,
            "last_name":uuid,
            "email":"member_%s@domain.com" % uuid,
            "institution":"Laboratory",
            "active":True,
            "default_group":default_gid,
            "other_groups":[default_gid],
            "password":"123",
            "confirmation":"123" 
        }
        request = fakeRequest(method="post", params=params)
        eid2 = _createExperimenter(request, conn)
        
        # create private group
        params = {
            "name":"webadmin_test_group_private %s" % uuid,
            "description":"test group",
            "permissions":0
        }
        request = fakeRequest(method="post", params=params)
        gid = _createGroup(request, conn)
        
        # check if group created
        group = conn.getObject("ExperimenterGroup", gid)
        permissions = getActualPermissions(group)
        self.assertEquals(params['name'], group.name)
        self.assertEquals(params['description'], group.description)
        self.assertEquals(params['permissions'], permissions)
        
        # upgrade group to collaborative
        # read-only group and add new owner and members
        params = {
            "name":"webadmin_test_group_read-only %s" % uuid,
            "description":"test group changed",
            "owners": [eid],
            "members": [eid2],
            "permissions":1
        }
        request = fakeRequest(method="post", params=params)
        _updateGroup(request, conn, gid)
        
        # check if group updated
        group = conn.getObject("ExperimenterGroup", gid)
        ownerIds = [e.id for e in group.getOwners()]
        memberIds = [e.id for e in group.getMembers()]
        permissions = getActualPermissions(group)
        self.assertEquals(params['name'], group.name)
        self.assertEquals(params['description'], group.description)
        self.assertEquals(params['owners'], sorted(ownerIds))
        self.assertEquals(sorted(mergeLists(params['owners'], params['members'])), sorted(memberIds))
        self.assertEquals(params['permissions'], permissions)
        
        # upgrade group to collaborative
        # read-ann group and change owners and members
        params = {
            "name":"webadmin_test_group_read-only %s" % uuid,
            "description":"test group changed",
            "owners": [eid2],
            "members": [0,eid],
            "permissions":1
        }
        request = fakeRequest(method="post", params=params)
        _updateGroup(request, conn, gid)
        
        # check if group updated
        group = conn.getObject("ExperimenterGroup", gid)
        ownerIds = [e.id for e in group.getOwners()]
        memberIds = [e.id for e in group.getMembers()]
        permissions = getActualPermissions(group)
        self.assertEquals(params['name'], group.name)
        self.assertEquals(params['description'], group.description)
        self.assertEquals(params['owners'], sorted(ownerIds))
        self.assertEquals(sorted(mergeLists(params['owners'], params['members'])), sorted(memberIds))
        self.assertEquals(params['permissions'], permissions)
        
    def test_badUpdateGroup(self):
        conn = self.rootconn
        uuid = conn._sessionUuid
        
        # create group
        params = {
            "name":"webadmin_test_group_private %s" % uuid,
            "description":"test group",
            "owners": [0L],
            "permissions":0
        }
        request = fakeRequest(method="post", params=params)
        gid = _createGroup(request, conn)
        
        # create new users
        params2 = {
            "omename":"webadmin_test_user1 %s" % uuid,
            "first_name":uuid,
            "middle_name": uuid,
            "last_name":uuid,
            "email":"user1_%s@domain.com" % uuid,
            "institution":"Laboratory",
            "active":True,
            "default_group":gid,
            "other_groups":[gid],
            "password":"123",
            "confirmation":"123" 
        }
        request = fakeRequest(method="post", params=params2)
        eid = _createExperimenter(request, conn)
        
        # check if group updated
        group = conn.getObject("ExperimenterGroup", gid)
        ownerIds = [e.id for e in group.getOwners()]
        memberIds = [e.id for e in group.getMembers()]
        self.assertEquals(params['owners'], sorted(ownerIds))
        self.assertEquals(sorted(mergeLists(params['owners'], [eid])), sorted(memberIds))
        
        # remove user from the group
        params["members"] = [0]
        request = fakeRequest(method="post", params=params)
        _updateGroup(request, conn, gid)
        
        # check if group updated
        group = conn.getObject("ExperimenterGroup", gid)
        memberIds = [e.id for e in group.getMembers()]
        if eid not in memberIds:
            self.fail("Can't remove user from the group members if this is hs default group")
        
    def test_createExperimenters(self):
        conn = self.rootconn
        uuid = conn._sessionUuid
        
        # private group
        params = {
            "name":"webadmin_test_group_private %s" % uuid,
            "description":"test group",
            "permissions":0
        }
        request = fakeRequest(method="post", params=params)
        gid = _createGroup(request, conn)
        
        params = {
            "omename":"webadmin_test_user %s" % uuid,
            "first_name":uuid,
            "middle_name": uuid,
            "last_name":uuid,
            "email":"user_%s@domain.com" % uuid,
            "institution":"Laboratory",
            "active":True,
            "default_group":gid,
            "other_groups":[gid],
            "password":"123",
            "confirmation":"123" 
        }
        request = fakeRequest(method="post", params=params)
        eid = _createExperimenter(request, conn)
        
        # check if experimenter created
        experimenter = conn.getObject("Experimenter", eid)
        otherGroupIds = [g.id for g in experimenter.getOtherGroups()]
        self.assertEquals(params['omename'], experimenter.omeName)
        self.assertEquals(params['first_name'], experimenter.firstName)
        self.assertEquals(params['middle_name'], experimenter.middleName)
        self.assertEquals(params['last_name'], experimenter.lastName)
        self.assertEquals(params['email'], experimenter.email)
        self.assertEquals(params['institution'], experimenter.institution)
        self.assert_(not experimenter.isAdmin())
        self.assertEquals(params['active'], experimenter.isActive())
        self.assertEquals(params['default_group'], experimenter.getDefaultGroup().id)
        self.assertEquals(sorted(params['other_groups']), sorted(otherGroupIds))
        
        params = {
            "omename":"webadmin_test_admin %s" % uuid,
            "first_name":uuid,
            "middle_name": uuid,
            "last_name":uuid,
            "email":"admin_%s@domain.com" % uuid,
            "institution":"Laboratory",
            "administrator": True,
            "active":True,
            "default_group":gid,
            "other_groups":[0,gid],
            "password":"123",
            "confirmation":"123" 
        }
        request = fakeRequest(method="post", params=params)
        eid = _createExperimenter(request, conn)
        
        # check if experimenter created
        experimenter = conn.getObject("Experimenter", eid)
        otherGroupIds = [g.id for g in experimenter.getOtherGroups()]
        self.assertEquals(params['omename'], experimenter.omeName)
        self.assertEquals(params['first_name'], experimenter.firstName)
        self.assertEquals(params['middle_name'], experimenter.middleName)
        self.assertEquals(params['last_name'], experimenter.lastName)
        self.assertEquals(params['email'], experimenter.email)
        self.assertEquals(params['institution'], experimenter.institution)
        self.assertEquals(params['administrator'], experimenter.isAdmin())
        self.assertEquals(params['active'], experimenter.isActive())
        self.assertEquals(params['default_group'], experimenter.getDefaultGroup().id)
        self.assertEquals(sorted(params['other_groups']), sorted(otherGroupIds))
        
        params = {
            "omename":"webadmin_test_off %s" % uuid,
            "first_name":uuid,
            "middle_name": uuid,
            "last_name":uuid,
            "email":"off_%s@domain.com" % uuid,
            "institution":"Laboratory",
            "default_group":gid,
            "other_groups":[gid],
            "password":"123",
            "confirmation":"123" 
        }
        request = fakeRequest(method="post", params=params)
        eid = _createExperimenter(request, conn)
        
        # check if experimenter created
        experimenter = conn.getObject("Experimenter", eid)
        otherGroupIds = [g.id for g in experimenter.getOtherGroups()]
        self.assertEquals(params['omename'], experimenter.omeName)
        self.assertEquals(params['first_name'], experimenter.firstName)
        self.assertEquals(params['middle_name'], experimenter.middleName)
        self.assertEquals(params['last_name'], experimenter.lastName)
        self.assertEquals(params['email'], experimenter.email)
        self.assertEquals(params['institution'], experimenter.institution)
        self.assert_(not experimenter.isAdmin())
        self.assert_(not experimenter.isActive())
        self.assertEquals(params['default_group'], experimenter.getDefaultGroup().id)
        self.assertEquals(sorted(params['other_groups']), sorted(otherGroupIds))
    
    def test_badCreateExperimenters(self):
        conn = self.rootconn
        uuid = conn._sessionUuid
        
        # empty fields
        params = {}
        request = fakeRequest(method="post", params=params)
        try:
            eid = _createExperimenter(request, conn)
        except Exception, e:
            pass
        else:
            self.fail("Can't create user with no parameters")
            
    def test_updateExperimenter(self):
        conn = self.rootconn
        uuid = conn._sessionUuid
        
        # private group
        params = {
            "name":"webadmin_test_group_private %s" % uuid,
            "description":"test group",
            "permissions":0
        }
        
        request = fakeRequest(method="post", params=params)
        gid = _createGroup(request, conn)
        
        # default group - helper
        params = {
            "name":"webadmin_test_default %s" % uuid,
            "description":"test group default",
            "permissions":0
        }
        request = fakeRequest(method="post", params=params)
        default_gid = _createGroup(request, conn)
        
        # create experimenter
        params = {
            "omename":"webadmin_test_user %s" % uuid,
            "first_name":uuid,
            "middle_name": uuid,
            "last_name":uuid,
            "email":"user_%s@domain.com" % uuid,
            "institution":"Laboratory",
            "active":True,
            "administrator":False,
            "default_group":gid,
            "other_groups":[gid],
            "password":"123",
            "confirmation":"123" 
        }
        request = fakeRequest(method="post", params=params)
        eid = _createExperimenter(request, conn)
        
        # add admin privilages and change default group
        params = {
            "omename":"webadmin_test_admin %s" % uuid,
            "first_name":uuid,
            "middle_name": uuid,
            "last_name":uuid,
            "email":"admin_%s@domain.com" % uuid,
            "institution":"Laboratory",
            "administrator": True,
            "active":True,
            "default_group":default_gid,
            "other_groups":[0,gid,default_gid],
            "password":"123",
            "confirmation":"123" 
        }
        request = fakeRequest(method="post", params=params)
        _updateExperimenter(request, conn, eid)
        
        # check if experimenter updated
        experimenter = conn.getObject("Experimenter", eid)
        otherGroupIds = [g.id for g in experimenter.getOtherGroups()]
        self.assertEquals(params['omename'], experimenter.omeName)
        self.assertEquals(params['first_name'], experimenter.firstName)
        self.assertEquals(params['middle_name'], experimenter.middleName)
        self.assertEquals(params['last_name'], experimenter.lastName)
        self.assertEquals(params['email'], experimenter.email)
        self.assertEquals(params['institution'], experimenter.institution)
        self.assert_(experimenter.isAdmin())
        self.assert_(experimenter.isActive())
        self.assertEquals(params['default_group'], experimenter.getDefaultGroup().id)
        self.assertEquals(sorted(params['other_groups']), sorted(otherGroupIds))
        
        # remove admin privilages and deactivate account 
        params = {
            "omename":"webadmin_test_admin %s" % uuid,
            "first_name":uuid,
            "middle_name": uuid,
            "last_name":uuid,
            "email":"admin_%s@domain.com" % uuid,
            "institution":"Laboratory",
            "administrator": False,
            "active":False,
            "default_group":default_gid,
            "other_groups":[gid,default_gid],
            "password":"123",
            "confirmation":"123" 
        }
        request = fakeRequest(method="post", params=params)
        _updateExperimenter(request, conn, eid)
        
        # check if experimenter updated
        experimenter = conn.getObject("Experimenter", eid)
        otherGroupIds = [g.id for g in experimenter.getOtherGroups()]
        self.assertEquals(params['omename'], experimenter.omeName)
        self.assertEquals(params['first_name'], experimenter.firstName)
        self.assertEquals(params['middle_name'], experimenter.middleName)
        self.assertEquals(params['last_name'], experimenter.lastName)
        self.assertEquals(params['email'], experimenter.email)
        self.assertEquals(params['institution'], experimenter.institution)
        self.assert_(not experimenter.isAdmin())
        self.assert_(not experimenter.isActive())
        self.assertEquals(params['default_group'], experimenter.getDefaultGroup().id)
        self.assertEquals(sorted(params['other_groups']), sorted(otherGroupIds))
        
        
        try:
           self.loginAsUser(params['omename'], params['password'])
           self.fail('This user was deactivated. Login failure error!')
        except:
            pass
    
    def test_changePassword(self):
        conn = self.rootconn
        uuid = conn._sessionUuid
        
        # private group
        params = {
            "name": uuid,
            "description":"password test",
            "permissions":0
        }
        
        request = fakeRequest(method="post", params=params)
        gid = _createGroup(request, conn)
        
        # create experimenter
        params = {
            "omename":'password%s' % uuid,
            "first_name":uuid,
            "middle_name": uuid,
            "last_name":uuid,
            "email":"password_%s@domain.com" % uuid,
            "institution":"Laboratory",
            "administrator": False,
            "active":True,
            "default_group":gid,
            "other_groups":[gid],
            "password":"123",
            "confirmation":"123" 
        }
        request = fakeRequest(method="post", params=params)
        eid = _createExperimenter(request, conn)
        
        #change password as root        
        params_passwd = {
            "password":"abc",
            "confirmation":"abc",
            "old_password":self.root_password
        }
        request = fakeRequest(method="post", params=params_passwd)        
        _changePassword(request, conn, eid)
        
        # login as user and change my password
        user_conn = self.loginAsUser(params['omename'], params_passwd['password'])
        params_passwd = {
            "old_password":"abc",
            "password":"foo",
            "confirmation":"foo" 
        }
        request = fakeRequest(method="post", params=params_passwd)
        _changePassword(request, user_conn)
        
        self.loginAsUser(params['omename'], params_passwd['password'])
    
    def test_badChangePassword(self):
        conn = self.rootconn
        uuid = conn._sessionUuid
        
        # private group
        params = {
            "name": uuid,
            "description":"password test",
            "permissions":0
        }
        
        request = fakeRequest(method="post", params=params)
        gid = _createGroup(request, conn)
        
        # create experimenter
        params = {
            "omename":'password%s' % uuid,
            "first_name":uuid,
            "middle_name": uuid,
            "last_name":uuid,
            "email":"password_%s@domain.com" % uuid,
            "institution":"Laboratory",
            "administrator": False,
            "active":True,
            "default_group":gid,
            "other_groups":[gid],
            "password":"123",
            "confirmation":"123" 
        }
        request = fakeRequest(method="post", params=params)
        eid = _createExperimenter(request, conn)
        
        # login as user and change my password
        user_conn = self.loginAsUser(params['omename'], params['password'])
        params_passwd = {}
        request = fakeRequest(method="post", params=params_passwd)
        try:
            _changePassword(request, user_conn)
        except:
            pass
        else:
            self.fail("Can't change password with no parameters")
            
        self.loginAsUser(params['omename'], params['password'])

####################################
# helpers

def _changePassword(request, conn, eid=None):
    password_form = ChangePassword(data=request.POST.copy())
    if password_form.is_valid():
        old_password = password_form.cleaned_data['old_password']
        password = password_form.cleaned_data['password']
        if conn.isAdmin():
            exp = conn.getObject("Experimenter", eid)
            conn.changeUserPassword(exp.omeName, password, old_password)
        else:
            conn.changeMyPassword(password, old_password)
    else:
        raise Exception(password_form.errors.as_text())
        
def _createGroup(request, conn):
    #create group
    experimenters = list(conn.getObjects("Experimenter"))
    name_check = conn.checkGroupName(request.REQUEST.get('name'))
    form = GroupForm(initial={'experimenters':experimenters}, data=request.POST.copy(), name_check=name_check)
    if form.is_valid():
        name = form.cleaned_data['name']
        description = form.cleaned_data['description']
        owners = form.cleaned_data['owners']
        members = form.cleaned_data['members']
        permissions = form.cleaned_data['permissions']
        
        perm = setActualPermissions(permissions)
        listOfOwners = getSelectedExperimenters(conn, owners)
        gid = conn.createGroup(name, perm, listOfOwners, description)
        new_members = getSelectedExperimenters(conn, mergeLists(members,owners))
        group = conn.getObject("ExperimenterGroup", gid)
        conn.setMembersOfGroup(group, new_members)
        return gid
    else:
        raise Exception(form.errors.as_text())

def _updateGroup(request, conn, gid):
    # update group
    experimenters = list(conn.getObjects("Experimenter"))
    group = conn.getObject("ExperimenterGroup", gid)
    name_check = conn.checkGroupName(request.REQUEST.get('name'), group.name)
    form = GroupForm(initial={'experimenters':experimenters}, data=request.POST.copy(), name_check=name_check)
    if form.is_valid():
        name = form.cleaned_data['name']
        description = form.cleaned_data['description']
        owners = form.cleaned_data['owners']
        permissions = form.cleaned_data['permissions']
        members = form.cleaned_data['members']
        
        listOfOwners = getSelectedExperimenters(conn, owners)
        if permissions != int(permissions):
            perm = setActualPermissions(permissions)
        else:
            perm = None
        conn.updateGroup(group, name, perm, listOfOwners, description)
        
        new_members = getSelectedExperimenters(conn, mergeLists(members,owners))
        conn.setMembersOfGroup(group, new_members)
    else:
        raise Exception(form.errors.as_text())            

def _createExperimenter(request, conn):
    # create experimenter
    groups = list(conn.getObjects("ExperimenterGroup"))
    groups.sort(key=lambda x: x.getName().lower())
    
    name_check = conn.checkOmeName(request.REQUEST.get('omename'))
    email_check = conn.checkEmail(request.REQUEST.get('email'))
    
    initial={'with_password':True, 'groups':otherGroupsInitialList(groups)}
    form = ExperimenterForm(initial=initial, data=request.REQUEST.copy(), name_check=name_check, email_check=email_check)
    if form.is_valid():
        omename = form.cleaned_data['omename']
        firstName = form.cleaned_data['first_name']
        middleName = form.cleaned_data['middle_name']
        lastName = form.cleaned_data['last_name']
        email = form.cleaned_data['email']
        institution = form.cleaned_data['institution']
        admin = toBoolean(form.cleaned_data['administrator'])
        active = toBoolean(form.cleaned_data['active'])
        defaultGroup = form.cleaned_data['default_group']
        otherGroups = form.cleaned_data['other_groups']
        password = form.cleaned_data['password']
        
        # default group
        for g in groups:
            if long(defaultGroup) == g.id:
                dGroup = g
                break

        listOfOtherGroups = set()
        # rest of groups
        for g in groups:
            for og in otherGroups:
                # remove defaultGroup from otherGroups if contains
                if long(og) == long(dGroup.id):
                    pass
                elif long(og) == g.id:
                    listOfOtherGroups.add(g)

        return conn.createExperimenter(omename, firstName, lastName, email, admin, active, dGroup, listOfOtherGroups, password, middleName, institution)
    else:
        raise Exception(form.errors.as_text())

def _updateExperimenter(request, conn, eid):
    groups = list(conn.getObjects("ExperimenterGroup"))
    groups.sort(key=lambda x: x.getName().lower())
    
    experimenter, defaultGroup, otherGroups, isLdapUser, hasAvatar = prepare_experimenter(conn, eid)
    
    name_check = conn.checkOmeName(request.REQUEST.get('omename'), experimenter.omeName)
    email_check = conn.checkEmail(request.REQUEST.get('email'), experimenter.email)
    initial={'active':True, 'groups':otherGroupsInitialList(groups)}
    
    form = ExperimenterForm(initial=initial, data=request.POST.copy(), name_check=name_check, email_check=email_check)
       
    if form.is_valid():
        omename = form.cleaned_data['omename']
        firstName = form.cleaned_data['first_name']
        middleName = form.cleaned_data['middle_name']
        lastName = form.cleaned_data['last_name']
        email = form.cleaned_data['email']
        institution = form.cleaned_data['institution']
        admin = toBoolean(form.cleaned_data['administrator'])
        active = toBoolean(form.cleaned_data['active'])
        defaultGroup = form.cleaned_data['default_group']
        otherGroups = form.cleaned_data['other_groups']
        
        # default group
        for g in groups:
            if long(defaultGroup) == g.id:
                dGroup = g
                break

        listOfOtherGroups = set()
        # rest of groups
        for g in groups:
            for og in otherGroups:
                # remove defaultGroup from otherGroups if contains
                if long(og) == long(dGroup.id):
                    pass
                elif long(og) == g.id:
                    listOfOtherGroups.add(g)

        conn.updateExperimenter(experimenter, omename, firstName, lastName, email, admin, active, dGroup, listOfOtherGroups, middleName, institution)
    else:
        raise Exception(form.errors.as_text())
