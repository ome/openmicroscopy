// **********************************************************************
//
// Copyright (c) 2003-2008 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

// Ice version 3.3.0

package ome.services.sharing.data;

public class ShareMap extends Freeze.Map
{
    private
    ShareMap(Freeze.Connection __connection, String __dbName, java.util.Comparator __comparator)
    {
        super(__connection, __dbName, __comparator);
        _indices = new Freeze.Map.Index[2];
        _indices[0] = new IdIndex("id");
        _indices[1] = new OwnerIndex("owner");
    }

    public
    ShareMap(Freeze.Connection __connection, String __dbName, boolean __createDb, java.util.Comparator __comparator, java.util.Map __indexComparators)
    {
        this(__connection, __dbName, __comparator);
        init(_indices, __dbName, "long", "::ome::services::sharing::data::ShareData", __createDb, __indexComparators);
    }

    public
    ShareMap(Freeze.Connection __connection, String __dbName, boolean __createDb, java.util.Comparator __comparator)
    {
        this(__connection, __dbName, __createDb, __comparator, null);
    }

    public
    ShareMap(Freeze.Connection __connection, String __dbName, boolean __createDb)
    {
        this(__connection, __dbName, __createDb, null);
    }

    public
    ShareMap(Freeze.Connection __connection, String __dbName)
    {
        this(__connection, __dbName, true);
    }

    public static void
    recreate(Freeze.Connection __connection, String __dbName, java.util.Comparator __comparator, java.util.Map __indexComparators)
    {
        ShareMap __tmpMap = new ShareMap(__connection, __dbName, __comparator);
        recreate(__tmpMap, __dbName, "long", "::ome::services::sharing::data::ShareData", __tmpMap._indices, __indexComparators);
    }

    public static void
    recreate(Freeze.Connection __connection, String __dbName, java.util.Comparator __comparator)
    {
        recreate(__connection, __dbName, __comparator, null);
    }

    public Freeze.Map.EntryIterator
    findById(long __index, boolean __onlyDups)
    {
        return _indices[0].untypedFind(new java.lang.Long(__index), __onlyDups);
    }

    public Freeze.Map.EntryIterator
    findById(long __index)
    {
        return _indices[0].untypedFind(new java.lang.Long(__index), true);
    }

    public int
    idCount(long __index)
    {
        return _indices[0].untypedCount(new java.lang.Long(__index));
    }

    public Freeze.Map.EntryIterator
    findByOwner(long __index, boolean __onlyDups)
    {
        return _indices[1].untypedFind(new java.lang.Long(__index), __onlyDups);
    }

    public Freeze.Map.EntryIterator
    findByOwner(long __index)
    {
        return _indices[1].untypedFind(new java.lang.Long(__index), true);
    }

    public int
    ownerCount(long __index)
    {
        return _indices[1].untypedCount(new java.lang.Long(__index));
    }

    public byte[]
    encodeKey(Object o, Ice.Communicator communicator)
    {
        assert(o instanceof java.lang.Long);
        IceInternal.BasicStream __os = new IceInternal.BasicStream(Ice.Util.getInstance(communicator));
        __os.writeLong(((java.lang.Long)o).longValue());
        IceInternal.Buffer __buf = __os.prepareWrite();
        byte[] __r = new byte[__buf.size()];
        __buf.b.get(__r);
        return __r;
    }

    public Object
    decodeKey(byte[] b, Ice.Communicator communicator)
    {
        IceInternal.BasicStream __is = new IceInternal.BasicStream(Ice.Util.getInstance(communicator));
        __is.resize(b.length, true);
        IceInternal.Buffer __buf = __is.getBuffer();
        __buf.b.position(0);
        __buf.b.put(b);
        __buf.b.position(0);
        java.lang.Long __r;
        __r = new java.lang.Long(__is.readLong());
        return __r;
    }

    public byte[]
    encodeValue(Object o, Ice.Communicator communicator)
    {
        assert(o instanceof ome.services.sharing.data.ShareData);
        IceInternal.BasicStream __os = new IceInternal.BasicStream(Ice.Util.getInstance(communicator));
        __os.startWriteEncaps();
        __os.writeObject(((ome.services.sharing.data.ShareData)o));
        __os.writePendingObjects();
        __os.endWriteEncaps();
        IceInternal.Buffer __buf = __os.prepareWrite();
        byte[] __r = new byte[__buf.size()];
        __buf.b.get(__r);
        return __r;
    }

