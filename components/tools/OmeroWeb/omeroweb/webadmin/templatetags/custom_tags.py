#!/usr/bin/env python
# 
# Copyright (c) 2008 University of Dundee. All rights reserved.
# Author: Aleksandra Tarkowska
# Use is subject to license terms supplied in LICENSE.txt
# 
# Version: 1.0
# 

"""
    OMERO.Web
    
"""

import datetime

from django.conf import settings
from django.template import Library

register = Library()

@register.simple_tag
def get_webadmin_root_url():
    return str( settings.WEBADMIN_ROOT_URL )

@register.simple_tag
def get_webadmin_static_url():
    return str( settings.WEBADMIN_STATIC_URL )

