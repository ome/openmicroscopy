#!/usr/bin/env python
"""
 :author: Josh Moore, josh at glencoesoftware.com

 OMERO Grid node controller

 Copyright 2008 Glencoe Software, Inc.  All Rights Reserved.
 Use is subject to license terms supplied in LICENSE.txt

"""
import os, sys, omero, subprocess
from exceptions import Exception as Exc

class Control:

    def __init__(self, client = omero.client()):
        ic = client.ic
        self.client = client
        self.ic = ic
        self.nodename = ic.getProperties().getProperty("IceGrid.Node.Name")
        self.datadir = ic.getProperties().getProperty("IceGrid.Node.Data")
        self.cfgfile = os.path.join("etc",self.nodename+".cfg")
        self.pidfile = os.path.join(self.datadir,self.nodename+".pid")

    def start(self):
        command = ["icegridnode","--Ice.Config=%s" % self.cfgfile]
        command = command + ["--daemon", "--pidfile", self.pidfile,"--nochdir"]
        self.call(command)

    def stop(self):
        self.call(["stop"])

    def kill(self):
        pid = open(self.pidfile,"r").readline()
        self.call(["kill", pid])

    def call(self, string):
        """ Used internally for processing """
        rv = subprocess.call(string)
        if not rv == 0:
            raise Exc( "Error during:\"%s\"" % string )
            return rv

    def dispatch(self, cmd):
        if hasattr(self, cmd):
            f = getattr(self, cmd)
            f()
        else:
            raise Exc( "Unknown command:" + cmd )


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print sys.argv[0], " [start|stop|kill|status]"
    else:
        Control().dispatch(sys.argv[1])
