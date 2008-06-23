/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
 *------------------------------------------------------------------------------
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ols.Ontologies;
import ols.OntologyLookUp;

public class OLSMetadataPanel extends JPanel {

	public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);
	
	// variables
	String ontologyId;
	String termId;
	String termName;
	Map termMetaData;
	
	Box horizontalBox;
	
	JEditorPane metadataPane;
	String metadataHTML = "";
	
	public OLSMetadataPanel (String termId) {
		
		this.ontologyId = Ontologies.getOntologyIdFromTermId(termId);
		this.termId = termId;
		
		metadataPane = new JEditorPane("text/html", metadataHTML);
		metadataPane.setEditable(false);
		
		horizontalBox = Box.createHorizontalBox();
		horizontalBox.add(metadataPane);
		
		refreshTermMetadata();
		
		this.setLayout(new BorderLayout());
		this.add(horizontalBox, BorderLayout.WEST);
	}
	
	public void resetTerm(String termId) {
		
		if (termId == null) {
			this.ontologyId = null;
			this.termId = null;
		} else {
			this.ontologyId = Ontologies.getOntologyIdFromTermId(termId);
			this.termId = termId;
		}
		
		refreshTermMetadata();
	}
	
	public void refreshTermMetadata() {
		
		if ((termId == null) || (ontologyId == null)) {
			metadataPane.setText("");
			this.validate();
			return;
		}
		
		termMetaData = OntologyLookUp.getTermMetadata(termId, ontologyId);
		
		Map termParents = OntologyLookUp.getTermParents(termId, ontologyId);
		
		// definitionHeader and definitionText are not shown, unless definitionText is > 0
		String definitionHeader = "";
		String definition = "";
		
		String parentsHtml = "";
		
		if (termMetaData.get("definition") != null) {
			definition = termMetaData.get("definition").toString();
			if (definition.length() > 0) {
				metadataPane.setVisible(true);

				definitionHeader = "Definition";	
			}
		}
		
		
		for (Iterator i = termParents.keySet().iterator(); i.hasNext();){
			String key = (String) i.next();
			String name = termParents.get(key).toString();
			parentsHtml = parentsHtml + "<a href='#'>" + name + "</a><br>";
		}
		
		metadataHTML = "<html><table>" +
				"<tr><td valign='top' align='left'><b>" + definitionHeader + "</b></td> " + 
				"<td valign='top' align='left' width=350>" + definition + "</td>" +
				"<td valign='top' align='left'>" + "<b>Parents</b>" + "</td>" +
				"<td valign='top' align='left' width=200>" + parentsHtml + "</td></tr></table></html>";
	
	
		
		metadataPane.setText(metadataHTML);
		this.validate();
	}
}
