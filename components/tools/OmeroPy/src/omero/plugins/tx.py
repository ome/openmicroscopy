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


import re
import sys
import shlex
import fileinput

from omero.cli import BaseControl, CLI, ExceptionHandler


class TxArg(object):

    VAR_PATTERN = "[a-zA-Z][a-zA-Z0-9]*"
    ARG_RE = re.compile(("(?P<FIELD>[a-zA-Z]+)"
                         "(?P<OPER>[@])?="
                         "(?P<VALUE>.*)"))

    def __init__(self, ctx, arg):
        self.ctx = ctx
        self.arg = arg
        self.parse_arg(ctx, arg)

    def parse_arg(self, ctx, arg):
        m = self.ARG_RE.match(arg)
        if not m:
            raise Exception("Unparseable argument: %s" % arg)
        self.argname = m.group("FIELD")
        capitalized = self.argname[0].upper() + self.argname[1:]
        self.setter = "set%s" % capitalized
        self.value = m.group("VALUE")
        self.oper = m.group("OPER")
        if self.oper == "@":
            # Treat value like an array lookup
            if re.match('\d+$', self.value):
                self.value = ctx.get("tx.out")[int(self.value)]
            elif re.match(self.VAR_PATTERN + '$', self.value):
                self.value = ctx.get("tx.vars")[self.value]
            else:
                raise Exception("Invalid reference: %s" % self.value)

    def call_setter(self, obj):
        getattr(obj, self.setter)(self.value, wrap=True)


class TxAction(object):
    """
    Parsed transaction operation provided by the user.
    In the current implementation these actions are
    handled client-side, but eventually a list of them
    should be provided to the server for processing in
    a single transaction.
    """

    def __init__(self, tx_state, arg_list, dest):
        self.order = tx_state.next()
        self.arg_list = arg_list
        self.dest = dest

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

    def check_requirements(self, ctx, obj, completed):
        missing = []
        total = dict(obj._field_info._asdict())
        for arg in completed:
            del total[arg.argname]
        for remaining, info in total.items():
            if info.nullable is False:
                missing.append(remaining)

        if missing:
            ctx.die(103, "required arguments: %s" %
                    ", ".join(missing))

    def go(self, ctx, args):
        c = ctx.conn(args)
        up = c.sf.getUpdateService()
        obj = self.instance(ctx)
        kls = obj.__class__.__name__
        if kls.endswith("I"):
            kls = kls[0:-1]

        completed = []
        for arg in self.arg_list[1:]:
            arg = TxArg(ctx, arg)
            try:
                arg.call_setter(obj)
                completed.append(arg)
            except AttributeError:
                ctx.die(500, "No field %s for %s" % (arg.argname, kls))

        self.check_requirements(ctx, obj, completed)
        out = up.saveAndReturnObject(obj)
        proxy = "%s:%s" % (kls, out.id.val)
        ctx.out("Created %s" % proxy)
        ctx.get("tx.out").append(proxy)
        if self.dest:
            ctx.get("tx.vars")[self.dest] = proxy


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
new ProjectDatasetLink parent@=0 child@=1

comment1: new CommentAnnotation textValue=baz
new DatasetAnnotationLink parent@=1 child@=comment1
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
        self.ctx.set("tx.vars", {})
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
        m = re.match("(?P<VARNAME>%s):$" % TxArg.VAR_PATTERN, arg_list[0])
        if m:
            m = m.group('VARNAME')
            arg_list = arg_list[1:]
        if arg_list[0] == "new":
            return NewObjectTxAction(tx_state, arg_list[1:], m)
        else:
            raise self.ctx.die(100, "Unknown command: " + arg_list[0])


try:
    register("tx", TxControl, TxControl.__doc__)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("tx", TxControl, TxControl.__doc__)
        cli.invoke(sys.argv[1:])
