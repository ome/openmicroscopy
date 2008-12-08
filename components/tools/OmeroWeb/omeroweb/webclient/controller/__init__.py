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

import operator
from omeroweb.webclient.models import Advice

class BaseController(object):
    
    conn = None
    eContext = dict()
    
    def __init__(self, conn, **kw):
        self.conn = conn
        self.eContext['context'] = self.conn.getEventContext()
        self.eContext['user'] = self.conn.getUserWrapped()
        self.eContext['memberOfGroups'] = self.sortAsc(list(self.conn.getGroupsMemberOf()), "name")
        self.eContext['advice'] = Advice.objects.get(pk=1)
    
    def sortAsc(self, seq, attr, reverse=False):
        # Use the "Schwartzian transform".
        # Wrapped object only.
        intermed = map(None, map(getattr, seq, (attr,)*len(seq)), xrange(len(seq)), seq)
        intermed.sort()
        if reverse:
            intermed.reverse()
        return map(operator.getitem, intermed, (-1,) * len(intermed))
        