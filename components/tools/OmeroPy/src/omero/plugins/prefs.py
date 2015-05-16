#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   prefs plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   The pref plugin makes use of prefs.class from the common component.

   Copyright 2007-2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import sys
import traceback

from path import path
from omero.cli import CLI
from omero.cli import BaseControl
from omero.cli import ExistingFile
from omero.cli import NonZeroReturnCode
from omero.config import ConfigXml
from omero.util import edit_path, get_user_dir
from omero.util.decorators import wraps
from omero_ext import portalocker

import omero.java

HELP = """Commands for server configuration

A config.xml file will be modified under your etc/grid directory. If you do
not have one, "upgrade" will create a new 4.2 configuration file.

The configuration values are used by bin/omero admin {start,deploy} to set
properties on launch. See etc/grid/(win)default.xml. The "Profile" block
contains a reference to "__ACTIVE__" which is the current value in config.xml

By default, OMERO.grid will use the file in etc/grid/config.xml. If you would
like to configure your system to use $HOME/omero/config.xml, you will need to
modify the application descriptor.

Environment variables:
    OMERO_CONFIG - Changes the active profile

"""


def getprefs(args, dir):
    """
    Kept around temporarily for upgrading users from pre-4.2 configurations.
    """
    if not isinstance(args, list):
        raise Exception("Not a list")
    cmd = ["prefs"] + list(args)
    return omero.java.run(cmd, chdir=dir)


def _make_open_and_close_config(func, allow_readonly):
    def open_and_close_config(*args, **kwargs):
        args = list(args)
        self = args[0]
        argp = args[1]
        config = None
        if len(args) == 2:
            config = self.open_config(argp)
            if not allow_readonly:
                self.die_on_ro(config)
            args.append(config)
        try:
            return func(*args, **kwargs)
        finally:
            if config:
                config.close()
    return open_and_close_config


def with_config(func):
    """
    opens a config and passes it as the second argument.
    """
    return wraps(func)(_make_open_and_close_config(func, True))


def with_rw_config(func):
    """
    opens a config and passes it as the second argument.
    Requires that the returned config be writeable
    """
    return wraps(func)(_make_open_and_close_config(func, False))


class WriteableConfigControl(BaseControl):
    """
    Base class for controls which need write access to the OMERO configuration
    using the @with_rw_config decorator

    Note BaseControl should be used for read-only access using @with_config
    """

    def die_on_ro(self, config):
        if not config.save_on_close:
            self.ctx.die(333, "Cannot modify %s" % config.filename)


