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

public final class _TDelM extends Ice._ObjectDelM implements _TDel
{
    public ome.model.roi.Roi5DRemote
    getRoi5D(java.util.Map __ctx)
	throws IceInternal.NonRepeatable
    {
	IceInternal.Outgoing __og = __connection.getOutgoing(__reference, "getRoi5D", Ice.OperationMode.Normal, __ctx, __compress);
	try
	{
	    boolean __ok = __og.invoke();
	    try
	    {
		IceInternal.BasicStream __is = __og.is();
		if(!__ok)
		{
		    try
		    {
			__is.throwException();
		    }
		    catch(Ice.UserException __ex)
		    {
			throw new Ice.UnknownUserException(__ex.ice_name());
		    }
		}
		ome.model.roi.Roi5DRemoteHolder __ret = new ome.model.roi.Roi5DRemoteHolder();
		__is.readObject(__ret.getPatcher());
		__is.readPendingObjects();
		return __ret.value;
	    }
	    catch(Ice.LocalException __ex)
	    {
		throw new IceInternal.NonRepeatable(__ex);
	    }
	}
	finally
	{
	    __connection.reclaimOutgoing(__og);
	}
    }
}
