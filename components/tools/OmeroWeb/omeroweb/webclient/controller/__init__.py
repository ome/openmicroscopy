#!/usr/bin/env python
# 
# 
# 
# Copyright (c) 2008-2011 University of Dundee.
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

from django.conf import settings
PAGE = settings.PAGE

class BaseController(object):
    
    conn = None
    eContext = dict()
    
    def __init__(self, conn, **kw):
        self.conn = conn
        self.eContext['image_limit'] = PAGE
        self.eContext['context'] = self.conn.getEventContext()
        gr = self.conn.getObject("ExperimenterGroup", self.conn.getEventContext().groupId)
        self.eContext['isReadOnly'] = gr.isReadOnly()
        self.eContext['isLeader'] = gr.isLeader()
        if not gr.isPrivate() and not gr.isReadOnly():
            self.eContext['isEditable'] = True
        else:
            self.eContext['isEditable'] = False
        self.eContext['user'] = self.conn.getUser()        
        grs = list(self.conn.getGroupsMemberOf())
        grs.sort(key=lambda x: x.getName().lower())
        self.eContext['memberOfGroups'] = grs
        self.eContext['allGroups'] = grs
        self.eContext['advice'] = None
    
    def getShareId(self):
        return self.conn._shareId
    
    ###########################################################
    # Paging
        
    def doPaging(self, page, page_size, total_size, limit=PAGE):
        total = list()
        t = (total_size/limit) + (total_size%limit > 0 and 1 or 0)
        if total_size > (limit*10):
            if page > 10 :
                total.append(-1)
            for i in range((1, page-9)[ page-9 >= 1 ], (t+1, page+10)[ page+9 < t ]):
                total.append(i)
            if page < t-9:
                total.append(-1)

        elif total_size > limit and total_size <= (limit*10):
            for i in range(1, t+1):
                total.append(i)
        else:
            total.append(1)
        next = None
        if page_size == limit and (page*limit) < total_size:
            next = page + 1
        prev = None
        if page > 1:
            prev = page - 1
        if len(total)>1:
            return {'page': page, 'total':total, 'next':next, "prev":prev}
        return None