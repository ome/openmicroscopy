#!/usr/bin/env python
# -*- coding: utf-8 -*-
from turbogears.database import PackageHub
# from sqlobject import *

hub = PackageHub('validator')
__connection__ = hub

# class YourDataClass(SQLObject):
#     pass
