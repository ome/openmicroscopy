#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   conftest.py - py.test fixtures for gatewaytest

   Copyright 2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from django.http import QueryDict

from omero.gateway.pytest_fixtures import TestDBHelper
from omeroweb.decorators import login_required
from omeroweb.webgateway import views

from test_webgateway import fakeRequest


class GatewayWrapper (TestDBHelper):
    def __init__(self):
        super(GatewayWrapper, self).__init__()
        self.setUp(skipTestDB=False, skipTestImages=True)

    def doLogin(self, user=None):
        self.gateway = None
        if user:
            r = fakeRequest()
            q = QueryDict('', mutable=True)
            q.update({'username': user.name, 'password': user.passwd})
            r.REQUEST.dicts += (q,)
            t = login_required(isAdmin=user.admin)
            self.gateway = t.get_connection(1, r)  # , group=user.groupname)
        if self.gateway is None:
            # If the login framework was customized (using this app outside
            # omeroweb) the above fails
            super(GatewayWrapper, self).doLogin(user)
            self.gateway.user = views.UserProxy(self.gateway)
