#
#   $Id$
#
#   Copyright 2007 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
#

import os, shlex

DEFAULT_DEBUG = "-Xrunjdwp:server=y,transport=dt_socket,address=8787,suspend=n"

def makeVar(key):
        if os.environ.has_key(key):
                return [key+"="+os.environ[key]]
        else:
                return []

def run(args,\
        use_exec = False,\
        java = "java",\
        xargs = None,\
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
    java = [java] # Convert string to an array for appending

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

    # Add the actual arguments now
    java += args

    if use_exec:
        env = os.environ
        print java[2]
        os.execvpe(java[0], java, env)
    else:
        env = ['env']
        PATH = makeVar("PATH")
        LIBS = makeVar("LIBS")
        LIBM = makeVar("LIBM")
        command  = env+PATH+LIBS+LIBM+java+args

        import subprocess
        output = subprocess.Popen(command, stdout=subprocess.PIPE).communicate()[0]
        return output
