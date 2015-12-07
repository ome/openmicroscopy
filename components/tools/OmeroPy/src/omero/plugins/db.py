#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2008-2013 Glencoe Software, Inc. All Rights Reserved.
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
   Plugin for our managing the OMERO database.

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.
"""

from omero.cli import BaseControl
from omero.cli import CLI

from omero_ext.argparse import FileType, SUPPRESS

from path import path

import omero.java
import time
import sys

HELP = """Database tools for creating scripts, setting passwords, etc."""


class DatabaseControl(BaseControl):

    def _configure(self, parser):
        sub = parser.sub()

        script = sub.add_parser(
            "script", help="Generates a DB creation script")
        script.set_defaults(func=self.script)
        script.add_argument(
            "-f", "--file", type=FileType(mode="w"),
            help="Optional file to save to. Use '-' for stdout.")

        script.add_argument("posversion", nargs="?", help=SUPPRESS)
        script.add_argument("pospatch", nargs="?", help=SUPPRESS)
        script.add_argument("pospassword", nargs="?", help=SUPPRESS)

        script.add_argument("--version", help=SUPPRESS)
        script.add_argument("--patch", help=SUPPRESS)
        script.add_argument("--password", help="OMERO root password")

        pw = sub.add_parser(
            "password",
            help="Prints SQL command for updating your root password")
        pw.add_argument("password", nargs="?")
        pw.set_defaults(func=self.password)
        pw.add_argument("--user-id",
                        help="User ID to salt into the password. "
                        "Defaults to '0', i.e. 'root'",
                        default="0")

        for x in (pw, script):
            x.add_argument(
                "--no-salt", action="store_true",
                help="Disable the salting of passwords")

    def _lookup(self, key, defaults, args):
        """
        Get a value from a flag arg, positional arg, or default properties
        """
        propname = "omero.db." + key
        vdef = defaults.properties.getProperty(propname)
        varg = getattr(args, key)
        vpos = getattr(args, 'pos' + key)
        if varg:
            if vpos:
                self.ctx.die(
                    1, "ERROR: Flag and positional argument given for %s" % key
                    )
            v = varg
        elif vpos:
            v = vpos
        elif vdef:
            v = vdef
        else:
            self.ctx.die(1, "No value found for %s" % propname)
        self.ctx.err("Using %s for %s" % (v, key))
        return v

    def _has_user_id(self, args):
        return args and "user_id" in args and args.user_id is not None

    def _get_password_hash(self, args, root_pass=None, old_prompt=False):

        prompt = " for OMERO "
        if self._has_user_id(args) and not old_prompt:
            prompt += "user %s" % args.user_id
        else:
            prompt += "root user"
        root_pass = self._ask_for_password(prompt, root_pass)

        server_jar = self.ctx.dir / "lib" / "server" / "server.jar"
        cmd = ["ome.security.auth.PasswordUtil", root_pass]
        if not args.no_salt and self._has_user_id(args):
            cmd.append(args.user_id)
        p = omero.java.popen(["-cp", str(server_jar)] + cmd)
        rc = p.wait()
        if rc != 0:
            out, err = p.communicate()
            self.ctx.die(rc, "PasswordUtil failed: %s" % err)
        value = p.communicate()[0]
        if not value or len(value) == 0:
            self.ctx.die(100, "Encoded password is empty")
        return value.strip()

    def _copy(self, input_path, output, func, cfg=None):
            input = open(str(input_path))
            try:
                for s in input.xreadlines():
                        try:
                            if cfg:
                                output.write(func(s) % cfg)
                            else:
                                output.write(func(s))
                        except Exception, e:
                            self.ctx.die(
                                154, "Failed to map line: %s\nError: %s"
                                % (s, e))
            finally:
                input.close()

    def _make_replace(self, root_pass, db_vers, db_patch):
        def replace_method(str_in):
                str_out = str_in.replace("@ROOTPASS@", root_pass)
                str_out = str_out.replace("@DBVERSION@", db_vers)
                str_out = str_out.replace("@DBPATCH@", db_patch)
                return str_out
        return replace_method

    def _db_profile(self):
        import re
        server_lib = self.ctx.dir / "lib" / "server"
        model_jars = server_lib.glob("model-*.jar")
        if len(model_jars) != 1:
            self.ctx.die(200, "Invalid model-*.jar state: %s"
                         % ",".join(model_jars))
        model_jar = model_jars[0]
        model_jar = str(model_jar.basename())
        match = re.search("model-(.*?).jar", model_jar)
        return match.group(1)

    def _sql_directory(self, db_vers, db_patch):
        """
        See #2689
        """
        dbprofile = self._db_profile()
        sql_directory = self.ctx.dir / "sql" / dbprofile / \
            ("%s__%s" % (db_vers, db_patch))
        if not sql_directory.exists():
            self.ctx.die(2, "Invalid Database version/patch: %s does not"
                         " exist" % sql_directory)
        return sql_directory

    def _create(self, sql_directory, db_vers, db_patch, password_hash, args,
                location=None):
        sql_directory = self._sql_directory(db_vers, db_patch)
        if not sql_directory.exists():
            self.ctx.die(2, "Invalid Database version/patch: %s does not"
                         " exist" % sql_directory)

        if args and args.file:
            output = args.file
            script = "<filename here>"
        else:
            script = "%s__%s.sql" % (db_vers, db_patch)
            location = path.getcwd() / script
            output = open(location, 'w')
            self.ctx.out("Saving to " + location)

        try:
            dbprofile = self._db_profile()
            header = sql_directory / ("%s-header.sql" % dbprofile)
            footer = sql_directory / ("%s-footer.sql" % dbprofile)
            if header.exists():
                # 73 multiple DB support. OMERO 4.3+
                cfg = {
                    "TIME": time.ctime(time.time()),
                    "DIR": sql_directory,
                    "SCRIPT": script}
                self._copy(header, output, str, cfg)
                self._copy(sql_directory/"schema.sql", output, str)
                self._copy(sql_directory/"views.sql", output, str)
                self._copy(
                    footer, output,
                    self._make_replace(password_hash, db_vers, db_patch), cfg)
            else:
                # OMERO 4.2.x and before
                output.write("""
