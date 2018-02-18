#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Reconcile and cleanse where necessary an OMERO data directory of orphaned data.
"""

#
#  Copyright (c) 2009-2016 University of Dundee. All rights reserved.
#
#  Redistribution and use in source and binary forms, with or without
#  modification, are permitted provided that the following conditions
#  are met:
#  1. Redistributions of source code must retain the above copyright
#     notice, this list of conditions and the following disclaimer.
#  2. Redistributions in binary form must reproduce the above copyright
#     notice, this list of conditions and the following disclaimer in the
#     documentation and/or other materials provided with the distribution.
#
#  THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
#  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
#  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
#  ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
#  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
#  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
#  OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
#  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
#  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
#  OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
#  SUCH DAMAGE.

import omero.clients
import omero
import sys
import os
import getpass

from Glacier2 import PermissionDeniedException
from getopt import getopt, GetoptError
from omero.callbacks import CmdCallbackI
from omero.cmd import DoAll
from omero.util import get_user
from stat import ST_SIZE

import time


# The directories underneath an OMERO data directory to search for "dangling"
# files and reconcile with the database. Directory name key and corresponding
# OMERO data type value.
SEARCH_DIRECTORIES = {
    'Pixels': 'Pixels',
    'Files': 'OriginalFile',
    'Thumbnails': 'Thumbnail'
}


def usage(error):
    """
    Prints usage so that we don't have to. :)
    """
    cmd = sys.argv[0]
    print """%s
Usage: %s [--dry-run] [-u username | -k] <omero.data.dir>
Cleanses files in the OMERO data directory that have no reference in the
OMERO database. NOTE: As this script is designed to be run via cron or in
a scheduled manner it produces NO output unless a dry run is performed.

Options:
  -u          Administrator username to log in to OMERO with
  -k          Session key to log in to OMERO with
  --dry-run   Just prints out what would have been done

Examples:
  %s --dry-run -u root /OMERO

