/*
 * org.openmicroscopy.shoola.util.processing.graph.NetworkGraph
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *----------------------------------------------------------------------------*/
package org.openmicroscopy.shoola.util.processing.graph;

//Java imports

//Third-party libraries
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import picking.Picker;
import processing.core.PApplet;
import traer.physics.Attraction;
import traer.physics.Particle;
import traer.physics.ParticleSystem;
import traer.physics.Spring;

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class NetworkGraph
{
	/** The size of the nodes in the graph. */
	float nodeSize; 
	/** The length of the edges in the graph. */
	float edgeLength;
	
	/** The strength of the connections in the nodes in the graph. */
	float edgeStrength;
	
	/** The spacer strength of the nodes in the graph. */
	float spacerStrength; 
	
	/** The colour of the selected nodes in the graph. */
	int selectedNodeColour;
	
	/** The colour of the selected fixed nodes in the graph. */
	int selectedFixedNodeColour;
	
	/** The colour of the fixed nodes in the graph. */
	int fixedNodeColour;
	
	/** The colour of the non-fixed, selected nodes of the graph. */
	int otherNodesColour;
	
	/** The Processing parent object. */
	private PApplet parent;
	
	/** The data to represent the nodes in the graph. */
	GraphNode root;
	
	/** The Physics object. */
	ParticleSystem physics;
	
	/** The picker system. */
	Picker picker;
	
	/** Has an object been picked. */
	boolean picked;

	/** The id of the picked element in the graph. */
	int pickedId;
	
	/** The nodes that are fixed in the graph. */
	Set<Integer> madeFixed;
	
	/** The map of particles to nodes. */
	Map<Particle, GraphNode> nodeMap;
	
	/** The picked particle. */
	Particle p;
	
	/**
	 * The Network graph object, inits the physics engine and instantiates the
	 * graph data.
	 * @param parent See above.
	 * @param graphData See above.
	 */
	public NetworkGraph(PApplet parent, GraphNode root)
	{
		this.parent = parent;
		this.root = root;
		initialise();
		buildGraph();
	}
	
	/**
	 * Set the particle system, node colours, etc.
	 */
	private void initialise()
	{
		physics = new ParticleSystem(0,0.2f);
		nodeSize = 20;
		edgeLength = 20;
		edgeStrength = 0.2f;
		spacerStrength = 1000;	
		selectedNodeColour = new Color(255, 102,102).getRGB();
		selectedFixedNodeColour = new Color(128, 64,64).getRGB();
		fixedNodeColour = new Color(102, 204, 255).getRGB();
		otherNodesColour = 160;
		pickedId = -1;
		madeFixed = new HashSet<Integer>();
		nodeMap = new HashMap<Particle, GraphNode>();
		picker=new Picker(parent);
		picked = false;
		p = null;
		initPhysics();
	}

	public void removeSprings()
	{				physics.setGravity(0);

		for(int i = 0 ; i < physics.numberOfSprings(); i++)
		{
			
			Spring a = physics.getSpring(i);
			Particle p1 = a.getOneEnd();
			Particle p2 = a.getTheOtherEnd();
			GraphNode n1 = nodeMap.get(p1);
			GraphNode n2 = nodeMap.get(p2);
			//if(n2.getParent()!=null)
			//if((n2.getChildren().size()==0))
			//{
			//a.setStrength(0);
			if((nodeMap.get(a.getTheOtherEnd()).getChildren().size()==0))
			{
				//a.turnOff();
			}
			//}
			
		}
		for(int i = 0 ; i < physics.numberOfAttractions(); i++)
		{
			Attraction a = physics.getAttraction(i);
			if((nodeMap.get(a.getTheOtherEnd()).getChildren().size()==0))
			{
				a.turnOff();
			}
		}
		
	}
	
	/**
	 * Tick the physics engine.
	 */
	public void tick()
	{
		
		physics.tick();
	}
	
	/**
	 * Draw the network on the parent.
	 */
	public void drawNetwork()
	{      
	  // draw vertices
	  parent.fill( 160 );
	  parent.noStroke();
	  for ( int i = 0; i < physics.numberOfParticles(); ++i )
	  {
	    Particle v = physics.getParticle( i );
	    picker.start(i);
	    
	    if(i==pickedId)
	    {
	    	if(madeFixed.contains(i))
	    		parent.fill(selectedFixedNodeColour);
	    	else
	    		parent.fill(selectedNodeColour);
	    }else if(madeFixed.contains(i))
	    {
	    	parent.fill(fixedNodeColour);
	    }	
	    else
	    {
	    	parent.fill(otherNodesColour);
	    }
	    GraphNode node = nodeMap.get(v);
	    node.render(parent, v.position().x(), v.position().y());
	    picker.stop();
	  }

	  // draw edges 
	  parent.stroke( 0 );
	  for ( int i = 0; i < physics.numberOfSprings(); ++i )
	  {
	    Spring e = physics.getSpring( i );
	    Particle a = e.getOneEnd();
	    Particle b = e.getTheOtherEnd();
	    //picker.start(physics.numberOfParticles()+i);
	    parent.line(a.position().x(), a.position().y(),b.position().x(), b.position().y()  );
		 //   vertex( a.position().x(), a.position().y() );
		 //   vertex( b.position().x(), b.position().y() );
		//endShape();
		//picker.stop();
	  }
	  
	}
	
	/**
	 * Set the root node of the graph.
	 * @param node See above.
	 */
	public void setRootNode(GraphNode node)
	{
		root = node;
		buildGraph();
	}
	
	/**
	 * Initialise the Particle Model.
	 */
	public void initPhysics()
	{
		physics.clear();
		/*physics.makeParticle();
		madeFixed.add(0);
		physics.getParticle(0).makeFixed();*/
	}

	/**
	 * Make edge between nodes a, and b
	 * @param a See above.
	 * @param b See above.
	 */
	private void makeEdgeBetween( Particle a, Particle b )
	{
		float length = edgeLength;
		length = length * (float)(nodeMap.get(b).getChildren().size());
		float strength = edgeStrength;
		strength = strength / (float)(nodeMap.get(b).getChildren().size()/2);
		physics.makeSpring( a, b, edgeStrength, edgeStrength, edgeLength );
	}
	
	/**
	 * Add spacers between node p, and all others.
	 * @param p See above.
	 * @param r Seea above.
	 */
	public void addSpacersToNode( Particle p, Particle r )
	{
	  for ( int i = 0; i < physics.numberOfParticles(); ++i )
	  {
	    Particle q = physics.getParticle( i );
	    if ( p != q && p != r )
	      physics.makeAttraction( p, q, -spacerStrength, 20 );
	  }
	}
	
	/**
	 * build the graph from the root node.
	 */
	private void buildGraph()
	{
		LinkedList<GraphNode> queue = new LinkedList<GraphNode>();
		queue.addLast(root);
		while(queue.size()!=0)
		{
			GraphNode node = queue.removeFirst();
			addNode(node);
			for(GraphNode childNode : node.getChildren())
			{
				queue.addLast(childNode);
			}
		}
		
	}
	
	/**
	 * Add the node to the particle model.
	 * @param node See above.
	 */
	public void addNode(GraphNode node)
	{
		if(node.getParent() == null)
		{
			Particle p = physics.makeParticle();
			madeFixed.add(0);
			p.makeFixed();
			node.setNode(p);
			nodeMap.put(p,node);
		}
		else
		{
			Particle p = physics.makeParticle();
			node.setNode(p);
			nodeMap.put(p, node);
			Particle q = node.getParent().getNode();
			addSpacersToNode( p, q );
			makeEdgeBetween( p, q );
			p.position().set( q.position().x() + parent.random( -1, 1 ), q.position().y() + parent.random( -1, 1 ), 0 );
		}
	}
	
	/**
	 * Add node to the graph, attached to particle p;
	 * @param node The node to add to model.
	 * @param p the particle the node is attached to.
	 */
	public void addNode(GraphNode node, Particle q)
	{ 
		Particle p = physics.makeParticle();
		node.setNode(p);
		nodeMap.put(p, node);
		addSpacersToNode( p, q );
		makeEdgeBetween( p, q );
		p.position().set( q.position().x() + parent.random( -1, 1 ), q.position().y() + parent.random( -1, 1 ), 0 );
		//p.setMass(node.getParent().getChildren().size()*50);
	}
	
	/**
	 * The mouse has been released.
	 */
	public void mouseReleased()
	{
		if(picked==true)
		{
			p.makeFree();
			if(pickedId==0)
				p.makeFixed();
			//p.setMass(1);
			picked = false;

		}
	}
	
	/**
	 * The mouse has been pressed.
	 * @param mouseX The x coordinate of the mouse.
	 * @param mouseY The y coordinate of the mouse.
	 */
	public void mousePressed(float mouseX, float mouseY)
	{
			
		 int id = picker.get((int)mouseX, (int)mouseY);
		 System.err.println(id);
		  if(id<0)
			  return;
		  else if(id<physics.numberOfParticles())
		  {
		    	if(parent.mouseEvent.getClickCount()==2) 
		    	{
		    		p = physics.getParticle(id);
		    		if(!madeFixed.contains(id))
		    		{
		    			p.makeFixed();
		    			madeFixed.add(id);
		    		}
		    		else
		    		{
		    			p.makeFree();
		    			madeFixed.remove(id);
		    		}
		    	}
		    	else
		    	{
		    	pickedId=id;
		    	picked = true;
		    	p = physics.getParticle(id);
		    	p.makeFixed();
		    	}
		    	System.err.println(id);
		  }
		  else
		  {
			  System.err.println("SPRING : " + id);
		  }
	}

	/**
	 * The mouse has been released.
	 * @param mouseX The x coordinate of the mouse.
	 * @param mouseY The y coordinate of the mouse.
	 * @param dmouseX The old x coordinate of the mouse.
	 * @param dmouseY The old y coordinate of the mouse.
	 */
	public void mouseDragged(float mouseX, float mouseY, float dmouseX, float dmouseY)
	{
		if(picked==true)
		{
		 
		System.err.println(p.position());
		System.err.println(p.position());
		//physics.removeSpring(id);
		//p.setMass(30);
		p.position().set(p.position().x()+(mouseX-dmouseX),p.position().y()+(mouseY-dmouseY),p.position().z());
		}
	}
	
}
