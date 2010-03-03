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

sys = __import__("sys")

import cmd, string, re, os, subprocess, socket, exceptions, traceback, glob, platform, time
import shlex as pyshlex
from exceptions import Exception as Exc
from threading import Thread, Lock
from omero_version import omero_version
from omero_ext.argparse import ArgumentError, ArgumentParser, SUPPRESS
from omero.util.sessions import SessionsStore
from path import path

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
OMERODIR = os.getenv('OMERODIR', None)
if OMERODIR is not None:
    OMERODIR = path(OMERODIR)
else:
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
#        arg = Arguments(args)
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

class BadArgument(Exc): pass


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
    method so::

        def method(self, *args):
            args = Arguments(args)

    and it will handle the above cases as well as wrapping other Argument
    instances.

    """

    def __init__(self, args = [], shortopts = None, longopts = None):

        original = args

        self.argmap = {}
        self.opts = {}
        self.longopts = longopts
        self.shortopts = shortopts

        if args == None:
            self.args = []
        elif isinstance(args, Arguments):
            self._copy_args(args)
            opts, self.args = self._argparse(self.args, shortopts, longopts)
            self.opts.update(opts)
        elif isinstance(args, (str, unicode)):
            self.args = self.shlex(args)
            self._apply_opts()
            self._make_argmap()
        elif isinstance(args, (list, tuple)):
            skip = False
            if len(args) == 1:
                if isinstance(args[0], (list, tuple)):
                    args = args[0] # Unwrap if necessary
                for l in args:
                    if isinstance(l, (str, unicode)):
                        pass
                    elif isinstance(l, Arguments):
                        self._copy_args(l)
                        skip = True
                        continue
                    else:
                        raise BadArgument("arg not string: %s in %s" % (l, args))
            if not skip:
                self.args = list(args)
                self._apply_opts()
                self._make_argmap()
        else:
            raise BadArgument("Unknown argument: %s" % args)
        if DEBUG:
            print """
    Current arguments:
       Passed:    %s, %s, %s
       Arguments: %s
       Options:   %s
       Map:       %s
            """ % (original, shortopts, longopts, self.args, self.opts, self.argmap)

    def _copy_args(self, args):
        self.longopts = args.longopts
        self.shortopts = args.shortopts
        self.opts = dict(args.opts)
        self.args = list(args.args)
        self.argmap = dict(args.argmap)

    def _apply_opts(self):

        if self.longopts == None:
            self.longopts = []
        if not 'quiet' in self.longopts:
            self.longopts = ['quiet'] + self.longopts

        if self.shortopts == None:
            self.shortopts = ''
        if not 'q' in self.shortopts:
            self.shortopts = 'q' + self.shortopts

        lloginopts = ["create", "server=", "port=", "user=", "key=", "password="]
        sloginopts = "Cs:p:u:k:w:"

        for l in lloginopts:
            if l in self.longopts:
                raise BadArgument("Duplicate longopt: %s", l)

        for s in sloginopts:
            if s in self.shortopts and s != ":":
                raise BadArgument("Duplicate shortopt: %s", s)

        self.longopts = lloginopts + self.longopts
        self.shortopts = "%s%s" % (sloginopts, self.shortopts)
        self.opts, self.args = self._argparse(self.args, self.shortopts, self.longopts)

    def _argparse(self, args, shortopts, longopts):

        try:
            # TODO: currently adapting getopt like parameters
            # to use argparse, but a better way to use argparse
            # may be to replace this class completely!
            #
            a = ArgumentParser()

            if shortopts is not None:
                i = 0
                while i < len(shortopts):
                    o = shortopts[i]
                    c = "store_true"
                    try:
                        if shortopts[i+1] == ":":
                            i += 1
                            c = "store"
                    except IndexError:
                        pass
                    a.add_argument("-%s" % o, default=SUPPRESS, action=c, required=False)
                    i += 1

            if longopts is not None:
                for o in longopts:
                    c = "store_true"
                    if o.endswith("="):
                        o = o[:-1]
                        c = "store"
                    a.add_argument("--%s" % o, default=SUPPRESS, action=c, required=False)

            ns, args = a.parse_known_args(args)
            opts = dict(ns._get_kwargs())
            return opts, args
        except ArgumentError, ae:
            raise BadArgument(ae)

    def _make_argmap(self):
        for arg in self.args:
            parts = arg.split("=", 1)
            if parts[0] in self.argmap:
                raise BadArgument("Argument overwrite: %s" % parts[0])
            if len(parts) == 1:
                self.argmap[parts[0]] = True
            else:
                self.argmap[parts[0]] = parts[1]

    def is_quiet(self):
        return "q" in self.opts or "quiet" in self.opts

    def firstOther(self):
        """
        Returns the first non-opts argument to this instance as a string,
        and a second Arguments object with that first argument removed.
        """
        other = Arguments(self)
        if len(self.args) == 0:
            return (None, other)

        first = self.args[0]
        other.popFirst()
        return (first, other)

    def popFirst(self):
        rv = self.args.pop(0)
        self.argmap.pop(rv)
        return rv

    def insert(self, idx, key, value = True):
        """
        Allows to undo the effects of popFirst or firstOther
        """
        self.args.insert(idx, key)
        self.argmap[key] = value

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
    # Usability methods :
    # Allow an Argument instance to be used
    # more like a list and a dict
    #
    def get(self, key, defvalue):
        return self.argmap.get(key, defvalue)
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

    def getBool(self, key, defvalue):
        value = self.get(key, defvalue)
        value = str(value).lower()
        return value in ("true", "yes", "1")

    def getInt(self, key, defvalue):
        value = self.get(key, defvalue)
        if value is None:
            return value
        else:
            value = int(value)
            return value

    #######################################
    #
    # Login methods
    #

    def is_arg(self, long, short):
        return long in self.opts or short in self.opts

    def get_arg(self, long, short):
        rv = self.opts.get(long, None)
        if not rv:
            rv = self.opts.get(short, None)
        return rv

    def is_create(self): return self.is_arg("create", "C")
    def get_server(self): return self.get_arg("server", "s")
    def get_port(self): return self.get_arg("port", "p")
    def get_user(self): return self.get_arg("user", "u")
    def get_password(self): return self.get_arg("password", "w")
    def get_key(self): return self.get_arg("key", "k")

    def as_props(self):
        props = {}
        srv = self.get_server()
        prt = self.get_port()
        key = self.get_key()
        usr = self.get_user()
        psw = self.get_password()

        if srv: props["omero.host"] = srv
        if prt: props["omero.port"] = prt
        if key:
            props["omero.user"] = key
            props["omero.pass"] = key
        else:
            if usr: props["omero.user"] = usr
            if psw: props["omero.pass"] = psw

    def as_args(self):
        args = []
        srv = self.get_server()
        prt = self.get_port()
        key = self.get_key()
        usr = self.get_user()
        psw = self.get_password()

        if self.is_create(): args.append("-C")
        if srv: args.extend(["-s", srv])
        if prt: args.extend(["-p", prt])
        if key: args.extend(["-k", key])
        if usr: args.extend(["-u", usr])
        if psw: args.extend(["-w", psw])

        return args

    def acquire(self, ctx):
        """
        If passed a context object, will use the current settings to connect.
        If required attributes are missing, will delegate to the login command.
        """
        ctx.pub(["sessions", "login"] + self.as_args())


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

    def __init__(self, controls = {}, params = {}):
        self.params = {}
        self.controls = controls
        self.dir = OMERODIR
        self.isdebug = DEBUG # This usage will go away and default will be False

    def get(self, key, defvalue = None):
        return self.params.get(key, defvalue)

    def set(self, key, value = True):
        self.params[key] = value

    def setdebug(self):
        self.isdebug = True

    def safePrint(self, text, stream, newline = True):
        """
        Prints text to a given string, caputring any exceptions.
        """
        try:
            stream.write(text % {"program_name": sys.argv[0]})
            if newline:
                stream.write("\n")
            else:
                stream.flush()
        except:
            print >>sys.stderr, "Error printing text"
            print >>sys.stdout, text
            if self.isdebug:
                traceback.print_exc()

    def pythonpath(self):
        """
        Converts the current sys.path to a PYTHONPATH string
        to be used by plugins which must start a new process.

        Note: this was initially created for running during
        testing when PYTHONPATH is not properly set.
        """
        path = list(sys.path)
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

    def pub(self, args, strict = False):
        self.safePrint(str(args), sys.stdout)

    def input(self, prompt, hidden = False, required = False):
        """
        Reads from standard in. If hidden == True, then
        uses getpass
        """
        try:
            while True:
                if hidden:
                    import getpass
                    defuser = getpass.getuser()
                    rv = getpass.getpass(prompt)
                else:
                    rv = raw_input(prompt)
                if required and not rv:
                    self.out("Input required")
                    continue
                return rv
        except KeyboardInterrupt:
            self.die(1, "Cancelled")

    def out(self, text, newline = True):
        """
        Expects as single string as argument"
        """
        self.safePrint(text, sys.stdout, newline)

    def err(self, text, newline = True):
        """
        Expects a single string as argument.
        """
        self.safePrint(text, sys.stderr, newline)

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

    def call(self, args):
        self.out(str(args))

    def popen(self, args):
        self.out(str(args))


#####################################################
#
class BaseControl:
    """Controls get registered with a CLI instance on loadplugins().

    To create a new control, subclass BaseControl and end your module with::

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
        import Ice
        if not hasattr(self, "_props") or self._props == None:
            self._props = Ice.createProperties()
            for cfg in self._cfglist():
                try:
                    self._props.load(str(cfg))
                except Exc, exc:
                    self.ctx.die(3, "Could not find file: "+cfg + "\nDid you specify the proper node?")
        return self._props.getPropertiesForPrefix(prefix)

    def _ask_for_password(self, reason = "", root_pass = None):
        while not root_pass or len(root_pass) < 1:
            root_pass = self.ctx.input("Please enter password%s: "%reason, hidden = True)
            if root_pass == None or root_pass == "":
                self.ctx.err("Password cannot be empty")
                continue
            confirm = self.ctx.input("Please re-enter password%s: "%reason, hidden = True)
            if root_pass != confirm:
                root_pass = None
                self.ctx.err("Passwords don't match")
                continue
            break
        return root_pass

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
        args = Arguments(args)
        first, other = args.firstOther()
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
            if not self._likes(sys.argv[1:]):
                self.help()
            else:
                self.__call__(sys.argv[1:])

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

        args = Arguments(args)
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
""" % {"program_name":sys.argv[0],"version":VERSION}

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
                self.ctx.unknown_command(first)

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
        maintains a single active client. The "session" plugin is responsible
        for the loading of the client object.
        """
        cmd.Cmd.__init__(self)
        Context.__init__(self)
        self.prompt = 'omero> '
        self.interrupt_loop = False
        self._client = None
        self._pluginsLoaded = CLI.PluginsLoaded()
        self.rv = 0 # Return value to be returned

    def assertRC(self):
        if self.rv != 0:
            raise NonZeroReturnCode(self.rv, "assert failed")

    def invoke(self, line, strict = False):
        """
        Copied from cmd.py
        """
        line = self.precmd(line)
        stop = self.onecmd(line)
        stop = self.postcmd(stop, line)
        if strict:
            self.assertRC()

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
        try:
            # Starting a new command. Reset the return value to 0
            # If err or die are called, set rv non-0 value
            self.rv = 0
            return cmd.Cmd.onecmd(self, line)
        except BadArgument, exc:
            self.rv = -1
            self.err("Bad arguments: %s" % exc)
            return False
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
                return (line[0], "", line[0])
            else:
                return (line[0], line[1:], Arguments(line))
        elif isinstance(line, Arguments):
            first, other = line.firstOther()
            return (first, other, line) # Passing new args instance as "other"
        else:
            return cmd.Cmd.parseline(self, line)

    def default(self, arg):
        args = Arguments(arg)
        try:
            args["EOF"]
            self.exit("")
        except KeyError:
            first, other = args.firstOther()
            file = OMEROCLI / "plugins" / (first + ".py")
            loc = {"register": self.register}
            try:
                execfile( str(file), loc )
            except Exc, ex:
                self.dbg("Could not load %s: %s" % (first, ex))
                self.waitForPlugins()

            if self.controls.has_key(first):
                return self.invoke(args)
            else:
                self.unknown_command(first)

    def unknown_command(self, first):
            self.err("""Unknown command: "%s" Try "help".""" % first)

    def completenames(self, text, line, begidx, endidx):
        names = self.controls.keys()
        return [ str(n + " ") for n in names if n.startswith(line) ]

    # Delegation
    def delegate(self, prepend, *args):
        args = Arguments(args)
        prepend = list(prepend)
        prepend.reverse()
        for p in prepend:
            args.insert(0, p)
        self.pub(args)

    # Note: this delegation doesn't work well with "bin/omero debug ..."
    def do_start(self, args): self.delegate(["node", "start"], args)
    def do_login(self, args):  self.delegate(["sessions", "login"], args)
    def do_logout(self, args):  self.delegate(["sessions", "logout"], args)

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

    def pub(self, args, strict = False):
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
            if hasattr(self, "do_%s" % first):
                m = getattr(self, "do_%s" % first)
                m(other)
            else:
                self.die(11, "Missing required plugin: "+ str(ke))
        if strict:
            self.assertRC()

    def _env(self):
        """
        Configure environment with PYTHONPATH as
        setup by bin/omero
        """
        home = str(self.dir / "lib" / "python")
        env = dict(os.environ)
        pypath = env.get("PYTHONPATH", None)
        if pypath is None:
            pypath = home
        else:
            if pypath.endswith(os.path.pathsep):
                pypath = "%s%s" % (pypath, home)
            else:
                pypath = "%s%s%s" % (pypath, os.path.pathsep, home)
        env["PYTHONPATH"] = pypath
        return env

    def _cwd(self, cwd):
        if cwd is None:
            cwd = str(OMERODIR)
        else:
            cwd = str(cwd)
        return cwd

    def call(self, args, strict = True, cwd = None):
        """
        Calls the string in a subprocess and dies if the return value is not 0
        """
        self.dbg("Executing: %s" % args)
        rv = subprocess.call(args, env = self._env(), cwd = self._cwd(cwd))
        if strict and not rv == 0:
            raise NonZeroReturnCode(rv, "%s => %d" % (" ".join(args), rv))
        return rv

    def popen(self, args, cwd = None):
        self.dbg("Returning popen: %s" % args)
        return subprocess.Popen(args, env = self._env(), cwd = self._cwd(cwd), stdout = subprocess.PIPE, stderr = subprocess.PIPE)

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
        try:
            output = getprefs(["get"], str(OMERODIR / "lib"))
        except OSError, err:
            self.err("Error getting preferences")
            self.dbg(err)
            output = ""

        import Ice
        data = Ice.InitializationData()
        data.properties = Ice.createProperties()
        for k,v in properties.items():
            data.properties.setProperty(k,v)
        self.parsePropertyFile(data, output)
        return data

    def conn(self, args = None):
        """
        Returns any active _client object. If one is present but
        not alive, it will be removed.
        """
        if self._client:
            self.dbg("Found client")
            try:
                self._client.getSession().keepAlive(None)
                self.dbg("Using client")
                return self._client
            except:
                self.dbg("Removing client")
                self._client.closeSession()
                self._client = None
        if args is not None:
            args.acquire(self)
            return self._client # Added by "login"

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
                except KeyboardInterrupt:
                    raise
                except:
                    self.err("Error loading:"+plugin)
                    traceback.print_exc()
        self._pluginsLoaded.set()

    ## End Cli
    ###########################################################

def argv(args=sys.argv):
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
