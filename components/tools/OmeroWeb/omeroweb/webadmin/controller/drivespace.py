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

from webadmin.controller import BaseController

import omero

class BaseDriveSpace(BaseController):

    freeSpace = None
    usedSpace = None
    topTen = None

    def __init__(self, conn):
        BaseController.__init__(self, conn)
        self.freeSpace = self.conn.getFreeSpace()

def _bytes_per_pixel(pixel_type):
    if pixel_type == "int8" or pixel_type == "uint8":
        return 1
    elif pixel_type == "int16" or pixel_type == "uint16":
        return 2
    elif pixel_type == "int32" or pixel_type == "uint32" or pixel_type == "float":
        return 4
    elif pixel_type == "double":
        return 8;
    else:
        raise AttributeError("Unknown pixel type: %s" % (pixel_type))
    
def _usage_map_helper(pixels_list, pixels_originalFiles_list, exps):
    tt = dict()
    for p in pixels_list:
        oid = p.details.owner.id.val
        p_size = p.sizeX.val * p.sizeY.val * p.sizeZ.val * p.sizeC.val * p.sizeT.val
        p_size = p_size*_bytes_per_pixel(p.pixelsType.value.val)
        if tt.has_key(oid):
            tt[oid]['data']+=p_size
        else:
            tt[oid] = dict()
            tt[oid]['label']=exps[oid]
            tt[oid]['data']=p_size
    
    for pof in pixels_originalFiles_list:
        oid = pof.details.owner.id.val
        p_size = pof.parent.size.val
        if tt.has_key(oid):
            tt[oid]['data']+=p_size
        
    return tt #sorted(tt.iteritems(), key=lambda (k,v):(v,k), reverse=True)

def usersData(conn, offset=0):
    loading = False
    usage_map = dict()
    exps = dict()
    for e in list(conn.getObjects("Experimenter")):
        exps[e.id] = e.getFullName()
        
    PAGE_SIZE = 1000
    offset = long(offset)
    
    ctx = dict()
    if conn.isAdmin():
        ctx['omero.group'] = '-1'
    else:
        ctx['omero.group'] = str(conn.getEventContext().groupId)
        
    p = omero.sys.ParametersI()
    p.page(offset, PAGE_SIZE)
    pixels_list = conn.getQueryService().findAllByQuery(
            "select p from Pixels as p join fetch p.pixelsType " \
            "order by p.id", p, ctx)
    
    # archived files
    if len(pixels_list) > 0:
        pids = omero.rtypes.rlist([p.id for p in pixels_list])
        p2 = omero.sys.ParametersI()
        p2.add("pids", pids)
        pixels_originalFiles_list = conn.getQueryService().findAllByQuery(
            "select m from PixelsOriginalFileMap as m join fetch m.parent " \
            "where m.child.id in (:pids)", p2, ctx)
    
        count = len(pixels_list)
        usage_map = _usage_map_helper(pixels_list, pixels_originalFiles_list, exps)
    
        count = len(pixels_list)
        offset += count
    
        if count == PAGE_SIZE:
            loading = True
    
    return {'loading':loading, 'offset':offset, 'usage':usage_map}
