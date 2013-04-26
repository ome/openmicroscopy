#!/usr/bin/env python
# -*- coding: utf-8 -*-
import weakref
import threading

keyobject = weakref.WeakValueDictionary()
keyvalue = weakref.WeakKeyDictionary()
keylookup = weakref.WeakValueDictionary()
threadlock = threading.RLock()

class Key(object):
	pass

def key(obj, attrname):
	k = str(id(obj)) + attrname
	try:
		return keylookup[k]
	except KeyError:
		newkey = Key()
		keylookup[k] = newkey
		return newkey

def set(obj, attrname, attrvalue):
	threadlock.acquire()
	try:
		k = key(obj, attrname)
		keyobject[k] = obj
		keyvalue[k] = attrvalue
	finally:
		threadlock.release()

def get(obj, attrname):
	threadlock.acquire()
	try:
		k = key(obj, attrname)
		try:
			return keyvalue[k]
		except KeyError:
			raise AttributeError("'%s' object has no attribute '%s'" % (type(obj), attrname))
	finally:
		threadlock.release()

def debug():
	print 'KEYOBJECT', len(keyobject)
	print 'KEYVALUE', len(keyvalue)
	print 'KEYLOOKUP', len(keylookup)

if __name__ == '__main__':
	class MyThing(object):
		pass

	debug()
	for i in range(45):
		a = MyThing()
		set(a, 'asdf', i)
		print 'ASDF', get(a, 'asdf')
		debug()
