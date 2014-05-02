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

from omero.cli import admin_only
from omero.cli import BaseControl
from omero.cli import CLI


HELP = """Filesystem utilities"""

#
# Copied from:
# blitz/src/ome/formats/importer/transfers/AbstractFileTransfer.java
#
TRANSFERS = {
    "ome.formats.importer.transfers.HardlinkFileTransfer": "ln",
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
        repos.add_style_argument()

        sets = parser.add(sub, self.sets, self.sets.__doc__)
        sets.add_argument(
            "--by-age", action="store_true")
        sets.add_argument(
            "--without-images", action="store_true")
        sets.add_argument(
            "--with-transfer", nargs="*", action="append")
        sets.add_argument(
            "--check", action="store_true",
            help="Checks each fileset for validity.")
        sets.add_style_argument()
        sets.add_limit_arguments()

    def _table(self, args):
        """
        """
        from omero.util.text import TableBuilder
        tb = TableBuilder("#")
        if args.style:
            tb.set_style(args.style)
        return tb

    def repos(self, args):
        """
        List all repositories.
        """

        from omero.grid import ManagedRepositoryPrx as MRepo

        client = self.ctx.conn(args)
        shared = client.sf.sharedResources()
        repos = shared.repositories()
        repos = zip(repos.descriptions, repos.proxies)
        repos.sort(lambda a, b: cmp(a[0].id.val, b[0].id.val))

        tb = self._table(args)
        tb.cols(["Id", "UUID", "Type", "Path"])
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
            tb.row(idx, *(desc.id.val, desc.hash.val, type, path))
        self.ctx.out(str(tb.build()))

    def sets(self, args):
        """
        List filesets by various criteria
        """

        from omero.constants.namespaces import NSFILETRANSFER
        from omero_sys_ParametersI import ParametersI
        from omero.rtypes import unwrap
        from omero.cmd import OK

        client = self.ctx.conn(args)
        service = client.sf.getQueryService()

        params = ParametersI()
        params.page(args.offset, args.limit)
        params.addString("ns", NSFILETRANSFER)

        query1 = (
            "select fs.id, fs.templatePrefix, "
            "(select size(f2.images) from Fileset f2 where f2.id = fs.id),"
            "(select size(f3.usedFiles) from Fileset f3 where f3.id = fs.id),"
            "ann.textValue "
            "from Fileset fs "
            "left outer join fs.annotationLinks fal "
            "left outer join fal.child ann "
            "where (ann is null or ann.ns = :ns) ")
        query2 = (
            "group by fs.id, fs.templatePrefix, ann.textValue "
            "order by fs.id desc")

        ## TODO:
        # - limit the above to just the latest annotation.
        # - list which repo a fileset lives in (--in-repo={oid|uuid})
        # - push filters into query
        # - --check for symlinks
        # - do a quick pre-check for SLICEPATH
        # - add rank for repositories (or boolean?)
        #     \___> in 5.1 should be part of model?
        #      __> anything to be done (NS, etc) in 5.0
        #          to allow upgrading a map?

        if args.by_age:
            pass  # Unused

        if args.without_images:
            query = "%s and fs.images is empty %s" % (query1, query2)
        else:
            query = "%s %s" % (query1, query2)

        objs = service.projection(query, params, {"omero.group": "-1"})
        objs = unwrap(objs)

        cols = ["Id", "Prefix", "Images", "Files", "Transfer"]
        if args.check:
            cols.append("Check")
        tb = self._table(args)
        tb.cols(cols)
        for idx, obj in enumerate(objs):

            # Map the transfer name to the CLI symbols
            ns = obj[-1]
            if ns is None:
                ns = ""
            elif ns in TRANSFERS:
                ns = TRANSFERS[ns]
            obj[-1] = ns

            # Map any requested transfers as well
            allowed = args.with_transfer is not None \
                and args.with_transfer or []
            for idx, x in enumerate(allowed):
                x = x[0]  # Strip argparse wrapper
                x = TRANSFERS.get(x, x)  # map
                allowed[idx] = x

            # Filter based on the ns symbols
            if allowed:
                if ns not in allowed:
                    continue

            # Now perform check if required
            if args.check:
                from omero.grid import RawAccessRequest
                desc, prx = self.get_managed_repo(client)
                ctx = client.getContext(group=-1)
                check_params = ParametersI()
                check_params.addId(obj[0])
                rows = service.projection((
                    "select h.value, f.hash, "
                    "f.path || '/' || f.name "
                    "from Fileset fs join fs.usedFiles uf "
                    "join uf.originalFile f join f.hasher h "
                    "where fs.id = :id"
                    ), check_params, ctx)

                if not rows:
                    obj.append("Empty")

                err = None
                for row in rows:
                    row = unwrap(row)
                    raw = RawAccessRequest()
                    raw.repoUuid = desc.hash.val
                    raw.command = "checksum"
                    raw.args = map(str, row)
                    cb = client.submit(raw)
                    try:
                        rsp = cb.getResponse()
                        if not isinstance(rsp, OK):
                            err = rsp
                            break
                    finally:
                        cb.close(True)

                if err:
                    obj.append("ERROR!")
                elif rows:
                    obj.append("OK")

            tb.row(idx, *tuple(obj))
        self.ctx.out(str(tb.build()))

    @admin_only
    def set_repo(self, args):
        """
        Change configuration properties for single repositories
        """
        pass

    def get_managed_repo(self, client):
        """
        For the moment this assumes there's only one.
        """
        from omero.grid import ManagedRepositoryPrx as MRepo

        shared = client.sf.sharedResources()
        repos = shared.repositories()
        repos = zip(repos.descriptions, repos.proxies)
        repos.sort(lambda a, b: cmp(a[0].id.val, b[0].id.val))

        for idx, pair in enumerate(repos):
            if MRepo.checkedCast(pair[1]):
                return pair

try:
    register("fs", FsControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("fs", FsControl, HELP)
        cli.invoke(sys.argv[1:])
