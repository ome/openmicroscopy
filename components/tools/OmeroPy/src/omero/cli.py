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
OMERODIR = OMEROCLI.dirname().dirname()

#
# Possibilities:
#  - Always return and print any output
#  - Have a callback on the fired event
#  - switch register() to take a class.
#  - add an argument class which is always used at the top of a control
#    def somemethod(self, args): # args always assumed to be a shlex'd arg, but checked
#        arg = Argument(args)

def noneOneSome(none, one, some, args):
    """
    Dispatches to the appropriate method (none, one, or some)
    based on the contents of "args" according to the following:

      args == None    ==> none()
      len(args) == 0  ==> none()
      len(args) == 1  ==> one(args[0])
      otherwise       ==> some(args[0], args[1:])
    """
    if args == None or len(args) == 0: return none()
    elif len(args) == 1: return one(args[0])
    else: return some(args[0], args[1:])

def safePrint(text, stream):
    """
    Prints text to a given string, caputring any exceptions.
    """
    if not isinstance(text,str):
        if DEBUG:
            print >>pysys.stderr, "Received text argument of type "+str(type(text))
        text = str(text)
    try:
        print >>stream, (text % {"program_name": pysys.argv[0]})
    except:
        print >>pysys.stderr, "Error printing text"
        if DEBUG: traceback.print_exc()
        print >>pysys.stdout, text


class Context:
    """Simple context used for default logic. The CLI registry which registers
    the plugins installs itself as a fully functional Context.

    The Context class is designed to increase pluggability. Rather than
    making calls directly on other plugins directly, the pub() method
    routes messages to other commands. Similarly, out() and err() should
    be used for printing statements to the user, and die() should be
    used for exiting fatally.

    """

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

    def pub(self, args):
        print args

    def out(self, text):
        safePrint(text, pysys.stdout)

    def err(self, text):
        safePrint(text, pysys.stderr)

    def dbg(self, text):
        if DEBUG:
            self.err(text)

    def die(self, rc, args):
        raise exceptions.Exception((rc,args))

    def exit(self, args):
        self.out(args)
        raise exceptions.Exception("Normal exit")

    def popen(self, string):
        print string

    def conn(self):
        raise NotImplementedException()


