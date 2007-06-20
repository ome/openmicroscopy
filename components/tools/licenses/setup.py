#!/usr/bin/python
from ez_setup import use_setuptools
use_setuptools()
from setuptools import setup, find_packages

setup(name="licenses",
      version="0.1dev",
      description="Python bindings for OMERO licensing.",
      long_description="""\
Python bindings for OMERO licensing.
""",
      author="Josh Moore",
      author_email="josh@glencoesoftware.com",
      url="https://trac.openmicroscopy.org.uk/omero/wiki/OmeroPy",
      download_url="https://trac.openmicroscopy.org.uk/omero/wiki/OmeroPy",
      package_dir = {"": "target"},
      packages=[''])

