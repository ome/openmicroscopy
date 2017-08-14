OMERO.web
=========

Introduction
------------

OMERO.web provides a web based client and plugin infrastructure.

Dependencies
------------

Direct dependencies of OMERO.web are:

- `OMERO.py`_
- `ZeroC IcePy`_
- `Pillow`_
- `NumPy`_
- A WSGI capable web server

Installation
------------

See: `OMERO`_ documentation

Usage
-----

See: `OMERO`_ documentation

Contributing
------------

See: `OMERO`_ documentation

Running tests
-------------

The tests are located under the `test` directory. To run all the tests, use
the `test` target of `build.py` run from the root of your repository::

  ./build.py -f components/tools/OmeroWeb/build.xml test

Unit tests
^^^^^^^^^^

Unit tests are stored under the `test/unit` folder and can be run by calling::

  ./build.py -f components/tools/OmeroWeb/build.xml test

Integration tests
^^^^^^^^^^^^^^^^^

Integration tests are stored under `test/integration` and depend on the
OMERO integration testing framework.  They can be run by calling::

  ./build.py -f components/tools/OmeroWeb/build.xml integration

Reading about `Running and writing tests`_ in the `OMERO`_ documentation
is essential.

License
-------

OMERO.web is released under the AGPL.

Copyright
---------

2009-2016, The Open Microscopy Environment, Glencoe Software, Inc.

.. _OMERO.py: https://openmicroscopy.org/
.. _ZeroC IcePy: https://zeroc.com/
.. _Pillow: https://python-pillow.org/
.. _NumPy: http://matplotlib.org/
.. _Running and writing tests: https://docs.openmicroscopy.org/latest/omero/developers/testing.html

