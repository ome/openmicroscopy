#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""

:author: Josh Moore <josh@glencoesoftware.com>

Generator script for producing the ome.services.sharing.data classes
Copyright (c) 2008, Glencoe Software, Inc.
See LICENSE for details.

"""
import sys
import os
import subprocess
import time
import glob


def readlink(file=sys.argv[0]):
    import stat

    file = sys.argv[0]
    while stat.S_ISLNK(os.lstat(file)[stat.ST_MODE]):
        target = os.readlink(file)
        if target[0] != "/":
            file = os.path.join(os.path.dirname(file), target)
        else:
            file = target

    file = os.path.abspath(file)
    return file

exe = readlink()
shr = os.path.join(exe, os.pardir)
shr = os.path.normpath(shr)
dat = os.path.join(shr, "data")
src = os.path.join(shr, os.pardir, os.pardir, os.pardir)
src = os.path.normpath(src)
top = os.path.join(src, os.pardir, os.pardir, os.pardir)
rep = os.path.join(top, "lib", "repository")
rep = os.path.normpath(rep)


def call(cmd, cwd="."):
    rc = subprocess.call(cmd, shell=True, cwd=cwd)
    if rc != 0:
        print "Halting..."
        sys.exit(rc)


def clean(dir=dat):
        if os.path.exists(dat):
            print "Removing %s. Cancel now if necessary. Waiting 5 seconds." \
                % dat
            time.sleep(5)
            ls = os.listdir(dat)
            for file in ls:
                print "Removing %s" % file
                os.remove(os.path.join(dat, file))
            os.rmdir(dat)


def slice(dir=dat):
        os.mkdir(dat)
        README = open(os.path.join(dat, "README.txt"), "w")
        README.write("""
        THE FILES IN THIS DIRECTORY ARE GENERATE
        AND WILL BE AUTOMATICALLY DELETED
        """)
        README.flush()
        README.close()
        call("""slice2freezej \
--dict ome.services.sharing.data.ShareMap,\
long,ome::services::sharing::data::ShareData \
--dict-index ome.services.sharing.data.ShareMap,id \
--dict-index ome.services.sharing.data.ShareMap,owner \
--output-dir %s Share.ice""" % src)
        call("""slice2freezej \
--dict ome.services.sharing.data.ShareItems,\
long,ome::services::sharing::data::ShareItem \
--dict-index ome.services.sharing.data.ShareItems,type \
--dict-index ome.services.sharing.data.ShareItems,share \
--output-dir %s Share.ice""" % src)
        call("""slice2java --output-dir %s Share.ice""" % src)


def compile(dir=dat):
        proc = subprocess.Popen(
            "slice2java --version",
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            shell=True)
        version = proc.communicate()[1].strip()
        pat = "%s/ice*%s.jar" % (rep, version)
        cp = ":".join(glob.glob(pat))
        javac_cmd = "javac -source 1.6 -target 1.6 -cp "
        javac_cmd += ("%s %s/*.java""" % (cp, dat))
        print javac_cmd
        call(javac_cmd, cwd=src)
        jar_cmd = "jar cvf "
        jar_cmd += ("%s/lib/repository/omero-shares-%s.jar " % (top, version))
        jar_cmd += ("ome/services/sharing/data/*.java ")
        jar_cmd += ("ome/services/sharing/data/*.class")
        call(jar_cmd, cwd=src)

if __name__ == "__main__":
    clean()
    slice()
    compile()
    print "Be sure to run for Ice 3.4, Ice 3.5 and Ice 3.6"
