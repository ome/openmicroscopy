#!/usr/bin/env python
# -*- coding: utf-8 -*-
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

import datetime
import time

import omero
from omero.rtypes import rtime

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

    def __init__(self, conn, share_id=None, **kw):
        BaseController.__init__(self, conn)
        
        if share_id is not None: 
            self.share = self.conn.getShare(share_id)
            if self.share is None:
                raise AttributeError("We are sorry, but that share either does not exist, or if it does, you have not been invited to see it. Contact the user you think might own this share for more information.")
            if self.share._obj is None:
                raise AttributeError("We are sorry, but that share either does not exist, or if it does, you have not been invited to see it. Contact the user you think might own this share for more information.")
            if self.share is not None and not self.share.active and not self.share.isOwned():
                raise AttributeError("%s is not active and cannot be visible. Please contact the user you think might own this share for more information." % self.share.getShareType())

    def obj_type(self):
        """ Same as BaseContainer. Used to create identifier E.g. share-123 in right-hand panel """
        return self.share.getShareType().lower()

    def obj_id(self):
        """ Same as BaseContainer. Used to create identifier E.g. share-123 in right-hand panel """
        return self.share.getId()

    def createShare(self, host, blitz_id, image, message, members, enable, expiration=None):
        expiration_date = None
        if expiration is not None:
            d1 = datetime.datetime.strptime(expiration+" 23:59:59", "%Y-%m-%d %H:%M:%S")
            expiration_date = long(time.mktime(d1.timetuple())+1e-6*d1.microsecond)*1000
        ms = [str(m) for m in members]
        
        self.conn.createShare(host, int(blitz_id), image, message, ms, enable, expiration_date)

    def createDiscussion(self, host, blitz_id, message, members, enable, expiration=None):
        expiration_date = None
        if expiration is not None:
            d1 = datetime.datetime.strptime(expiration+" 23:59:59", "%Y-%m-%d %H:%M:%S")
            expiration_date = rtime(long(time.mktime(d1.timetuple())+1e-6*d1.microsecond)*1000)
        ms = [long(m) for m in members]
        
        self.conn.createShare(host, int(blitz_id), [], message, ms, enable, expiration_date)
    
    def updateShareOrDiscussion(self, host, blitz_id, message, members, enable, expiration=None):
        expiration_date = None
        if expiration is not None:
            d1 = datetime.datetime.strptime(expiration+" 23:59:59", "%Y-%m-%d %H:%M:%S")
            expiration_date = long(time.mktime(d1.timetuple())+1e-6*d1.microsecond)*1000
        
        old_groups =  [m._obj for m in self.conn.getAllMembers(self.share.id)]
        new_groups = [e._obj for e in self.conn.getObjects("Experimenter", members)]
        
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
        return self.conn.addComment(host, int(blitz_id), self.share.id, comment)

    def getShares(self):
        sh_list = list(self.conn.getOwnShares())
        sh_list.extend(list(self.conn.getMemberShares()))
        sh_list.sort(key=lambda x: x.id, reverse=True)
        sh_list_with_counters = list()
        
        sh_ids = [sh.id for sh in sh_list]
        if len(sh_ids) > 0:
            sh_annotation_counter = self.conn.getCommentCount(sh_ids)
            
            for sh in sh_list:
                sh.annotation_counter = sh_annotation_counter.get(sh.id)
                sh_list_with_counters.append(sh)
            
        self.shares = sh_list_with_counters
        self.shSize = len(self.shares)

    def getComments(self, share_id):
        self.comments = list(self.conn.getComments(share_id))
        self.comments.sort(key=lambda x: x.creationEventDate(), reverse=True)
        self.cmSize = len(self.comments)

    def removeImage(self, image_id):
        self.conn.removeImage(self.share.id, image_id)
    
    def getMembers(self, share_id):
        self.membersInShare = [m.id for m in self.conn.getAllMembers(share_id)]
    
    def getAllUsers(self, share_id):
         self.allInShare = list(self.conn.getAllMembers(share_id))#list(self.conn.getAllUsers(share_id))

    def loadShareContent(self):
        content = self.conn.getContents(self.share.id)
        
        imageInShare = list()

        for ex in content:
            if isinstance(ex._obj, omero.model.ImageI):
                imageInShare.append(ex)

        self.containers = {'images': imageInShare}
        self.c_size = len(imageInShare)
