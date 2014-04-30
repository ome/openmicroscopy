#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013 Glencoe Software, Inc. All Rights Reserved.
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
Plugin for performing OMERO transactional changes (DML)
from a simple script. This macro like language is intended
to be used simply from the command-line as well as in scripts
and tests.
"""


import sys
import shlex
import fileinput

from omero.cli import BaseControl, CLI, ExceptionHandler


class TxAction(object):
    """
    Parsed transaction operation provided by the user.
    In the current implementation these actions are
    handled client-side, but eventually a list of them
    should be provided to the server for processing in
    a single transaction.
    """

    def __init__(self, tx_state, arg_list):
        self.order = tx_state.next()
        self.arg_list = arg_list

    def go(self, ctx, args):
        raise Exception("Unimplemented")


class NewObjectTxAction(TxAction):

    def class_name(self):
        kls = self.arg_list[0]
        if not kls.endswith("I"):
            kls = "%sI" % kls
        return kls

    def instance(self, ctx):
        import omero
        import omero.all
        try:
            kls = getattr(omero.model, self.class_name())
            return kls()
        except AttributeError:
            ctx.die(102, "No class named '%s'" % self.class_name())

    def go(self, ctx, args):
        c = ctx.conn(args)
        up = c.sf.getUpdateService()
        obj = self.instance(ctx)
        kls = obj.__class__.__name__
        if kls.endswith("I"):
            kls = kls[0:-1]
        for arg in self.arg_list[1:]:
            parts = arg.split("=", 1)
            argname = parts[0]
            capitalized = argname[0].upper() + argname[1:]
            setter = "set%s" % capitalized
            value = parts[1]
            try:
                getattr(obj, setter)(value, wrap=True)
            except AttributeError:
                ctx.die(500, "No field %s for %s" % (argname, kls))
        out = up.saveAndReturnObject(obj)
        ctx.out("Created %s:%s" % (kls, out.id.val))
        ctx.get("tx.out").append(out.proxy())


class TxState(object):

    def __init__(self):
        self.count = 0
        self.is_stdin = sys.stdin.isatty()

    def next(self):
        self.count += 1
        return self.count


class TxControl(BaseControl):
    """Object manipulation tool

Examples:

omero tx new Dataset name=foo

omero tx << EOF
new Project name=bar
new Dataset name=foo
EOF
    """

    def _configure(self, parser):

        self.exc = ExceptionHandler()
        parser.add_login_arguments()
        parser.add_argument("-f", "--file")
        parser.add_argument(
            "item",
            nargs="*",
        )
        parser.set_defaults(func=self.process)

    def process(self, args):
        self.ctx.set("tx.out", [])
        state = TxState()
        actions = []
        if len(args.item) == 0:
            path = "-"
            if args.file:
                path = args.file
            for line in fileinput.input([path]):
                line = line.strip()
                if line and not line.startswith("#"):
                    actions.append(self.parse(state, shlex.split(line)))
        else:
            if args.file:
                self.ctx.err("Ignoring %s" % args.file)
            actions.append(self.parse(state, args.item))

        for action in actions:
            action.go(self.ctx, args)
        return actions

    def parse(self, tx_state, arg_list):
        """
        Takes a single command list and turns
        it into a TxAction object
        """
        if arg_list[0] == "new":
            return NewObjectTxAction(tx_state, arg_list[1:])
        else:
            raise self.ctx.die(100, "Unknown command: " + arg_list[0])


try:
    register("tx", TxControl, TxControl.__doc__)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("tx", TxControl, TxControl.__doc__)
        cli.invoke(sys.argv[1:])
