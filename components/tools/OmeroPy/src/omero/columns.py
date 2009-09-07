"""
/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
"""

"""
Concrete implementations of the omero.grid.Column
type hierarchy which know how to convert themselves
to PyTables types.
"""

import omero, Ice

try:
    import numpy
    tables = __import__("tables") # Pytables
    has_pytables = True
except ImportError:
    has_pytables = False


def columns2definition(cols):
    """
    Takes a list of columns and converts them into a map
    from names to tables.* column descriptors
    """
    definition = {}
    for i in range(len(cols)):
        column = cols[i]
        instance = column.descriptor(pos=i)
        definition[column.name] = instance
        # Descriptions are handled separately
    return definition

class AbstractColumn(object):
    """
    Base logic for all columns
    """

    def __init__(self):
        d = self.descriptor(0)
        self.recarrtype = d.recarrtype

    def size(self, size):
        if size is None:
            self.values = None
        else:
            self.values = [None for x in range(size)]

    def array(self):
        return numpy.array(self.values, dtype=self.recarrtype)

class FileColumnI(AbstractColumn, omero.grid.FileColumn):
    def __init__(self, *args): AbstractColumn.__init__(self); omero.grid.FileColumn.__init__(self, *args)
    def descriptor(self, pos):
        return tables.Int64Col(pos=pos)

class ImageColumnI(AbstractColumn, omero.grid.ImageColumn):
    def __init__(self, *args): AbstractColumn.__init__(self); omero.grid.ImageColumn.__init__(self, *args)
    def descriptor(self, pos):
        return tables.Int64Col(pos=pos)

class WellColumnI(AbstractColumn, omero.grid.WellColumn):
    def __init__(self, *args): AbstractColumn.__init__(self); omero.grid.WellColumn.__init__(self, *args)
    def descriptor(self, pos):
        return tables.Int64Col(pos=pos)

class RoiColumnI(AbstractColumn, omero.grid.RoiColumn):
    def __init__(self, *args): AbstractColumn.__init__(self); omero.grid.RoiColumn.__init__(self, *args)
    def descriptor(self, pos):
        return tables.Int64Col(pos=pos)

class ImageColumnI(AbstractColumn, omero.grid.ImageColumn):
    def __init__(self, *args): AbstractColumn.__init__(self); omero.grid.ImageColumn.__init__(self, *args)
    def descriptor(self, pos):
        return tables.Int64Col(pos=pos)

class BoolColumnI(AbstractColumn, omero.grid.BoolColumn):
    def __init__(self, *args): AbstractColumn.__init__(self); omero.grid.BoolColumn.__init__(self, *args)
    def descriptor(self, pos):
        return tables.BoolCol(pos=pos)

class DoubleColumnI(AbstractColumn, omero.grid.DoubleColumn):
    def __init__(self, *args): AbstractColumn.__init__(self); omero.grid.DoubleColumn.__init__(self, *args)
    def descriptor(self, pos):
        return tables.Float64Col(pos=pos)

class LongColumnI(AbstractColumn, omero.grid.LongColumn):
    def __init__(self, *args): AbstractColumn.__init__(self); omero.grid.LongColumn.__init__(self, *args)
    def descriptor(self, pos):
        return tables.Int64Col(pos=pos)

class StringColumnI(AbstractColumn, omero.grid.StringColumn):
    def __init__(self, *args): AbstractColumn.__init__(self); omero.grid.StringColumn.__init__(self, *args)
    def descriptor(self, pos):
        return tables.StringCol(pos=pos)


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


# Object factories
# =========================================================================

ObjectFactories = {
    FileColumnI: ObjectFactory(FileColumnI, lambda: FileColumnI()),
    ImageColumnI: ObjectFactory(ImageColumnI, lambda: ImageColumnI()),
    RoiColumnI: ObjectFactory(RoiColumnI, lambda: RoiColumnI()),
    WellColumnI: ObjectFactory(WellColumnI, lambda: WellColumnI()),
    BoolColumnI: ObjectFactory(BoolColumnI, lambda: BoolColumnI()),
    DoubleColumnI: ObjectFactory(DoubleColumnI, lambda: DoubleColumnI()),
    LongColumnI: ObjectFactory(LongColumnI, lambda: LongColumnI()),
    StringColumnI: ObjectFactory(StringColumnI, lambda: StringColumnI())
    }
