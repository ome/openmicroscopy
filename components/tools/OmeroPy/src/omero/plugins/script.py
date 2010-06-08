#!/usr/bin/env python
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

import exceptions, subprocess, os, sys, signal, time, atexit
from omero.cli import CLI
from omero.cli import BaseControl
from omero_ext.argparse import Action
from omero_ext.strings import shlex
from omero_ext.functional import wraps


HELP = """ Support for launching, uploading and otherwise managing OMERO.scripts """

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

class ScriptControl(BaseControl):

    def _configure(self, parser):
        def _who(parser):
            return parser.add_argument("who", nargs="*", help="Who to search for: user, group, user=1, group=5")

        sub = parser.sub()

        help = parser.add(sub, self.help, "Extended help")

        demo = parser.add(sub, self.demo, "Runs a short demo of the scripting system")

        cat = parser.add(sub, self.cat, "Prints a script to standard out")
        cat.add_argument("original_file", type=long, help="Id of an original file stored in OMERO")

        jobs = parser.add(sub, self.jobs, help = "List current jobs for user or group")
        jobs.add_argument("--all", action="store_true", help="Show all jobs, not just running ones")
        _who(jobs)

        launch = parser.add(sub, self.launch, help = "Launch a script with parameters")
        launch.add_argument("original_file", type=long, help="Id of an original file stored in OMERO")
        launch.add_argument("input", nargs="*", help="Inputs for the script of the form 'param=value'")

        list = parser.add(sub, self.list, help = "List files for user or group")
        _who(list)

        serve = parser.add(sub, self.serve, help = "Start a usermode processor for non-official scripts")
        serve.add_argument("-d", "--debug", action="store_true", help="Enable debug logging on processor")
        serve.add_argument("-b", "--background", action="store_true", help="Run processor in background. Used in demo")
        serve.add_argument("-t", "--timeout", default=0, type=long, help="Seconds that the processor should run. 0 means no timeout")
        _who(serve)

        upload = parser.add(sub, self.upload, help = "Upload a non-official script")

        run = parser.add(sub, self.run, help = "Run a script with the OMERO libraries loaded")
        run.add_argument("file", help = "Script to run")

        log = parser.add(sub, self.log, help = "TBD", tbd="TRUE")

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
        serve user
        serve group
        serve user=[id|name]
        serve group=[id|name]
        serve group=[id|name] user=[id|name]
        serve background=[true|false]
        serve timeout={min}
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

        import sha
        digest = sha.new()
        digest.update(DEMO_SCRIPT)
        sha1 = digest.hexdigest()

        self.ctx.out("\nExample script writing session")
        self.ctx.out("="*80)

        def msg(title, method = None, *args):
            self.ctx.out("\n")
            self.ctx.out("\t+" + ("-"*68) + "+")
            title = "\t| %-66.66s | " % title
            self.ctx.out(title)
            if method:
                cmd = "%s %s" % (method.__name__, " ".join(args))
                cmd = "\t| COMMAND: bin/omero script %-40.40s | " % cmd
                self.ctx.out(cmd)
            self.ctx.out("\t+" + ("-"*68) + "+")
            self.ctx.out(" ")
            if method:
                try:
                    method(*args)
                except exceptions.Exception, e:
                    self.ctx.out("\nEXECUTION FAILED: %s" % e)

        client = self.ctx.conn(args)
        admin = client.sf.getAdminService()
        current_user = admin.getEventContext().userId
        query = "select o from OriginalFile o where o.sha1 = '%s' and o.details.owner.id = %s" % (sha1, current_user)
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
        msg("Printing script params for file %s" % id, self.params, "file=%s" % id)
        msg("Serving file %s in background" % id, self.serve, "user", "background=true")
        msg("Launching script with parameters: a=bad-string (fails)", self.launch, "file=%s" % id, "a=bad-string")
        msg("Launching script with parameters: a=bad-string opt=6 (fails)", self.launch, "file=%s" % id, "a=bad-string", "opt=6")
        msg("Launching script with parameters: a=foo opt=1 (passes)", self.launch, "file=%s" % id, "a=foo", "opt=1")

        ## self.ctx.out("\nDeleting script from server...")
        ## self.delete(args.for_pub(str(id)))

    def cat(self, args):
        ofile = args.original_file
        client = self.ctx.conn(args)
        try:
            self.ctx.out(client.sf.getScriptService().getScriptText(ofile))
        except exceptions.Exception, e:
            self.ctx.err("Failed to find script: %s (%s)" % (ofile, e))

    def jobs(self, args):
        client = self.ctx.conn(args)
        for x in client.sf.getQueryService().findAllByQuery("select j from Job j where j.finished is null", None):
            id = x.id.val
            msg = x.message and x.message.val
            start = x.started and x.started.val
            self.ctx.out("%s %s %s" % (id, msg, start))

    def launch(self, args):
        """
        """

        client = self.ctx.conn(args)
        script_id = args.original_file

        import omero
        import omero.scripts
        import omero.rtypes
        svc = client.sf.getScriptService()
        params = svc.getParams(script_id)

        inputs = {}
        for input in args.inputs:
            parts = input.split("=")
            if len(parts) == 1:
                parts.append("")
            inputs[parts[0]] = parts[1]

        m = {}
        for key, param in params.inputs.items():
            a = inputs.get(key, None)
            if not a and not param.optional:
                self.ctx.die(321, "Missing input: %s" % key)
            else:
                if a is None:
                    m[key] = None
                elif isinstance(param.prototype,\
                    (omero.RLong, omero.RString, omero.RInt,\
                     omero.RTime, omero.RDouble, omero.RFloat)):
                    m[key] = param.prototype.__class__(a)
                elif isinstance(param.prototype, omero.RList):
                    items = a.split(",")
                    if len(param.prototype.val) == 0:
                        # Don't know what needs to be added here, so calling wrap
                        # which will produce an rlist of rstrings.
                        items = omero.rtypes.wrap(items)
                    else:
                        p = param.prototype.val[0]
                        m[key] = omero.rtypes.rlist([p.__class__(x) for x in items])
                else:
                    self.ctx.die(146, "No converter for: %s" % param.prototype)

        try:
            proc = svc.runScript(script_id, m, None)
            job = proc.getJob()
        except omero.ValidationException, ve:
            self.ctx.err("Bad parameters:\n%s" % ve)
            return # EARLY EXIT

        # Adding notification to wait on result
        cb = omero.scripts.ProcessCallbackI(client, proc)
        try:
            self.ctx.out("Job %s ready" % job.id.val)
            self.ctx.out("Waiting....")
            count = 0
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
                    val = val.replace("\n","\n\t* ")
                    self.ctx.out(val, newline=False)
                def close(this):
                    pass

            f = rv.get(m, None)
            if f and f.val:
                self.ctx.out("\n\t*** start %s ***" % m)
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
        import omero_api_IScript_ice
        client = self.ctx.conn(args)
        sf = client.sf
        svc = sf.getScriptService()
        if args.who:
            who = [self._parse_who(sf, w) for w in args.who]
            scripts = svc.getUserScripts(who)
            banner = "Scripts for %s" % ", ".join(args.who)
        else:
            scripts = svc.getScripts()
            banenr = "Primary scripts"
        self._parse_scripts(scripts, banner)

    def log(self, args):
        print args
        pass

    def params(self, args):
        client = self.ctx.conn(args)
        script_id = self._file(args, client)
        import omero_api_IScript_ice
        svc = client.sf.getScriptService()
        job_params = svc.getParams(script_id)
        if job_params:
            self.ctx.out("")
            self.ctx.out("id=%s" % script_id)
            self.ctx.out("authors=%s" % ", ".join(job_params.authors))
            self.ctx.out("institutions=%s" % ", ".join(job_params.institutions))
            self.ctx.out("name=%s" % job_params.name)
            self.ctx.out("description=%s" % job_params.description)
            self.ctx.out("namespaces=%s" % ", ".join(job_params.namespaces))
            self.ctx.out("stdout=%s" % job_params.stdoutFormat)
            self.ctx.out("stderr=%s" % job_params.stderrFormat)
            def print_params(which, params):
                import omero
                self.ctx.out(which)
                for k, v in params.items():
                    self.ctx.out("  %s - %s" % (k, (v.description and v.description or "(no description)")))
                    self.ctx.out("    Optional: %s" % v.optional)
                    self.ctx.out("    Type: %s" % v.prototype.ice_staticId())
                    if isinstance(v.prototype, omero.RCollection):
                        self.ctx.out("    Subtype: %s" % v.prototype.val[0].ice_staticId())
                    elif isinstance(v.prototype, omero.RMap):
                        self.ctx.out("    Subtype: %s" % v.prototype.val.values[0].ice_staticId())
                    self.ctx.out("    Min: %s" % (v.min and v.min.val or ""))
                    self.ctx.out("    Max: %s" % (v.max and v.max.val or ""))
                    values = omero.rtypes.unwrap(v.values)
                    self.ctx.out("    Values: %s" % (values and ", ".join(values) or ""))
            print_params("inputs:", job_params.inputs)
            print_params("outputs:", job_params.outputs)

    def serve(self, args):
        debug = args.debug
        background = args.background
        timeout = args.timeout
        client = self.ctx.conn(args)
        sf = client.sf
        who = self._parse_who(sf, args.who)
        if not who:
            who = self._parse_who(sf, ["user"])

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
                impl = usermode_processor(client, serverid = "omer.scripts.serve", accepts_list = who)
            except exceptions.Exception, e:
                self.ctx.die(100, "Failed initialization: %s" % e)

            if background:
                def cleanup():
                    impl.cleanup()
                    logging._handlerList = original
                    logging.getLogger().handlers = roots
                atexit.register(cleanup)
            else:
                try:
                    def handler(signum, frame):
                        raise SystemExit()
                    old = signal.signal(signal.SIGALRM, handler)
                    signal.alarm(timeout)
                    self.ctx.input("Press any key to exit...\n")
                    signal.alarm(0)
                finally:
                    self.ctx.dbg("DONE")
                    signal.signal(signal.SIGTERM, old)
                    impl.cleanup()
        finally:
            if not background:
                logging._handlerList = original
                logging.getLogger().handlers = roots


    def upload(self, args):
        args.insert(0, "upload")
        self.ctx.pub(args, strict=True)
        id = self.ctx.get("last.upload.id")
        self.ctx.set("script.file.id", id)

    def replace(self, args):

        if len(args) != 2:
            self.ctx.die(111, "Usage: <original file id> <path to file")

        ofile = long(args.args[0])
        fpath = str(args.args[1])

        client = self.ctx.conn(args)
        ofile = client.sf.getQueryService().get("OriginalFile", ofile)
        client.upload(fpath, ofile=ofile)

    def delete(self, args):
        if len(args) != 1:
            self.ctx.die(123, "Usage: <original file id>")

        ofile = long(args.args[0])
        import omero_api_IScript_ice
        client = self.ctx.conn(args)
        try:
            client.sf.getScriptService().deleteScript(ofile)
        except exceptions.Exception, e:
            self.ctx.err("Failed to delete script: %s (%s)" % (ofile, e))

    #
    # Other
    #
    def run(self, args):
        if not os.path.exists(args.file):
            self.ctx.error("No such file: %s" % args.file)
        else:
            p = self.ctx.popen([sys.executable, args.file], stdout=sys.stdout, stderr=sys.stderr)
            p.wait()
            if p.poll() != 0:
                self.ctx.die(p.poll(), "Execution failed.")

    #
    # Helpers
    #

    def _parse_scripts(self, scripts, msg):
        """
        Parses a list of scripts to self.ctx.out
        """
        ids = list(scripts.keys())
        ids.sort()
        self.ctx.out(msg)
        self.ctx.out("="*40)
        for id in ids:
            self.ctx.out("%8.8s - %s" % (id, scripts[id]))

    def _file(self, args, client):
        f = args.original_file
        if f is None:
            self.ctx.die(100, "No script provided")
        try:
            script_id = long(f)
        except:
            q = client.sf.getQueryService()
            script_name = str(f)
            p = omero.sys.ParametersI()
            p.addString("name", script_name)
            scripts = q.findAllByQuery("select s from OriginalFile s where s.name = :name", p)
            if len(scripts) != 1:
                self.ctx.err("Didn't find a single script, but %s" % len(scripts))
            script_id = scripts[0].id.val
        return script_id

    def _parse_who(self, sf, who):
        """
        Parses who items of the form: "user", "group", "user=1", "group=6"
        """

        WHO_FACTORY  = {"user":omero.model.ExperimenterI, "group":omero.model.ExperimenterGroupI}
        WHO_CURRENT = { "user":lambda sf: sf.getAdminService().getEventContext().userId,
                        "group":lambda sf: sf.getAdminService().getEventContext().groupId}

        for key, factory in WHO_FACTORY.items():
            if who.startswith(key):
                if who == key:
                    id = WHO_CURRENT[key](sf)
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
