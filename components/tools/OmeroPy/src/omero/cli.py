#!/usr/bin/env python

"""
Python driver for OMERO

Provides access to various OMERO.blitz server- and client-side
utilities, including starting and stopping servers, running
analyses, configuration, and more.

Usable via the ./omero script provided with the distribution
as well as from python via "import omero.cli; omero.cli.argv()"

Arguments are taken from (in order of priority): the run method
arguments, sys.argv, and finally from standard-in using the
cmd.Cmd.cmdloop method.

Josh Moore, josh at glencoesoftware.com
Copyright (c) 2007, Glencoe Software, Inc.
See LICENSE for details.

"""

sys = __import__("sys")

import cmd, string, re, os, subprocess, socket, exceptions, traceback, glob, platform, time
import shlex
from exceptions import Exception as Exc
from threading import Thread, Lock
from path import path

from omero_ext.argparse import ArgumentError, ArgumentParser, RawTextHelpFormatter, FileType, SUPPRESS
from omero.util.sessions import SessionsStore

import omero

#
# Static setup
#

try:
    from omero_version import omero_version
    VERSION=omero_version
except ImportError:
    VERSION="Unknown" # Usually during testing

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
#  - how should state machine work?
#   -- is the last control stored somwhere? in a stack history[3]
#   -- or do they all share a central memory? self.ctx["MY_VARIABLE"]
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

class Parser(ArgumentParser):
    """
    Extension of ArgumentParser for simplifying the
    _configure() code in most Controls
    """

    def sub(self):
        sub = self.add_subparsers(title="Subcommands", help="""
                Use %(prog)s <subcommand> -h for more information.
        """)
        return sub

    def add(self, sub, func, help, **kwargs):
        parser = sub.add_parser(func.im_func.__name__, help=help)
        parser.set_defaults(func=func, **kwargs)
        return parser


