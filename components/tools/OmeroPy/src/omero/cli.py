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

import cmd, string, re, os, sys, subprocess, socket, exceptions, traceback, glob
from omero_ext import pysys
import shlex as pyshlex
from exceptions import Exception as Exc
from path import path
import Ice

#
# Static setup
#

VERSION=1.0
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

COMMENT = re.compile("^\s*#")
RELFILE = re.compile("^\w")
LINEWSP = re.compile("^\s*\w+\s+")

#
# Possibilities:
#  - Always return and print any output
#  - Have a callback on the fired event
#  - switch register() to take a class.
#  - how should state machine work?
#   -- is the last control stored somwhere? in a stack history[3]
#   -- or do they all share a central memory? self.ctx["MY_VARIABLE"]
#  - add an argument class which is always used at the top of a control
#    def somemethod(self, args): # args always assumed to be a shlex'd arg, but checked
#        arg = Argument(args)
#  - In almost all cases, mark a flag in the CLI "lastError" and continue,
#    allowing users to do something of the form: on_success or on_fail


#####################################################
#
# Exceptions
#
class NonZeroReturnCode(Exc):
    def __init__(self, rv, *args):
        self.rv = rv
        Exc.__init__(self, *args)


#####################################################
#
class Arguments:
    """
    Wrapper for arguments in all controls. All non-"_" control methods are
    assumed to take some representation of the command line. This can either
    be:

        - the line as a string
        - the shlex'd line as a string list

    To simplify usage, this class can be used at the beginning of every
    method so:

        def method(self, args):
            args = Arguments(args)

    and it will handle the above cases as well as wrapping other Argument
    instances. If the method takes varargs and it is desired to test for
    single argument of the above type, then use:

        args = Arguments(*args)

    """

    def __init__(self, args = []):
        if args == None:
            self.args = []
        elif isinstance(args, Arguments):
            self.args = args.args
        elif isinstance(args, str):
            self.args = self.shlex(args)
        elif isinstance(args, list):
            for l in args:
                assert isinstance(l, str)
            self.args = args
        else:
            raise exceptions.Exception("Unknown argument: %s" % args)

    def firstOther(self):
        if len(self.args) == 0:
            return (None,[])
        elif len(self.args) == 1:
            return (self.args[0], [])
        else:
            return (self.args[0], self.args[1:])

    def popFirst(self):
        return self.args.pop(0)

    def shlex(self, input):
        """
        Used to split a string argument via shlex.split(). If the
        argument is not a string, then it is returned unchnaged.
        This is useful since the arg argument to all plugins can
        be either a list or a string.
        """
        if None == input:
            return []
        elif isinstance(input, str):
            return pyshlex.split(input)
        else:
            return input

    #######################################
    #
    # Usability methods
    #
    def __iter__(self):
        return iter(self.args)
    def __len__(self):
        return len(self.args)
    def __str__(self):
        return ", ".join(self.args)
    def join(self, text):
        return text.join(self.args)

#####################################################
#
class Context:
    """Simple context used for default logic. The CLI registry which registers
    the plugins installs itself as a fully functional Context.

    The Context class is designed to increase pluggability. Rather than
    making calls directly on other plugins directly, the pub() method
    routes messages to other commands. Similarly, out() and err() should
    be used for printing statements to the user, and die() should be
    used for exiting fatally.

    """

    def __init__(self, controls = {}):
        self.controls = controls

    def safePrint(self, text, stream):
        """
        Prints text to a given string, caputring any exceptions.
        """
        try:
            print >>stream, (text % {"program_name": pysys.argv[0]})
        except:
            print >>pysys.stderr, "Error printing text"
            print >>pysys.stdout, text
            if DEBUG:
                traceback.print_exc()

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
        self.safePrint(str(args), pysys.stdout)

    def out(self, text):
        """
        Expects as single string as argument"
        """
        self.safePrint(text, pysys.stdout)

    def err(self, text):
        """
        Expects a single string as argument.
        """
        self.safePrint(text, pysys.stderr)

    def dbg(self, text):
        """
        Passes text to err() if DEBUG is set
        """
        if DEBUG:
            self.err(text)

    def die(self, rc, args):
        raise exceptions.Exception((rc,args))

    def exit(self, args):
        self.out(args)
        self.interrupt_loop = True

    def popen(self, args):
        self.out(str(args))

    def conn(self):
        raise NotImplementedException()



