#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# $Id$
#
# Copyright 2009 Glencoe Software, Inc. All rights reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# General build scripts.

import os
import re
import sys
import time
import subprocess

BUILD_PY = "-Dbuild.py=true"


def popen(args, stdin=None, stdout=subprocess.PIPE, stderr=subprocess.PIPE):
        copy = os.environ.copy()
        return subprocess.Popen(args,
                env=copy,
                stdin=stdin,
                stdout=stdout,
                stderr=stderr)


def execute(args):
    p = popen(args, stdout=sys.stdout, stderr=sys.stderr)
    rc = p.wait()
    if rc != 0:
        sys.exit(rc)


def notification(msg, prio):
    """
    Provides UI notification.
    """

    # May want to revert this to be OMERO_BUILD_NOTIFICATION, or whatever.
    if "OMERO_QUIET" in os.environ or sys.platform == "win32":
        return

    try:
        p = popen(["growlnotify","-t","OMERO Build Status","-p",str(prio)], stdin=subprocess.PIPE)
        p.communicate(msg)
        rc = p.wait()
        if rc != 0:
            pass # growl didn't work
    except OSError:
        pass # No growlnotify found, may want to use another tool

def java_omero(args):
    command = [ find_java() ]
    p = os.path.join( os.path.curdir, "lib", "log4j-build.xml")
    command.append("-Dlog4j.configuration=%s" % p)
    command.append(BUILD_PY)
    command.extend( calculate_memory_args() )
    command.extend(["omero"])
    command.extend(choose_omero_version())
    if isinstance(args,str):
        command.append(args)
    else:
        command.extend(args)
    execute(command)

def find_java():
    return "java"

def calculate_memory_args():
    return "-Xmx600M -XX:MaxPermSize=256m".split(" ")

def choose_omero_version():
    """
    Returns an array specifying the build parameter for
    ant. Returned as an array so that an empty value can
    be extended into the build command.

    If BUILD_NUMER is set, then "-Domero.version=${omero.version}-b${BUILD_NUMBER}"
    otherwise nothing.
    """

    omero_build = os.environ.get("BUILD_NUMBER", "")
    if omero_build:
        omero_build = "-b%s" % omero_build

    command = [ find_java(), "omero",BUILD_PY,"-q","version" ]
    err = ""
    try:
        p = popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        omero_version, err = p.communicate()
        omero_version = omero_version.split()[1]

        # If we're not on hudson, then we don't want to force
        # users to deal with rebuilding after each commit.
        # Instead, drop everything except for "-DEV"
        #
        # See gh-67 for the discussion.
        if not omero_build:
            omero_version = re.sub("([-]DEV)?-\d+-[a-f0-9]+(-dirty)?",\
                    "-DEV", omero_version)
    except:
        print "Error getting version for BUILD_NUMBER=%s" % omero_build
        if err:
            print err
        sys.exit(1)

    command = [ find_java(), "omero",BUILD_PY,"-q","plainversion" ]
    err = ""
    try:
        p = popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        omero_plain_version, err = p.communicate()
        omero_plain_version = omero_plain_version.split()[1]
        if not omero_build:
            omero_plain_version = re.sub("([-]DEV)?-\d+-[a-f0-9]+(-dirty)?",\
                    "-DEV", omero_plain_version)
    except:
        print "Error getting plain version for BUILD_NUMBER=%s" % omero_build
        if err:
            print err
        sys.exit(1)

    command = [ find_java(), "omero",BUILD_PY,"-q","shortversion" ]
    err = ""
    try:
        p = popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        omero_short_version, err = p.communicate()
        omero_short_version = omero_short_version.split()[1]
        if not omero_build:
            omero_short_version = re.sub("([-]DEV)?-\d+-[a-f0-9]+(-dirty)?",\
                    "-DEV", omero_short_version)
    except:
        print "Error getting short version for BUILD_NUMBER=%s" % omero_build
        if err:
            print err
        sys.exit(1)

    return [ "-Domero.version=%s%s" % (omero_version, omero_build),
             "-Domero.plainversion=%s" % (omero_plain_version),
             "-Domero.shortversion=%s" % (omero_short_version) ]


def handle_tools(args):
    _ = os.path.sep.join
    additions = []
    mappings = {
        "-top": _(["build.xml"]),
        "-cpp": _(["components","tools","OmeroCpp","build.xml"]),
        "-fs": _(["components","tools","OmeroFS","build.xml"]),
        "-java": _(["components","tools","OmeroJava","build.xml"]),
        "-py": _(["components","tools","OmeroPy","build.xml"]),
        "-web": _(["components","tools","OmeroWeb","build.xml"]),
    }
    while len(args) > 0 and args[0] in mappings.keys()+["-perf"]:
        if args[0] == "-perf":
            args.pop(0)
            A = "-listener net.sf.antcontrib.perf.AntPerformanceListener".split()
            additions.extend(A)
        elif args[0] in mappings.keys():
            F = mappings[args.pop(0)]
            A = ["-f", F]
            additions.extend(A)
    return additions + args


def handle_relative(args):
    """
    If no other specific file has been requested,
    then use whatever relative path is needed to
    specify build.xml in the local directory.

    Regardless, os.chdir is called to the top.
    """
    additions = []
    this = os.path.abspath(__file__)
    this_dir = os.path.abspath(os.path.join(this, os.pardir))
    cwd = os.path.abspath(os.getcwd())
    os.chdir(this_dir)
    if "-f" not in args:
        build_xml = os.path.join(cwd, "build.xml")
        if os.path.exists(build_xml):
            additions.append("-f")
            additions.append(build_xml)
    return additions + args

if __name__ == "__main__":
    #
    # use java_omero which will specially configure the build system.
    #
    args = list(sys.argv)
    args.pop(0)

    # Unset CLASSPATH, since this breaks the build
    if os.environ.get('CLASSPATH'):
        del os.environ['CLASSPATH']

    try:
        args = handle_tools(args)
        args = handle_relative(args)
        java_omero(args)
        notification(""" Finished: %s """ % " ".join(args), 0)
    except KeyboardInterrupt:
        sys.stderr.write("\nCancelled by user\n")
        sys.exit(2)
    except SystemExit, se:
        notification(""" Failed: %s """ % " ".join(args), 100)
        sys.exit(se.code)
