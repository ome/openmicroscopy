#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
This script creates a NativeWrapper script,
which calls some executable. WARNING: This
can pose a large security risk on your system.
Please contact the list if you are unsure of
what you are doing:

    ome-users@lists.openmicroscopy.org.uk

"""

NATIVE_WRAPPER = """
import os
import sys
import platform

import omero
import omero.scripts as scripts

from subprocess import Popen

client = scripts.client('NativeWrapper.py',
    'Example of how to wrap some native call with a script',
    scripts.String("Target", optional=False),
    scripts.Long("Some_Param"))

# In this example, we simply list the file.
# In another context, you would want to join
# the same session that the script is a member
# of and use the values in the context:
#
#   client = omero.client()
#   session = client.createSession()
#   input = session.getInput("Some_Param")
#

if "Windows" == platform.system():
    executable = "dir"
else:
    executable = "ls"

target = client.getInput("Target", unwrap=True)

popen = Popen([executable, target])
sys.exit(popen.wait())

"""

import os
import time
import omero
import omero.processor
import omero.util
import uuid

# To see the actions of the usermode
# processor, enable logging here.
# ----------------------------------
# omero.util.configure_logging()

client = omero.client()
try:
    sf = client.createSession()
    scriptService = sf.getScriptService()

    # Store the script on the server. A uuid
    # is appended to the name since only one
    # script of each name is allowed.
    id = scriptService.uploadScript(
        "/examples/NativeWrapper-%s.py" % uuid.uuid4(), NATIVE_WRAPPER)

    # Now we create a processor for handling the
    # script we just created since it is not an
    # official script. If we had used
    # scriptService.uploadOfficialScript, or if
    # the script was already installed in the server
    # we would not need the ump.
    ump = omero.processor.usermode_processor(client)
    try:
        # Sending this file itself as the target
        # but it could of course be any value you'd
        # like.
        target = os.path.join(os.getcwd(), __file__)
        target = omero.rtypes.rstring(target)

        # Launch the script
        proc = scriptService.runScript(id, {"Target": target}, None)

        # Now wait on it to complete.
        cb = omero.scripts.ProcessCallbackI(client, proc)
        launched = time.time()
        while cb.block(500) is None:
            if 10.0 < (time.time() - launched):
                raise StopIteration("Too long!")
    finally:
        ump.cleanup()

    # Here you can print out the results or similar.
    # print proc.getResults(0)
    print "Success"

finally:
    client.closeSession()
