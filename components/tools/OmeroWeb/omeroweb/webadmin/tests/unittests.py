import unittest, time, os, datetime
import tempfile

import omero

from django.http import QueryDict
from django.conf import settings

from webgateway import views as webgateway_views
from webadmin.forms import LoginForm, GroupForm
                   
from webadmin.controller.experimenter import BaseExperimenter
from webadmin.controller.group import BaseGroup
from webadmin_test_library import WebTest, fakeRequest


class WebAdminTest(WebTest):
    
    def test_isServerOn(self):
        from omeroweb.webadmin.views import _isServerOn
        if not _isServerOn('localhost', 4064):
            self.fail('Server is offline')
            
    def test_checkVersion(self):
        from omeroweb.webadmin.views import _checkVersion
        if not _checkVersion('localhost', 4064):
            self.fail('Client version does not match server')
    
    def test_loginFromRequest(self):
        params = {
            'username': 'root',
            'password': self.root_password,
            'server':1,
            'ssl':'on'
        }        
        request = fakeRequest(method="post", params=params)
        
        blitz = settings.SERVER_LIST.get(pk=request.REQUEST.get('server')) 
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
            self.fail('Cnnection was not closed')

    def test_loginFromForm(self):
        params = {
            'username': 'root',
            'password': self.root_password,
            'server':1,
            'ssl':'on'
        }        
        request = fakeRequest(method="post", params=params)
        
        form = LoginForm(data=request.REQUEST.copy())        
        if form.is_valid():
            
            blitz = settings.SERVER_LIST.get(pk=form.cleaned_data['server']) 
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
                self.fail('Cnnection was not closed')
            
        else:
            errors = form.errors.as_text()
            self.fail(errors)
            
    def test_loginFailure(self):
        params = {
            'username': 'notauser',
            'password': 'nonsence',
            'server':1
        }        
        request = fakeRequest(method="post", params=params)
        
        form = LoginForm(data=request.REQUEST.copy())        
        if form.is_valid():
            blitz = settings.SERVER_LIST.get(pk=form.cleaned_data['server']) 
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
    
    def test_createGroup(self):        
        conn = self.rootconn
        uuid = conn._sessionUuid
        
        params = {
            "name":"webadmin_test_group_private %s" % uuid,
            "description":"test group",
            "owners": ['0'],
            "permissions":'0'
        }        
        request = fakeRequest(method="post", params=params)

        #create group
        controller = BaseGroup(conn)
        name_check = conn.checkGroupName(request.REQUEST.get('name'))

        name_check = conn.checkGroupName(request.REQUEST.get('name'))
        form = GroupForm(initial={'experimenters':controller.experimenters}, data=request.POST.copy(), name_check=name_check)
        if form.is_valid():
            name = form.cleaned_data['name']
            description = form.cleaned_data['description']
            owners = form.cleaned_data['owners']
            permissions = form.cleaned_data['permissions']
            readonly = form.cleaned_data['readonly']
            controller.createGroup(name, owners, permissions, readonly, description)
        else:
            errors = form.errors.as_text()
            self.fail(errors)
    