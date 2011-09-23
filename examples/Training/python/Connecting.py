# This script shows a simple connection to OMERO, printing details of the connection.
# NB: You will need to edit the 'host', 'user' and 'password' fields before running.

# Connect to the Python Blitz Gateway

# See OmeroPy/Gateway for more info

# import the libraries we need
from omero.gateway import BlitzGateway
from omero.rtypes import *
# use these login details:
host = 'localhost'
user = 'will'
password = 'ome'
# create a connection
conn = BlitzGateway(user, password, host=host, port=4064)
conn.connect()

# Using secure connection.

# By default, once we have logged in, data transfer is not encrypted (faster)

# To use secure connection
conn.setSecure(True)

# Current session details

# By default, you will have logged into your 'current' group in OMERO. This can be changed by
# switching group in the OMERO insight or web clients.

user = conn.getUser()
print "Current user:"
print "   ID:", user.getId()
print "   Username:", user.getName()
print "   Full Name:", user.getFullName()
print "Member of:"
for g in conn.getGroupsMemberOf():
    print "   ID:", g.getName(), " Name:", g.getId()
group = conn.getGroupFromContext()
print "Current group: ", group.getName()
print "Current group Members:"
for groupExpLink in group.copyGroupExperimenterMap():
    print "   ID:", groupExpLink.child.id.val, " Username:", groupExpLink.child.omeName.val
# The 'context' of our current session
ctx = conn.getEventContext()
# print ctx     # for more info

# Close connection

# When you're done, close the session to free up server resources. 

conn._closeSession()