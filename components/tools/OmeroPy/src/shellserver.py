#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# OMERO Grid Shell Server
# Copyright 2008 Glencoe Software, Inc.  All Rights Reserved.
#
# A python wrapper which can call arbitrary shell commands
# and wait on a signal from the OMERO.grid to shut them
# down again.

import Ice, os, signal, subprocess, sys, time

def call(arr):
    # The following work around is due to the relative paths
    # passed by icegridnode
    env = dict(os.environ)
    pth = list(sys.path)
    for i in range(0, len(pth)):
        pth[i] = os.path.abspath(pth[i])
    env["PYTHONPATH"] = os.path.pathsep.join(pth)
    return subprocess.Popen(arr, env = env)

class Ex(Exception):
    pass

class ShellServer(Ice.Application):

    def __init__(self, name):
        self.name = name
        self.restarts = 0
        def handler(signal, frame):
            if not hasattr(self, "proc"):
                self.log.error("No process to send %s" % signal)
            else:
                self.log.error("Passing sig %s to child process %s" % signal)
                os.kill(self.proc.pid, signal)
        for sig in [signal.SIGQUIT, signal.SIGTERM]:
            signal.signal(sig, handler)

    def init(self):
        self.log = self.communicator().getLogger()
        return "%sAdapter" % self.name

    def start(self):
        self.proc = call([sys.executable, "bin/omero", "server", self.name.lower()]) # Lowercasing should be done in cli.py

    def stop(self):
        if not hasattr(self, "proc"):
            self.log.error("No process found. Exiting")
        else:
            proc = self.proc
            del self.proc
            if not proc.poll():
                os.kill(proc.pid, signal.SIGTERM)
                self.log.warning("Sent %s SIGTERM. Sleeping..." % str(proc.pid))
                for i in range(1,30):
                    time.sleep(1)
                    self.log.warning("tick")
                    if proc.poll():
                        self.log.warning("Stopped.")
                        break
            if not proc.poll():
                os.kill(proc.pid, signal.SIGKILL)
                self.log.error("\nKilling %s..." % str(proc.pid))
        sys.exit(0)

    def run(self,args):
        """
        Starts the defined process and watches for it to exit.
        If it exits before stop() is called, it will be restarted.
        """
        self.shutdownOnInterrupt()
        adapterName = self.init()
        try:
            self.start()
            adapter = self.communicator().createObjectAdapter(adapterName)
            adapter.activate()
            while not self.communicator()._impl.waitForShutdown(1000):
                if self.proc and self.proc.poll():
                    self.restarts += 1
                    if self.restarts > 5:
                        raise Ex("Too many restarts")
                    else:
                        self.log.warning("Restart " + str(self.restarts))
                        self.start()
        finally:
            self.stop()

if __name__ == "__main__":
    if len(sys.argv) == 0:
        raise Ex("Requires argument to pass to bin/omero server")
    name = sys.argv[1]
    app=ShellServer(name)
    sys.exit(app.main(sys.argv))
