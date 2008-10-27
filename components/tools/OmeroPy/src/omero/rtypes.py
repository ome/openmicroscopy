"""
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

import omero, Ice

# Static factory methods (primitives)
# =========================================================================

def rbool(val):
    if val:
        return rtrue
    else:
        return rfalse

def rdouble(val):
    return RDoubleI(val)

def rfloat(val):
    return RFloatI(val)

def rint(val):
    if val == 0:
        return rint0
    return RIntI(val)

def rlong(val):
    if val == 0:
        return rlong0
    return RLongI(val)

def rtime(val):
    return RTimeI(val)

def rtime(date):
    if date == None:
        return None
    else:
        return RTimeI(date)

# Static factory methods (objects)
# =========================================================================

def rinternal(val):
    if val == None:
        return rnullinternal
    return RInternalI(val)

def robject(val):
    if val == None:
        return rnullobject
    return RObjectI(val)

def rclass(val):
    if val == None or len(val) == 0:
        return remptyclass
    return RClassI(val)

def rstring(val):
    if val == None or len(val) == 0:
        return remptystr
    return RStringI(val)

# Static factory methods (collections)
# =========================================================================

def rarray(val = None, *args):
    return RArrayI(val, *args)

def rlist(val = None, *args):
    return RListI(val, *args)

def rset(val = None, *args):
    return RSetI(val, *args)

def rmap(val = None, **kwargs):
    return RMapI(val, **kwargs)

# Implementations (primitives)
# =========================================================================

class RBoolI(omero.RBool):

    def __init__(self, value):
        omero.RBool.__init__(self, value)

    def getValue(self, current = None):
        return self._val

    def compare(self, rhs, current = None):
        raise NotImplementedError("compare")

    def __eq__(self, obj):
        if obj is self:
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

class RDoubleI(omero.RDouble):

    def __init__(self, value):
        omero.RDouble.__init__(self, value)

    def getValue(self, current = None):
        return self._val

    def compare(self, rhs, current = None):
        raise NotImplementedError("compare")

    def __eq__(self, obj):
        if obj is self:
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

class RFloatI(omero.RFloat):

    def __init__(self, value):
        omero.RFloat.__init__(self, value)

    def getValue(self, current = None):
        return self._val

    def compare(self, rhs, current = None):
        raise NotImplementedError("compare")

    def __eq__(self, obj):
        if obj is self:
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

class RIntI(omero.RInt):

    def __init__(self, value):
        omero.RInt.__init__(self, value)

    def getValue(self, current = None):
        return self._val

    def compare(self, rhs, current = None):
        raise NotImplementedError("compare")

    def __eq__(self, obj):
        if obj is self:
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

class RLongI(omero.RLong):

    def __init__(self, value):
        omero.RLong.__init__(self, value)

    def getValue(self, current = None):
        return self._val

    def compare(self, rhs, current = None):
        raise NotImplementedError("compare")

    def __eq__(self, obj):
        if obj is self:
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

class RTimeI(omero.RTime):

    def __init__(self, value):
        omero.RTime.__init__(self, value)

    def getValue(self, current = None):
        return self._val

    def compare(self, rhs, current = None):
        raise NotImplementedError("compare")

    def __eq__(self, obj):
        if obj is self:
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


# Implementations (objects)
# =========================================================================

class RInternalI(omero.RInternal):

    def __init__(self, value):
        omero.RInternal.__init__(self, value)

    def getValue(self, current = None):
        return self._val

    def compare(self, rhs, current = None):
        raise NotImplementedError("compare")

    def __eq__(self, obj):
        if obj is self:
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

class RObjectI(omero.RObject):

    def __init__(self, value):
        omero.RObject.__init__(self, value)

    def getValue(self, current = None):
        return self._val

    def compare(self, rhs, current = None):
        raise NotImplementedError("compare")

    def __eq__(self, obj):
        if obj is self:
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

class RStringI(omero.RString):

    def __init__(self, value):
        omero.RString.__init__(self, value)

    def getValue(self, current = None):
        return self._val

    def compare(self, rhs, current = None):
        raise NotImplementedError("compare")

    def __eq__(self, obj):
        if obj is self:
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

class RClassI(omero.RClass):

    def __init__(self, value):
        omero.RClass.__init__(self, value)

    def getValue(self, current = None):
        return self._val

    def compare(self, rhs, current = None):
        raise NotImplementedError("compare")

    def __eq__(self, obj):
        if obj is self:
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


# Implementations (collections)
# =========================================================================

class RArrayI(omero.RArray):
    """
    Guaranteed to never contain an empty list.
    """

    def __init__(self, arg = None, *args):
        if arg == None:
            self._val = []
        elif not hasattr(arg, "__iter__"):
            self._val = [arg]
        else:
            self._val = list(arg)
        self._val.extend(args)

    def compare(self, rhs, current = None):
        raise NotImplementedError("compare")

    def getValue(self, current = None):
        return self._val

    def get(self, index, current = None):
        return self._val[index]

    def size(self, current = None):
        return len(self._val)

    def add(self, value, current = None):
        self._val.append(value)

    def addAll(self, values, current = None):
        self.val.append(values)

    def __eq__(self, obj):
        if self is obj:
            return True
        elif isinstance(obj, omero.RArray):
            return False
        return self._val == obj._val

    def __ne__(self, obj):
        return not self.__eq__(obj)

    def __hash__(self, obj):
        """
        Not allowed. Hashing a list is not supported.
        """
        return hash(self._val) # Throws an exception

    def __getattr__(self, attr):
        if attr == "val":
            return self.getValue()
        else:
            raise AttributeError(attr)

class RListI(omero.RList):
    """
    Guaranteed to never contain an empty list.
    """

    def __init__(self, arg = None, *args):
        if arg == None:
            self._val = []
        elif not hasattr(arg, "__iter__"):
            self._val = [arg]
        else:
            self._val = list(arg)
        self._val.extend(args)

    def compare(self, rhs, current = None):
        raise NotImplementedError("compare")

    def getValue(self, current = None):
        return self._val

    def get(self, index, current = None):
        return self._val[index]

    def size(self, current = None):
        return len(self._val)

    def add(self, value, current = None):
        self._val.append(value)

    def addAll(self, values, current = None):
        self.val.append(values)

    def __eq__(self, obj):
        if self is obj:
            return True
        elif isinstance(obj, omero.RList):
            return False
        return self._val == obj._val

    def __ne__(self, obj):
        return not self.__eq__(obj)

    def __hash__(self, obj):
        """
        Not allowed. Hashing a list is not supported.
        """
        return hash(self._val) # Throws an exception

    def __getattr__(self, attr):
        if attr == "val":
            return self.getValue()
        else:
            raise AttributeError(attr)

class RSetI(omero.RSet):
    """
    Guaranteed to never contain an empty list.
    """

    def __init__(self, arg = None, *args):
        if arg == None:
            self._val = []
        elif not hasattr(arg, "__iter__"):
            self._val = [arg]
        else:
            self._val = list(arg)
        self._val.extend(args)

    def compare(self, rhs, current = None):
        raise NotImplementedError("compare")

    def getValue(self, current = None):
        return self._val

    def get(self, index, current = None):
        return self._val[index]

    def size(self, current = None):
        return len(self._val)

    def add(self, value, current = None):
        self._val.append(value)

    def addAll(self, values, current = None):
        self.val.append(values)

    def __eq__(self, obj):
        if self is obj:
            return True
        elif isinstance(obj, omero.RSet):
            return False
        return self._val == obj._val

    def __ne__(self, obj):
        return not self.__eq__(obj)

    def __hash__(self, obj):
        """
        Not allowed. Hashing a list is not supported.
        """
        return hash(self._val) # Throws an exception

    def __getattr__(self, attr):
        if attr == "val":
            return self.getValue()
        else:
            raise AttributeError(attr)

class RMapI(omero.RMap):

    def __init__(self, arg = None, **kwargs):
        if arg == None:
            self._val = {}
        else:
            self._val = dict(arg) # May throw an exception
        self._val.update(kwargs)

    def compare(self, rhs, current = None):
        raise NotImplementedError("compare")

    def getValue(self, current = None):
        return self._val

    def get(self, key, current = None):
        return self._val[key]

    def put(self, key, value, current = None):
        self._val[key] = value

    def size(self, current = None):
        return len(self._val)

    def __eq__(self, obj):
        if self is obj:
            return True
        elif isinstance(obj, omero.RMap):
            return False
        return self._val == obj._val

    def __ne__(self, obj):
        return not self.__eq__(obj)

    def __hash__(self, obj):
        """
        Not allowed. Hashing a list is not supported.
        """
        return hash(self._val) # Throws an exception

    def __getattr__(self, attr):
        if attr == "val":
            return self.getValue()
        else:
            raise AttributeError(attr)

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

remptystr = RStringI(None)

remptyclass = RClassI(None)

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

