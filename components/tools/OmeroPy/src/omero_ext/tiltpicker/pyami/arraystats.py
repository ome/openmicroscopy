#!/usr/bin/env python
# -*- coding: utf-8 -*-
'''
Functions for calculating statistics on arrays (either numpy or numarray)
A statistic is only calculated once on an array.  Future attempts to
calculate the statistic will return the previously calculated value.
This assumes that the values in the array are constant.  If the
array values have changed, and you want to recalculate the statistic,
then you must specify force=True.
'''

import weakattr
try:
	import numarray
	import numarray.nd_image
except:
	numarray = None

import numpy
if not hasattr(numpy, 'min'):
	numpy.min = numpy.minimum
	numpy.max = numpy.maximum

debug = False
def dprint(s):
	if debug:
		print s

## publicly available functions

def min(a, force=False):
	return calc_stat(a, 'min', force)

def max(a, force=False):
	return calc_stat(a, 'max', force)

def mean(a, force=False):
	return calc_stat(a, 'mean', force)

def std(a, force=False):
	return calc_stat(a, 'std', force)

def all(a, force=False):
	return calc_stat(a, 'all', force)


if numarray is None:
	def notimplemented(inputarray):
		raise NotImplementedError('numarray not available')
	numarray_min = numarray_max = numarray_mean = numarray_std = notimplemented
else:
	numarray_mean = numarray.nd_image.mean
	numarray_std = numarray.nd_image.standard_deviation
	def numarray_min(inputarray):
		'''
		faster than numarray.nd_image.min
		'''
		f = numarray.ravel(inputarray)
		i = numarray.argmin(f)
		return float(f[i])

	def numarray_max(inputarray):
		'''
		faster than numarray.nd_image.max
		'''
		f = numarray.ravel(inputarray)
		i = numarray.argmax(f)
		return float(f[i])

statnames = ('min','max','mean','std')
stat_functions = {
	'numpy': {
		'min': numpy.min,
		'max': numpy.max,
		'mean': numpy.mean,
		'std': numpy.std,
	},

	'numarray': {
		'min': numarray_min,
		'max': numarray_max,
		'mean': numarray_mean,
		'std': numarray_std,
	}
}

def calc_stat(a, stat, force=False):
	'''
	Get statistic either from cache or by calculating it.
	'''
	if stat == 'all':
		need = list(statnames)
	else:
		need = [stat]
	results = {}

	## try getting it from cache
	if not force:
		for statname in list(need):
			dprint(statname)
			try:
				results[statname] = getCachedStat(a, statname)
				need.remove(statname)
			except:
				pass

	dprint('calculating: %s' % (need,))

	## calculate the rest
	if numarray and isinstance(a, numarray.ArrayType):
		module = 'numarray'
	else:
		## numpy.ndarray and other sequences
		module = 'numpy'
	for statname in need:
		value = stat_functions[module][statname](a)
		results[statname] = value
		try:
			setCachedStat(a, statname, value)
		except:
			pass

	if stat == 'all':
		return results
	else:
		return results[stat]

def getCachedStats(a):
	try:
		return a.stats
	except AttributeError:
		return weakattr.get(a, 'stats')

def setCachedStats(a, stats):
	try:
		a.stats = stats
	except AttributeError:
		weakattr.set(a, 'stats', stats)

def getCachedStat(a, stat):
	return getCachedStats(a)[stat]

def setCachedStat(a, stat, value):
	try:
		stats = getCachedStats(a)
	except:
		stats = {}
		setCachedStats(a, stats)
	stats[stat] = value

if __name__ == '__main__':
	debug = True
