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

    #def loadData(self):
    #    self.supervisor = self.conn.getCurrentSupervisor()
    #    self.leaderOfGroups = self.sortByAttr(list(self.conn.getGroupsLeaderOf()), "name")
    #    self.colleagues = self.sortByAttr(list(self.conn.getColleagues()), "omeName")
    #    self.staffs = self.sortByAttr(list(self.conn.getStaffs()), "omeName")
    #    self.default_group = self.conn.getDefaultGroup(self.eContext['context'].userId)
    
    def loadMostRecent(self):
        #shc.extend(list(self.conn.listMostRecentComments()))
        self.mostRecentSharesComments = self.sortByAttr(list(self.conn.listMostRecentShareCommentLinks()), 'child.details.creationEvent.time', True)
        shares = list()
        for sh in list(self.conn.listMostRecentShares()):
            flag = True
            for s in shares:
                if sh.parent.id.val == s.id:
                    flag = False 
            if flag:
                shares.append(sh.getShare())
        self.mostRecentShares = self.sortByAttr(shares, 'started', True)
    
    def loadTagCloud(self):
        tags = dict()
        for ann in list(self.conn.listMostRecentTags()):
            try:
                if tags[ann.id]['count'] > 0:
                    tags[ann.id]['count'] = tags[ann.id]['count'] + 1
                else:
                    tags[ann.id]['count'] = 1
            except:
                tags[ann.id] = {'obj':ann, 'count':1}
            if len(tags) == 20:
                break
        
        font = {'max': 0, 'min': 1}
        for key, value in tags.items():
            if value['count'] < font['min']:
                font['min'] = value['count']
            if value['count'] > font['max']:
                font['max'] = value['count']
        self.font = font
        self.mostRecentTags = tags
    
    def loadLastAcquisitions(self):
        self.lastAcquiredImages = list(self.conn.listLastImportedImages())
