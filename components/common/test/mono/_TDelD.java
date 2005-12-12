// **********************************************************************
//
// Copyright (c) 2003-2005 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

// Ice version 3.0.0

package mono;

public final class _TDelD extends Ice._ObjectDelD implements _TDel
{
    public ome.model.roi.Roi5DRemote
    getRoi5D(java.util.Map __ctx)
	throws IceInternal.NonRepeatable
    {
	Ice.Current __current = new Ice.Current();
	__initCurrent(__current, "getRoi5D", Ice.OperationMode.Normal, __ctx);
	while(true)
	{
	    IceInternal.Direct __direct = new IceInternal.Direct(__current);
	    try
	    {
		T __servant = null;
		try
		{
		    __servant = (T)__direct.servant();
		}
		catch(ClassCastException __ex)
		{
		    Ice.OperationNotExistException __opEx = new Ice.OperationNotExistException();
		    __opEx.id = __current.id;
		    __opEx.facet = __current.facet;
		    __opEx.operation = __current.operation;
		    throw __opEx;
		}
		try
		{
		    return __servant.getRoi5D(__current);
		}
		catch(Ice.LocalException __ex)
		{
		    throw new IceInternal.NonRepeatable(__ex);
		}
	    }
	    finally
	    {
		__direct.destroy();
	    }
	}
    }
}
