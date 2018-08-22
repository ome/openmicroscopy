#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014-2016 Glencoe Software, Inc. All Rights Reserved.
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

import platform
import sys

from collections import defaultdict
from collections import namedtuple

from omero import client as Client
from omero import CmdError
from omero import ResourceError
from omero import ServerError
from omero import ValidationException
from omero.cli import admin_only
from omero.cli import CmdControl
from omero.cli import CLI
from omero.cli import ProxyStringType
from omero.gateway import BlitzGateway
from omero.model.enums import (
    AdminPrivilegeChown,
    AdminPrivilegeWriteOwned, AdminPrivilegeWriteManagedRepo,
    AdminPrivilegeDeleteOwned, AdminPrivilegeDeleteManagedRepo)
from omero.rtypes import rstring
from omero.rtypes import unwrap
from omero.sys import Principal
from omero.util.temp_files import create_path
from omero.util.text import filesizeformat
from omero.fs import TRANSFERS

from omero.install.windows_warning import windows_warning, WINDOWS_WARNING

HELP = """Filesystem utilities"""


if platform.system() == 'Windows':
    HELP += ("\n\n%s" % WINDOWS_WARNING)

Entry = namedtuple("Entry", ("level", "id", "path", "mimetype"))


def contents(mrepo, path, ctx=None):
    """
    Yield Entry namedtuples for each return value
    from treeList for the given path.
    """
    tree = unwrap(mrepo.treeList(path, ctx))

    def parse(tree, level=0):
        for k, v in tree.items():
            yield Entry(level, v.get("id"),
                        k, v.get("mimetype"))
            if "files" in v:
                for sub in parse(v.get("files"), level+1):
                    yield sub

    for entry in parse(tree):
        yield entry


def prep_directory(client, mrepo):
    """
    Create an empty FS directory by performing an import and
    then deleting the created fileset.
    """

    from omero.cmd import Delete2, DoAll
    from omero.grid import ImportSettings

    from omero.model import ChecksumAlgorithmI
    from omero.model import FilesetI
    from omero.model import FilesetEntryI
    from omero.model import UploadJobI

    fs = FilesetI()
    fs.linkJob(UploadJobI())
    entry = FilesetEntryI()
    entry.clientPath = rstring("README.txt")
    fs.addFilesetEntry(entry)
    settings = ImportSettings()
    settings.checksumAlgorithm = ChecksumAlgorithmI()
    settings.checksumAlgorithm.value = rstring("SHA1-160")
    proc = mrepo.importFileset(fs, settings)
    try:

        tmp = create_path()
        prx = proc.getUploader(0)
        try:
            tmp.write_text("THIS IS A PLACEHOLDER")
            hash = client.sha1(tmp)
            with open(tmp, "r") as source:
                client.write_stream(source, prx)
        finally:
            prx.close()
        tmp.remove()

        handle = proc.verifyUpload([hash])
        try:
            req = handle.getRequest()
            fs = req.activity.parent
        finally:
            handle.close()

        dir = unwrap(mrepo.treeList(fs.templatePrefix.val))
        oid = dir.items()[0][1].get("id")
        ofile = client.sf.getQueryService().get("OriginalFile", oid)

        delete1 = Delete2(targetObjects={'Fileset': [fs.id.val]})
        delete2 = Delete2(targetObjects={'OriginalFile': [ofile.id.val]})
        doall = DoAll()
        doall.requests = [delete1, delete2]
        cb = client.submit(doall)
        cb.close(True)

    finally:
        proc.close()

    return fs.templatePrefix.val


def get_logfile(query, fid):
    from omero.sys import ParametersI
    q = ("select o from FilesetJobLink l "
         "join l.parent as fs join l.child as j "
         "join j.originalFileLinks l2 join l2.child as o "
         "where fs.id = :id and "
         "o.mimetype = 'application/omero-log-file'")
    return query.findByQuery(q, ParametersI().addId(fid))


