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

public final class ObjPrxHelper extends Ice.ObjectPrxHelperBase implements ObjPrx
{
    public static ObjPrx
    checkedCast(Ice.ObjectPrx __obj)
    {
        ObjPrx __d = null;
        if(__obj != null)
        {
            try
            {
                __d = (ObjPrx)__obj;
            }
            catch(ClassCastException ex)
            {
                if(__obj.ice_isA("::ome::services::sharing::data::Obj"))
                {
                    ObjPrxHelper __h = new ObjPrxHelper();
                    __h.__copyFrom(__obj);
                    __d = __h;
                }
            }
        }
        return __d;
    }

    public static ObjPrx
    checkedCast(Ice.ObjectPrx __obj, java.util.Map<String, String> __ctx)
    {
        ObjPrx __d = null;
        if(__obj != null)
        {
            try
            {
                __d = (ObjPrx)__obj;
            }
            catch(ClassCastException ex)
            {
                if(__obj.ice_isA("::ome::services::sharing::data::Obj", __ctx))
                {
                    ObjPrxHelper __h = new ObjPrxHelper();
                    __h.__copyFrom(__obj);
                    __d = __h;
                }
            }
        }
        return __d;
    }

    public static ObjPrx
    checkedCast(Ice.ObjectPrx __obj, String __facet)
    {
        ObjPrx __d = null;
        if(__obj != null)
        {
            Ice.ObjectPrx __bb = __obj.ice_facet(__facet);
            try
            {
                if(__bb.ice_isA("::ome::services::sharing::data::Obj"))
                {
                    ObjPrxHelper __h = new ObjPrxHelper();
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

    public static ObjPrx
    checkedCast(Ice.ObjectPrx __obj, String __facet, java.util.Map<String, String> __ctx)
    {
        ObjPrx __d = null;
        if(__obj != null)
        {
            Ice.ObjectPrx __bb = __obj.ice_facet(__facet);
            try
            {
                if(__bb.ice_isA("::ome::services::sharing::data::Obj", __ctx))
                {
                    ObjPrxHelper __h = new ObjPrxHelper();
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

    public static ObjPrx
    uncheckedCast(Ice.ObjectPrx __obj)
    {
        ObjPrx __d = null;
        if(__obj != null)
        {
            ObjPrxHelper __h = new ObjPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
        }
        return __d;
    }

    public static ObjPrx
    uncheckedCast(Ice.ObjectPrx __obj, String __facet)
    {
        ObjPrx __d = null;
        if(__obj != null)
        {
            Ice.ObjectPrx __bb = __obj.ice_facet(__facet);
            ObjPrxHelper __h = new ObjPrxHelper();
            __h.__copyFrom(__bb);
            __d = __h;
        }
        return __d;
    }

    protected Ice._ObjectDelM
    __createDelegateM()
    {
        return new _ObjDelM();
    }

    protected Ice._ObjectDelD
    __createDelegateD()
    {
        return new _ObjDelD();
    }

    public static void
    __write(IceInternal.BasicStream __os, ObjPrx v)
    {
        __os.writeProxy(v);
    }

    public static ObjPrx
    __read(IceInternal.BasicStream __is)
    {
        Ice.ObjectPrx proxy = __is.readProxy();
        if(proxy != null)
        {
            ObjPrxHelper result = new ObjPrxHelper();
            result.__copyFrom(proxy);
            return result;
        }
        return null;
    }
}
