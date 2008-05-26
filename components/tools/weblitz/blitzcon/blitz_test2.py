import sys, os

sys.path.append('icepy')
sys.path.append('lib')

import blitz_connector
import omero

#
# blitz_test2.py - manhole to test and debug blitz_connector classes
# 
# Copyright (c) 2007, 2008 Glencoe Software, Inc. All rights reserved.
# 
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# jason@glencoesoftware.com.

#c = blitz_connector.BlitzConnector('demo1','1omed','envy.glencoesoftware.com',9998)

if os.path.exists('etc/ice.config'):
    blitz_connector.BlitzConnector.ICE_CONFIG='etc/ice.config'

def login (user, passwd):
    global client
    global query
    global update
    global search

    client = blitz_connector.BlitzConnector(user, passwd,'127.0.0.1',4063)
    client.allow_thread_timeout = False
    if not client.connect():
        print "Can not connect" 
        return False
    query = client.getQueryService()
    update = client.getUpdateService()
    search = client.createSearchService()

    return True

def loginAsDemo ():
    return login('demo1', '1omed')

def loginAsRoot ():
    return login('root', 'ome')
    

def image_search_test ():
    loginAsRoot()
    global imgs
    global p
    global ds
    p = client.listProjects(only_owned=False).next()
    print "Project:", p.id, p.description
    ds = p.listChildren().next()
    print "Dataset:", ds.id, ds.description
    imgs = list(ds.listChildren())
    print "Images:"
    for i in imgs:
        print i.id,i.name


image_search_test()
#x = ds.listParents()
#print x.next()

def dosearch (text):
  #text = '+dump -whacky bin thing here'
  #split text in tokens
  some = []
  must = []
  none = []
  for token in text.split(' '):
      if token.startswith('+'):
          must.append(token[1:])
      elif token.startswith('-'):
          none.append(token[1:])
      else:
          some.append(token)

  search.onlyType('Project')
  search.bySomeMustNone(some, must, none)
  search.onlyType('Dataset')
  search.bySomeMustNone(some, must, none)
  search.onlyType('Image')
  search.bySomeMustNone(some, must, none)

for a in client.listExperimenters():
    d = a.getDetails()
    print d
    search.onlyType('Image')
    search.onlyOwnedBy(d)
    search.byFullText('description')
    print "Author: %s %s" % (d, search.hasNext())
    while search.hasNext():
        search.results()
