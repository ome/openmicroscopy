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

        images = parser.add(sub, self.images, self.images.__doc__)
        images.add_style_argument()
        images.add_limit_arguments()
        images.add_argument(
            "--order", default="newest",
            choices=("newest", "oldest", "largest"),
            help="order of the rows returned")
        images.add_argument(
            "--archived", action="store_true",
            help="list only images with archived data")

        repos = parser.add(sub, self.repos, self.repos.__doc__)
        repos.add_style_argument()
        repos.add_argument(
            "--managed", action="store_true",
            help="repos only managed repositories")

        sets = parser.add(sub, self.sets, self.sets.__doc__)
        sets.add_style_argument()
        sets.add_limit_arguments()
        sets.add_argument(
            "--order", default="newest",
            choices=("newest", "oldest", "prefix"),
            help="order of the rows returned")
        sets.add_argument(
            "--without-images", action="store_true",
            help="list only sets without images (i.e. corrupt)")
        sets.add_argument(
            "--with-transfer", nargs="+", action="append",
            help="list sets by their in-place import method")
        sets.add_argument(
            "--check", action="store_true",
            help="checks each fileset for validity (admins only)")

        for x in (images, sets):
            x.add_argument(
                "--extended", action="store_true",
                help="provide more details for each (slow)")

    def _table(self, args):
        """
        """
        from omero.util.text import TableBuilder
        tb = TableBuilder("#")
        if args.style:
            tb.set_style(args.style)
        return tb

    def _extended_info(self, client, row, values):

        from omero.cmd import ManageImageBinaries
        from omero.util.text import filesizeformat

        rsp = None
        try:
            mib = ManageImageBinaries()
            mib.imageId = row[0]
            cb = client.submit(mib)
            try:
                rsp = cb.getResponse()
            finally:
                cb.close(True)
        except Exception, e:
            self.ctx.dbg("Error on MIB: %s" % e)

        if rsp is None:
            values.extend(["ERR", "ERR"])
            return  # Early exit!

        if rsp.pixelsPresent:
            values.append(filesizeformat(rsp.pixelSize))
        elif rsp.pixelSize == 0:
            values.append(filesizeformat(0))
        else:
            v = "%s (bak)" % filesizeformat(rsp.pixelSize)
            values.append(v)
        values.append(filesizeformat(rsp.pyramidSize))

    def images(self, args):
        """List images, filtering for archives, etc.

This command is useful for showing pre-FS (i.e. OMERO 4.4
and before) images which have original data archived with
them. It *may* be possible to convert these to OMERO 5
filesets.

Examples:

    bin/omero fs images --archived       # List only OMERO4 images
    bin/omero fs images --order=newest   # Default
    bin/omero fs images --order=largest  # Most used space
    bin/omero fs images --limit=500      # Longer listings
    bin/omero fs images --extended       # More details
        """

        from omero.rtypes import unwrap
        from omero.sys import ParametersI
        from omero.util.text import filesizeformat

        select = (
            "select i.id, i.name, fs.id,"
            "count(f1.id)+count(f2.id), "
            "sum(coalesce(f1.size,0) + coalesce(f2.size, 0)) ")
        archived = (not args.archived and "left outer " or "")
        query1 = (
            "from Image i join i.pixels p "
            "%sjoin p.pixelsFileMaps m %sjoin m.parent f1 "
            "left outer join i.fileset as fs "
            "left outer join fs.usedFiles as uf "
            "left outer join uf.originalFile as f2 ") % \
            (archived, archived)
        query2 = (
            "group by i.id, i.name, fs.id ")

        if args.order == "newest":
            query3 = "order by i.id desc"
        elif args.order == "oldest":
            query3 = "order by i.id asc"
        elif args.order == "largest":
            query3 = "order by "
            query3 += "sum(coalesce(f1.size,0) + coalesce(f2.size, 0)) desc"

        client = self.ctx.conn(args)
        service = client.sf.getQueryService()

        count = unwrap(service.projection(
            "select count(i) " + query1,
            None, {"omero.group": "-1"}))[0][0]
        rows = unwrap(service.projection(
            select + query1 + query2 + query3,
            ParametersI().page(args.offset, args.limit),
            {"omero.group": "-1"}))

        # Formatting
        for row in rows:
            if row[2] is None:
                row[2] = ""
            bytes = row[4]
            row[4] = filesizeformat(bytes)

        cols = ["Image", "Name", "FS", "# Files", "Size"]
        if args.extended:
            cols.extend(["Pixels", "Pyramid"])

        tb = self._table(args)
        tb.page(args.offset, args.limit, count)
        tb.cols(cols)
        for idx, row in enumerate(rows):
            values = list(row)
            if args.extended:
                self._extended_info(client, row, values)
            tb.row(idx, *tuple(values))
        self.ctx.out(str(tb.build()))

    def repos(self, args):
        """List all repositories.

These repositories are where OMERO stores all binary data for your
system. Most useful is likely the "ManagedRepository" where OMERO 5
imports to.

Examples:

    bin/omero fs repos            # Show all
    bin/omero fs repos --managed  # Show only the managed repo
                                  # Or to print only the directory
                                  # under Unix:

    bin/omero fs repos --managed --style=plain | cut -d, -f5

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
            path = "".join([desc.path.val, desc.name.val])

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
        """List filesets by various criteria

