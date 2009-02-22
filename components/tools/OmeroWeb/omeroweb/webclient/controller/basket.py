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

from django.conf import settings

from webclient.controller import BaseController

class BaseBasket(BaseController):

    imageInBasket = None

    imgSize = 0
    dsSize = 0
    prSize = 0
    sizeOfBasket = 0

    def __init__(self, conn, **kw):
        BaseController.__init__(self, conn)

    def buildBreadcrumb(self, menu=None):
        if menu is not None:
            self.eContext['breadcrumb'] = ['<a href="/%s/basket/">Basket</a>' % (settings.WEBCLIENT_ROOT_BASE), menu[2:len(menu)].title()]
        else:
            self.eContext['breadcrumb'] = ['Basket']
    
    def load_basket(self, request):
        imInBasket = list()
        dsInBasket = list()
        prInBasket = list()

        for imgId in request.session['imageInBasket']:
            imInBasket.append(imgId)

        if len(imInBasket) > 0: 
            self.imageInBasket = list(self.conn.getSpecifiedImages(imInBasket))
            self.imgSize = len(self.imageInBasket)
        
        self.sizeOfBasket = self.imgSize
    