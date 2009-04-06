package ome.ij;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.StringTokenizer;

/**
 * Stores credentials for logging into an OMERO server.
 */
public class OMEROCredentials {

	// -- Fields --

	public String server;
	public String port;
	public Integer portInteger;
	public String username;
	public String password;
	public String session;
	public long imageID;
	public boolean isOMERO;

	// -- Constructor --

	public OMEROCredentials(String server, String username, String password) {
		this.server = server;
		this.username = username;
		this.password = password;
	}

	/**
	 * Get credentials from a string. The following two formats are recognized:
	 * <code>ip.address?port=54321&username=login&password=secret&session=17f14177-6d5c-494c-a4e7-bbad92672d17&id=12345</code>
	 * or:
	 * <pre>
	 * server=ip.address
	 * port=54321
	 * user=login
	 * password=secret
	 * session=17f14177-6d5c-494c-a4e7-bbad92672d17
	 * id=12345
	 * </pre>
	 * Strings are assumed to be encoded with the HTML form encoding scheme,
	 * and will be decoded accordingly.
	 */
	public OMEROCredentials(String s) {
		final String invalidMsg = "Invalid credentials string";
		if (s == null) {
			throw new IllegalArgumentException(invalidMsg);
		}

		String split = s.indexOf("\n") < 0 ? "?&" : "\n";
		StringTokenizer st = new StringTokenizer(s, split);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();

			int equals = token.indexOf("=");
			String key = equals < 0 ? "server" : token.substring(0, equals);
			String value = token.substring(equals + 1);

			try {
				key = URLDecoder.decode(key, "UTF-8").trim();
				value = URLDecoder.decode(value, "UTF-8").trim();
			}
			catch (UnsupportedEncodingException exc) {
				throw new IllegalArgumentException(invalidMsg, exc);
			}

			if (key.equals("server")) server = value;
			else if (key.equals("username")) username = value;
			else if (key.equals("port")) 
			{
				port = value;
				try {
					portInteger = Integer.parseInt(value);
				}
				catch (NumberFormatException exc) {
					throw new IllegalArgumentException(invalidMsg, exc);
				}
			}
			else if (key.equals("password")) password = value;
			else if (key.equals("session")) session = value;
			else if (key.equals("id")) {
				try {
					imageID = Long.parseLong(value);
				}
				catch (NumberFormatException exc) {
					throw new IllegalArgumentException(invalidMsg, exc);
				}
			}
		}
	}
}
