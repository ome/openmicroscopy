#!/usr/bin/env python
# 
# Main controller
# 
# Copyright (c) 2008 University of Dundee. All rights reserved.
# This file is distributed under the same license as the OMERO package.
# Use is subject to license terms supplied in LICENSE.txt
# 
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2008.
# 
# Version: 1.0
#

class BaseController(object):
    
    conn = None
    
    def __init__(self, conn, **kw):
        self.conn = conn