    public Object
    decodeValue(byte[] b, Ice.Communicator communicator)
    {
        IceInternal.BasicStream __is = new IceInternal.BasicStream(Ice.Util.getInstance(communicator));
        __is.sliceObjects(false);
        __is.resize(b.length, true);
        IceInternal.Buffer __buf = __is.getBuffer();
        __buf.b.position(0);
        __buf.b.put(b);
        __buf.b.position(0);
        __is.startReadEncaps();
        Patcher __p = new Patcher();
        __is.readObject(__p);
        __is.readPendingObjects();
        __is.endReadEncaps();
        return __p.value;
    }

    private class IdIndex extends Freeze.Map.Index
    {
        public byte[]
        encodeKey(Object key, Ice.Communicator communicator)
        {
            IceInternal.BasicStream __os = new IceInternal.BasicStream(Ice.Util.getInstance(communicator));
            __os.writeLong(((java.lang.Long)key).longValue());
            IceInternal.Buffer buf = __os.prepareWrite();
            byte[] r = new byte[buf.size()];
            buf.b.get(r);
            return r;
        }

        public Object
        decodeKey(byte[] bytes, Ice.Communicator communicator)
        {
            IceInternal.BasicStream __is = new IceInternal.BasicStream(Ice.Util.getInstance(communicator));
            __is.resize(bytes.length, true);
            IceInternal.Buffer buf = __is.getBuffer();
            buf.b.position(0);
            buf.b.put(bytes);
            buf.b.position(0);
            java.lang.Long r;
            r = new java.lang.Long(__is.readLong());
            return r;
        }

        public int
        compare(Object o1, Object o2)
        {
            assert _comparator != null;
            byte[] d1 = (byte[])o1;
            byte[] d2 = (byte[])o2;
            Ice.Communicator communicator = ((Freeze.Connection)_connection).getCommunicator();
            return _comparator.compare(
                decodeKey(d1, communicator),
                decodeKey(d2, communicator));
        }

        public Object
        extractKey(Object value)
        {
            ome.services.sharing.data.ShareData typedValue = (ome.services.sharing.data.ShareData)value;
            return new java.lang.Long(typedValue.id);
        }

        private IdIndex(String name)
        {
            super(name);
        }
    }

    private class OwnerIndex extends Freeze.Map.Index
    {
        public byte[]
        encodeKey(Object key, Ice.Communicator communicator)
        {
            IceInternal.BasicStream __os = new IceInternal.BasicStream(Ice.Util.getInstance(communicator));
            __os.writeLong(((java.lang.Long)key).longValue());
            IceInternal.Buffer buf = __os.prepareWrite();
            byte[] r = new byte[buf.size()];
            buf.b.get(r);
            return r;
        }

        public Object
        decodeKey(byte[] bytes, Ice.Communicator communicator)
        {
            IceInternal.BasicStream __is = new IceInternal.BasicStream(Ice.Util.getInstance(communicator));
            __is.resize(bytes.length, true);
            IceInternal.Buffer buf = __is.getBuffer();
            buf.b.position(0);
            buf.b.put(bytes);
            buf.b.position(0);
            java.lang.Long r;
            r = new java.lang.Long(__is.readLong());
            return r;
        }

        public int
        compare(Object o1, Object o2)
        {
            assert _comparator != null;
            byte[] d1 = (byte[])o1;
            byte[] d2 = (byte[])o2;
            Ice.Communicator communicator = ((Freeze.Connection)_connection).getCommunicator();
            return _comparator.compare(
                decodeKey(d1, communicator),
                decodeKey(d2, communicator));
        }

        public Object
        extractKey(Object value)
        {
            ome.services.sharing.data.ShareData typedValue = (ome.services.sharing.data.ShareData)value;
            return new java.lang.Long(typedValue.owner);
        }

        private OwnerIndex(String name)
        {
            super(name);
        }
    }

    private static class Patcher implements IceInternal.Patcher
    {
        public void
        patch(Ice.Object v)
        {
            value = (ome.services.sharing.data.ShareData)v;
        }

        public String
        type()
        {
            return "::ome::services::sharing::data::ShareData";
        }

        ome.services.sharing.data.ShareData value;
    }

    private Freeze.Map.Index[] _indices;
}
