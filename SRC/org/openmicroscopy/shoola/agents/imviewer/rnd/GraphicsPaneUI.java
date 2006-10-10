/*
 * org.openmicroscopy.shoola.agents.imviewer.rnd.GraphicsPaneUI
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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

package org.openmicroscopy.shoola.agents.imviewer.rnd;

// Java imports
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

// Third-party libraries

// Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;

/**
 * Component displaying the plane histogram.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $ $Date: $)
 *          </small>
 * @since OME2.2
 */
class GraphicsPaneUI extends JPanel {
	/** A temporary image of a histogram */
	ImageIcon histogramImage;

	/** A reference to the renderer model. */
	RendererModel model;

	final static Color GREYCOLOUR = new Color(128, 128, 128, 128);

	/** Creates a new instance. */
	GraphicsPaneUI(RendererModel model) {
		this.model = model;
		IconManager icons = IconManager.getInstance();
		histogramImage = icons.getImageIcon(IconManager.TEMPORARY_HISTOGRAM);
	}

	/**
	 * Overridden to paint the histogram image.
	 * 
	 * @see JPanel#paintComponent(Graphics)
	 */
	public void paintComponent(Graphics og) {
		super.paintComponent(og);
		Graphics2D g = (Graphics2D) og;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g.drawImage(histogramImage.getImage(), 0, 0, this.getWidth() - 1, this
				.getHeight() - 1, null);
		double width = this.getWidth();
		double height = this.getHeight();
		double codomainMin = model.getCodomainStart();
		double codomainMax = model.getCodomainEnd();
		double codomainRange = codomainMax - codomainMin;
		double bitResolution = Math.pow(2, model.getBitResolution());
		double domainGlobalMin = model.getGlobalMin();
		double domainGlobalMax = model.getGlobalMax();
		double domainMin = model.getWindowStart();
		double domainMax = model.getWindowEnd();
		double domainRange = domainMax - domainMin;

		double domainMinScreenX = (domainMin / domainGlobalMax) * width;
		double domainMaxScreenX = (domainMax / domainGlobalMax) * width;
		double codomainMinScreenY = ((255 - codomainMin) / 255.0f) * height;
		double codomainMaxScreenY = ((255 - codomainMax) / 255.0f) * height;
		double domainRangeScreen = domainMaxScreenX - domainMinScreenX;
		double codomainRangeScreen = codomainMinScreenY - codomainMaxScreenY;

		g.setColor(GREYCOLOUR);
		g.fillRect(0, 0, (int) domainMinScreenX, (int) height);
		g.fillRect((int) domainMaxScreenX, 0, (int) width, (int) height);

		g.fillRect(0, 0, (int) width, (int) codomainMaxScreenY);
		g.fillRect(0, (int) codomainMinScreenY, (int) width, (int) height);

		if (model.getFamily().equals(RendererModel.LINEAR)) {
			double b = codomainMinScreenY;
			double a = codomainRangeScreen / (domainRangeScreen);
			double currentX = domainMinScreenX;
			double currentY = b;
			double oldX, oldY;
			for (double x = 0; x < domainRangeScreen; x += 1) {
				oldX = currentX;
				oldY = currentY;
				currentX = x + domainMinScreenX;
				currentY = b - a * x;
				g.setColor(new Color(198, 198, 198, 255));
				g.setStroke(new BasicStroke(2.0f));
				g.drawLine((int) oldX, (int) oldY, (int) currentX,
						(int) currentY);
			}
		}
		
		if (model.getFamily().equals(RendererModel.LOGARITHMIC)) {
			double b = codomainMinScreenY;
			double a = codomainRangeScreen / Math.log(domainRangeScreen);
			double currentX = domainMinScreenX-1;
			double currentY = b;
			double oldX, oldY;
			for (double x = 1; x < domainRangeScreen; x += 1) {
				oldX = currentX;
				oldY = currentY;
				currentX = x + domainMinScreenX-1;
				currentY = b - a * Math.log(x);
				
				g.setColor(new Color(198, 198, 198, 255));
				g.setStroke(new BasicStroke(2.0f));
				g.drawLine((int) oldX, (int) oldY, (int) currentX,
						(int) currentY);
			}
		}
		if (model.getFamily().equals(model.EXPONENTIAL)) {
			double b = codomainMinScreenY;
			double a = codomainRangeScreen / Math.exp(Math.pow(domainRangeScreen
					,model.getCurveCoefficient()));
			double currentX = domainMinScreenX-1;
			double currentY = b;
			double oldX, oldY;
			for (double x = 1; x < domainRangeScreen; x += 1) {
				oldX = currentX;
				oldY = currentY;
				currentX = x + domainMinScreenX-1;
				currentY = b - a * Math.exp(Math.pow(x,
						model.getCurveCoefficient()));
				
				g.setColor(new Color(198, 198, 198, 255));
				g.setStroke(new BasicStroke(2.0f));
				g.drawLine((int) oldX, (int) oldY, (int) currentX,
						(int) currentY);
			}
		}
		if (model.getFamily().equals(model.POLYNOMIAL)) {
			double b = codomainMinScreenY;
			double a = codomainRangeScreen / Math.pow(domainRangeScreen
					,model.getCurveCoefficient());
			double currentX = domainMinScreenX-1;
			double currentY = b;
			double oldX, oldY;
			for (double x = 1; x < domainRangeScreen; x += 1) {
				oldX = currentX;
				oldY = currentY;
				currentX = x + domainMinScreenX-1;
				currentY = b - a * Math.pow(x,
						model.getCurveCoefficient());
				
				g.setColor(new Color(198, 198, 198, 255));
				g.setStroke(new BasicStroke(2.0f));
				g.drawLine((int) oldX, (int) oldY, (int) currentX,
						(int) currentY);
			}
		}
	}
}
