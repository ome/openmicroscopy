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
Plugin for performing OMERO transactional changes (DML)
from a simple script. This macro like language is intended
to be used simply from the command-line as well as in scripts
and tests.
"""


import re
import sys
import shlex
import fileinput

from omero_ext.argparse import SUPPRESS
from omero.cli import BaseControl, CLI, ExceptionHandler
from omero.model import NamedValue as NV
from omero.rtypes import rlong


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

            (var = ) action type(:id) ( field(@)=value ... )

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
    Parsed operation provided by the user chosen
    based on the first non-variable field of the
    command. Implementations can choose how they
    will handle the fields parsed by TxCmd.
    """

    def __init__(self, tx_state, tx_cmd):
        self.tx_state = tx_state
        self.tx_cmd = tx_cmd

    def go(self, ctx, args):
        raise Exception("Unimplemented")

    def class_name(self):
        kls = self.tx_cmd.type.split(":")[0]
        if not kls.endswith("I"):
            kls = "%sI" % kls
        return kls

    def obj_id(self):
        parts = self.tx_cmd.type.split(":")
        try:
            return long(parts[1])
        except:
            return None

    def instance(self, ctx):
        import omero
        import omero.all
        try:
            kls = getattr(omero.model, self.class_name())
            obj = kls()
            oid = self.obj_id()
            if oid is not None:
                obj.setId(rlong(oid))
                obj.unload()
            kls = kls.__name__
            if kls.endswith("I"):
                kls = kls[0:-1]
            return obj, kls
        except AttributeError:
            ctx.die(102, "No class named '%s'" % self.class_name())


class NewObjectTxAction(TxAction):

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
        import omero
        self.tx_state.add(self)
        c = ctx.conn(args)
        up = c.sf.getUpdateService()
        obj, kls = self.instance(ctx)

        completed = []
        for field, setter in self.tx_cmd.setters():
            try:
                setter(obj)
                completed.append(field)
            except omero.ClientError, ce:
                ctx.die(333, "%s" % ce)

        self.check_requirements(ctx, obj, completed)
        try:
            out = up.saveAndReturnObject(obj)
        except omero.ServerError, se:
            ctx.die(336, "Failed to create %s - %s" %
                    (kls, se.message))
        proxy = "%s:%s" % (kls, out.id.val)
        self.tx_state.set_value(proxy, dest=self.tx_cmd.dest)


class UpdateObjectTxAction(TxAction):

    def go(self, ctx, args):
        import omero
        self.tx_state.add(self)
        c = ctx.conn(args)
        q = c.sf.getQueryService()
        up = c.sf.getUpdateService()
        obj, kls = self.instance(ctx)
        if obj.id is None:
            ctx.die(334, "No id given for %s. Use e.g. '%s:123'"
                    % (kls, kls))
        try:
            obj = q.get(kls, obj.id.val, {"omero.group": "-1"})
        except omero.ServerError:
            ctx.die(334, "No object found: %s:%s" %
                    (kls, obj.id.val))

        for field, setter in self.tx_cmd.setters():
            try:
                setter(obj)
            except omero.ClientError, ce:
                ctx.die(335, "%s" % ce)

        try:
            out = up.saveAndReturnObject(obj)
        except omero.ServerError, se:
            ctx.die(336, "Failed to update %s:%s - %s" %
                    (kls, obj.id.val, se.message))
        proxy = "%s:%s" % (kls, out.id.val)
        self.tx_state.set_value(proxy, dest=self.tx_cmd.dest)


class NonFieldTxAction(TxAction):
    """
    Base class for use with command actions which
    don't take the standard a=b c=d fields.
    """

    def go(self, ctx, args):
        import omero
        self.tx_state.add(self)
        self.client = ctx.conn(args)
        self.query  = self.client.sf.getQueryService()
        self.update = self.client.sf.getUpdateService()
        self.obj, self.kls = self.instance(ctx)
        if self.obj.id is None:
            ctx.die(334, "No id given for %s. Use e.g. '%s:123'"
                    % (self.kls, self.kls))
        try:
            self.obj = self.query.get(
                self.kls, self.obj.id.val, {"omero.group": "-1"})
        except omero.ServerError:
            ctx.die(334, "No object found: %s:%s" %
                    (self.kls, self.obj.id.val))

        self.on_go(ctx, args)

    def save_and_return(self):
        import omero
        try:
            out = self.update.saveAndReturnObject(self.obj)
        except omero.ServerError, se:
            ctx.die(336, "Failed to update %s:%s - %s" %
                    (self.kls, self.obj.id.val, se.message))
        proxy = "%s:%s" % (self.kls, out.id.val)
        self.tx_state.set_value(proxy, dest=self.tx_cmd.dest)


