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

import string
import datetime
import time

import omero
from omero.rtypes import *
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ProjectI import ProjectI

from webclient.controller import BaseController

class BaseShare(BaseController):

    shares = None
    shSize = None
    ownShares = None
    oshSize = None
    memberShares = None
    mshSize = None
    
    share = None
    imageInShare = None
    imgSize = 0
    membersInShare = None

    comments = None
    cmSize = None

    def __init__(self, menu, conn, conn_share=None, share_id=None, action=None, **kw):
        BaseController.__init__(self, conn)
        if conn_share is None:
            if share_id: 
                self.share = self.conn.getShare(share_id)
                self.eContext['breadcrumb'] = [ menu.title(), "Share", action ]
            elif action:
                self.eContext['breadcrumb'] = ["Basket", action ]
            else:
                self.eContext['breadcrumb'] = [ menu.title() ]
        else:
            self.conn_share = conn_share
            self.share = self.conn.getShare(share_id)
            self.conn_share.activateShare(share_id)

    def createShare(self, host, blitz_id, imageInBasket, message, expiration, members, enable):
        # only for python 2.5
        # d1 = datetime.strptime(expiration+" 23:59:59", "%Y-%m-%d %H:%M:%S")
        d1 = datetime.datetime(*(time.strptime((expiration+" 23:59:59"), "%Y-%m-%d %H:%M:%S")[0:6]))
        expiration_date = rtime(long(time.mktime(d1.timetuple())+1e-6*d1.microsecond)*1000)
        ms = [str(m) for m in members]
        #gs = str(guests).split(';')
        self.conn.createShare(host, int(blitz_id), imageInBasket, message, expiration_date, ms, enable)

    def createDiscussion(self, host, blitz_id, message, expiration, members, enable):
        # only for python 2.5
        # d1 = datetime.strptime(expiration+" 23:59:59", "%Y-%m-%d %H:%M:%S")
        d1 = datetime.datetime(*(time.strptime((expiration+" 23:59:59"), "%Y-%m-%d %H:%M:%S")[0:6]))
        expiration_date = rtime(long(time.mktime(d1.timetuple())+1e-6*d1.microsecond)*1000)
        ms = [str(m) for m in members]
        #gs = str(guests).split(';')
        self.conn.createShare(host, int(blitz_id), [], message, expiration_date, ms, enable)
    
    def updateShare(self, message, expiration, members, enable):
        # only for python 2.5
        # d1 = datetime.strptime(expiration+" 23:59:59", "%Y-%m-%d %H:%M:%S")
        d1 = datetime.datetime(*(time.strptime((expiration+" 23:59:59"), "%Y-%m-%d %H:%M:%S")[0:6]))
        expiration_date = rtime(long(time.mktime(d1.timetuple())+1e-6*d1.microsecond)*1000)
        ms = [str(m) for m in members]
        #gs = str(guests).split(';')
        self.conn.updateShare(self.share.id, message, expiration_date, ms, enable)
        
    def addComment(self, host, blitz_id, comment):
        self.conn.addComment(host, blitz_id, self.share.id, comment)

    def getAllShares(self):
        self.shares = self.sortByAttr(list(self.conn.getAllShares()), 'started')

    def getShares(self):
        self.ownShares = list(self.conn.getOwnShares())
        self.memberShares = list(self.conn.getMemberShares())
        self.oshSize = len(self.ownShares)
        self.mshSize = len(self.memberShares)

    def getComments(self, share_id):
        self.comments = self.sortByAttr(list(self.conn.getComments(share_id)), 'details.creationEvent.time')
        self.cmSize = len(self.comments)

    def getMembers(self, share_id):
        self.membersInShare = [m.id for m in self.conn.getAllMembers(share_id)]
    
    def getAllUsers(self, share_id):
         self.allInShare = list(self.conn.getAllMembers(share_id))#list(self.conn.getAllUsers(share_id))

    def loadShareContent(self):
        try:
            if self.conn_share._shareId is not None:
                content = self.conn_share.getContents(self.conn_share._shareId)
            else:
                raise AttributeError('Share was not activated.')
        except:
            raise AttributeError('Share was not activated.')
        
        self.imageInShare = list()

        for ex in content:
            if isinstance(ex._obj, omero.model.ImageI):
                self.imageInShare.append(ex)

        self.imgSize = len(self.imageInShare)
        
        self.sizeOfShare = self.imgSize

# ### Test code below this line ###

if __name__ == '__main__':
    print "share"
