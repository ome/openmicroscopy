package ui.components;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import util.BareBonesBrowserLaunch;
import util.ImageFactory;

public class OLSLinkPanel extends JPanel {
	
	public OLSLinkPanel() {
		this.setLayout(new BorderLayout());
		
		this.setBorder(new EtchedBorder());
		
		// a link to the Ontology-Lookup-Service website
		Icon olsIcon = ImageFactory.getInstance().getIcon(ImageFactory.OLS_LOGO_SMALL);
		JButton olsButton = new JButton("Uses the EBI OLS", olsIcon);
		olsButton.setBorder(new EmptyBorder(2,2,2,2));
		olsButton.addActionListener(new OlsLinkListener());
		
		// makes the panel shrink to the size of the button (if in WEST/EAST)
		this.setAlignmentX(LEFT_ALIGNMENT);
		this.add(olsButton, BorderLayout.WEST);
	}
	
	
	public class OlsLinkListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			BareBonesBrowserLaunch.openURL("http://www.ebi.ac.uk/ontology-lookup");
		}
	}

}
