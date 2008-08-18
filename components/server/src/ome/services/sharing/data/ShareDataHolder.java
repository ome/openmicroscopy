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

public final class ShareDataHolder
{
    public
    ShareDataHolder()
    {
    }

    public
    ShareDataHolder(ShareData value)
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
                value = (ShareData)v;
            }
            catch(ClassCastException ex)
            {
                IceInternal.Ex.throwUOE(type(), v.ice_id());
            }
        }

        public String
        type()
        {
            return "::ome::services::sharing::data::ShareData";
        }
    }

    public Patcher
    getPatcher()
    {
        return new Patcher();
    }

    public ShareData value;
}
