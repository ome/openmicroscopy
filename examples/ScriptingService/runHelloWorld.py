"""
 examples/ScriptingService/runHelloWorld.py

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

This script demonstrates how to use the scripting service to call a script on
an OMERO server. 
	
@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2
 
"""

import omero
import getopt, sys, os, subprocess
import omero_api_IScript_ice
from omero.rtypes import *

def run(commandArgs):

	# log on to the server with values from the command arguments, creating a client and session
	client = omero.client(commandArgs["host"])
	session = client.createSession(commandArgs["username"], commandArgs["password"]);
	scriptService = session.getScriptService();

	# make a map of all the parameters we want to pass to the script
	# keys are strings. Values must be omero.rtypes such as rlong, rbool, rlist. 
	map = {
		"message": omero.rtypes.rstring("Sending this message to the server!"),
	}  
	
	# make the parameter map into an rmap
	scriptId = commandArgs["scriptId"]
	argMap = omero.rtypes.rmap(map)
	
	# runs the script
	scriptService.runScript(scriptId, map)
	
	
def readCommandArgs():
	"""
	Read the arguments from the command line and put them in a map
	
	@return 	A map of the command args, with keys: "host", "username", "password", "scriptId"
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
	for opt, arg in opts: 
		if opt in ("-h","--host"):
			host = arg;
		elif opt in ("-u","--username"): 
			username = arg;	
		elif opt in ("-p","--password"): 
			password = arg;	
		elif opt in ("-s","--script"): 
			script = arg;	
	returnMap = {"host":host, "username":username, "password":password, "scriptId":long(script)}				
	return returnMap

if __name__ == "__main__":	    
	commandArgs = readCommandArgs();
	run(commandArgs);