class NewFileType(FileType):
    """
    Extension of the argparse.FileType to prevent
    overwrite existing files.
    """
    def __call__(self, string):
        if os.path.exists(string):
            raise ValueError("File exists: %s" % string)
        return FileType.__call__(self, string)


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
        self.parser = Parser(prog=sys.argv[0], formatter_class=RawTextHelpFormatter)
        self.subparsers = self.parser_init(self.parser)

    def parser_init(self, parser):
        parser.add_argument("-v", "--version", action="version", version="%%(prog)s %s" % VERSION)
        parser.add_argument("-s", "--server")
        parser.add_argument("-p", "--port")
        parser.add_argument("-g", "--group")
        parser.add_argument("-u", "--user")
        parser.add_argument("-w", "--password")
        parser.add_argument("-k", "--key")
        parser.add_argument("-C", "--create", action="store_true")
        parser.add_argument("-L", "--last", action="store_true")
        subparsers = parser.add_subparsers(title="Subcommands")
        return subparsers

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

    To create a new control, subclass BaseControl, implement _configure,
    and end your module with::

        try:
            register("name", MyControl, HELP)
        except:
            if __name__ == "__main__":
                cli = CLI()
                cli.register("name", MyControl, HELP)
                cli.invoke(sys.argv[1:])

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
        first, other = args.firstOther()
        if first == None or hasattr(self, first):
            return True
        return False

    def __call__(self, args):
        """
        Main dispatch method for a control instance. The default
        implementation assumes that the *args consists of either
        no elements or exactly one list of strings ==> (["str"],)

        If no args are present, _noargs is called. Subclasses may want
        to read from stdin or drop into a shell from _noargs().

        Otherwise, the rest of the arguments are passed to the method
        named by the first argument, if _likes() returns True.
        """
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

    def _configure(self, parser):
        parser.set_defaults(func=self.__call__)
        parser.add_argument("topic", nargs="?", help="Topic for more information")

    def _complete(self, text, line, begidx, endidx):
        """
        This is something of a hack. This should either be a part
        of the context interface, or we should put it somewhere
        in a utility. FIXME.
        """
        return self.ctx.completenames(text, line, begidx, endidx)

    def __call__(self, args):

        self.ctx.waitForPlugins()
        controls = self.ctx.controls.keys()
        controls.sort()

        if not args.topic:
            print """OmeroCli client, version %(version)s

Usage: %(program_name)s <command> [options] args
See 'help <command>' for more information on syntax
Type 'quit' to exit

Available commands:
""" % {"program_name":sys.argv[0],"version":VERSION}

            for name in controls:
                print """ %s""" % name
            print """
For additional information, see http://trac.openmicroscopy.org.uk/omero/wiki/OmeroCli
Report bugs to <ome-users@openmicroscopy.org.uk>"""

        else:
            try:
                c = self.ctx.controls[args.topic]
                if hasattr(c, "help"):
                    c.help(args)
                else:
                    self.ctx.err("No extended help")
            except KeyError, ke:
                self.ctx.unknown_command(args.topic)


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
        try:
            line = self.precmd(line)
            stop = self.onecmd(line)
            stop = self.postcmd(stop, line)
            if strict:
                self.assertRC()
        finally:
            self.close()

    def invokeloop(self):
        try:
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
        finally:
            self.close()

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
            return self.execute(line)
        except SystemExit, exc: # Thrown by argparse
            self.rv = exc.code
            self.err("Bad arguments: %s" % exc)
            return False
        #
        # This was perhaps only needed previously
        # Omitting for the moment with the new
        # argparse refactoring
        #
        #except AttributeError, ae:
        #    self.err("Possible error in plugin:")
        #    self.err(str(ae))
        #    if self.isdebug:
        #        traceback.print_exc()
        except NonZeroReturnCode, nzrc:
            self.rv = nzrc.rv
        return False # Continue

    def postcmd(self, stop, line):
        return self.interrupt_loop

    def execute(self, line):

        if line == "EOF" or line == ["EOF"]:
            self.exit("")

        if isinstance(line, (str, unicode)):
            args = shlex.split(line)
        else:
            args = list(line)

        args = self.parser.parse_args(args)
        args.prog = self.parser.prog
        self.waitForPlugins()
        args.func(args)

        # How to handle: background loading, unknown commands, do_ methods, delegation
        if False:
            first, other = args.firstOther()
            file = OMEROCLI / "plugins" / (first + ".py")
            loc = {"register": self.register}
            try:
                execfile( str(file), loc )
                print loc.keys()
            except Exc, ex:
                self.dbg("Could not load %s: %s" % (first, ex))
                self.waitForPlugins()

            if self.controls.has_key(first):
                return self.invoke(args)
            elif hasattr(self, "do_%s" % first):
                return getattr(self, "do_%s" % first)(other)
            else:
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

    def popen(self, args, cwd = None, stdout = subprocess.PIPE, stderr = subprocess.PIPE):
        self.dbg("Returning popen: %s" % args)
        return subprocess.Popen(args, env = self._env(), cwd = self._cwd(cwd), stdout = stdout, stderr = stderr)

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

        If no client is found and arguments are available,
        will use the current settings to connect.

        If required attributes are missing, will delegate to the login command.

        FIXME: Currently differing setting sessions on the same CLI instance
        will misuse a client.
        """
        if self._client:
            self.dbg("Found client")
            try:
                self._client.getSession().keepAlive(None)
                self.dbg("Using client")
                return self._client
            except KeyboardInterrupt:
                raise
            except:
                self.dbg("Removing client")
                self._client.closeSession()
                self._client = None

        if args is not None:
            self.controls["sessions"].login(args)

        return self._client # Possibly added by "login"

    def close(self):
        client = self._client
        if client:
            self.dbg("Closing client: %s" % client)
            client.__del__()

    ##
    ## Plugin registry
    ##

    def register(self, name, Control, help):
        """ This method is added to the globals when execfile() is
        called on each plugin. A Control class should be
        passed to the register method which will be added to the CLI.
        """
        control = Control(ctx = self)
        parser = self.subparsers.add_parser(name, help=help)
        if hasattr(control, "_configure"):
            control._configure(parser)
        elif hasattr(control, "__call__"):
            parser.set_defaults(func=control.__call__)
        self.controls[name] = control

    def waitForPlugins(self):
        if True:
            return # Disabling. See comment in argv
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
        cli.register("help", HelpControl, "Extend help for all commands")
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