Report bugs to OME Users <ome-users@lists.openmicroscopy.org.uk>""" % \
        (error, cmd, cmd)
    sys.exit(2)


class Cleanser(object):

    """
    Keeps file cleansing state and performs OMERO database reconciliation of
    files within an OMERO binary repository.
    """

    # Number of objects to defer before we actually make a query
    QUERY_THRESHOLD = 25

    # Strings identifying pyramid files
    PYRAMID_FILE = "_pyramid"
    PYRAMID_LOCK = ".pyr_lock"
    PYRAMID_TEMP = ".tmp"

    def __init__(self, query_service, object_type):
        self.query_service = query_service
        self.object_type = object_type
        self.cleansed = list()
        self.bytes_cleansed = 0
        self.deferred_paths = list()
        self.dry_run = False

    def cleanse(self, root):
        """
        Begins a cleansing operation from a given OMERO binary repository
        root directory. /OMERO/Files or /OMERO/Pixels for instance.
        """
        for file in os.listdir(root):
            path = os.path.join(root, file)
            if os.path.isdir(path):
                self.cleanse(path)
            else:
                self.query_or_defer(path)

    def query_or_defer(self, path):
        """
        Adds a given path to the list of deferred paths. If the number of
        deferred paths has reached the QUERY_THRESHOLD (to reduce database
        hits) a reconciliation check will happen against OMERO.
        """
        self.deferred_paths.append(path)
        if len(self.deferred_paths) == self.QUERY_THRESHOLD:
            self.do_cleanse()

    def do_cleanse(self):
        """
        Actually performs the reconciliation check against OMERO and
        removes relevant files.
        """
        if len(self.deferred_paths) == 0:
            return
        split = os.path.split
        object_ids = []
        for path in self.deferred_paths:
            file_name = split(path)[1]
            try:
                object_id = omero.rtypes.rlong(long(file_name))
            except ValueError:
                try:
                    file_name.index(self.PYRAMID_FILE)
                    id_part = file_name.split("_")[0]
                    if file_name.endswith(self.PYRAMID_FILE):
                        object_id = omero.rtypes.rlong(long(id_part))
                    elif (file_name.endswith(self.PYRAMID_LOCK)
                            or file_name.endswith(self.PYRAMID_TEMP)):
                        object_id = omero.rtypes.rlong(
                            long(id_part.lstrip('.')))
                    else:
                        object_id = omero.rtypes.rlong(-1)
                except ValueError:
                    object_id = omero.rtypes.rlong(-1)
            object_ids.append(object_id)

        parameters = omero.sys.Parameters()
        parameters.map = {'ids': omero.rtypes.rlist(object_ids)}
        rows = self.query_service.projection(
            "select o.id from %s as o where o.id in (:ids)" % self.object_type,
            parameters, {"omero.group": "-1"})
        existing_ids = [cols[0].val for cols in rows]

        for i, object_id in enumerate(object_ids):
            path = self.deferred_paths[i]
            if object_id.val not in existing_ids:
                if object_id.val == -1:
                    if self.dry_run:
                        print "   \_ %s (ignored/keep)" % path
                else:
                    size = os.stat(path)[ST_SIZE]
                    self.cleansed.append(path)
                    self.bytes_cleansed = size
                    if self.dry_run:
                        print "   \_ %s (remove)" % path
                    else:
                        try:
                            os.unlink(path)
                        except OSError, e:
                            print e
            elif self.dry_run:
                print "   \_ %s (keep)" % path
        self.deferred_paths = list()

    def finalize(self):
        """
        Takes the final set of deferred paths and performs a reconciliation
        check against OMERO for them. This method's purpose is basically to
        catch the final set of paths in the deferred path list and/or perform
        any cleanup.
        """
        self.do_cleanse()

    def __str__(self):
        return "Cleansing context: %d files (%d bytes)" % \
            (len(self.cleansed), self.bytes_cleansed)


def initial_check(config_service, admin_service=None):
    if admin_service is None:
        raise Exception("No admin service provided!")

    ctx = admin_service.getEventContext()
    if not ctx.isAdmin:
        raise Exception('SecurityViolation: Admins only!')

    #
    # Compare server versions. See ticket #3123
    #
    if config_service is None:
        print("No config service provided! "
              "Waiting 10 seconds to allow cancellation")
        from threading import Event
        Event().wait(10)

    server_version = config_service.getVersion()
    server_tuple = tuple([int(x) for x in server_version.split(".")])
    if server_tuple < (4, 2, 1):
        print "Server version is too old! (%s) Aborting..." % server_version
        sys.exit(3)


def cleanse(data_dir, client, dry_run=False):
    client.getImplicitContext().put(omero.constants.GROUP, '-1')

    admin_service = client.sf.getAdminService()
    query_service = client.sf.getQueryService()
    config_service = client.sf.getConfigService()

    initial_check(config_service, admin_service)

    try:
        cleanser = ""
        for directory in SEARCH_DIRECTORIES:
            full_path = os.path.join(data_dir, directory)
            if not os.path.exists(full_path):
                print "%s does not exist. Skipping..." % full_path
                continue
            if dry_run:
                print "Reconciling OMERO data directory...\n %s" % full_path
            object_type = SEARCH_DIRECTORIES[directory]
            cleanser = Cleanser(query_service, object_type)
            cleanser.dry_run = dry_run
            cleanser.cleanse(full_path)
            cleanser.finalize()
    finally:
        if dry_run:
            print cleanser

    # delete empty directories from the managed repositories
    proxy, description = client.getManagedRepository(description=True)
    if proxy:
        root = description.path.val + description.name.val
        print "Removing empty directories from...\n %s" % root
        delete_empty_dirs(proxy, root, client, dry_run)


def delete_empty_dirs(repo, root, client, dry_run):
    # empty subdirectories are to be appended to this list
    to_delete = []

    # find the empty subdirectories
    is_empty_dir(repo, '/', False, to_delete)

    if dry_run:
        for directory in to_delete:
            print "   \_ %s%s (remove)" % (root, directory)
    elif to_delete:
        # probably less than a screenful
        batch_size = 20

        for from_index in range(0, len(to_delete), batch_size):
            batch_to_delete = to_delete[from_index:from_index + batch_size]
            for directory in batch_to_delete:
                print "Removing %s%s" % (root, directory)
            handle = repo.deletePaths(batch_to_delete, True, False)
            try:
                client.waitOnCmd(handle, closehandle=True)
            except omero.CmdError, ce:
                if isinstance(ce.err, omero.cmd.GraphException):
                    raise Exception("failed delete: " + ce.err.message)
                else:
                    raise Exception("failed: " + ce.err.name)


def is_empty_dir(repo, directory, may_delete_dir, to_delete):
    empty_subdirs = []
    is_empty = True

    for entry in repo.listFiles(directory):
        subdirectory = directory + entry.name.val + '/'
        may_delete_subdir = entry.details.permissions.canDelete()
        if entry.mimetype is not None and \
           entry.mimetype.val == 'Directory' and \
           is_empty_dir(repo, subdirectory, may_delete_subdir, empty_subdirs):
            if may_delete_subdir:
                # note empty subdirectories that can be deleted
                empty_subdirs.append(subdirectory)
        else:
            is_empty = False

    if not (may_delete_dir and is_empty):
        # cannot delete this directory, so note empty subdirectories
        to_delete.extend(empty_subdirs)

    return is_empty


def fixpyramids(data_dir, query_service,
                dry_run=False, config_service=None, admin_service=None):
    initial_check(config_service, admin_service)

    # look for any pyramid files with length 0
    # if there is no matching .*.tmp or .*.pyr_lock file, then
    # the pyramid file will be removed

    pixels_dir = os.path.join(data_dir, "Pixels")
    for root, dirs, files in os.walk(pixels_dir):
        for f in files:
            pixels_file = os.path.join(root, f)
            length = os.path.getsize(pixels_file)
            if length == 0 and f.endswith("_pyramid"):
                delete_pyramid = True
                for lockfile in os.listdir(pixels_dir):
                    if lockfile.startswith("." + f) and \
                        (lockfile.endswith(".tmp") or
                            lockfile.endswith(".pyr_lock")):
                        delete_pyramid = False
                        break

                if delete_pyramid:
                    if dry_run:
                        print "Would remove %s" % f
                    else:
                        print "Removing %s" % f
                        os.remove(pixels_file)


def removepyramids(client, little_endian=False, dry_run=False,
                   imported_after=None):
    client.getImplicitContext().put(omero.constants.GROUP, '-1')
    admin_service = client.sf.getAdminService()
    config_service = client.sf.getConfigService()

    initial_check(config_service, admin_service)

    # look for any pyramid files with the specified endianness
    # the pyramid file will be removed
    to_delete = []
    request = omero.cmd.FindPyramids()
    request.littleEndian = little_endian
    request.importeAfter = -1
    if imported_after is not None:
        date = time.strptime(imported_after, "%d/%m/%Y")
        request.importeAfter = time.mktime(date)
    rsp = submit(client, request)

    if len(rsp.pyramidFiles) == 0:
        print "No pyramids to remove"

    # Prepare the requests
    for j in range(len(rsp.pyramidFiles)):
        image_id = rsp.pyramidFiles[j]
        if dry_run:
            print "Would remove pyramid for image %s" % image_id
        else:
            print "Removing pyramid for image %s" % image_id
            req = omero.cmd.ManageImageBinaries()
            req.imageId = image_id
            req.deletePyramid = True
            req.deleteThumbnails = True
            to_delete.append(req)

    if len(to_delete) > 0:
        submit(client, to_delete)
        print "%s Pyramids removed" % len(to_delete)


def submit(client, request):
    if isinstance(request, list):
        request = DoAll(request)
    handle = client.sf.submit(request)
    cb = CmdCallbackI(client, handle)
    try:
        cb.loop(50, 1000)
        rsp = cb.getResponse()
        if isinstance(rsp, omero.cmd.ERR):
            raise Exception(rsp)
        return rsp
    finally:
        cb.close(True)


def main():
    """
    Default main() that performs OMERO data directory cleansing.
    """
    try:
        options, args = getopt(sys.argv[1:], "u:k:", ["dry-run"])
    except GetoptError, (msg, opt):
        usage(msg)

    try:
        data_dir, = args
    except:
        usage('Expecting single OMERO data directory!')

    username = get_user("root")
    session_key = None
    dry_run = False
    for option, argument in options:
        if option == "-u":
            username = argument
        if option == "-k":
            session_key = argument
        if option == "--dry-run":
            dry_run = True

    if session_key is None:
        print "Username: %s" % username
        try:
            password = getpass.getpass()
        except KeyboardInterrupt:
            sys.exit(2)

    try:
        client = omero.client('localhost')
        client.setAgent("OMERO.cleanse")
        if session_key is None:
            client.createSession(username, password)
        else:
            client.createSession(session_key)
    except PermissionDeniedException:
        print "%s: Permission denied" % sys.argv[0]
        print "Sorry."
        sys.exit(1)

    try:
        cleanse(data_dir, client, dry_run)
    finally:
        if session_key is None:
            client.closeSession()

if __name__ == '__main__':
    main()
