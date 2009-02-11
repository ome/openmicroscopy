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

from webclient.controller import BaseController

class BaseIndex(BaseController):

    def __init__(self, conn):
        BaseController.__init__(self, conn)
        self.eContext['breadcrumb'] = ["Home"]

    def loadData(self):
        self.supervisor = self.conn.getCurrentSupervisor()
        self.leaderOfGroups = self.sortByAttr(list(self.conn.getGroupsLeaderOf()), "name")
        self.colleagues = self.sortByAttr(list(self.conn.getColleagues()), "omeName")
        self.staffs = self.sortByAttr(list(self.conn.getStaffs()), "omeName")
        self.default_group = self.conn.getDefaultGroup(self.eContext['context'].userId)
    
    def loadMostRecent(self):
        #shc.extend(list(self.conn.getMostRecentComments()))
        self.mostRecentSharesComments = self.sortByAttr(list(self.conn.getMostRecentSharesComments()), 'details.creationEvent.time')
    
    def loadTagCloud(self):
        tag_links = list(self.conn.getMostRecentTags())
        tags = dict()
        for t in tag_links:
            try:
                if tags[t.getAnnotation().id][1] > 0:
                    tags[t.getAnnotation().id][1] = tags[t.getAnnotation().id][1] + 1
            except:
                tags[t.getAnnotation().id] = [t.getAnnotation(), 0]
        self.mostRecentTags = tags
    
    def loadLastImports(self):
        self.lastImportedImages = list(self.conn.getLastImportedImages())
        
