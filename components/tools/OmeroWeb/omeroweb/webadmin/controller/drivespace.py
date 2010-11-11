#!/usr/bin/env python
# 
# 
# 
# Copyright (c) 2008 University of Dundee. 
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
# 
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
        self.freeSpace = self.conn.getFreeSpace()
        self.usedSpace = self.conn.getUsedSpace()
        self.experimenters = list(self.conn.lookupExperimenters())
        self.usage = self.conn.getUsage()
    
    def usersData(self):
        tt = dict()
        for k,v in self.usage.iteritems():
            for exp in self.experimenters:
                if long(exp.id) == k:
                    tt[str(exp.omeName)] = v
                    break
        self.usage = sorted(tt.iteritems(), key=lambda (k,v):(v,k), reverse=True)
    
    def pieChartData(self):
        tt = {"free space":self.freeSpace}
        used = 0
        i=0
        for k,v in self.usage.iteritems():
            if i < 10:
                for exp in self.experimenters:
                    if long(exp.id) == k:
                        tt[str(exp.omeName)] = v
                        break
            else:
                used += v
            i+=1
        tt["others"] = used
        self.usage = sorted(tt.iteritems(), key=lambda (k,v):(v,k), reverse=True)
