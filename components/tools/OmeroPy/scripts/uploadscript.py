import omero
import getopt, sys, os, subprocess
import omero_api_IScript_ice

def uploadScript(commandArgs):
	client = omero.client(commandArgs["host"])
	session = client.createSession(commandArgs["username"], commandArgs["password"]);
	scriptService = session.getScriptService();
	file = open(commandArgs["script"])
	script = file.read();
	file.close();
	print script
	print scriptService.uploadScript(script)
	
def readCommandArgs():
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