--
-- GENERATED %s from %s
--
-- This file was created by the bin/omero db script command
-- and contains an MD5 version of your OMERO root users's password.
-- You should think about deleting it as soon as possible.
--
-- To create your database:
--
--     createdb omero
--     createlang plpgsql omero
--     psql omero < %s
--

BEGIN;
                """ % (time.ctime(time.time()), sql_directory, script))
                self._copy(sql_directory/"schema.sql", output, str)
                self._copy(
                    sql_directory/"data.sql", output,
                    self._make_replace(password_hash, db_vers, db_patch))
                self._copy(sql_directory/"views.sql", output, str)
                output.write("COMMIT;\n")

        finally:
            output.flush()
            if output != sys.stdout:
                output.close()

    def password(self, args):
        root_pass = None
        user_id = 0
        old_prompt = True
        if self._has_user_id(args):
            user_id = args.user_id
            if user_id != '0':  # For non-root, use new_prompt
                old_prompt = False
        try:
            root_pass = args.password
        except Exception, e:
            self.ctx.dbg("While getting arguments:" + str(e))
        password_hash = self._get_password_hash(args, root_pass, old_prompt)
        self.ctx.out("UPDATE password SET hash = '%s' "
                     "WHERE experimenter_id  = %s;""" %
                     (password_hash, user_id))

    def loaddefaults(self):
        try:
            data2 = self.ctx.initData({})
            output = self.ctx.readDefaults()
            self.ctx.parsePropertyFile(data2, output)
        except Exception, e:
            self.ctx.dbg(str(e))
            data2 = None
        return data2

    def script(self, args):
        if args.posversion is not None:
            self.ctx.err("WARNING: Positional arguments are deprecated")

        defaults = self.loaddefaults()
        db_vers = self._lookup("version", defaults, args)
        db_patch = self._lookup("patch", defaults, args)
        root_pass = args.password
        if root_pass:
            if args.pospassword:
                self.ctx.die(
                    1, "ERROR: Flag and positional argument given for password"
                    )
        else:
            root_pass = args.pospassword
        if root_pass:
            self.ctx.err("Using password from commandline")

        args.user_id = "0"
        sql = self._sql_directory(db_vers, db_patch)
        pwhash = self._get_password_hash(args, root_pass, True)
        self._create(sql, db_vers, db_patch, pwhash, args)

try:
    register("db", DatabaseControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("db", DatabaseControl, HELP)
        cli.invoke(sys.argv[1:])
