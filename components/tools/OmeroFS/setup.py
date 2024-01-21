#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   Copyright 2008-2016 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import glob
import sys
import os

from setuptools import setup
from omero_version import omero_version as ov

url = 'https://docs.openmicroscopy.org/latest/omero/developers/Server/FS.html'
setup(name="OmeroFS",
      version=ov,
      description="OMERO.fs server for watching directories",
      long_description="OMERO.fs server for watching directories",
      author="The Open Microscopy Team",
      author_email="ome-devel@lists.openmicroscopy.org.uk",
      url=url,
      download_url=url,
      package_dir={"": "target"},
      packages=[''],
      tests_require=['pytest', 'pytest-xdist'])
