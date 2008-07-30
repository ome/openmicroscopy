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

public final class ShareItemPrxHelper extends Ice.ObjectPrxHelperBase implements ShareItemPrx
{
    public static ShareItemPrx
    checkedCast(Ice.ObjectPrx __obj)
    {
        ShareItemPrx __d = null;
        if(__obj != null)
        {
            try
            {
                __d = (ShareItemPrx)__obj;
            }
            catch(ClassCastException ex)
            {
                if(__obj.ice_isA("::ome::services::sharing::data::ShareItem"))
                {
                    ShareItemPrxHelper __h = new ShareItemPrxHelper();
                    __h.__copyFrom(__obj);
                    __d = __h;
                }
            }
        }
        return __d;
    }

    public static ShareItemPrx
    checkedCast(Ice.ObjectPrx __obj, java.util.Map<String, String> __ctx)
    {
        ShareItemPrx __d = null;
        if(__obj != null)
        {
            try
            {
                __d = (ShareItemPrx)__obj;
            }
            catch(ClassCastException ex)
            {
                if(__obj.ice_isA("::ome::services::sharing::data::ShareItem", __ctx))
                {
                    ShareItemPrxHelper __h = new ShareItemPrxHelper();
                    __h.__copyFrom(__obj);
                    __d = __h;
                }
            }
        }
        return __d;
    }

    public static ShareItemPrx
    checkedCast(Ice.ObjectPrx __obj, String __facet)
    {
        ShareItemPrx __d = null;
        if(__obj != null)
        {
            Ice.ObjectPrx __bb = __obj.ice_facet(__facet);
            try
            {
                if(__bb.ice_isA("::ome::services::sharing::data::ShareItem"))
                {
                    ShareItemPrxHelper __h = new ShareItemPrxHelper();
                    __h.__copyFrom(__bb);
                    __d = __h;
                }
            }
            catch(Ice.FacetNotExistException ex)
            {
            }
        }
        return __d;
    }

    public static ShareItemPrx
    checkedCast(Ice.ObjectPrx __obj, String __facet, java.util.Map<String, String> __ctx)
    {
        ShareItemPrx __d = null;
        if(__obj != null)
        {
            Ice.ObjectPrx __bb = __obj.ice_facet(__facet);
            try
            {
                if(__bb.ice_isA("::ome::services::sharing::data::ShareItem", __ctx))
                {
                    ShareItemPrxHelper __h = new ShareItemPrxHelper();
                    __h.__copyFrom(__bb);
                    __d = __h;
                }
            }
            catch(Ice.FacetNotExistException ex)
            {
            }
        }
        return __d;
    }

    public static ShareItemPrx
    uncheckedCast(Ice.ObjectPrx __obj)
    {
        ShareItemPrx __d = null;
        if(__obj != null)
        {
            ShareItemPrxHelper __h = new ShareItemPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
        }
        return __d;
    }

    public static ShareItemPrx
    uncheckedCast(Ice.ObjectPrx __obj, String __facet)
    {
        ShareItemPrx __d = null;
        if(__obj != null)
        {
            Ice.ObjectPrx __bb = __obj.ice_facet(__facet);
            ShareItemPrxHelper __h = new ShareItemPrxHelper();
            __h.__copyFrom(__bb);
            __d = __h;
        }
        return __d;
    }

    protected Ice._ObjectDelM
    __createDelegateM()
    {
        return new _ShareItemDelM();
    }

    protected Ice._ObjectDelD
    __createDelegateD()
    {
        return new _ShareItemDelD();
    }

    public static void
    __write(IceInternal.BasicStream __os, ShareItemPrx v)
    {
        __os.writeProxy(v);
    }

    public static ShareItemPrx
    __read(IceInternal.BasicStream __is)
    {
        Ice.ObjectPrx proxy = __is.readProxy();
        if(proxy != null)
        {
            ShareItemPrxHelper result = new ShareItemPrxHelper();
            result.__copyFrom(proxy);
            return result;
        }
        return null;
    }
}
