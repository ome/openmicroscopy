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

public final class ShareDataPrxHelper extends Ice.ObjectPrxHelperBase implements ShareDataPrx
{
    public static ShareDataPrx
    checkedCast(Ice.ObjectPrx __obj)
    {
        ShareDataPrx __d = null;
        if(__obj != null)
        {
            try
            {
                __d = (ShareDataPrx)__obj;
            }
            catch(ClassCastException ex)
            {
                if(__obj.ice_isA("::ome::services::sharing::data::ShareData"))
                {
                    ShareDataPrxHelper __h = new ShareDataPrxHelper();
                    __h.__copyFrom(__obj);
                    __d = __h;
                }
            }
        }
        return __d;
    }

    public static ShareDataPrx
    checkedCast(Ice.ObjectPrx __obj, java.util.Map<String, String> __ctx)
    {
        ShareDataPrx __d = null;
        if(__obj != null)
        {
            try
            {
                __d = (ShareDataPrx)__obj;
            }
            catch(ClassCastException ex)
            {
                if(__obj.ice_isA("::ome::services::sharing::data::ShareData", __ctx))
                {
                    ShareDataPrxHelper __h = new ShareDataPrxHelper();
                    __h.__copyFrom(__obj);
                    __d = __h;
                }
            }
        }
        return __d;
    }

    public static ShareDataPrx
    checkedCast(Ice.ObjectPrx __obj, String __facet)
    {
        ShareDataPrx __d = null;
        if(__obj != null)
        {
            Ice.ObjectPrx __bb = __obj.ice_facet(__facet);
            try
            {
                if(__bb.ice_isA("::ome::services::sharing::data::ShareData"))
                {
                    ShareDataPrxHelper __h = new ShareDataPrxHelper();
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

    public static ShareDataPrx
    checkedCast(Ice.ObjectPrx __obj, String __facet, java.util.Map<String, String> __ctx)
    {
        ShareDataPrx __d = null;
        if(__obj != null)
        {
            Ice.ObjectPrx __bb = __obj.ice_facet(__facet);
            try
            {
                if(__bb.ice_isA("::ome::services::sharing::data::ShareData", __ctx))
                {
                    ShareDataPrxHelper __h = new ShareDataPrxHelper();
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

    public static ShareDataPrx
    uncheckedCast(Ice.ObjectPrx __obj)
    {
        ShareDataPrx __d = null;
        if(__obj != null)
        {
            try
            {
                __d = (ShareDataPrx)__obj;
            }
            catch(ClassCastException ex)
            {
                ShareDataPrxHelper __h = new ShareDataPrxHelper();
                __h.__copyFrom(__obj);
                __d = __h;
            }
        }
        return __d;
    }

    public static ShareDataPrx
    uncheckedCast(Ice.ObjectPrx __obj, String __facet)
    {
        ShareDataPrx __d = null;
        if(__obj != null)
        {
            Ice.ObjectPrx __bb = __obj.ice_facet(__facet);
            ShareDataPrxHelper __h = new ShareDataPrxHelper();
            __h.__copyFrom(__bb);
            __d = __h;
        }
        return __d;
    }

    protected Ice._ObjectDelM
    __createDelegateM()
    {
        return new _ShareDataDelM();
    }

    protected Ice._ObjectDelD
    __createDelegateD()
    {
        return new _ShareDataDelD();
    }

    public static void
    __write(IceInternal.BasicStream __os, ShareDataPrx v)
    {
        __os.writeProxy(v);
    }

    public static ShareDataPrx
    __read(IceInternal.BasicStream __is)
    {
        Ice.ObjectPrx proxy = __is.readProxy();
        if(proxy != null)
        {
            ShareDataPrxHelper result = new ShareDataPrxHelper();
            result.__copyFrom(proxy);
            return result;
        }
        return null;
    }
}
