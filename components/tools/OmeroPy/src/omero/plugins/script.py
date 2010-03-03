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

import subprocess, os, sys
from omero.cli import BaseControl, Arguments
from omero.util.temp_files import create_path
from omero_ext.strings import shlex
from omero_ext.functional import wraps


def wrapper(func):
    def wrap(*args, **kwargs):
        print "Wrapped", func
        return func(*args, **kwargs)
    wrap = wraps(func)(wrap)
    return wrap

class ScriptControl(BaseControl):

    def help(self, args = None):
        self.ctx.out("""
Syntax: %(program_name)s script file [configuration parameters]
        Executes a file as a script. Can be used to test scripts
        for later deployment on the grid.

        set
        set script=[id|name]
        set user=[id|name]
        set group=[id|name]
        set user=

        Note: Some of the other actions call set internally.


        cat
        cat script=[id|name]

        launch
        launch script=[id|name]

        list
        list user
        list group
        list user=[id|name]
        list group=[id|name]

        log
        log user
        log group
        log script
        log user=[id|name]
        log group=[id|name]
        log script=[id|name]

        params
        params script=[id|name]

        serve
        serve user
        serve group
        serve user=[id|name]
        serve group=[id|name]
        serve group=[id|name] user=[id|name]
        serve background=[true|false]
        server timeout={min}

        upload file=/tmp/my_script.py

        #
        # Other
        #

        run
        """)

    def TODO(self, *args):

        if hasattr(self, "secure"):
            self.ctx.err("Secure cli cannot execture python scripts")

    @wrapper
    def cat(self, *args):
        pass

    @wrapper
    def launch(self, *args):
        pass

    #@wrapper
    def list(self, *args):
        args = Arguments(args)
        args.acquire(self.ctx)

        user = args.get("user", None)
        if user:
            sf = self.ctx._client.sf
            id = sf.getAdminService().getEventContext().userId
            q = sf.getQueryService()
            objs = q.findAllByQuery("select s from OriginalFile s where s.format.value = 'text/x-python' and s.details.owner.id = %s" % id, None)
            scripts = {}
            for obj in objs:
                scripts[obj.id.val] = obj.name.val
            self._parse(scripts, "Scripts for user=%s" % id)
        else:
            import omero_api_IScript_ice
            svc = self.ctx._client.sf.getScriptService()
            scripts = svc.getScripts()
            self._parse(scripts, "Primary scripts")

    @wrapper
    def log(self, *args):
        pass

    @wrapper
    def params(self, *args):
        pass

    def serve(self, *args):
        args = Arguments(args)
        client = self.ctx.conn(args)
        background = args.getBool("background", False)
        timeout = args.getInt("timeout", 0)

        # Similar to omero.util.Server starting here
        import omero
        from omero.util import ServerContext
        from omero.processor import ProcessorI

        try:
            stop_event = omero.util.concurrency.get_event()
            serverid = "omero.scripts.serve"
            ctx = ServerContext(serverid, client.ic, stop_event)
            impl = ProcessorI(ctx)
            prx = client.adapter.addWithUUID(impl)
            ADD TO SESSION HERE. UPDATE REGISTRY METHODS TO CHECK
        except:
            self.ctx.die(100, "Failed initialization")

        if background:
            REGISTER_CLEANUP(timeout)
        else:
            Handle interrupt
            stop_event.wait(timeout)
            stop_event.set()
            impl.cleanup()

    @wrapper
    def upload(self, *args):
        args = Arguments(args)
        args.acquire(self.ctx)
        file, other = args.firstOther()
        self.ctx.pub(["upload", file])
        self.ctx.assertRC()

    #
    # Other
    #
    def run(self, *args):
        args = Arguments(args)
        first, other = args.firstOther()

        if not first:
            self.ctx.err("No file given")
        elif len(other) == 0:
            if not os.path.exists(first):
                self.ctx.error("No such file: %s" % first)

            env = os.environ
            env["PYTHONPATH"] = self.ctx.pythonpath()
            p = subprocess.Popen(args,env=os.environ)
            p.wait()
            if p.poll() != 0:
                self.ctx.die(p.poll(), "Execution failed.")

    #
    # Helpers
    #

    def _parse(self, scripts, msg):
        ids = list(scripts.keys())
        ids.sort()
        self.ctx.out(msg)
        self.ctx.out("="*100)
        for id in ids:
            self.ctx.out("%8.8s - %s" % (id, scripts[id]))

try:
    register("script", ScriptControl)
except NameError:
    ScriptControl()._main()
