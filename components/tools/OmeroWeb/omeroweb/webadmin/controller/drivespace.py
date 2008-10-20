#!/usr/bin/env python
# 
# Drive space controller
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

class BaseDriveSpace(BaseController):

    freeSpace = None
    usedSpace = None
    topTen = None

    def __init__(self, conn):
        BaseController.__init__(self, conn)
        self.freeSpace = self.conn.getFreeSpaceInKilobytes()
        self.usedSpace = self.conn.getUsedSpaceInKilobytes()

    def pieChartData(self):
        experimenters = list(self.conn.lookupExperimenters())
        tt = {"free space":self.freeSpace}
        used = 0
        i=0
        for k,v in self.conn.getUsage().iteritems():
            if i<10:
                for exp in experimenters:
                    if long(exp.id) == k:
                        tt[str(exp.omeName)] = v / 1024
                        used += v / 1024
                        break
            i+=1
        tt["rest"] = self.usedSpace - used
        self.topTen = sorted(tt.iteritems(), key=lambda (k,v):(v,k), reverse=True)