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


class BaseBasket(BaseController):

    imageInBasket = None

    imgSize = 0
    dsSize = 0
    prSize = 0
    sizeOfBasket = 0

    def __init__(self, conn, **kw):
        BaseController.__init__(self, conn)

    def load_basket(self, request):
        imInBasket = list()

        for imgId in request.session['imageInBasket']:
            imInBasket.append(imgId)
        # for dsId in request.session['datasetInBasket']:
        #    dsInBasket.append(dsId)

        if len(imInBasket) > 0:
            self.imageInBasket = list(
                self.conn.getObjects("Image", imInBasket))
            self.imgSize = len(self.imageInBasket)
        # if len(dsInBasket) > 0:
        #    self.datasetInBasket = list(
        #         self.conn.getDatasetsWithImages(dsInBasket))
        #    self.dsSize = len(self.datasetInBasket)
        self.sizeOfBasket = self.imgSize  # +self.dsSize