class BaseControl:
    """Controls get registered with a CLI instance on loadplugins().

    To create a new control, subclass BaseControl, implement minimally
    _name(), and end your module with:

    c = MyControl()
    try:
        registry(c)
    except:
        c._main()

    This module should be put in the omero.plugins package.

    All methods which do NOT begin with "_" are assume to be accessible
    to CLI users.
    """

    ###############################################
    #
    # Mostly reusable code
    #
    def __init__(self, ctx = Context(), dir = OMERODIR):
        self.dir = path(dir) # Guaranteed to be a path
        self.ctx = ctx

    def _host(self):
        """
        Return hostname of current machine. Termed to be the
        value return from socket.gethostname() up to the first
        decimal.
        """
        if not hasattr(self, "hostname") or not self.hostname:
            self.hostname = socket.gethostname()
            if self.hostname.find(".") > 0:
                self.hostname = self.hostname.split(".")[0]
        return self.hostname

    def _node(self, omero_node = None):
        """
        Return the name of this node, using either the environment
        vairable OMERO_NODE or _host(). Some subclasses may
        override this functionality, most notably "admin" commands
        which assume a node name of "master".

        If the optional argument is not None, then the OMERO_NODE
        environment variable will be set.
        """
        if omero_node != None:
                os.environ["OMERO_NODE"] = omero_node

        if os.environ.has_key("OMERO_NODE"):
            return os.environ["OMERO_NODE"]
        else:
            return self._host()

    def _data(self):
        """
        Returns the data directory path for this node. This is determined
        from the "IceGrid.Node.Data" property in the _properties()
        map.

        The directory will be created if it does not exist.
        """
        try:
            nodedata = path(self._properties()["IceGrid.Node.Data"])
        except KeyError, ke:
            self.ctx.err("IceGrid.Node.Data is not configured")
            self.ctx.die(4, ke)

        if not nodedata.exists():
            self.ctx.out("Creating "+nodedata)
            nodedata.makedirs()
        return nodedata

    def _pid(self):
        """
        Returns a path of the form "_data() / _node() + ".pid",
        i.e. a file named NODENAME.pid in the node's data directory.
        """
        pidfile = self._data() / (self._node() + ".pid")
        return pidfile

    def _cfglist(self):
        """
        Returns a list of configuration files for this node. This
        defaults to the internal configuration for all nodes,
        followed by a file named NODENAME.cfg under the etc/
        directory.
        """
        cfgs = self.dir / "etc"
        internal = cfgs / "internal.cfg"
        owncfg = cfgs / self._node() + ".cfg"
        return (internal,owncfg)

    def _icecfg(self):
        """
        Uses _cfglist() to return a string argument of the form
        "--Ice.Config=..." suitable for passing to omero.client
        as an argument.
        """
        icecfg = "--Ice.Config=%s" % ",".join(self._cfglist())
        return icecfg

    def _properties(self, prefix=""):
        """
        Loads all files returned by _cfglist() into a new
        Ice.Properties instance and return the map from
        getPropertiesForPrefix(prefix) where the default is
        to return all properties.
        """
        if not hasattr(self, "_props") or self._props == None:
            self._props = Ice.createProperties()
            for cfg in self._cfglist():
                try:
                    self._props.load(str(cfg))
                except Exc, exc:
                    self.ctx.err("Could not find file: "+cfg)
                    self.ctx.die(3, exc)
        return self._props.getPropertiesForPrefix(prefix)

    ###############################################
    #
    # Methods likely to be implement by subclasses
    #
    def help(self):
        return """ Help not implemented """

    def _likes(self, args):
        """
        Checks whether or not it is likely for the given args
        to be run successfully by the given command. This is
        useful for plugins which have significant start up
        times.

        Simply return True is a possible solution. The default
        implementation checks that the subclass has a method
        matching the first argument, such that the default
        __call__() implementation could dispatch to it.
        """
        args = shlex(args)
        # Here we accept all commands of length zero since
        # there's a default implementation for that.
        if len(args) == 0 or hasattr(self,args[0]):
            return True
        return False

    def __call__(self, *args):
        """
        Main dispatch method for a control instance. The default
        implementation assumes that the *args consists of either
        no elements or exactly one list of strings ==> (["str"],)

        If no args are present, _noargs is called. If more than one
        vararg is present, an exception is raised. Otherwise, _noneSomeOne()
        is called with the shlexed array.
        """

        # For empty varargs
        if len(args) == 0:
            return self._noargs()
        elif len(args) == 1:
            none = lambda : self._noargs()
            one  = lambda x : self._onearg(x)
            some = lambda x,args : self._someargs(x,args)
            args = shlex(args[0])
            noneOneSome(none, one, some, args)
        else:
            self.ctx.die(6, "Don't know how to handle more than noargs or shlex args by default")

    def _noargs(self):
        """
        Method called when __call__() is called without any arguments. Some implementations
        may want to drop the user into a shell or read from standard in. By default, help()
        is printed.
        """
        self.help()

    def _onearg(self, arg):
       """
       Method called with __call__() gets an string list of length one.
       """
       if not self._likes([arg]):
           if DEBUG:
                raise Exc("Bad argument: " + arg)
           self.ctx.err("Bad argument: " + arg)
           self.help()
           self.ctx.die(7, "Exiting.")
       else:
           m = getattr(self, arg)
           return m()

    def _someargs(self, command, args):
       """
       Method called with __call__() gets an string list of length > 1. The first
       value is passed as command and [1:] of the list are passed as args.
       """
       if not self._likes(args):
           if DEBUG:
                raise Exc("Bad arguments: " + str(args))
           self.ctx.err("Bad arguments: " + ",".join(args))
           self.help()
           self.ctx.die(8, "Exiting.")
       else:
           m = getattr(self, args[0])
           return m(args[1:])

    def _name(self):
        """
        Required of all subclasses so that the CLI registry knows how to display the control"
        """
        raise NotImplementedException()

    def _main(self):
        """
        Simple _main() logic which is reusable by subclasses to do something when the control
        is executed directly. It is unlikely that such an excution will function properly,
        but it may be useful for testing purposes.
        """
        if __name__ == "__main__":
            if not self._likes(pysys.argv[1:]):
                self.help()
            else:
                self.__call__(pysys.argv[1:])

