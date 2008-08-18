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

public final class IdMapHolder
{
    public
    IdMapHolder()
    {
    }

    public
    IdMapHolder(java.util.Map<java.lang.String, java.util.List<Long>> value)
    {
        this.value = value;
    }

    public java.util.Map<java.lang.String, java.util.List<Long>> value;
}
