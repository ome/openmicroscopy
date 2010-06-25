"""
 examples/ScriptingService/adminWorkflow.py

-----------------------------------------------------------------------------
  Copyright (C) 2006-2010 University of Dundee. All rights reserved.


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
import getopt, sys, os, subprocess
import omero_api_IScript_ice
import omero_SharedResources_ice
from omero.rtypes import rstring, RListI, robject, rint, rlong, rlist, unwrap
import getpass

import time

startTime = 0

def printDuration(reset=False):
    global startTime
    if startTime == 0 or reset:
        print "Resetting timer:"
        startTime = time.time()
    print "time = %s secs" % (time.time() - startTime)

def uploadScript(scriptService, scriptPath):
    """
    Tries to upload the specified script as an official script.
    *WARNING* 
    If the script has already been uploaded, then it will be replaced with
    the new script.
    If uploaded scripts are not valid, they will not be returned by getScripts().
    """
    
    file = open(scriptPath)
    scriptText = file.read()
    file.close()
    #print scriptText
    
    # first check if the script has already been uploaded
    existingScript = getScript(scriptService, scriptPath)
    if existingScript == None:
        print "Uploading script:", scriptPath
        # try upload new script
        scriptId = scriptService.uploadOfficialScript(scriptPath, scriptText)
        print "Script uploaded with ID:", scriptId   
    else:
        print "Editing script:", scriptPath
        # if it has, edit the existing script
        scriptService.editScript(existingScript, scriptText)
        print "Script ID: %s was edited" % existingScript.id.val
        
        
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
        

def getScript(scriptService, scriptPath):
    """
    This method first tries to get the named script as an 'Official' script using getScripts()
    If this fails, then it calls getUserScripts() in case the named script is a user script. 
    """
    
    print "getScript:", scriptPath
    scripts = scriptService.getScripts()     # returns list of OriginalFiles     
        
    for s in scripts:
        print s.id.val, s.path.val + s.name.val
        
    # make sure path starts with a slash. 
    # ** If you are a Windows client - will need to convert all path separators to "/" since server stores /path/to/script.py **
    if not scriptPath.startswith("/"):
        scriptPath =  "/" + scriptPath
        
    namedScripts = [s for s in scripts if s.path.val + s.name.val == scriptPath]
    
    if len(namedScripts) == 0:
        print "Didn't find any Official scripts with specified path: %s" % scriptPath
        return getUserScript(scriptService, scriptPath)
    
    if len(namedScripts) > 1:
        print "Found more than one script with specified path: %s" % scriptPath
        
    return namedScripts[-1]
    

def getUserScript(scriptService, scriptPath):
    """ 
    Looks up a user script by name. 
    Since many user scripts may have the same path/name (user scripts not unique),
    this returns the matching script with the highest ID (most recent)
    """
    
    print "getUserScript:", scriptPath
    scripts = scriptService.getUserScripts([])     # returns list of OriginalFiles     
           
    # make sure path starts with a slash. 
    # ** If you are a Windows client - will need to convert all path separators to "/" since server stores /path/to/script.py **
    if not scriptPath.startswith("/"):
        scriptPath =  "/" + scriptPath
        
    script = None
    for s in scripts:
        # look for the script that has matching paths and highest ID
        if s.path.val + s.name.val == scriptPath:
            if script == None or script.id.val < s.id.val:
                script = s
                
    if script == None:
        print "Didn't find any User scripts with specified path: %s" % scriptPath
    return script
    
    
def getParams(scriptService, scriptPath):
    """
    This simply queries the parameters of the script and prints them out, 
    including various attributes such as descriptions, default values etc. 
    Useful for checking that parameters are being defined correctly.
    """
    
    scriptFile = getScript(scriptService, scriptPath)
    scriptId = scriptFile.id.val
    
    params = scriptService.getParams(scriptId)
    
    for key, param in params.inputs.items():
        print key
        if param.description: print "   ", param.description
        if param.min: print "   min:", param.min.getValue()
        if param.max: print "   max:", param.max.getValue()
        if param.values: print ", ".join([v.getValue() for v in param.values.getValue()])
        if param.useDefault: print "   default:", param.prototype.val
      
    
def runScript(session, scriptService, scriptPath):
    """
    This will attempt to run the specified script, asking the user
    for inputs for all the script parameters, and printing out the 
    results when the script completes. 
    """
    
    scriptFile = getScript(scriptService, scriptPath)
    scriptId = scriptFile.id.val
    
    # Identify the script we want to run: Get all 'official' scripts and filter by path.  
    print "Running script: %s with ID: %s" % (scriptPath, scriptId)
    
    # make a map of all the parameters we want to pass to the script
    # keys are strings. Values must be omero.rtypes such as rlong, rbool, rlist. 
    
    map = {}  
    
    params = scriptService.getParams(scriptId)
    for key, param in params.inputs.items():
        
        print ""
        print key
        if param.description: print param.description
        if not param.optional: print " * Required"
        if param.values:  print "Options:", ", ".join(unwrap(param.values))
        if param.min: print "Min:", param.min.getValue()
        if param.max: print "Max:", param.max.getValue()
        
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
            if len(l) > 0:       # we have a prototype
                listClass = l[0].getValue().__class__
                if listClass == int(1).__class__:
                    listClass = omero.rtypes.rint
                if listClass == long(1).__class__:
                    listClass = omero.rtypes.rlong
                    
            print "List:"
            while(True):
                value = raw_input(prompt)
                if value == "": break
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
        else:
            value = raw_input(prompt)
            while(True):
                if value == "":
                    if default:  map[key] = param.prototype
                    break
                try:
                    map[key] = pclass(value)
                    break
                except:
                    print "Invalid entry"
            
    print map
    printDuration(True)
    
    # The last parameter is how long to wait as an RInt
    proc = scriptService.runScript(scriptId, map, None)
    try:
        cb = omero.scripts.ProcessCallbackI(client, proc)
        while not cb.block(1000) and proc.poll() is not None:
            pass
        cb.close()
        results = proc.getResults(0)    # ms
    finally:
        proc.close(False)
    
    printDuration()
    # handle any results from the script 
    #print results.keys()
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
            print "\n******** Script generated StdOut in file:%s  *******" % fileId
            print scriptUtil.readFromOriginalFile(rawFileService, queryService, fileId)
        if 'stderr' in results:
            origFile = results['stderr'].getValue()
            fileId = origFile.getId().getValue()
            print "\n******** Script generated StdErr in file:%s  *******" % fileId
            print scriptUtil.readFromOriginalFile(rawFileService, queryService, fileId)


def disableScript(session, scriptService, scriptPath):
    """ This will simply stop a defined script from being returned by getScripts()"""
    
    updateService = session.getUpdateService()
    scriptFile = getScript(scriptService, scriptPath)
    
    print "Disabling script:", scriptFile.id.val, scriptFile.path.val + scriptFile.name.val
    scriptFile.setMimetype(rstring("text/plain"))
    updateService.saveObject(scriptFile)
    

def cleanUpScriptFiles(session, scriptService):
    """ 
    In the case where official script files have been manually deleted (from /lib/scripts/ ) 
    they will not be returned by getScripts(), but they are still in the OriginalFiles table in DB
    which means that uploadScript(path, text) will fail. 
    This can be fixed by setting the mimetype to 'text/x-python' for all scripts that are still in the 
    OriginalFiles table, but not returned by getScripts() or getUserScripts() so that they become disabled,
    allowing uploadScript(path, text) to work again. 
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
    query_string = "select o from OriginalFile o where o.mimetype='text/x-python'"
    scriptFiles = queryService.findAllByQuery(query_string, None)
    
    print "\n DISABLING invalid scripts.... "
    for s in scriptFiles:
        #print s.id.val, s.path.val + s.name.val
        if s.id.val not in scriptIds:
            print s.id.val, s.path.val + s.name.val,
            s.setMimetype(rstring("text/plain"))
            updateService.saveObject(s)


