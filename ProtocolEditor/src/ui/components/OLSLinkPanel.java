package ui.components;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.BareBonesBrowserLaunch;
import util.ImageFactory;

public class OLSLinkPanel extends JPanel {
	
	public OLSLinkPanel() {
		this.setLayout(new BorderLayout());
		
		// a link to the Ontology-Lookup-Service website
		Icon olsIcon = ImageFactory.getInstance().getIcon(ImageFactory.OLS_LOGO_SMALL);
		JLabel olsLabel = new JLabel("Uses the EBI OLS");
		JButton olsButton = new JButton(olsIcon);
		olsButton.setBorder(null);
		olsButton.addActionListener(new OlsLinkListener());
		this.add(olsLabel, BorderLayout.WEST);
		this.add(olsButton, BorderLayout.CENTER);
	}
	
	
	public class OlsLinkListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			BareBonesBrowserLaunch.openURL("http://www.ebi.ac.uk/ontology-lookup");
		}
	}

}
