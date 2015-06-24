#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
 examples/ScriptingService/adminWorkflow.py

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

This script demonstrates how a server Admin might upload an "Offical" trusted
script and run it.

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
import omero.util.script_utils as scriptUtil
import getopt
import sys
from omero.rtypes import rstring, unwrap
import getpass

import time

startTime = 0


def printDuration(reset=False):
    """
    Method for timing the running of scripts. For performance testing only
    """
    global startTime
    if startTime == 0 or reset:
        startTime = time.time()
    print "timer = %s secs" % (time.time() - startTime)


def uploadScript(session, scriptService, scriptPath):
    """
    Tries to upload the specified script as an official script.
    *WARNING*
    If the script has already been uploaded, then it will be replaced with
    the new script.
    If uploaded scripts are not valid, they will not be returned by
    getScripts().
    """

    file = open(scriptPath)
    scriptText = file.read()
    file.close()
    # print scriptText

    # first check if the script has already been uploaded
    scriptId = scriptService.getScriptID(scriptPath)
    if scriptId is None or scriptId < 0:
        print "Uploading script:", scriptPath
        # try upload new script
        scriptId = scriptService.uploadOfficialScript(scriptPath, scriptText)
        print "Script uploaded with ID:", scriptId
    else:
        print "Editing script:", scriptPath
        # if it has, edit the existing script
        scriptFile = session.getQueryService().get("OriginalFile", scriptId)
        scriptService.editScript(scriptFile, scriptText)
        print "Script ID: %s was edited" % scriptFile.id.val

    return scriptId


def listScripts(scriptService):
    """
    Prints out the available Official Scripts returned by getScripts()
    and User Scripts returned by getUserScripts()
    """

    print "--OFFICIAL SCRIPTS--"
    scripts = scriptService.getScripts()
    for s in scripts:
        print s.id.val, s.path.val + s.name.val

    print "--USER SCRIPTS--"
    userGroups = []     # gives me available scripts for default group
    scripts = scriptService.getUserScripts(userGroups)
    for s in scripts:
        print s.id.val, s.path.val + s.name.val


def getParams(scriptService, scriptPath):
    """
    This simply queries the parameters of the script and prints them out.
    Script can be specified by it's path or ID.
    Prints various parameter attributes such as descriptions, default values
    etc.
    Useful for checking that parameters are being defined correctly.
    """

    try:
        scriptId = long(scriptPath)
    except:
        scriptId = scriptService.getScriptID(scriptPath)

    params = scriptService.getParams(scriptId)

    print "\nScript Name:", params.name
    print "Authors:", ", ".join([a for a in params.authors])
    print "Contact:", params.contact
    print "Version:", params.version
    print "Institutions:", ", ".join([i for i in params.institutions])

    print "Inputs:"
    for key, param in params.inputs.items():
        print " ", key
        if not param.optional:
            print "    * Required"
        if param.description:
            print "   ", param.description
        if param.min:
            print "    min:", param.min.getValue()
        if param.max:
            print "    max:", param.max.getValue()
        if param.values:
            print "   ", ", ".join([
                v.getValue() for v in param.values.getValue()])
        if param.useDefault:
            print "    default:", param.prototype.val


def runScript(session, scriptService, scriptPath):
    """
    This will attempt to run the script (scriptPath can be path or ID), asking
    the user for inputs for all the script parameters, and printing out the
    results when the script completes.
    """

    try:
        scriptId = long(scriptPath)
    except:
        scriptId = scriptService.getScriptID(scriptPath)

    # Identify the script we want to run: Get all 'official' scripts and
    # filter by path.
    print "Running script: %s with ID: %s" % (scriptPath, scriptId)

    # make a map of all the parameters we want to pass to the script
    # keys are strings. Values must be omero.rtypes such as rlong, rbool,
    # rlist.

    map = {}

    params = scriptService.getParams(scriptId)
    for key, param in params.inputs.items():

        print ""
        print key
        if param.description:
            print param.description
        if not param.optional:
            print " * Required"
        if param.values:
            print "Options:", ", ".join(unwrap(param.values))
        if param.min:
            print "Min:", param.min.getValue()
        if param.max:
            print "Max:", param.max.getValue()

        prototype = param.prototype
        prompt = ": "
        default = None
        if param.useDefault:
            default = param.prototype.val
            prompt = "[%s]: " % default
        pclass = prototype.__class__

        if pclass == omero.rtypes.RListI:
            valueList = []
            listClass = omero.rtypes.rstring
            l = prototype.val     # list
            # check if a value type has been set (first item of prototype
            # list)
            if len(l) > 0:
                listClass = l[0].getValue().__class__
                if listClass == int(1).__class__:
                    listClass = omero.rtypes.rint
                if listClass == long(1).__class__:
                    listClass = omero.rtypes.rlong

            print "List:"
            while(True):
                value = raw_input(prompt)
                if value == "":
                    break
                try:
                    obj = listClass(value)
                except:
                    print "Invalid entry"
                    continue
                if isinstance(obj, omero.model.IObject):
                    valueList.append(omero.rtypes.robject(obj))
                else:
                    valueList.append(obj)
            if len(valueList) > 0:
                map[key] = omero.rtypes.rlist(valueList)

        elif pclass == omero.rtypes.RMapI:
            print "MAP!"
            # check if a value type has been set for the map
            m = prototype.val
            print m

        else:
            value = raw_input(prompt)
            while(True):
                if value == "":
                    if default:
                        map[key] = param.prototype
                    break
                try:
                    map[key] = pclass(value)
                    break
                except:
                    print "Invalid entry"

    print map

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
        print "\nRESULTS:", results['Message'].getValue()

    for result in results.keys():
        if result not in ["Message", "stdout", "stderr"]:
            print "\n", result, results[result].getValue().__class__

    printOutErr = True
    if printOutErr:
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


