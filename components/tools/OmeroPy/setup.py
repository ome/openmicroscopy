#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   setuptools entry point

   Tests run by default using the OmeroPy/dist egg as the omero python lib but you can override
   that by using the --test-pythonpath flag to ./setup.py test.

   For testing that require a running Omero server, the ice.config file must exist and hold
   the proper configuration either at the same directory as this file or in some place
   pointed to by the --test-ice-config flag to ./setup.py test.

   For example:

      ./setup.py test # this will run all tests under OmeroPy/test/
      ./setup.py test -s test/gatewaytest # run all tests under OmeroPy/test/gatewaytest
      ./setup.py test -k TopLevelObjects # run all tests that include TopLevelObjects in the name
      ./setup.py test -x # exit on first failure
      ./setup.py test --pdb # drop to the pdb debugger on failure
      

   Copyright 2007-2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
from setuptools.command.test import test as TestCommand

import glob
import sys
import os

for tools in glob.glob("../../../lib/repository/setuptools*.egg"):
    if tools.find(".".join(map(str, sys.version_info[0:2]))) > 0:
       sys.path.insert(0, tools)

from ez_setup import use_setuptools
use_setuptools(to_dir='../../../lib/repository')
from setuptools import setup, find_packages
from omero_version import omero_version as ov

if os.path.exists("target"):
    packages = find_packages("target")+[""]
else:
    packages = [""]

class PyTest(TestCommand):
    user_options = TestCommand.user_options + \
                   [('test-pythonpath=', 'p', "prepend 'pythonpath' to PYTHONPATH"),
                    ('test-ice-config=', 'i', "use specified 'ice config' file instead of default"),
                    ('test-string=', 'k', "only run tests including 'string'"),
                    ('test-path=', 's', "base dir for test collection"),
                    ('test-failfast', 'x', "Exit on first error"),
                    ('test-verbose', 'v', "more verbose output"),
                    ('test-quiet', 'q', "less verbose output"),
                    ('pdb',None,"fallback to pdb on error"),]
    def initialize_options(self):
        TestCommand.initialize_options(self)
        self.test_pythonpath = None
        self.test_ice_config = None
        self.test_string = None
        self.test_path = None
        self.test_failfast = False
        self.test_quiet = False
        self.test_verbose = False
        self.pdb = False
    def finalize_options(self):
        TestCommand.finalize_options(self)
        if self.test_path is None:
            self.test_path = 'test'
        self.test_args = [self.test_path]
        if self.test_string is not None:
            self.test_args.extend(['-k', self.test_string])
        if self.test_failfast:
            self.test_args.extend(['-x'])
        if self.test_verbose:
            self.test_args.extend(['-v'])
        if self.test_quiet:
            self.test_args.extend(['-q'])
        if self.pdb:
            self.test_args.extend(['--pdb'])
        print self.test_failfast
        self.test_suite = True
        if self.test_ice_config is None:
            self.test_ice_config = os.path.abspath('ice.config')
        if not os.environ.has_key('ICE_CONFIG'):
            os.environ['ICE_CONFIG'] = self.test_ice_config
    def run_tests(self):
        if self.test_pythonpath is not None:
            sys.path.insert(0, self.test_pythonpath)
        #import here, cause outside the eggs aren't loaded
        import pytest
        errno = pytest.main(self.test_args)
        sys.exit(errno)

setup(name="omero_client",
      version=ov,
      description="Python bindings to the OMERO.blitz server",
      long_description="""\
Python bindings to the OMERO.blitz server.
""",
      author="Josh Moore",
      author_email="josh@glencoesoftware.com",
      url="http://trac.openmicroscopy.org.uk/ome/wiki/OmeroPy",
      download_url="http://trac.openmicroscopy.org.uk/ome/wiki/OmeroPy",
      package_dir = {"": "target"},
      packages=packages,
      package_data={'omero.gateway':['pilfonts/*'], 'omero.gateway.scripts':['imgs/*']},
      cmdclass = {'test': PyTest},
      tests_require=['pytest'],
)



