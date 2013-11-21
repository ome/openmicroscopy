#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the return code functionality

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
from path import path
from omero.cli import BaseControl, CLI

omeroDir = path(os.getcwd()) / "build"


class TestRCode(object):

    class T(BaseControl):
        def __call__(self, *args):
            self.ctx.rv = 1

    def testOne(self):
        cli = CLI()
        cli.register("t", TestRCode.T, "TEST")
        cli.invoke(["t"])
        assert cli.rv == 1, cli.rv
