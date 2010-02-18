"""
::
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
import omero_Tables_ice

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
        if isinstance(d, tables.IsDescription):
            cols = d.columns
            try:
                del cols["_v_pos"]
            except KeyError:
                pass
            self.recarrtypes = [None for x in range(len(cols))]
            for k, v in cols.items():
                self.recarrtypes[v._v_pos] = ("%s/%s" % (self.name, k), v.recarrtype)
        else:
            self.recarrtypes = [(self.name, d.recarrtype)]

    def settable(self, tbl):
        """
        Called by tables.py when first initializing columns.
        Can be used to complete further initialization.
        """
        self.__table = tbl

    def append(self, tbl):
        """
        Called by tables.py to give columns. By default, does nothing.
        """
        pass

    def readCoordinates(self, tbl, rowNumbers):
        if rowNumbers is None or len(rowNumbers) == 0:
            rows = tbl.read()
        else:
            rows = tbl.readCoordinates(rowNumbers)
        self.fromrows(rows)

    def read(self, tbl, start, stop):
        rows = tbl.read(start, stop)
        self.fromrows(rows)

    def getsize(self):
        """
        Any method which does not use the "values" field
        will need to override this method.
        """
        if self.values is None:
            return None
        else:
            return len(self.values)

    def setsize(self, size):
        """
        Any method which does not use the "values" field
        will need to override this method.
        """
        if size is None:
            self.values = None
        else:
            self.values = [None for x in range(size)]

    def names(self):
        """
        Any method which does not use the "values" field
        will need to override this method.
        """
        return [self.name]

    def arrays(self):
        """
        Any method which does not use the "values" field
        will need to override this method.
        """
        return [numpy.array(self.values, dtype=self.recarrtypes[0][1])]

    def fromrows(self, rows):
        """
        Any method which does not use the "values" field
        will need to override this method.
        """
        self.values = rows[self.name]
        # WORKAROUND: http://www.zeroc.com/forums/bug-reports/4165-icepy-can-not-handle-buffers-longs-i64.html#post20468
        d = self.recarrtypes[0][1]
        if isinstance(d, str):
            if d.endswith("i8"):
                self.values = self.values.tolist()
        elif d.kind == "i" and d.itemsize == "8":
            self.values = self.values.tolist()

class FileColumnI(AbstractColumn, omero.grid.FileColumn):

    def __init__(self, name = "Unknown", *args):
        omero.grid.FileColumn.__init__(self, name, *args)
        AbstractColumn.__init__(self)

    def descriptor(self, pos):
        return tables.Int64Col(pos=pos)

class ImageColumnI(AbstractColumn, omero.grid.ImageColumn):

    def __init__(self, name = "Unknown", *args):
        omero.grid.ImageColumn.__init__(self, name, *args)
        AbstractColumn.__init__(self)

    def descriptor(self, pos):
        return tables.Int64Col(pos=pos)

class WellColumnI(AbstractColumn, omero.grid.WellColumn):

    def __init__(self, name = "Unknown", *args):
        omero.grid.WellColumn.__init__(self, name, *args)
        AbstractColumn.__init__(self)

    def descriptor(self, pos):
        return tables.Int64Col(pos=pos)

class RoiColumnI(AbstractColumn, omero.grid.RoiColumn):

    def __init__(self, name = "Unknown", *args):
        omero.grid.RoiColumn.__init__(self, name, *args)
        AbstractColumn.__init__(self)

    def descriptor(self, pos):
        return tables.Int64Col(pos=pos)

class ImageColumnI(AbstractColumn, omero.grid.ImageColumn):

    def __init__(self, name = "Unknown", *args):
        omero.grid.ImageColumn.__init__(self, name, *args)
        AbstractColumn.__init__(self)

    def descriptor(self, pos):
        return tables.Int64Col(pos=pos)

class BoolColumnI(AbstractColumn, omero.grid.BoolColumn):

    def __init__(self, name = "Unknown", *args):
        omero.grid.BoolColumn.__init__(self, name, *args)
        AbstractColumn.__init__(self)

    def descriptor(self, pos):
        return tables.BoolCol(pos=pos)

class DoubleColumnI(AbstractColumn, omero.grid.DoubleColumn):

    def __init__(self, name = "Unknown", *args):
        omero.grid.DoubleColumn.__init__(self, name, *args)
        AbstractColumn.__init__(self)

    def descriptor(self, pos):
        return tables.Float64Col(pos=pos)

class LongColumnI(AbstractColumn, omero.grid.LongColumn):

    def __init__(self, name = "Unknown", *args):
        omero.grid.LongColumn.__init__(self, name, *args)
        AbstractColumn.__init__(self)

    def descriptor(self, pos):
        return tables.Int64Col(pos=pos)

class StringColumnI(AbstractColumn, omero.grid.StringColumn):

    def __init__(self, name = "Unknown", *args):
        omero.grid.StringColumn.__init__(self, name, *args)
        AbstractColumn.__init__(self)

    def settable(self, tbl):
        AbstractColumn.settable(self, tbl)
        self.size = getattr(tbl.cols, self.name).dtype.itemsize

    def arrays(self):
        """
        Overriding to correct for size.
        """
        sz = self.size
        return [numpy.array(self.values, dtype="S%s"%sz)]

    def descriptor(self, pos):
        # During initialization, size might be zero
        # to prevent exceptions we set it to 1
        if not self.size or self.size < 0:
            self.size = 1
        return tables.StringCol(pos=pos, itemsize=self.size)

class MaskColumnI(AbstractColumn, omero.grid.MaskColumn):

    def __init__(self, name = "Unknown", *args):
        omero.grid.MaskColumn.__init__(self, name, *args)
        AbstractColumn.__init__(self)

    def __noneorsame(self, a, b):
        if a is None:
            if b is None:
                return
        # a not none
        if b is not None:
            if len(a) == len(b):
                return
        raise omero.ValidationException(None, None, "Columns don't match")

    def __sanitycheck(self):
        self.__noneorsame(self.imageId, self.theZ)
        self.__noneorsame(self.imageId, self.theT)
        self.__noneorsame(self.imageId, self.x)
        self.__noneorsame(self.imageId, self.y)
        self.__noneorsame(self.imageId, self.w)
        self.__noneorsame(self.imageId, self.h)
        self.__noneorsame(self.imageId, self.bytes)

    def descriptor(self, pos):
        class MaskDescription(tables.IsDescription):
            _v_pos = pos
            i = tables.Int64Col(pos=0)
            z = tables.Int32Col(pos=1)
            t = tables.Int32Col(pos=2)
            x = tables.Float64Col(pos=3)
            y = tables.Float64Col(pos=4)
            w = tables.Float64Col(pos=5)
            h = tables.Float64Col(pos=6)
        return MaskDescription()

    def names(self):
        return [x[0] for x in self.recarrtypes]

    def arrays(self):
        self.__sanitycheck()
        a = [
            numpy.array(self.imageId, dtype=self.recarrtypes[0][1]),
            numpy.array(self.theZ, dtype=self.recarrtypes[1][1]),
            numpy.array(self.theT, dtype=self.recarrtypes[2][1]),
            numpy.array(self.x, dtype=self.recarrtypes[3][1]),
            numpy.array(self.y, dtype=self.recarrtypes[4][1]),
            numpy.array(self.w, dtype=self.recarrtypes[5][1]),
            numpy.array(self.h, dtype=self.recarrtypes[6][1]),
            ]
        return a

    def getsize(self):
        self.__sanitycheck()
        if self.imageId is None:
            return None
        else:
            return len(self.imageId)

    def setsize(self, size):
        if size is None:
            self.imageId = None
            self.theZ = None
            self.theT = None
            self.x = None
            self.y = None
            self.w = None
            self.h = None
        else:
            self.imageId = numpy.zeroes(size, dtype = self.recarrtypes[0][1])
            self.theZ    = numpy.zeroes(size, dtype = self.recarrtypes[1][1])
            self.theT    = numpy.zeroes(size, dtype = self.recarrtypes[2][1])
            self.x       = numpy.zeroes(size, dtype = self.recarrtypes[3][1])
            self.y       = numpy.zeroes(size, dtype = self.recarrtypes[4][1])
            self.w       = numpy.zeroes(size, dtype = self.recarrtypes[5][1])
            self.h       = numpy.zeroes(size, dtype = self.recarrtypes[6][1])

    def readCoordinates(self, tbl, rowNumbers):
        self.__sanitycheck()
        AbstractColumn.readCoordinates(self, tbl, rowNumbers) # calls fromrows
        masks = self._getmasks(tbl)
        if rowNumbers is None or len(rowNumbers) == 0:
            rowNumbers = range(masks.nrows)
        self.getbytes(masks, rowNumbers)

    def read(self, tbl, start, stop):
        self.__sanitycheck()
        AbstractColumn.read(self, tbl, start, stop) # calls fromrows
        masks = self._getmasks(tbl)
        rowNumbers = range(start, stop)
        self.getbytes(masks, rowNumbers)

    def getbytes(self, masks, rowNumbers):
        self.bytes = []
        for idx in rowNumbers:
            self.bytes.append(masks[idx].tolist())

    def fromrows(self, all_rows):
        rows = all_rows[self.name]
        # WORKAROUND: http://www.zeroc.com/forums/bug-reports/4165-icepy-can-not-handle-buffers-longs-i64.html#post20468
        self.imageId = rows["i"].tolist()
        self.theZ = rows["z"].tolist() # ticket:1665
        self.theT = rows["t"].tolist() # ticket:1665
        self.x = rows["x"]
        self.y = rows["y"]
        self.w = rows["w"]
        self.h = rows["h"]

    def append(self, tbl):
        self.__sanitycheck()
        masks = self._getmasks(tbl)
        for x in self.bytes:
            masks.append(numpy.fromstring(x, count=len(x), dtype=tables.UInt8Atom()))

    def _getmasks(self, tbl):
        n = tbl._v_name
        f = tbl._v_file
        p = tbl._v_parent
        # http://www.zeroc.com/doc/Ice-3.3.1/manual/Slice.5.8.html#200
        # Ice::Byte can be -128 to 127 OR 0 to 255, but using UInt8 for the moment
        try:
            masks = getattr(p, "%s_masks" % n)
        except tables.NoSuchNodeError:
            masks = f.createVLArray(p, "%s_masks" % n, tables.UInt8Atom())
        return masks

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
    StringColumnI: ObjectFactory(StringColumnI, lambda: StringColumnI()),
    MaskColumnI: ObjectFactory(MaskColumnI, lambda: MaskColumnI())
    }
