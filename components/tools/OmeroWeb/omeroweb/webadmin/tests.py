#!/usr/bin/env python
# encoding: utf-8
# 
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
# Author: Colin Blackburn, 2008.
# 
# Version: 1.0
#

"""
tests.py

"""

import unittest

# Some methods in unittest.TestCase seem to be overridden by django.test.TestCase
# Classes here are subclassed from django.test.TestCase so tc is provided as a hook
# to access some of the methods in unittest.TestCase
from unittest import TestCase as tc

from django.test.client import Client
from django.test import TestCase
from django import forms

from webadmin.forms import LoginForm

# If omero is not imported here then later omero imports appear to fail.
import omero

# If these two imports are not made here then a bus error results
import omero_api_IScript_ice
import omero_api_Gateway_ice


"""
Contents of fixture database text file: testdb.json

[
  {
    "model": "webadmin.gateway",
    "pk": 1,
    "fields": {
      "server": "omero",
      "host": "localhost"
      "port": 4064
    }
  }
]

"""


class LoginFormStaticTestCase(TestCase):
    """
        Test the form on the login page.The test check the integrity of th forms
        fields and validation. No server is necessary for static tests.
        
    """
    # A fixture database that is created and destroyed between each test.
    fixtures = ["webadmin/testdb.json"]
    
    def setUp(self):
        """ No specific set-up. """
        pass

    def tearDown(self):
        """ No specific tear-down. """
        pass
    
    def test01(self):        
        """ A form with no data set should be unbound and invalid. """
        form = LoginForm()
        self.assertEquals(False, form.is_bound)
        self.assertEquals(False, form.is_valid())
                 
    def test02(self):
        """ A form with empty data set should be bound but invalid."""
        form = LoginForm({})
        self.assertEquals(True, form.is_bound)
        self.assertEquals(False, form.is_valid())

    def test03(self):
        """ The base field is required, and should validate against a db entry."""
        # This result is dependent on the entry in the fixture database
        result = 'localhost:4063'
        
        form = LoginForm({})
        field = form.fields['server']
        import pdb
        #self.assertEquals('...', field.empty_label)
        self.assertEquals(result, str(field.clean(1)))
        self.assertRaises(forms.ValidationError, field.clean, 2)   
        self.assertEquals(False, form.is_valid())
                              
    def test04(self):
        """ The username field is required and should fail to validate is empty."""
        data = 'omero'
        
        form = LoginForm({})
        field = form.fields['username']
        self.assertEquals(True, field.required)
        self.assertEquals(data, field.clean(data))
        self.assertRaises(forms.ValidationError, field.clean, '')   
        self.assertEquals(False, form.is_valid())
        
    def test05(self):
        """ The password field is required and should fail to validate is empty."""
        data = 'omero'
        
        form = LoginForm({})
        field = form.fields['password']
        self.assertEquals(True, field.required)
        self.assertEquals(data, field.clean(data))
        self.assertRaises(forms.ValidationError, field.clean, '')           
        self.assertEquals(False, form.is_valid())

    def test06(self):
        """ The form with all fields filled and a valid choice for base should validate"""
        # This fixture depends on there being at least one entry in the fixture database
        data = {'server':1, 'username': 'omero', 'password':'omero'}
        
        form = LoginForm(data)
        self.assertEquals(True, form.is_valid())
        
    def test07(self):
        """The form with all fields filled and a valid choice for base should validate"""
        # This fixture depends on there being at least one entry in the fixture database
        data = {'server':1, 'username': 'nonsense', 'password':'garbage'}
        
        form = LoginForm(data)
        self.assertEquals(True, form.is_valid())
        
    def test08(self):
        """The form with all fields filled and an invalid choice for base should not validate"""
        # This fixture depends on there being no more than 10 entries in the fixture database
        data = {'server':11, 'username': 'omero', 'password':'omero'}

        form = LoginForm(data)
        self.assertEquals(False, form.is_valid())
        
  
class LoginFormDynamicTestCase(TestCase):
    """
        This classes tests the GETting and POSTing involved in using the
        login page. 
        
    """
    # A fixture database that is created and destroyed between each test.
    fixtures = ["webadmin/testdb.json"]
    
    # A set of known login details. This depends on the above fixture database.
    knownLoginDetails = {'server':1, 'login': 'root', 'password': 'ome'}
                
    def test01(self):
        "GET login page"
        response = self.client.get('/webadmin')
        self.assertEquals(response.status_code, 301)
        response =  self.client.get('/webadmin/')
        expectedIfRedirect = "Location: http://testserver/webadmin/login/"
        self.assertNotEquals(-1, str(response).find(expectedIfRedirect))
        
        response = self.client.get('/webadmin/login')
        self.assertEquals(response.status_code, 301)
        response =  self.client.get('/webadmin/login/')
        self.assertEquals(response.status_code, 200)
        self.assertTemplateUsed(response, 'webadmin/login.html')

    def test02(self):
        "POST invalid gateway details"
        # This fixture depends on there being no more than 10 entries in the fixture database
        data = {'server':11, 'login': 'root', 'password': 'ome'}

        tc.assertRaises(self, models.Gateway.DoesNotExist, 
            self.client.post, '/webadmin/login/', data)

    def test03(self):
        "POST invalid login name"
        # This fixture depends on there being at least one entry in the fixture database.
        # This fixture depends on there not being an omero user called broot.
        data = {'server':1, 'username': 'broot', 'password': 'ome'}
        expectedError = "Error: Connection not available"

        response =  self.client.post('/webadmin/login/', data)       
        self.assertContains(response, expectedError)

    def test04(self):
        "POST invalid password details"
        # This fixture depends on there being at least one entry in the fixture database.
        # This fixture depends on there being an omero user called root without this password.
        data = {'server':1, 'username': 'root', 'password': 'home'}
        expectedError = "Error: Connection not available"

        response =  self.client.post('/webadmin/login/', data)       
        self.assertContains(response, expectedError)

    def test05(self):
        "POST valid root login details, then log out"
        # This fixture depends on there being at least one entry in the fixture database
        # This fixture depends on there being an omero user called root with this password.
        data = {'server':1, 'username': 'root', 'password': 'ome'}
        expectedIfLoggedIn = "Location: http://testserver/webadmin/experimenters/"
        expectedIfLoggedOut = "Location: http://testserver/webadmin/login/"

        # Redirect response assertContains() can't be used apparently.
        response =  self.client.post('/webadmin/login/', data)  
        self.assertEquals(-1, str(response).find(expectedIfLoggedIn))
        # Redirect response assertContains() can't be used apparently.
        response = self.client.post('/webadmin/logout/')
        self.assertEquals(-1, str(response).find(expectedIfLoggedOut))

    def test06(self):
        "POST valid user login details, then log out"
        # This fixture depends on there being an omero user called root with this password.
        # This fixture depends on there being an omero user called omero with this password.
        data = {'server':1, 'username': 'omero', 'password': 'omero'}
        expectedIfLoggedIn = "Location: http://testserver/webadmin/myaccount/"
        expectedIfLoggedOut = "Location: http://testserver/webadmin/login/"

        # Redirect response assertContains() can't be used apparently.
        response =  self.client.post('/webadmin/login/', data)  
        self.assertEquals(-1, str(response).find(expectedIfLoggedIn))
        # Redirect response assertContains() can't be used apparently.
        response = self.client.post('/webadmin/logout/')
        self.assertEquals(-1, str(response).find(expectedIfLoggedOut))

        

