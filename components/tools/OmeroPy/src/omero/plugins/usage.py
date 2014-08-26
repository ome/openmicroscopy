#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright 2014 University of Dundee. All rights reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
    Plugin for reporting disk usage

"""

from omero.cli import CLI, CmdControl

import sys

HELP = """Report disk usage

"""


class UsageControl(CmdControl):

    def cmd_type(self):
        import omero
        import omero.all
        return omero.cmd.DiskUsage

    def _configure(self, parser):
        super(UsageControl, self)._configure(parser)
        parser.add_style_argument()
        parser.add_argument(
            "--size_only", action="store_true",
            help="Print total bytes used in bytes")
        parser.add_argument(
            "--report", action="store_true",
            help="Print detailed breakdown of disk usage")
        parser.add_argument(
            "--units", choices="KMGTP",
            help="Units to use for disk usage")
        parser.add_argument(
            "obj", nargs="+",
            help="Objects to be queried in the form '<Class>:<Id>[,<Id> ...]'")

    def main_method(self, args):
        client = self.ctx.conn(args)
        req = self.cmd_type()()  # we need an instance here rather than a type.
        self._process_request(req, args, client)

    def parseObj(self, obj):
        """
        Take the positional arguments and marshal them into
        a dictionary for the command argument.
        """
        objects = {}
        for o in obj:
            try:
                parts = o.split(":", 1)
                assert len(parts) == 2
                type = parts[0]
                ids = parts[1].split(",")
                ids = map(long, ids)
                objects[type] = ids
            except:
                raise ValueError("Bad object: ", o)

        return objects

    def toUnits(self, size, units):
        """
        Convert from bytes to KiB, MiB, GiB, TiB or PiB.
        """
        oneK = 1024.0
        powers = {'K': 1, 'M': 2, 'G': 3, 'T': 4, 'P': 5}
        if units in powers.keys():
            return round(size/oneK**powers[units], 2)
        else:
            raise ValueError("Unrecognized units: ", units)

    def _process_request(self, req, args, client):
        """
        Parse the positional arguments then process the request.
        """
        req.objects = self.parseObj(args.obj)
        cb = None
        try:
            rsp, status, cb = self.response(client, req, wait=args.wait)
            self.print_report(req, rsp, status, args)
        finally:
            if cb is not None:
                cb.close(True)  # Close handle

    def print_report(self, req, rsp, status, args):
        """
        Output the total bytes used or the error,
        optionally provide more details.
        """
        err = self.get_error(rsp)
        if err:
            self.ctx.err(err)
        else:
            if args.size_only:
                self.ctx.out(rsp.totalBytesUsed)
            elif args.units:
                size = self.toUnits(rsp.totalBytesUsed, args.units)
                files = rsp.totalFileCount
                self.ctx.out(
                    "Total disk usage: %d %siB in %d files"
                    % (size, args.units, files))
            else:
                self.ctx.out(
                    "Total disk usage: %d bytes in %d files"
                    % (rsp.totalBytesUsed, rsp.totalFileCount))

        if args.report and not args.size_only:
            self.print_detailed_report(req, rsp, status, args)

    def print_detailed_report(self, req, rsp, status, args):
        """
        Print a breakdown of disk usage in table form.
        """
        from omero.util.text import TableBuilder
        tb = TableBuilder("component", "size (bytes)", "files")
        if args.style:
            tb.set_style(args.style)

        for (element, size) in rsp.bytesUsedByReferer.items():
            row = [element, size, rsp.fileCountByReferer[element]]
            tb.row(*tuple(row))
        self.ctx.out(str(tb.build()))

try:
    register("usage", UsageControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("usage", UsageControl, HELP)
        cli.invoke(sys.argv[1:])
