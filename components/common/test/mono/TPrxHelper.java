// **********************************************************************
//
// Copyright (c) 2003-2005 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

// Ice version 2.1.2

package mono;

public final class TPrxHelper extends Ice.ObjectPrxHelperBase implements TPrx
{
    public ome.model.roi.Roi5DRemote
    getRoi5D()
    {
	return getRoi5D(__defaultContext());
    }

    public ome.model.roi.Roi5DRemote
    getRoi5D(java.util.Map __ctx)
    {
	int __cnt = 0;
	while(true)
	{
	    try
	    {
		__checkTwowayOnly("getRoi5D");
		Ice._ObjectDel __delBase = __getDelegate();
		_TDel __del = (_TDel)__delBase;
		return __del.getRoi5D(__ctx);
	    }
	    catch(IceInternal.NonRepeatable __ex)
	    {
		__rethrowException(__ex.get());
	    }
	    catch(Ice.LocalException __ex)
	    {
		__cnt = __handleException(__ex, __cnt);
	    }
	}
    }

    public static TPrx
    checkedCast(Ice.ObjectPrx b)
    {
	TPrx d = null;
	if(b != null)
	{
	    try
	    {
		d = (TPrx)b;
	    }
	    catch(ClassCastException ex)
	    {
		if(b.ice_isA("::mono::T"))
		{
		    TPrxHelper h = new TPrxHelper();
		    h.__copyFrom(b);
		    d = h;
		}
	    }
	}
	return d;
    }

    public static TPrx
    checkedCast(Ice.ObjectPrx b, java.util.Map ctx)
    {
	TPrx d = null;
	if(b != null)
	{
	    try
	    {
		d = (TPrx)b;
	    }
	    catch(ClassCastException ex)
	    {
		if(b.ice_isA("::mono::T", ctx))
		{
		    TPrxHelper h = new TPrxHelper();
		    h.__copyFrom(b);
		    d = h;
		}
	    }
	}
	return d;
    }

    public static TPrx
    checkedCast(Ice.ObjectPrx b, String f)
    {
	TPrx d = null;
	if(b != null)
	{
	    Ice.ObjectPrx bb = b.ice_newFacet(f);
	    try
	    {
		if(bb.ice_isA("::mono::T"))
		{
		    TPrxHelper h = new TPrxHelper();
		    h.__copyFrom(bb);
		    d = h;
		}
	    }
	    catch(Ice.FacetNotExistException ex)
	    {
	    }
	}
	return d;
    }

    public static TPrx
    checkedCast(Ice.ObjectPrx b, String f, java.util.Map ctx)
    {
	TPrx d = null;
	if(b != null)
	{
	    Ice.ObjectPrx bb = b.ice_newFacet(f);
	    try
	    {
		if(bb.ice_isA("::mono::T", ctx))
		{
		    TPrxHelper h = new TPrxHelper();
		    h.__copyFrom(bb);
		    d = h;
		}
	    }
	    catch(Ice.FacetNotExistException ex)
	    {
	    }
	}
	return d;
    }

    public static TPrx
    uncheckedCast(Ice.ObjectPrx b)
    {
	TPrx d = null;
	if(b != null)
	{
	    TPrxHelper h = new TPrxHelper();
	    h.__copyFrom(b);
	    d = h;
	}
	return d;
    }

    public static TPrx
    uncheckedCast(Ice.ObjectPrx b, String f)
    {
	TPrx d = null;
	if(b != null)
	{
	    Ice.ObjectPrx bb = b.ice_newFacet(f);
	    TPrxHelper h = new TPrxHelper();
	    h.__copyFrom(bb);
	    d = h;
	}
	return d;
    }

    protected Ice._ObjectDelM
    __createDelegateM()
    {
	return new _TDelM();
    }

    protected Ice._ObjectDelD
    __createDelegateD()
    {
	return new _TDelD();
    }

    public static void
    __write(IceInternal.BasicStream __os, TPrx v)
    {
	__os.writeProxy(v);
    }

    public static TPrx
    __read(IceInternal.BasicStream __is)
    {
	Ice.ObjectPrx proxy = __is.readProxy();
	if(proxy != null)
	{
	    TPrxHelper result = new TPrxHelper();
	    result.__copyFrom(proxy);
	    return result;
	}
	return null;
    }
}
