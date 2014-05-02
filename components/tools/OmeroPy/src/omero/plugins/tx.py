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


class TxField(object):

    ARG_RE = re.compile(("(?P<FIELD>[a-zA-Z]+)"
                         "(?P<OPER>[@])?="
                         "(?P<VALUE>.*)"))

    def __init__(self, tx_state, arg):
        self.tx_state = tx_state
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
                self.value = tx_state.get_row(int(self.value))
            elif re.match(TxCmd.VAR_NAME + '$', self.value):
                self.value = tx_state.get_var(self.value)
            else:
                raise Exception("Invalid reference: %s" % self.value)

    def __call__(self, obj):
        return getattr(obj, self.setter)(self.value, wrap=True)


class TxCmd(object):

    VAR_NAME = "(?P<DEST>[a-zA-Z][a-zA-Z0-9]*)"
    VAR_RE = re.compile(("^\s*%s"
                         "\s*=\s"
                         "(?P<REST>.*)$") % VAR_NAME)

    def __init__(self, tx_state, arg_list=None, line=None):
        """
        Command of the form:

            (var = ) action type ( field(@)=value ... )

        where parentheses denote optional values.
        """
        self.tx_state = tx_state
        self.arg_list = arg_list
        self.orig_line = line
        self.dest = None
        self.action = None
        self.type = None
        self.fields = []
        self._parse_early()

    def _parse_early(self):
        line = self.orig_line
        if self.arg_list and line:
            raise Exception("Both arg_list and line specified")
        elif not self.arg_list and not line:
            raise Exception("Neither arg_list nor line specified")
        elif line:
            m = re.match(self.VAR_RE, line)
            if m:
                self.dest = m.group("DEST")
                line = m.group("REST")
            self.arg_list = shlex.split(line)
        self.action = self.arg_list[0]
        if len(self.arg_list) > 1:
            self.type = self.arg_list[1]

    def _parse_late(self):
        if len(self.arg_list) > 2:
            for arg in self.arg_list[2:]:
                self.fields.append(TxField(self.tx_state, arg))

    def setters(self):
        self._parse_late()
        for field in self.fields:
            yield (field.argname, field)

    def __str__(self):
        return " ".join(self.arg_list)


class TxAction(object):
    """
    Parsed transaction operation provided by the user.
    In the current implementation these actions are
    handled client-side, but eventually a list of them
    should be provided to the server for processing in
    a single transaction.
    """

    def __init__(self, tx_state, tx_cmd):
        self.tx_state = tx_state
        self.tx_cmd = tx_cmd

    def go(self, ctx, args):
        raise Exception("Unimplemented")


class NewObjectTxAction(TxAction):

    def class_name(self):
        kls = self.tx_cmd.type
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
            del total[arg]
        for remaining, info in total.items():
            if info.nullable is False:
                missing.append(remaining)

        if missing:
            ctx.die(103, "required arguments: %s" %
                    ", ".join(missing))

    def go(self, ctx, args):
        self.tx_state.add(self)
        c = ctx.conn(args)
        up = c.sf.getUpdateService()
        obj = self.instance(ctx)
        kls = obj.__class__.__name__
        if kls.endswith("I"):
            kls = kls[0:-1]

        completed = []
        for field, setter in self.tx_cmd.setters():
            setter(obj)
            completed.append(field)

        self.check_requirements(ctx, obj, completed)
        out = up.saveAndReturnObject(obj)
        proxy = "%s:%s" % (kls, out.id.val)
        self.tx_state.set_value(proxy, dest=self.tx_cmd.dest)


class TxState(object):

    def __init__(self, ctx):
        self.ctx = ctx
        self._commands = []
        self._vars = {}
        self.is_stdin = sys.stdin.isatty()

    def add(self, command):
        self._commands.append([command, None])
        return len(self._commands)

    def set_value(self, proxy, dest=None):
        idx = len(self._commands) - 1
        self.ctx.out("Created #%s %s" % (idx, proxy))
        self._commands[idx][1] = proxy
        if dest:
            self._vars[dest] = proxy

    def get_row(self, i):
        return self._commands[i][1]

    def get_var(self, key):
        return self._vars[key]

    def __len__(self):
        return len(self._commands)


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
        state = TxState(self.ctx)
        self.ctx.set("tx.state", state)
        actions = []
        if len(args.item) == 0:
            path = "-"
            if args.file:
                path = args.file
            for line in fileinput.input([path]):
                line = line.strip()
                if line and not line.startswith("#"):
                    actions.append(self.parse(state, line=line))
        else:
            if args.file:
                self.ctx.err("Ignoring %s" % args.file)
            actions.append(self.parse(state, arg_list=args.item))

        for action in actions:
            action.go(self.ctx, args)
        return actions

    def parse(self, tx_state, arg_list=None, line=None):
        """
        Takes a single command list and turns
        it into a TxAction object
        """
        tx_cmd = TxCmd(tx_state, arg_list=arg_list, line=line)
        if tx_cmd.action == "new":
            return NewObjectTxAction(tx_state, tx_cmd)
        else:
            raise self.ctx.die(100, "Unknown command: %s" % tx_cmd)


try:
    register("tx", TxControl, TxControl.__doc__)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("tx", TxControl, TxControl.__doc__)
        cli.invoke(sys.argv[1:])
