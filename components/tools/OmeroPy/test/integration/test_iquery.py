#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2010-2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""
   Integration test focused on the omero.api.IQuery interface.

"""

import library as lib
from omero.rtypes import unwrap, wrap
from omero.model import TagAnnotationI, ImageI, ImageAnnotationLinkI
from omero.model import PermissionsI
from omero.sys import ParametersI
from helpers import createImageWithPixels


class TestQuery(lib.ITest):

    # ticket:1849

    def testGetPixelsCount(self):
        q = self.root.sf.getQueryService()
        a = self.root.sf.getAdminService()
        groups = a.lookupGroups()
        for group in groups:
            rtypeseqseq = q.projection(
                """
                select p.pixelsType.value,
                sum(cast(p.sizeX as long) * p.sizeY
                    * p.sizeZ * p.sizeT * p.sizeC)
                from Pixels p group by p.pixelsType.value
                """,
                None, {"omero.group": str(group.id.val)})
            rv = unwrap(rtypeseqseq)
            as_map = dict()
            for obj_array in rv:
                as_map[obj_array[0]] = obj_array[1]
            if len(as_map) > 0:
                print "Group %s: %s" % (group.id.val, as_map)

    def testQueryTaggedUnique(self):

        # get group we're working on...
        ctx = self.client.sf.getAdminService().getEventContext()
        groupId = ctx.groupId
        print 'groupId', groupId

        # Admin sets permissions to read-ann
        admin = self.root.sf.getAdminService()
        rootUpdate = self.root.sf.getUpdateService()
        gr = admin.getGroup(groupId)
        p = PermissionsI()
        p.setUserRead(True)
        p.setUserWrite(True)
        p.setGroupRead(True)
        p.setGroupAnnotate(True)
        p.setGroupWrite(False)
        p.setWorldRead(False)
        p.setWorldAnnotate(False)
        p.setWorldWrite(False)
        gr.details.permissions = p
        admin.updateGroup(gr)

        # Update context for user
        ctx = self.client.sf.getAdminService().getEventContext()
        update = self.client.sf.getUpdateService()
        queryService = self.client.sf.getQueryService()
        tagCount = 5
        # User creates tag linked to images
        tag = TagAnnotationI()
        tag.textValue = wrap("test_iQuerySpeed")
        links = []

        for i in range(tagCount):
            iid = createImageWithPixels(self.client, self.uuid())
            link = ImageAnnotationLinkI()
            link.parent = ImageI(iid, False)
            link.child = tag
            links.append(link)
        links = update.saveAndReturnArray(links)
        tag = links[0].child
        # check permissions
        p = tag.getDetails().getPermissions()
        assert p.isGroupRead()
        assert p.isGroupAnnotate()

        # Root also links user's tag to images
        rootLinks = []
        for l in links:
            link = ImageAnnotationLinkI()
            link.parent = ImageI(l.parent.id, False)
            link.child = TagAnnotationI(l.child.id, False)
            rootLinks.append(link)
        rootUpdate.saveAndReturnArray(rootLinks, {'omero.group': str(groupId)})

        q = """select new map(obj.id as id,
               obj.name as name,
               obj.details.owner.id as ownerId,
               obj as image_details_permissions,
               obj.fileset.id as filesetId
             ,
             pix.sizeX as sizeX,
             pix.sizeY as sizeY,
             pix.sizeZ as sizeZ
             )
            from Image obj  left outer join obj.pixels pix
            join obj.annotationLinks alink
            where %s"""

        params = ParametersI()
        params.add('tid', tag.id)

        # We can get all the tagged images like this.
        # We use an additional select statement to give 'unique' results
        uniqueClause = """alink.id = (select max(alink.id)
                from ImageAnnotationLink alink
                where alink.child.id=:tid and alink.parent.id=obj.id)"""
        query = q % uniqueClause
        result = queryService.projection(query, params,
                                         {'omero.group': str(groupId)})
        assert len(result) == tagCount

        # Without the select statement, we get the same image returned
        # multiple times
        # TODO: is there a 'nicer' way to ensure unique results without
        # a second select statement?
        clause = "alink.parent.id=obj.id"
        query = q % clause
        result = queryService.projection(query, params,
                                         {'omero.group': str(groupId)})
        assert len(result) == tagCount
