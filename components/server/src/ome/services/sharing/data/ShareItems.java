// **********************************************************************
//
// Copyright (c) 2003-2007 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

// Ice version 3.2.1

package ome.services.sharing.data;

public class ShareItems extends Freeze.Map
{
    public
    ShareItems(Freeze.Connection __connection, String __dbName, boolean __createDb, java.util.Comparator __comparator, java.util.Map __indexComparators)
    {
        super(__connection, __dbName, __comparator);
        _indices = new Freeze.Map.Index[2];
        _indices[0] = new TypeIndex("type");
        _indices[1] = new ShareIndex("share");
        init(_indices, __dbName, "long", "::ome::services::sharing::data::ShareItem", __createDb, __indexComparators);
    }

    public
    ShareItems(Freeze.Connection __connection, String __dbName, boolean __createDb, java.util.Comparator __comparator)
    {
        this(__connection, __dbName, __createDb, __comparator, null);
    }

    public
    ShareItems(Freeze.Connection __connection, String __dbName, boolean __createDb)
    {
        this(__connection, __dbName, __createDb, null);
    }

    public
    ShareItems(Freeze.Connection __connection, String __dbName)
    {
        this(__connection, __dbName, true);
    }

    public Freeze.Map.EntryIterator
    findByType(String __index, boolean __onlyDups)
    {
        return _indices[0].untypedFind(__index, __onlyDups);
    }

    public Freeze.Map.EntryIterator
    findByType(String __index)
    {
        return _indices[0].untypedFind(__index, true);
    }

    public int
    typeCount(String __index)
    {
        return _indices[0].untypedCount(__index);
    }

    public Freeze.Map.EntryIterator
    findByShare(long __index, boolean __onlyDups)
    {
        return _indices[1].untypedFind(new java.lang.Long(__index), __onlyDups);
    }

    public Freeze.Map.EntryIterator
    findByShare(long __index)
    {
        return _indices[1].untypedFind(new java.lang.Long(__index), true);
    }

    public int
    shareCount(long __index)
    {
        return _indices[1].untypedCount(new java.lang.Long(__index));
    }

    public byte[]
    encodeKey(Object o, Ice.Communicator communicator)
    {
        assert(o instanceof java.lang.Long);
        IceInternal.BasicStream __os = new IceInternal.BasicStream(Ice.Util.getInstance(communicator));
        __os.writeLong(((java.lang.Long)o).longValue());
        java.nio.ByteBuffer __buf = __os.prepareWrite();
        byte[] __r = new byte[__buf.limit()];
        __buf.get(__r);
        return __r;
    }

    public Object
    decodeKey(byte[] b, Ice.Communicator communicator)
    {
        IceInternal.BasicStream __is = new IceInternal.BasicStream(Ice.Util.getInstance(communicator));
        __is.resize(b.length, true);
        java.nio.ByteBuffer __buf = __is.prepareRead();
        __buf.position(0);
        __buf.put(b);
        __buf.position(0);
        java.lang.Long __r;
        __r = new java.lang.Long(__is.readLong());
        return __r;
    }

    public byte[]
    encodeValue(Object o, Ice.Communicator communicator)
    {
        assert(o instanceof ome.services.sharing.data.ShareItem);
        IceInternal.BasicStream __os = new IceInternal.BasicStream(Ice.Util.getInstance(communicator));
        __os.startWriteEncaps();
        __os.writeObject(((ome.services.sharing.data.ShareItem)o));
        __os.writePendingObjects();
        __os.endWriteEncaps();
        java.nio.ByteBuffer __buf = __os.prepareWrite();
        byte[] __r = new byte[__buf.limit()];
        __buf.get(__r);
        return __r;
    }

    public Object
    decodeValue(byte[] b, Ice.Communicator communicator)
    {
        IceInternal.BasicStream __is = new IceInternal.BasicStream(Ice.Util.getInstance(communicator));
        __is.sliceObjects(false);
        __is.resize(b.length, true);
        java.nio.ByteBuffer __buf = __is.prepareRead();
        __buf.position(0);
        __buf.put(b);
        __buf.position(0);
        __is.startReadEncaps();
        Patcher __p = new Patcher();
        __is.readObject(__p);
        __is.readPendingObjects();
        __is.endReadEncaps();
        return __p.value;
    }

    private class TypeIndex extends Freeze.Map.Index
    {
        public byte[]
        encodeKey(Object key, Ice.Communicator communicator)
        {
            IceInternal.BasicStream __os = new IceInternal.BasicStream(Ice.Util.getInstance(communicator));
            __os.writeString(((String)key));
            java.nio.ByteBuffer buf = __os.prepareWrite();
            byte[] r = new byte[buf.limit()];
            buf.get(r);
            return r;
        }

        public Object
        decodeKey(byte[] bytes, Ice.Communicator communicator)
        {
            IceInternal.BasicStream __is = new IceInternal.BasicStream(Ice.Util.getInstance(communicator));
            __is.resize(bytes.length, true);
            java.nio.ByteBuffer buf = __is.prepareRead();
            buf.position(0);
            buf.put(bytes);
            buf.position(0);
            java.lang.String r;
            r = __is.readString();
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
            ome.services.sharing.data.ShareItem typedValue = (ome.services.sharing.data.ShareItem)value;
            return typedValue.type;
        }

        private TypeIndex(String name)
        {
            super(name);
        }
    }

    private class ShareIndex extends Freeze.Map.Index
    {
        public byte[]
        encodeKey(Object key, Ice.Communicator communicator)
        {
            IceInternal.BasicStream __os = new IceInternal.BasicStream(Ice.Util.getInstance(communicator));
            __os.writeLong(((java.lang.Long)key).longValue());
            java.nio.ByteBuffer buf = __os.prepareWrite();
            byte[] r = new byte[buf.limit()];
            buf.get(r);
            return r;
        }

        public Object
        decodeKey(byte[] bytes, Ice.Communicator communicator)
        {
            IceInternal.BasicStream __is = new IceInternal.BasicStream(Ice.Util.getInstance(communicator));
            __is.resize(bytes.length, true);
            java.nio.ByteBuffer buf = __is.prepareRead();
            buf.position(0);
            buf.put(bytes);
            buf.position(0);
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
            ome.services.sharing.data.ShareItem typedValue = (ome.services.sharing.data.ShareItem)value;
            return new java.lang.Long(typedValue.share);
        }

        private ShareIndex(String name)
        {
            super(name);
        }
    }

    private static class Patcher implements IceInternal.Patcher
    {
        public void
        patch(Ice.Object v)
        {
            value = (ome.services.sharing.data.ShareItem)v;
        }

        public String
        type()
        {
            return "::ome::services::sharing::data::ShareItem";
        }

        ome.services.sharing.data.ShareItem value;
    }

    private Freeze.Map.Index[] _indices;
}
