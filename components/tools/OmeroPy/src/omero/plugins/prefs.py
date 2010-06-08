#!/usr/bin/env python
"""
   prefs plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   The pref plugin makes use of prefs.class from the common component.

   Copyright 2007 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import sys, os

from exceptions import Exception
from path import path
from omero.cli import CLI
from omero.cli import BaseControl
from omero.config import ConfigXml
from omero_ext.argparse import FileType
from omero_ext.strings import shlex
from omero.util import edit_path, get_user_dir
from omero.util.decorators import wraps
import omero.java

HELP="""
  Commands for configuration server installation.
  A config.xml file will be created under your etc/grid
  directory.

  Environment variables:
    OMERO_CONFIG - Changes the active profile
"""

def getprefs(args, dir):
    """
    Kept around temporarily for upgrading users from pre-4.2 configurations.
    """
    if not isinstance(args,list):
        raise Exception("Not a list")
    cmd = ["prefs"]+list(args)
    return omero.java.run(cmd, chdir=dir)

def with_config(func):
    """
    opens a config and passes it as the second argument.
    """
    def open_config(*args, **kwargs):
        args = list(args)
        self = args[0]
        argp = args[1]
        config = None
        if len(args) == 2:
            config = self.open(argp)
            args.append(config)
        try:
            return func(*args, **kwargs)
        finally:
            if config:
                config.close()
    return wraps(func)(open_config)


class PrefsControl(BaseControl):

    def _configure(self, parser):
        parser.add_argument("--source", help="""
            Which configuration file should be used. By default, OMERO.grid
            will use the file in etc/grid/config.xml. If you would like to
            configure your system to use $HOME/omero/config.xml, you will
            need to modify the application descriptor.
        """)

        sub = parser.add_subparsers(title="Subcommands", help="""
                Use %(prog)s <subcommand> -h for more information.
        """)

        all = sub.add_parser("all", help="""
            List all properties in the current config.xml file.
        """)
        all.set_defaults(func=self.all)

        default = sub.add_parser("def", help="""
            List (or set) the current properties. See 'all' for a list of available properties.
        """)
        default.set_defaults(func=self.default)
        default.add_argument("NAME", nargs="?", help="""
            Name of the profile which should be made the new active profile.
        """)

        get = sub.add_parser("get", help="""
            Get keys from the current profile. All by default
        """)
        get.set_defaults(func=self.get)
        get.add_argument("KEY", nargs="*")

        set = sub.add_parser("set", help="""
            Set key-value pair in the current profile.
        """)
        set.set_defaults(func=self.set)
        set.add_argument("KEY")
        set.add_argument("VALUE", nargs="?", help="Value to be set. If it is missing, the key will be removed")

        drop = sub.add_parser("drop", help="""
            Removes the profile from the configuration file
        """)
        drop.set_defaults(func=self.drop)
        drop.add_argument("NAME")

        keys = sub.add_parser("keys", help="""
            List all keys for the current profile
        """)
        keys.set_defaults(func=self.keys)

        load = sub.add_parser("load", help="""
            Read into current profile from a file or standard in
        """)
        load.set_defaults(func=self.load)
        load.add_argument("-q", action="store_true", help="No error on conflict")
        load.add_argument("file", nargs="+", type=FileType('r'), default=sys.stdin, help="Read from files or standard in")

        edit = parser.add(sub, self.edit, "Presents the properties for the current profile in your editor. Saving them will update your profile.")
        version = parser.add(sub, self.version, "Prints the configuration version for the current profile.")
        path = parser.add(sub, self.path, "Prints the file that is used for configuration")
        upgrade = parser.add(sub, self.upgrade, "Creates a 4.2 config.xml file based on your current Java Preferences")
        old = parser.add(sub, self.old, "Delegates to the old configuration system using Java preferences")
        old.add_argument("target", nargs="*")

    def open(self, args):
        if args.source:
            cfg_xml = path(args.source)
            if not cfg_xml.exists():
                self.ctx.die(124, "File not found: %s" % args.source)
        else:
            cfg_xml = path("etc") / "grid" / "config.xml"
        if not cfg_xml.exists():
            userdir = path(get_user_dir())
            usr_xml = userdir / "omero"/ "config.xml"
            self.ctx.err("%s not found; using %s" % (cfg_xml, usr_xml))
            cfg_xml = usr_xml
        return ConfigXml(str(cfg_xml))

    @with_config
    def all(self, args, config):
        for k, v in config.properties(None, True):
            self.ctx.out(k)

    @with_config
    def default(self, args, config):
        if args.NAME:
            config.default(args.NAME)
        else:
            self.ctx.out(config.default(args.NAME))

    @with_config
    def drop(self, args, config):
        config.remove(args.NAME)

    @with_config
    def get(self, args, config):
        orig = sorted(list(config.keys()))
        keys = sorted(list(args.KEY))
        if not keys:
            keys = orig
            for k in config.IGNORE:
                k in keys and keys.remove(k)

        for k in keys:
            if k not in orig:
                continue
            if args.KEY and len(args.KEY) == 1:
                self.ctx.out(config[k])
            else:
                self.ctx.out("%s=%s" % (k, config[k]))

    @with_config
    def set(self, args, config):
        if "=" in args.KEY and args.VALUE is None:
            k, v = args.KEY.split("=")
            self.ctx.err(""" "=" in key name. Did you mean "...set %s %s"?""" % (k, v))
        if args.VALUE is None:
            del config[args.KEY]
        else:
            config[args.KEY] = args.VALUE

    @with_config
    def keys(self, args, config):
        for k in config.keys():
            if k not in config.IGNORE:
                self.ctx.out(k)

    @with_config
    def load(self, args, config):
        keys = None
        if not args.q:
            keys = config.keys()

        for f in args.file:
            try:
                for line in f:
                    self.handle_line(line, config, keys)
            finally:
                f.close()

    @with_config
    def edit(self, args, config, edit_path = edit_path):
        from omero.util.temp_files import create_path, remove_path
        start_text = "# Edit your preferences below. Comments are ignored\n"
        for k in config.keys():
            start_text += ("%s=%s" % (k, config[k]))
        temp_file = create_path()
        try:
            edit_path(temp_file, start_text)
        except RuntimeError, re:
            self.ctx.die(954, "%s: Failed to edit %s" % (getattr(re, "pid", "Unknown"), temp_file))
        args.NAME = config.default()
        self.drop(args, config)
        args.file = [open(str(temp_file), "r")]
        args.q = True
        self.load(args, config)
        remove_path(temp_file)

    @with_config
    def version(self, args, config):
        self.ctx.out(config.version(config.default()))

    @with_config
    def path(self, args, config):
        self.ctx.out(config.filename)

    @with_config
    def upgrade(self, args, config):
        self.ctx.out("Importing pre-4.2 preferences")
        txt = getprefs(["get"], str(self.ctx.dir / "lib"))
        for line in txt.split("\n"):
            self.handle_line(line, config, None)

    def handle_line(self, line, config, keys):
        line = line.strip()
        parts = line.split("=")
        if len(parts[0]) == 0:
            return
        if len(parts) == 1:
            parts.append("")
        if keys and parts[0] in keys:
            self.ctx.die(502, "Duplicate property: %s (%s => %s)"\
                % (parts[0], config[parts[0]], parts[1]))
            keys.append(parts[0])
        config[parts[0]] = parts[1]

    def old(self, args):
        self.ctx.out(getprefs(args.target, str(self.ctx.dir / "lib")))

try:
    register("config", PrefsControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("config", PrefsControl, HELP)
        cli.invoke(sys.argv[1:])
