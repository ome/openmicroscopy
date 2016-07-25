
import requests

session = requests.Session()

# Start by getting supported versions from the base url...
r = session.get('http://localhost:4080/webgateway/api/')
# we get a list of versions
versions = r.json()['versions']
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

# Trying to access data without logging in
# gives 403 'Forbidden' and error message
r = session.get(projects_url)
assert r.status_code == 403
print 'Forbidden error:', r.json()['message']

# Trying to POST (E.g. login) without CSRF token fails
r = session.post(login_url, data={})
assert r.status_code == 403
print 'CSRF error:', r.json()['message']

# To login we need to get CSRF token
token_url = urls['token_url']
token = session.get(token_url).json()['token']
print 'CSRF token', token
# We add this to our session header
# Needed for all POST, PUT, DELETE requests
session.headers.update({'X-CSRFToken': token})

# List the servers available to connect to
servers = session.get(servers_url).json()['servers']
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


# Invalid login returns 403 and message
r = session.post(login_url, data={'username': 'bob'})
assert r.status_code == 403
print 'Login failed:', r.json()['message']


# Login with username, password and token
payload = {'username': 'will',
           'password': 'ome',
           'server': server['id']}
r = session.post(login_url, data=payload)
login_rsp = r.json()
# print "Login Response", login_rsp
assert r.status_code == 200

print 'LOGIN'
print r.json()

# This will give us 'Event Context' for this session
if 'success' in login_rsp:
    eventContext = login_rsp['eventContext']
    # print eventContext
    print 'Logged in! User ID', eventContext['userId']
else:
    # Login failure will contain 'message'
    print 'Login failed:', login_rsp['message']
    import sys
    sys.exit()

# With succesful login, request.session will contain
# OMERO session details and reconnect to OMERO on
# each subsequent call...

# List projects:
# Limit number of projects per page
payload = {'limit': 2}
data = session.get(projects_url, params=payload).json()
assert len(data['projects']) < 3
print "Projects:"
for p in data['projects']:
    print '  ', p['@id'], p['Name']

# Create a project:
# If we submit invalid data (E.g. no 'name')...
r = session.post(projects_url, {'description': 'API TEST'})
# ...get error message
assert r.status_code == 422
errors = r.json()['errors']
assert 'name' in errors
print "Errors", errors

# Re-submit with valid data...
r = session.post(projects_url, {'name': 'API TEST'})
assert r.status_code == 200
project = r.json()
print 'Created Project:', project['@id'], project['Name']
