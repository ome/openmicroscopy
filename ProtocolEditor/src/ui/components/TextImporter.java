
/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

package ui.components;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.StringReader;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;


public class TextImporter extends JPanel{

	protected JTextArea textArea;
	
	public TextImporter() {
		
		setLayout(new BorderLayout());
		
		textArea = new JTextArea();
		
		textArea.setText("new string test \nnew line");
		
		textArea.setRows(20);
		textArea.setColumns(40);
		
		this.add(textArea, BorderLayout.CENTER);
		
		JButton importButton = new JButton("import");
		importButton.addActionListener(new ImportListener());
		this.add(importButton, BorderLayout.SOUTH);
		
	}
	
	
	public class ImportListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String wholeText = textArea.getText();
			
			StringReader sr = new StringReader(wholeText);
			
			BufferedReader br = new BufferedReader(sr);
			
			try {
				String newLine = br.readLine();
				
				while (newLine != null) {
					System.out.println(newLine);
					newLine = br.readLine();
				}
				
			} catch (IOException ioEx) {
				// TODO Auto-generated catch block
				ioEx.printStackTrace();
			}
		}
		
	}
	
	public static void main(String[] args) {
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.getContentPane().add(new TextImporter());
		
		frame.pack();
		frame.setVisible(true);
	}
	
}
