"""
 examples/ScriptingService/HelloWorld.py

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

This script demonstrates the bare minimum framework of a script that can be run by the 
scripting service on an OMERO server. 
It defines a name, description and parameter list for the script. 
	
@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2
 
"""

import omero
import omero.scripts as scripts


if __name__ == "__main__":
	
	"""
	The main entry point of the script, as called by the client via the scripting service, passing the required parameters. 
	"""
	
	client = scripts.client('HelloWorld.py', 'Hello World example script', 
	scripts.String("message", optional=True).inout())	# 	Message to pass to the script. 
	
	session = client.getSession();
	
	# process the list of args above. Not scrictly necessary, but useful for more complex scripts
	parameterMap = {}
	for key in client.getInputKeys():
		if client.getInput(key):
			parameterMap[key] = client.getInput(key).getValue()		# convert from rtype to value (String, Integer etc)
	
	# now we can work with arguments in our parameterMap
	if "message" in parameterMap:
		print "Hello World"					
		print parameterMap["message"]
	else:
		# print statments (std.out), if any, will go into one file on the server (E.g. /OMERO/Files/001) 
		print "No message parameter"	
		
		# Exceptions (std.err), if any, will go in another file on the server (E.g. /OMERO/Files/002) 
		raise Exception("message parameter was not in the argument list")	
		
	
	