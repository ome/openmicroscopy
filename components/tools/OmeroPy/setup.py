#!/usr/bin/env python
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

from ez_setup import use_setuptools
use_setuptools()
from setuptools import setup

setup(name="OmeroPy",
      version="3.0-Beta3.1",
      description="Python bindings to the OMERO.blitz server",
      long_description="""\
Python bindings to the OMERO.blitz server.
""",
      author="Josh Moore",
      author_email="josh@glencoesoftware.com",
      url="https://trac.openmicroscopy.org.uk/omero/wiki/OmeroPy",
      download_url="https://trac.openmicroscopy.org.uk/omero/wiki/OmeroPy",
      package_dir = {"": "target"},
      packages=['', 'omero', 'omero.plugins','omero.model','omero.api','omero.util','omero.romio','omero.util','omero_ext'],
      test_suite='test.suite'
)