def usage():
    print "USAGE: python adminWorkflow.py -s server -u username -f file [options]" 
               
def printHelp(args):          
    
    print ""
    usage()
    
    print "\nThe -f argument to specifiy a script file is only required for some options below"
    print "Admin permissions are required for upload, remove and clean options"
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
    
    print "\n remove" 
    print disableScript.__doc__


def readCommandArgs():
    """
    Read the arguments from the command line and put them in a map
    
    @return     A map of the command args, with keys: "host", "username", "password", "scriptId"
    """
    host = ""
    username = ""
    password = ""
    script = ""
    
    try:
        opts, args = getopt.getopt(sys.argv[1:] ,"s:u:p:f:", ["server=", "username=", "password=", "file="])
    except getopt.GetoptError, err:          
        usage()                         
        sys.exit(2)                     
    returnMap = {}                  
    for opt, arg in opts: 
        if opt in ("-s","--server"):
            returnMap["host"] = arg
        elif opt in ("-u","--username"): 
            returnMap["username"] = arg    
        elif opt in ("-p","--password"): 
            returnMap["password"] = arg  
        elif opt in ("-f","--file"): 
            returnMap["script"] = arg   
    return returnMap, args
    
    
if __name__ == "__main__":
    commandArgs, args = readCommandArgs()
    
    if "help" in args:
        printHelp(args)
        
    else:
        # log on to the server, create client and session and scripting service
        client = omero.client(commandArgs["host"])
        if "password" in commandArgs:
            password = commandArgs["password"]
        else:
            password = getpass.getpass()
        try:
            printDuration()
            session = client.createSession(commandArgs["username"], password)
            scriptService = session.getScriptService()
            print "got session"
            printDuration()
            if len(args) == 0:  print "Choose from these options by adding argument: help, list, upload, params, run, remove, clean"
    
            # list scripts
            if "list" in args:
                listScripts(scriptService)
                printDuration()
        
            # upload script.
            if "upload" in args:
                uploadScript(scriptService, commandArgs["script"])
                printDuration()
    
            # get params of script
            if "params" in args:
                getParams(scriptService, commandArgs["script"])
                printDuration()
    
            # run script
            if "run" in args:
                runScript(session, scriptService, commandArgs["script"])
                printDuration()
    
            # disables script by changing the OriginalFile mimetype, from 'text/x-python' to 'text/plain'
            if "remove" in args:
                disableScript(session, scriptService, commandArgs["script"])
                printDuration()
            
            if "clean" in args:
                cleanUpScriptFiles(session, scriptService)
                printDuration()
        except:
            raise
        finally:
            client.closeSession()