#####################################################
#
class BaseControl:
    """Controls get registered with a CLI instance on loadplugins().

    To create a new control, subclass BaseControl and end your module with:

    try:
        registry("name", MyControl)
    except:
        MyControl()._main()

    This module should be put in the omero.plugins package.

    All methods which do NOT begin with "_" are assumed to be accessible
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

    def _icedata(self, property):
        """
        General data for getting an creating a path from
        an Ice property.
        """
        try:
            nodepath = self._properties()[property]

            if RELFILE.match(nodepath):
                nodedata = OMERODIR / path(nodepath)
            else:
                nodedata = path(nodepath)

            created = False
            if not nodedata.exists():
                self.ctx.out("Creating "+nodedata)
                nodedata.makedirs()
                created = True
            return (nodedata, created)

        except KeyError, ke:
            self.ctx.err(property + " is not configured")
            self.ctx.die(4, str(ke))

    def _nodedata(self):
        """
        Returns the data directory path for this node. This is determined
        from the "IceGrid.Node.Data" property in the _properties()
        map.

        The directory will be created if it does not exist.
        """
        data, created = self._icedata("IceGrid.Node.Data")
        return data

    def _regdata(self):
        """
        Returns the data directory for the IceGrid registry.
        This is determined from the "IceGrid.Registry.Data" property
        in the _properties() map.

        The directory will be created if it does not exist, and
        a warning issued.
        """
        data, created = self._icedata("IceGrid.Registry.Data")
        if created:
            self.ctx.out("""
  Warning:
  %s,
  the IceGrid.Registry.Data directory is not present.
  This is the first time you've started OmeroGrid.

  No servers have been deployed yet. To do so, you
  will need to run "admin deploy" after the following
  initialization. See the files under etc/grid/ for
  example application descriptors.

  This warning will not be shown again.
            """ % data)

    def _pid(self):
        """
        Returns a path of the form "_nodedata() / _node() + ".pid",
        i.e. a file named NODENAME.pid in the node's data directory.
        """
        pidfile = self._nodedata() / (self._node() + ".pid")
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

    def _intcfg(self):
        """
        Returns an Ice.Config string with only the internal configuration
        file for connecting to the IceGrid Locator.
        """
        intcfg = self.dir / "etc" / "internal.cfg"
        intcfg.abspath()
        return "--Ice.Config=%s" % intcfg

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
                    self.ctx.die(3, "Could not find file: "+cfg + "\nDid you specify the proper node?")
        return self._props.getPropertiesForPrefix(prefix)

    ###############################################
    #
    # Methods likely to be implemented by subclasses
    #
    def help(self, args = []):
        return """ Help not implemented """

    def _complete(self, text, line, begidx, endidx):
        try:
            import readline
            # import rlcompleter
        except ImportError, ie:
            self.ctx.err("No readline")
            return []

        # readline.parse_and_bind("tab: complete")
        # readline.set_completer_delims(' \t\n`~!@#$%^&*()-=+[{]}\\|;:\'",<>;?')

        completions = [method for method in dir(self) if callable(getattr(self, method)) ]
        completions = [ str(method + " ") for method in completions if method.startswith(text) and not method.startswith("_") ]
        return completions

    def _likes(self, args):
        """
        Checks whether or not it is likely for the given args
        to be run successfully by the given command. This is
        useful for plugins which have significant start up
        times.

        Simply return True is a possible solution. The default
        implementation checks that the subclass has a method
        matching the first argument, such that the default
        __call__() implementation could dispatch to it. Or if
        no arguments are given, True is returned since self._noargs()
        can be called.
        """
        args = Arguments(args)
        first, other = args.firstOther()
        if first == None or hasattr(self, first):
            return True
        return False

    def __call__(self, *args):
        """
        Main dispatch method for a control instance. The default
        implementation assumes that the *args consists of either
        no elements or exactly one list of strings ==> (["str"],)

        If no args are present, _noargs is called. Subclasses may want
        to read from stdin or drop into a shell from _noargs().

        Otherwise, the rest of the arguments are passed to the method
        named by the first argument, if _likes() returns True.
        """
        args = Arguments(*args)
        first,other = args.firstOther()
        if first == None:
            self._noargs()
        else:
            if not self._likes(args):
                if DEBUG:
                    # Throwing an exception
                    # so we can see how we got here.
                    raise Exc("Bad arguments: " + str(args))
                self.ctx.err("Bad arguments: " + ",".join(args))
                self.help()
                self.ctx.die(8, "Exiting.")
            else:
                m = getattr(self, first)
                return m(other)

    def _noargs(self):
        """
        Method called when __call__() is called without any arguments. Some implementations
        may want to drop the user into a shell or read from standard in. By default, help()
        is printed.
        """
        self.help()

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
        Context.__init__(self)
        self.prompt = 'omero> '
        self.interrupt_loop = False
        self._client = None
        self.rv = 0 # Return value to be returned

    def invoke(self, line):
        """
        Copied from cmd.py
        """
        line = self.precmd(line)
        stop = self.onecmd(line)
        stop = self.postcmd(stop, line)

    def invokeloop(self):
        self.selfintro = TEXT
        if not self.stdin.isatty():
            self.selfintro = ""
            self.prompt = ""
        while not self.interrupt_loop:
            try:
                self.cmdloop(self.selfintro)
            except KeyboardInterrupt, ki:
                # We've done the intro once now. Don't repeat yourself.
                self.selfintro = ""
                try:
                    import readline
                    if len(readline.get_line_buffer()) > 0:
                        self.out("")
                    else:
                        self.out("Use quit to exit")
                except ImportError:
                    self.out("Use quit to exit")

    def precmd(self, input):
        if not isinstance(input,list):
            if COMMENT.match(input):
                return ""
        return input

    def onecmd(self, line):
        try:
            # Starting a new command. Reset the return value to 0
            # If err or die are called, set rv non-0 value
            self.rv = 0
            return cmd.Cmd.onecmd(self, line)
        except AttributeError, ae:
            self.err("Possible error in plugin:")
            self.err(str(ae))
            if DEBUG:
                traceback.print_exc()
        except NonZeroReturnCode, nzrc:
            self.rv = nzrc.rv
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
            self.err("Unknown command: " + arg)

    def completenames(self, text, line, begidx, endidx):
        names = self.controls.keys()
        return [ str(n + " ") for n in names if n.startswith(line) ]

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
        self.interrupt_loop = True

    def die(self, rc, text):
        self.err(text)
        self.rv = rc
        self.interrupt_loop = True
        raise NonZeroReturnCode(rc, "die called")

    def pub(self, args):
        """
        Publishes the command, using the first argument as routing
        information, i.e. the name of the plugin to be instantiated,
        and the rest as the arguments to its __call__() method.
        """
        try:
            args = Arguments(args)
            first, other = args.firstOther()
            if first == None:
                self.ctx.die(2, "No plugin given. Giving up")
            else:
                control = self.controls[first]
                control(other)
        except KeyError, ke:
            self.die(11, "Missing required plugin: "+ str(ke))

    def popen(self, args, strict = True):
        """
        Calls the string in a subprocess and dies if the return value is not 0
        """
        rv = subprocess.call(args, env = os.environ)
        if strict and not rv == 0:
            raise NonZeroReturnCode(rv, "%s => %d" % (" ".join(args), rv))
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
           if len(parts) == 2:
               data.properties.setProperty(parts[0],parts[1])
           else:
               if DEBUG:
                   self.err("Bad property:"+str(parts))

        import omero
        try:
            self._client = omero.client(pysys.argv, id = data)
            self._client.createSession()
            return self._client
        except Exc, exc:
            self._client = None
            raise exc

    ##
    ## Plugin registry
    ##

    def register(self, name, Control):
        """ This method is added to the globals when execfile() is
        called on each plugin. An instance of the control should be
        passed to the register method which will be added to the CLI.
        """

        class Wrapper:
            def __init__(self, ctx, control):
                self.ctx = ctx
                self.Control = Control
                self.control = None
            def _setup(self):
                if self.control == None:
                    self.control = self.Control(ctx = self.ctx)
            def do_method(self, *args):
                try:
                    self._setup()
                    return self.control.__call__(*args)
                except NonZeroReturnCode, nzrc:
                    raise
                except Exc, exc:
                    if DEBUG:
                        traceback.print_exc()
                    self.ctx.err("Error:"+str(exc))
            def complete_method(self, *args):
                try:
                    self._setup()
                    return self.control._complete(*args)
                except Exc, exc:
                    self.ctx.err("Completion error:"+str(exc))
            def __call__(self, *args):
                """
                If the wrapper gets treated like the control
                instance, and __call__()'d, then pass the *args
                to do_method()
                """
                return self.do_method(*args)

        wrapper = Wrapper(self, Control)
        self.controls[name] = wrapper
        setattr(self, "do_" + name, wrapper.do_method)
        setattr(self, "complete_" + name, wrapper.complete_method)

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

    # Modiying the run-time environment
    old_ice_config = os.getenv("ICE_CONFIG")
    os.unsetenv("ICE_CONFIG")
    try:

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
            return cli.rv
        else:
            cli.invokeloop()
            return cli.rv
    finally:
        if old_ice_config:
            os.putenv("ICE_CONFIG", old_ice_config)
