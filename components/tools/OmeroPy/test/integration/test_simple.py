#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Simple integration test which makes various calls on the
   a running server.

   Copyright 2008-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import library as lib


class TestSimple(lib.ITest):

    DEFAULT_PERMS = 'rwra--'  # Override DEFAULT_PERMS of ITest

    def testUserId(self):
        assert self.ctx.userId == self.user.id.val

    def testGroupId(self):
        assert self.ctx.groupId == self.group.id.val

    def testGroupPermissions(self):
        assert str(self.group.details.permissions) == self.DEFAULT_PERMS
