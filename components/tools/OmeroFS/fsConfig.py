### These should be in a config file or taken from an existing config

# omero server - should be sourced elsewhere
host = "localhost"
port = 4063

# session acquisition retry parameters
# these deal with the time it can take the server to start.
maxTries = 5      # number   
retryInterval = 3 # seconds. 

# dropdox directory settings
clientIdString = "DropBox"
clientAdapterName = "omerofs.DropBox"
dropBoxDir = "DropBox"
# root and guest can't import 
# (these could be excluded using blacklist below)
excludedUsers = ["root", "guest"]
# e.g. dv files may have an associated log file.
relatedTypes = {".dv" : [".log"]}
# e.g. how long to wait after a dv file appears for a log file to appear (seconds).
waitTimes = {".dv": 30}
# e.g. how long after a log file appears should it be abandoned (seconds).
dropTimes = {".dv": 120}

# fs server settings
serverIdString = "FSServer"
serverAdapterName = "omerofs.MonitorServer"
# At present just looking for new files
eventType = "Create"
pathMode = "Follow"
# file extensions to watch for
fileTypes = set([".lsm", ".dv", ".tif", ".tiff"])
# add the related extensions
for ext in relatedTypes.keys():
    for extra in relatedTypes[ext]:
        fileTypes.add(extra)
# subdirectories to exclude. NOT CURRENTLY USED
blacklist = [""]

# importer path relative to dist or base install directory
climporter = "bin/omero import"

###
