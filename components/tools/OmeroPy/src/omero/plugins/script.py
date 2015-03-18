#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   script plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   The script plugin is used to run arbitrary blitz scripts which
   take as their sole input Ice configuration arguments, including
   --Ice.Config=file1,file2.

   The first parameter, the script itself, should be natively executable
   on a given platform. I.e. invokable by subprocess.call([file,...])

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import re
import os
import sys
import signal
import atexit

from omero.cli import CLI
from omero.cli import BaseControl

from omero.util.sessions import SessionsStore

from path import path

MIMETYPE = "text/x-python"

HELP = """Support for launching, uploading and otherwise managing \
OMERO.scripts"""

DEMO_SCRIPT = """#!/usr/bin/env python
import omero
import omero.rtypes as rtypes
import omero.scripts as scripts

o = scripts.Long("opt", min=0, max=5)
a = scripts.String("a", values=("foo", "bar"), optional=False)
b = scripts.Long("b").out()

client = scripts.client("length of input string",
\"\"\"
    Trivial example script which calculates the length
    of the string passed in as the "a" input, and returns
    the value as the long "b"
\"\"\", a, b, o,

authors = ["OME Team"],
institutions = ["openmicroscopy.org"])

print "Starting script"

try:
    a = client.getInput("a").getValue()
    b = len(a)
    client.setOutput("b", rtypes.rlong(b))
    client.setOutput("unregistered-output-param", rtypes.wrap([1,2,3]))
finally:
    client.closeSession()

print "Finished script"
"""

RE0 = re.compile("\s*script\s+upload\s*")
RE1 = re.compile("\s*script\s+upload\s+--official\s*")


