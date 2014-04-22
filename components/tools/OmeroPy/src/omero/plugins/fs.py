#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 Glencoe Software, Inc. All Rights Reserved.
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
fs plugin for querying repositories, filesets, and the like.
"""

import sys

from omero.cli import BaseControl
from omero.cli import CLI


HELP = """Filesystem utilities"""

#
# Copied from:
# blitz/src/ome/formats/importer/transfers/AbstractFileTransfer.java
#
TRANSFERS = {
    "ome.formats.importer.transfers.HardLinkFileTransfer": "ln",
    "ome.formats.importer.transfers.MoveFileTransfer": "ln_rm",
    "ome.formats.importer.transfers.SymlinkFileTransfer": "ln_s",
    "ome.formats.importer.transfers.UploadFileTransfer": "",
    }


class FsControl(BaseControl):

    def _configure(self, parser):

        parser.add_login_arguments()
        sub = parser.sub()

        repos = parser.add(sub, self.repos, self.repos.__doc__)
        repos.add_argument(
            "--managed", action="store_true",
            help="repos only managed repositories")

        sets = parser.add(sub, self.sets, self.sets.__doc__)
        sets.add_argument(
            "--by-age", action="store_true")
        sets.add_argument(
            "--with-transfer", nargs="*", action="append")

    def repos(self, args):
        """
        List all repositories.
        """

        from omero.util.text import TableBuilder
        from omero.grid import ManagedRepositoryPrx as MRepo

        client = self.ctx.conn(args)
        shared = client.sf.sharedResources()
        repos = shared.repositories()
        repos = zip(repos.descriptions, repos.proxies)
        repos.sort(lambda a, b: cmp(a[0].id.val, b[0].id.val))

        tb = TableBuilder("#")
        tb.cols(["Id", "Type", "Path"])
        for idx, pair in enumerate(repos):
            desc, prx = pair
            path = "/".join([desc.path.val, desc.name.val])

            type = "Public"
            is_mrepo = MRepo.checkedCast(prx)
            if is_mrepo:
                type = "Managed"
            if args.managed and not is_mrepo:
                continue
            if desc.hash.val == "ScriptRepo":
                type = "Script"
            tb.row(idx, *(desc.id.val, type, path))
        self.ctx.out(str(tb.build()))

    def sets(self, args):
        """
        List filesets by various criteria
        """

        from omero.constants.namespaces import NSFILETRANSFER
        from omero_sys_ParametersI import ParametersI
        from omero.util.text import TableBuilder
        from omero.rtypes import unwrap

        client = self.ctx.conn(args)
        service = client.sf.getQueryService()

        params = ParametersI()
        params.page(0, 25)
        params.addString("ns", NSFILETRANSFER)

        if args.by_age:
            query = (
                "select fs.id, fs.templatePrefix, count(uf.id), ann.textValue "
                "from Fileset fs "
                "join fs.usedFiles uf "
                "left outer join fs.annotationLinks fal "
                "left outer join fal.child ann "
                "where (ann is null or ann.ns = :ns) "
                "group by fs.id, fs.templatePrefix, ann.textValue "
                "order by fs.id desc")

        objs = service.projection(query, params, {"omero.group": "-1"})
        objs = unwrap(objs)

        tb = TableBuilder("#")
        tb.cols(["Id", "Prefix", "File count", "Transfer"])
        for idx, obj in enumerate(objs):

            # Map the transfer name to the CLI symbols
            ns = obj[-1]
            if ns is None:
                ns = ""
            elif ns in TRANSFERS:
                ns = TRANSFERS[ns]
            obj[-1] = ns

            # Map any requested transfers as well
            allowed = []
            for x in args.with_transfer:
                x = x[0]  # Strip argparse wrapper
                x = TRANSFERS.get(x, x)  # map
                allowed.append(x)

            # Filter based on the ns symbols
            if args.with_transfer:
                if ns not in allowed:
                    continue
            tb.row(idx, *tuple(obj))
        self.ctx.out(str(tb.build()))


try:
    register("fs", FsControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("fs", FsControl, HELP)
        cli.invoke(sys.argv[1:])
