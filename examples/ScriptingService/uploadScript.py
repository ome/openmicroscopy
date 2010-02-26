"""
 examples/ScriptingService/uploadScript.py

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

This script demonstrates how to use the scripting service to upload a script to
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
import getopt, sys, os, subprocess
import omero_api_IScript_ice

def uploadScript(commandArgs):
	
	# log on to the server, create client and session
	client = omero.client(commandArgs["host"])
	session = client.createSession(commandArgs["username"], commandArgs["password"]);
	
	# get the scripting service and upload the script
	scriptService = session.getScriptService();
	file = open(commandArgs["script"])
	script = file.read();
	file.close();
	print script
	# prints the script ID to the command line. This can be used to run the script. E.g. see runHelloWorld.py
	print scriptService.uploadScript(script)
	
def readCommandArgs():
	"""
	Read the arguments from the command line and put them in a map
	Arguments are:
	"host"			The server location: E.g. omero.openmicroscopy.org.uk
	"username"		Username
	"password"		Password
	"script"		The path/name of the script. E.g. /Examples/HelloWorld.py
	
	@return 	A map of the command args, with keys: "host", "username", "password", "script"
	"""
	
	host = ""
	username = ""
	password = ""
	script = ""
	
	def usage():
		print "Usage: uploadscript --host host --username username --password password --script script"
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
	returnMap = {"host":host, "username":username, "password":password, "script":script}				
	return returnMap

if __name__ == "__main__":	    
	commandArgs = readCommandArgs();
	uploadScript(commandArgs);
