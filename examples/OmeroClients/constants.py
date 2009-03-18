import omero

print "By default, no method call can pass more than %s kb" % omero.constants.MESSAGESIZEMAX
print "By default, client.createSession() will wait %s seconds for a connection" % CONNECTTIMEOUT/1000
