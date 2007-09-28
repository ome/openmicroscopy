/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.icy.service.utests;

import java.util.HashMap;

import Ice.BooleanHolder;
import Ice.ConnectionI;
import Ice.EndpointSelectionType;
import Ice.InitializationData;
import Ice.LocatorPrx;
import Ice.RouterPrx;
import IceInternal.EndpointI;
import IceInternal.Reference;

/**
 * {@link IceInternal.Reference} subclasses which can be used 
 * to stub a proxy helper. For example,
 * 
 *   IAdminPrxHelper a = new IAdminPrxHelper();
 *   a.setup(new Ref());
 *   
 * @author Josh Moore, josh at glencoesoftware.com
 *
 */
public class Ref extends IceInternal.Reference {

	
	public Ref() {
		super(inst(), communicator(), Ice.Util.stringToIdentity("test:test"), new HashMap(), "facet", 0);
	}

	static IceInternal.Instance inst() {
		IceInternal.Instance i = new IceInternal.Instance(null, new InitializationData());
		return i;
	}
	
	static Ice.Communicator communicator() {
		return null;
	}
	
	@Override
	public Reference changeAdapterId(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeCacheConnection(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeCollocationOptimization(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeCompress(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeConnectionId(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeEndpointSelection(EndpointSelectionType arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeEndpoints(EndpointI[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeLocator(LocatorPrx arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeLocatorCacheTimeout(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changePreferSecure(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeRouter(RouterPrx arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeSecure(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeThreadPerConnection(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reference changeTimeout(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAdapterId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getCacheConnection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getCollocationOptimization() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ConnectionI getConnection(BooleanHolder arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EndpointSelectionType getEndpointSelection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EndpointI[] getEndpoints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLocatorCacheTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getPreferSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getThreadPerConnection() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
