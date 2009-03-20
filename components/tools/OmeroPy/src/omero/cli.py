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

import cmd, string, re, os, sys, subprocess, socket, exceptions, traceback, glob, platform, time
import shlex as pyshlex
from exceptions import Exception as Exc
from threading import Thread, Lock
from omero_version import omero_version
from omero_ext import pysys
from path import path
import Ice

#
# Static setup
#

VERSION=omero_version
DEBUG = False
if os.environ.has_key("DEBUG"):
    print "Deprecated warning: use the 'bin/omero debug [args]' to debug"
    print "Running omero with debugging on"
    DEBUG = True
TEXT="""
  OMERO Python Shell. Version %s
  Type "help" for more information, "quit" or Ctrl-D to exit
""" % str(VERSION)

OMEROCLI = path(__file__).expand().dirname()
OMERODIR = OMEROCLI.dirname().dirname().dirname()

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
            self.argmap = {}
        elif isinstance(args, Arguments):
            self.args = args.args
            self.argmap = args.argmap
        elif isinstance(args, str):
            self.args = self.shlex(args)
            self.make_argmap()
        elif isinstance(args, list):
            for l in args:
                assert isinstance(l, str)
            self.args = args
            self.make_argmap()
        else:
            raise exceptions.Exception("Unknown argument: %s" % args)

    def make_argmap(self):
        self.argmap = {}
        for arg in self.args:
            parts = arg.split("=", 1)
            if len(parts) == 1:
                self.argmap[parts[0]] = True
            else:
                self.argmap[parts[0]] = parts[1]

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
    def __getitem__(self, idx):
        """
        For every argument without an "=" we return True. Otherwise,
        the value following the first "=" is returned.
        """
        return self.argmap[idx]

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
        self.dir = OMERODIR
        self.isdebug = DEBUG # This usage will go away and default will be False

    def setdebug(self):
        self.isdebug = True

    def safePrint(self, text, stream, newline = True):
        """
        Prints text to a given string, caputring any exceptions.
        """
        try:
            stream.write(text % {"program_name": pysys.argv[0]})
            if newline:
                stream.write("\n")
            else:
                stream.flush()
        except:
            print >>pysys.stderr, "Error printing text"
            print >>pysys.stdout, text
            if self.isdebug:
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

    def userdir(self):
        """
        Returns a user directory (as path.path) which can be used
        for storing configuration. The directory is guaranteed to
        exist and be private (700) after execution.
        """
        dir = path(os.path.expanduser("~")) / "omero" / "cli"
        if not dir.exists():
            dir.mkdir()
        elif not dir.isdir():
            raise Exc("%s is not a directory"%dir)
        dir.chmod(0700)
        return dir

    def pub(self, args):
        self.safePrint(str(args), pysys.stdout)

    def input(self, prompt, hidden = False):
        """
        Reads from standard in. If hidden == True, then
        uses getpass
        """
        if hidden:
            import getpass
            defuser = getpass.getuser()
            return getpass.getpass(prompt)
        else:
            return raw_input(prompt)

    def out(self, text, newline = True):
        """
        Expects as single string as argument"
        """
        self.safePrint(text, pysys.stdout, newline)

    def err(self, text, newline = True):
        """
        Expects a single string as argument.
        """
        self.safePrint(text, pysys.stderr, newline)

    def dbg(self, text, newline = True):
        """
        Passes text to err() if self.isdebug is set
        """
        if self.isdebug:
            self.err(text, newline)

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

    def _isWindows(self):
        p_s = platform.system()
        if p_s == 'Windows':
                return True
        else:
                return False

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
        General data method for creating a path from an Ice property.
        """
        try:
            nodepath = self._properties()[property]

            if RELFILE.match(nodepath):
                nodedata = self.dir / path(nodepath)
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

    def _initDir(self):
        """
        Initialize the directory into which the current node will log.
        """
        props = self._properties()
        nodedata = self._nodedata()
        logdata = self.dir / path(props["Ice.StdOut"]).dirname()
        if not logdata.exists():
            self.ctx.out("Initializing %s" % logdata)
            logdata.makedirs()


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
        directory, following by PLATFORM.cfg if it exists.
        """
        cfgs = self.dir / "etc"
        internal = cfgs / "internal.cfg"
        owncfg = cfgs / self._node() + ".cfg"
        results = [internal,owncfg]
        # Look for <platform>.cfg
        p_s = platform.system()
        p_c = cfgs / p_s + ".cfg"
        if p_c.exists():
            results.append(p_c)
        return results

    def _icecfg(self):
        """
        Uses _cfglist() to return a string argument of the form
        "--Ice.Config=..." suitable for passing to omero.client
        as an argument.
        """
        icecfg = "--Ice.Config=%s" % ",".join(self._cfglist())
        return str(icecfg)

    def _intcfg(self):
        """
        Returns an Ice.Config string with only the internal configuration
        file for connecting to the IceGrid Locator.
        """
        intcfg = self.dir / "etc" / "internal.cfg"
        intcfg.abspath()
        return str("--Ice.Config=%s" % intcfg)

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
                if self.ctx.isdebug:
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

