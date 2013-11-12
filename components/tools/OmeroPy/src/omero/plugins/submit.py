#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   submit plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2007 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from omero.cli import BaseControl, CLI
import sys

prompt = "omero submit [%s]> "


class Save(Exception):
    pass


class Cancel(Exception):
    pass


class SubmitCLI(CLI):

    def __init__(self):
        CLI.__init__(self)
        self.queue = []
        self.prompt = prompt % str(0)

    def postcmd(self, stop, line):
        self.queue.append(line)
        self.prompt = prompt % str(len(self.queue))
        return CLI.postcmd(self, stop, line)

    def do_save(self, arg):
        raise Save()

    def do_cancel(self, arg):
        raise Cancel()

    def post_process(self):
        print "Uploading"
        print self.queue

HELP = """When run without arguments, submit shell is opened
which takes commands without executing them. On save,
the file is trasferred to the server, and executed."""


class SubmitControl(BaseControl):

    def _configure(self, parser):
        parser.add_argument("arg", nargs="*", help="single command with args")
        parser.set_defaults(func=self.__call__)

    def __call__(self, args):
        submit = SubmitCLI()
        arg = args.arg
        if arg and len(arg) > 0:
            submit.invoke(arg)
            submit.post_process()
        else:
            try:
                submit.invokeloop()
            except Save:
                submit.execute()
            except Cancel:
                l = len(submit.queue)
                if l > 0:
                    print l, " items queued. Really cancel? [Yn]"

try:
    # register("submit", SubmitControl, HELP)
    pass
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("submit", SubmitControl, HELP)
        cli.invoke(sys.argv[1:])