class ScriptControl(BaseControl):

    def _complete(self, text, line, begidx, endidx):
        """
        Returns a file after "upload" and otherwise delegates to the
        BaseControl
        """
        for RE in (RE1, RE0):
            m = RE.match(line)
            if m:
                replaced = RE.sub('', line)
                suggestions = self._complete_file(replaced, os.getcwd())
                if False:  # line.find("--official") < 0:
                    add = "--official"
                    parts = line.split(" ")
                    if "--official".startswith(parts[-1]):
                        new = add[len(parts[-1]):]
                        if new:
                            add = new
                    suggestions.insert(0, add)
                return suggestions
        return BaseControl._complete(self, text, line, begidx, endidx)

    def _configure(self, parser):
        def _who(parser):
            return parser.add_argument(
                "who", nargs="*",
                help="Who to execute for: user, group, user=1, group=5"
                " (default=official)")

        parser.add_login_arguments()
        sub = parser.sub()

        # Disabling for 4.2 release. help = parser.add(sub, self.help,
        # "Extended help")

        demo = parser.add(
            sub, self.demo,
            "Runs a short demo of the scripting system")

        list = parser.add(
            sub, self.list, help="List files for user or group")
        _who(list)

        cat = parser.add(sub, self.cat, "Prints a script to standard out")
        edit = parser.add(
            sub, self.edit,
            "Opens a script in $EDITOR and saves it back to the server")
        params = parser.add(
            sub, self.params, help="Print the parameters for a given script")
        launch = parser.add(
            sub, self.launch, help="Launch a script with parameters")
        disable = parser.add(
            sub, self.disable,
            help="Makes script non-executable by setting the mimetype")
        disable.add_argument(
            "--mimetype", default="text/plain",
            help="Use a mimetype other than the default (%(default)s)")
        enable = parser.add(
            sub, self.enable, help="Makes a script executable (e.g. sets"
            " mimetype to %s)" % MIMETYPE)
        enable.add_argument(
            "--mimetype", default=MIMETYPE,
            help="Use a mimetype other than the default (%(default)s)")
        replace = parser.add(
            sub, self.replace,
            help="Replace an existing script with a new value")

        for x in (launch, params, cat, disable, enable, edit, replace):
            x.add_argument(
                "original_file",
                help="Id or path of a script file stored in OMERO")
        launch.add_argument(
            "input", nargs="*",
            help="Inputs for the script of the form 'param=value'")

        jobs = parser.add(
            sub, self.jobs, help="List current jobs for user or group")
        jobs.add_argument(
            "--all", action="store_true",
            help="Show all jobs, not just running ones")
        _who(jobs)

        serve = parser.add(
            sub, self.serve,
            help="Start a usermode processor for scripts")
        serve.add_argument(
            "--verbose", action="store_true",
            help="Enable debug logging on processor")
        serve.add_argument(
            "-b", "--background", action="store_true",
            help="Run processor in background. Used in demo")
        serve.add_argument(
            "-t", "--timeout", default=0, type=long,
            help="Seconds that the processor should run. 0 means no timeout")
        _who(serve)

        upload = parser.add(sub, self.upload, help="Upload a script")
        upload.add_argument(
            "--official", action="store_true",
            help="If set, creates a system script. Must be an admin")
        upload.add_argument(
            "file", help="Local script file to upload to OMERO")

        replace.add_argument(
            "file",
            help="Local script which should overwrite the existing one")

        delete = parser.add(
            sub, self.delete, help="delete an existing script")
        delete.add_argument(
            "id", type=long,
            help="Id of the original file which is to be deleted")

        run = parser.add(
            sub, self.run,
            help="Run a script with the OMERO libraries loaded and current"
            " login")
        run.add_argument("file", help="Local script file to run")
        run.add_argument(
            "input", nargs="*",
            help="Inputs for the script of the form 'param=value'")

        # log = parser.add(sub, self.log, help = "TBD", tbd="TRUE")
        for x in (demo, cat, edit, params, launch, disable, enable, jobs,
                  serve, upload, replace, delete, run):
            x.add_login_arguments()

    def help(self, args):
        self.ctx.out("""

        Available or planned(*) commands:
        ================================
        demo

        set
        set file=[id|name]
        set user=[id|name]
        set group=[id|name]
        set user=

        Note: Some of the other actions call set internally.


        cat
        cat file=[id|name]
        mv
        rm
        cp
        --replace / --overwrite

        register
        publish
        processors
        chain
        edit

        jobs
        jobs user
        jobs group

        launch
        launch file=[id|name]

        list
        list user
        list group
        list user=[id|name]
        list group=[id|name]
        list publication=[regex]
        list ofifical
        list namespace=[]

        log
        log file
        log user
        log group
        log file=[id|name]
        log user=[id|name]
        log group=[id|name]

        params
        params file=[id|name]

        serve
        serve --background
        serve --timeout={min}
        serve --verbose
        serve user
        serve group
        serve user=[id|name]
        serve group=[id|name]
        serve group=[id|name] user=[id|name]
        serve count=1
        serve log
        serve log=some/file/somewhere

        upload file=/tmp/my_script.py
        replace
        delete

        #
        # Other
        #

        run
        """)

    def demo(self, args):
        from omero.util.temp_files import create_path
        t = create_path("Demo_Script", ".py")

        try:
            from hashlib import sha1 as sha_new
        except ImportError:
            from sha import new as sha_new

        digest = sha_new()
        digest.update(DEMO_SCRIPT)
        sha1 = digest.hexdigest()

        self.ctx.out("\nExample script writing session")
        self.ctx.out("="*80)

        def msg(title, method=None, *arguments):
            self.ctx.out("\n")
            self.ctx.out("\t+" + ("-"*68) + "+")
            title = "\t| %-66.66s | " % title
            self.ctx.out(title)
            if method:
                cmd = "%s %s" % (method.__name__, " ".join(arguments))
                cmd = "\t| COMMAND: bin/omero script %-40.40s | " % cmd
                self.ctx.out(cmd)
            self.ctx.out("\t+" + ("-"*68) + "+")
            self.ctx.out(" ")
            if method:
                try:
                    self.ctx.invoke(['script', method.__name__] +
                                    list(arguments))
                except Exception, e:
                    import traceback
                    self.ctx.out("\nEXECUTION FAILED: %s" % e)
                    self.ctx.dbg(traceback.format_exc())

        client = self.ctx.conn(args)
        current_user = self.ctx.get_event_context().userId
        query = "select o from OriginalFile o where o.hash = '%s' and" \
            " o.details.owner.id = %s" % (sha1, current_user)
        files = client.sf.getQueryService().findAllByQuery(query, None)
        if len(files) == 0:
            msg("Saving demo script to %s" % t)
            t.write_text(DEMO_SCRIPT)

            msg("Uploading script", self.upload, str(t))
            id = self.ctx.get("script.file.id")
        else:
            id = files[0].id.val
            msg("Reusing demo script %s" % id)

        msg("Listing available scripts for user", self.list, "user")
        msg("Printing script content for file %s" % id, self.cat, str(id))
        msg("Serving file %s in background" % id, self.serve, "user",
            "--background")
        msg("Printing script params for file %s" % id, self.params,
            "file=%s" % id)
        msg("Launching script with parameters: a=bad-string (fails)",
            self.launch, "file=%s" % id, "a=bad-string")
        msg("Launching script with parameters: a=bad-string opt=6 (fails)",
            self.launch, "file=%s" % id, "a=bad-string", "opt=6")
        msg("Launching script with parameters: a=foo opt=1 (passes)",
            self.launch, "file=%s" % id, "a=foo", "opt=1")
        try:
            for p in list(getattr(self, "_processors", [])):
                p.cleanup()
                self._processors.remove(p)
        except Exception, e:
            self.ctx.err("Failed to clean processors: %s" % e)

        self.ctx.out("\nDeleting script from server...")
        args.id = long(id)
        self.delete(args)

    def cat(self, args):
        client = self.ctx.conn(args)
        script_id, ofile = self._file(args, client)
        try:
            self.ctx.out(client.sf.getScriptService().getScriptText(script_id))
        except Exception, e:
            self.ctx.err("Failed to find script: %s (%s)" % (script_id, e))

    def edit(self, args):
        client = self.ctx.conn(args)
        scriptSvc = client.sf.getScriptService()
        script_id, ofile = self._file(args, client)
        txt = None
        try:
            txt = client.sf.getScriptService().getScriptText(script_id)
            if not txt:
                self.ctx.err("No text for script: %s" % script_id)
                self.ctx.err("Does this file appear in the script list?")
                self.ctx.err("If not, try 'replace'")
                return
            from omero.util.temp_files import create_path
            from omero.util import edit_path
            p = create_path()
            edit_path(p, txt)
            scriptSvc.editScript(ofile, p.text())
        except Exception, e:
            self.ctx.err("Failed to find script: %s (%s)" % (script_id, e))

    def jobs(self, args):
        self.ctx.conn(args)
        cols = ("username", "groupname", "started", "finished")
        query = "select j, %s, s.value from Job j join j.status s" \
            % (",".join(["j.%s" % j for j in cols]))
        if not args.all:
            query += " where j.finished is null"

        self.ctx.out("Running query via 'hql' subcommand: %s" % query)
        self.ctx.invoke("""hql "%s" """ % query)

    def launch(self, args):
        """
        """

        client = self.ctx.conn(args)
        script_id, ofile = self._file(args, client)

        import omero
        import omero.scripts
        import omero.rtypes
        svc = client.sf.getScriptService()
        try:
            params = svc.getParams(script_id)
        except omero.ValidationException, ve:
            self.ctx.die(502, "ValidationException: %s" % ve.message)

        m = self._parse_inputs(args, params)

        try:
            proc = svc.runScript(script_id, m, None)
            job = proc.getJob()
        except omero.ValidationException, ve:
            self.ctx.err("Bad parameters:\n%s" % ve)
            return  # EARLY EXIT

        # Adding notification to wait on result
        cb = omero.scripts.ProcessCallbackI(client, proc)
        try:
            self.ctx.out("Job %s ready" % job.id.val)
            self.ctx.out("Waiting....")
            while proc.poll() is None:
                cb.block(1000)
            self.ctx.out("Callback received: %s" % cb.block(0))
            rv = proc.getResults(3)
        finally:
            cb.close()

        def p(m):
            class handle(object):
                def write(this, val):
                    val = "\t* %s" % val
                    val = val.replace("\n", "\n\t* ")
                    self.ctx.out(val, newline=False)

                def close(this):
                    pass

            f = rv.get(m, None)
            if f and f.val:
                self.ctx.out("\n\t*** start %s (id=%s)***"
                             % (m, f.val.id.val))
                try:
                    client.download(ofile=f.val, filehandle=handle())
                except:
                    self.ctx.err("Failed to display %s" % m)
                self.ctx.out("\n\t*** end %s ***\n" % m)

        p("stdout")
        p("stderr")
        self.ctx.out("\n\t*** out parameters ***")
        for k, v in rv.items():
            if k not in ("stdout", "stderr", "omero.scripts.parse"):
                self.ctx.out("\t* %s=%s" % (k, omero.rtypes.unwrap(v)))
        self.ctx.out("\t***  done ***")

    def list(self, args):
        client = self.ctx.conn(args)
        sf = client.sf
        svc = sf.getScriptService()
        if args.who:
            who = [self._parse_who(w) for w in args.who]
            scripts = svc.getUserScripts(who)
            banner = "Scripts for %s" % ", ".join(args.who)
        else:
            scripts = svc.getScripts()
            banner = "Official scripts"
        self._parse_scripts(scripts, banner)

    def log(self, args):
        print args
        pass

    def params(self, args):
        client = self.ctx.conn(args)
        script_id, ofile = self._file(args, client)
        import omero
        svc = client.sf.getScriptService()

        try:
            job_params = svc.getParams(script_id)
        except omero.ValidationException, ve:
            self.ctx.die(454, "ValidationException: %s" % ve.message)
        except omero.ResourceError, re:
            self.ctx.die(455, "ResourceError: %s" % re.message)

        if job_params:
            self.ctx.out("")
            self.ctx.out("id:  %s" % script_id)
            self.ctx.out("name:  %s" % job_params.name)
            self.ctx.out("version:  %s" % job_params.version)
            self.ctx.out("authors:  %s" % ", ".join(job_params.authors))
            self.ctx.out("institutions:  %s"
                         % ", ".join(job_params.institutions))
            self.ctx.out("description:  %s" % job_params.description)
            self.ctx.out("namespaces:  %s" % ", ".join(job_params.namespaces))
            self.ctx.out("stdout:  %s" % job_params.stdoutFormat)
            self.ctx.out("stderr:  %s" % job_params.stderrFormat)

            def print_params(which, params):
                import omero
                self.ctx.out(which)
                for k in sorted(params,
                                key=lambda name: params.get(name).grouping):
                    v = params.get(k)
                    self.ctx.out("  %s - %s" % (k, (v.description and
                                 v.description or "(no description)")))
                    self.ctx.out("    Optional: %s" % v.optional)
                    self.ctx.out("    Type: %s" % v.prototype.ice_staticId())
                    if isinstance(v.prototype, omero.RCollection):
                        coll = v.prototype.val
                        if len(coll) == 0:
                            self.ctx.out("    Subtype: (empty)")
                        else:
                            self.ctx.out("    Subtype: %s"
                                         % coll[0].ice_staticId())

                    elif isinstance(v.prototype, omero.RMap):
                        try:
                            proto_value = \
                                v.prototype.val.values[0].ice_staticId()
                        except:
                            proto_value = None

                        self.ctx.out("    Subtype: %s" % proto_value)

                    # ticket:11472 - string min/max need quoting
                    def min_max(x):
                        if x:
                            if x.val is None:
                                return ""
                            elif isinstance(x, omero.RString):
                                return "'%s'" % x.val
                            else:
                                return x.val
                        return ""

                    self.ctx.out("    Min: %s" % min_max(v.min))
                    self.ctx.out("    Max: %s" % min_max(v.max))
                    values = omero.rtypes.unwrap(v.values)
                    self.ctx.out("    Values: %s"
                                 % (values and ", ".join(values) or ""))
            print_params("inputs:", job_params.inputs)
            print_params("outputs:", job_params.outputs)

    def serve(self, args):

        # List of processors which have been started
        if not hasattr(self, "_processors"):
            self._processors = []

        debug = args.verbose
        background = args.background
        timeout = args.timeout
        client = self.ctx.conn(args)
        who = [self._parse_who(w) for w in args.who]
        if not who:
            who = []  # Official scripts only

        # Similar to omero.util.Server starting here
        import logging
        original = list(logging._handlerList)
        roots = list(logging.getLogger().handlers)
        logging._handlerList = []
        logging.getLogger().handlers = []

        from omero.util import configure_logging
        from omero.processor import usermode_processor
        lvl = debug and 10 or 20
        configure_logging(loglevel=lvl)

        try:
            try:
                impl = usermode_processor(
                    client, serverid="omero.scripts.serve", accepts_list=who,
                    omero_home=self.ctx.dir)
                self._processors.append(impl)
            except Exception, e:
                self.ctx.die(100, "Failed initialization: %s" % e)

            if background:
                def cleanup():
                    impl.cleanup()
                    logging._handlerList = original
                    logging.getLogger().handlers = roots
                atexit.register(cleanup)
            else:
                if self._isWindows():
                    self.foreground_win(impl, timeout)
                else:
                    self.foreground_nix(impl, timeout)
        finally:
            if not background:
                logging._handlerList = original
                logging.getLogger().handlers = roots

        return impl

    def foreground_nix(self, impl, timeout):
        """
        Use signal.SIGALRM to wait for the timeout to signal
        """

        def handler(signum, frame):
            raise SystemExit()

        old = signal.signal(signal.SIGALRM, handler)
        try:
            signal.alarm(timeout)
            self.ctx.input("Press any key to exit...\n")
            signal.alarm(0)
        finally:
            self.ctx.dbg("DONE")
            signal.signal(signal.SIGTERM, old)
            impl.cleanup()

    def foreground_win(self, impl, timeout):
        """
        Note: currently simply fails.
        An implementation might be possible using msvcrt.
        See: \
http://stackoverflow.com/questions/3471461/raw-input-and-timeout/3911560
        """
        try:
            if timeout != 0:
                self.ctx.die(144, "Timeout not supported on Windows")
            else:
                self.ctx.input("Press any key to exit...\n")
                self.ctx.dbg("DONE")
        finally:
            impl.cleanup()

    def upload(self, args):
        p = path(args.file)
        if not p.exists():
            self.ctx.die(502, "File does not exist: %s" % p.abspath())

        import omero
        c = self.ctx.conn(args)
        scriptSvc = c.sf.getScriptService()

        if args.official:
            try:
                id = scriptSvc.uploadOfficialScript(args.file, p.text())
            except omero.ApiUsageException, aue:
                if "editScript" in aue.message:
                    self.ctx.die(502, "%s already exists; use 'replace'"
                                 " instead" % args.file)
                else:
                    self.ctx.die(504, "ApiUsageException: %s" % aue.message)
            except omero.SecurityViolation, sv:
                self.ctx.die(503, "SecurityViolation: %s" % sv.message)
        else:
            id = scriptSvc.uploadScript(args.file, p.text())

        self.ctx.err("Uploaded %sscript"
                     % (args.official and "official " or ""))
        self.ctx.out("OriginalFile:%s" % id)
        self.ctx.set("script.file.id", id)

    def replace(self, args):
        import omero
        client = self.ctx.conn(args)
        script_id, ofile = self._file(args, client)
        fpath = args.file

        file = open(fpath)
        scriptText = file.read()
        file.close()
        scriptSvc = client.sf.getScriptService()

        try:
            scriptSvc.editScript(ofile, scriptText)
        except omero.SecurityViolation, sv:
            self.ctx.die(200, sv.message)

    def delete(self, args):
        ofile = args.id
        client = self.ctx.conn(args)
        try:
            client.sf.getScriptService().deleteScript(ofile)
        except Exception, e:
            self.ctx.err("Failed to delete script: %s (%s)" % (ofile, e))

    def disable(self, args):
        ofile = self.setmimetype(args)
        self.ctx.out("Disabled %s by setting mimetype to %s"
                     % (ofile.id.val, args.mimetype))

    def enable(self, args):
        ofile = self.setmimetype(args)
        self.ctx.out("Enabled %s by setting mimetype to %s"
                     % (ofile.id.val, args.mimetype))

    def setmimetype(self, args):
        from omero.rtypes import rstring
        client = self.ctx.conn(args)
        script_id, ofile = self._file(args, client)
        if args.mimetype == MIMETYPE:  # is default
            if ofile.name.val.endswith(".jy"):
                args.mimetype = "text/x-jython"
            elif ofile.name.val.endswith(".m"):
                args.mimetype = "text/x-matlab"
        ofile.setMimetype(rstring(args.mimetype))
        return client.sf.getUpdateService().saveAndReturnObject(ofile)

    #
    # Other
    #
    def run(self, args):
        if not os.path.exists(args.file):
            self.ctx.die(670, "No such file: %s" % args.file)
        else:
            client = self.ctx.conn(args)
            store = SessionsStore()
            srv, usr, uuid, port = store.get_current()
            props = store.get(srv, usr, uuid)

            from omero.scripts import parse_file
            from omero.util.temp_files import create_path
            path = create_path()
            text = """
omero.host=%(omero.host)s
omero.user=%(omero.sess)s
omero.pass=%(omero.sess)s
            """
            path.write_text(text % props)

            params = parse_file(args.file)
            m = self._parse_inputs(args, params)
            for k, v in m.items():
                if v is not None:
                    client.setInput(k, v)

            p = self.ctx.popen([sys.executable, args.file], stdout=sys.stdout,
                               stderr=sys.stderr, ICE_CONFIG=str(path))
            p.wait()
            if p.poll() != 0:
                self.ctx.die(p.poll(), "Execution failed.")

    #
    # Helpers
    #
    def _parse_inputs(self, args, params):
        from omero.scripts import parse_inputs, parse_input, MissingInputs
        try:
            rv = parse_inputs(args.input, params)
        except MissingInputs, mi:
            rv = mi.inputs
            for key in mi.keys:
                value = self.ctx.input("""Enter value for "%s": """ % key,
                                       required=True)
                rv.update(parse_input("%s=%s" % (key, value), params))
        return rv

    def _parse_scripts(self, scripts, msg):
        """
        Parses a list of scripts to self.ctx.out
        """
        from omero.util.text import TableBuilder
        tb = TableBuilder("id", msg)
        for x in scripts:
            tb.row(x.id.val, x.path.val + x.name.val)
        self.ctx.out(str(tb.build()))

    def _file(self, args, client):
        f = args.original_file
        q = client.sf.getQueryService()
        svc = client.sf.getScriptService()

        if f is None:
            self.ctx.die(100, "No script provided")
        elif f.startswith("file="):
            f = f[5:]

        try:
            script_id = long(f)
        except:
            script_path = str(f)
            script_id = svc.getScriptID(script_path)
        ofile = q.get("OriginalFile", script_id)

        return script_id, ofile

    def _parse_who(self, who):
        """
        Parses who items of the form: "user", "group", "user=1", "group=6"
        """

        import omero
        WHO_FACTORY = {"user": omero.model.ExperimenterI,
                       "group": omero.model.ExperimenterGroupI}
        WHO_CURRENT = {"user": lambda ec: ec.userId,
                       "group": lambda ec: ec.groupId}

        for key, factory in WHO_FACTORY.items():
            if who.startswith(key):
                if who == key:
                    id = WHO_CURRENT[key](self.ctx.get_event_context())
                    return factory(id, False)
                else:
                    parts = who.split("=")
                    if len(parts) != 2:
                        continue
                    else:
                        id = long(parts[1])
                        return factory(id, False)

try:
    register("script", ScriptControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("script", ScriptControl, HELP)
        cli.invoke(sys.argv[1:])
