#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
 examples/ScriptingService/runHelloWorld.py

-----------------------------------------------------------------------------
  Copyright (C) 2006-2014 University of Dundee. All rights reserved.


  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along
  with this program; if not, write to the Free Software Foundation, Inc.,
  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

------------------------------------------------------------------------------

This script demonstrates how to use the scripting service to call a script on
an OMERO server.

@author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2

"""

import omero
import omero.scripts
import getopt
import sys
import os
import omero.util.script_utils as scriptUtil


def uploadScript(scriptService, scriptPath):

    file = open(scriptPath)
    script = file.read()
    file.close()
    # print script
    # prints the script ID to the command line. This can be used to run the
    # script. E.g. see runHelloWorld.py
    print "Uploading script:", scriptPath
    scriptId = scriptService.uploadScript(scriptPath, script)
    print "Script uploaded with ID:", scriptId


def runScript(session, scriptService, scriptPath):

    # Identify the script we want to run: Get all 'my' scripts and filter by
    # path.
    # An empty list implies that the server should return what it would by
    # default trust.
    acceptsList = []
    # returns list of OriginalFiles
    scripts = scriptService.getUserScripts(acceptsList)
    for s in scripts:
        print s.id.val, s.path.val + s.name.val

    namedScripts = [s.id.val for s in scripts
                    if s.path.val + s.name.val == scriptPath]

    if len(namedScripts) == 0:
        print "Didn't find any scripts with specified path"

    # use the most recent script (highest ID)
    scriptId = max(namedScripts)

    print "Running script: %s with ID: %s" % (scriptPath, scriptId)

    # Don't attempt to run script without starting user processor!
    # return

    # make a map of all the parameters we want to pass to the script
    # keys are strings. Values must be omero.rtypes such as rlong, rbool,
    # rlist.
    map = {
        "Input_Message": omero.rtypes.rstring(
            "Sending this message to the server!"),
    }

    # The last parameter is how long to wait as an RInt
    proc = scriptService.runScript(scriptId, map, None)
    try:
        cb = omero.scripts.ProcessCallbackI(client, proc)
        while not cb.block(1000):  # ms.
            pass
        cb.close()
        results = proc.getResults(0)    # ms
    finally:
        proc.close(False)

    # handle any results from the script
    # print results.keys()
    if 'Message' in results:
        print results['Message'].getValue()

    rawFileService = session.createRawFileStore()
    queryService = session.getQueryService()
    if 'stdout' in results:
        origFile = results['stdout'].getValue()
        fileId = origFile.getId().getValue()
        print ("\n******** Script generated StdOut in file:%s  *******"
               % fileId)
        print scriptUtil.readFromOriginalFile(
            rawFileService, queryService, fileId)
    if 'stderr' in results:
        origFile = results['stderr'].getValue()
        fileId = origFile.getId().getValue()
        print ("\n******** Script generated StdErr in file:%s  *******"
               % fileId)
        print scriptUtil.readFromOriginalFile(
            rawFileService, queryService, fileId)
    rawFileService.close()


def readCommandArgs():
    """
    Read the arguments from the command line and put them in a map

    @return     A map of the command args, with keys: "host", "username",
                "password", "scriptId"
    """

    def usage():
        print ("Usage: runHelloWorld.py --server server --username username"
               " --password password --file file")
    try:
        opts, args = getopt.getopt(
            sys.argv[1:], "s:u:p:f:",
            ["server=", "username=", "password=", "file="])
    except getopt.GetoptError:
        usage()
        sys.exit(2)
    returnMap = {}
    for opt, arg in opts:
        if opt in ("-s", "--server"):
            returnMap["host"] = arg
        elif opt in ("-u", "--username"):
            returnMap["username"] = arg
        elif opt in ("-p", "--password"):
            returnMap["password"] = arg
        elif opt in ("-f", "--file"):
            returnMap["script"] = arg

    return returnMap


if __name__ == "__main__":
    commandArgs = readCommandArgs()

    # log on to the server, create client and session and scripting service
    client = omero.client(commandArgs["host"])
    session = client.createSession(
        commandArgs["username"], commandArgs["password"])
    scriptPath = commandArgs["script"]
    scriptService = session.getScriptService()

    scriptPath = os.path.abspath(scriptPath)

    # upload script. Could comment this out if you just want to run.
    uploadScript(scriptService, scriptPath)

    # run script
    runScript(session, scriptService, scriptPath)
