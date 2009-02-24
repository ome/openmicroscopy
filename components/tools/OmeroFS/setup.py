#!/usr/bin/env python
"""
   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from ez_setup import use_setuptools
use_setuptools()
from setuptools import setup
from omero_version import omero_version as ov

setup(name="OmeroFS",
      version=ov,
      description="OMERO.fs server for watching directories",
      long_description="""\
OMERO.fs server for watching directories"
""",
      author="Colin Blackburn",
      author_email="",
      url="https://trac.openmicroscopy.org.uk/omero/wiki/OmeroFs",
      download_url="https://trac.openmicroscopy.org.uk/omero/wiki/OmeroFs",
      packages=['', 'monitors'],
      test_suite='test.suite'
)

