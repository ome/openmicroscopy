#!/usr/bin/env python
# encoding: utf-8
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

from webadmin.controller.experimenter import BaseExperimenter
from webadmin.controller.group import BaseGroup
from webadmin_test_library import WebTest, WebAdminClientTest

from webadmin.custom_models import Server

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
        
        s2 = Server.find('example2.com')
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


# Testing client, URLs
class WebAdminUrlTest(WebAdminClientTest):
    
    def test_login(self):
        params = {
            'username': 'root',
            'password': self.root_password,
            'server':self.server_id,
            'ssl':'on'
        }
        
        response = self.client.post(reverse(viewname="walogin"), params)
        # Check that the response was a 302 (redirect)
        self.failUnlessEqual(response.status_code, 302)
        self.failUnlessEqual(response['Location'], reverse(viewname="waindex"))
    
    def test_urlsAsRoot(self):        
        self.client.login('root', self.root_password)
        
        # response 200
        response = self.client.get(reverse(viewname="waexperimenters"))
        self.failUnlessEqual(response.status_code, 200)
        
        # response 200
        response = self.client.get(reverse(viewname="wamanageexperimenterid", args=["new"]))
        self.failUnlessEqual(response.status_code, 200)
        
        # response 302
        response = self.client.get(reverse(viewname="wamanageexperimenterid", args=["create"]))
        self.failUnlessEqual(response.status_code, 302)
        self.failUnlessEqual(response['Location'], reverse(viewname="wamanageexperimenterid", args=["new"]))
        
        # response 200
        response = self.client.get(reverse(viewname="wamanageexperimenterid", args=["edit", "1"]))
        self.failUnlessEqual(response.status_code, 200)
        
        # response 302
        response = self.client.get(reverse(viewname="wamanageexperimenterid", args=["save", "1"]))
        self.failUnlessEqual(response.status_code, 302)
        self.failUnlessEqual(response['Location'], reverse(viewname="wamanageexperimenterid", args=["edit", "1"]))
        
        # response 200
        response = self.client.get(reverse(viewname="wagroups"))
        self.failUnlessEqual(response.status_code, 200)
        
        # response 200
        response = self.client.get(reverse(viewname="wamanagegroupid", args=["new"]))
        self.failUnlessEqual(response.status_code, 200)
        
        # response 302
        response = self.client.get(reverse(viewname="wamanagegroupid", args=["create"]))
        self.failUnlessEqual(response.status_code, 302)
        self.failUnlessEqual(response['Location'], reverse(viewname="wamanagegroupid", args=["new"]))
        
        # response 200
        response = self.client.get(reverse(viewname="wamanagegroupid", args=["edit", "2"]))
        self.failUnlessEqual(response.status_code, 200)
        
        # response 302
        response = self.client.get(reverse(viewname="wamanagegroupid", args=["save", "2"]))
        self.failUnlessEqual(response.status_code, 302)
        self.failUnlessEqual(response['Location'], reverse(viewname="wamanagegroupid", args=["edit", "2"]))

        # response 200
        response = self.client.get(reverse(viewname="wamanagechangepasswordid", args=["1"]))
        self.failUnlessEqual(response.status_code, 200)
        

    def test_urlsAsUser(self):
        conn = self.rootconn
        uuid = conn._sessionUuid
        
        # private group
        params = {
            "name":"webadmin_test_group_private %s" % uuid,
            "description":"test group",
            "owners": [0L],
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
        
        # TODO:Create experimenter 
        self.client.login("webadmin_test_user %s" % uuid, '123')
        
        # response 200
        response = self.client.get(reverse(viewname="wamyaccount"))
        self.failUnlessEqual(response.status_code, 200)
        
        response = self.client.get(reverse(viewname="wadrivespace"))
        self.failUnlessEqual(response.status_code, 200)
        
        response = self.client.get(reverse(viewname="wamyphoto"))
        self.failUnlessEqual(response.status_code, 200)
        
        response = self.client.get(reverse(viewname="wamanagechangepasswordid", args=["4"]))
        self.failUnlessEqual(response.status_code, 200)
        
        # response 404
        response = self.client.get(reverse(viewname="waexperimenters"))
        self.failUnlessEqual(response.status_code, 404)
                
        response = self.client.get(reverse(viewname="wamanageexperimenterid", args=["new"]))
        self.failUnlessEqual(response.status_code, 404)
        
        response = self.client.get(reverse(viewname="wamanageexperimenterid", args=["create"]))
        self.failUnlessEqual(response.status_code, 404)
        
        response = self.client.get(reverse(viewname="wamanageexperimenterid", args=["edit", "1"]))
        self.failUnlessEqual(response.status_code, 404)
        
        response = self.client.get(reverse(viewname="wamanageexperimenterid", args=["save", "1"]))
        self.failUnlessEqual(response.status_code, 404)
        
        response = self.client.get(reverse(viewname="wagroups"))
        self.failUnlessEqual(response.status_code, 404)
        
        response = self.client.get(reverse(viewname="wamanagegroupid", args=["new"]))
        self.failUnlessEqual(response.status_code, 404)
        
        response = self.client.get(reverse(viewname="wamanagegroupid", args=["create"]))
        self.failUnlessEqual(response.status_code, 404)
        
        response = self.client.get(reverse(viewname="wamanagegroupid", args=["edit", "2"]))
        self.failUnlessEqual(response.status_code, 404)
        
        response = self.client.get(reverse(viewname="wamanagegroupid", args=["save", "2"]))
        self.failUnlessEqual(response.status_code, 404)
     
class WebAdminConfigTest(unittest.TestCase):
    
    def setUp (self):
        c = omero.client(pmap=['--Ice.Config='+(os.environ.get("ICE_CONFIG"))])
        try:
            self.root_password = c.ic.getProperties().getProperty('omero.rootpass')
            self.omero_host = c.ic.getProperties().getProperty('omero.host')
            self.omero_port = c.ic.getProperties().getProperty('omero.port')
        finally:
            c.__del__()
    
    def test_isServerOn(self):
        from omeroweb.webadmin.webadmin_utils import _isServerOn
        if not _isServerOn(self.omero_host, self.omero_port):
            self.fail('Server is offline')
            
    def test_checkVersion(self):
        from omeroweb.webadmin.webadmin_utils import _checkVersion
        if not _checkVersion(self.omero_host, self.omero_port):
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
        
        blitz = Server.get(pk=request.REQUEST.get('server')) 
        request.session['server'] = blitz.id
        request.session['host'] = blitz.host
        request.session['port'] = blitz.port
        request.session['username'] = request.REQUEST.get('username').encode('utf-8').strip()
        request.session['password'] = request.REQUEST.get('password').encode('utf-8').strip()
        request.session['ssl'] = (True, False)[request.REQUEST.get('ssl') is None]

        conn = webgateway_views.getBlitzConnection(request, useragent="TEST.webadmin")
        if conn is None:
            self.fail('Cannot connect')
        webgateway_views._session_logout(request, request.session.get('server'))
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
        
        form = LoginForm(data=request.REQUEST.copy())        
        if form.is_valid():
            
            blitz = Server.get(pk=form.cleaned_data['server']) 
            request.session['server'] = blitz.id
            request.session['host'] = blitz.host
            request.session['port'] = blitz.port
            request.session['username'] = form.cleaned_data['username'].strip()
            request.session['password'] = form.cleaned_data['password'].strip()
            request.session['ssl'] = form.cleaned_data['ssl']

            conn = webgateway_views.getBlitzConnection(request, useragent="TEST.webadmin")
            if conn is None:
                self.fail('Cannot connect')            
            webgateway_views._session_logout(request, request.session.get('server'))
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
        
        form = LoginForm(data=request.REQUEST.copy())        
        if form.is_valid():
            blitz = Server.get(pk=form.cleaned_data['server']) 
            request.session['server'] = blitz.id
            request.session['host'] = blitz.host
            request.session['port'] = blitz.port
            request.session['username'] = form.cleaned_data['username'].strip()
            request.session['password'] = form.cleaned_data['password'].strip()
            request.session['ssl'] = form.cleaned_data['ssl']

            conn = webgateway_views.getBlitzConnection(request, useragent="TEST.webadmin")
            if conn is not None:
                self.fail('This user does not exist. Login failure error!')
                webgateway_views._session_logout(request, request.session.get('server'))

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
            "permissions":0
        }
        request = fakeRequest(method="post", params=params)
        gid = _createGroup(request, conn)
           
        # check if group created
        controller = BaseGroup(conn, gid)
        perm = controller.getActualPermissions()
        self.assertEquals(params['name'], controller.group.name)
        self.assertEquals(params['description'], controller.group.description)
        self.assertEquals(sorted(params['owners']), sorted(controller.owners))
        self.assertEquals(params['permissions'], perm)
        self.assertEquals(False, controller.isReadOnly())
    
    def test_createGroups(self):        
        conn = self.rootconn
        uuid = conn._sessionUuid
        
        # private group
        params = {
            "name":"webadmin_test_group_private %s" % uuid,
            "description":"test group",
            "owners": [0L],
            "permissions":0
        }
        request = fakeRequest(method="post", params=params)
        gid = _createGroup(request, conn)
           
        # check if group created
        controller = BaseGroup(conn, gid)
        perm = controller.getActualPermissions()
        self.assertEquals(params['name'], controller.group.name)
        self.assertEquals(params['description'], controller.group.description)
        self.assertEquals(sorted(params['owners']), sorted(controller.owners))
        self.assertEquals(params['permissions'], perm)
        self.assertEquals(False, controller.isReadOnly())
        
        # read-only group
        params = {
            "name":"webadmin_test_group_read-only %s" % uuid,
            "description":"test group",
            "owners": [0L],
            "permissions":1,
            "readonly":True
        }
        request = fakeRequest(method="post", params=params)
        gid = _createGroup(request, conn)
              
        # check if group created
        controller = BaseGroup(conn, gid)
        perm = controller.getActualPermissions()
        self.assertEquals(params['name'], controller.group.name)
        self.assertEquals(params['description'], controller.group.description)
        self.assertEquals(sorted(params['owners']), sorted(controller.owners))
        self.assertEquals(params['permissions'], perm)
        self.assertEquals(params['readonly'], controller.isReadOnly())
    
    def test_updateGroups(self):        
        conn = self.rootconn
        uuid = conn._sessionUuid
        
        # private group
        params = {
            "name":"webadmin_test_group_private %s" % uuid,
            "description":"test group",
            "owners": [0L],
            "permissions":0
        }
        request = fakeRequest(method="post", params=params)
        gid = _createGroup(request, conn)
        
        # create new user
        params = {
            "omename":"webadmin_test_owner %s" % uuid,
            "first_name":uuid,
            "middle_name": uuid,
            "last_name":uuid,
            "email":"owner_%s@domain.com" % uuid,
            "institution":"Laboratory",
            "active":True,
            "default_group":gid,
            "other_groups":[gid],
            "password":"123",
            "confirmation":"123" 
        }
        request = fakeRequest(method="post", params=params)
        eid = _createExperimenter(request, conn)
        
        # upgrade group to collaborative
        # read-only group and add new owner
        params = {
            "name":"webadmin_test_group_read-only %s" % uuid,
            "description":"test group changed",
            "owners": [0, eid],
            "permissions":1,
            "readonly":True
        }
        request = fakeRequest(method="post", params=params)
        _updateGroup(request, conn, gid)
        
        # check if updated
        controller = BaseGroup(conn, gid)
        perm = controller.getActualPermissions()
        self.assertEquals(params['name'], controller.group.name)
        self.assertEquals(params['description'], controller.group.description)
        self.assertEquals(sorted(params['owners']), sorted(controller.owners))
        self.assertEquals(params['permissions'], perm)
        self.assertEquals(params['readonly'], controller.isReadOnly())
        
    def test_updateMembersOfGroup(self):
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
        
        ######################################
        # default group - helper
        params = {
            "name":"webadmin_test_default %s" % uuid,
            "description":"test group default",
            "owners": [0L],
            "permissions":0
        }
        request = fakeRequest(method="post", params=params)
        default_gid = _createGroup(request, conn)
        
        # create two new users
        params = {
            "omename":"webadmin_test_user1 %s" % uuid,
            "first_name":uuid,
            "middle_name": uuid,
            "last_name":uuid,
            "email":"user1_%s@domain.com" % uuid,
            "institution":"Laboratory",
            "active":True,
            "default_group":default_gid,
            "other_groups":[default_gid],
            "password":"123",
            "confirmation":"123" 
        }
        request = fakeRequest(method="post", params=params)
        eid1 = _createExperimenter(request, conn)
        
        # create few new users
        params = {
            "omename":"webadmin_test_user2 %s" % uuid,
            "first_name":uuid,
            "middle_name": uuid,
            "last_name":uuid,
            "email":"user2_%s@domain.com" % uuid,
            "institution":"Laboratory",
            "active":True,
            "default_group":default_gid,
            "other_groups":[default_gid],
            "password":"123",
            "confirmation":"123" 
        }
        request = fakeRequest(method="post", params=params)
        eid2 = _createExperimenter(request, conn)
        # make other users a member of the group
        
        # add them to group
        params = {
            'available':[],
            'members':[0,eid1,eid2]
        }
        request = fakeRequest(method="post", params=params)
        
        controller = BaseGroup(conn, gid)
        controller.containedExperimenters()
        form = ContainedExperimentersForm(initial={'members':controller.members, 'available':controller.available})
        if not form.is_valid():
            #available = form.cleaned_data['available']
            available = request.POST.getlist('available')
            #members = form.cleaned_data['members']
            members = request.POST.getlist('members')
            controller.setMembersOfGroup(available, members)        
            
        # check if updated
        controller = BaseGroup(conn, gid)
        controller.containedExperimenters()        
        self.assertEquals(sorted(params['members']), sorted([e.id for e in controller.members]))

        # remove them from the group
        params = {
            'available':[eid1,eid2],
            'members':[0]
        }
        request = fakeRequest(method="post", params=params)
        
        controller = BaseGroup(conn, gid)
        controller.containedExperimenters()
        form = ContainedExperimentersForm(initial={'members':controller.members, 'available':controller.available})
        if not form.is_valid():
            #available = form.cleaned_data['available']
            available = request.POST.getlist('available')
            #members = form.cleaned_data['members']
            members = request.POST.getlist('members')
            controller.setMembersOfGroup(available, members)
            
        # check if updated
        controller = BaseGroup(conn, gid)
        controller.containedExperimenters()
        self.assertEquals(sorted(params['members']), sorted([e.id for e in controller.members]))
        
    def test_createExperimenters(self):        
        conn = self.rootconn
        uuid = conn._sessionUuid
        
        # private group
        params = {
            "name":"webadmin_test_group_private %s" % uuid,
            "description":"test group",
            "owners": [0L],
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
        controller = BaseExperimenter(conn, eid)
        self.assertEquals(params['omename'], controller.experimenter.omeName)
        self.assertEquals(params['first_name'], controller.experimenter.firstName)
        self.assertEquals(params['middle_name'], controller.experimenter.middleName)
        self.assertEquals(params['last_name'], controller.experimenter.lastName)
        self.assertEquals(params['email'], controller.experimenter.email)
        self.assertEquals(params['institution'], controller.experimenter.institution)
        self.assert_(not controller.experimenter.isAdmin())
        self.assertEquals(params['active'], controller.experimenter.isActive())
        self.assertEquals(params['default_group'], controller.defaultGroup)        
        self.assertEquals(sorted(params['other_groups']), sorted(controller.otherGroups))
        
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
        controller = BaseExperimenter(conn, eid)
        self.assertEquals(params['omename'], controller.experimenter.omeName)
        self.assertEquals(params['first_name'], controller.experimenter.firstName)
        self.assertEquals(params['middle_name'], controller.experimenter.middleName)
        self.assertEquals(params['last_name'], controller.experimenter.lastName)
        self.assertEquals(params['email'], controller.experimenter.email)
        self.assertEquals(params['institution'], controller.experimenter.institution)
        self.assertEquals(params['administrator'], controller.experimenter.isAdmin())
        self.assertEquals(params['active'], controller.experimenter.isActive())
        self.assertEquals(params['default_group'], controller.defaultGroup)        
        self.assertEquals(sorted(params['other_groups']), sorted(controller.otherGroups))
        
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
        controller = BaseExperimenter(conn, eid)
        self.assertEquals(params['omename'], controller.experimenter.omeName)
        self.assertEquals(params['first_name'], controller.experimenter.firstName)
        self.assertEquals(params['middle_name'], controller.experimenter.middleName)
        self.assertEquals(params['last_name'], controller.experimenter.lastName)
        self.assertEquals(params['email'], controller.experimenter.email)
        self.assertEquals(params['institution'], controller.experimenter.institution)
        self.assert_(not controller.experimenter.isAdmin())
        self.assert_(not controller.experimenter.isActive())
        self.assertEquals(params['default_group'], controller.defaultGroup)        
        self.assertEquals(sorted(params['other_groups']), sorted(controller.otherGroups))
    
    def test_updateExperimenter(self):        
        conn = self.rootconn
        uuid = conn._sessionUuid
        
        # private group
        params = {
            "name":"webadmin_test_group_private %s" % uuid,
            "description":"test group",
            "owners": [0L],
            "permissions":0
        }
        
        request = fakeRequest(method="post", params=params)
        gid = _createGroup(request, conn)
        
        # default group - helper
        params = {
            "name":"webadmin_test_default %s" % uuid,
            "description":"test group default",
            "owners": [0L],
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
        controller = BaseExperimenter(conn, eid)
        self.assertEquals(params['omename'], controller.experimenter.omeName)
        self.assertEquals(params['first_name'], controller.experimenter.firstName)
        self.assertEquals(params['middle_name'], controller.experimenter.middleName)
        self.assertEquals(params['last_name'], controller.experimenter.lastName)
        self.assertEquals(params['email'], controller.experimenter.email)
        self.assertEquals(params['institution'], controller.experimenter.institution)
        self.assertEquals(params['administrator'], controller.experimenter.isAdmin())
        self.assertEquals(params['active'], controller.experimenter.isActive())
        self.assertEquals(params['default_group'], controller.defaultGroup)        
        self.assertEquals(sorted(params['other_groups']), sorted(controller.otherGroups))
        
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
        controller = BaseExperimenter(conn, eid)
        self.assertEquals(params['omename'], controller.experimenter.omeName)
        self.assertEquals(params['first_name'], controller.experimenter.firstName)
        self.assertEquals(params['middle_name'], controller.experimenter.middleName)
        self.assertEquals(params['last_name'], controller.experimenter.lastName)
        self.assertEquals(params['email'], controller.experimenter.email)
        self.assertEquals(params['institution'], controller.experimenter.institution)
        self.assert_(not controller.experimenter.isAdmin())
        self.assert_(not controller.experimenter.isActive())
        self.assertEquals(params['default_group'], controller.defaultGroup)        
        self.assertEquals(sorted(params['other_groups']), sorted(controller.otherGroups))
        
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
            "owners": [0L],
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
    controller = BaseGroup(conn)
    name_check = conn.checkGroupName(request.REQUEST.get('name'))
    form = GroupForm(initial={'experimenters':controller.experimenters}, data=request.POST.copy(), name_check=name_check)
    if form.is_valid():
        name = form.cleaned_data['name']
        description = form.cleaned_data['description']
        owners = form.cleaned_data['owners']
        permissions = form.cleaned_data['permissions']
        readonly = toBoolean(form.cleaned_data['readonly'])
        return controller.createGroup(name, owners, permissions, readonly, description)
    else:
        raise Exception(form.errors.as_text())

def _updateGroup(request, conn, gid):
    # update group
    controller = BaseGroup(conn, gid)
    name_check = conn.checkGroupName(request.REQUEST.get('name'), controller.group.name)
    form = GroupForm(initial={'experimenters':controller.experimenters}, data=request.POST.copy(), name_check=name_check)
    if form.is_valid():
        name = form.cleaned_data['name']
        description = form.cleaned_data['description']
        owners = form.cleaned_data['owners']
        permissions = form.cleaned_data['permissions']
        readonly = toBoolean(form.cleaned_data['readonly'])
        controller.updateGroup(name, owners, permissions, readonly, description)
    else:
        raise Exception(form.errors.as_text())            

def _createExperimenter(request, conn):
    # create experimenter
    controller = BaseExperimenter(conn)
    name_check = conn.checkOmeName(request.REQUEST.get('omename'))
    email_check = conn.checkEmail(request.REQUEST.get('email'))

    initial={'with_password':True}

    exclude = list()            
    if len(request.REQUEST.getlist('other_groups')) > 0:
        others = controller.getSelectedGroups(request.REQUEST.getlist('other_groups'))   
        initial['others'] = others
        initial['default'] = [(g.id, g.name) for g in others]
        exclude.extend([g.id for g in others])

    available = controller.otherGroupsInitialList(exclude)
    initial['available'] = available
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
        return controller.createExperimenter(omename, firstName, lastName, email, admin, active, defaultGroup, otherGroups, password, middleName, institution)
    else:
        raise Exception(form.errors.as_text())

def _updateExperimenter(request, conn, eid):
    # update experimenter
    controller = BaseExperimenter(conn, eid)
    name_check = conn.checkOmeName(request.REQUEST.get('omename'), controller.experimenter.omeName)
    email_check = conn.checkEmail(request.REQUEST.get('email'), controller.experimenter.email)
    
    initial={'active':True}
    exclude = list()

    if len(request.REQUEST.getlist('other_groups')) > 0:
        others = controller.getSelectedGroups(request.REQUEST.getlist('other_groups'))   
        initial['others'] = others
        initial['default'] = [(g.id, g.name) for g in others]
        exclude.extend([g.id for g in others])

    available = controller.otherGroupsInitialList(exclude)
    initial['available'] = available

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
        controller.updateExperimenter(omename, firstName, lastName, email, admin, active, defaultGroup, otherGroups, middleName, institution)
    else:
        raise Exception(form.errors.as_text())