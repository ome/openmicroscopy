#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   PyTest command class

   setup.py files in each package should include a cmdclass mapping
   from "test" to PyTest.

   Copyright 2007-2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import sys

from setuptools.command.test import test as TestCommand

LIB = os.path.join("..", "..", "tests", "python")
sys.path.insert(0, LIB)


class PyTest(TestCommand):

    user_options = [
        ('test-path=', 't', "base dir for test collection"),
        ('test-pythonpath=', 'p', "prepend 'pythonpath' to PYTHONPATH"),
        ('test-ice-config=', 'i',
         "use specified 'ice config' file instead of default"),
        ('test-string=', 'k', "only run tests including 'string'"),
        ('test-marker=', 'm', "only run tests including 'marker'"),
        ('test-no-capture', 's', "don't suppress test output"),
        ('test-failfast', 'x', "Exit on first error"),
        ('test-verbose', 'v', "more verbose output"),
        ('test-quiet', 'q', "less verbose output"),
        ('junitxml=', None, "create junit-xml style report file at 'path'"),
        ('pdb', None, "fallback to pdb on error"),
        ('markers', None, "list available markers'"),
        ]

    def initialize_options(self):
        TestCommand.initialize_options(self)
        self.test_pythonpath = None
        self.test_ice_config = None
        self.test_string = None
        self.test_marker = None
        self.test_path = None
        self.test_no_capture = False
        self.test_failfast = False
        self.test_quiet = False
        self.test_verbose = False
        self.junitxml = None
        self.pdb = False
        self.markers = False

    def finalize_options(self):
        TestCommand.finalize_options(self)
        if self.test_path is None:
            self.test_path = 'test'
        self.test_args = [self.test_path]
        if self.test_string is not None:
            self.test_args.extend(['-k', self.test_string])
        if self.test_marker is not None:
            self.test_args.extend(['-m', self.test_marker])
        if self.test_no_capture:
            self.test_args.extend(['-s'])
        if self.test_failfast:
            self.test_args.extend(['-x'])
        if self.test_verbose:
            self.test_args.extend(['-v'])
        if self.test_quiet:
            self.test_args.extend(['-q'])
        if self.junitxml is not None:
            self.test_args.extend(['--junitxml', self.junitxml])
        if self.pdb:
            self.test_args.extend(['--pdb'])
        print self.test_failfast
        self.test_suite = True
        if self.markers:
            self.test_args = "--markers"
        if self.test_ice_config is None:
            self.test_ice_config = os.path.abspath('ice.config')
        if 'ICE_CONFIG' not in os.environ:
            os.environ['ICE_CONFIG'] = self.test_ice_config

    def run_tests(self):
        if self.test_pythonpath is not None:
            sys.path.insert(0, self.test_pythonpath)
        # import here, cause outside the eggs aren't loaded
        import pytest
        errno = pytest.main(self.test_args)
        sys.exit(errno)
