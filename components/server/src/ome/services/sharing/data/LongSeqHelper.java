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

public final class LongSeqHelper
{
    public static void
    write(IceInternal.BasicStream __os, java.util.List<Long> __v)
    {
        if(__v == null)
        {
            __os.writeSize(0);
        }
        else
        {
            __os.writeSize(__v.size());
            for(long __elem : __v)
            {
                __os.writeLong(__elem);
            }
        }
    }

    public static java.util.List<Long>
    read(IceInternal.BasicStream __is)
    {
        java.util.List<Long> __v;
        __v = new java.util.ArrayList<Long>();
        final int __len0 = __is.readSize();
        __is.checkFixedSeq(__len0, 8);
        for(int __i0 = 0; __i0 < __len0; __i0++)
        {
            long __elem;
            __elem = __is.readLong();
            __v.add(__elem);
        }
        return __v;
    }
}
