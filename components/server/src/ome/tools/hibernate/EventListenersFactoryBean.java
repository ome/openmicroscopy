/* ome.tools.hibernate.EventListenersFactoryBean
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package ome.tools.hibernate;

// Java imports


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//Third-party imports
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.hibernate.event.EventListeners;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.CustomizableTraceInterceptor;
import org.springframework.aop.target.EmptyTargetSource;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.Assert;

// Application-internal dependencies
import ome.security.ACLEventListener;
import ome.security.SecuritySystem;

/**
 * configuring all the possible {@link EventListeners event listeners} within
 * XML can be cumbersome. 
 */
public class EventListenersFactoryBean extends AbstractFactoryBean
{
	private SecuritySystem secSys;
	
	private EventListeners eventListeners = new EventListeners();

	private Map<String,Collection> map = new HashMap<String, Collection>();
	
	// ~ FactoryBean
	// =========================================================================
	
	public EventListenersFactoryBean( SecuritySystem securitySystem )
	{
		Assert.notNull(securitySystem);
		this.secSys = securitySystem;
		add("auto-flush", eventListeners.getAutoFlushEventListeners());
		add("merge", eventListeners.getMergeEventListeners());
		add("create", eventListeners.getPersistEventListeners());
		add("create-onflush", eventListeners.getPersistOnFlushEventListeners());
		add("delete", eventListeners.getDeleteEventListeners());
		add("dirty-check", eventListeners.getDirtyCheckEventListeners());
		add("evict", eventListeners.getEvictEventListeners());
		add("flush", eventListeners.getFlushEventListeners());
		add("flush-entity", eventListeners.getFlushEntityEventListeners());
		add("load", eventListeners.getLoadEventListeners());
		add("load-collection", eventListeners.getInitializeCollectionEventListeners());
		add("lock", eventListeners.getLockEventListeners());
		add("refresh", eventListeners.getRefreshEventListeners());
		add("replicate", eventListeners.getReplicateEventListeners());
		add("save-update", eventListeners.getSaveOrUpdateEventListeners());
		add("save", eventListeners.getSaveEventListeners());
		add("update", eventListeners.getUpdateEventListeners());
		add("pre-load", eventListeners.getPreLoadEventListeners());
		add("pre-update", eventListeners.getPreUpdateEventListeners());
		add("pre-delete", eventListeners.getPreDeleteEventListeners());
		add("pre-insert", eventListeners.getPreInsertEventListeners());
		add("post-load", eventListeners.getPostLoadEventListeners());
		add("post-update", eventListeners.getPostUpdateEventListeners());
		add("post-delete", eventListeners.getPostDeleteEventListeners());
		add("post-insert", eventListeners.getPostInsertEventListeners());
		add("post-commit-update", eventListeners.getPostCommitUpdateEventListeners());
		add("post-commit-delete", eventListeners.getPostCommitDeleteEventListeners());
		add("post-commit-insert", eventListeners.getPostCommitInsertEventListeners());
		assertHasAllKeys(map);
	}

	public Class getObjectType() {
		return Map.class;
	}

	public boolean isSingleton() {
		return true;
	}

	@Override
	protected Object createInstance() throws Exception {
		overrides();
		if (debugAll)
		{
			Object debug = getDebuggingProxy();
			for (String key : map.keySet()) {
				map.get(key).add(debug);
			}
		}
		return map;
	}
	// ~ Configuration
	// =========================================================================

	protected void overrides()
	{
		override("merge", 
				new ome.tools.hibernate.MergeEventListener(secSys));
		override(new String[]{"pre-load","post-load","pre-update","post-update",
				"pre-insert","post-insert","pre-delete","post-delete"},
				new ome.tools.hibernate.EventLogListener(
						new EventDiffHolder(secSys)));
		override(new String[]{"replicate","save","update"},
				getDisablingProxy());
		
		ACLEventListener acl = new ACLEventListener(secSys);
		map.get("post-load").add(acl);
		map.get("pre-insert").add(acl);
		map.get("pre-update").add(acl);
		map.get("pre-delete").add(acl);
	}
		
	
	protected boolean debugAll = false;
	
	public void setDebugAll( boolean debug )
	{
		this.debugAll = debug;
	}
	
	// ~ Helpers
	// =========================================================================
	private void assertHasAllKeys(Map<String,?> map)
	{
		// eventListeners has only private state. :(
	}
	
	private Class[] allInterfaces()
	{
		Set<Class> set = new HashSet<Class>();
		for (String str : map.keySet()) {
			Class iface = eventListeners.getListenerClassFor(str);
			if (iface == null)
			{
				logger.warn("No interface found for "+str);
			}
			
			else
			{
			set.add(iface);
			}
		}
		return set.toArray(new Class[set.size()]);
	}
	
	private Object getDisablingProxy()
	{
		EventMethodInterceptor disable = new EventMethodInterceptor();
		disable.setDisableAll(true);
		return getProxy(disable);
	}
	
	private Object getDebuggingProxy()
	{
		EventMethodInterceptor debug = new EventMethodInterceptor();
		debug.setDebug(true);
		return getProxy(debug);
	}

	private Object getProxy( Advice...adviceArray)
	{
		ProxyFactory factory = new ProxyFactory();
		factory.setInterfaces(allInterfaces());
		for (Advice advice : adviceArray) {
			factory.addAdvice(advice);
		}
		return factory.getProxy();		
	}
	
	private void override(String[] keys, Object object){
		for (String key : keys) {
			override(key, object);
		}
	}
	
	private void override(String key, Object object)
	{
		map.remove(key);
		map.put(key, new ArrayList());
		map.get(key).add(object);
	}
	
	private void add(String key, Object[] objs)
	{
		map.put(key,new ArrayList(Arrays.asList(objs)));
	}

}
