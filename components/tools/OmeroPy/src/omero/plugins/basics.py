#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   load, quit, version, help plugins

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   The load plugin is used to read in files with omero cli commands
   (omitting the omero). For example,

   ./omero load some/file.osh

   The help, quit, and version plugins are self-explanatory.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import sys

from omero_ext.argparse import FileType

from omero.cli import BaseControl
from omero.cli import CLI
from omero.cli import VERSION


class QuitControl(BaseControl):

    def __call__(self, args):
        self.ctx.exit("", newline=False)


class VersionControl(BaseControl):

    def __call__(self, args):
        self.ctx.out(VERSION)


class LoadControl(BaseControl):

    def _configure(self, parser):
        parser.add_argument("infile", nargs="*", type=FileType("r"),
                            default=[sys.stdin])
        parser.set_defaults(func=self.__call__)

    def __call__(self, args):
        for file in args.infile:
            self.ctx.dbg("Loading file %s" % file)
            for line in file:
                self.ctx.invoke(line)


class ShellControl(BaseControl):

    def _configure(self, parser):
        parser.add_argument(
            "--login", action="store_true",
            help="Logins in and sets the 'client' variable")
        parser.add_argument("arg", nargs="*", help="Arguments for IPython.")
        parser.set_defaults(func=self.__call__)

    def __call__(self, args):
        """
        Copied from IPython embed-short example
        """
        import logging
        logging.basicConfig()
        from omero.util.upgrade_check import UpgradeCheck
        check = UpgradeCheck("shell")
        check.run()
        if check.isUpgradeNeeded():
            self.ctx.out("")

        ns = {}
        if args.login:
            import omero
            client = self.ctx.conn(args)
            ns = {"client": client, "omero": omero}

        try:
            # IPython 0.11 (see #7112)
            from IPython import embed
            embed(user_ns=ns)
        except ImportError:
            from IPython.Shell import IPShellEmbed
            ipshell = IPShellEmbed(args.arg)
            ipshell(local_ns=ns)


class HelpControl(BaseControl):
    """
    Defined here since the background loading might be too
    slow to have all help available
    """

    def _configure(self, parser):
        self.__parser__ = parser  # For formatting later
        parser.set_defaults(func=self.__call__)
        parser.add_argument(
            "--all", action="store_true", help="Print help for all topics")
        parser.add_argument(
            "topic", nargs="?", help="Topic for more information")

    def _complete(self, text, line, begidx, endidx):
        """
        This is something of a hack. This should either be a part
        of the context interface, or we should put it somewhere
        in a utility. FIXME.
        """
        return self.ctx.completenames(text, line, begidx, endidx)

    def __call__(self, args):

        self.ctx.waitForPlugins()
        # commands = "\n".join([" %s" % name for name in
        # sorted(self.ctx.controls)])
        # topics = "\n".join([" %s" % topic for topic in self.ctx.topics])
        commands, topics = [
            self.__parser__._format_list(x) for x in
            [sorted(self.ctx.controls), sorted(self.ctx.topics)]]

        if args.all:
            for control in sorted(self.ctx.controls):
                self.ctx.out("*" * 80)
                self.ctx.out(control)
                self.ctx.out("*" * 80)
                self.ctx.invoke([control, "-h"])
                self.ctx.out("\n")
            for topic in sorted(self.ctx.topics):
                self.ctx.out("*" * 80)
                self.ctx.out(topic)
                self.ctx.out("*" * 80)
                self.ctx.out(self.ctx.topics[topic])
                self.ctx.out("\n")
        elif not args.topic:
            # self.ctx.invoke("-h")
            key_list = {"program_name": sys.argv[0], "version": VERSION,
                        "commands": commands, "topics": topics}
            print """usage: %(program_name)s <command> [options] args
See 'help <command>' or '<command> -h' for more information on syntax
Type 'quit' to exit

Available commands:
%(commands)s

Other help topics:
%(topics)s

For additional information, see:
http://www.openmicroscopy.org/site/support/omero4/users/\
command-line-interface.html
Report bugs to <ome-users@lists.openmicroscopy.org.uk>
""" % key_list

        else:
            try:
                print 1
                self.ctx.controls[args.topic]
                self.ctx.invoke("%s -h" % args.topic)
            except KeyError:
                try:
                    self.ctx.out(self.ctx.topics[args.topic])
                except KeyError:
                    self.ctx.err("Unknown help topic: %s" % args.topic)

controls = {
    "help": (HelpControl, "Syntax help for all commands"),
    "quit": (QuitControl, "Quit application"),
    "shell": (ShellControl, """Starts an IPython interpreter session

All arguments not understood vi %(prog)s will be passed to the shell.
Use "--" to end parsing, e.g. '%(prog)s -- --help' for IPython help"""),
    "version": (VersionControl, "Version number"),
    "load": (LoadControl, """Load file as if it were sent on standard in.

This can be used as the #! header of a file to make standand-alone script.

Examples:
    #!/usr/bin/env omero load
    login -C root@localhost
    group add new_group
    user add foo some user new_group

 or

    $ bin/omero login       # login can't take place in HERE-document
    $ bin/omero load <<EOF
    user list
    group list
    EOF

""")
}

try:
    for k, v in controls.items():
        register(k, v[0], v[1])
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        for k, v in controls.items():
            cli.register(k, v[0], v[1])
        cli.invoke(sys.argv[1:])