Filesets are bundles of original data imported into OMERO 5 and above
which represent 1 *or more* images.

Examples:

    bin/omero fs sets --order=newest        # Default
    bin/omero fs sets --order=oldest
    bin/omero fs sets --order=largest
    bin/omero fs sets --without-images      # Corrupt filesets
    bin/omero fs sets --with-transfer=ln_s  # Symlinked filesets
    bin/omero fs sets --check               # Proof the checksums
        """

        from omero.constants.namespaces import NSFILETRANSFER
        from omero_sys_ParametersI import ParametersI
        from omero.rtypes import unwrap
        from omero.cmd import OK

        client = self.ctx.conn(args)
        service = client.sf.getQueryService()
        admin = client.sf.getAdminService()

        if args.check and not admin.getEventContext().isAdmin:
            self.error_admin_only(fatal=True)

        select = (
            "select fs.id, fs.templatePrefix, "
            "(select size(f2.images) from Fileset f2 where f2.id = fs.id),"
            "(select size(f3.usedFiles) from Fileset f3 where f3.id = fs.id),"
            "ann.textValue ")
        query1 = (
            "from Fileset fs "
            "left outer join fs.annotationLinks fal "
            "left outer join fal.child ann "
            "where (ann is null or ann.ns = :ns) ")
        query2 = (
            "group by fs.id, fs.templatePrefix, ann.textValue ")

        if args.order:
            if args.order == "newest":
                query2 += "order by fs.id desc"
            elif args.order == "oldest":
                query2 += "order by fs.id asc"
            elif args.order == "prefix":
                query2 += "order by fs.templatePrefix"

        if args.without_images:
            query = "%s and fs.images is empty %s" % (query1, query2)
        else:
            query = "%s %s" % (query1, query2)

        params = ParametersI()
        params.addString("ns", NSFILETRANSFER)
        count = service.projection("select count(fs) " + query1,
                                   params, {"omero.group": "-1"})

        params.page(args.offset, args.limit)
        objs = service.projection(select + query,
                                  params, {"omero.group": "-1"})
        objs = unwrap(objs)
        count = unwrap(count)[0][0]

        cols = ["Id", "Prefix", "Images", "Files", "Transfer"]
        if args.check:
            cols.append("Check")
        tb = self._table(args)
        tb.cols(cols)
        tb.page(args.offset, args.limit, count)

        # Map any requested transfers as well
        if args.with_transfer:
            restricted = [TRANSFERS.get(x, x) for x in args.with_transfer[0]]
        else:
            restricted = None

        for idx, obj in enumerate(objs):

            # Map the transfer name to the CLI symbols
            ns = obj[-1]
            if ns is None:
                ns = ""
            elif ns in TRANSFERS:
                ns = TRANSFERS[ns]
            obj[-1] = ns

            # Filter based on the ns symbols
            if restricted and ns not in restricted:
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
        """Change configuration properties for single repositories
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
