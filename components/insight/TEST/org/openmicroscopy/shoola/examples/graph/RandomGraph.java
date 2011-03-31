/*
 * org.openmicroscopy.shoola.examples.graph.RandomArboretum
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
package org.openmicroscopy.shoola.examples.graph;
//Java imports
import java.awt.Color;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


//Third-party libraries
import traer.physics.*;
import processing.core.PApplet;

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
public class RandomGraph
	extends PApplet
{
	/** The node size in the graph. */
	final float NODE_SIZE = 20;
	
	/** The length of the edge in the graph. */
	final float EDGE_LENGTH = 20;
	
	/** The strengthg of the spring connection. */
	final float EDGE_STRENGTH = 0.2f;
	
	/** Spacer strength. */
	final float SPACER_STRENGTH = 1000;
	
	/** The coloyr of the selected node. */
	final int selectedNodeColour = new Color(255, 102,102).getRGB();
	
	/** The Colour of the selected, fixed node. */
	final int selectedFixedNodeColour = new Color(128, 64,64).getRGB();
	
	/** The colour of fixed nodes. */
	final int fixedNodeColour = new Color(102, 204, 255).getRGB();
	
	/** The colour of other nodes. */
	final int otherNodesColour = 160;
	
	/** The particle system. */
	ParticleSystem physics;
	
	/** The picker. */
	//Picker picker;
	
	/** The scale of the zooming level. */
	float scale = 1;
	
	/** <code>true</code> if a node has been picked. */
	boolean picked;
	
	/** The picked particle. */
	Particle p;
	
	/** The id of the picked particle. */
	int pickedId;
	
	/** The scale of the zooming level. */
	float zoom=1;
	
	/** Set of fixed particles. */
	Set<Integer> madeFixed;
	
	/**
	 * Instatiate the main class. 
	 * @param args
	 */
	public static void main(String[] args)
	{
		PApplet.main(new String[] {"org.openmicroscopy.shoola.examples.graph.RandomGraph"});
	}
	
	/**
	 * Setup the processing element.
	 */
	public void setup()
	{
		addMouseWheelListener(new java.awt.event.MouseWheelListener() 
		{ 
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) 
			{ 
			      mouseWheel(evt.getWheelRotation());
			 }
		}); 
		madeFixed = new HashSet<Integer>();
		picked = false;
		pickedId=-1;
		size( 800, 800 );
		smooth();
		strokeWeight( 2 );
		ellipseMode( CENTER );       
	  
		physics = new ParticleSystem(0, 0.1f );
		//picker = new Picker(this);
		  
		initialize();
	}

	/**
	 * Called when the mouse wheel is moved; used to change the zooming.
	 * @param i The amount the wheel moved.
	 */
	public void mouseWheel(int i)
	{
		zoom = zoom+((float)i)/10.0f;
	}
	
	/**
	 * Call the drawing method in the processing lib.
	 */
	public void draw()
	{
	  physics.tick();
		if ( physics.numberOfParticles() > 1 && !picked)
	  {
	    //updateCentroid();
	  }
	  background( 255 );
	  fill( 0 );
	  text( "" + physics.numberOfParticles() + " PARTICLES\n" + (int)frameRate + " FPS", 10, 20 );
	  translate( width/2 , height/2 );
	  scale( scale );
	 
	  //pushMatrix();
	  scale(zoom);
	  drawNetwork();
	  //popMatrix();
	}

	/**
	 * Draw the network.
	 */
	public void drawNetwork()
	{      
	  // draw vertices
	  fill( 160 );
	  noStroke();
	  for ( int i = 0; i < physics.numberOfParticles(); ++i )
	  {
	    Particle v = physics.getParticle( i );
	    //picker.start(i);
	    
	    if(i==pickedId)
	    {
	    	if(madeFixed.contains(i))
	    		fill(selectedFixedNodeColour);
	    	else
	    		fill(selectedNodeColour);
	    }else if(madeFixed.contains(i))
	    {
    		fill(fixedNodeColour);
	    }	
	    else
	    {
	    	fill(otherNodesColour);
	    }
	    ellipse( v.position().x(), v.position().y(), NODE_SIZE, NODE_SIZE );
		// picker.stop();
	  }

	  // draw edges 
	  stroke( 0 );
	  for ( int i = 0; i < physics.numberOfSprings(); ++i )
	  {
	    Spring e = physics.getSpring( i );
	    Particle a = e.getOneEnd();
	    Particle b = e.getTheOtherEnd();
	    //picker.start(physics.numberOfParticles()+i);
	    //beginShape( LINES );
	    

	    	//		a.position().y()-1, b.position().x()-1, b.position().y()+1,b.position().x()+1, b.position().y()-1);
	    //strokeWeight(30);
	    line(a.position().x(), a.position().y(),b.position().x(), b.position().y()  );
		 //   vertex( a.position().x(), a.position().y() );
		 //   vertex( b.position().x(), b.position().y() );
		//endShape();
		//picker.stop();
	  }
	  
	}

	/**
	 * The mouse has been released, after a drag.
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
	 * The mouse has been pressed, check to see if a node has been selected.
	 */
	public void mousePressed()
	{
			
		 int id = -1;//picker.get(mouseX, mouseY);
		 System.err.println(id);
		  if(id<0)
			  return;
		  else if(id<physics.numberOfParticles())
		  {
		    	if(mouseEvent.getClickCount()==2) 
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
	 * The mouse has been dragged, check to see if a node has been picked, and
	 * then moved.
	 */
	public void mouseDragged()
	{
		if(picked==true)
		{
		 
		System.err.println(p.position());
		System.err.println(mouseX+" "+ mouseY);
		System.err.println(dmouseX+" "+ dmouseY);
		System.err.println(p.position());
		//physics.removeSpring(id);
		//p.setMass(30);
		p.position().set(p.position().x()+(mouseX-dmouseX),p.position().y()+(mouseY-dmouseY),p.position().z());
		}
	}

	/**
	 * If the 'c' key is pressed reset the model, if the space is pressed 
	 * add a new random node.
	 */
	public void keyPressed()
	{
	  if ( key == 'c' )
	  {
	    initialize();
	    return;
	  }
	  
	  if ( key == ' ' )
	  {
	    addNode();
	    return;
	  }
	}

	// ME ////////////////////////////////////////////

	
	/**
	 * Add a spacer between particles p, r
	 * @param p The first particle.
	 * @param q The first particle.
	 */
	public void addSpacersToNode( Particle p, Particle r )
	{
	  for ( int i = 0; i < physics.numberOfParticles(); ++i )
	  {
	    Particle q = physics.getParticle( i );
	    if ( p != q && p != r )
	      physics.makeAttraction( p, q, -SPACER_STRENGTH, 20 );
	  }
	}

	/**
	 * Make an edge between particles in the model.
	 * @param a First particle.
	 * @param b Second Particle.
	 */
	public void makeEdgeBetween( Particle a, Particle b )
	{
	  physics.makeSpring( a, b, EDGE_STRENGTH, EDGE_STRENGTH, EDGE_LENGTH );
	}

	/**
	 * Initialise the physics engine. 
	 */
	public void initialize()
	{
	  physics.clear();
	  physics.makeParticle();
	  madeFixed.add(0);
	  physics.getParticle(0).makeFixed();
	}

	/**
	 * Add a new random node to the engine.
	 */
	public void addNode()
	{ 
	  Particle p = physics.makeParticle();
	  Particle q = physics.getParticle( (int)random( 0, physics.numberOfParticles()-1) );
	  while ( q == p )
	    q = physics.getParticle( (int)random( 0, physics.numberOfParticles()-1) );
	  addSpacersToNode( p, q );
	  makeEdgeBetween( p, q );
	  p.position().set( q.position().x() + random( -1, 1 ), q.position().y() + random( -1, 1 ), 0 );
	  q.setMass(q.mass()+1);
	}

}
