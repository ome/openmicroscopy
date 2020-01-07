#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   Copyright 2008-2016 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import glob
import sys
import os

from omero_setup import PyTest
from setuptools import setup
from omero_version import omero_version as ov

if "test" in sys.argv:
    os.environ.setdefault("DJANGO_SETTINGS_MODULE", "omeroweb.settings")

    import django
    if django.VERSION > (1, 7):
        django.setup()


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
      tests_require=['pytest', 'pytest-xdist'],
      )