class CLI(cmd.Cmd, Context):
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
        self._controls = {}

    def invoke(self, line):
        """
        Copied from cmd.py
        """
        line = self.precmd(line)
        stop = True
        stop = self.onecmd(line)
        stop = self.postcmd(stop, line)

    def invokeloop(self):
        try:
            self.selfintro = TEXT
            if not self.stdin.isatty():
                self.selfintro = ""
                self.prompt = ""
            self.cmdloop(self.selfintro)
        except KeyboardInterrupt, ki:
            self.out("")
        self.interrupt_loop = True

    def precmd(self, input):
        if not isinstance(input,list):
            if COMMENT.match(input):
                return ""
        return input

    def onecmd(self, line):
        try:
            return cmd.Cmd.onecmd(self, line)
        except AttributeError, ae:
            self.err("Possible error in plugin:")
            self.err(ae)
        return False # Continue

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

    def default(self,arg):
        if arg.startswith("EOF"):
            self.exit("")
        else:
            self.err("Unkown command: " + arg)

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

    def do_help(self, args):
        args = shlex(args)
        if not args or len(args) == 0:
            print """OmeroCli client, version %(version)s

Usage: %(program_name)s <command> [options] args
See 'help <command>' for more information on syntax
Type 'quit' to exit

Available commands:
""" % {"program_name":pysys.argv[0],"version":VERSION}

            controls = list(self._controls)
            controls.sort()
            for name in controls:
                print """ %s""" % name
            print """
For additional information, see http://trac.openmicroscopy.org.uk/omero/wiki/OmeroCli"""
        elif not self._controls.has_key(args[0]):
            self.err("Unknown command:" + args[0])
        else:
            self._controls[args[0]].help()

    do_h = do_help

    def do_quit(self, arg):
        self.exit("")
    do_q = do_quit

    def do_version(self, arg):
        "print current version"
        print VERSION
    do_v = do_version

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
    def do_start(self, args):
        """
        Alias for "node start"
        """
        args = shlex(args)
        if not args:
            args = ["start"]
        else:
            args = ["start"] + args
        self.pub(args)

    ##########################################
    ##
    ## Context interface
    ##
    def exit(self, args):
        self.out(args)
        pysys.exit(0)

    def die(self, rc, args):
        self.err(args)
        pysys.exit(rc)

    def pub(self, args):
        """
        Publishes the command as given via noneOneSome logic
        """
        try:
            args = shlex(args)
            none = lambda: self.ctx.die(2, "Don't know what to do")
            one  = lambda c: self._controls[c]()
            some = lambda c, args: self._controls[c](args)
            noneOneSome(none, one, some, args)
        except KeyError, ke:
            self.die(11, "Missing required plugin: "+ str(ke))

    def popen(self, string):
        """
        Calls the string in a subprocess and dies if the return value is not 0
        """
        rv = subprocess.call(string)
        if not rv == 0:
            self.die(rv, "Error during:\"%s\"" % string )
        return rv

    def conn(self, properties={}, profile=None):
        """
        Either creates or returns the exiting omero.client instance.

        Usess "omero prefs" to property configure the client.
        """

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
        control.ctx = self
        self._controls[control._name()] = control
        setattr(self, "do_" + control._name(), control.__call__)

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

