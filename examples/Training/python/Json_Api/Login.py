#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2016-2017 University of Dundee & Open Microscopy Environment.
#                    All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import requests

from Parse_OMERO_Properties import USERNAME, PASSWORD, OMERO_WEB_HOST, \
    SERVER_NAME

session = requests.Session()

# Start by getting supported versions from the base url...
api_url = '%s/api/' % OMERO_WEB_HOST
print "Starting at:", api_url
r = session.get(api_url)
# we get a list of versions
versions = r.json()['data']

# use most recent version...
version = versions[-1]
# get the 'base' url
base_url = version['url:base']
r = session.get(base_url)
# which lists a bunch of urls as starting points
urls = r.json()
servers_url = urls['url:servers']
login_url = urls['url:login']
projects_url = urls['url:projects']
save_url = urls['url:save']
schema_url = urls['url:schema']

# To login we need to get CSRF token
token_url = urls['url:token']
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
# find one called SERVER_NAME
servers = [s for s in servers if s['server'] == SERVER_NAME]
if len(servers) < 1:
    raise Exception("Found no server called '%s'" % SERVER_NAME)
server = servers[0]

# Login with username, password and token
payload = {'username': USERNAME,
           'password': PASSWORD,
           # 'csrfmiddlewaretoken': token,  # Using CSRFToken in header instead
           'server': server['id']}

r = session.post(login_url, data=payload)
login_rsp = r.json()
assert r.status_code == 200
assert login_rsp['success']
eventContext = login_rsp['eventContext']
print 'eventContext', eventContext
# Can get our 'default' group
groupId = eventContext['groupId']

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
# Need to specify target group
url = save_url + '?group=' + str(groupId)
r = session.post(url, json={'Name': 'API TEST foo', '@type': projType})
assert r.status_code == 201
project = r.json()['data']
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
