#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# OMERO Registry
# Copyright 2007 Glencoe Software, Inc.  All Rights Reserved.
#
# NOTES:
# 9090/test seems not to work. check with and without slash
# should do all comparisons by stripping slashes

version = "Beta-4.0-RC2"
config = {
    'port':9998,
    'version':version,
    'agentprefix':'OMERO',
    'redirect':'http://trac.openmicroscopy.org.uk/ome/wiki/UpgradeCheck',
    'upgrade':'Please upgrade to %s See http://trac.openmicroscopy.org.uk/ome for the latest version' % version
    }

from twisted.application import internet, service
from twisted.web import static, server
from twisted.web.resource import Resource
from twisted.web.util import redirectTo

import logging, types, re
import db

VALID = re.compile("^[\-\w.]+$")
INVALID = re.compile("\s")

# Timestamp to twistd.log to correlate with the access.log
print "Starting OMERO.registry"

class ReportResource(Resource):
    """
    Main entity in this file.
    This class is used to render all GET calls made to this host and port.
    See render_GET.
    """

    def __init__(self):
        """
        Currently unused initialization.
        """
        Resource.__init__(self)
        self.db = db.accessdb("sqlite.db")

    def __del__(self):
        self.db.close()

    def render_GET(self, request):
        """
        Main method in this file. Uses the GET arguments to determine
        whether or not an upgrade command should be sent. If the user-agent
        for the connection is wrong, or an argument is missing, then
        a redirect will be sent.
        """

        # Debug
        import os
        try:
            if os.environ["DEBUG"] == "1":
                import pdb
                pdb.set_trace()
        except KeyError:
            pass

        #
        # Input
        #

        args = dict(request.args)
        print args
        for key in args.keys():
            value = args[key]
            if isinstance(value, types.ListType):
                args[key] = value[0]

        headers = request.getAllHeaders()
        if headers.has_key("x-forwarded-for"):
            __ip = headers["x-forwarded-for"]
	else:
            __ip = request.getClientIP()
	if not __ip:
            __ip = "unknown"
	# Here we deal with IP addresses which are comma separated
	# Take the last one in the list and strip white-space
	if __ip.find(",") >= 0:
	    print "WARN: CSV IP %s " % __ip
	    __ip = __ip.split(",")[-1].strip()
	args["ip"] = __ip

        if not args.has_key('poll'):
            args['poll'] = 'unknown'
        else:
            try:
                args['poll'] = int(args['poll'])
            except ValueError:
                args['poll'] = 'unknown'

        if not args.has_key('java.vm.vendor'):
            args['java.vm.vendor'] = 'unknown'
        if not args.has_key('java.runtime.version'):
            args['java.runtime.version'] = 'unknown'
        if not args.has_key('os.name'):
            args['os.name'] = 'unknown'
        if not args.has_key('os.arch'):
            args['os.arch'] = 'unknown'
        if not args.has_key('os.version'):
            args['os.version'] = 'unknown'
        #
        # Output
        #

        # If the "API" is not adhered to (there are missing
        # parameters, then user will also be redirected.
        if not args.has_key('version'):
            return self.redirect(args, request)
        args['version'] = args['version'].replace("\n","NEWLINE")

        # If not an "OMERO" user-agent, redirect to the page on
        # upgrade checks.
        userAgent = request.getHeader('user-agent')
        if not userAgent.startswith(config['agentprefix']):
            return self.redirect(args, request)

        # Otherwise, record successful hit
        # def hit(self, ip, version, poll, vmvendor, vmruntime, osname, osarch, osversion, other):
        self.db.hit(args["ip"], args["version"], args["poll"],\
                args["java.vm.vendor"],args["java.runtime.version"],\
                args["os.name"],args["os.arch"],args["os.version"],\
                str(headers), "", userAgent)

        # And, return either an upgrade comment or blank
        if args['version'] != config['version']:
                output = config["upgrade"]
        else:
                output = ""
        return (output)


    def redirect(self, args, request):
        print "Redirect: %(ip)s" % args
        return redirectTo(config['redirect'], request)


    def getChild(self, name, request):
        """
        Guarantees that whatever resource is requested
        it will always be handled by the render_GET method.
        """
	return self

#
# This is the standard bit which lets twistd
# do its magic. Should not need to be modified.
#

root = ReportResource()
application = service.Application('web')
site = server.Site(root)
sc = service.IServiceCollection(application)
i = internet.TCPServer(config['port'], site)
i.setServiceParent(sc)