class MapSetTxAction(NonFieldTxAction):

    def on_go(self, ctx, args):
        import omero

        if len(self.tx_cmd.arg_list) != 5:
            ctx.die(335, "usage: map-set OBJ FIELD KEY VALUE")

        field = self.tx_cmd.arg_list[2]
        current = getattr(self.obj, field)
        if current is None:
            setattr(self.obj, field, [])

        name, value = self.tx_cmd.arg_list[3:]
        state = None
        for nv in current:
            if nv and nv.name == name:
                nv.value = value
                state = "SET"
                break

        if state != "SET":
            current.append(NV(name, value))

        self.save_and_return()


class MapGetTxAction(NonFieldTxAction):

    def on_go(self, ctx, args):

        if len(self.tx_cmd.arg_list) != 4:
            ctx.die(335, "usage: map-get OBJ FIELD KEY")

        field = self.tx_cmd.arg_list[2]
        current = getattr(self.obj, field)
        if current is None:
            setattr(self.obj, field, [])

        name = self.tx_cmd.arg_list[3]
        value = None
        for nv in current:
            if nv and nv.name == name:
                value = nv.value

        self.tx_state.set_value(value, dest=self.tx_cmd.dest)


class NullTxAction(NonFieldTxAction):

    def on_go(self, ctx, args):

        if len(self.tx_cmd.arg_list) != 3:
            ctx.die(335, "usage: null OBJ FIELD")

        field = self.tx_cmd.arg_list[2]
        setattr(self.obj, field, None)
        self.save_and_return()


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
        self.ctx.out("%s" % proxy)
        self._commands[idx][1] = proxy
        if dest:
            self._vars[dest] = proxy

    def get_row(self, i):
        return self._commands[i][1]

    def get_var(self, key):
        return self._vars[key]

    def __len__(self):
        return len(self._commands)


class ObjControl(BaseControl):
    """Create and Update OMERO objects

The obj command allows inserting any objects into the OMERO
database as well as updating existing ones. This is likely
useful for preparing datasets for import and similar.

Examples:

    $ bin/omero obj new Dataset name=foo
    Dataset:123

    $ bin/omero obj update Dataset:123 description=bar
    Dataset:123

    $ bin/omero obj null Dataset:123 description
    Dataset:123

    $ bin/omero obj new MapAnnotation ns=example.com
    MapAnnotation:456
    $ bin/omero obj map-set MapAnnotation mapValue foo bar
    MapAnnotation:456
    $ bin/omero obj map-get MapAnnotation mapValue foo
    bar

Bash examples:

    $ project=$(bin/omero obj new Project name='my Project')
    $ dataset=$(bin/omero obj new Dataset name='my Dataset')
    $ bin/omero obj new ProjectDatasetLink parent=$project child=$dataset
    ProjectDatasetLink:456
    $ bin/omero import -d $dataset ...

    """

    def _configure(self, parser):

        self.exc = ExceptionHandler()
        parser.add_login_arguments()
        parser.add_argument(
            "--file", help=SUPPRESS)
        parser.add_argument(
            "command", nargs="?",
            choices=("new", "update", "null", "map-get",
                     "map-set", "map-add", "map-edit"),
            help="operation to be performed")
        parser.add_argument(
            "Class", nargs="?",
            help="OMERO model object name, e.g. Project")
        parser.add_argument(
            "fields", nargs="*",
            help="fields to be set, e.g. name=foo")
        parser.set_defaults(func=self.process)

    def process(self, args):
        state = TxState(self.ctx)
        self.ctx.set("tx.state", state)
        actions = []
        if not args.command:
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
            actions.append(
                self.parse(state,
                           arg_list=[args.command, args.Class] +
                           args.fields))

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
        elif tx_cmd.action == "update":
            return UpdateObjectTxAction(tx_state, tx_cmd)
        elif tx_cmd.action == "map-set":
            return MapSetTxAction(tx_state, tx_cmd)
        elif tx_cmd.action == "map-get":
            return MapGetTxAction(tx_state, tx_cmd)
        elif tx_cmd.action == "null":
            return NullTxAction(tx_state, tx_cmd)
        else:
            raise self.ctx.die(100, "Unknown command: %s" % tx_cmd)


try:
    register("obj", ObjControl, ObjControl.__doc__)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("obj", ObjControl, ObjControl.__doc__)
        cli.invoke(sys.argv[1:])
