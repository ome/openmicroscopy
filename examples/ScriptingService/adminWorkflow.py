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
import getopt, sys, os, subprocess
import omero_api_IScript_ice
import omero_SharedResources_ice
from omero.rtypes import *


def uploadScript(scriptService, scriptPath):
    
    file = open(scriptPath)
    script = file.read()
    file.close()
    print script
    
    # first check if the script has already been uploaded
    existingScript = getScript(scriptService, scriptPath)
    if existingScript == None:
        # if not, upload new script
        scriptId = scriptService.uploadOfficialScript(scriptPath, script)
        print "Script uploaded with ID:", scriptId
    else:
        # if it has, edit the existing script
        scriptService.editScript(existingScript, script)
        print "Script ID: %s was edited" % existingScript.id.val
        

def getScript(scriptService, scriptPath):
    
    scripts = scriptService.getScripts()     # returns list of OriginalFiles     
        
    for s in scripts:
        print s.id.val
        print s.name.val  
        print s.path.val
        
    # make sure path starts with a slash. 
    # ** If you are a Windows client - will need to convert all path separators to "/" since server stores /path/to/script.py **
    if not scriptPath.startswith("/"):
        scriptPath =  "/" + scriptPath
        
    namedScripts = [s for s in scripts if s.path.val + s.name.val == scriptPath]
    
    if len(namedScripts) == 0:
        print "Didn't find any scripts with specified path: %s" % scriptPath
        return
    
    if len(namedScripts) > 1:
        print "Found more than one script with specified path: %s" % scriptPath
        
    return namedScripts[0]
    
    
def runScript(scriptService, scriptPath):
    
    scriptFile = getScript(scriptService, scriptPath)
    scriptId = scriptFile.id.val
    
    # Identify the script we want to run: Get all 'official' scripts and filter by path.  
    print "Running script: %s with ID: %s" % (scriptPath, scriptId)
    
    # make a map of all the parameters we want to pass to the script
    # keys are strings. Values must be omero.rtypes such as rlong, rbool, rlist. 
    map = {
        "message": omero.rtypes.rstring("Sending this message to the server!"),
    }  
            
    # The last parameter is how long to wait as an RInt
    proc = scriptService.runScript(scriptId, map, None)
    try:
        cb = omero.scripts.ProcessCallbackI(client, proc)
        while not cb.block(1000): # ms.
            pass
        cb.close()
        results = proc.getResults(0)    # ms
    finally:
        proc.close(False)
    
    # handle any results from the script 
    #print results.keys()
    if 'returnMessage' in results:
        print results['returnMessage'].getValue()
    if 'stdout' in results:
        origFile = results['stdout'].getValue()
        print "Script generated StdOut in file:" , origFile.getId().getValue()
    if 'stderr' in results:
        origFile = results['stderr'].getValue()
        print "Script generated StdErr in file:" , origFile.getId().getValue()


def readCommandArgs():
    """
    Read the arguments from the command line and put them in a map
    
    @return     A map of the command args, with keys: "host", "username", "password", "scriptId"
    """
    host = ""
    username = ""
    password = ""
    script = ""
    
    def usage():
        print "Usage: runscript --host host --username username --password password --script script"
    try:
        opts, args = getopt.getopt(sys.argv[1:] ,"h:u:p:s:", ["host=", "username=", "password=","script="])
    except getopt.GetoptError, err:          
        usage()                         
        sys.exit(2)                     
    returnMap = {}                  
    for opt, arg in opts: 
        if opt in ("-h","--host"):
            returnMap["host"] = arg
        elif opt in ("-u","--username"): 
            returnMap["username"] = arg    
        elif opt in ("-p","--password"): 
            returnMap["password"] = arg  
        elif opt in ("-s","--script"): 
            returnMap["script"] = arg  
                   
    return returnMap


if __name__ == "__main__":        
    commandArgs = readCommandArgs();
    
    # log on to the server, create client and session and scripting service
    client = omero.client(commandArgs["host"])
    session = client.createSession(commandArgs["username"], commandArgs["password"]);
    scriptPath = commandArgs["script"]
    scriptService = session.getScriptService()
    
    # upload script. Could comment this out if you just want to run. 
    uploadScript(scriptService, scriptPath)
    
    # run script
    #runScript(scriptService, scriptPath)
