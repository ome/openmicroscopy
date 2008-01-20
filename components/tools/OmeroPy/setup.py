#!/usr/bin/env python
from ez_setup import use_setuptools
use_setuptools()
from setuptools import setup

setup(name="OmeroPy",
      version="0.1dev",
      description="Python bindings to the OMERO.blitz server",
      long_description="""\
Python bindings to the OMERO.blitz server.
""",
      author="Josh Moore",
      author_email="josh@glencoesoftware.com",
      url="https://trac.openmicroscopy.org.uk/omero/wiki/OmeroPy",
      download_url="https://trac.openmicroscopy.org.uk/omero/wiki/OmeroPy",
      package_dir = {"": "target"},
      packages=['', 'omero', 'omero.test','omero.plugins','omero.model','omero.api','omero.util','omero.romio','omero.util','omero_ext'],
      test_suite='test.suite'
)

