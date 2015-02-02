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

from omero_ext.argparse import FileType
from omero.cli import CLI, ExceptionHandler
from omero.rtypes import rlong
from omero.plugins.prefs import ConfigControl, with_config


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


class ObjControl(ConfigControl):
    """Create and Update OMERO objects

The obj command allows inserting any objects into the OMERO
database as well as updating existing ones. This is likely
useful for preparing datasets for import and similar.

Examples:

    $ bin/omero obj new Dataset name=foo
    Dataset:123

    $ bin/omero obj update Dataset:123 description=bar
    Dataset:123

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
        sub = parser.sub()

        new = parser.add(sub, self.new, "Create a new OMERO object")

        update = parser.add(
            sub, self.update, "Update an existing OMERO object")

        load = parser.add(
            sub, self.load,
            "Load a series of commands from a file or the stdin")
        load.add_argument("file", nargs="*", type=FileType("r"),
                          default=[sys.stdin])

        graphreport = parser.add(
            sub, self.graphreport, "Generate")
        graphreport.add_argument(
            "--source", help="Configuration file to be used. Default:"
            " etc/grid/config.xml")
        graphreport.add_argument(
            "file", help="Output file")

        for x in [new, update]:
            x.add_argument(
                "Class", nargs="?",
                help="OMERO model object name, e.g. Project")
            x.add_argument(
                "fields", nargs="*",
                help="fields to be set, e.g. name=foo")

    def new(self, args):
        self.process('new', args)

    def update(self, args):
        self.process('update', args)

    def load(self, args):
        self.process('load', args)

    def process(self, command, args):
        state = TxState(self.ctx)
        self.ctx.set("tx.state", state)
        actions = []
        if command == 'load':
            for f in args.file:
                for line in f:
                    line = line.strip()
                    if line and not line.startswith("#"):
                        actions.append(self.parse(state, line=line))
        else:
            actions.append(
                self.parse(state,
                           arg_list=[command, args.Class] +
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
        if tx_cmd.action == "update":
            return UpdateObjectTxAction(tx_state, tx_cmd)
        else:
            raise self.ctx.die(100, "Unknown command: %s" % tx_cmd)

    @with_config
    def graphreport(self, args, config):

        import os
        import omero.java
        server_dir = self.ctx.dir / "lib" / "server"
        classpath = [file.abspath() for file in server_dir.files("*.jar")]
        xargs = ["-cp", os.pathsep.join(classpath)]

        cfg = config.as_map()
        config.close()  # Early close. See #9800
        self.set_db_arguments(cfg, xargs)

        cmd = ["ome.services.graphs.GraphPathReport", args.file]

        debug = False
        self.ctx.dbg(
            "Launching Java: %s, debug=%s, xargs=%s" % (cmd, debug, xargs))
        p = omero.java.run(cmd, use_exec=True, debug=debug, xargs=xargs,
                           stdout=sys.stdout, stderr=sys.stderr)
        self.ctx.rv = p.wait()


try:
    register("obj", ObjControl, ObjControl.__doc__)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("obj", ObjControl, ObjControl.__doc__)
        cli.invoke(sys.argv[1:])
