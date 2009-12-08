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

from omero.rtypes import *
from omero.model import ImageI, DatasetI, ProjectI

from webclient.controller import BaseController

class BaseShare(BaseController):

    shares = None
    shSize = None
    ownShares = None
    oshSize = 0
    memberShares = None
    mshSize = 0
    
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
                if self.share is None:
                    raise AttributeError("We are sorry, but that share either does not exist, or if it does, you have not been invited to see it. Contact the user you think might own this share for more information.")
                if self.share._obj is None:
                    raise AttributeError("We are sorry, but that share either does not exist, or if it does, you have not been invited to see it. Contact the user you think might own this share for more information.")
                if self.share is not None and not self.share.active and not self.share.isOwned():
                    raise AttributeError("%s is not active and cannot be visible. Please contact the user you think might own this share for more information." % self.share.getShareType())
                self.eContext['breadcrumb'] = [ menu.title(), "Share", action ]
            elif action:
                self.eContext['breadcrumb'] = ["Basket", action ]
            else:
                self.eContext['breadcrumb'] = [ menu.title() ]
        else:
            self.conn_share = conn_share
            self.share = self.conn.getShare(share_id)
            if self.share is not None and not self.share.active and not self.share.isOwned:
                raise AttributeError("%s is not active and cannot be visible. Please contact the user you think might own this share for more information." % self.share.getShareType())
            if self.share is None:
                raise AttributeError("We are sorry, but that share either does not exist, or if it does, you have not been invited to see it. Contact the user you think might own this share for more information.")
            if self.share._obj is None:
                raise AttributeError("We are sorry, but that share either does not exist, or if it does, you have not been invited to see it. Contact the user you think might own this share for more information.")

    def createShare(self, host, blitz_id, imageInBasket, message, members, enable, expiration=None):
        # only for python 2.5
        # d1 = datetime.strptime(expiration+" 23:59:59", "%Y-%m-%d %H:%M:%S")
        expiration_date = None
        if expiration is not None:
            d1 = datetime.datetime(*(time.strptime((expiration+" 23:59:59"), "%Y-%m-%d %H:%M:%S")[0:6]))
            expiration_date = rtime(long(time.mktime(d1.timetuple())+1e-6*d1.microsecond)*1000)
        ms = [str(m) for m in members]
        
        self.conn.createShare(host, int(blitz_id), imageInBasket, message, ms, enable, expiration_date)

    def createDiscussion(self, host, blitz_id, message, members, enable, expiration=None):
        # only for python 2.5
        # d1 = datetime.strptime(expiration+" 23:59:59", "%Y-%m-%d %H:%M:%S")
        expiration_date = None
        if expiration is not None:
            d1 = datetime.datetime(*(time.strptime((expiration+" 23:59:59"), "%Y-%m-%d %H:%M:%S")[0:6]))
            expiration_date = rtime(long(time.mktime(d1.timetuple())+1e-6*d1.microsecond)*1000)
        ms = [long(m) for m in members]
        
        self.conn.createShare(host, int(blitz_id), [], message, ms, enable, expiration_date)
    
    def updateShareOrDiscussion(self, host, blitz_id, message, members, enable, expiration=None):
        # only for python 2.5
        # d1 = datetime.strptime(expiration+" 23:59:59", "%Y-%m-%d %H:%M:%S")
        expiration_date = None
        if expiration is not None:
            d1 = datetime.datetime(*(time.strptime((expiration+" 23:59:59"), "%Y-%m-%d %H:%M:%S")[0:6]))
            expiration_date = rtime(long(time.mktime(d1.timetuple())+1e-6*d1.microsecond)*1000)
        
        old_groups =  [m._obj for m in self.conn.getAllMembers(self.share.id)]
        new_groups = [e._obj for e in self.conn.getExperimenters(members)]
        
        add_mem = list()
        rm_mem = list()
        
        # remove
        for ogr in old_groups:
            flag = False
            for ngr in new_groups:
                if ngr.id.val == ogr.id.val:
                    flag = True
            if not flag:
                rm_mem.append(ogr)
        
        # add
        for ngr in new_groups:
            flag = False
            for ogr in old_groups:
                if ogr.id.val == ngr.id.val:
                    flag = True
            if not flag:
                add_mem.append(ngr)
                
        self.conn.updateShareOrDiscussion(host, int(blitz_id), self.share.id, message, add_mem, rm_mem, enable, expiration_date)
    
    def addComment(self, host, blitz_id, comment):
        self.conn.addComment(host, int(blitz_id), self.share.id, comment)

    def getShares(self):
        own_list = self.sortByAttr(list(self.conn.getOwnShares()), 'started', True)
        mem_list = self.sortByAttr(list(self.conn.getMemberShares()), 'started', True)
        
        os_list_with_counters = list()
        ms_list_with_counters = list()
        
        own_ids = [sh.id for sh in own_list]
        if len(own_ids) > 0:
            osh_child_counter = self.conn.getMemberCount(own_ids)
            osh_annotation_counter = self.conn.getCommentCount(own_ids)
            
            for sh in own_list:
                sh.child_counter = osh_child_counter.get(sh.id)-1
                sh.annotation_counter = osh_annotation_counter.get(sh.id)
                os_list_with_counters.append(sh)
            
            self.ownShares = os_list_with_counters
            self.oshSize = len(self.ownShares)
            
        mem_ids = [sh.id for sh in mem_list]
        if len(mem_ids) > 0:
            msh_child_counter = self.conn.getMemberCount(mem_ids)
            msh_annotation_counter = self.conn.getCommentCount(mem_ids)
            
            for sh in mem_list:
                sh.child_counter = msh_child_counter.get(sh.id)-1
                sh.annotation_counter = msh_annotation_counter.get(sh.id)
                ms_list_with_counters.append(sh)
            
            self.memberShares = ms_list_with_counters
            self.mshSize = len(self.memberShares)
        self.sharesSize = self.oshSize + self.mshSize

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
    
    def loadShareOwnerContent(self, share_id):
        content = self.conn.getContents(long(share_id))
        
        self.imageInShare = list()

        for ex in content:
            if isinstance(ex._obj, omero.model.ImageI):
                self.imageInShare.append(ex)

        self.imgSize = len(self.imageInShare)
    