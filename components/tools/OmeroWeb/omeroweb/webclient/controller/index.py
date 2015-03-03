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

from webclient.controller import BaseController


class BaseIndex(BaseController):

    def __init__(self, conn):
        BaseController.__init__(self, conn)

    def loadMostRecent(self):
        self.mostRecentSharesComments = list(
            self.conn.listMostRecentShareComments())
        self.mostRecentSharesComments.sort(
            key=lambda x: x.creationEventDate(), reverse=True)
        self.mostRecentShares = list()
        for sh in list(self.conn.listMostRecentShares()):
            flag = True
            for s in self.mostRecentShares:
                if sh.id == s.id:
                    flag = False
            if flag:
                self.mostRecentShares.append(sh)
        self.mostRecentShares.sort(key=lambda x: x.started, reverse=True)

    def loadTagCloud(self):
        tags = dict()
        for ann in list(self.conn.listMostRecentTags()):
            try:
                if tags[ann.id]['count'] > 0:
                    tags[ann.id]['count'] = tags[ann.id]['count'] + 1
                else:
                    tags[ann.id]['count'] = 1
            except:
                tags[ann.id] = {'obj': ann, 'count': 1}
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
