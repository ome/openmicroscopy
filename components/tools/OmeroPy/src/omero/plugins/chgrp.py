#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
   chgrp plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.
"""

from omero.cli import BaseControl, CLI
import sys

HELP="""Move data between groups

Example Usage:

  omero chgrp 101 /Image:1                     # Move all of Image 1 to group 101
  omero chgrp Group:101 /Image:1               # Move all of Image 1 to group 101
  omero chgrp ExperimenterGroup:101 /Image:1   # Move all of Image 1 to group 101
  omero chgrp "My Lab" /Image:1                # Move all of Image 1 to group "myLab"

  omero chgrp --edit 101 /Image:1              # Open an editor with all the chgrp
                                               # options filled out with defaults.

  omero chgrp --opt /Image:KEEP /Plate:1       # Calls chgrp on Plate, leaving all
                                               # images in the previous group.

  What data is moved is the same as that which would be deleted by a similar
  call to "omero delete /Image:1"

"""

class ExperimenterGroup(object):

    def __init__(self, arg):
        self.orig = arg
        self.grp = None
        try:
            self.grp = long(arg)
        except ValueError, ve:
            if ":" in arg:
                parts = arg.split(":", 1)
                if parts[0] == "Group" or "ExperimenterGroup":
                    try:
                        self.grp = long(parts[1])
                    except ValueError, ve:
                        pass

    def lookup(self, client):
        if self.grp is None:
            import omero
            a = client.sf.getAdminService()
            try:
                self.grp = a.lookupGroup(self.orig).id.val
            except omero.ApiUsageException, aue:
                pass
        return self.grp


class ChgrpArg(object):

    def __call__(self, arg):
        parts = arg.split(":", 1)
        assert len(parts) == 2
        type = parts[0]
        id = long(parts[1])

        import omero
        import omero.cmd
        return omero.cmd.Chgrp(\
                type=type,\
                id=id,\
                options={})


class ChgrpControl(BaseControl):

    def _configure(self, parser):
        parser.set_defaults(func=self.chgrp)
        parser.add_argument("--wait", type=long, help="Number of seconds to"+\
                " wait for the chgrp to complete (Indefinite < 0; No wait=0).", default=-1)
        parser.add_argument("--edit", action="store_true", help="""Configure options in a text editor""")
        parser.add_argument("--opt", action="append", help="""Modifies the given option (e.g. /Image:KEEP). Applied *after* 'edit' """)
        parser.add_argument("--list", action="store_true", help="""Print a list of all available chgrp specs""")
        parser.add_argument("--list-details", action="store_true",
                help="""Print a list of all available chgrp specs along with detailed info""")
        parser.add_argument("--report", action="store_true", help="""Print more detailed report of each chgrp""")
        parser.add_argument("grp", type=ExperimenterGroup, help="""Group to move objects to""")
        parser.add_argument("obj", nargs="*", type=ChgrpArg(), help="""Objects to be chgrp'd in the form "<Class>:<Id>""")

    def chgrp(self, args):

        import omero

        client = self.ctx.conn(args)
        cb = None
        try:
            speclist, status, cb = self.response(client, omero.cmd.GraphSpecList())
        finally:
            if cb is not None:
                cb.close()
        specs = speclist.list
        specmap = dict()
        for s in specs:
            specmap[s.type] = s
        keys = sorted(specmap)

        if args.list_details:
            for key in keys:
                spec = specmap[key]
                self.ctx.out("=== %s ===" % key)
                for k, v in spec.options.items():
                    self.ctx.out("%s" % (k,))
            return # Early exit.
        elif args.list:
            self.ctx.out("\n".join(keys))
            return # Early exit.

        for obj in args.obj:
            if args.edit:
                obj.options = self.edit_options(obj, specmap)
            if args.opt:
                for opt in args.opt:
                    self.line_to_opts(opt, obj.options)

            print obj.options
            obj.grp = args.grp.lookup(client)
            if obj.grp is None:
                self.ctx.die(196, "Failed to find group: %s" % args.grp.orig)

            cb = None
            try:
                print args.wait
                rsp, status, cb = self.response(client, obj, wait = args.wait)
                print rsp
                print status
            finally:
                if cb is not None:
                    cb.close()


    def edit_options(self, req, specmap):

        from omero.util import edit_path
        from omero.util.temp_files import create_path, remove_path

        start_text = """# Edit options for your operation below.\n"""
        start_text += ("# === %s ===\n" % req.type)
        if req.type not in specmap:
            self.ctx.die(162, "Unknown type: %s" % req.type)
        start_text += self.append_options(req.type, dict(specmap))

        temp_file = create_path()
        try:
            edit_path(temp_file, start_text)
            txt = temp_file.text()
            print txt
            rv = dict()
            for line in txt.split("\n"):
                self.line_to_opts(line, rv)
            return rv
        except RuntimeError, re:
            self.ctx.die(954, "%s: Failed to edit %s" % (getattr(re, "pid", "Unknown"), temp_file))

    def append_options(self, key, specmap, indent = 0):
        spec = specmap.pop(key)
        start_text = ""
        for optkey in sorted(spec.options):
            optval = spec.options[optkey]
            start_text += ("%s%s=%s\n" % ("  " * indent, optkey, optval))
            if optkey in specmap:
                start_text += self.append_options(optkey, specmap, indent+1)
        return start_text

    def line_to_opts(self, line, opts):
        if not line or line.startswith("#"):
            return
        parts = line.split("=", 1)
        if len(parts) == 1:
            parts.append("")
        opts[parts[0].strip()] = parts[1].strip()

    def response(self, client, req, loops = 8, ms = 500, wait = None):
        import omero.callbacks
        handle = client.sf.submit(req)
        cb = omero.callbacks.CmdCallbackI(client, handle)

        rsp = None
        if wait is None:
            rsp = cb.loop(loops, ms)
        elif wait == 0:
            self.ctx.out("Exiting immediately")
        elif wait > 0:
            ms = wait * 1000
            ms = ms / loops
            self.ctx.out("Waiting %s loops of %s ms" % (ms, loops))
            rsp = cb.loop(loops, ms)
        else:
            try:
                # Wait for finish
                while True:
                    rsp = cb.block(ms)
                    if rsp is not None:
                        break

            # If user uses Ctrl-C, then cancel
            except KeyboardInterrupt:
                self.ctx.out("Attempting cancel...")
                if handle.cancel():
                    self.ctx.out("Cancelled")
                else:
                    self.ctx.out("Failed to cancel")

        return rsp, handle.getStatus(), cb

try:
    register("chgrp", ChgrpControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("chgrp", ChgrpControl, HELP)
        cli.invoke(sys.argv[1:])