def disableScript(session, scriptId):
    """
    This will simply stop a script, defined by ID, from being returned by
    getScripts() by editing it's mime-type to 'text/plain'
    """

    updateService = session.getUpdateService()
    scriptFile = session.getQueryService().get("OriginalFile", long(scriptId))

    print "Disabling script:", scriptFile.id.val, scriptFile.path.val + \
        scriptFile.name.val
    scriptFile.setMimetype(rstring("text/plain"))
    updateService.saveObject(scriptFile)


def cleanUpScriptFiles(session, scriptService):
    """
    In the case where official script files have been manually deleted (from
    /lib/scripts/ ) they will not be returned by getScripts(), but they are
    still in the OriginalFiles table in DB which means that uploadScript(path,
    text) will fail.
    This can be fixed by setting the mimetype to 'text/x-python' for all
    scripts that are still in the OriginalFiles table, but not returned by
    getScripts() or getUserScripts() so that they become disabled, allowing
    uploadScript(path, text) to work again.
    """
    queryService = session.getQueryService()
    updateService = session.getUpdateService()

    scriptIds = []

    scripts = scriptService.getScripts()
    print "\n Scripts: "
    for s in scripts:
        scriptIds.append(s.id.val)
        print s.id.val, s.path.val + s.name.val

    userScripts = scriptService.getScripts()
    print "\n User Scripts: "
    for s in userScripts:
        scriptIds.append(s.id.val)
        print s.id.val, s.path.val + s.name.val

    # get all script files in the DB
    query_string = ("select o from OriginalFile o"
                    " where o.mimetype='text/x-python'")
    scriptFiles = queryService.findAllByQuery(query_string, None)

    print "\n DISABLING invalid scripts.... "
    for s in scriptFiles:
        # print s.id.val, s.path.val + s.name.val
        if s.id.val not in scriptIds:
            print s.id.val, s.path.val + s.name.val
            s.setMimetype(rstring("text/plain"))
            updateService.saveObject(s)


def usage():
    print ("USAGE: python adminWorkflow.py -s server -u username -f file"
           " [options]")


def printHelp(args):

    print ""
    usage()

    print ("\nThe -f argument to specifiy a script file (by path or ID) is"
           " only required for some options below")
    print ("Admin permissions are required for upload, disable and clean"
           " options")
    print "\nOPTIONS:"

    print "\n list"
    print listScripts.__doc__

    print "\n upload"
    print uploadScript.__doc__

    print "\n params"
    print getParams.__doc__

    print "\n run"
    print runScript.__doc__

    print "\n clean"
    print cleanUpScriptFiles.__doc__

    print "\n disable"
    print disableScript.__doc__


def readCommandArgs():
    """
    Read the arguments from the command line and put them in a map

    @return     A map of the command args, with keys: "host", "username",
                "password", "scriptId"
    """

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
    return returnMap, args


def doWorkflow(client, commandArgs):
    """
    The main workflow is performed here, creating a connection to the server,
    processing the user commands and calling the appropriate methods.
    """

    session = client.createSession(
        commandArgs["username"], commandArgs["password"])
    scriptService = session.getScriptService()
    print "got session..."
    if len(args) == 0:
        print ("Choose from these options by adding argument: help, list,"
               " upload, params, run, disable, clean")

    # list scripts
    if "list" in args:
        listScripts(scriptService)

    # upload script, assigning the script ID to commandArgs(for running etc
    # without looking up ID)
    if "upload" in args:
        commandArgs["script"] = uploadScript(
            session, scriptService, commandArgs["script"])

    # get params of script
    if "params" in args:
        getParams(scriptService, commandArgs["script"])

    # run script
    if "run" in args:
        runScript(session, scriptService, commandArgs["script"])

    # disables script by changing the OriginalFile mimetype, from
    # 'text/x-python' to 'text/plain'
    if "disable" in args:
        disableScript(session, commandArgs["script"])

    if "clean" in args:
        cleanUpScriptFiles(session, scriptService)


if __name__ == "__main__":
    commandArgs, args = readCommandArgs()

    if "help" in args:
        printHelp(args)

    elif "host" not in commandArgs:
        print "No server specified. Use -s serverName"
        print "For more info, use:   python adminWorkflow help"
    elif "username" not in commandArgs:
        print "No user specified. Use -u userName"
        print "For more info, use:   python adminWorkflow help"
    else:
        client = omero.client(commandArgs["host"])
        try:
            # log on to the server, create client and session and scripting
            # service
            if "password" not in commandArgs:
                print "NB: you can also run script with -p yourPassword"
                commandArgs["password"] = getpass.getpass()

            doWorkflow(client, commandArgs)
        except:
            raise
        finally:
            client.closeSession()
