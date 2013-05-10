#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013 Glencoe Software, Inc. All Rights Reserved.
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
   Integration test suite specifically for FS development.
"""

import logging
logging.basicConfig(level=logging.WARN)

import unittest
from omero_ext import xmlrunner

class TopLevel(unittest.TestCase):
    pass

def additional_tests():
    load = unittest.defaultTestLoader.loadTestsFromName
    suite = unittest.TestSuite()

    suite.addTest(load("integration.chgrp"))
    suite.addTest(load("integration.delete"))
    suite.addTest(load("integration.reporawfilestore"))
    suite.addTest(load("integration.repository"))
    suite.addTest(load("gatewaytest.fs"))

    return suite

if __name__ == "__main__":
    xmlrunner.XMLTestRunner(verbose=True, output='target/reports').run(additional_tests())
