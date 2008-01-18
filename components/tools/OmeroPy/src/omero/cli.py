#!/usr/bin/env python

"""
Python driver for OMERO

Provides access to various OMERO.blitz server- and client-side
utilities, including starting and stopping servers, running
analyses, configuration, and more.

Usable via the ./omero script provided with the distribution
as well as from python via "import omero.cli; omero.cli.run()"

Arguments are taken from (in order of priority): the run method
arguments, sys.argv, and finally from standard in using the
cmd.Cmd.cmdloop method.

Josh Moore, josh at glencoesoftware.com
Copyright (c) 2007, Glencoe Software, Inc.
See LICENSE for details.

"""

import cmd, string, re, os, types, shlex
from omero_ext import pysys

VERSION=1.0
COMMENT = re.compile("^\s*#")
DEBUG = False
if os.environ.has_key("DEBUG"):
    print "Running omero with debugging on"
    DEBUG = True
TEXT="""
  OMERO Python Shell. Version %s
  Type "?" or "help" for more information, "quit" or Ctrl-D to exit
""" % str(VERSION)

class Command:
    """
    Currently unused.
    """

    def __init__(self):
        pass

    def __call__(self, arg):
        print arg


class CLI(cmd.Cmd):
    """
    Command line interface class. Supports various styles of executing the
    registered plugins. Each plugin is given the chance to update this class
    by adding methods of the form "do_<plugin name>".
    """

    def __init__(self):
        """
        Also sets the "_client" field for this instance to None. Each cli
        maintains a single active client.
        """
        cmd.Cmd.__init__(self)
        self.prompt = 'omero> '
        self.interrupt_loop = False
        self._client = None

    def invoke(self, line):
        """
        Copied from cmd.py
        """
        line = self.precmd(line)
        stop = self.onecmd(line)
        stop = self.postcmd(stop, line)

    def invokeloop(self):
        try:
            self.selfintro = TEXT
            if not self.stdin.isatty():
                self.selfintro = ""
                self.prompt = ""
            self.cmdloop()
        except KeyboardInterrupt, ki:
            print
        self.interrupt_loop = True

    def precmd(self, input):
        if not isinstance(input,types.ListType):
            if COMMENT.match(input):
                return ""
        return input

    def postcmd(self, stop, line):
        return self.interrupt_loop

    def emptyline(self):
        pass

    def parseline(self, line):
        """
        Overrides the parseline functionality of cmd.py in order to
        take command line parameters without shlex'ing and unshlex'ing
        them. If "line" is an array, then the first element will be
        returned as "cmd" and the rest as "args".
        """
        if isinstance(line,types.ListType):
            if not line:
                return (None, None, None)
            elif len(line) == 0:
                return (None, None, "")
            elif len(line) == 1:
                return (line[0],None,line[0])
            else:
                return (line[0]," ".join(line[1:])," ".join(line))
        else:
            return cmd.Cmd.parseline(self,line)

    def shlex(self, input):
        if isinstance(input, types.StringType):
            return shlex.split(input)
        return input

    def default(self,arg):
        if arg.startswith("EOF"):
            pysys.exit(0)
        else:
            print "Unkown command: " + arg

    def completedefault(self, text, line, begidx, endidx):
        try:
            import readline
            # import rlcompleter
        except ImportError, ie:
            return []

        # readline.parse_and_bind("tab: complete")
        readline.set_completer_delims(' \t\n`~!@#$%^&*()-=+[{]}\\|;:\'",<>;?')
        import glob
        return glob.glob('%s*' % text)

    def do_help(self, arg):
        if not arg or len(arg) == 0:
            print """

        Use "help <topic>" for more information
        -------------------------------------------------
        longhelp  -  long output of help topics
        v[ersion] -  print version number
        q[uit]    -

        """
        else:
            cmd.Cmd.do_help(self, arg)
    do_h = do_help
    do_usage = do_help

    def do_longhelp(self, arg):
        cmd.Cmd.do_help(self, arg)

    #
    # User Methods
    #

    # Topics

    def help_basics(self):
        print "Simple commands include: version, help, quit, reload"

    def help_login(self):
        print """
        The OMERO cli uses the prefs Java class to configure the current profile.
        """

    def do_quit(self, arg):
        pysys.exit(1)
    do_q = do_quit

    def help_quit(self):
        print "syntax: quit",
        print "-- terminates the application"
    help_q = help_quit

    def do_version(self, arg):
        "print current version"
        print VERSION
    do_v = do_version

    def help_server(self):
        print "omero-<name>"

    def do_load(self, arg):
        file = open(arg,'r')
        for line in file:
           self.invoke(line)

    def help_load(self):
        status = "enabled"
        try:
            import readline
        except:
            status = "disabled"

        print "load file as if it were sent on standard in. File tab-complete %s" % status

    #
    # Blitz methods
    #

    def client(self, properties={}, profile=None):

        if self._client:
            return self._client

        import omero.java
        if profile:
            omero.java.run(["prefs","def","profile"])
        output = omero.java.run(["prefs","get"])

        import Ice
        data = Ice.InitializationData()
        data.properties = Ice.createProperties()
        for line in output.splitlines():
           parts = line.split("=",1)
           data.properties.setProperty(parts[0],parts[1])

        import omero
        self._client = omero.client(pysys.argv, id = data)
        self._client.createSession()
        return self._client

    ## End Cli

def argv(args=pysys.argv):
    """
    Main entry point for the OMERO command-line interface. First
    loads all plugins by passing them the classes defined here
    so they can add their methods.

    Then the case where arguments are passed on the command line are
    handled.

    Finally, the cli enters a command loop reading from standard in.
    """
    UniqueCLI = loadplugins()

    # Modifying the args list if the name of the file
    # has arguments encoded in it
    if args[0].find("-") >= 0:
        parts = args[0].split("-")
        for arg in args[1:]:
            parts.append(arg)
        args = parts

    cli = UniqueCLI()
    if len(args) > 1:
        cli.invoke(args[1:])
    else:
        cli.invokeloop()

def loadplugins():

    class UniqueCLI(CLI):
        pass

    loc = {}
    loc["CLI"] = UniqueCLI
    loc["Command"] = Command

    from os.path import abspath, realpath, join, dirname
    dir = join(dirname(abspath(__file__)),"plugins")
    for root, dirs, files in os.walk(dir):
        for file in files:
            if DEBUG:
                print "Loading " + file
            if -1 == file.find("#"):
                execfile( root + "/" + file, loc )

    return UniqueCLI
