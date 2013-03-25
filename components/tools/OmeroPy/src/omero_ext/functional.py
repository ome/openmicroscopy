#!/usr/bin/env python
# -*- coding: utf-8 -*-
# From: http://code.djangoproject.com/browser/django/trunk/django/utils/functional.py
# License for code in this file that was taken from Python 2.5.

# PYTHON SOFTWARE FOUNDATION LICENSE VERSION 2
# --------------------------------------------
#
# 1. This LICENSE AGREEMENT is between the Python Software Foundation
# ("PSF"), and the Individual or Organization ("Licensee") accessing and
# otherwise using this software ("Python") in source or binary form and
# its associated documentation.
#
# 2. Subject to the terms and conditions of this License Agreement, PSF
# hereby grants Licensee a nonexclusive, royalty-free, world-wide
# license to reproduce, analyze, test, perform and/or display publicly,
# prepare derivative works, distribute, and otherwise use Python
# alone or in any derivative version, provided, however, that PSF's
# License Agreement and PSF's notice of copyright, i.e., "Copyright (c)
# 2001, 2002, 2003, 2004, 2005, 2006, 2007 Python Software Foundation;
# All Rights Reserved" are retained in Python alone or in any derivative
# version prepared by Licensee.
#
# 3. In the event Licensee prepares a derivative work that is based on
# or incorporates Python or any part thereof, and wants to make
# the derivative work available to others as provided herein, then
# Licensee hereby agrees to include in any such work a brief summary of
# the changes made to Python.
#
# 4. PSF is making Python available to Licensee on an "AS IS"
# basis.  PSF MAKES NO REPRESENTATIONS OR WARRANTIES, EXPRESS OR
# IMPLIED.  BY WAY OF EXAMPLE, BUT NOT LIMITATION, PSF MAKES NO AND
# DISCLAIMS ANY REPRESENTATION OR WARRANTY OF MERCHANTABILITY OR FITNESS
# FOR ANY PARTICULAR PURPOSE OR THAT THE USE OF PYTHON WILL NOT
# INFRINGE ANY THIRD PARTY RIGHTS.
#
# 5. PSF SHALL NOT BE LIABLE TO LICENSEE OR ANY OTHER USERS OF PYTHON
# FOR ANY INCIDENTAL, SPECIAL, OR CONSEQUENTIAL DAMAGES OR LOSS AS
# A RESULT OF MODIFYING, DISTRIBUTING, OR OTHERWISE USING PYTHON,
# OR ANY DERIVATIVE THEREOF, EVEN IF ADVISED OF THE POSSIBILITY THEREOF.
#
# 6. This License Agreement will automatically terminate upon a material
# breach of its terms and conditions.
#
# 7. Nothing in this License Agreement shall be deemed to create any
# relationship of agency, partnership, or joint venture between PSF and
# Licensee.  This License Agreement does not grant permission to use PSF
# trademarks or trade name in a trademark sense to endorse or promote
# products or services of Licensee, or any third party.
#
# 8. By copying, installing or otherwise using Python, Licensee
# agrees to be bound by the terms and conditions of this License
# Agreement.


def curry(_curried_func, *args, **kwargs):
    def _curried(*moreargs, **morekwargs):
        return _curried_func(*(args+moreargs), **dict(kwargs, **morekwargs))
    return _curried

### Begin from Python 2.5 functools.py ########################################

# Summary of changes made to the Python 2.5 code below:
#   * swapped ``partial`` for ``curry`` to maintain backwards-compatibility
#     in Django.
#   * Wrapped the ``setattr`` call in ``update_wrapper`` with a try-except
#     block to make it compatible with Python 2.3, which doesn't allow
#     assigning to ``__name__``.

# Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006, 2007 Python Software Foundation.
# All Rights Reserved.

###############################################################################

# update_wrapper() and wraps() are tools to help write
# wrapper functions that can handle naive introspection

WRAPPER_ASSIGNMENTS = ('__module__', '__name__', '__doc__')
WRAPPER_UPDATES = ('__dict__',)
def update_wrapper(wrapper,
                   wrapped,
                   assigned = WRAPPER_ASSIGNMENTS,
                   updated = WRAPPER_UPDATES):
    """Update a wrapper function to look like the wrapped function

       wrapper is the function to be updated
       wrapped is the original function
       assigned is a tuple naming the attributes assigned directly
       from the wrapped function to the wrapper function (defaults to
       functools.WRAPPER_ASSIGNMENTS)
       updated is a tuple naming the attributes off the wrapper that
       are updated with the corresponding attribute from the wrapped
       function (defaults to functools.WRAPPER_UPDATES)
    """
    for attr in assigned:
        try:
            setattr(wrapper, attr, getattr(wrapped, attr))
        except TypeError: # Python 2.3 doesn't allow assigning to __name__.
            pass
    for attr in updated:
        getattr(wrapper, attr).update(getattr(wrapped, attr))
    # Return the wrapper so this can be used as a decorator via curry()
    return wrapper

def wraps(wrapped,
          assigned = WRAPPER_ASSIGNMENTS,
          updated = WRAPPER_UPDATES):
    """Decorator factory to apply update_wrapper() to a wrapper function

       Returns a decorator that invokes update_wrapper() with the decorated
       function as the wrapper argument and the arguments to wraps() as the
       remaining arguments. Default arguments are as for update_wrapper().
       This is a convenience function to simplify applying curry() to
       update_wrapper().
    """
    return curry(update_wrapper, wrapped=wrapped,
                 assigned=assigned, updated=updated)

