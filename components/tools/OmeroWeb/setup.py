#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   Copyright 2008-2016 Glencoe Software, Inc. All rights reserved.
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

if "test" in sys.argv:
    os.environ.setdefault('OMERO_HOME', os.path.abspath(
        os.path.join("..", "..", "..", "dist")))

    sys.path.insert(0, os.path.join("..", "target", "lib", "fallback"))
    LIB = os.path.join("..", "target", "lib", "python")
    sys.path.insert(0, LIB)
    OMEROWEB_LIB = os.path.join(LIB, "omeroweb")
    sys.path.insert(1, OMEROWEB_LIB)

    os.environ.setdefault("DJANGO_SETTINGS_MODULE", "omeroweb.settings")

    import django
    if django.VERSION > (1, 7):
        django.setup()

from ez_setup import use_setuptools
use_setuptools(to_dir='../../../lib/repository')
from setuptools import setup
from omero_version import omero_version as ov

setup(name="OmeroWeb",
      version=ov,
      description="OmeroWeb",
      long_description="""\
OmeroWeb is the container of the web clients for OMERO."
""",
      author="The Open Microscopy Team",
      author_email="",
      url="https://github.com/openmicroscopy/openmicroscopy/",
      download_url="https://github.com/openmicroscopy/openmicroscopy/",
      packages=[''],
      test_suite='test.suite',
      cmdclass={'test': PyTest},
      tests_require=['pytest<3'],
      )
