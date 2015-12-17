#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
# All rights reserved.
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
Plugin for manipulating all children of an object
"""

import sys

from omero.cli import BaseControl
from omero.cli import CLI
from omero.gateway import BlitzGateway
import omero.model
from omero.sys import ParametersI
from omero.sys import Filter
from omero.rtypes import rlong


HELP = """Manipulate all children of an object.

For instance, you can move all images in a dataset to another dataset,
or move all orphaned images.

Examples:

    # Move all orphaned images into Dataset:1
    omero children move Image:orphan Dataset:1

    # Move all images owned by any user from Dataset:1 into Dataset:2
    omero children move --all Dataset:1 Dataset:2

    # Delete all orphaned images owned by current user
    omero children delete Image:orphan

    # Delete all orphaned images owned by any user
    omero children delete --all Image:orphan
"""


class ChildrenControl(BaseControl):

    def _configure(self, parser):
        parser.add_login_arguments()

        sub = parser.sub()
        move = parser.add(sub, self.move)
        delete = parser.add(sub, self.delete)

        for x in (move, delete):
            x.add_argument("parent",
                           help="Object in Parent:ID format, or Class:orphan")
            x.add_argument("--all", action="store_true",
                           help="Select children from all experimenters")
            x.add_argument("--report", action="store_true", help=(
                "Show additional information"))
            dry_or_not = x.add_mutually_exclusive_group()
            dry_or_not.add_argument("-n", "--dry-run", action="store_true")
            dry_or_not.add_argument("-f", "--force", action="store_false",
                                    dest="dry_run")

        move.add_argument("dest", help="Destination in Class:ID format")

    def _clientconn(self, args):
        client = self.ctx.conn(args)
        conn = BlitzGateway(client_obj=client)
        return client, conn

    def _type_id(self, obj):
        t = obj._obj.ice_staticId().split("::")[-1]
        i = obj.getId()
        return (t, i)

    def _get_children(self, args):
        client, conn = self._clientconn(args)
        try:
            klass, oid = args.parent.split(':')
            if oid == 'orphan':
                eid = None if args.all else conn.getUserId()
                children = conn.listOrphans(klass, eid=eid)
            else:
                obj = conn.getObject(klass, oid)
                if not obj:
                    raise ValueError()
                children = obj.listChildren()
        except (AttributeError, KeyError, ValueError):
            self.ctx.die(100, 'Invalid parent: %s' % args.parent)
        return children

    def delete(self, args):
        children = self._get_children(args)
        spec = [("/%s:%s" % self._type_id(c)) for c in children]
        cmd = ["delete"] + spec
        if args.report:
            cmd += ["--report"]
        if args.dry_run:
            cmd += ["--dry-run"]
        return self.ctx.invoke(cmd)

    def move(self, args):
        client, conn = self._clientconn(args)

        pt, pi = args.parent.split(":")
        dt, di = args.dest.split(":")
        dest = conn.getObject(dt, di)
        if not dest:
            self.ctx.die(100, "Destination object not found")

        if dt == "Dataset":
            ct = "Image"
        elif dt == "Project":
            ct = "Dataset"
        elif dt == "Screen":
            ct = "Plate"
        else:
            self.ctx.die(100, "Unsupported parent type")

        if pi == "orphan":
            if pt != ct:
                self.ctx.die(
                    100,
                    "Orphans of type %s aren't supported for destination" % pt)
            links = self._move_orphans(args, dt, ct, dest)
        else:
            if pt != dt:
                self.ctx.die(
                    100, "Old and new parents must be of the same type")
            links = self._move_children(args, dt, pi, ct, dest)

        if args.report:
            for link in links:
                self.ctx.out("Moving %s:%s" % (ct, link.child.id._val))

        if not args.dry_run:
            conn.getUpdateService().saveArray(links)

    def _move_children(self, args, pt, pi, ct, dest):
        client, conn = self._clientconn(args)
        linkq = "FROM %s%sLink WHERE parent.id=:id" % (pt, ct)
        params = ParametersI()
        params.addId(pi)

        if args.all:
            params.theFilter = Filter(ownerId=rlong(conn.getUserId()))

        links = conn.getQueryService().findAllByQuery(linkq, params)
        if not links:
            self.ctx.die(100, 'No children found')
        for link in links:
            link.setParent(dest._obj)
        return links

    def _move_orphans(self, args, pt, ct, dest):
        linktype = getattr(omero.model, "%s%sLinkI" % (pt, ct))
        links = []
        for c in self._get_children(args):
            link = linktype()
            link.setParent(dest._obj)
            link.setChild(c._obj)
            links.append(link)
        return links

try:
    register("children", ChildrenControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("children", ChildrenControl, HELP)
        cli.invoke(sys.argv[1:])
