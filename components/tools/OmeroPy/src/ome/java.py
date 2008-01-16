#
#   $Id$
#
#   Copyright 2007 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
#

import os

def makeVar(key):
        if os.environ.has_key(key):
                return [key+"="+os.environ[key]]
        else:
                return []

def run(args):
    java = ["java"]
    if os.environ.has_key("JAVA_OPTS"):
            java += os.environ["JAVA_OPTS"].split()
    if os.environ.has_key("DEBUG"):
            java += ['-Xrunjdwp:server=y,transport=dt_socket,address=9777,suspend=n']

    env = ['env']
    PATH = makeVar("PATH")
    LIBS = makeVar("LIBS")
    LIBM = makeVar("LIBM")
    command  = env+PATH+LIBS+LIBM+java+args

    
    import subprocess
    output = subprocess.Popen(command, stdout=subprocess.PIPE).communicate()[0]
    return output
