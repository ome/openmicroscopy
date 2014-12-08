#!/usr/bin/env python
# -*- coding: utf-8 -*-

# This file is an import-only file providing a mechanism for other files to
# import a range of modules in a controlled way. It could be made to pass
# flake8 but given its simplicity it is being marked as noqa for now.
#
# flake8: noqa

from omero import ObjectFactoryRegistrar

import IceImport

IceImport.load("omero_model_NamedValue_ice")
