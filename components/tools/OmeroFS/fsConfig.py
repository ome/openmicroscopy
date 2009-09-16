### These should be in a config file or taken from an existing config

# omero server - should be sourced elsewhere
host = "localhost"
port = 4063

# dropdox directory settings
clientIdString = "DropBox"
clientAdapterName = "omerofs.DropBox"

# fs server settings
serverIdString = "FSServer"
serverAdapterName = "omerofs.MonitorServer"

# A more general solution needs to be found for the various
# multi-file formats. For now this is how .dv files are handled...
# e.g. dv files may have an associated log file.
relatedTypes = {".dv" : [".log"]}
# e.g how long to wait after a dv file appears for a log file to appear (seconds).
waitTimes = {".dv": 30}
# e.g. how long after a log file appears should it be abandoned (seconds).
dropTimes = {".dv": 120}

# file extensions to watch for.
#     (although only .ome.tif are of genuine interest.)
#whitelist = [".jpg", ".lsm", ".dv", ".tif", ".tiff"]
# An empty filelist equates to a wildcard extension, 
# watch for and report all file types
whitelist = []
fileTypes = set(whitelist)
# add the related extensions
for ext in relatedTypes.keys():
    if ext in fileTypes:
        for extra in relatedTypes[ext]:
            fileTypes.add(extra)

# subdirectories to exclude. NOT CURRENTLY USED
blacklist = [""]

# importer path relative to dist or base install directory
#climporter = "bin/omero import"

# This path may need to be used when testing if the above path causes problems.
climporter = "../components/tools/OmeroImporter/target/importer-cli"

# Values used only by the test client
# dropdox directory settings
#testClientIdString = "FSTestClient"
#testClientAdapterName = "omerofs.FSTestClient"
##testBase = "/OMERO/"
##testBase = "/OMERODEV/"

###
