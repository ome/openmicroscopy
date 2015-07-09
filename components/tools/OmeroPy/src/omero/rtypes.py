#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
::
    /*
     *   $Id$
     *
     *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
     *   Use is subject to license terms supplied in LICENSE.txt
     *
     */
"""

"""
Module which is responsible for creating rtypes from static
factory methods. Where possible, factory methods return cached values
(the fly-weight pattern) such that <code>rbool(true) == rbool(true)</code>
might hold true.

This module is meant to be kept in sync with the abstract Java class
omero.rtypes as well as the omero/rtypes.{h,cpp} files.
"""

import omero
import Ice
import IceImport
IceImport.load("omero_RTypes_ice")
IceImport.load("omero_Scripts_ice")
IceImport.load("omero_model_RTypes_ice")

from types import StringTypes


def rtype(val):
    """
    If None or an RType, return the argument itself. Otherwise,
    attempts to dispatch to the other omero.rtypes.* static methods
    to create a proper {@link RType} subclass by checking the type
    of the input. If no conversion is found, a {@link ClientError} is
    thrown.

    Note: unlike the statically typed languages, the rtype implementation
    in Python is somewhat limited by the lack of types (float v. double)
    All float-like values will produce an omero.RFloat subclass. Similar
    problems may arise with rlong and rint
    """
    if val is None:
        return None
    elif isinstance(val, omero.RType):
        return val
    elif isinstance(val, bool):
        return rbool(val)
    elif isinstance(val, int):
        return rint(val)
    elif isinstance(val, long):
        return rlong(val)
    elif isinstance(val, float):
        return rfloat(val)
    elif isinstance(val, str):
        return rstring(val)
    elif isinstance(val, omero.model.IObject):
        return robject(val)
    elif isinstance(val, omero.Internal):
        return rinternal(val)
    elif isinstance(val, list):
        return rlist(val)
    elif isinstance(val, set):
        return rset(val)
    elif isinstance(val, dict):
        return rmap(val)
    else:
        raise omero.ClientError("Cannot handle conversion from: %s"
                                % type(val))


def wrap(val, cache=None):
    """
    """
    if cache is None:
        cache = {}
    elif id(val) in cache:
        return cache[id(val)]

    if val is None:
        return None
    elif isinstance(val, (list, tuple)):
        rv = rlist()
        cache[id(val)] = rv
        for x in val:
            rv.val.append(wrap(x, cache))
    elif isinstance(val, set):
        rv = rset()
        cache[id(val)] = rv
        for x in val:
            rv.val.add(wrap(x, cache))
    elif isinstance(val, dict):
        rv = rmap()
        cache[id(val)] = rv
        for k, v in val.items():
            rv.val[k] = wrap(v, cache)
        rv._validate()
    elif isinstance(val, omero.RType):
        rv = val
    else:
        rv = rtype(val)

    return rv


def unwrap(val, cache=None):
    """
    """
    if cache is None:
        cache = {}
    elif id(val) in cache:
        return cache[id(val)]

    if val is None:
        return None
    elif isinstance(val, (list, tuple)):
        rv = []
        cache[id(val)] = rv
        for x in val:
            rv.append(unwrap(x, cache))
    elif isinstance(val, set):
        rv = set()
        cache[id(val)] = rv
        for x in val:
            rv.add(unwrap(x, cache))
    elif isinstance(val, dict):
        rv = {}
        cache[id(val)] = rv
        for k, v in val.items():
            rv[unwrap(k, cache)] = unwrap(v, cache)
    elif isinstance(val, omero.RCollection):
        if val.val is None:
            rv = None
            cache[id(val)] = None
        else:
            rv = []
            cache[id(val)] = rv
            for x in val.val:
                rv.append(unwrap(x, cache))
    elif isinstance(val, omero.RMap):
        if val.val is None:
            rv = None
            cache[id(val)] = None
        else:
            rv = {}
            cache[id(val)] = rv
            for k, v in val.val.items():
                rv[unwrap(k, cache)] = unwrap(v, cache)
    elif isinstance(val, omero.RType):  # Non-recursive
        rv = val.val
        cache[id(val)] = rv
    else:
        rv = val

    return rv


# Static factory methods (primitives)
# =========================================================================


def rbool(val):
    """
    Returns the argument itself if None or an instance of RBool.
    Otherwise, checks the value for"trueness" and returns either
    rtrue or rfalse.
    """
    if val is None or isinstance(val, omero.RBool):
        return val
    elif val:
        return rtrue
    else:
        return rfalse


def rdouble(val):
    """
    Returns the argument itself if None or an instance of RDouble.
    Otherwise, assigns a coerced float to the value of a new RDouble.
    """
    if val is None or isinstance(val, omero.RDouble):
        return val
    return RDoubleI(val)


def rfloat(val):
    """
    Returns the argument itself if None or an instance of RFloat.
    Otherwise, assigns a coerced float to the value of a new RFloat.
    """
    if val is None or isinstance(val, omero.RFloat):
        return val
    return RFloatI(val)


def rint(val):
    """
    Returns the argument itself if None or an instance of RInt.
    If the argument is 0, rint0 is returned.
    Otherwise, assigns a coerced int to the value of a new RInt
    """
    if val is None or isinstance(val, omero.RInt):
        return val
    elif val == 0:
        return rint0
    return RIntI(val)


def rlong(val):
    """
    Returns the argument itself if None or an instance of RLong.
    If the argument is 0, rlong 0 is returned.
    Otherwise, assigns a coerced int to the value of a new RLong
    """
    if val is None or isinstance(val, omero.RLong):
        return val
    elif val == 0:
        return rlong0
    return RLongI(val)


def rtime(val):
    """
    Returns the argument itself if None or an instance of RTime.
    Otherwise, assigns a coerced long to the value of a new RTime
    """
    if val is None or isinstance(val, omero.RTime):
        return val
    return RTimeI(val)

# Static factory methods (objects)
# =========================================================================


def rinternal(val):
    """
    If argument is None, returns rnullinternal.
    If an RInternal, returns the argument itself.
    Otherwise creates a new RInternal.
    """
    if val is None:
        return rnullinternal
    elif isinstance(val, omero.RInternal):
        return val
    elif isinstance(val, omero.Internal):
        return RInternalI(val)
    else:
        raise ValueError("Not Internal type: %s" % type(val))


def robject(val):
    """
    If argument is None, returns rnullobject.
    If an RObject, returns the argument itself.
    Otherwise creates a new RObject
    """
    if val is None:
        return rnullobject
    elif isinstance(val, omero.RObject):
        return val
    elif isinstance(val, omero.model.IObject):
        return RObjectI(val)
    else:
        raise ValueError("Not IObject type: %s" % type(val))


def rclass(val):
    """
    If argument is None or "", returns emptyclass.
    If an RClass, returns the argument itself.
    Otherwise creates a new RClass
    """
    if val is None:
        return remptyclass
    elif isinstance(val, omero.RClass):
        return val
    elif isinstance(val, str):
        if len(val) == 0:
            return remptyclass
        else:
            return RClassI(val)
    raise ValueError("Not string type: %s" % type(val))


def rstring(val):
    """
    If argument is None or "", returns emptystring.
    If an RString, returns the argument itself.
    Otherwise creates a new RString
    """
    if val is None:
        return remptystr
    elif isinstance(val, omero.RString):
        return val
    elif isinstance(val, StringTypes):
        if len(val) == 0:
            return remptystr
        else:
            return RStringI(val)
    else:
        return RStringI(str(val))

# Static factory methods (collections)
# =========================================================================


def rarray(val=None, *args):
    return RArrayI(val, *args)


def rlist(val=None, *args):
    return RListI(val, *args)


def rset(val=None, *args):
    return RSetI(val, *args)


def rmap(val=None, **kwargs):
    return RMapI(val, **kwargs)

# Implementations (primitives)
# =========================================================================


class RBoolI(omero.RBool):

    def __init__(self, value):
        omero.RBool.__init__(self, value)

    def getValue(self, current=None):
        return self._val

    def compare(self, rhs, current=None):
        raise NotImplementedError("compare")

    def __eq__(self, obj):
        if obj is None:
            return False
        elif obj is self:
            return True
        elif not isinstance(obj, omero.RBool):
            return False
        return obj._val == self._val

    def __ne__(self, obj):
        return not self.__eq__(obj)

    def __hash__(self):
        if self._val:
            return hash(True)
        else:
            return hash(False)

    def __getattr__(self, attr):
        if attr == "val":
            return self.getValue()
        else:
            raise AttributeError(attr)

    def __setattr__(self, attr, value):
        if attr == "val":
            if "_val" in self.__dict__:
                raise omero.ClientError("Cannot write to val")
            else:
                self.__dict__["_val"] = value
        else:
            object.__setattr__(self, attr, value)

    def ice_postUnmarshal(self):
        """
        Provides additional initialization once all data loaded
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused

    def ice_preMarshal(self):
        """
        Provides additional validation before data is sent
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused


class RDoubleI(omero.RDouble):

    def __init__(self, value):
        omero.RDouble.__init__(self, float(value))

    def getValue(self, current=None):
        return self._val

    def compare(self, rhs, current=None):
        raise NotImplementedError("compare")

    def __eq__(self, obj):
        if obj is None:
            return False
        elif obj is self:
            return True
        elif not isinstance(obj, omero.RDouble):
            return False
        return obj._val == self._val

    def __ne__(self, obj):
        return not self.__eq__(obj)

    def __hash__(self):
        return hash(self._val)

    def __getattr__(self, attr):
        if attr == "val":
            return self.getValue()
        else:
            raise AttributeError(attr)

    def __setattr__(self, attr, value):
        if attr == "val":
            if "_val" in self.__dict__:
                raise omero.ClientError("Cannot write to val")
            else:
                self.__dict__["_val"] = value
        else:
            object.__setattr__(self, attr, value)

    def ice_postUnmarshal(self):
        """
        Provides additional initialization once all data loaded
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused

    def ice_preMarshal(self):
        """
        Provides additional validation before data is sent
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused


class RFloatI(omero.RFloat):

    def __init__(self, value):
        omero.RFloat.__init__(self, float(value))

    def getValue(self, current=None):
        return self._val

    def compare(self, rhs, current=None):
        raise NotImplementedError("compare")

    def __eq__(self, obj):
        if obj is None:
            return False
        elif obj is self:
            return True
        elif not isinstance(obj, omero.RFloat):
            return False
        return obj._val == self._val

    def __ne__(self, obj):
        return not self.__eq__(obj)

    def __hash__(self):
        return hash(self._val)

    def __getattr__(self, attr):
        if attr == "val":
            return self.getValue()
        else:
            raise AttributeError(attr)

    def __setattr__(self, attr, value):
        if attr == "val":
            if "_val" in self.__dict__:
                raise omero.ClientError("Cannot write to val")
            else:
                self.__dict__["_val"] = value
        else:
            object.__setattr__(self, attr, value)

    def ice_postUnmarshal(self):
        """
        Provides additional initialization once all data loaded
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused

    def ice_preMarshal(self):
        """
        Provides additional validation before data is sent
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused


class RIntI(omero.RInt):

    def __init__(self, value):
        omero.RInt.__init__(self, int(value))

    def getValue(self, current=None):
        return self._val

    def compare(self, rhs, current=None):
        raise NotImplementedError("compare")

    def __eq__(self, obj):
        if obj is None:
            return False
        elif obj is self:
            return True
        elif not isinstance(obj, omero.RInt):
            return False
        return obj._val == self._val

    def __ne__(self, obj):
        return not self.__eq__(obj)

    def __hash__(self):
        return hash(self._val)

    def __getattr__(self, attr):
        if attr == "val":
            return self.getValue()
        else:
            raise AttributeError(attr)

    def __setattr__(self, attr, value):
        if attr == "val":
            if "_val" in self.__dict__:
                raise omero.ClientError("Cannot write to val")
            else:
                self.__dict__["_val"] = value
        else:
            object.__setattr__(self, attr, value)

    def ice_postUnmarshal(self):
        """
        Provides additional initialization once all data loaded
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused

    def ice_preMarshal(self):
        """
        Provides additional validation before data is sent
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused


class RLongI(omero.RLong):

    def __init__(self, value):
        omero.RLong.__init__(self, long(value))

    def getValue(self, current=None):
        return self._val

    def compare(self, rhs, current=None):
        raise NotImplementedError("compare")

    def __eq__(self, obj):
        if obj is None:
            return False
        elif obj is self:
            return True
        elif not isinstance(obj, omero.RLong):
            return False
        return obj._val == self._val

    def __ne__(self, obj):
        return not self.__eq__(obj)

    def __hash__(self):
        return hash(self._val)

    def __getattr__(self, attr):
        if attr == "val":
            return self.getValue()
        else:
            raise AttributeError(attr)

    def __setattr__(self, attr, value):
        if attr == "val":
            if "_val" in self.__dict__:
                raise omero.ClientError("Cannot write to val")
            else:
                self.__dict__["_val"] = value
        else:
            object.__setattr__(self, attr, value)

    def ice_postUnmarshal(self):
        """
        Provides additional initialization once all data loaded
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused

    def ice_preMarshal(self):
        """
        Provides additional validation before data is sent
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused


class RTimeI(omero.RTime):

    def __init__(self, value):
        omero.RTime.__init__(self, long(value))

    def getValue(self, current=None):
        return self._val

    def compare(self, rhs, current=None):
        raise NotImplementedError("compare")

    def __eq__(self, obj):
        if obj is None:
            return False
        elif obj is self:
            return True
        elif not isinstance(obj, omero.RTime):
            return False
        return obj._val == self._val

    def __ne__(self, obj):
        return not self.__eq__(obj)

    def __hash__(self):
        return hash(self._val)

    def __getattr__(self, attr):
        if attr == "val":
            return self.getValue()
        else:
            raise AttributeError(attr)

    def __setattr__(self, attr, value):
        if attr == "val":
            if "_val" in self.__dict__:
                raise omero.ClientError("Cannot write to val")
            else:
                self.__dict__["_val"] = value
        else:
            object.__setattr__(self, attr, value)

    def ice_postUnmarshal(self):
        """
        Provides additional initialization once all data loaded
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused

    def ice_preMarshal(self):
        """
        Provides additional validation before data is sent
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused

# Implementations (objects)
# =========================================================================


class RInternalI(omero.RInternal):

    def __init__(self, value):
        omero.RInternal.__init__(self, value)

    def getValue(self, current=None):
        return self._val

    def compare(self, rhs, current=None):
        raise NotImplementedError("compare")

    def __eq__(self, obj):
        if obj is None:
            return False
        elif obj is self:
            return True
        elif not isinstance(obj, omero.RInternal):
            return False
        return obj._val == self._val

    def __ne__(self, obj):
        return not self.__eq__(obj)

    def __hash__(self):
        return hash(self._val)

    def __getattr__(self, attr):
        if attr == "val":
            return self.getValue()
        else:
            raise AttributeError(attr)

    def __setattr__(self, attr, value):
        if attr == "val":
            if "_val" in self.__dict__:
                raise omero.ClientError("Cannot write to val")
            else:
                self.__dict__["_val"] = value
        else:
            object.__setattr__(self, attr, value)

    def ice_postUnmarshal(self):
        """
        Provides additional initialization once all data loaded
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused

    def ice_preMarshal(self):
        """
        Provides additional validation before data is sent
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused


class RObjectI(omero.RObject):

    def __init__(self, value):
        omero.RObject.__init__(self, value)

    def getValue(self, current=None):
        return self._val

    def compare(self, rhs, current=None):
        raise NotImplementedError("compare")

    def __eq__(self, obj):
        if obj is None:
            return False
        elif obj is self:
            return True
        elif not isinstance(obj, omero.RObject):
            return False
        return obj._val == self._val

    def __ne__(self, obj):
        return not self.__eq__(obj)

    def __hash__(self):
        return hash(self._val)

    def __getattr__(self, attr):
        if attr == "val":
            return self.getValue()
        else:
            raise AttributeError(attr)

    def __setattr__(self, attr, value):
        if attr == "val":
            if "_val" in self.__dict__:
                raise omero.ClientError("Cannot write to val")
            else:
                self.__dict__["_val"] = value
        else:
            object.__setattr__(self, attr, value)

    def ice_postUnmarshal(self):
        """
        Provides additional initialization once all data loaded
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused

    def ice_preMarshal(self):
        """
        Provides additional validation before data is sent
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused


class RStringI(omero.RString):

    def __init__(self, value):
        omero.RString.__init__(self, value)

    def getValue(self, current=None):
        return self._val

    def compare(self, rhs, current=None):
        raise NotImplementedError("compare")

    def __eq__(self, obj):
        if obj is None:
            return False
        elif obj is self:
            return True
        elif not isinstance(obj, omero.RString):
            return False
        return obj._val == self._val

    def __ne__(self, obj):
        return not self.__eq__(obj)

    def __hash__(self):
        return hash(self._val)

    def __getattr__(self, attr):
        if attr == "val":
            return self.getValue()
        else:
            raise AttributeError(attr)

    def __setattr__(self, attr, value):
        if attr == "val":
            if "_val" in self.__dict__:
                raise omero.ClientError("Cannot write to val")
            else:
                self.__dict__["_val"] = value
        else:
            object.__setattr__(self, attr, value)

    def ice_postUnmarshal(self):
        """
        Provides additional initialization once all data loaded
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused

    def ice_preMarshal(self):
        """
        Provides additional validation before data is sent
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused


class RClassI(omero.RClass):

    def __init__(self, value):
        omero.RClass.__init__(self, value)

    def getValue(self, current=None):
        return self._val

    def compare(self, rhs, current=None):
        raise NotImplementedError("compare")

    def __eq__(self, obj):
        if obj is None:
            return False
        elif obj is self:
            return True
        elif not isinstance(obj, omero.RClass):
            return False
        return obj._val == self._val

    def __ne__(self, obj):
        return not self.__eq__(obj)

    def __hash__(self):
        return hash(self._val)

    def __getattr__(self, attr):
        if attr == "val":
            return self.getValue()
        else:
            raise AttributeError(attr)

    def __setattr__(self, attr, value):
        if attr == "val":
            if "_val" in self.__dict__:
                raise omero.ClientError("Cannot write to val")
            else:
                self.__dict__["_val"] = value
        else:
            object.__setattr__(self, attr, value)

    def ice_postUnmarshal(self):
        """
        Provides additional initialization once all data loaded
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused

    def ice_preMarshal(self):
        """
        Provides additional validation before data is sent
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused

# Implementations (collections)
# =========================================================================


class RArrayI(omero.RArray):
    """
    Guaranteed to never contain an empty list.
    """

    def __init__(self, arg=None, *args):
        if arg is None:
            self._val = []
        elif not hasattr(arg, "__iter__"):
            self._val = [arg]
        else:
            self._val = list(arg)
        self._val.extend(args)
        for v in self._val:
            if not isinstance(v, omero.RType):
                raise ValueError("Item of wrong type: %s" % type(v))

    def compare(self, rhs, current=None):
        raise NotImplementedError("compare")

    def getValue(self, current=None):
        return self._val

    def get(self, index, current=None):
        return self._val[index]

    def size(self, current=None):
        return len(self._val)

    def add(self, value, current=None):
        self._val.append(value)

    def addAll(self, values, current=None):
        self.val.append(values)

    def __eq__(self, obj):
        if obj is None:
            return False
        elif self is obj:
            return True
        elif not isinstance(obj, omero.RArray):
            return False
        return self._val == obj._val

    def __ne__(self, obj):
        return not self.__eq__(obj)

    def __hash__(self, obj):
        """
        Not allowed. Hashing a list is not supported.
        """
        return hash(self._val)  # Throws an exception

    def __getattr__(self, attr):
        if attr == "val":
            return self.getValue()
        else:
            raise AttributeError(attr)

    def __setattr__(self, attr, value):
        if attr == "val":
            if "_val" in self.__dict__:
                raise omero.ClientError("Cannot write to val")
            else:
                self.__dict__["_val"] = value
        else:
            object.__setattr__(self, attr, value)

    def ice_postUnmarshal(self):
        """
        Provides additional initialization once all data loaded
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused

    def ice_preMarshal(self):
        """
        Provides additional validation before data is sent
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused


class RListI(omero.RList):
    """
    Guaranteed to never contain an empty list.
    """

    def __init__(self, arg=None, *args):
        if arg is None:
            self._val = []
        elif not hasattr(arg, "__iter__"):
            self._val = [arg]
        else:
            self._val = list(arg)
        self._val.extend(args)
        for v in self._val:
            if not isinstance(v, omero.RType):
                raise ValueError("Item of wrong type: %s" % type(v))

    def compare(self, rhs, current=None):
        raise NotImplementedError("compare")

    def getValue(self, current=None):
        return self._val

    def get(self, index, current=None):
        return self._val[index]

    def size(self, current=None):
        return len(self._val)

    def add(self, value, current=None):
        self._val.append(value)

    def addAll(self, values, current=None):
        self.val.append(values)

    def __eq__(self, obj):
        if obj is None:
            return False
        elif self is obj:
            return True
        elif not isinstance(obj, omero.RList):
            return False
        return self._val == obj._val

    def __ne__(self, obj):
        return not self.__eq__(obj)

    def __hash__(self, obj):
        """
        Not allowed. Hashing a list is not supported.
        """
        return hash(self._val)  # Throws an exception

    def __getattr__(self, attr):
        if attr == "val":
            return self.getValue()
        else:
            raise AttributeError(attr)

    def __setattr__(self, attr, value):
        if attr == "val":
            if "_val" in self.__dict__:
                raise omero.ClientError("Cannot write to val")
            else:
                self.__dict__["_val"] = value
        else:
            object.__setattr__(self, attr, value)

    def ice_postUnmarshal(self):
        """
        Provides additional initialization once all data loaded
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused

    def ice_preMarshal(self):
        """
        Provides additional validation before data is sent
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused


class RSetI(omero.RSet):
    """
    Guaranteed to never contain an empty list.
    """

    def __init__(self, arg=None, *args):
        if arg is None:
            self._val = []
        elif not hasattr(arg, "__iter__"):
            self._val = [arg]
        else:
            self._val = list(arg)
        self._val.extend(args)
        for v in self._val:
            if not isinstance(v, omero.RType):
                raise ValueError("Item of wrong type: %s" % type(v))

    def compare(self, rhs, current=None):
        raise NotImplementedError("compare")

    def getValue(self, current=None):
        return self._val

    def get(self, index, current=None):
        return self._val[index]

    def size(self, current=None):
        return len(self._val)

    def add(self, value, current=None):
        self._val.append(value)

    def addAll(self, values, current=None):
        self.val.append(values)

    def __eq__(self, obj):
        if obj is None:
            return False
        elif self is obj:
            return True
        elif not isinstance(obj, omero.RSet):
            return False
        return self._val == obj._val

    def __ne__(self, obj):
        return not self.__eq__(obj)

    def __hash__(self, obj):
        """
        Not allowed. Hashing a list is not supported.
        """
        return hash(self._val)  # Throws an exception

    def __getattr__(self, attr):
        if attr == "val":
            return self.getValue()
        else:
            raise AttributeError(attr)

    def __setattr__(self, attr, value):
        if attr == "val":
            if "_val" in self.__dict__:
                raise omero.ClientError("Cannot write to val")
            else:
                self.__dict__["_val"] = value
        else:
            object.__setattr__(self, attr, value)

    def ice_postUnmarshal(self):
        """
        Provides additional initialization once all data loaded
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused

    def ice_preMarshal(self):
        """
        Provides additional validation before data is sent
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused


class RMapI(omero.RMap):

    def __init__(self, arg=None, **kwargs):
        if arg is None:
            self._val = {}
        else:
            self._val = dict(arg)  # May throw an exception
        self._val.update(kwargs)
        self._validate()

    def _validate(self):
        for k, v in self._val.items():
            if not isinstance(k, str):
                raise ValueError("Key of wrong type: %s" % type(k))
            if v is not None and not isinstance(v, omero.RType):
                raise ValueError("Value of wrong type: %s" % type(v))

    def compare(self, rhs, current=None):
        raise NotImplementedError("compare")

    def getValue(self, current=None):
        return self._val

    def get(self, key, current=None):
        return self._val[key]

    def put(self, key, value, current=None):
        self._val[key] = value

    def size(self, current=None):
        return len(self._val)

    def __eq__(self, obj):
        if obj is None:
            return False
        elif self is obj:
            return True
        elif not isinstance(obj, omero.RMap):
            return False
        return self._val == obj._val

    def __ne__(self, obj):
        return not self.__eq__(obj)

    def __hash__(self, obj):
        """
        Not allowed. Hashing a list is not supported.
        """
        return hash(self._val)  # Throws an exception

    def __getattr__(self, attr):
        if attr == "val":
            return self.getValue()
        else:
            raise AttributeError(attr)

    def __setattr__(self, attr, value):
        if attr == "val":
            if "_val" in self.__dict__:
                raise omero.ClientError("Cannot write to val")
            else:
                self.__dict__["_val"] = value
        else:
            object.__setattr__(self, attr, value)

    def ice_postUnmarshal(self):
        """
        Provides additional initialization once all data loaded
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused

    def ice_preMarshal(self):
        """
        Provides additional validation before data is sent
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused

# Helpers
# ========================================================================

# Conversion classes are for omero.model <--> ome.model only (no python)


class ObjectFactory(Ice.ObjectFactory):

    def __init__(self, cls, f):
        self.id = cls.ice_staticId()
        self.f = f

    def create(self, string):
        return self.f()

    def destroy(self):
        pass

    def register(self, ic):
        ic.addObjectFactory(self, self.id)


# Shared state (flyweight)
# =========================================================================

rtrue = RBoolI(True)

rfalse = RBoolI(False)

rlong0 = RLongI(0)

rint0 = RIntI(0)

remptystr = RStringI("")

remptyclass = RClassI("")

rnullinternal = RInternalI(None)

rnullobject = RObjectI(None)

# Object factories
# =========================================================================

ObjectFactories = {
    RBoolI: ObjectFactory(RBoolI, lambda: RBoolI(False)),
    RDoubleI: ObjectFactory(RDoubleI, lambda: RDoubleI(0.0)),
    RFloatI: ObjectFactory(RFloatI, lambda: RFloatI(0.0)),
    RIntI: ObjectFactory(RIntI, lambda: RIntI(0)),
    RLongI: ObjectFactory(RLongI, lambda: RLongI(0)),
    RTimeI: ObjectFactory(RTimeI, lambda: RTimeI(0)),
    RClassI: ObjectFactory(RClassI, lambda: RClassI("")),
    RStringI: ObjectFactory(RStringI, lambda: RStringI("")),
    RInternalI: ObjectFactory(RInternalI, lambda: RInternalI(None)),
    RObjectI: ObjectFactory(RObjectI, lambda: RObjectI(None)),
    RArrayI: ObjectFactory(RArrayI, lambda: RArrayI()),
    RListI: ObjectFactory(RListI, lambda: RListI()),
    RSetI: ObjectFactory(RSetI, lambda: RSetI()),
    RMapI: ObjectFactory(RMapI, lambda: RMapI())
    }
