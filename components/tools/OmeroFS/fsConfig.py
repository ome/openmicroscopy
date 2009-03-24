### These should be in a config file or taken from an existing config

# omero server - should be sourced elsewhere
host = "localhost"
port = 4063

# root user - should be sourced elsewhere
username = "root"
password ="omero"

# session acquisition retry parameters
maxTries = 5      # number   
retryInterval = 3 # seconds. 

# dropdox directory settings
clientIdString = "DropBox"
clientAdapterName = "omerofs.DropBox"
dropBoxDir = "DropBox"
excludedUsers = ["root","guest"]

# fs server settings
serverIdString = "FSServer"
serverAdapterName = "omerofs.MonitorServer"
eventType = "Create"
pathMode = "Follow"
fileTypes = [".lsm"]
blacklist = [""]

# importer path relative to dist
climporter = "bin/omero import"

###
