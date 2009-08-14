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

# This config item should be removed. See tickets: #1420 and #1421
config.excludedUsers = []

# A more general solution needs to be found for the various
# multi-file formats. For now this is how .dv files are handled...
# e.g. dv files may have an associated log file.
relatedTypes = {".dv" : [".log"]}
# e.g how long to wait after a dv file appears for a log file to appear (seconds).
waitTimes = {".dv": 30}
# e.g. how long after a log file appears should it be abandoned (seconds).
dropTimes = {".dv": 120}

# fs server settings
serverIdString = "FSServer"
serverAdapterName = "omerofs.MonitorServer"

# At present just looking for new files
eventType = "Create"

#    pyinotify as it stands misses some events when a new
#    directory is created, "Follow" will not report all events
#    until this issue is fixed.
pathMode = "Follow"

# file extensions to watch for.
#     (although only .ome.tif are of genuine interest.)
fileTypes = set([".jpg", ".lsm", ".dv", ".tif", ".tiff"])
# add the related extensions
for ext in relatedTypes.keys():
    for extra in relatedTypes[ext]:
        fileTypes.add(extra)

# subdirectories to exclude. NOT CURRENTLY USED
blacklist = [""]

# importer path relative to dist or base install directory
climporter = "bin/omero import"

# Values used only by the test client
# dropdox directory settings
testClientIdString = "FSTestClient"
testClientAdapterName = "omerofs.FSTestClient"
testBase = "/OMERO/"
##testBase = "/OMERODEV/"

###
