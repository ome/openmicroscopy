#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   setuptools entry point

   Tests run by default using the OmeroPy/dist egg as the omero python lib but
   you can override that by using the --test-pythonpath flag to ./setup.py
   test.

   For testing that require a running Omero server, the ice.config file must
   exist and hold the proper configuration either at the same directory as
   this file or in some place pointed to by the --test-ice-config flag to
   ./setup.py test.

   For example:

      # this will run all tests under OmeroPy/test/
      ./setup.py test
       # run all tests under OmeroPy/test/gatewaytest
      ./setup.py test -s test/gatewaytest
      # run all tests that include TopLevelObjects in the name
      ./setup.py test -k TopLevelObjects
      # exit on first failure
      ./setup.py test -x
      # drop to the pdb debugger on failure
      ./setup.py test --pdb


   Copyright 2007-2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import glob
import sys
import os

sys.path.append("..")
from test_setup import PyTest

for tools in glob.glob("../../../lib/repository/setuptools*.egg"):
    if tools.find(".".join(map(str, sys.version_info[0:2]))) > 0:
        sys.path.insert(0, os.path.abspath(tools))

from ez_setup import use_setuptools
use_setuptools(to_dir='../../../lib/repository')
from setuptools import setup, find_packages
from omero_version import omero_version as ov

if os.path.exists("target"):
    packages = find_packages("target")+[""]
else:
    packages = [""]

setup(
    name="omero_client",
    version=ov,
    description="Python bindings to the OMERO.blitz server",
    long_description="Python bindings to the OMERO.blitz server.",
    author="Josh Moore",
    author_email="josh@glencoesoftware.com",
    url="http://trac.openmicroscopy.org.uk/ome/wiki/OmeroPy",
    download_url="http://trac.openmicroscopy.org.uk/ome/wiki/OmeroPy",
    package_dir={"": "target"},
    packages=packages,
    package_data={
        'omero.gateway': ['pilfonts/*'],
        'omero.gateway.scripts': ['imgs/*']},
    cmdclass={'test': PyTest},
    tests_require=['pytest'])