def rename_fileset(client, mrepo, fileset, new_dir, ctx=None):
    """
    Loads each OriginalFile found under orig_dir and
    updates its path field to point at new_dir. Files
    are not yet moved.
    """

    from omero.constants.namespaces import NSFSRENAME
    from omero.model import CommentAnnotationI
    from omero.model import FilesetAnnotationLinkI

    tomove = []
    tosave = []
    query = client.sf.getQueryService()
    update = client.sf.getUpdateService()
    orig_dir = fileset.templatePrefix.val

    def parse_parent(dir):
        """
        Note that final elements are empty
        """
        parts = dir.split("/")
        parpath = "/".join(parts[0:-2]+[""])
        parname = parts[-2]
        logname = parts[-2] + ".log"
        return parpath, parname, logname

    orig_parpath, orig_parname, orig_logname = parse_parent(orig_dir)
    new_parpath, new_parname, new_logname = parse_parent(new_dir)

    for entry in contents(mrepo, orig_dir, ctx):

        ofile = query.get("OriginalFile", entry.id, ctx)
        path = ofile.path.val

        if entry.level == 0:
            tomove.append((orig_dir, new_dir))
            assert orig_parpath in path
            repl = path.replace(orig_parpath, new_parpath)
            ofile.name = rstring(new_parname)
        else:
            assert orig_dir in path
            repl = path.replace(orig_dir, new_dir)
        ofile.path = rstring(repl)
        tosave.append(ofile)
    fileset.templatePrefix = rstring(new_dir)
    # TODO: placing the fileset at the end of this list
    # causes ONLY the fileset to be updated !!
    tosave.insert(0, fileset)

    # Add an annotation to the fileset as well so
    # we can detect if something's gone wrong.
    link = FilesetAnnotationLinkI()
    link.parent = fileset.proxy()
    link.child = CommentAnnotationI()
    link.child.ns = rstring(NSFSRENAME)
    link.child.textValue = rstring("previous=%s" % orig_dir)
    tosave.insert(1, link)

    # And now move the log file as well:
    log = get_logfile(query, fileset.id.val)
    if log is not None:
        target = new_parpath + new_logname
        source = orig_parpath + orig_logname
        tomove.append((source, target))
        log.path = rstring(new_parpath)
        log.name = rstring(new_logname)
        tosave.append(log)

    # Done. Save in one transaction and return tomove
    update.saveAndReturnArray(tosave, ctx)
    return tomove


