#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   setuptools entry point

   This script is used by the ant build (build.xml) to
   package and test the OmeroPy bindings. For most uses,
   see ant.

   Testing specific portions of OmeroPy, however, is easier
   from this script after "ant tools-build" has been invoked.

   For example:

      ./setup.py test -s test.pkg # Be careful of non test scripts
      ./setup.py test -s test.pkg.module
      ./setup.py test -s test.pkg.module.Class
      ./setup.py test -s test.pkg.module.Class:function

   Copyright 2007 Glencoe Software, Inc. All rights reserved.
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
                   [('test-path=', 'p', "prepend 'path' to PYTHONPATH"),
                    ('test-ice-config=', 'i', "use specified 'ice config' file instead of default")]
    def initialize_options(self):
        TestCommand.initialize_options(self)
        self.test_path = None
        self.test_ice_config = None
    def finalize_options(self):
        TestCommand.finalize_options(self)
        self.test_args = ['test']
        self.test_suite = True
        if self.test_ice_config is None:
            self.test_ice_config = 'ice.config'
        if not os.environ.has_key('ICE_CONFIG'):
            os.environ['ICE_CONFIG'] = self.test_ice_config
    def run_tests(self):
        if self.test_path is not None:
            sys.path.insert(0, self.test_path)
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
      test_suite='test.suite',
      cmdclass = {'pytest': PyTest},
)

#    tests_require=['pytest'],


