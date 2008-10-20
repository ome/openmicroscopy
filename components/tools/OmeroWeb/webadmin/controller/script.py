#!/usr/bin/env python
# 
# Script controller
# 
# Copyright (c) 2008 University of Dundee. All rights reserved.
# This file is distributed under the same license as the OMERO package.
# Use is subject to license terms supplied in LICENSE.txt
# 
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2008.
# 
# Version: 1.0
#

from webadmin.controller import BaseController

class BaseScripts(BaseController):

    scripts = None
    scriptsCount = 0

    def __init__(self, conn, sc_id=None,):
        BaseController.__init__(self, conn)
        self.scripts = self.conn.lookupScripts()

class BaseScript(BaseController):
    
    script = None
    details = None
    
    def __init__(self, conn, sc_id):
        BaseController.__init__(self, conn)
        for k,v in self.conn.getScriptwithDetails(sc_id).iteritems():
            self.script = k
            self.details = v
    
    