class FsControl(CmdControl):

    def _configure(self, parser):

        parser.add_login_arguments()
        sub = parser.sub()

        images = parser.add(sub, self.images)
        images.add_style_argument()
        images.add_limit_arguments()
        images.add_argument(
            "--order", default="newest",
            choices=("newest", "oldest", "largest"),
            help="order of the rows returned")
        images.add_argument(
            "--archived", action="store_true",
            help="list only images with archived data")

        mkdir = parser.add(sub, self.mkdir)
        mkdir.add_argument(
            "new_dir",
            help="directory to create in the repository")
        mkdir.add_argument(
            "--parents", action="store_true",
            help="ensure whole path exists")

        rename = parser.add(sub, self.rename)
        rename.add_argument(
            "fileset",
            type=ProxyStringType("Fileset"),
            help="Fileset which should be renamed: ID or Fileset:ID")
        rename.add_argument(
            "--no-move", action="store_true",
            help="do not move original files and import log")

        repos = parser.add(sub, self.repos)
        repos.add_style_argument()
        repos.add_argument(
            "--managed", action="store_true",
            help="repos only managed repositories")

        sets = parser.add(sub, self.sets)
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
            help="verify the file checksums for each fileset (admins only)")

        ls = parser.add(sub, self.ls)
        ls.add_argument(
            "fileset",
            type=ProxyStringType("Fileset"))

        logfile = parser.add(sub, self.logfile)
        logfile.add_argument("fileset", type=ProxyStringType("Fileset"))
        logfile.add_argument(
            "filename",  nargs="?", default="-",
            help="Local filename to be saved to. '-' for stdout")
        logopts = logfile.add_mutually_exclusive_group()
        logopts.add_argument(
            "--name", action="store_true",
            help="return the path of the logfile within the ManagedRepository")
        logopts.add_argument(
            "--size", action="store_true",
            help="return the size of the logfile in bytes")

        usage = parser.add(sub, self.usage)
        usage.set_args_unsorted()
        usage.add_login_arguments()
        usage.add_style_argument()
        usage.add_argument(
            "--wait", type=long,
            help="Number of seconds to wait for the processing to complete "
            "(Indefinite < 0; No wait=0).", default=-1)
        usage.add_argument(
            "--size-only", action="store_true",
            help="Print total bytes used in bytes")
        usage.add_argument(
            "--report", action="store_true",
            help="Print detailed breakdown of disk usage")
        usage.add_argument(
            "--sum-by", nargs="+", choices=("user", "group", "component"),
            help=("Breakdown of disk usage by a combination of "
                  "user, group and component"))
        usage.add_argument(
            "--sort-by", nargs="+",
            choices=("user", "group", "component", "size", "files"),
            help=("Sort the report table by one or more of "
                  "user, group, component, size and files"))
        usage.add_argument(
            "--reverse", action="store_true",
            help="Reverse sort order")
        unit_group = usage.add_mutually_exclusive_group()
        unit_group.add_argument(
            "--units", choices="KMGTP",
            help="Units to use for disk usage")
        unit_group.add_argument(
            "--human-readable", action="store_true",
            help="Use most appropriate units")
        usage.add_argument(
            "--groups",  action="store_true",
            help="Print size for all current user's groups")
        usage.add_argument(
            "obj", nargs="*",
            help=("Objects to be queried in the form "
                  "'<Class>:<Id>[,<Id> ...]', or '<Class>:*' "
                  "to query all the objects of the given type "))

        importtime = parser.add(sub, self.importtime)
        importtime.add_argument(
            "fileset", nargs="?",
            type=ProxyStringType("Fileset"))
        importtime_alternatives = importtime.add_mutually_exclusive_group()
        importtime_alternatives.add_argument(
            "--cache", action="store_true",
            help="to cache the results by annotating the fileset")
        importtime_alternatives.add_argument(
            "--summary", action="store_true",
            help="summarize the results cached for filesets")

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

    @admin_only(AdminPrivilegeWriteManagedRepo, AdminPrivilegeChown)
    def mkdir(self, args):
        """Make a new directory (admin-only)

Creates a new empty directory in the managed repository.
A new storage volume may then be mounted at that location
and the import template (omero.fs.repo.path) adjusted to
target it. Once created, the directory may be deleted from
the underlying filesystem and replaced with a symbolic link.
Directories that violate the root-owned prefix components of
omero.fs.repo.path are all set to be owned by the root user.
"""

        if len(args.new_dir) < 2:
            raise ValueError("directory path too short", args.new_dir)
        if args.new_dir[0] == '/':
            args.new_dir = args.new_dir[1:]
        if args.new_dir[-1] != '/':
            args.new_dir += '/'

        client = self.ctx.conn(args)

        mrepo = client.getManagedRepository()
        mrepo.makeDir(args.new_dir, args.parents)

    @windows_warning
    # Remove decorator from disabled rename to more promptly raise Exception.
    # @admin_only(AdminPrivilegeWriteOwned, AdminPrivilegeWriteManagedRepo,
    #             AdminPrivilegeDeleteOwned, AdminPrivilegeDeleteManagedRepo)
    def rename(self, args):
        """Moves an existing fileset to a new location (admin-only)

After the import template (omero.fs.repo.path) has been changed,
it may be useful to rename an existing fileset to match the new
template. By default the original files and import log are also
moved.
"""

        # See https://trello.com/c/J3LNquSH/ for more information.
        # When reenabling, also reenable testRenameAdminOnly.
        self.ctx.die(30, 'disabled since OMERO 5.4.7 due to Pixels.path bug')
        # Keep privilege imports used until @admin_only decorator restored.
        [AdminPrivilegeWriteOwned, AdminPrivilegeWriteManagedRepo,
         AdminPrivilegeDeleteOwned, AdminPrivilegeDeleteManagedRepo]

        fid = args.fileset.id.val
        client = self.ctx.conn(args)
        uid = self.ctx.get_event_context().userId
        isAdmin = self.ctx.get_event_context().isAdmin
        query = client.sf.getQueryService()

        try:
            fileset = query.get("Fileset", fid, {"omero.group": "-1"})
            p = fileset.details.permissions
            oid = fileset.details.owner.id.val
            gid = fileset.details.group.id.val
            if not p.canEdit():
                self.ctx.die(110, "Cannot edit Fileset:%s" % fid)
            elif oid != uid and not isAdmin:
                self.ctx.die(111, "Fileset:%s belongs to %s" % (fid, oid))
        except ServerError, se:
            self.ctx.die(
                112, "Could not load Fileset:%s- %s" % (fid, se.message))

        new_client = None
        if oid != uid:
            user = query.get("Experimenter", oid)
            group = query.get("ExperimenterGroup", gid)
            principal = Principal(
                user.omeName.val, group.name.val, "Sessions")
            service = client.sf.getSessionService()
            session = service.createSessionWithTimeouts(
                principal, 0, 30000)
            props = client.getPropertyMap()
            new_client = Client(props)
            new_client.joinSession(session.uuid.val)
            client = new_client

        tomove = []
        try:
            mrepo = client.getManagedRepository()
            root = mrepo.root()
            prefix = prep_directory(client, mrepo)
            self.ctx.err("Renaming Fileset:%s to %s" % (fid, prefix))
            tomove = rename_fileset(client, mrepo, fileset, prefix)
        finally:
            if new_client is not None:
                new_client.__del__()

        if not tomove:
            self.ctx.die(113, "No files moved!")
        elif not args.no_move:
            from omero.grid import RawAccessRequest
            for from_path, to_path in tomove:
                raw = RawAccessRequest()
                raw.repoUuid = root.hash.val
                raw.command = "mv"
                raw.args = [from_path, to_path]
                self.ctx.err("Moving %s to %s" % (from_path, to_path))
                try:
                    self.ctx.get_client().submit(raw)
                except CmdError, ce:
                    self.ctx.die(114, ce.err)
        else:
            self.ctx.err(
                "Done. You will now need to move these files manually:")
            self.ctx.err(
                "-----------------------------------------------------")
            b = "".join([root.path.val, root.name.val])
            for from_path, to_path in tomove:
                t = "/".join([b, to_path])
                f = "/".join([b, from_path])
                cmd = "mv %s %s" % (f, t)
                self.ctx.out(cmd)

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

        client = self.ctx.conn(args)
        service = client.sf.getQueryService()
        admin = client.sf.getAdminService()

        if args.check and not admin.getEventContext().isAdmin:
            self.error_admin_only(fatal=True)

        annselect = (
            "(select ann.textValue from Fileset f4 "
            "join f4.annotationLinks fal join fal.child ann "
            "where f4.id = fs.id and ann.ns =:ns) ")
        select = (
            "select fs.id, fs.templatePrefix, "
            "(select size(f2.images) from Fileset f2 "
            "where f2.id = fs.id),"
            "(select size(f3.usedFiles) from Fileset f3 "
            "where f3.id = fs.id),") \
            + annselect
        query1 = (
            "from Fileset fs "
            "where 1 = 1 ")
        query2 = (
            "group by fs.id, fs.templatePrefix ")

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
                    try:
                        cb = client.submit(raw)
                        cb.close(True)
                    except CmdError, ce:
                        err = ce.err
                        self.ctx.dbg(err)

                if err:
                    obj.append("ERROR!")
                elif rows:
                    obj.append("OK")

            tb.row(idx, *tuple(obj))
        self.ctx.out(str(tb.build()))

    def ls(self, args):
        """List all the original files contained in a fileset"""
        client = self.ctx.conn(args)
        gateway = BlitzGateway(client_obj=client)
        gateway.SERVICE_OPTS.setOmeroGroup("-1")
        fileset = gateway.getObject("Fileset", args.fileset.id.val)

        defaultdict(list)
        for ofile in fileset.listFiles():
            print ofile.path + ofile.name

    def logfile(self, args):
        """Return the logfile associated with a fileset"""
        client = self.ctx.conn(args)
        query = client.sf.getQueryService()
        log = get_logfile(query, args.fileset.id.val)
        if log is not None:
            if args.name:
                self.ctx.out(log.path.val + log.name.val)
            elif args.size:
                self.ctx.out(log.size.val)
            else:
                target_file = str(args.filename)
                try:
                    if target_file == "-":
                        client.download(log, filehandle=sys.stdout)
                        sys.stdout.flush()
                    else:
                        client.download(log, target_file)
                except ValidationException, ve:
                    # This should effectively be handled by None being
                    # returned from the logfile query above.
                    self.ctx.die(115, "ValidationException: %s" % ve.message)
                except ResourceError, re:
                    # ID exists in DB, but not on FS
                    self.ctx.die(116, "ResourceError: %s" % re.message)
        else:
            self.ctx.die(
                117,
                "Log file not accessible for Fileset:%s" % args.fileset.id.val)

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

    def usage(self, args):
        """Shows the disk usage for various objects.

This command shows the total disk usage of various objects including:
ExperimenterGroup, Experimenter, Project, Dataset, Folder, Screen, Plate,
Well, WellSample, Image, Pixels, Annotation, Job, Fileset, OriginalFile.
The total size returned will comprise the disk usage by all related files. Thus
an image's size would typically include the files uploaded to a fileset,
import log (Job), thumbnails, and, possibly, associated pixels or original
files. These details can be displayed using the --report option.

Examples:

    bin/omero fs usage             # total usage for current user
    bin/omero fs usage --report    # more detailed usage for current user
    bin/omero fs usage --groups    # total usage for current user's groups
    # total usage for five images with minimal output
    bin/omero fs usage Image:1,2,3,4,5 --size_only
    # total usage for all images with in a human readable format
    bin/omero fs usage Image:* --human-readable
    # total usage for all users broken down by user and group
    bin/omero fs usage Experimenter:* --report --sum-by user group
    # total usage for two projects and one dataset Megabytes
    bin/omero fs usage Project:1,2 Dataset:5 --units M
    # in this last case if the dataset was within project 1 or 2
    # then the size returned would be identical to:
    bin/omero fs usage Project:1,2 --units M
        """
        from omero.cmd import DiskUsage2

        client = self.ctx.conn(args)
        req = DiskUsage2()
        if not args.obj:
            admin = client.sf.getAdminService()
            uid = admin.getEventContext().userId
            if not args.groups:
                args.obj.append("Experimenter:%d" % uid)
            else:
                exp = admin.getExperimenter(uid)
                groups = exp.linkedExperimenterGroupList()
                gids = [x.id.val for x in groups]
                args.obj.append(
                    "ExperimenterGroup:%s" % ",".join(map(str, gids)))

        req.targetObjects, req.targetClasses = self._usage_obj(args.obj)
        cb = None
        try:
            rsp, status, cb = self.response(client, req, wait=args.wait)
            self._usage_report(req, rsp, status, args)
        finally:
            if cb is not None:
                cb.close(True)  # Close handle

    def _usage_obj(self, obj):
        """
        Take the positional arguments and marshal them into
        a dictionary and a list for the command argument.
        """
        objects = {}
        classes = set()
        for o in obj:
            try:
                parts = o.split(":", 1)
                assert len(parts) == 2
                klass = parts[0]
                if '*' in parts[1]:
                    classes.add(klass)
                else:
                    ids = [long(id) for id in parts[1].split(",")]
                    objects.setdefault(klass, []).extend(ids)
            except:
                raise ValueError("Bad object: ", o)

        return (objects, list(classes))

    def _to_units(self, size, units):
        """
        Convert from bytes to KiB, MiB, GiB, TiB or PiB.
        """
        oneK = 1024.0
        powers = {'K': 1, 'M': 2, 'G': 3, 'T': 4, 'P': 5}
        if units in powers.keys():
            return round(size/oneK**powers[units], 1)
        else:
            raise ValueError("Unrecognized units: ", units)

    def _usage_report(self, req, rsp, status, args):
        """
        Output the total bytes used or the error,
        optionally provide more details.
        """
        err = self.get_error(rsp)
        if err:
            self.ctx.err("Error: " + rsp.parameters['message'])
        else:
            size = sum(rsp.totalBytesUsed.values())
            if args.size_only:
                self.ctx.out(size)
            else:
                files = sum(rsp.totalFileCount.values())
                if args.units:
                    size = ("%s %siB"
                            % (self._to_units(size, args.units), args.units))
                elif args.human_readable:
                    size = filesizeformat(size)
                self.ctx.out(
                    "Total disk usage: %s bytes in %d files"
                    % (size, files))

            if args.report and not args.size_only and size > 0:
                self._detailed_usage_report(req, rsp, status, args)

    def _detailed_usage_report(self, req, rsp, status, args):
        """
        Print a breakdown of disk usage in table form, including user,
        group and component information according to the args.
        """
        from omero.util.text import TableBuilder

        sum_by = ("user", "group", "component")
        if args.sum_by is not None:
            sum_by = args.sum_by
        showCols = list(sum_by)
        showCols.extend(["size", "files"])

        align = 'l'*len(sum_by)
        align += 'rr'
        tb = TableBuilder(*showCols)
        tb.set_align(align)
        if args.style:
            tb.set_style(args.style)

        subtotals = {}
        if "component" in sum_by:
            for userGroup in rsp.bytesUsedByReferer.keys():
                for (element, size) in rsp.bytesUsedByReferer[
                        userGroup].items():
                    files = rsp.fileCountByReferer[userGroup][element]
                    keyList = []
                    if "user" in sum_by:
                        keyList.append(userGroup.first)
                    if "group" in sum_by:
                        keyList.append(userGroup.second)
                    keyList.append(element)
                    key = tuple(keyList)
                    if key in subtotals.keys():
                        subtotals[key][0] += size
                        subtotals[key][1] += files
                    else:
                        subtotals[key] = [size, files]
        else:
            for userGroup in rsp.totalBytesUsed.keys():
                size = rsp.totalBytesUsed[userGroup]
                files = rsp.totalFileCount[userGroup]
                keyList = []
                if "user" in sum_by:
                    keyList.append(userGroup.first)
                if "group" in sum_by:
                    keyList.append(userGroup.second)
                key = tuple(keyList)
                if key in subtotals.keys():
                    subtotals[key][0] += size
                    subtotals[key][1] += files
                else:
                    subtotals[key] = [size, files]

        for key in subtotals.keys():
            row = list(key)
            row.extend(subtotals[key])
            tb.row(*tuple(row))

        # Since an order in the response is not guaranteed if not sort keys
        # are specified then sort by the first column at least.
        if args.sort_by:
            keys = []
            for col in args.sort_by:
                try:
                    pos = showCols.index(col)
                    keys.append(pos)
                except:
                    pass
        else:
            keys = [0]
        tb.sort(cols=keys, reverse=args.reverse)

        # Format the size column after sorting.
        if args.units:
            col = tb.get_col("size")
            col = [self._to_units(val, args.units) for val in col]
            tb.replace_col("size", col)
            tb.replace_header("size", "size (%siB)" % args.units)
        elif args.human_readable:
            col = tb.get_col("size")
            col = [filesizeformat(val) for val in col]
            tb.replace_col("size", col)
        else:
            tb.replace_header("size", "size (bytes)")

        self.ctx.out(str(tb.build()))

    def importtime(self, args):
        """Find out how long it took to import an existing fileset"""
        client = self.ctx.conn(args)
        import_time = ImportTime(self.ctx, client.sf.getQueryService())
        if args.fileset:
            if args.summary:
                self.ctx.die(28, "no summary if fileset provided")
            import_time.fileset_id = args.fileset.id
            import_time.get_cache()
            if not import_time.metrics:
                import_time.query_durations()
                import_time.query_counts()
                if args.cache:
                    import_time.write_cache(client.sf.getUpdateService())
            import_time.print_report()
        elif args.summary:
            import_time.print_summary()
        else:
            self.ctx.die(29, "provide fileset or request summary")


