#
#   $Id$
#
#   Copyright 2007 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
#

import os, shlex
import platform
import subprocess

DEFAULT_DEBUG = "-Xrunjdwp:server=y,transport=dt_socket,address=8787,suspend=n"

def makeVar(key, env):
        if os.environ.has_key(key):
                env[key] = os.environ[key]

def run(args,\
        use_exec = False,\
        java = "java",\
        xargs = None,\
        chdir = None,\
        debug = None,\
        debug_string = DEFAULT_DEBUG):
    """
    Execute a Java process, either via subprocess waiting for the process to finish and
    returning the output or if use_exec is True, via os.execvpe with the current environment.

    -X style arguments for the Java process can be set either via the xargs argument
    or if unset, the JAVA_OPTS environment variable will be checked. Note: shlex.split()
    is called on the JAVA_OPTS value and so bash-style escaping can be used to protect
    whitespaces.

    Debugging can more simply be turned on by passing True for the debug argument.
    If more control over the debugging configuration is needed, pass debug_string.
    """
    # Convert strings to an array for appending
    if isinstance(java,str):
        java = [java]
    if isinstance(xargs,str):
        xargs = shlex.split(xargs)

    # Add our logging configuration early
    # so that it can be overwritten by xargs
    java += [ "-Dlog4j.configuration=%s" % os.path.join("etc", "log4j.xml") ]

    # Preapre arguments
    if xargs == None:
        if os.environ.has_key("JAVA_OPTS"):
            java += shlex.split(os.environ["JAVA_OPTS"])
    else:
        java += xargs

    # Prepare debugging
    if debug == None:
        if os.environ.has_key("DEBUG"):
            java += ["-Xdebug",debug_string]
    else:
        if debug:
            java += ["-Xdebug",debug_string]

    # Do any mandatory configuration very late
    java += [ "-Djava.awt.headless=true" ]

    # Add the actual arguments now
    java += args

    if use_exec:
        env = os.environ
        if chdir:
            os.chdir(chdir)
        if platform.system() == "Windows":
             command = [ "\"%s\"" % i for i in java ]
             os.execvpe(java[0], command, env)
        else:
             os.execvpe(java[0], java, env)
    else:
        if not chdir:
            chdir = os.getcwd()
        output = subprocess.Popen(java, stdout=subprocess.PIPE, cwd=chdir, env = os.environ).communicate()[0]
        return output