class HelpControl(BaseControl):
    """
    Defined here since the background loading might be too
    slow to have all help available
    """

    def _complete(self, text, line, begidx, endidx):
        """
        This is something of a hack. This should either be a part
        of the context interface, or we should put it somewhere
        in a utility. FIXME.
        """
        return self.ctx.completenames(text, line, begidx, endidx)

    def help(self, args = None):
        self.out("Print help")

    def __call__(self, *args):

        args = Arguments(*args)
        first, other = args.firstOther()

        self.ctx.waitForPlugins()
        controls = self.ctx.controls.keys()
        controls.sort()

        if not first:
            print """OmeroCli client, version %(version)s

Usage: %(program_name)s <command> [options] args
See 'help <command>' for more information on syntax
Type 'quit' to exit

Available commands:
""" % {"program_name":pysys.argv[0],"version":VERSION}

            for name in controls:
                print """ %s""" % name
            print """
For additional information, see http://trac.openmicroscopy.org.uk/omero/wiki/OmeroCli"""

        else:
            try:
                self.ctx.controls[first].help_method()
                ##event = [first, "help"]
                ##event.extend(other)
                ##self.ctx.pub(event)
            except KeyError, ke:
                self.ctx.err("Unknown command:" + first)

class CLI(cmd.Cmd, Context):
    """
    Command line interface class. Supports various styles of executing the
    registered plugins. Each plugin is given the chance to update this class
    by adding methods of the form "do_<plugin name>".
    """

    class PluginsLoaded(object):
        """
        Thread-safe class for storing whether or not all the plugins
        have been loaded
        """
        def __init__(self):
            self.lock = Lock()
            self.done = False
        def get(self):
            self.lock.acquire()
            try:
                return self.done
            finally:
                self.lock.release()
        def set(self):
            self.lock.acquire()
            try:
                self.done = True
            finally:
                self.lock.release()

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
        self._pluginsLoaded = CLI.PluginsLoaded()
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
        if isinstance(input,str):
            if COMMENT.match(input):
                return ""
        return input

    def onecmd(self, line):
        args = Arguments(line)
        try:
            # Starting a new command. Reset the return value to 0
            # If err or die are called, set rv non-0 value
            self.rv = 0
            return cmd.Cmd.onecmd(self, args)
        except AttributeError, ae:
            self.err("Possible error in plugin:")
            self.err(str(ae))
            if self.isdebug:
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
                return (line[0],line[1:],Arguments(line))
        elif isinstance(line, Arguments):
            first,other = line.firstOther()
            return (first, other, line)
        else:
            return cmd.Cmd.parseline(self,line)

    def default(self,arg):
        arg = Arguments(arg)
        try:
            arg["EOF"]
            self.exit("")
        except KeyError:
            first, other = arg.firstOther()
            file = OMEROCLI / "plugins" / (first + ".py")
            loc = {"register": self.register}
            try:
                execfile( str(file), loc )
            except Exc, ex:
                self.dbg("Could not load %s: %s" % (first, ex))
                self.waitForPlugins()

            if self.controls.has_key(first):
                return self.invoke(arg.args)
            else:
                self.err("Unknown command: " + arg.join(" "))

    def completenames(self, text, line, begidx, endidx):
        names = self.controls.keys()
        return [ str(n + " ") for n in names if n.startswith(line) ]

    # Delegation
    def do_start(self, args):
        """
        Alias for "node start"
        """
        args = pyshlex.split(args)
        if not args:
            args = ["node","start"]
        else:
            args = ["node","start"] + args
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

    def popen(self, args, strict = True, stdout = None):
        """
        Calls the string in a subprocess and dies if the return value is not 0
        """
        if not stdout:
            self.dbg("Executing: %s" % args)
            rv = subprocess.call(args, env = os.environ, cwd = OMERODIR)
            if strict and not rv == 0:
                raise NonZeroReturnCode(rv, "%s => %d" % (" ".join(args), rv))
            return rv
        else:
            self.dbg("Returning popen: %s" % args)
            return subprocess.Popen(args, env = os.environ, cwd = OMERODIR, stdout = subprocess.PIPE)

    def readDefaults(self):
        try:
            f = path(OMERODIR) / "etc" / "omero.properties"
            f = f.open()
            output = "".join(f.readlines())
            f.close()
        except:
            if self.isdebug:
                raise
            print "No omero.properties found"
            output = ""
        return output

    def parsePropertyFile(self, data, output):
        for line in output.splitlines():
            if line.startswith("Listening for transport dt_socket at address"):
                self.dbg("Ignoring stdout 'Listening for transport' from DEBUG=1")
                continue
            parts = line.split("=",1)
            if len(parts) == 2:
                data.properties.setProperty(parts[0],parts[1])
                self.dbg("Set property: %s=%s" % (parts[0],parts[1]) )
            else:
                self.dbg("Bad property:"+str(parts))
        return data

    def initData(self, properties={}):
        """
        Uses "omero prefs" to create an Ice.InitializationData().
        """
        from omero.plugins.prefs import getprefs
        output = getprefs(["get"], str(OMERODIR / "lib"))

        import Ice
        data = Ice.InitializationData()
        data.properties = Ice.createProperties()
        self.parsePropertyFile(data, output)
        return data


    def conn(self, properties={}, profile=None):
        """
        Either creates or returns the exiting omero.client instance.
        Uses the comm() method with the same signature.
        """

        if self._client:
            return self._client

        import omero
        try:
            data = self.initData(properties)
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
                    if self.ctx.isdebug:
                        traceback.print_exc()
                    ## Prevent duplication - self.ctx.err("Error:"+str(exc))
                    self.ctx.die(10, str(exc))
            def complete_method(self, *args):
                try:
                    self._setup()
                    return self.control._complete(*args)
                except Exc, exc:
                    self.ctx.err("Completion error:"+str(exc))
            def help_method(self, *args):
                try:
                    self._setup()
                    return self.control.help(*args)
                except Exc, exc:
                    self.ctx.err("Help error:"+str(exc))
            def __call__(self, *args):
                """
                If the wrapper gets treated like the control
                instance, and __call__()'d, then pass the *args
                to do_method()
                """
                return self.do_method(*args)

        wrapper = Wrapper(self, Control)
        setattr(self, "do_" + name, wrapper.do_method)
        setattr(self, "complete_" + name, wrapper.complete_method)
        setattr(self, "help_" + name, wrapper.help_method)
        self.controls[name] = wrapper

    def waitForPlugins(self):
        self.dbg("Starting waitForPlugins")
        while not self._pluginsLoaded.get():
            self.dbg("Waiting for plugins...")
            time.sleep(0.1)

    def loadplugins(self):
        """ Finds all plugins and gives them a chance to register
        themselves with the CLI instance """

        loc = {"register": self.register}

        plugins = OMEROCLI / "plugins"
        for plugin in plugins.walkfiles("*.py"):
            if self.isdebug:
                print "Loading " + plugin
            if -1 == plugin.find("#"): # Omit emacs files
                try:
                    execfile( plugin, loc )
                except:
                    self.err("Error loading:"+plugin)
                    traceback.print_exc()
        self._pluginsLoaded.set()

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
        executable = path(args[0])
        executable = str(executable.basename())
        if executable.find("-") >= 0:
            parts = executable.split("-")
            for arg in args[1:]:
                parts.append(arg)
            args = parts

        cli = CLI()
        cli.register("help", HelpControl)
        class PluginLoader(Thread):
            def run(self):
                cli.loadplugins()
	# Disabling background loading
	# until 2.4 hangs are fixed
        PluginLoader().run() # start()

        if len(args) > 1:
            cli.invoke(args[1:])
            return cli.rv
        else:
            cli.invokeloop()
            return cli.rv
    finally:
        if old_ice_config:
            os.putenv("ICE_CONFIG", old_ice_config)
