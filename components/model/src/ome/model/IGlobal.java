/*
 *   ome.model.IGlobal
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.model;

/**
 * marker interface for all global types, which have no {@link Details} fields
 * other than permissions. It can be assumed that such entities will always have
 * null for the other {@link Details}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public interface IGlobal extends IObject {

}
