#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
#                    All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import requests

session = requests.Session()

# Start by getting supported versions from the base url...
r = session.get('http://localhost:4080/api/')
# we get a list of versions
versions = r.json()['data']
print 'Versions', versions

# use most recent version...
version = versions[-1]
# get the 'base' url
base_url = version['base_url']
r = session.get(base_url)
# which lists a bunch of urls as starting points
urls = r.json()
servers_url = urls['servers_url']
login_url = urls['login_url']
projects_url = urls['projects_url']
save_url = urls['save_url']
schema_url = urls['schema_url']

# To login we need to get CSRF token
token_url = urls['token_url']
token = session.get(token_url).json()['data']
print 'CSRF token', token
# We add this to our session header
# Needed for all POST, PUT, DELETE requests
session.headers.update({'X-CSRFToken': token,
                        'Referer': login_url})

# List the servers available to connect to
servers = session.get(servers_url).json()['data']
print 'Servers:'
for s in servers:
    print '-id:', s['id']
    print ' name:', s['server']
    print ' host:', s['host']
    print ' port:', s['port']
# find one called 'omero'
servers = [s for s in servers if s['server'] == 'omero']
if len(servers) < 1:
    print "Found no server called 'omero'"
server = servers[0]

# Login with username, password and token
payload = {'username': 'ben',
           'password': 'secret',
           'server': server['id']}

r = session.post(login_url, data=payload)
login_rsp = r.json()
assert r.status_code == 200
assert login_rsp['success']
eventContext = login_rsp['eventContext']
print 'eventContext', eventContext

# With succesful login, request.session will contain
# OMERO session details and reconnect to OMERO on
# each subsequent call...

# List projects:
# Limit number of projects per page
payload = {'limit': 2}
data = session.get(projects_url, params=payload).json()
assert len(data['data']) < 3
print "Projects:"
for p in data['data']:
    print '  ', p['@id'], p['Name']

# Create a project:
projType = schema_url + '#Project'
r = session.post(save_url, json={'name': 'API TEST foo',
                                 '@type': projType})
assert r.status_code == 200
project = r.json()
project_id = project['@id']
print 'Created Project:', project_id, project['Name']

# Get project by ID
project_url = projects_url + str(project_id) + '/'
r = session.get(project_url)
project = r.json()
print project

# Update a project
project['Name'] = 'API test updated'
r = session.put(save_url, json=project)

# Delete a project:
r = session.delete(project_url)
