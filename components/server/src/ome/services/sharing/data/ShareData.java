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

public class ShareData extends Ice.ObjectImpl
{
    public ShareData()
    {
    }

    public ShareData(long id, String owner, java.util.List<Long> members, java.util.List<String> guests, java.util.Map<java.lang.String, java.util.List<Long>> objects, boolean enabled)
    {
        this.id = id;
        this.owner = owner;
        this.members = members;
        this.guests = guests;
        this.objects = objects;
        this.enabled = enabled;
    }

    private static class __F extends Ice.LocalObjectImpl implements Ice.ObjectFactory
    {
        public Ice.Object
        create(String type)
        {
            assert(type.equals(ice_staticId()));
            return new ShareData();
        }

        public void
        destroy()
        {
        }
    }
    private static Ice.ObjectFactory _factory = new __F();

    public static Ice.ObjectFactory
    ice_factory()
    {
        return _factory;
    }

    public static final String[] __ids =
    {
        "::Ice::Object",
        "::ome::services::sharing::data::ShareData"
    };

    public boolean
    ice_isA(String s)
    {
        return java.util.Arrays.binarySearch(__ids, s) >= 0;
    }

    public boolean
    ice_isA(String s, Ice.Current __current)
    {
        return java.util.Arrays.binarySearch(__ids, s) >= 0;
    }

    public String[]
    ice_ids()
    {
        return __ids;
    }

    public String[]
    ice_ids(Ice.Current __current)
    {
        return __ids;
    }

    public String
    ice_id()
    {
        return __ids[1];
    }

    public String
    ice_id(Ice.Current __current)
    {
        return __ids[1];
    }

    public static String
    ice_staticId()
    {
        return __ids[1];
    }

    public void
    __write(IceInternal.BasicStream __os)
    {
        __os.writeTypeId(ice_staticId());
        __os.startWriteSlice();
        __os.writeLong(id);
        __os.writeString(owner);
        LongSeqHelper.write(__os, members);
        StringSeqHelper.write(__os, guests);
        IdMapHelper.write(__os, objects);
        __os.writeBool(enabled);
        __os.endWriteSlice();
        super.__write(__os);
    }

    public void
    __read(IceInternal.BasicStream __is, boolean __rid)
    {
        if(__rid)
        {
            __is.readTypeId();
        }
        __is.startReadSlice();
        id = __is.readLong();
        owner = __is.readString();
        members = LongSeqHelper.read(__is);
        guests = StringSeqHelper.read(__is);
        objects = IdMapHelper.read(__is);
        enabled = __is.readBool();
        __is.endReadSlice();
        super.__read(__is, true);
    }

    public void
    __write(Ice.OutputStream __outS)
    {
        Ice.MarshalException ex = new Ice.MarshalException();
        ex.reason = "type ome::services::sharing::data::ShareData was not generated with stream support";
        throw ex;
    }

    public void
    __read(Ice.InputStream __inS, boolean __rid)
    {
        Ice.MarshalException ex = new Ice.MarshalException();
        ex.reason = "type ome::services::sharing::data::ShareData was not generated with stream support";
        throw ex;
    }

    public long id;

    public String owner;

    public java.util.List<Long> members;

    public java.util.List<String> guests;

    public java.util.Map<java.lang.String, java.util.List<Long>> objects;

    public boolean enabled;
}