class PrefsControl(WriteableConfigControl):

    def _configure(self, parser):
        parser.add_argument(
            "--source", help="Configuration file to be used. Default:"
            " etc/grid/config.xml")

        sub = parser.sub()

        parser.add(
            sub, self.all,
            "List all profiles in the current config.xml file.")

        default = sub.add_parser(
            "def", help="List (or set) the current active profile.",
            description="List (or set) the current active profile.")
        default.set_defaults(func=self.default)
        default.add_argument(
            "NAME", nargs="?",
            help="Name of the profile which should be made the new active"
            " profile.")

        list = parser.add(
            sub, self.list,
            "List all key-value pairs from the current profile")
        list.set_defaults(func=self.list)

        get = parser.add(
            sub, self.get,
            "Get key-value pairs from the current profile. All by default")
        get.set_defaults(func=self.get)
        get.add_argument(
            "KEY", nargs="*", help="Names of keys in the current profile")
        get.add_argument(
            "--hide-password", action="store_true",
            help="Hide values of password keys in the current profile")

        set = parser.add(
            sub, self.set,
            "Set key-value pair in the current profile. Omit the"
            " value to remove the key.")
        append = parser.add(
            sub, self.append, "Append value to a key in the current profile.")
        remove = parser.add(
            sub, self.remove,
            "Remove value from a key in the current profile.")

        for x in [set, append, remove]:
            x.add_argument(
                "KEY", help="Name of the key in the current profile")
        set.add_argument(
            "-f", "--file", type=ExistingFile('r'),
            help="Load value from file")
        set.add_argument(
            "VALUE", nargs="?",
            help="Value to be set. If it is missing, the key will be removed")
        append.add_argument("VALUE", help="Value to be appended")
        remove.add_argument("VALUE", help="Value to be removed")

        drop = parser.add(
            sub, self.drop, "Remove the profile from the configuration file")
        drop.add_argument("NAME", help="Name of the profile to remove")

        parser.add(sub, self.keys, "List all keys for the current profile")

        load = parser.add(
            sub, self.load,
            "Read into current profile from a file or standard input")
        load.add_argument(
            "-q", action="store_true", help="No error on conflict")
        load.add_argument(
            "file", nargs="*", type=ExistingFile('r'), default="-",
            help="Files to read from. Default to standard input if not"
            " specified")

        parse = parser.add(
            sub, self.parse,
            "Parse the configuration properties from the etc/omero.properties"
            " file and Web properties for readability.")
        parse.add_argument(
            "-f", "--file", type=ExistingFile('r'),
            help="Alternative location for a Java properties file")
        parse_group = parse.add_mutually_exclusive_group()
        parse_group.add_argument(
            "--defaults", action="store_true",
            help="Show key/value configuration defaults")
        parse_group.add_argument(
            "--rst", action="store_true",
            help="Generate reStructuredText from omero.properties")
        parse_group.add_argument(
            "--keys", action="store_true",
            help="Print just the keys from omero.properties")
        parse_group.add_argument(
            "--headers", action="store_true",
            help="Print all headers from omero.properties")
        parse.add_argument(
            "--no-web", action="store_true",
            help="Do not parse Web properties")

        parser.add(sub, self.edit, "Present the properties for the current"
                   " profile in your editor. Saving them will update your"
                   " profile.")
        parser.add(sub, self.version, "Print the configuration version for"
                   " the current profile.")
        parser.add(sub, self.path, "Print the file that is used for "
                   " configuration")
        parser.add(sub, self.lock, "Acquire the config file lock and hold"
                   " it")
        parser.add(sub, self.upgrade, "Create a 4.2 config.xml file based on"
                   " your current Java Preferences")
        old = parser.add(sub, self.old, "Delegate to the old configuration"
                         " system using Java preferences")
        old.add_argument("target", nargs="*")

    def open_config(self, args):
        if args.source:
            cfg_xml = path(args.source)
            if not cfg_xml.exists():
                self.ctx.die(124, "File not found: %s" % args.source)
        else:
            grid_dir = self.ctx.dir / "etc" / "grid"
            if grid_dir.exists():
                cfg_xml = grid_dir / "config.xml"
            else:
                userdir = path(get_user_dir())
                usr_xml = userdir / "omero" / "config.xml"
                self.ctx.err("%s not found; using %s" % (grid_dir, usr_xml))
                cfg_xml = usr_xml
        try:
            return ConfigXml(str(cfg_xml))
        except portalocker.LockException:
            self.ctx.die(112, "Could not acquire lock on %s" % cfg_xml)
        except Exception, e:
            self.ctx.die(113, str(e))

    @with_config
    def all(self, args, config):
        for k, v in config.properties(None, True):
            self.ctx.out(k)

    @with_config
    def default(self, args, config):
        if args.NAME is not None:
            self.die_on_ro(config)
        self.ctx.out(config.default(args.NAME))

    @with_config
    def drop(self, args, config):
        try:
            config.remove(args.NAME)
        except KeyError:
            self.ctx.err("Unknown configuration: %s" % args.NAME)

    @with_config
    def list(self, args, config):
        args.KEY = []
        self.get(args, config)

    @with_config
    def get(self, args, config):
        orig = sorted(list(config.keys()))
        keys = sorted(list(args.KEY))
        if not keys:
            keys = orig
            for k in config.IGNORE:
                k in keys and keys.remove(k)

        hide_password = 'hide_password' in args and args.hide_password
        is_password = lambda x: x.endswith('.pass') or x.endswith('.password')
        for k in keys:
            if k not in orig:
                continue
            if args.KEY and len(args.KEY) == 1:
                self.ctx.out(config[k])
            else:
                if (hide_password and is_password(k)):
                    self.ctx.out("%s=%s" % (k, '*' * 8 if config[k] else ''))
                else:
                    self.ctx.out("%s=%s" % (k, config[k]))

    @with_rw_config
    def set(self, args, config):
        if "=" in args.KEY:
            k, v = args.KEY.split("=", 1)
            msg = """ "=" in key name. Did you mean "...set %s %s"?"""
            if args.VALUE is None:
                k, v = args.KEY.split("=", 1)
                self.ctx.err(msg % (k, v))
            elif args.KEY.endswith("="):
                self.ctx.err(msg % (k, args.VALUE))
        elif args.file:
            if args.file == "-":
                # Read from standard input
                import fileinput
                f = fileinput.input(args.file)
            else:
                f = args.file
            try:
                config[args.KEY] = (''.join(f)).rstrip()
            finally:
                f.close()
        elif args.VALUE is None:
            del config[args.KEY]
        else:
            config[args.KEY] = args.VALUE

    def get_list_value(self, args, config):
        import json
        try:
            list_value = json.loads(config[args.KEY])
        except ValueError:
            self.ctx.die(510, "No JSON object could be decoded")

        if not isinstance(list_value, list):
            self.ctx.die(511, "Property %s is not a list" % args.KEY)
        return list_value

    def get_omeroweb_default(self, key):
        try:
            from omeroweb import settings
            setting = settings.CUSTOM_SETTINGS_MAPPINGS.get(key)
            default = setting[2](setting[1]) if setting else []
        except:
            self.ctx.die(514, "Cannot retrieve default value for property %s" %
                         key)
        if not isinstance(default, list):
            self.ctx.die(515, "Property %s is not a list" % key)
        return default

    @with_rw_config
    def append(self, args, config):
        import json
        if args.KEY in config.keys():
            list_value = self.get_list_value(args, config)
            list_value.append(json.loads(args.VALUE))
        elif args.KEY.startswith('omero.web.'):
            list_value = self.get_omeroweb_default(args.KEY)
            list_value.append(json.loads(args.VALUE))
        else:
            list_value = [json.loads(args.VALUE)]
        config[args.KEY] = json.dumps(list_value)

    @with_rw_config
    def remove(self, args, config):
        if args.KEY not in config.keys():
            if args.KEY.startswith('omero.web.'):
                list_value = self.get_omeroweb_default(args.KEY)
            else:
                self.ctx.die(512, "Property %s is not defined" % (args.KEY))
        else:
            list_value = self.get_list_value(args, config)
        import json
        if json.loads(args.VALUE) not in list_value:
            self.ctx.die(513, "%s is not defined in %s"
                         % (args.VALUE, args.KEY))

        list_value.remove(json.loads(args.VALUE))
        config[args.KEY] = json.dumps(list_value)

    @with_config
    def keys(self, args, config):
        for k in config.keys():
            if k not in config.IGNORE:
                self.ctx.out(k)

    def parse(self, args):
        if args.file:
            args.file.close()
            cfg = path(args.file.name)
        else:
            cfg = self.dir / "etc" / "omero.properties"

        from omero.install.config_parser import PropertyParser
        pp = PropertyParser()
        pp.parse_file(str(cfg.abspath()))

        # Parse PSQL profile file
        for p in pp:
            if p.key == "omero.db.profile":
                psql_file = self.dir / "etc" / "profiles" / p.val
                pp.parse_file(str(psql_file.abspath()))
                break

        # Parse OMERO.web configuration properties
        if not args.no_web:
            pp.parse_module('omeroweb.settings')

        # Display options
        if args.headers:
            pp.print_headers()
        elif args.keys:
            pp.print_keys()
        elif args.rst:
            pp.print_rst()
        else:
            pp.print_defaults()

    @with_rw_config
    def load(self, args, config):
        keys = None
        if not args.q:
            keys = config.keys()

        try:
            for f in args.file:
                if f == "-":
                    # Read from standard input
                    import fileinput
                    f = fileinput.input(f)

                try:
                    previous = None
                    for line in f:
                        if previous:
                            line = previous + line
                        previous = self.handle_line(line, config, keys)
                finally:
                    if f != "-":
                        f.close()
        except NonZeroReturnCode:
            raise
        except Exception, e:
            self.ctx.die(968, "Cannot read %s: %s" % (args.file, e))

    @with_rw_config
    def edit(self, args, config, edit_path=edit_path):
        from omero.util.temp_files import create_path, remove_path
        start_text = "# Edit your preferences below. Comments are ignored\n"
        for k in sorted(config.keys()):
            start_text += ("%s=%s\n" % (k, config[k]))
        temp_file = create_path()
        try:
            edit_path(temp_file, start_text)
        except RuntimeError, re:
            self.ctx.dbg(traceback.format_exc())
            self.ctx.die(954, "%s: Failed to edit %s"
                         % (getattr(re, "pid", "Unknown"), temp_file))
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

    @with_rw_config
    def lock(self, args, config):
        self.ctx.input("Press enter to unlock")

    @with_rw_config
    def upgrade(self, args, config):
        self.ctx.out("Importing pre-4.2 preferences")
        txt = getprefs(["get"], str(self.ctx.dir / "lib"))
        for line in txt.split("\n"):
            self.handle_line(line, config, None)

        # Upgrade procedure for 4.2
        MSG = """Manually modify them via "omero config old set ..." and \
re-run"""
        m = config.as_map()
        for x in ("keyStore", "keyStorePassword", "trustStore",
                  "trustStorePassword"):
            old = "omero.ldap." + x
            new = "omero.security." + x
            if old in m:
                config[new] = config[old]

        attributes, values = [], []
        if "omero.ldap.attributes" in m:
            attributes = config["omero.ldap.attributes"]
            attributes = attributes.split(",")
        if "omero.ldap.values" in m:
            values = config["omero.ldap.values"]
            values = values.split(",")

        if len(attributes) != len(values):
            raise ValueError("%s != %s\nLDAP properties in pre-4.2"
                             " configuration are invalid.\n%s"
                             % (attributes, values, MSG))
        pairs = zip(attributes, values)
        if pairs:
            if len(pairs) == 1:
                user_filter = "(%s=%s)" % (tuple(pairs[0]))
            else:
                user_filter = "(&%s)" % ["(%s=%s)" % tuple(pair)
                                         for pair in pairs]
            config["omero.ldap.user_filter"] = user_filter

        if "omero.ldap.groups" in m:
            raise ValueError("Not currently handling omero.ldap.groups\n%s"
                             % MSG)

        config["omero.config.upgraded"] = "4.2.0"

    def handle_line(self, line, config, keys):
        line = line.strip()
        if not line or line.startswith("#"):
            return None
        if line.endswith("\\"):
            return line[:-1]

        parts = line.split("=", 1)
        if len(parts[0]) == 0:
            return
        if len(parts) == 1:
            parts.append("")

        _key = parts[0]
        _new = parts[1]
        if _key in config.keys():
            _old = config[_key]
        else:
            _old = None

        if keys and _key in keys and _new != _old:

            self.ctx.die(502, "Duplicate property: %s ('%s' => '%s')"
                         % (_key, _old, _new))
            keys.append(_key)

        config[_key] = _new

    def old(self, args):
        self.ctx.out(getprefs(args.target, str(self.ctx.dir / "lib")))

try:
    register("config", PrefsControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("config", PrefsControl, HELP)
        cli.invoke(sys.argv[1:])
