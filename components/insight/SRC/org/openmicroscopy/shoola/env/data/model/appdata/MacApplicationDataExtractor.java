package org.openmicroscopy.shoola.env.data.model.appdata;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import org.openmicroscopy.shoola.env.data.util.Parser;
import org.openmicroscopy.shoola.util.image.io.IconReader;

public class MacApplicationDataExtractor implements ApplicationDataExtractor {

	/** The default location on <code>MAC</code> platform. */
	public static final String LOCATION_MAC = "/Applications";

	@Override
	public String getDefaultAppDirectory() {
		return LOCATION_MAC;
	}

	/**
	 * Converts the <code>.icns</code> to an icon.
	 * 
	 * @param path
	 *            The path to the file to convert.
	 * @return See above.
	 */
	private Icon convert(String path) {
		if (path == null)
			return null;
		if (!path.endsWith("icns"))
			path += ".icns";
		IconReader reader = new IconReader(path);
		BufferedImage img = null;
		try {
			img = reader.decode(IconReader.ICON_16);
		} catch (Exception e) {
		}
		if (img == null)
			return null;
		return new ImageIcon(img);
	}

	@Override
	public ApplicationData extractAppData(File file) throws Exception {
		Map<String, Object> m = Parser.parseInfoPList(file.getAbsolutePath());
		
		String executablePath = (String) m.get(Parser.EXECUTABLE_PATH);
		Icon icon = convert((String) m.get(Parser.EXECUTABLE_ICON));
		String applicationName = (String) m.get(Parser.EXECUTABLE_NAME);
		
		ApplicationData data = new ApplicationData(icon, applicationName, executablePath);
		
		return data;
	}
}
