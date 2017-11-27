#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   Copyright 2008-2016 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import glob
import sys
import os

for tools in glob.glob("../../../lib/repository/setuptools*.egg"):
    if tools.find(".".join(map(str, sys.version_info[0:2]))) > 0:
        sys.path.insert(0, os.path.abspath(tools))

sys.path.append("../OmeroPy/src")
from omero_setup import PyTest

from ez_setup import use_setuptools
use_setuptools(to_dir='../../../lib/repository')
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
      cmdclass={'test': PyTest},
      tests_require=['pytest<3'])