### End from Python 2.5 functools.py ##########################################

def memoize(func, cache, num_args):
    """
    Wrap a function so that results for any argument tuple are stored in
    'cache'. Note that the args to the function must be usable as dictionary
    keys.

    Only the first num_args are considered when creating the key.
    """
    def wrapper(*args):
        mem_args = args[:num_args]
        if mem_args in cache:
            return cache[mem_args]
        result = func(*args)
        cache[mem_args] = result
        return result
    return wraps(func)(wrapper)

class Promise(object):
    """
    This is just a base class for the proxy class created in
    the closure of the lazy function. It can be used to recognize
    promises in code.
    """
    pass

def lazy(func, *resultclasses):
    """
    Turns any callable into a lazy evaluated callable. You need to give result
    classes or types -- at least one is needed so that the automatic forcing of
    the lazy evaluation code is triggered. Results are not memoized; the
    function is evaluated on every access.
    """
    class __proxy__(Promise):
        """
        Encapsulate a function call and act as a proxy for methods that are
        called on the result of that function. The function is not evaluated
        until one of the methods on the result is called.
        """
        __dispatch = None

        def __init__(self, args, kw):
            self.__func = func
            self.__args = args
            self.__kw = kw
            if self.__dispatch is None:
                self.__prepare_class__()

        def __prepare_class__(cls):
            cls.__dispatch = {}
            for resultclass in resultclasses:
                cls.__dispatch[resultclass] = {}
                for (k, v) in resultclass.__dict__.items():
                    if hasattr(cls, k):
                        continue
                    setattr(cls, k, cls.__promise__(resultclass, k, v))
            cls._delegate_str = str in resultclasses
            cls._delegate_unicode = unicode in resultclasses
            assert not (cls._delegate_str and cls._delegate_unicode), "Cannot call lazy() with both str and unicode return types."
            if cls._delegate_unicode:
                cls.__unicode__ = cls.__unicode_cast
            elif cls._delegate_str:
                cls.__str__ = cls.__str_cast
        __prepare_class__ = classmethod(__prepare_class__)

        def __promise__(cls, klass, funcname, func):
            # Builds a wrapper around some magic method and registers that magic
            # method for the given type and method name.
            def __wrapper__(self, *args, **kw):
                # Automatically triggers the evaluation of a lazy value and
                # applies the given magic method of the result type.
                res = self.__func(*self.__args, **self.__kw)
                for t in type(res).mro():
                    if t in self.__dispatch:
                        return self.__dispatch[t][funcname](res, *args, **kw)
                raise TypeError("Lazy object returned unexpected type.")

            if klass not in cls.__dispatch:
                cls.__dispatch[klass] = {}
            cls.__dispatch[klass][funcname] = func
            return __wrapper__
        __promise__ = classmethod(__promise__)

        def __unicode_cast(self):
            return self.__func(*self.__args, **self.__kw)

        def __str_cast(self):
            return str(self.__func(*self.__args, **self.__kw))

        def __cmp__(self, rhs):
            if self._delegate_str:
                s = str(self.__func(*self.__args, **self.__kw))
            elif self._delegate_unicode:
                s = unicode(self.__func(*self.__args, **self.__kw))
            else:
                s = self.__func(*self.__args, **self.__kw)
            if isinstance(rhs, Promise):
                return -cmp(rhs, s)
            else:
                return cmp(s, rhs)

        def __mod__(self, rhs):
            if self._delegate_str:
                return str(self) % rhs
            elif self._delegate_unicode:
                return unicode(self) % rhs
            else:
                raise AssertionError('__mod__ not supported for non-string types')

        def __deepcopy__(self, memo):
            # Instances of this class are effectively immutable. It's just a
            # collection of functions. So we don't need to do anything
            # complicated for copying.
            memo[id(self)] = self
            return self

    def __wrapper__(*args, **kw):
        # Creates the proxy object, instead of the actual value.
        return __proxy__(args, kw)

    return wraps(func)(__wrapper__)

def allow_lazy(func, *resultclasses):
    """
    A decorator that allows a function to be called with one or more lazy
    arguments. If none of the args are lazy, the function is evaluated
    immediately, otherwise a __proxy__ is returned that will evaluate the
    function when needed.
    """
    def wrapper(*args, **kwargs):
        for arg in list(args) + kwargs.values():
            if isinstance(arg, Promise):
                break
        else:
            return func(*args, **kwargs)
        return lazy(func, *resultclasses)(*args, **kwargs)
    return wraps(func)(wrapper)

class LazyObject(object):
    """
    A wrapper for another class that can be used to delay instantiation of the
    wrapped class.

    This is useful, for example, if the wrapped class needs to use Django
    settings at creation time: we want to permit it to be imported without
    accessing settings.
    """
    def __init__(self):
        self._wrapped = None

    def __getattr__(self, name):
        if self._wrapped is None:
            self._setup()
        if name == "__members__":
            # Used to implement dir(obj)
            return self._wrapped.get_all_members()
        return getattr(self._wrapped, name)

    def __setattr__(self, name, value):
        if name == "_wrapped":
            # Assign to __dict__ to avoid infinite __setattr__ loops.
            self.__dict__["_wrapped"] = value
        else:
            if self._wrapped is None:
                self._setup()
            setattr(self._wrapped, name, value)

    def _setup(self):
        """
        Must be implemented by subclasses to initialise the wrapped object.
        """
        raise NotImplementedError

