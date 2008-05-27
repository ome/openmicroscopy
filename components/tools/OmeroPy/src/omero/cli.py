#!/usr/bin/env python

"""
Python driver for OMERO

Provides access to various OMERO.blitz server- and client-side
utilities, including starting and stopping servers, running
analyses, configuration, and more.

Usable via the ./omero script provided with the distribution
as well as from python via "import omero.cli; omero.cli.argv()"

Arguments are taken from (in order of priority): the run method
arguments, sys.argv, and finally from standard in using the
cmd.Cmd.cmdloop method.

Josh Moore, josh at glencoesoftware.com
Copyright (c) 2007, Glencoe Software, Inc.
See LICENSE for details.

"""

import cmd, string, re, os, sys, subprocess, socket, exceptions, traceback
from omero_ext import pysys
from omero_ext.strings import shlex
from exceptions import Exception as Exc
from path import path
import Ice


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

OMEROCLI = path(__file__).expand().dirname()

#
# Possibilities:
#  - Always return and print any output
#  - Have a callback on the fired event


class Event:
    """Simple event stub used for testing plugins.

    The Event class is designed to increase pluggability. Rather than
    making calls directly on other plugins directly, the pub() method
    routes messages to other commands. Similarly, out() and err() should
    be used for printing statements to the user, and die() should be 
    used for exiting fatally.

    The CLI registry which registers the plugins installs itself as the
    Event.
    """

    def pub(self, args):
        print args

    def out(self, text):
        print >>pysys.stdout, text % {"program_name":pysys.argv[0]}

    def err(self, text):
        print >>pysys.stderr, text

    def die(self, args):
        raise exceptions.Exception(args)

    def popen(self, string):
        print string

    def conn(self):
        raise NotImplementedException()

class BaseControl:
    """Controls get registered with a CLI instance on loadplugins()
    """

    def __init__(self, omeroDir, event):
        self.omeroDir = path(omeroDir) # Guaranteed to be a path
        self.event = event

    def _host(self):
        if not hasattr(self, "hostname") or not self.hostname:
            self.hostname = socket.gethostname()
            if self.hostname.find(".") > 0:
                self.hostname = self.hostname.split(".")[0]
        return self.hostname

    def _node(self):
        if os.environ.has_key("OMERO_NODE"):
            return os.environ["OMERO_NODE"]
        else:
            return self._host()

    def _data(self):
        try:
            nodedata = path(self._properties()["IceGrid.Node.Data"])
        except KeyError, ke:
            self.event.err("IceGrid.Node.Data is not configured")
            self.event.die(ke)

        if not nodedata.exists():
            self.event.out("Creating "+nodedata)
            nodedata.makedirs()
        return nodedata

    def _pid(self):
        pidfile = self._data() / (self._name() + ".pid")
        return pidfile

    def _cfglist(self):
        cfgs = self.omeroDir / "etc"
        internal = cfgs / "internal.cfg"
        owncfg = cfgs / self._node() + ".cfg"
        return (internal,owncfg)

    def _icecfg(self):
         icecfg = "--Ice.Config=%s" % ",".join(self._cfglist())
         return icecfg

    def _properties(self):
        if not hasattr(self, "_props") or self._props == None:
            properties = Ice.createProperties()
            for cfg in self._cfglist():
                try:
                    properties.load(str(cfg))
                except Exc, exc:
                    self.event.err("Could not find file: "+cfg)
                    self.event.die(exc)
            self._props = properties.getPropertiesForPrefix("")
        return self._props

    #
    # Methods to be implement by subclasses
    #

    def help(self):
        return """ Help not implemented """

    def _likes(self, args):
        args = shlex(args)
        # Here we accept all commands of length zero since
        # there's a default implementation for that.
        if len(args) == 0 or hasattr(self,args[0]):
            return True
        return False

    def __call__(self, *args):
        if len(args) == 0:
            return self._noargs()

        elif len(args) == 1:
            args = shlex(args[0])
            if not self._likes(args):
                self.event.err("%s cannot handle arguments: %s" % (self, args))
                self.event.abort()
            else:
                m = getattr(self, args[0])
                if len(args) > 1:
                    return m(args[1:])
                else:
                    return m()
        else:
            self.event.die("Don't know how to handle more than noargs or shlex args by default")

    def _noargs(self):
        self.help()

    def _name(self):
        raise NotImplementedException()

    def _main(self):
        if __name__ == "__main__":
            if not self._likes(pysys.argv[1:]):
                print self.help()
            else:
                self._run(pysys.argv[1:])

class CLI(cmd.Cmd, Event):
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
        stop = True
        try:
            stop = self.onecmd(line)
        except AttributeError, ae:
            print "Possible error in plugin:"
            print ae
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
        if not isinstance(input,list):
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
        if isinstance(line,list):
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

    def throw(self, string):
        """
        Simple method for throwing a Cli-specific exception
        """
        raise Exc(string)

    def pythonpath(self):
        """
        Converts the current sys.path to a PYTHONPATH string
        to be used by plugins which must start a new process.

        Note: this was initially created for running during
        testing when PYTHONPATH is not properly set.
        """
        path = list(pysys.path)
        for i in range(0,len(path)-1):
            if path[i] == '':
                path[i] = os.getcwd()
        pythonpath = ":".join(path)
        return pythonpath

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

    # Delegation
    def do_start(self, arg):
        """
        Alias for "node start"
        """
        arg = self.shlex(arg)
        if not arg:
            arg = "start"
        else:
            arg = ["start"] + arg
        self.do_node(arg)

    ##
    ## Event interface
    ##
    def pub(self, args):
        Event.pub(self, args)

    def out(self, text):
        Event.pub(self, text)

    def err(self, text):
        Event.err(self, text)

    def die(self, args):
        Event.err(self, args)

    def popen(self, string):
        rv = subprocess.call(string)
        if not rv == 0:
            self.die("Error during:\"%s\"" % string )
        return rv

    def conn(self, properties={}, profile=None):

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

    ##
    ## Plugin registry
    ##

    def register(self, control):
        """ This method is added to the globals when execfile() is
        called on each plugin. An instance of the control should be
        passed to the register method which will be added to the CLI.
        """
        control.omeroDir = OMEROCLI.dirname().dirname()
        control.event = self
        setattr(self, "do_" + control._name(), control.__call__)
        setattr(self, "help_" + control._name(), control.help)

    def loadplugins(self):
        """ Finds all plugins and gives them a chance to register
        themselves with the CLI instance """

        loc = {"register": self.register}

        plugins = OMEROCLI / "plugins"
        for plugin in plugins.walkfiles("*.py"):
            if DEBUG:
                print "Loading " + plugin
            if -1 == plugin.find("#"):
                try:
                    execfile( plugin, loc )
                except:
                    self.err("Error loading:"+plugin)
                    traceback.print_exc()

    ## End Cli
    ###########################################################

def argv(args=pysys.argv):
    """
    Main entry point for the OMERO command-line interface. First
    loads all plugins by passing them the classes defined here
    so they can register their methods.

    Then the case where arguments are passed on the command line are
    handled.

    Finally, the cli enters a command loop reading from standard in.
    """

    # Modifying the args list if the name of the file
    # has arguments encoded in it
    if args[0].find("-") >= 0:
        parts = args[0].split("-")
        for arg in args[1:]:
            parts.append(arg)
        args = parts

    cli = CLI()
    cli.loadplugins()

    if len(args) > 1:
        cli.invoke(args[1:])
    else:
        cli.invokeloop()

