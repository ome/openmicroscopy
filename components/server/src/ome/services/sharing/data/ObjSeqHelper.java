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

public final class ObjSeqHelper
{
    public static void
    write(IceInternal.BasicStream __os, java.util.List<ome.services.sharing.data.Obj> __v)
    {
        if(__v == null)
        {
            __os.writeSize(0);
        }
        else
        {
            __os.writeSize(__v.size());
            for(Obj __elem : __v)
            {
                __os.writeObject(__elem);
            }
        }
    }

    public static java.util.List<ome.services.sharing.data.Obj>
    read(IceInternal.BasicStream __is)
    {
        java.util.List<ome.services.sharing.data.Obj> __v;
        __v = new java.util.ArrayList<ome.services.sharing.data.Obj>();
        final int __len0 = __is.readSize();
        __is.startSeq(__len0, 4);
        final String __type0 = Obj.ice_staticId();
        for(int __i0 = 0; __i0 < __len0; __i0++)
        {
            __v.add(null);
            __is.readObject(new IceInternal.ListPatcher(__v, Obj.class, __type0, __i0));
            __is.checkSeq();
            __is.endElement();
        }
        __is.endSeq(__len0);
        return __v;
    }
}
