#!/usr/bin/env python

"""

:author: Josh Moore <josh@glencoesoftware.com>

This script adds an OMERO user via blitz.

Copyright (c) 2007, Glencoe Software, Inc.

"""

from optparse import OptionParser

def main():
	version="%prog 1.0"
	usage = "usage: %prog [--sudo=... --sudopass=...] [optional fields] omename firstname [middlename] lastname"
	parser = OptionParser(usage=usage, version=version)
	parser.add_option("-c", "--config", dest="file",
			  help="read Ice configuration from FILE [default: %default]", metavar="FILE",
			  default="etc/ice.config")
	parser.add_option("-e", "--email", dest="email",
			  help="user's EMAIL address", metavar="EMAIL")
	parser.add_option("-i","--institute", dest="institute",
			  help="user's INSTITUTE's name", metavar="INSTITUTE") 
	parser.add_option("-g","--group", dest="group",
			  help="user's default GROUP [default: %default]", metavar="GROUP",
			  default="default")
	parser.add_option("-p","--password", dest="password",
			  help="user's PASSWORD [default: %default]", metavar="PASSWORD",
			  default=None)
	parser.add_option("-s","--sudo", dest="sudo",
			  help="Make call as different user", default=None)
	parser.add_option("-r","--sudopass", dest="sudopass",
			  help="Password for sudo user")
	(options, args) = parser.parse_args()

	l = len(args)
	if l == 3 :
		on, fn, ln = args
		mn = None
	elif l == 4 :
		on, fn, mn, ln = args
	else:
		parser.error("Must provide omename, firstname, and lastname")

	import omero, Ice
	from omero_model_ExperimenterI import ExperimenterI as Exp
	a = ["--Ice.Config="+options.file]
	c = omero.client(a)
	p = c.ic.getProperties()
	if options.sudo:
		if not options.sudopass:
			parser.error("Must provide --sudopass when using --sudo")
		p.setProperty(omero.constants.USERNAME,options.sudo)
		p.setProperty(omero.constants.PASSWORD,options.sudopass)
	c.createSession()
	e = Exp()
	e.omeName = omero.RString(on)
	e.firstName = omero.RString(fn)
	e.lastName = omero.RString(ln)
	e.middleName = omero.RString(mn)
	e.email = omero.RString(options.email)
	e.institute = omero.RString(options.institute)
	admin = c.getSession().getAdminService()
	id = admin.createUser(e,options.group)
	print "Added user "+str(id)
	admin.changeUserPassword(on,omero.RString(options.password))
	
	
if __name__ == "__main__":
	main()

