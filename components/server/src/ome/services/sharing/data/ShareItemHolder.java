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

public final class ShareItemHolder
{
    public
    ShareItemHolder()
    {
    }

    public
    ShareItemHolder(ShareItem value)
    {
        this.value = value;
    }

    public class Patcher implements IceInternal.Patcher
    {
        public void
        patch(Ice.Object v)
        {
            try
            {
                value = (ShareItem)v;
            }
            catch(ClassCastException ex)
            {
                IceInternal.Ex.throwUOE(type(), v.ice_id());
            }
        }

        public String
        type()
        {
            return "::ome::services::sharing::data::ShareItem";
        }
    }

    public Patcher
    getPatcher()
    {
        return new Patcher();
    }

    public ShareItem value;
}