class ImportTime:

    def __init__(self, ctx, query):
        self.cli_ctx = ctx
        self.ice_ctx = {"omero.group": "-1"}
        self.query = query
        self.ns = 'openmicroscopy.org/omero/import/metrics'
        self.metrics = dict()

        import_tuples = [
            ('UPLOAD', 'upload (ms)'), ('UPLOAD_C', '# files'),
            ('SET_ID', 'setId (ms)'),
            ('METADATA', 'metadata (ms)'),
            ('PIXELDATA', 'pixeldata (ms)'), ('PIXELDATA_C', '# planes'),
            ('OVERLAY', 'overlays (ms)'),
            ('RDEF', 'rnd defs (ms)'), ('RDEF_C', '# settings'),
            ('THUMBNAIL', 'thumbnails (ms)'), ('THUMBNAIL_C', '# thumbnails')
        ]

        # Easier in Python 2.7 with OrderedDict.
        # Even easier in Python 3.7 in which dictionaries preserve ordering.
        self.import_phases = [key for (key, name) in import_tuples]
        self.import_phases_to_names = dict(import_tuples)
        self.import_names_to_phases = dict(
            [(y, x) for (x, y) in import_tuples])

    def query_durations(self):
        """Determine values for the phase durations for the import metrics"""
        from omero.sys import ParametersI

        # Get the upload job ID and its creation time, and the import log ID.

        hql = (
            "SELECT u.id, u.details.creationEvent.time, jol.child.id "
            "FROM FilesetJobLink fjl, UploadJob u, JobOriginalFileLink jol "
            "WHERE :id = fjl.parent.id AND fjl.child = u AND u = jol.parent "
            "AND jol.child.mimetype = :mimetype "
            "ORDER BY u.id"
        )

        results = self.query.projection(
            hql, ParametersI()
            .addId(self.fileset_id)
            .addString('mimetype', 'application/omero-log-file')
            .page(0, 1),
            self.ice_ctx)

        if not results:
            self.cli_ctx.die(30, 'Could not query for import log.')

        upload_job_id = results[0][0].val
        upload_start = results[0][1].val
        import_log_id = results[0][2].val

        # From the event log to find when the upload job was first updated.

        hql = (
            "SELECT event.time "
            "FROM EventLog "
            "WHERE action = :action "
            "AND entityType = :type AND entityId = :id "
            "ORDER BY id"
        )

        results = self.query.projection(
            hql, ParametersI()
            .addId(upload_job_id)
            .addString('type', 'ome.model.jobs.UploadJob')
            .addString('action', 'UPDATE')
            .page(0, 1),
            self.ice_ctx)

        if not results:
            self.cli_ctx.die(31, 'Upload job is created but not yet updated.')

        upload_end = results[0][0].val

        # Find when the import log was updated.
        # Its size is set after each step.

        hql = (
            "SELECT id, event.time "
            "FROM EventLog "
            "WHERE action = :action "
            "AND entityType = :type AND entityId = :id "
            "ORDER BY id"
        )

        results = self.query.projection(
            hql, ParametersI()
            .addId(import_log_id)
            .addString('type', 'ome.model.core.OriginalFile')
            .addString('action', 'UPDATE')
            .page(0, 3),
            self.ice_ctx)

        if not results or len(results) < 3:
            self.cli_ctx.die(32, 'Thumbnails step is not yet finished.')

        metadata_before_id = results[0][0]
        pixeldata_before_id = results[1][0]
        thumbnails_before_id = results[2][0]
        metadata_end = results[0][1].val
        pixeldata_end = results[1][1].val
        thumbnails_end = results[2][1].val

        # Find when the fileset's images were created.
        # Used as an estimate for when setId completed.
        # Ignores any inserts that follow the metadata phase.

        hql = (
            "SELECT el.event.time "
            "FROM Image i, EventLog el "
            "WHERE el.id < :last AND el.action = :action "
            "AND el.entityType = :type AND el.entityId = i.id "
            "AND i.fileset.id = :id"
        )

        results = self.query.projection(
            hql, ParametersI()
            .addId(self.fileset_id)
            .addString('type', 'ome.model.core.Image')
            .addString('action', 'INSERT')
            .add('last', metadata_before_id)
            .page(0, 1),
            self.ice_ctx)

        if not results:
            self.cli_ctx.die(33, 'Could not find images from metadata step.')

        set_id_end = results[0][0].val

        # Find when any ROIs were created during the thumbnails step.
        # These would be overlays as one finds with MIAS plates.

        hql = (
            "SELECT el.event.time "
            "FROM Roi r, EventLog el "
            "WHERE el.id > :first AND el.id < :last AND el.action = :action "
            "AND el.entityType = :type AND el.entityId = r.id "
            "AND r.image.fileset.id = :id"
        )

        results = self.query.projection(
            hql, ParametersI()
            .addId(self.fileset_id)
            .addString('type', 'ome.model.roi.Roi')
            .addString('action', 'INSERT')
            .add('first', pixeldata_before_id)
            .add('last', thumbnails_before_id)
            .page(0, 1),
            self.ice_ctx)

        overlays_start = results[0][0].val if results else None

        # Find when any rendering settings were created during the thumbnails
        # step. These are created if the thumbnails can already be generated.

        hql = (
            "SELECT el.event.time "
            "FROM RenderingDef r, EventLog el "
            "WHERE el.id > :first AND el.id < :last AND el.action = :action "
            "AND el.entityType = :type AND el.entityId = r.id "
            "AND r.pixels.image.fileset.id = :id"
        )

        results = self.query.projection(
            hql, ParametersI()
            .addId(self.fileset_id)
            .addString('type', 'ome.model.display.RenderingDef')
            .addString('action', 'INSERT')
            .add('first', pixeldata_before_id)
            .add('last', thumbnails_before_id)
            .page(0, 1),
            self.ice_ctx)

        settings_start = results[0][0].val if results else None

        # Find when any thumbnails were created during the thumbnails step.
        # These are created even if it is not yet possible to generate them.

        hql = (
            "SELECT el.event.time "
            "FROM Thumbnail t, EventLog el "
            "WHERE el.id > :first AND el.id < :last AND el.action = :action "
            "AND el.entityType = :type AND el.entityId = t.id "
            "AND t.pixels.image.fileset.id = :id"
        )

        results = self.query.projection(
            hql, ParametersI()
            .addId(self.fileset_id)
            .addString('type', 'ome.model.display.Thumbnail')
            .addString('action', 'INSERT')
            .add('first', pixeldata_before_id)
            .add('last', thumbnails_before_id)
            .page(0, 1),
            self.ice_ctx)

        thumbnails_start = results[0][0].val if results else None

        # Calculate duration of import phases.

        self.metrics['UPLOAD'] = upload_end - upload_start
        self.metrics['SET_ID'] = set_id_end - upload_end
        self.metrics['METADATA'] = metadata_end - set_id_end

        if overlays_start:
            if settings_start:
                self.metrics['OVERLAY'] = settings_start - pixeldata_end
            elif thumbnails_start:
                self.metrics['OVERLAY'] = thumbnails_start - pixeldata_end
            else:
                self.metrics['OVERLAY'] = thumbnails_end - pixeldata_end

        if settings_start:
            # If there are no rendering settings, pyramids must be built first.
            self.metrics['PIXELDATA'] = pixeldata_end - metadata_end

            if thumbnails_start:
                self.metrics['RDEF'] = thumbnails_start - settings_start
                self.metrics['THUMBNAIL'] = thumbnails_end - thumbnails_start
            else:
                self.metrics['RDEF'] = thumbnails_end - settings_start

    def query_counts(self):
        """Determine values for the per-item counts for the import metrics"""
        from omero.sys import ParametersI
        fileset = ParametersI().addId(self.fileset_id)

        hql = (
            "SELECT COUNT(*) FROM FilesetEntry " +
            "WHERE fileset.id = :id"
        )

        result = self.query.projection(hql, fileset,
                                       self.ice_ctx)[0][0].val
        if result > 0:
            self.metrics['UPLOAD_C'] = result

        if 'PIXELDATA' in self.metrics:
            hql = (
                "SELECT SUM(sizeC * sizeT * sizeZ) FROM Pixels " +
                "WHERE image.fileset.id = :id"
            )

            result = self.query.projection(hql, fileset,
                                           self.ice_ctx)[0][0].val
            if result > 0:
                self.metrics['PIXELDATA_C'] = result

        if 'RDEF' in self.metrics:
            hql = (
                "SELECT COUNT(*) FROM RenderingDef " +
                "WHERE pixels.image.fileset.id = :id " +
                "AND details.owner = pixels.details.owner"
                )

            result = self.query.projection(hql, fileset,
                                           self.ice_ctx)[0][0].val
            if result > 0:
                self.metrics['RDEF_C'] = result

        if 'THUMBNAIL' in self.metrics:
            hql = (
                "SELECT COUNT(*) FROM Thumbnail " +
                "WHERE pixels.image.fileset.id = :id " +
                "AND details.owner = pixels.details.owner"
                )

            result = self.query.projection(hql, fileset,
                                           self.ice_ctx)[0][0].val
            if result > 0:
                self.metrics['THUMBNAIL_C'] = result

    def get_cache(self):
        """Retrieve import metrics from a map annotation on the fileset"""
        from omero.sys import ParametersI

        hql = (
            "SELECT mv.name, mv.value "
            "FROM FilesetAnnotationLink AS l "
            "JOIN l.child.mapValue AS mv "
            "WHERE l.parent.id = :id AND l.child.ns = :ns"
        )

        results = self.query.projection(
            hql, ParametersI()
            .addId(self.fileset_id)
            .addString('ns', self.ns),
            self.ice_ctx)

        if results:
            for [name, value] in results:
                phase = self.import_names_to_phases.get(name.val)
                if phase:
                    self.metrics[phase] = long(value.val)

    def write_cache(self, update):
        """Write import metrics to a map annotation on the fileset"""
        from omero.model import FilesetI
        from omero.model import FilesetAnnotationLinkI
        from omero.model import MapAnnotationI
        from omero.model import NamedValue

        link = FilesetAnnotationLinkI()
        link.parent = FilesetI(self.fileset_id, False)
        link.child = MapAnnotationI()

        link.child.ns = rstring(self.ns)
        link.child.mapValue = []

        for phase in self.import_phases:
            if phase in self.metrics:
                link.child.mapValue.append(NamedValue(
                    self.import_phases_to_names[phase],
                    str(self.metrics[phase])))

        update.saveObject(link)

    def print_report(self):
        """Report how long it took to import an existing fileset"""
        metrics_keys = set(self.metrics)

        if set(['UPLOAD', 'UPLOAD_C']) <= metrics_keys:
            time = self.metrics['UPLOAD'] / 1000.0
            count = self.metrics['UPLOAD_C']
            plural = "s" if count > 1 else ""
            print(("   upload time of {0:6.2f}s for "
                   "{1} file{2} ({3:.3f}s/file)")
                  .format(time, count, plural, time/count))

        time = self.metrics['SET_ID'] / 1000.0
        print("    setId time of {0:6.2f}s".format(time))

        time = self.metrics['METADATA'] / 1000.0
        print(" metadata time of {0:6.2f}s".format(time))

        if set(['PIXELDATA', 'PIXELDATA_C']) <= metrics_keys:
            time = self.metrics['PIXELDATA'] / 1000.0
            count = self.metrics['PIXELDATA_C']
            plural = "s" if count > 1 else ""
            print(("   pixels time of {0:6.2f}s for "
                   "{1} plane{2} ({3:.3f}s/plane)")
                  .format(time, count, plural, time/count))

        if 'OVERLAY' in metrics_keys:
            time = self.metrics['OVERLAY'] / 1000.0
            print(" overlays time of {0:6.2f}s".format(time))

        if set(['RDEF', 'RDEF_C']) <= metrics_keys:
            time = self.metrics['RDEF'] / 1000.0
            count = self.metrics['RDEF_C']
            plural = "s" if count > 1 else ""
            print(("    rdefs time of {0:6.2f}s for "
                   "{1} rendering setting{2} ({3:.3f}s/rdef)")
                  .format(time, count, plural, time/count))

        if set(['THUMBNAIL', 'THUMBNAIL_C']) <= metrics_keys:
            time = self.metrics['THUMBNAIL'] / 1000.0
            count = self.metrics['THUMBNAIL_C']
            plural = "s" if count > 1 else ""
            print(("thumbnail time of {0:6.2f}s for "
                   "{1} thumbnail{2} ({3:.3f}s/thumbnail)")
                  .format(time, count, plural, time/count))

    def print_summary(self):
        """Report import metrics from map annotations on filesets"""
        from omero.sys import ParametersI

        hql = (
            "SELECT l.parent.id, mv.name, mv.value "
            "FROM FilesetAnnotationLink AS l "
            "JOIN l.child.mapValue AS mv "
            "WHERE l.child.ns = :ns "
            "ORDER BY l.parent.id"
        )

        results = self.query.projection(
            hql, ParametersI()
            .addString('ns', self.ns),
            self.ice_ctx)

        if not results:
            print "no import times to report"
            return

        columns = ['fileset']
        for phase in self.import_phases:
            columns.append(self.import_phases_to_names[phase])
        print(','.join(['"{0}"'.format(column) for column in columns]))

        self.fileset_id = None

        for result in results:
            if self.fileset_id != result[0].val:
                self.print_summary_line()
                self.fileset_id = result[0].val
            phase = self.import_names_to_phases.get(result[1].val)
            if phase:
                self.metrics[phase] = long(result[2].val)
        self.print_summary_line()

    def print_summary_line(self):
        """Report import metrics from the map annotations on a fileset"""
        if not self.metrics:
            return
        values = [str(self.fileset_id)]
        for phase in self.import_phases:
            if phase in self.metrics:
                values.append(str(self.metrics[phase]))
            else:
                values.append('')
        print(','.join(values))
        self.metrics.clear()


try:
    register("fs", FsControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("fs", FsControl, HELP)
        cli.invoke(sys.argv[1:])
