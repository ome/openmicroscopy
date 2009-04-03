package wiki;


/**
 *  @(#)Wiki.java 0.16 15/06/2008
 *  Copyright (C) 2007 - 2008 MER-C
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 3
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
 
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import java.util.zip.*;
import javax.imageio.*;
import javax.security.auth.login.*; // useful exception types
 
/**
 *  This is a somewhat sketchy bot framework for editing MediaWiki wikis.
 *  Requires JDK 1.5 (5.0) or greater. Uses the [[mw:API|MediaWiki API]] for
 *  most operations. It is recommended that the server runs the latest version
 *  of MediaWiki (1.12), otherwise some functions may not work.
 *
 *  <p>
 *  A typical program would go something like this:
 *
 *  <pre>
 *  Wiki wiki;
 *  File f = new File("wiki.dat");
 *  if (f.exists()) // we already have a copy on disk
 *  {
 *      ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
 *      wiki = (Wiki)in.readObject();
 *  }
 *  else
 *  {
 *      try
 *      {
 *          wiki = new Wiki("en.wikipedia.org"); // create a new wiki connection to en.wikipedia.org
 *          wiki.setThrottle(5000); // set the edit throttle to 0.2 Hz
 *          wiki.login("ExampleBot", password); // log in as user ExampleBot, with the specified password
 *      }
 *      catch (FailedLoginException ex)
 *      {
 *          // deal with failed login attempt
 *      }
 *  }
 *  String[] titles = . . . ; // fetch a list of titles
 *  for (int i = 0; i < titles.length; i++)
 *  {
 *      new Thread()
 *      {
 *          public void run()
 *          {
 *              try
 *              {
 *                  // do something with titles[i]
 *              }
 *              catch (Exception ex)
 *              {
 *                  if (ex.getClass().equals(CredentialException.class))
 *                      // deal with protected page
 *                  else if (ex.getClass().equals(CredentialNotFoundException.class))
 *                      // deal with trying to do something we can't
 *                  else if (ex instanceof AccountLockedException)
 *                      // deal with being blocked
 *                  else if (ex instanceof IOException)
 *                      // deal with I/O error
 *              }
 *      }.start();
 *  }
 *  </pre>
 *
 *  Don't forget to release system resources held by this object when done.
 *  This may be achieved by logging out of the wiki. Since <tt>logout()</tt> is
 *  entirely offline, we can have a persistent session by simply serializing
 *  this wiki, then logging out as follows:
 *
 *  <pre>
 *  File f = new File("wiki.dat");
 *  ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
 *  out.writeObject(wiki); // if we want the session to persist
 *  out.close();
 *  wiki.logout();
 *  </pre>
 *
 *  Long term storage of data (particularly greater than 20 days) is not
 *  recommended as the cookies may expire on the server.
 *
 *  <h4>Assertions</h4>
 *
 *  Without too much effort, it is possible to emulate assertions supported
 *  by [[mw:Extension:Assert Edit]]. The extension need not be installed
 *  for these assertions to work. Use <tt>setAssertionMode(int mode)</tt>
 *  to set the assertion mode. Checking for login, bot flag or new messages is
 *  supported by default. Other assertions can easily be defined, see {@link
 *  http://java.sun.com/j2se/1.4.2/docs/guide/lang/assert.html Programming
 *  With Assertions}. Assertions are applied on write methods only and are
 *  disabled by default.
 *
 *  <p>
 *  IMPORTANT: You need to run the program with the flag -enableassertions
 *  or -ea to enable assertions, example: <tt>java -ea Mybot</tt>.
 *
 *  <p>
 *  Please file bug reports at [[User talk:MER-C/Wiki.java#Bugs]]. Revision
 *  history is on the same page.
 *  <!-- all wikilinks are relative to the English Wikipedia -->
 *
 *  @author MER-C
 *  @version 0.16
 */
public class Wiki implements Serializable
{
    // namespaces
 
    /**
     *  Denotes the namespace of images and media, such that there is no
     *  description page. Uses the "Media:" prefix.
     *  @see IMAGE_NAMESPACE
     *  @since 0.03
     */
    public static final int MEDIA_NAMESPACE = -2;
 
    /**
     *  Denotes the namespace of pages with the "Special:" prefix. Note
     *  that many methods dealing with special pages may spew due to
     *  raw content not being available.
     *  @since 0.03
     */
    public static final int SPECIAL_NAMESPACE = -1;
 
    /**
     *  Denotes the main namespace, with no prefix.
     *  @since 0.03
     */
    public static final int MAIN_NAMESPACE = 0;
 
    /**
     *  Denotes the namespace for talk pages relating to the main
     *  namespace, denoted by the prefix "Talk:".
     *  @since 0.03
     */
    public static final int TALK_NAMESPACE = 1;
 
    /**
     *  Denotes the namespace for user pages, given the prefix "User:".
     *  @since 0.03
     */
    public static final int USER_NAMESPACE = 2;
 
    /**
     *  Denotes the namespace for user talk pages, given the prefix
     *  "User talk:".
     *  @since 0.03
     */
    public static final int USER_TALK_NAMESPACE = 3;
 
    /**
     *  Denotes the namespace for pages relating to the project,
     *  with prefix "Project:". It also goes by the name of whatever
     *  the project name was.
     *  @since 0.03
     */
    public static final int PROJECT_NAMESPACE = 4;
 
    /**
     *  Denotes the namespace for talk pages relating to project
     *  pages, with prefix "Project talk:". It also goes by the name
     *  of whatever the project name was, + "talk:".
     *  @since 0.03
     */
    public static final int PROJECT_TALK_NAMESPACE = 5;
 
    /**
     *  Denotes the namespace for image description pages. Has the prefix
     *  prefix "Image:". Do not create these directly, use upload() instead.
     *  @see MEDIA_NAMESPACE
     *  @since 0.03
     */
    public static final int IMAGE_NAMESPACE = 6;
 
    /**
     *  Denotes talk pages for image description pages. Has the prefix
     *  "Image talk:".
     *  @since 0.03
     */
    public static final int IMAGE_TALK_NAMESPACE = 7;
 
    /**
     *  Denotes the namespace for (wiki) system messages, given the prefix
     *  "MediaWiki:".
     *  @since 0.03
     */
    public static final int MEDIAWIKI_NAMESPACE = 8;
 
    /**
     *  Denotes the namespace for talk pages relating to system messages,
     *  given the prefix "MediaWiki talk:".
     *  @since 0.03
     */
    public static final int MEDIAWIKI_TALK_NAMESPACE = 9;
 
    /**
     *  Denotes the namespace for templates, given the prefix "Template:".
     *  @since 0.03
     */
    public static final int TEMPLATE_NAMESPACE = 10;
 
    /**
     *  Denotes the namespace for talk pages regarding templates, given
     *  the prefix "Template talk:".
     *  @since 0.03
     */
    public static final int TEMPLATE_TALK_NAMESPACE = 11;
 
    /**
     *  Denotes the namespace for help pages, given the prefix "Help:".
     *  @since 0.03
     */
    public static final int HELP_NAMESPACE = 12;
 
    /**
     *  Denotes the namespace for talk pages regarding help pages, given
     *  the prefix "Help talk:".
     *  @since 0.03
     */
    public static final int HELP_TALK_NAMESPACE = 13;
 
    /**
     *  Denotes the namespace for category description pages. Has the
     *  prefix "Category:".
     *  @since 0.03
     */
    public static final int CATEGORY_NAMESPACE = 14;
 
    /**
     *  Denotes the namespace for talk pages regarding categories. Has the
     *  prefix "Category talk:".
     *  @since 0.03
     */
    public static final int CATEGORY_TALK_NAMESPACE = 15;
 
    /**
     *  Denotes all namespaces.
     *  @since 0.03
     */
    public static final int ALL_NAMESPACES = 0x09f91102;
 
    // user rights
 
    /**
     *  Denotes no user rights.
     *  @see User#userRights()
     *  @since 0.05
     */
    public static final int IP_USER = -1;
 
    /**
     *  Denotes a registered account.
     *  @see User#userRights()
     *  @since 0.05
     */
    public static final int REGISTERED_USER = 1;
 
    /**
     *  Denotes a user who has admin rights.
     *  @see User#userRights()
     *  @since 0.05
     */
    public static final int ADMIN = 2;
 
    /**
     *  Denotes a user who has bureaucrat rights.
     *  @see User#userRights()
     *  @since 0.05
     */
    public static final int BUREAUCRAT = 4;
 
    /**
     *  Denotes a user who has steward rights.
     *  @see User#userRights()
     *  @since 0.05
     */
    public static final int STEWARD = 8;
 
    /**
     *  Denotes a user who has a bot flag.
     *  @see User#userRights()
     *  @since 0.05
     */
    public static final int BOT = 16;
 
    // log types
 
    /**
     *  Denotes all logs.
     *  @since 0.06
     */
    public static final String ALL_LOGS = "";
 
    /**
     *  Denotes the user creation log.
     *  @since 0.06
     */
    public static final String USER_CREATION_LOG = "newusers";
 
    /**
     *  Denotes the upload log.
     *  @since 0.06
     */
    public static final String UPLOAD_LOG = "upload";
 
    /**
     *  Denotes the deletion log.
     *  @since 0.06
     */
    public static final String DELETION_LOG = "delete";
 
    /**
     *  Denotes the move log.
     *  @since 0.06
     */
    public static final String MOVE_LOG = "move";
 
    /**
     *  Denotes the block log.
     *  @since 0.06
     */
    public static final String BLOCK_LOG = "block";
 
    /**
     *  Denotes the protection log.
     *  @since 0.06
     */
    public static final String PROTECTION_LOG = "protect";
 
    /**
     *  Denotes the user rights log.
     *  @since 0.06
     */
    public static final String USER_RIGHTS_LOG = "rights";
 
    /**
     *  Denotes the user renaming log.
     *  @since 0.06
     */
    public static final String USER_RENAME_LOG = "renameuser";
 
    /**
     *  Denotes the bot status log.
     *  @since 0.08
     *  @deprecated [[Special:Makebot]] is deprecated, use
     *  <tt>USER_RIGHTS_LOG</tt> instead.
     */
    public static final String BOT_STATUS_LOG = "makebot";
 
    /**
     *  Denotes the page importation log.
     *  @since 0.08
     */
    public static final String IMPORT_LOG = "import";
 
    /**
     *  Denotes the edit patrol log.
     *  @since 0.08
     */
    public static final String PATROL_LOG = "patrol";
 
    // protection levels
 
    /**
     *  Denotes a non-protected page.
     *  @since 0.09
     */
    public static final int NO_PROTECTION = -1;
 
    /**
     *  Denotes semi-protection (i.e. only autoconfirmed users can edit this page)
     *  [edit=autoconfirmed;move=autoconfirmed].
     *  @since 0.09
     */
    public static final int SEMI_PROTECTION = 1;
 
    /**
     *  Denotes full protection (i.e. only admins can edit this page)
     *  [edit=sysop;move=sysop].
     *  @see #ADMIN
     *  @see User#userRights()
     *  @since 0.09
     */
    public static final int FULL_PROTECTION = 2;
 
    /**
     *  Denotes move protection (i.e. only admins can move this page) [move=sysop].
     *  We don't define semi-move protection because only autoconfirmed users
     *  can move pages anyway.
     *
     *  @see #ADMIN
     *  @see User#userRights()
     *  @since 0.09
     */
    public static final int MOVE_PROTECTION = 3;
 
    /**
     *  Denotes move and semi-protection (i.e. autoconfirmed editors can edit the
     *  page, but you need to be a sysop to move) [edit=autoconfirmed;move=sysop].
     *  Naturally, this value (4) is equal to SEMI_PROTECTION (1) +
     *  MOVE_PROTECTION (3).
     *
     *  @see #ADMIN
     *  @see User#userRights()
     *  @since 0.09
     */
    public static final int SEMI_AND_MOVE_PROTECTION = 4;
 
    /**
     *  Denotes protected deleted pages [create=sysop].
     *  @since 0.12
     *  @see #ADMIN
     */
     public static final int PROTECTED_DELETED_PAGE = 5;
 
    // assertion modes
 
    /**
     *  Use no assertions (i.e. 0).
     *  @see #setAssertionMode
     *  @since 0.11
     */
    public static final int ASSERT_NONE = 0;
 
    /**
     *  Assert that we are logged in (i.e. 1).
     *  @see #setAssertionMode
     *  @since 0.11
     */
    public static final int ASSERT_LOGGED_IN = 1;
 
    /**
     *  Assert that we have a bot flag (i.e. 2).
     *  @see #setAssertionMode
     *  @since 0.11
     */
    public static final int ASSERT_BOT = 2;
 
    /**
     *  Assert that we have no new messages. Not defined in Assert Edit, but
     *  some bots have this.
     *  @see #setAssertionMode
     *  @since 0.11
     */
    public static final int ASSERT_NO_MESSAGES = 4;
 
    // the domain of the wiki
    private String domain, query, base;
    private String scriptPath = "/w"; // need this for sites like partyvan.info
 
    // something to handle cookies
    private HashMap cookies = new HashMap(12);
    private HashMap cookies2 = new HashMap(10);
    private User user;
 
    // internal data storage
    private HashMap namespaces = null;
    private int max = 500; // awkward workaround
    private static Logger logger = Logger.getLogger("wiki"); // only one required
    private int throttle = 10000; // throttle
    private int maxlag = 5;
    private volatile long lastlagcheck;
    private int assertion = 0; // assertion mode
 
    // serial version
    private static final long serialVersionUID = -8745212681497644126L;
 
    // constructors and config
 
    /**
     *  Logs which version we're using.
     *  @since 0.12
     */
    static
    {
        logger.logp(Level.CONFIG, "Wiki", "<init>", "Using Wiki.java v0.16.");
    }
 
    /**
     *  Creates a new connection to the English Wikipedia.
     *  @since 0.02
     */
    public Wiki()
    {
        this("");
    }
 
    /**
     *  Creates a new connection to a wiki. WARNING: if the wiki uses a
     *  $wgScriptpath other than the default <tt>/w</tt>, you need to call
     *  <tt>getScriptPath()</tt> to automatically set it. Alternatively, you
     *  can use the constructor below if you know it in advance.
     *
     *  @param domain the wiki domain name e.g. en.wikipedia.org (defaults to
     *  en.wikipedia.org)
     */
    public Wiki(String domain)
    {
        if (domain == null || domain.equals(""))
            domain = "en.wikipedia.org";
        this.domain = domain;
 
        // init variables
        base = "http://" + domain + scriptPath + "/index.php?title=";
        query = "http://" + domain + scriptPath +  "/api.php?format=xml&";
    }
 
    /**
     *  Creates a new connection to a wiki with $wgScriptpath set to
     *  <tt>scriptPath</tt>.
     *
     *  @param domain the wiki domain name
     *  @param scriptPath the script path
     *  @since 0.14
     */
    public Wiki(String domain, String scriptPath)
    {
        this.domain = domain;
        this.scriptPath = scriptPath;
 
        // init variables
        base = "http://" + domain + scriptPath + "/index.php?title=";
        query = "http://" + domain + scriptPath +  "/api.php?format=xml&";
    }
 
    /**
     *  Gets the domain of the wiki, as supplied on construction.
     *  @return the domain of the wiki
     *  @since 0.06
     */
    public String getDomain()
    {
        return domain;
    }
 
    /**
     *  Gets the editing throttle.
     *  @return the throttle value in milliseconds
     *  @see #setThrottle
     *  @since 0.09
     */
    public int getThrottle()
    {
        return throttle;
    }
 
    /**
     *  Sets the editing throttle. Read requests are not throttled or restricted
     *  in any way.
     *  @param throttle the new throttle value in milliseconds
     *  @see #getThrottle
     *  @since 0.09
     */
    public void setThrottle(int throttle)
    {
        this.throttle = throttle;
        log(Level.CONFIG, "Throttle set to " + throttle + " milliseconds", "setThrottle");
    }
 
    /**
     *  Detects the $wgScriptpath wiki variable and sets the bot framework up
     *  to use it. You need not call this if you know the script path is
     *  <tt>/w</tt>. See also [[mw:Manual:$wgScriptpath]].
     *
     *  @throws IOException if a network error occurs
     *  @return the script path, if you have any use for it
     *  @since 0.14
     */
    public String getScriptPath() throws IOException
    {
        scriptPath = parseAndCleanup("{{SCRIPTPATH}}");
        base = "http://" + domain + scriptPath + "/index.php?title=";
        query = "http://" + domain + scriptPath +  "/api.php?format=xml&";
        return scriptPath;
    }
 
    /**
     *  Determines whether this wiki is equal to another object.
     *  @param obj the object to compare
     *  @return whether this wiki is equal to such object
     *  @since 0.10
     */
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Wiki))
            return false;
        return domain.equals(((Wiki)obj).domain);
    }
 
    /**
     *  Returns a hash code of this object.
     *  @return a hash code
     *  @since 0.12
     */
    public int hashCode()
    {
        return domain.hashCode() * maxlag - throttle;
    }
 
    /**
     *   Returns a string representation of this Wiki.
     *   @return a string representation of this Wiki.
     *   @since 0.10
     */
    public String toString()
    {
        try
        {
            // domain
            StringBuilder buffer = new StringBuilder("Wiki[domain=");
            buffer.append(domain);
 
            // user
            buffer.append(",user=");
            if (user != null)
            {
                buffer.append(user.getUsername());
                buffer.append("[rights=");
                buffer.append(user.userRights());
                buffer.append("],");
            }
            else
                buffer.append("null,");
 
            // throttle mechanisms
            buffer.append("throttle=");
            buffer.append(throttle);
            buffer.append(",maxlag=");
            buffer.append(maxlag);
            buffer.append(",assertionMode=");
            buffer.append(assertion);
            buffer.append(",cookies=");
            buffer.append(cookies);
            buffer.append(",cookies2=");
            buffer.append(cookies2);
            return buffer.toString();
        }
        catch (IOException ex)
        {
            // this shouldn't happen due to the user rights cache
            logger.logp(Level.SEVERE, "Wiki", "toString()", "Cannot retrieve user rights!", ex);
            return "";
        }
    }
 
    /**
     *  Gets the maxlag parameter. See [[mw:Manual:Maxlag parameter]], but
     *  this is implemented locally because screen scraping is something that
     *  should be avoided if possible.
     *
     *  @return the current maxlag, in seconds
     *  @see #setMaxLag
     *  @see #getCurrentDatabaseLag
     *  @since 0.11
     */
    public int getMaxLag()
    {
        return maxlag;
    }
 
    /**
     *  Sets the maxlag parameter. See [[mw:Manual:Maxlag parameter]], but
     *  this is implemented locally because screen scraping is something that
     *  should be avoided if possible. A value of less than 1s disables this
     *  mechanism.
     *
     *  @param lag the desired maxlag in seconds
     *  @see #getMaxLag
     *  @see #getCurrentDatabaseLag
     *  @since 0.11
     */
    public void setMaxLag(int lag)
    {
        maxlag = lag;
        log(Level.CONFIG, "Setting maximum allowable database lag to " + lag, "setMaxLag");
    }
 
    /**
     *  Gets the assertion mode. See [[mw:Extension:Assert Edit]] for what
     *  functionality this mimics. Assertion modes are bitmasks.
     *  @return the current assertion mode
     *  @see #setAssertionMode
     *  @since 0.11
     */
    public int getAssertionMode()
    {
        return assertion;
    }
 
    /**
     *  Sets the assertion mode. See [[mw:Extension:Assert Edit]] for what this
     *  functionality this mimics. Assertion modes are bitmasks.
     *  @param an assertion mode
     *  @see #getAssertionMode
     *  @since 0.11
     */
    public void setAssertionMode(int mode)
    {
        assertion = mode;
        log(Level.CONFIG, "Set assertion mode to " + mode, "setAssertionMode");
    }
 
    // meta stuff
 
    /**
     *  Logs in to the wiki. This method is thread-safe. If the specified
     *  username or password is incorrect, the thread blocks for 60 seconds
     *  then throws an exception.
     *
     *  @param username a username
     *  @param password a password (as a char[] due to JPasswordField)
     *  @throws FailedLoginException if the login failed due to incorrect
     *  username and/or password
     *  @throws IOException if a network error occurs
     *  @see #logout
     */
    public synchronized void login(String username, char[] password) throws IOException, FailedLoginException
    {
        // @revised 0.11 to remove screen scraping
 
        // sanitize
        String ps = new String(password);
        username = URLEncoder.encode(username, "UTF-8");
        ps = URLEncoder.encode(ps, "UTF-8");
 
        // start
        String url = query + "action=login";
        URLConnection connection = new URL(url).openConnection();
        logurl(url, "login");
        setCookies(connection, cookies);
        connection.setDoOutput(true);
        connection.connect();
 
        // send
        PrintWriter out = new PrintWriter(connection.getOutputStream());
        out.print("lgname=");
        out.print(username);
        out.print("&lgpassword=");
        out.print(password);
        out.close();
 
        // get the cookies
        grabCookies(connection, cookies);
 
        // determine success
        BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
        boolean success = in.readLine().indexOf("result=\"Success\"") != -1;
        in.close();
        if (success)
        {
            user = new User(username);
            boolean apihighlimit = (user.userRights() & BOT) == BOT || (user.userRights() & ADMIN) == ADMIN;
            max = apihighlimit ? 5000 : 500;
            log(Level.INFO, "Successfully logged in as " + username + ", highLimit = " + apihighlimit, "login");
        }
        else
        {
            log(Level.WARNING, "Failed to log in as " + username, "login");
            try
            {
                Thread.sleep(60000); // to prevent brute force
            }
            catch (InterruptedException e)
            {
                // nobody cares
            }
            throw new FailedLoginException("Login failed.");
        }
    }
 
    /**
     *  Logs out of the wiki. This method is thread safe (so that we don't log
     *  out during an edit). All operations are conducted offline, so you can
     *  serialize this Wiki first.
     *  @see #login
     *  @see #logoutServerSide
     */
    public synchronized void logout()
    {
        cookies.clear();
        cookies2.clear();
        user = null;
        max = 500;
        log(Level.INFO, "Logged out", "logout");
    }
 
    /**
     *  Logs out of the wiki and destroys the session on the server. You will
     *  need to log in again instead of just reading in a serialized wiki.
     *  Equivalent to [[Special:Userlogout]]. This method is thread safe
     *  (so that we don't log out during an edit).
     *
     *  @throws IOException if a network error occurs
     *  @since 0.14
     *  @see #login
     *  @see #logout
     */
    public synchronized void logoutServerSide() throws IOException
    {
        // send the request
        String url = query + "action=logout";
        logurl(url, "logoutServerSide");
        URLConnection connection = new URL(url).openConnection();
        setCookies(connection, cookies);
        connection.connect();
 
        // destroy local cookies
        logout();
    }
 
    /**
     *  Determines whether the current user has new messages. (A human would
     *  notice a yellow bar at the top of the page).
     *  @return whether the user has new messages
     *  @throws IOException if a network error occurs
     *  @since 0.11
     */
    public boolean hasNewMessages() throws IOException
    {
        String url = query + "action=query&meta=userinfo&uiprop=hasmsg";
        logurl(url, "hasNewMessages");
        URLConnection connection = new URL(url).openConnection();
        setCookies(connection, cookies);
        connection.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
        return in.readLine().indexOf("messages=\"\"") != -1;
    }
 
    /**
     *  Determines the current database replication lag.
     *  @return the current database replication lag
     *  @throws IOException if a network error occurs
     *  @see #setMaxLag
     *  @see #getMaxLag
     *  @since 0.10
     */
    public int getCurrentDatabaseLag() throws IOException
    {
        String url = query + "action=query&meta=siteinfo&siprop=dbrepllag";
        logurl(url, "getCurrentDatabaseLag");
        URLConnection connection = new URL(url).openConnection();
        setCookies(connection, cookies);
        connection.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
        String line = in.readLine();
        int z = line.indexOf("lag=\"") + 5;
        String lag = line.substring(z, line.indexOf("\" />", z));
        log(Level.INFO, "Current database replication lag is " + lag + " seconds", "getCurrentDatabaseLag");
        return Integer.parseInt(lag);
    }
 
    /**
     *  Fetches some site statistics, namely the number of articles, pages,
     *  files, edits, users and admins. Equivalent to [[Special:Statistics]].
     *
     *  @return a map containing the stats. Use "articles", "pages", "files"
     *  "edits", "users" or "admins" to retrieve the respective value
     *  @throws IOException if a network error occurs
     *  @since 0.14
     */
    public HashMap<String, Integer> getSiteStatistics() throws IOException
    {
        // ZOMG hack to avoid excessive substring code
        String text = parseAndCleanup("{{NUMBEROFARTICLES:R}} {{NUMBEROFPAGES:R}} {{NUMBEROFFILES:R}} {{NUMBEROFEDITS:R}} " +
                "{{NUMBEROFUSERS:R}} {{NUMBEROFADMINS:R}}");
        String[] values = text.split("\\s");
        HashMap<String, Integer> ret = new HashMap<String, Integer>();
        String[] keys =
        {
           "articles", "pages", "files", "edits", "users", "admins"
        };
        for (int i = 0; i < values.length; i++)
        {
            Integer value = new Integer(values[i]);
            ret.put(keys[i], value);
        }
        return ret;
    }
 
    /**
     *  Gets the version of MediaWiki this wiki runs e.g. 1.13 alpha (r31567).
     *  The r number corresponds to a revision in MediaWiki subversion
     *  (http://svn.wikimedia.org/viewvc/mediawiki/).
     *  @return the version of MediaWiki used
     *  @throws IOException if a network error occurs
     *  @since 0.14
     */
    public String version() throws IOException
    {
        return parseAndCleanup("{{CURRENTVERSION}}"); // ahh, the magicness of magic words
    }
 
    /**
     *  Renders the specified wiki markup by passing it to the MediaWiki
     *  parser through the API. (Note: this isn't implemented locally because
     *  I can't be stuffed porting Parser.php). One use of this method is to
     *  emulate the previewing functionality of the MediaWiki software.
     *
     *  @param markup the markup to parse
     *  @return the parsed markup as HTML
     *  @throws IOException if a network error occurs
     *  @since 0.13
     */
    public String parse(String markup) throws IOException
    {
        // start
        String url = query + "action=parse";
        URLConnection connection = new URL(url).openConnection();
        logurl(url, "parse");
        setCookies(connection, cookies);
        connection.setDoOutput(true);
        connection.connect();
 
        // send
        PrintWriter out = new PrintWriter(connection.getOutputStream());
        out.print("prop=text&text=");
        out.print(URLEncoder.encode(markup, "UTF-8"));
        out.close();
 
        // parse
        BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
        String line;
        StringBuilder text = new StringBuilder(100000);
        while ((line = in.readLine()) != null)
        {
            int y = line.indexOf("<text>");
            int z = line.indexOf("</text>");
            if (y != -1)
            {
                text.append(line.substring(y + 6));
                text.append("\n");
            }
            else if (z != -1)
            {
                text.append(line.substring(0, z));
                text.append("\n");
                break; // done
            }
            else
            {
                text.append(line);
                text.append("\n");
            }
        }
        return decode(text.toString());
    }
 
    /**
     *  Fetches a random page in the main namespace. Equivalent to
     *  [[Special:Random]].
     *  @return the title of the page
     *  @throws IOException if a network error occurs
     *  @since 0.13
     */
    public String random() throws IOException
    {
        return random(MAIN_NAMESPACE);
    }
 
    /**
     *  Fetches a random page in the specified namespace. Equivalent to
     *  [[Special:Random]].
     *
     *  @param namespace a namespace
     *  @return the title of the page
     *  @throws IOException if a network error occurs
     *  @since 0.13
     */
    public String random(int namespace) throws IOException
    {
        // fetch
        String url = query + "action=query&list=random";
                url += (namespace == ALL_NAMESPACES ? "" : "&rnnamespace=" + namespace);
                URLConnection connection = new URL(url).openConnection();
        setCookies(connection, cookies);
        connection.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
        String line = in.readLine();
 
        // parse
        int a = line.indexOf("title=\"") + 7;
        int b = line.indexOf("\"", a);
        return line.substring(a, b);
    }
 
    // static members
 
    /**
     *   Parses a list of links into its individual elements. Such a list
     *   should be in the form:
     *
     *  <pre>
     *  * [[Main Page]]
     *  * [[Wikipedia:Featured picture candidates]]
     *  * [[:Image:Example.png]]
     *  </pre>
     *
     *  in which case <tt>{ "Main Page", "Wikipedia:Featured picture
     *  candidates", "Image:Example.png" }</tt> is the return value.
     *
     *  @param list a list of pages
     *  @see #formatList
     *  @return an array of the page titles
     *  @since 0.11
     */
    public static String[] parseList(String list)
    {
        StringTokenizer tokenizer = new StringTokenizer(list, "[]");
        ArrayList<String> titles = new ArrayList<String>(667);
        tokenizer.nextToken(); // skip the first token
        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();
 
            // skip any containing new lines or double letters
            if (token.indexOf('\n') != -1)
                continue;
            if (token.equals(""))
                continue;
 
            // trim the starting colon, if present
            if (token.startsWith(":"))
                token = token.substring(1);
 
            titles.add(token);
        }
        return titles.toArray(new String[0]);
    }
 
    /**
     *  Formats a list of pages, say, generated from one of the query methods
     *  into something that would be editor-friendly. Does the exact opposite
     *  of <tt>parseList()</tt>, i.e. { "Main Page", "Wikipedia:Featured
     *  picture candidates", "Image:Example.png" } becomes the string:
     *
     *  <pre>
     *  *[[:Main Page]]
     *  *[[:Wikipedia:Featured picture candidates]]
     *  *[[:Image:Example.png]]
     *  </pre>
     *
     *  @param pages an array of page titles
     *  @return see above
     *  @see #parseList
     *  @since 0.14
     */
    public static String formatList(String[] pages)
    {
        StringBuilder buffer = new StringBuilder(10000);
        for (int i = 0; i < pages.length; i++)
        {
            buffer.append("*[[:");
            buffer.append(pages[i]);
            buffer.append("]]\n");
        }
        return buffer.toString();
    }
 
    /**
     *  Determines the intersection of two lists of pages a and b, i.e. a ∩ b.
     *  Such lists might be generated from the various list methods below.
     *  Examples from the English Wikipedia:
     *
     *  <pre>
     *  // find all orphaned and unwikified articles
     *  String[] articles = Wiki.intersection(wikipedia.getCategoryMembers("All orphaned articles", Wiki.MAIN_NAMESPACE),
     *      wikipedia.getCategoryMembers("All pages needing to be wikified", Wiki.MAIN_NAMESPACE));
     *
     *  // find all (notable) living people who are related to Barack Obama
     *  String[] people = Wiki.intersection(wikipedia.getCategoryMembers("Living people", Wiki.MAIN_NAMESPACE),
     *      wikipedia.whatLinksHere("Barack Obama", Wiki.MAIN_NAMESPACE));
     *  </pre>
     *
     *  @param a a list of pages
     *  @param b another list of pages
     *  @return a ∩ b (as String[])
     *  @since 0.04
     */
    public static String[] intersection(String[] a, String[] b)
    {
        // @revised 0.11 to take advantage of Collection.retainAll()
        // @revised 0.14 genericised to all page titles, not just category members
 
        List<String> aa = Arrays.asList(a);
        aa.retainAll(Arrays.asList(b));
        return aa.toArray(new String[0]);
    }
 
    /**
     *  Determines the list of articles that are in a but not b, i.e. a \ b.
     *  This is not the same as b \ a. Such lists might be generated from the
     *  various lists below. Some examples from the English Wikipedia:
     *
     *  <pre>
     *  // find all Martian crater articles that do not have an infobox
     *  String[] articles = Wiki.relativeComplement(wikipedia.getCategoryMembers("Craters on Mars"),
     *      wikipedia.whatTranscludesHere("Template:MarsGeo-Crater", Wiki.MAIN_NAMESPACE));
     *
     *  // find all images without a description that haven't been tagged "no license"
     *  String[] images = Wiki.relativeComplement(wikipedia.getCategoryMembers("Images lacking a description"),
     *      wikipedia.getCategoryMembers("All images with unknown copyright status"));
     *  </pre>
     *
     *  @param a a list of pages
     *  @param b another list of pages
     *  @return a \ b
     *  @since 0.14
     */
    public static String[] relativeComplement(String[] a, String[] b)
    {
        List<String> aa = Arrays.asList(a);
        aa.removeAll(Arrays.asList(b));
        return aa.toArray(new String[0]);
    }
 
    // page methods
 
    /**
     *  Returns the corresponding talk page to this page.
     *  @param title the page title
     *  @return the name of the talk page corresponding to <tt>title</tt>
     *  or "" if we cannot recognise it
     *  @throws IllegalArgumentException if given title is in a talk namespace
     *  or we try to retrieve the talk page of a Special: or Media: page.
     *  @throws IOException if a network error occurs
     *  @since 0.10
     */
    public String getTalkPage(String title) throws IOException
    {
        int namespace = namespace(title);
        if (namespace % 2 == 1)
            throw new IllegalArgumentException("Cannot fetch talk page of a talk page!");
        if (namespace < 0)
            throw new IllegalArgumentException("Special: and Media: pages do not have talk pages!");
        if (namespace != MAIN_NAMESPACE) // remove the namespace
            title = title.substring(title.indexOf(':') + 1, title.length());
 
        // you should override this to add wiki-specific namespaces e.g.
        // Portal on en.wikipedia.org or UnNews: on Uncyclopedia.
        switch(namespace)
        {
            case MAIN_NAMESPACE:
                return "Talk:" + title;
            case USER_NAMESPACE:
                return "User talk:" + title;
            case PROJECT_NAMESPACE:
                return "Project talk:" + title;
            case TEMPLATE_NAMESPACE:
                return "Template talk:" + title;
            case CATEGORY_NAMESPACE:
                return "Category talk:" + title;
            case MEDIAWIKI_NAMESPACE:
                return "MediaWiki talk:" + title;
            case HELP_NAMESPACE:
                return "Help talk:" + title;
            case IMAGE_NAMESPACE:
                return "Image talk:" + title;
        }
        return "";
    }
 
    /**
     *  Gets the protection status of a page. WARNING: returns NO_PROTECTION
     *  for pages that are protected through the cascading mechanism, e.g.
     *  [[Talk:W/w/index.php]].
     *
     *  @param title the title of the page
     *  @return one of the various protection levels (i.e,. NO_PROTECTION,
     *  SEMI_PROTECTION, MOVE_PROTECTION, FULL_PROTECTION,
     *  SEMI_AND_MOVE_PROTECTION, PROTECTED_DELETED_PAGE)
     *  @throws IOException if a network error occurs
     *  @since 0.10
     */
    public int getProtectionLevel(String title) throws IOException
    {
        // fetch
        String url = query + "action=query&prop=info&inprop=protection&titles=" + URLEncoder.encode(title, "UTF-8");
        logurl(url, "getProtectionLevel");
        checkLag("getProtectionLevel");
        URLConnection connection = new URL(url).openConnection();
        setCookies(connection, cookies);
        connection.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
        String line = in.readLine();
 
        // parse
        int z = line.indexOf("type=\"edit\"");
        if (z != -1)
        {
            String s = line.substring(z, z + 30);
            if (s.indexOf("sysop") != -1)
                return FULL_PROTECTION;
            s = line.substring(z + 30, line.length()); // cut out edit tag
            if (line.indexOf("level=\"sysop\"") != -1)
                return SEMI_AND_MOVE_PROTECTION;
            return SEMI_PROTECTION;
        }
        if (line.indexOf("type=\"move\"") != -1)
            return MOVE_PROTECTION;
        if (line.indexOf("type=\"create\"") != -1)
            return PROTECTED_DELETED_PAGE;
        return NO_PROTECTION;
    }
 
    /**
     *  Returns the namespace a page is in.
     *  @param title the title of the page
     *  @return one of namespace types above, or a number for custom
     *  namespaces or ALL_NAMESPACES if we can't make sense of it
     *  @throws IOException if a network error occurs
     *  @since 0.03
     */
    public int namespace(String title) throws IOException
    {
        // sanitise
        title = title.replace('_', ' ');
        if (title.indexOf(':') == -1)
            return MAIN_NAMESPACE;
        String namespace = title.substring(0, title.indexOf(':'));
 
        // all wiki namespace test
        if (namespace.equals("Project talk"))
            return PROJECT_TALK_NAMESPACE;
        if (namespace.equals("Project"))
            return PROJECT_NAMESPACE;
 
        if (namespaces == null) // cache this, as it will be called often
        {
            URLConnection connection = new URL(query + "action=query&meta=siteinfo&siprop=namespaces").openConnection();
            logurl(query + "action=query&meta=siteinfo&siprop=namespaces", "namespace");
            checkLag("namespace");
            setCookies(connection, cookies);
            connection.connect();
 
            // read the first line, as it is the only thing worth paying attention to
            BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
            String line = in.readLine();
 
            namespaces = new HashMap(30);
            while (line.indexOf("<ns") != -1)
            {
                int x = line.indexOf("<ns id=");
                if (line.charAt(x + 8) == '0') // skip main, it's a little different
                {
                    line = line.substring(13, line.length());
                    continue;
                }
                int y = line.indexOf("</ns>");
                String working = line.substring(x + 8, y);
                int ns = Integer.parseInt(working.substring(0, working.indexOf('"')));
                String name = working.substring(working.indexOf(">") + 1, working.length());
                namespaces.put(name, new Integer(ns));
                line = line.substring(y + 5, line.length());
            }
            in.close();
            log(Level.INFO, "Successfully retrieved namespace list (" + (namespaces.size() + 1) + " namespaces)", "namespace");
        }
 
        if (!namespaces.containsKey(namespace))
            return MAIN_NAMESPACE; // For titles like UN:NRV
        Iterator i = namespaces.entrySet().iterator();
        while (i.hasNext())
        {
            Map.Entry entry = (Map.Entry)i.next();
            if (entry.getKey().equals(namespace))
                return ((Integer)entry.getValue()).intValue();
        }
        return ALL_NAMESPACES; // unintelligble title
    }
 
    /**
     *  Determines whether a series of pages exist. Requires the
     *  [[mw:Extension:ParserFunctions|ParserFunctions extension]].
     *
     *  @param titles the titles to check
     *  @return whether the pages exist
     *  @throws IOException if a network error occurs
     *  @since 0.10
     */
    public boolean[] exists(String... titles) throws IOException
    {
        // @revised 0.15 optimized for multiple queries, now up to 500x faster!
 
        StringBuilder wikitext = new StringBuilder(15000);
        StringBuilder parsed = new StringBuilder(1000);
        for (int i = 0; i < titles.length; i++)
        {
            // build up the parser string
            wikitext.append("{{#ifexist:");
            wikitext.append(titles[i]);
            wikitext.append("|1|0}}"); // yay! binary! (well, almost)
 
            // Send them off in batches of 500. Change this if your expensive
            // parser function limit is different.
            if (i % 500 == 499 || i == titles.length - 1)
            {
                parsed.append(parseAndCleanup(wikitext.toString()));
                wikitext = new StringBuilder(15000);
            }
        }
 
        // now parse the resulting "binary"
        char[] characters = parsed.toString().toCharArray();
        boolean[] ret = new boolean[characters.length];
        for (int i = 0; i < characters.length; i++)
        {
            // we would want to use the ternary operator here but other things can go wrong
            if (characters[i] != '1' && characters[i] != '0')
                throw new UnknownError("Unable to parse output. Perhaps the ParserFunctions extension is not installed, or this is a bug.");
            ret[i] = (characters[i] == '1') ? true : false;
        }
        return ret;
    }
 
    /**
     *  Gets the raw wikicode for a page. WARNING: does not support special
     *  pages. Check [[User talk:MER-C/Wiki.java#Special page equivalents]]
     *  for fetching the contents of special pages. Use <tt>getImage()</tt> to
     *  fetch an image.
     *
     *  @param title the title of the page.
     *  @return the raw wikicode of a page. If the page is non-existent,
     *  (or blanked) this returns "".
     *  @throws UnsupportedOperationException if you try to retrieve the text of a
     *  Special: page or a Media: page
     *  @throws IOException if a network error occurs
     *  @see #edit
     */
    public String getPageText(String title) throws IOException
    {
        // pitfall check
        if (namespace(title) < 0)
            throw new UnsupportedOperationException("Cannot retrieve Special: or Media: pages!");
 
        // sanitise the title
        title = URLEncoder.encode(title, "UTF-8");
 
        // go for it
        String URL = base + title + "&action=raw";
        logurl(URL, "getPageText");
        checkLag("getPageText");
        URLConnection connection = new URL(URL).openConnection();
        setCookies(connection, cookies);
        connection.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
 
        // get the text
        String line;
        StringBuilder text = new StringBuilder(100000);
        while ((line = in.readLine()) != null)
        {
            text.append(line);
            text.append("\n");
        }
 
        in.close();
        log(Level.INFO, "Successfully retrieved text of " + title, "getPageText");
        return decode(text.toString());
    }
 
    /**
     *  Gets the contents of a page, rendered in HTML (as opposed to
     *  wikitext). WARNING: only supports special pages in certain
     *  circumstances, for example <tt>getRenderedText("Special:Recentchanges")
     *  </tt> returns the 50 most recent change to the wiki in pretty-print
     *  HTML. You should test any use of this method on-wiki through the text
     *  <tt>{{Special:Specialpage}}</tt>. Use <tt>getImage()</tt> to fetch an
     *  image. Be aware of any transclusion limits, as outlined at
     *  [[Wikipedia:Template limits]].
     *
     *  @param title the title of the page
     *  @return the rendered contents of that page
     *  @throws IOException if a network error occurs
     *  @since 0.10
     */
    public String getRenderedText(String title) throws IOException
    {
        // @revised 0.13 genericised to parse any wikitext
        return parse("{{" + title + "}}");
    }
 
    /**
     *  Edits a page by setting its text to the supplied value. This method is
     *  thread safe and blocks for a minimum time as specified by the
     *  throttle.
     *
     *  @param text the text of the page
     *  @param title the title of the page
     *  @param summary the edit summary. See [[Help:Edit summary]]. Summaries
     *  longer than 200 characters are truncated server-side.
     *  @param minor whether the edit should be marked as minor. See
     *  [[Help:Minor edit]].
     *  @throws IOException if a network error occurs
     *  @throws AccountLockedException if user is blocked
     *  @throws CredentialException if page is protected and we can't edit it
     *  @throws UnsupportedOperationException if you try to retrieve the text
     *  of a Special: page or a Media: page
     *  @see #getPageText
     */
    public synchronized void edit(String title, String text, String summary, boolean minor) throws IOException, LoginException
    {
        // throttle
        long start = System.currentTimeMillis();
 
        // pitfall check
        if (namespace(title) < 0)
            throw new UnsupportedOperationException("Cannot edit Special: or Media: pages!");
 
        // check if the page is protected, and if we can edit it
        if (!checkRights(getProtectionLevel(title), false))
        {
            CredentialException ex = new CredentialException("Permission denied: page is protected.");
            logger.logp(Level.WARNING, "Wiki", "edit()", "[" + domain + "] Cannot edit - permission denied.", ex);
            throw ex;
        }
 
        // sanitise
        title = URLEncoder.encode(title, "UTF-8");
        summary = URLEncoder.encode(summary, "UTF-8");
        text = URLEncoder.encode(text, "UTF-8");
 
        // What we need to do is get the edit page and fish out the wpEditToken, wpStartTime and
        // wpEditTime values. See [[mw:Manual:Parameters to index.php]]
        String URL = base + title + "&action=edit";
        logurl(URL, "edit"); // no need to check lag, have already done so in getProtectionLevel()
        URLConnection connection = new URL(URL).openConnection();
        setCookies(connection, cookies);
        connection.connect();
        grabCookies(connection, cookies2);
        cookies2.putAll(cookies);
        BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
 
        // more specifically, we're looking for "name="wpEditToken"", and related
        String line, wpEditToken = "", wpEdittime = "";
        boolean editRetrieved = false, timeRetrieved = false, watchRetrieved = false;
        boolean watched = false;
        while ((line = in.readLine()) != null)
        {
            if (line.indexOf("name=\"wpEditToken\"") != -1)
            {
                int x = line.indexOf("value=\"") + 7;
                wpEditToken = URLEncoder.encode(line.substring(x, line.indexOf('\"', x)), "UTF-8");
                editRetrieved = true;
            }
            else if (line.indexOf("name=\"wpEdittime\"") != -1)
            {
                int x = line.indexOf("value=\"") + 7;
                wpEdittime = line.substring(x, line.indexOf('\"', x));
                timeRetrieved = true;
            }
            else if (line.indexOf("name=\"wpWatchthis\"") != -1)
            {
                watched = (line.indexOf("checked=\"") != -1);
                watchRetrieved = true;
            }
            else if (editRetrieved && timeRetrieved && watchRetrieved)
            {
                in.close();
                break; // bandwidth hack
            }
        }
 
        // this is what accepts the text
        URL = base + title + "&action=submit";
        logurl(URL, "edit");
        connection = new URL(URL).openConnection();
        setCookies(connection, cookies2);
        connection.setDoOutput(true);
        PrintWriter out = new PrintWriter(connection.getOutputStream());
 
        // now we send the data
        out.print("wpTextbox1=");
        out.print(text);
        out.print("&wpSummary=");
        out.print(summary);
        if (minor)
            out.print("&wpMinoredit=1");
        if (watched)
            out.print("&wpWatchthis=1");
        out.print("&wpEdittime=");
        out.print(wpEdittime);
        out.print("&wpEditToken=");
        out.print(wpEditToken);
        // note that we can compute wpStartTime offline
        out.print("&wpStarttime=");
        String wpStarttime = calendarToTimestamp(new GregorianCalendar(TimeZone.getTimeZone("UTC")));
        out.print(wpStarttime);
 
        //done
        out.close();
        try
        {
            // it's somewhat strange that the edit only sticks when you start reading the response...
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            in.readLine();
 
            // debug version
//            while ((line = in.readLine()) != null)
//                 System.out.println(line);
        }
        catch (IOException e)
        {
//            InputStream err = ((HttpURLConnection)connection).getInputStream();
//            if (err == null)
//                throw e;
//            in = new BufferedReader(new InputStreamReader(err));
            logger.logp(Level.SEVERE, "Wiki", "edit()", "[" + domain + "] EXCEPTION:  ", e);
        }
        in.close();
        log(Level.INFO, "Successfully edited " + title, "edit");
 
         // throttle
        try
        {
            long z = throttle - System.currentTimeMillis() + start;
            if (z > 0)
                Thread.sleep(z);
        }
        catch (InterruptedException e)
        {
            // nobody cares
        }
    }
 
    /**
     *  New edit(), fetching edit token from the API, and using API edit (not yet
     *  enabled on Wikimedia). See above for docs.
     *  @since 0.16
     */
    public synchronized void apiEdit(String title, String text, String summary, boolean minor) throws IOException, LoginException
    {
        // @revised 0.16 to use API edit. No more screenscraping - yay!
        long start = System.currentTimeMillis();
 
        // sanitize some params
        title = URLEncoder.encode(title, "UTF-8");
 
        // Check the protection level. We don't use getProtectionLevel(title), as we
        // can fetch a move token at the same time!
        String url = query + "action=query&prop=info&inprop=protection&intoken=edit&titles=" + title;
        logurl(url, "edit");
        checkLag("edit");
        URLConnection connection = new URL(url).openConnection();
        setCookies(connection, cookies);
        connection.connect();
        // cookie fiddling
        grabCookies(connection, cookies2);
        cookies2.putAll(cookies);
        BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
        String line = in.readLine();
 
        // parse the page
        int level = NO_PROTECTION;
        int z = line.indexOf("type=\"edit\"");
        if (z != -1)
        {
            String s = line.substring(z, z + 30);
            if (s.indexOf("sysop") != -1)
                level = FULL_PROTECTION;
            s = line.substring(z + 30, line.length()); // cut out edit tag
            if (line.indexOf("level=\"sysop\"") != -1)
                level = SEMI_AND_MOVE_PROTECTION;
            level = SEMI_PROTECTION;
        }
        else if (line.indexOf("type=\"create\"") != -1)
            level = PROTECTED_DELETED_PAGE;
 
        // do the check
        if (!checkRights(level, false))
        {
            CredentialException ex = new CredentialException("Permission denied: page is protected.");
            logger.logp(Level.WARNING, "Wiki", "edit()", "[" + getDomain() + "] Cannot edit - permission denied.", ex);
            throw ex;
        }
 
        // find the move token
        int a = line.indexOf("token=\"") + 7;
        int b = line.indexOf("\"", a);
        String wpEditToken = line.substring(a, b);
 
        // fetch the appropriate URL
        url = query + "action=edit";
        logurl(url, "edit");
        connection = new URL(url).openConnection();
        setCookies(connection, cookies2);
        connection.setDoOutput(true);
        connection.connect();
 
        // send the data
        PrintWriter out = new PrintWriter(connection.getOutputStream());
        // PrintWriter out = new PrintWriter(System.out); // debug version
        out.write("title=");
        out.write(title);
        out.write("&text=");
        out.write(URLEncoder.encode(text, "UTF-8"));
        out.write("&summary=");
        out.write(URLEncoder.encode(summary, "UTF-8"));
        out.write("&token=");
        out.write(URLEncoder.encode(wpEditToken, "UTF-8"));
        if (minor)
            out.write("&minor=1");
        out.close();
 
        // done
        try
        {
            // it's somewhat strange that the edit only sticks when you start reading the response...
            in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
            in.readLine();
 
            // debug version
//                    String line2;
//            while ((line2 = in.readLine()) != null)
//                 System.out.println(line2);
        }
        catch (IOException e)
        {
            InputStream err = ((HttpURLConnection)connection).getInputStream();
            if (err == null)
                throw e;
            in = new BufferedReader(new InputStreamReader(err));
            logger.logp(Level.SEVERE, "Wiki", "edit()", "[" + domain + "] EXCEPTION:  ", e);
        }
        in.close();
        log(Level.INFO, "Successfully edited " + title, "edit");
 
        // throttle
        try
        {
            long time = throttle - System.currentTimeMillis() + start;
            if (time > 0)
                Thread.sleep(time);
        }
        catch (InterruptedException e)
        {
            // nobody cares
        }
    }
 
    /**
     *  Prepends something to the given page. A convenience method for
     *  adding maintainance templates, rather than getting and setting the
     *  page yourself. Edit summary is automatic, being "+whatever".
     *
     *  @param title the title of the page
     *  @param stuff what to prepend to the page
     *  @param minor whether the edit is minor
     *  @throws AccountLockedException if user is blocked
     *  @throws CredentialException if page is protected and we can't edit it
     *  @throws UnsupportedOperationException if you try to retrieve the text
     *  of a Special: page or a Media: page
     *  @throws IOException if a network error occurs
     */
    public void prepend(String title, String stuff, boolean minor) throws IOException, LoginException
    {
        StringBuilder text = new StringBuilder(100000);
        text.append(stuff);
        text.append(getPageText(title));
        edit(title, text.toString(), "+" + stuff, minor);
    }
 
    /**
     *  Gets the list of images used on a particular page. Capped at
     *  <tt>max</tt> number of images, there's no reason why there should be
     *  more than that.
     *
     *  @param title a page
     *  @return the list of images used in the page
     *  @throws IOException if a network error occurs
     *  @since 0.16
     */
    public String[] getImagesOnPage(String title) throws IOException
    {
        String url = query + "action=query&prop=images&imlimit=" + max + "&titles=" + URLEncoder.encode(title, "UTF-8");
        logurl(url, "getImagesOnPage");
        URLConnection connection = new URL(url).openConnection();
        setCookies(connection, cookies);
        checkLag("getImagesOnPage");
        connection.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
        String line = in.readLine();
 
        // parse the list
        // typical form: <im ns="6" title="Image:Example.jpg" />
        ArrayList<String> images = new ArrayList<String>(750);
        while (line.indexOf("title=\"") != -1)
        {
            int a = line.indexOf("title=\"Image:") + 7;
            int b = line.indexOf("\"", a);
            images.add(line.substring(a, b));
            line = line.substring(b);
        }
        log(Level.INFO, "Successfully retrieved images used on " + title, "getImagesOnPage");
        return images.toArray(new String[0]);
    }
 
    /**
     *  Gets the list of categories a particular page is in. Includes hidden
     *  categories. Capped at <tt>max</tt> number of categories, there's no
     *  reason why there should be more than that.
     *
     *  @param title a page
     *  @return the list of categories that page is in
     *  @throws IOException if a network error occurs
     *  @since 0.16
     */
    public String[] getCategories(String title) throws IOException
    {
        String url = query + "action=query&prop=categories&cllimit=" + max + "&titles=" + URLEncoder.encode(title, "UTF-8");
        logurl(url, "getCategories");
        URLConnection connection = new URL(url).openConnection();
        setCookies(connection, cookies);
        checkLag("getCategories");
        connection.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
        String line = in.readLine();
 
        // parse the list
        // typical form: <cl ns="14" title="Category:1879 births" />
        ArrayList<String> images = new ArrayList<String>(750);
        while (line.indexOf("title=\"") != -1)
        {
            int a = line.indexOf("title=\"Category:") + 7;
            int b = line.indexOf("\"", a);
            images.add(line.substring(a, b));
            line = line.substring(b);
        }
        log(Level.INFO, "Successfully retrieved categories used on " + title, "getCategories");
        return images.toArray(new String[0]);
    }
 
    /**
     *  Gets the list of templates used on a particular page. Capped at
     *  <tt>max</tt> number of templates, there's no reason why there should
     *  be more than that.
     *
     *  @param title a page
     *  @return the list of templates used on that page
     *  @throws IOException if a network error occurs
     *  @since 0.16
     */
    public String[] getTemplates(String title) throws IOException
    {
        return getTemplates(title, ALL_NAMESPACES);
    }
 
    /**
     *  Gets the list of templates used on a particular page that are in a
     *  particular namespace. Capped at <tt>max</tt> number of templates,
     *  there's no reason why there should be more than that.
     *
     *  @param title a page
     *  @param namespace a namespace
     *  @return the list of templates used on that page in that namespace
     *  @throws IOException if a network error occurs
     *  @since 0.16
     */
    public String[] getTemplates(String title, int namespace) throws IOException
    {
        String url = query + "action=query&prop=templates&tllimit=" + max + "&titles=" + URLEncoder.encode(title, "UTF-8");
        if (namespace != ALL_NAMESPACES)
            url += ("&tlnamespace=" + namespace);
        logurl(url, "getTemplates");
        URLConnection connection = new URL(url).openConnection();
        setCookies(connection, cookies);
        checkLag("getTemplates");
        connection.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
        String line = in.readLine();
 
        // parse the list
        // typical form: <tl ns="10" title="Template:POTD" />
        ArrayList<String> images = new ArrayList<String>(750);
        line = line.substring(line.indexOf("<templates>")); // drop off the first title, which is <tt>title</tt>
        while (line.indexOf("title=\"") != -1)
        {
            int a = line.indexOf("title=\"") + 7;
            int b = line.indexOf("\"", a);
            images.add(line.substring(a, b));
            line = line.substring(b);
        }
        log(Level.INFO, "Successfully retrieved templates used on " + title, "getTemplates");
        return images.toArray(new String[0]);
    }
 
 
    /**
     *  Moves a page. Moves the associated talk page and leaves redirects, if
     *  applicable. (Uses API edit, not yet enabled on Wikimedia.) Equivalent
     *  to [[Special:MovePage]]. This method is thread safe and is subject to
     *  the throttle.
     *
     *  @param title the title of the page to move
     *  @param newTitle the new title of the page
     *  @param reason a reason for the move
     *  @throws UnsupportedOperationException if the original page is in the
     *  Category or Image namespace. MediaWiki does not support moving of
     *  these pages.
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if not logged in
     *  @throws CredentialException if page is protected and we can't move it
     *  @since 0.16
     */
    public synchronized void move(String title, String newTitle, String reason) throws IOException, LoginException
    {
        move(title, newTitle, reason, false, true);
    }
 
    /**
     *  Moves a page. (Uses API edit, not yet enabled on Wikimedia.) Equivalent
     *  to [[Special:MovePage]]. This method is thread safe and is subject to
     *  the throttle.
     *
     *  @param title the title of the page to move
     *  @param newTitle the new title of the page
     *  @param reason a reason for the move
     *  @param noredirect don't leave a redirect behind. You need to be a
     *  admin to do this, otherwise this option is ignored.
     *  @param movetalk move the talk page as well (if applicable)
     *  @throws UnsupportedOperationException if the original page is in the
     *  Category or Image namespace. MediaWiki does not support moving of
     *  these pages.
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if not logged in
     *  @throws CredentialException if page is protected and we can't move it
     *  @since 0.16
     */
    public synchronized void move(String title, String newTitle, String reason, boolean noredirect, boolean movetalk) throws IOException, LoginException
    {
        long start = System.currentTimeMillis();
 
        // check for log in
        if (user == null)
        {
            CredentialNotFoundException ex = new CredentialNotFoundException("Permission denied: you need to be autoconfirmed to move pages.");
            logger.logp(Level.SEVERE, "Wiki", "move()", "[" + domain + "] Cannot move - permission denied.", ex);
            throw ex;
        }
 
        // check namespace
        int ns = namespace(title);
        if (ns == IMAGE_NAMESPACE || ns == CATEGORY_NAMESPACE)
            throw new UnsupportedOperationException("Tried to move a category/image.");
        // TODO: image renaming? TEST ME (MediaWiki, that is).
 
        // sanitize some params
        title = URLEncoder.encode(title, "UTF-8");
 
        // Check the protection level. We don't use getProtectionLevel(title), as we
        // can fetch a move token at the same time!
        String url = query + "action=query&prop=info&inprop=protection&intoken=move&titles=" + title;
        logurl(url, "move");
        checkLag("move");
        URLConnection connection = new URL(url).openConnection();
        setCookies(connection, cookies);
        connection.connect();
        // cookie fiddling
        grabCookies(connection, cookies2);
        cookies2.putAll(cookies);
        BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
        String line = in.readLine();
 
        // determine whether the page exists
        if (line.indexOf("missing=\"\"") != -1)
            throw new IllegalArgumentException("Tried to move a non-existant page!");
 
        // parse the page
 
        // check protection level
        if (line.indexOf("type=\"move\" level=\"sysop\"") != -1 && (user.userRights() & ADMIN) == 0)
        {
            CredentialException ex = new CredentialException("Permission denied: page is protected.");
            logger.logp(Level.WARNING, "Wiki", "move()", "[" + getDomain() + "] Cannot move - permission denied.", ex);
            throw ex;
        }
 
        // find the move token
        int a = line.indexOf("token=\"") + 7;
        int b = line.indexOf("\"", a);
        String wpMoveToken = line.substring(a, b);
 
        // check target
        if (!checkRights(getProtectionLevel(newTitle), true))
        {
            CredentialException ex = new CredentialException("Permission denied: target page is protected.");
            logger.logp(Level.WARNING, "Wiki", "move()", "[" + getDomain() + "] Cannot move - permission denied.", ex);
            throw ex;
        }
 
        // fetch the appropriate URL
        url = query + "action=move";
        logurl(url, "move");
        connection = new URL(url).openConnection();
        setCookies(connection, cookies2);
        connection.setDoOutput(true);
        connection.connect();
 
        // send the data
        PrintWriter out = new PrintWriter(connection.getOutputStream());
        // PrintWriter out = new PrintWriter(System.out); // debug version
        out.write("from=");
        out.write(title);
        out.write("&to=");
        out.write(URLEncoder.encode(newTitle, "UTF-8"));
        out.write("&reason=");
        out.write(URLEncoder.encode(reason, "UTF-8"));
        out.write("&token=");
        out.write(URLEncoder.encode(wpMoveToken, "UTF-8"));
        if (movetalk)
            out.write("&movetalk=1");
        if (noredirect && (user.userRights() & ADMIN) == ADMIN)
            out.write("&noredirect=1");
        out.close();
 
        // done
        try
        {
            // it's somewhat strange that the edit only sticks when you start reading the response...
            in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
            in.readLine();
 
            // debug version
//                    String line2;
//            while ((line2 = in.readLine()) != null)
//                 System.out.println(line2);
        }
        catch (IOException e)
        {
            InputStream err = ((HttpURLConnection)connection).getInputStream();
            if (err == null)
                throw e;
            in = new BufferedReader(new InputStreamReader(err));
            logger.logp(Level.SEVERE, "Wiki", "move()", "[" + domain + "] EXCEPTION:  ", e);
        }
        in.close();
        log(Level.INFO, "Successfully moved " + title + " to " + newTitle, "move");
 
        // throttle
        try
        {
            long time = throttle - System.currentTimeMillis() + start;
            if (time > 0)
                Thread.sleep(time);
        }
        catch (InterruptedException e)
        {
            // nobody cares
        }
    }
 
    // image methods
 
    /**
     *  Fetches a raster image file. This method uses <tt>ImageIO.read()</tt>,
     *  and as such only JPG, PNG, GIF and BMP formats are supported. SVG
     *  images are supported only if a thumbnail width and height are
     *  specified. Animated GIFs have not been tested yet.
     *
     *  @param title the title of the image (i.e. Image:Example.jpg, not
     *  Example.jpg)
     *  @return the image, encapsulated in a BufferedImage
     *  @throws IOException if a network error occurs
     *  @since 0.10
     */
    public BufferedImage getImage(String title) throws IOException
    {
        return getImage(title, -1, -1);
    }
 
    /**
     *  Fetches a thumbnail of a raster image file. This method uses
     *  <tt>ImageIO.read()</tt>, and as such only JPG, PNG, GIF and BMP
     *  formats are supported. SVG images are supported only if a thumbnail
     *  width and height are specified. Animated GIFs have not been tested yet.
     *
     *  @param title the title of the image without the Image: prefix (i.e.
     *  Example.jpg, not Example.jpg)
     *  @param width the width of the thumbnail (use -1 for actual width)
     *  @param height the height of the thumbnail (use -1 for actual height)
     *  @return the image, encapsulated in a BufferedImage, null if we cannot
     *  read the image
     *  @throws IOException if a network error occurs
     *  @since 0.13
     */
    public BufferedImage getImage(String title, int width, int height) throws IOException
    {
        // sanitise the title
        title = URLEncoder.encode(title, "UTF-8");
 
        // this is a two step process - first we fetch the image url
        StringBuilder url = new StringBuilder(query);
        url.append("&action=query&prop=imageinfo&iiprop=url&titles=Image:");
        url.append(URLEncoder.encode(title, "UTF-8"));
        url.append("&iiurlwidth=");
        url.append(width);
        url.append("&iirulheight=");
        url.append(height);
        logurl(url.toString(), "getImage");
        checkLag("getImage");
        URLConnection connection = new URL(url.toString()).openConnection();
        setCookies(connection, cookies);
        connection.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
        String line = in.readLine();
        int a = line.indexOf("url=\"") + 5;
        int b = line.indexOf("\"", a);
        String url2 = line.substring(a, b);
 
        // then we use ImageIO to read from it
        logurl(url2, "getImage");
        BufferedImage image = ImageIO.read(new URL(url2));
        log(Level.INFO, "Successfully retrieved image \"" + title + "\"", "getImage");
        return image;
    }
 
    /**
     *  Uploads an image. Equivalent to [[Special:Upload]]. Supported
     *  extensions are (case-insensitive) "png", "jpg", "gif" and "svg". You
     *  need to be logged on to do this. This method is thread safe and subject
     *  to the throttle.
     *
     *  @param file the image file
     *  @param filename the target file name (Example.png, not Image:Example.png)
     *  @param contents the contents of the image description page
     *  @throws CredentialNotFoundException if not logged in
     *  @throws CredentialException if page is protected and we can't upload
     *  @throws IOException if a network/local filesystem error occurs
     *  @throws AccountLockedException if user is blocked
     *  @since 0.11
     */
    public synchronized void upload(File file, String filename, String contents) throws IOException, LoginException
    {
        // throttle
        long start = System.currentTimeMillis();
 
        // check for log in
        if (user == null)
        {
            CredentialNotFoundException ex = new CredentialNotFoundException("Permission denied: you need to be registered to upload files.");
            logger.logp(Level.SEVERE, "Wiki", "upload()", "[" + domain + "] Cannot upload - permission denied.", ex);
            throw ex;
        }
 
        // check if the page is protected, and if we can upload
        String filename2 = URLEncoder.encode(filename.replace(" ", "_"), "UTF-8");
        String fname = "Image:" + filename2;
        if (!checkRights(getProtectionLevel(fname), false))
        {
            CredentialException ex = new CredentialException("Permission denied: image is protected.");
            logger.logp(Level.WARNING, "Wiki", "upload()", "[" + domain + "] Cannot upload - permission denied.", ex);
            throw ex;
        }
 
        // prepare MIME type
        String extension = filename2.substring(filename2.length() - 3).toUpperCase().toLowerCase();
        if (extension.equals("jpg"))
            extension = "jpeg";
        else if (extension.equals("svg"))
            extension += "+xml";
 
        // upload the image
        // this is how we do multipart post requests, by the way
        // see also: http://www.w3.org/TR/html4/interact/forms.html#h-17.13.4.2
        String url = base + "Special:Upload";
        URLConnection connection = new URL(url).openConnection();
        logurl(url, "upload");
        String boundary = "----------NEXT PART----------";
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        setCookies(connection, cookies);
        connection.setDoOutput(true);
        connection.connect();
 
        // send data
        boundary = "--" + boundary + "\r\n";
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
//        DataOutputStream out = new DataOutputStream(System.out); // debug version
        out.writeBytes(boundary);
        out.writeBytes("Content-Disposition: form-data; name=\"wpIgnoreWarning\"\r\n\r\n");
        out.writeBytes("true\r\n");
        out.writeBytes(boundary);
        out.writeBytes("Content-Disposition: form-data; name=\"wpDestFile\"\r\n\r\n");
        out.writeBytes(filename2);
        out.writeBytes("\r\n");
        out.writeBytes(boundary);
        out.writeBytes("Content-Disposition: form-data; name=\"wpUploadFile\"; filename=\"");
        out.writeBytes(filename);
        out.writeBytes("\"\r\n");
        out.writeBytes("Content-Type: image/");
        out.writeBytes(extension);
        out.writeBytes("\r\n\r\n");
 
        // write image
        FileInputStream fi = new FileInputStream(file);
        byte[] b = new byte[fi.available()];
        fi.read(b);
        out.write(b);
        fi.close();
 
        // write the rest
        out.writeBytes("\r\n");
        out.writeBytes(boundary);
        out.writeBytes("Content-Disposition: form-data; name=\"wpUploadDescription\"\r\n");
        out.writeBytes("Content-Type: text/plain\r\n\r\n");
        out.writeBytes(contents);
        out.writeBytes("\r\n");
        out.writeBytes(boundary);
        out.writeBytes("Content-Disposition: form-data; name=\"wpUpload\"\r\n\r\n");
        out.writeBytes("Upload file\r\n");
        out.writeBytes(boundary.substring(0, boundary.length() - 2) + "--\r\n");
        out.close();
 
        // done
        BufferedReader in;
        try
        {
            // it's somewhat strange that the edit only sticks when you start reading the response...
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            in.readLine();
 
            // debug version
//            String line;
//            while ((line = in.readLine()) != null)
//                 System.out.println(line);
        }
        catch (IOException e)
        {
            InputStream err = ((HttpURLConnection)connection).getInputStream();
            if (err == null)
                throw e;
            in = new BufferedReader(new InputStreamReader(err));
            logger.logp(Level.SEVERE, "Wiki", "upload()", "[" + domain + "] EXCEPTION:  ", e);
        }
        in.close();
        log(Level.INFO, "Successfully uploaded " + filename, "upload");
 
        // throttle
        try
        {
            long z = throttle - System.currentTimeMillis() + start;
            if (z > 0)
                Thread.sleep(z);
        }
        catch (InterruptedException e)
        {
            // nobody cares
        }
    }
 
    // lists
 
    /**
     *  Performs a full text search of the wiki. Equivalent to
     *  [[Special:Search]], or that little textbox in the sidebar.
     *
     *  @param search a search string
     *  @param namespaces the namespaces to search. If no parameters are passed
     *  then the default is MAIN_NAMESPACE only.
     *  @return the search results
     *  @throws IOException if a network error occurs
     *  @since 0.14
     */
    public String[] search(String search, int... namespaces) throws IOException
    {
        // this varargs thing is really handy, there's no need to define a
        // separate search(String search) while allowing multiple namespaces
 
        // default to main namespace
        if (namespaces.length == 0)
            namespaces = new int[] { MAIN_NAMESPACE };
        StringBuilder url = new StringBuilder(query);
        url.append("action=query&list=search&srwhat=text&srlimit=");
        url.append(250); // a hack, some searches may not produce results when there are some
        url.append("&srsearch=");
        url.append(URLEncoder.encode(search, "UTF-8"));
        url.append("&srnamespace=");
        for (int i = 0; i < namespaces.length; i++)
        {
            url.append(namespaces[i]);
            if (i != namespaces.length - 1)
                url.append("|");
        }
        url.append("&sroffset=");
 
        // some random variables we need later
        int sroffset = 0;
        boolean done = false;
        ArrayList<String> results = new ArrayList<String>(5000);
 
        // fetch and iterate through the search results
        while (!done)
        {
            String newURL = url.toString() + sroffset;
            URLConnection connection = new URL(newURL).openConnection();
            checkLag("search");
            logurl(newURL, "search");
            setCookies(connection, cookies);
            connection.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
            String line = in.readLine();
 
            // if this is the last page of results then there is no sroffset parameter
            if (line.indexOf("sroffset=\"") == -1)
                done = true;
 
            // strip the search results
            // typical form: <p ns="0" title="Main Page" />
            while (line.indexOf("title=\"") != -1)
            {
                int a = line.indexOf("title=\"") + 7;
                int b = line.indexOf("\"", a);
                results.add(line.substring(a, b));
                line = line.substring(b);
            }
            // increment the offset
            in.close();
            sroffset += 250; // CHANGE ME
        }
 
        log(Level.INFO, "Successfully searched for string \"" + search + "\" (" + results.size() + " items found)", "search");
        return results.toArray(new String[0]);
    }
 
    /**
     *  Returns a list of pages which the use the specified image.
     *  @param image the image (Example.png, not Image:Example.png)
     *  @return the list of pages that use this image
     *  @throws IOException if a network error occurs
     *  @since 0.10
     */
    public String[] imageUsage(String image) throws IOException
    {
        return imageUsage(image, ALL_NAMESPACES);
    }
 
    /**
     *  Returns a list of pages in the specified namespace which use the
     *  specified image.
     *  @param image the image (Example.png, not Image:Example.png)
     *  @param namespace a namespace
     *  @return the list of pages that use this image
     *  @throws IOException if a network error occurs
     *  @since 0.10
     */
    public String[] imageUsage(String image, int namespace) throws IOException
    {
        String url = query + "action=query&list=imageusage&iutitle=Image:" + URLEncoder.encode(image, "UTF-8") + "&iulimit=" + max;
        if (namespace != ALL_NAMESPACES)
            url += "&iunamespace=" + namespace;
 
        // fiddle
        ArrayList<String> pages = new ArrayList<String>(1333);
        String next = "";
        while ((pages.size() % max) == 0)
        {
            // connect
            if (pages.size() != 0)
                next = "&iucontinue="  + next;
            URLConnection connection = new URL(url + next).openConnection();
            logurl(url + next, "imageUsage");
            checkLag("imageUsage");
            setCookies(connection, cookies);
            connection.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
            String line = in.readLine();
 
            // parse
            if (line.indexOf("iucontinue") != -1)
                next = line.substring(line.indexOf("iucontinue") + 12, line.indexOf("\" />"));
            while (line.indexOf("title") != -1)
            {
                int x = line.indexOf("title=\"");
                int y = line.indexOf("\" />", x);
                pages.add(decode(line.substring(x + 7, y)));
                line = line.substring(y + 4, line.length());
            }
 
            // short circuit
            in.close();
            if (pages.size() == 0)
                break;
        }
        log(Level.INFO, "Successfully retrieved usages of Image:" + image + " (" + pages.size() + " items)", "imageUsage");
        return pages.toArray(new String[0]);
    }
 
    /**
     *  Returns a list of all pages linking to this page. Equivalent to
     *  [[Special:Whatlinkshere]].
     *
     *  @param title the title of the page
     *  @return the list of pages linking to the specified page
     *  @throws IOException if a network error occurs
     *  @since 0.10
     */
    public String[] whatLinksHere(String title) throws IOException
    {
        return whatLinksHere(title, ALL_NAMESPACES, false);
    }
 
    /**
     *  Returns a list of all pages linking to this page. Equivalent to
     *  [[Special:Whatlinkshere]].
     *
     *  @param title the title of the page
     *  @param namespace a namespace
     *  @return the list of pages linking to the specified page
     *  @throws IOException if a network error occurs
     *  @since 0.10
     */
    public String[] whatLinksHere(String title, int namespace) throws IOException
    {
        return whatLinksHere(title, namespace, false);
    }
 
    /**
     *  Returns a list of all pages linking to this page within the specified
     *  namespace. Alternatively, we can retrive a list of what redirects to a
     *  page by setting <tt>redirects</tt> to true. Equivalent to
     *  [[Special:Whatlinkshere]].
     *
     *  @param title the title of the page
     *  @param namespace a namespace
     *  @param redirects whether we should limit to redirects only
     *  @return the list of pages linking to the specified page
     *  @throws IOException if a network error occurs
     *  @since 0.10
     */
    public String[] whatLinksHere(String title, int namespace, boolean redirects) throws IOException
    {
        String url = query + "action=query&list=backlinks&bllimit=" + max + "&bltitle=" + URLEncoder.encode(title, "UTF-8");
        if (namespace != ALL_NAMESPACES)
            url += "&blnamespace=" + namespace;
        if (redirects)
            url += "&blfilterredir=redirects";
 
        // fiddle
        ArrayList<String> pages = new ArrayList<String>(6667); // generally enough
        String next = "";
        while ((pages.size() % max) == 0)
        {
            // connect
            if (pages.size() != 0)
                next = "&blcontinue=" + URLEncoder.encode(next, "UTF-8");
            logurl(url + next, "whatLinksHere");
            checkLag("whatLinksHere");
            URLConnection connection = new URL(url + next).openConnection();
            setCookies(connection, cookies);
            connection.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
            String line = in.readLine();
 
            // parse
            if (line.indexOf("blcontinue") != -1)
                next = line.substring(line.indexOf("blcontinue") + 12, line.indexOf("\" />"));
            while (line.indexOf("title") != -1)
            {
                int x = line.indexOf("title=\"");
                int y = line.indexOf("\" ", x);
                pages.add(decode(line.substring(x + 7, y)));
                line = line.substring(y + 4, line.length());
            }
 
            // short circuit
            in.close();
            if (pages.size() == 0)
                break;
        }
        log(Level.INFO, "Successfully retrieved " + (redirects ? "redirects to " : "links to ") + title + " (" + pages.size() + " items)", "whatLinksHere");
        return pages.toArray(new String[0]);
    }
 
    /**
     *  Returns a list of all pages transcluding to a page.
     *
     *  @param title the title of the page, e.g. "Template:Stub"
     *  @return the list of pages transcluding the specified page
     *  @throws IOException if a netwrok error occurs
     *  @since 0.12
     */
    public String[] whatTranscludesHere(String title) throws IOException
    {
        return whatTranscludesHere(title, ALL_NAMESPACES);
    }
 
    /**
     *  Returns a list of all pages transcluding to a page within the specified
     *  namespace.
     *
     *  @param title the title of the page, e.g. "Template:Stub"
     *  @param namespace a namespace
     *  @return the list of pages transcluding the specified page
     *  @throws IOException if a netwrok error occurs
     *  @since 0.12
     */
    public String[] whatTranscludesHere(String title, int namespace) throws IOException
    {
        String url = query + "action=query&list=embeddedin&eilimit=" + max + "&eititle=" + URLEncoder.encode(title, "UTF-8");
        if (namespace != ALL_NAMESPACES)
            url += "&einamespace=" + namespace;
 
        // fiddle
        ArrayList<String> pages = new ArrayList<String>(6667); // generally enough
        String next = "";
        while ((pages.size() % max) == 0)
        {
            // connect
            if (pages.size() != 0)
                next = "&eicontinue=" + URLEncoder.encode(next, "UTF-8");
            logurl(url + next, "whatTranscludesHere");
            checkLag("whatTranscludesHere");
            URLConnection connection = new URL(url + next).openConnection();
            setCookies(connection, cookies);
            connection.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
            String line = in.readLine();
 
            // parse
            if (line.indexOf("eicontinue") != -1)
                next = line.substring(line.indexOf("eicontinue") + 12, line.indexOf("\" />"));
            while (line.indexOf("title") != -1)
            {
                int x = line.indexOf("title=\"");
                int y = line.indexOf("\" />", x);
                pages.add(decode(line.substring(x + 7, y)));
                line = line.substring(y + 4, line.length());
            }
 
            // short circuit
            in.close();
            if (pages.size() == 0)
                break;
        }
        log(Level.INFO, "Successfully retrieved transclusions of " + title + " (" + pages.size() + " items)", "whatTranscludesHere");
        return pages.toArray(new String[0]);
    }
 
    /**
     *  Gets the members of a category.
     *
     *  @param name the name of the category (e.g. Candidates for speedy
     *  deletion, not Category:Candidates for speedy deletion)
     *  @return a String[] containing page titles of members of the category
     *  @throws IOException if a network error occurs
     *  @since 0.02
     */
    public String[] getCategoryMembers(String name) throws IOException
    {
        return getCategoryMembers(name, ALL_NAMESPACES);
    }
 
    /**
     *  Gets the members of a category.
     *
     *  @param name the name of the category (e.g. Candidates for speedy
     *  deletion, not Category:Candidates for speedy deletion)
     *  @param namespace filters by namespace, returns empty if namespace
     *  does not exist
     *  @return a String[] containing page titles of members of the category
     *  @throws IOException if a network error occurs
     *  @since 0.03
     */
    public String[] getCategoryMembers(String name, int namespace) throws IOException
    {
        String url = query + "action=query&list=categorymembers&cmprop=title&cmlimit=" + max + "&cmtitle=Category:" + URLEncoder.encode(name, "UTF-8");
        if (namespace != ALL_NAMESPACES)
            url += "&cmnamespace=" + namespace;
 
        // work around an arbitrary and silly limitation
        ArrayList<String> members = new ArrayList<String>(6667); // enough for most cats
        String next = "";
        while ((members.size() % max) == 0)
        {
            if (members.size() != 0)
                next = "&cmcontinue=" + URLEncoder.encode(next, "UTF-8");
            logurl(url + next, "getCategoryMembers");
            checkLag("getCategoryMembers");
            URLConnection connection = new URL(url + next).openConnection();
            setCookies(connection, cookies);
            connection.connect();
 
            // read the first line, as it is the only thing worth paying attention to
            BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
            String line = in.readLine();
 
            // parse
            if (line.indexOf("cmcontinue") != -1)
                next = line.substring(line.indexOf("cmcontinue") + 12, line.indexOf("\" />"));
            while (line.indexOf("title") != -1)
            {
                int x = line.indexOf("title=\"");
                int y = line.indexOf("\" />", x);
                members.add(decode(line.substring(x + 7, y)));
                line = line.substring(y + 4, line.length());
            }
 
            // short circuit
            in.close();
            if (members.size() == 0)
                break;
        }
        log(Level.INFO, "Successfully retrieved contents of Category:" + name + " (" + members.size() + " items)", "getCategoryMembers");
        return members.toArray(new String[0]);
    }
 
    /**
     *  Searches the wiki for external links. Equivalent to [[Special:Linksearch]].
     *  Returns two lists, where the first is the list of pages and the
     *  second is the list of urls. The index of a page in the first list
     *  corresponds to the index of the url on that page in the second list.
     *  Wildcards (*) are only permitted at the start of the search string.
     *
     *  @param pattern the pattern (String) to search for (e.g. example.com,
     *  *.example.com)
     *  @throws IOException if a network error occurs
     *  @return two lists - index 0 is the list of pages (String), index 1 is
     *  the list of urls (instance of <tt>java.net.URL</tt>)
     *  @since 0.06
     */
    public ArrayList[] spamsearch(String pattern) throws IOException
    {
        return spamsearch(pattern, ALL_NAMESPACES);
    }
 
    /**
     *  Searches the wiki for external links. Equivalent to [[Special:Linksearch]].
     *  Returns two lists, where the first is the list of pages and the
     *  second is the list of urls. The index of a page in the first list
     *  corresponds to the index of the url on that page in the second list.
     *  Wildcards (*) are only permitted at the start of the search string.
     *
     *  @param pattern the pattern (String) to search for (e.g. example.com,
     *  *.example.com)
     *  @param namespace filters by namespace, returns empty if namespace
     *  does not exist
     *  @throws IOException if a network error occurs
     *  @return two lists - index 0 is the list of pages (String), index 1 is
     *  the list of urls (instance of <tt>java.net.URL</tt>)
     *  @since 0.06
     */
    public ArrayList[] spamsearch(String pattern, int namespace) throws IOException
    {
        // set it up
        StringBuilder url = new StringBuilder(query);
        url.append("action=query&list=exturlusage&euprop=title|url&euquery=");
        url.append(pattern);
        url.append("&eulimit=");
        url.append(max);
        if (namespace != ALL_NAMESPACES)
        {
            url.append("&eunamespace=");
            url.append(namespace);
        }
        url.append("&euoffset=");
 
        // some variables we need later
        int euoffset = 0;
        boolean done = false;
        ArrayList[] ret = new ArrayList[] // no reason for more than 500 spamlinks
        {
            new ArrayList<String>(667), // page titles
            new ArrayList<URL>(667) // urls
        };
 
        // begin
        while (!done)
        {
            // fetch the results
            String newURL = url.toString() + euoffset;
            URLConnection connection = new URL(newURL).openConnection();
            logurl(newURL, "spamsearch");
            checkLag("spamsearch");
            setCookies(connection, cookies);
            connection.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
            String line = in.readLine();
 
            // if this is the last page of results then there is no euoffset parameter
            if (line.indexOf("euoffset=\"") == -1)
                done = true;
 
            // parse
            // typical form: <eu ns="0" title="Main Page" url="http://example.com" />
            while (line.indexOf("<eu ") != -1)
            {
                int x = line.indexOf("title=\"");
                int y = line.indexOf("\" url=\"");
                int z = line.indexOf("\" />", y);
 
                String title = line.substring(x + 7, y);
                String link = line.substring(y + 7, z);
                ret[0].add(decode(title));
                ret[1].add(new URL(link));
                line = line.substring(z + 3);
            }
 
            // update the offset
            euoffset += max;
            in.close();
        }
 
        // return value
        log(Level.INFO, "Successfully returned instances of external link " + pattern + " (" + ret[0].size() + " links)", "spamsearch");
        return ret;
    }
 
    /**
     *  Looks up a particular user in the IP block list, i.e. whether a user
     *  is currently blocked. Equivalent to [[Special:Ipblocklist]].
     *
     *  @param user a username or IP (e.g. "127.0.0.1")
     *  @return the block log entry
     *  @throws IOException if a network error occurs
     *  @since 0.12
     */
    public LogEntry[] getIPBlockList(String user) throws IOException
    {
        return getIPBlockList(user, null, null, 1);
    }
 
    /**
     *  Lists currently operating blocks that were made in the specified
     *  interval. Equivalent to [[Special:Ipblocklist]].
     *
     *  @param start the start date
     *  @param end the end date
     *  @return the currently operating blocks that were made in that interval
     *  @throws IOException if a network error occurs
     *  @since 0.12
     */
    public LogEntry[] getIPBlockList(Calendar start, Calendar end) throws IOException
    {
        return getIPBlockList("", start, end, Integer.MAX_VALUE);
    }
 
    /**
     *  Fetches part of the list of currently operational blocks. Equivalent to
     *  [[Special:Ipblocklist]]. WARNING: cannot tell whether a particular IP
     *  is autoblocked as this is non-public data (see also [[bugzilla:12321]]
     *  and [[foundation:Privacy policy]]). Don't call this directly, use one
     *  of the two above methods instead.
     *
     *  @param user a particular user that might have been blocked. Use "" to
     *  not specify one. May be an IP (e.g. "127.0.0.1") or a CIDR range (e.g.
     *  "127.0.0.0/16") but not an autoblock (e.g. "#123456").
     *  @param start what timestamp to start. Use null to not specify one.
     *  @param end what timestamp to end. Use null to not specify one.
     *  @param amount the number of blocks to retrieve. Use
     *  <tt>Integer.MAX_VALUE</tt> to not specify one.
     *  @return a LogEntry[] of the blocks
     *  @throws IOException if a network error occurs
     *  @throws IllegalArgumentException if start date is before end date
     *  @since 0.12
     */
    protected LogEntry[] getIPBlockList(String user, Calendar start, Calendar end, int amount) throws IOException
    {
        // quick param check
        if (start != null && end != null)
            if (start.before(end))
                throw new IllegalArgumentException("Specified start date is before specified end date!");
        String bkstart = start != null ? calendarToTimestamp(start) : "";
        String bkend = end != null ? calendarToTimestamp(end) : "";
 
        // url base
        StringBuilder urlBase = new StringBuilder(query);
        urlBase.append("action=query&list=blocks");
        if (end != null)
        {
            urlBase.append("&bkend=");
            urlBase.append(bkend);
        }
        if (!user.equals(""))
        {
            urlBase.append("&bkusers=");
            urlBase.append(user);
        }
        urlBase.append("&bklimit=");
        urlBase.append(amount < max ? amount : max);
        if (start != null) // start is only null when a user is specified
            urlBase.append("&bkstart=");
 
        // connection
        ArrayList<LogEntry> entries = new ArrayList<LogEntry>(1333);
                while ((entries.size() % max) == 0)
        {
            String url = urlBase.toString() + bkstart;
                logurl(url, "getIPBlockList");
            checkLag("getIPBlockList");
                URLConnection connection = new URL(url).openConnection();
                setCookies(connection, cookies);
            connection.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
            String line = in.readLine();
 
            // set start parameter to new value if required
            if (amount - entries.size() >= max)
            {
                int a = line.indexOf("bkstart=\"") + 9;
                bkstart = line.substring(a, line.indexOf("\"", a));
            }
 
            // parse xml
            while (entries.size() < amount  && line.indexOf("<block ") != -1)
            {
                // find entry
                int a = line.indexOf("<block ");
                int b = line.indexOf("/>", a);
                String entry = line.substring(a, b);
                line = line.substring(b);
 
                // target
                String target = "";
                a = entry.indexOf("user=\"") + 6;
                if (a < 6) // it's an autoblock, use block id instead (#zzzz)
                {
                    a = entry.indexOf("id=\"") + 4;
                    target = "#";
                }
                b = entry.indexOf("\"", a);
                target += entry.substring(a, b);
 
                // blocking admin
                a = entry.indexOf("by=\"") + 4;
                b = entry.indexOf("\"", a);
                User admin = new User(entry.substring(a, b));
 
                // timestamp
                a = entry.indexOf("timestamp=\"") + 11;
                b = entry.indexOf("\"", a);
                String timestamp = convertTimestamp(entry.substring(a, b));
 
                // reason
                a = entry.indexOf("reason=\"") + 8;
                b = entry.indexOf("\"", a);
                String reason = decode(entry.substring(a, b));
 
                // details see LogEntry.getDetails()
                Object[] details = new Object[5];
                details[0] = new Boolean(entry.indexOf("anononly") != -1); // anons only
                details[1] = new Boolean(entry.indexOf("nocreate") != -1); // account creation disabled
                details[2] = new Boolean(entry.indexOf("noautoblock") != -1); // autoblock disabled
                details[3] = new Boolean(entry.indexOf("noemail") != -1); // [[Special:Emailuser]] disabled
                a = entry.indexOf("expiry=\"") + 8;
                int c = entry.indexOf("\"", a);
                details[4] = entry.substring(a, c);
 
                // add
                entries.add(new LogEntry(BLOCK_LOG, "block", reason, admin, target, timestamp, details));
            }
 
            // short circuit
            if (entries.size() == 0)
                break;
        }
 
        // log statement
        StringBuilder logRecord = new StringBuilder("Successfully fetched IP block list ");
        if (!user.equals(""))
        {
            logRecord.append(" for ");
            logRecord.append(user);
        }
        if (start != null)
        {
            logRecord.append(" from ");
            logRecord.append(start.getTime().toString());
        }
        if (end != null)
        {
            logRecord.append(" to ");
            logRecord.append(end.getTime().toString());
        }
        logRecord.append(" (");
        logRecord.append(entries.size());
        logRecord.append(" entries)");
        log(Level.INFO, logRecord.toString(), "getIPBlockList");
        return entries.toArray(new LogEntry[0]);
     }
 
    /**
     *  Gets the most recent set of log entries up to the given amount.
     *  Equivalent to [[Special:Log]].
     *
     *  @param amount the amount of log entries to get
     *  @return the most recent set of log entries
     *  @throws IOException if a network error occurs
     *  @throws IllegalArgumentException if amount < 1
     *  @since 0.08
     */
    public LogEntry[] getLogEntries(int amount) throws IOException
    {
        return getLogEntries(null, null, amount, ALL_LOGS, null, "", ALL_NAMESPACES);
    }
 
    /**
     *  Gets log entries for a specific user. Equivalent to [[Special:Log]]. Dates
     *  and timestamps are in UTC.
     *
     *  @param user the user to get log entries for
     *  @throws IOException if a network error occurs
     *  @return the set of log entries created by that user
     *  @since 0.08
     */
    public LogEntry[] getLogEntries(User user) throws IOException
    {
        return getLogEntries(null, null, Integer.MAX_VALUE, ALL_LOGS, user, "", ALL_NAMESPACES);
    }
 
    /**
     *  Gets the log entries representing actions that were performed on a
     *  specific target. Equivalent to [[Special:Log]]. Dates and timestamps are
     *  in UTC.
     *
     *  @param target the target of the action(s).
     *  @throws IOException if a network error occurs
     *  @return the specified log entries
     *  @since 0.08
     */
    public LogEntry[] getLogEntries(String target) throws IOException
    {
        return getLogEntries(null, null, Integer.MAX_VALUE, ALL_LOGS, null, target, ALL_NAMESPACES);
    }
 
    /**
     *  Gets all log entries that occurred between the specified dates.
     *  WARNING: the start date is the most recent of the dates given, and
     *   the order of enumeration is from newest to oldest. Equivalent to
     *  [[Special:Log]]. Dates and timestamps are in UTC.
     *
     *  @param start what timestamp to start. Use null to not specify one.
     *  @param end what timestamp to end. Use null to not specify one.
     *  @throws IOException if something goes wrong
     *  @throws IllegalArgumentException if start &lt; end
     *  @return the specified log entries
     *  @since 0.08
     */
    public LogEntry[] getLogEntries(Calendar start, Calendar end) throws IOException
    {
        return getLogEntries(start, end, Integer.MAX_VALUE, ALL_LOGS, null, "", ALL_NAMESPACES);
    }
 
    /**
     *  Gets the last how ever many log entries in the specified log. Equivalent
     *  to [[Special:Log]]. Dates and timestamps are in UTC.
     *
     *  @param amount the number of entries to get
     *  @param type what log to get (e.g. DELETION_LOG)
     *  @throws IOException if a network error occurs
     *  @throws IllegalArgumentException if the log type doesn't exist
     *  @return the specified log entries
     */
    public LogEntry[] getLogEntries(int amount, String type) throws IOException
    {
        return getLogEntries(null, null, amount, type, null, "", ALL_NAMESPACES);
    }
 
    /**
     *  Gets the specified amount of log entries between the given times by
     *  the given user on the given target. Equivalent to [[Special:Log]].
     *  WARNING: the start date is the most recent of the dates given, and
     *  the order of enumeration is from newest to oldest. Dates and timestamps
     *  are in UTC.
     *
     *  @param start what timestamp to start. Use null to not specify one.
     *  @param end what timestamp to end. Use null to not specify one.
     *  @param amount the amount of log entries to get. If both start and
     *  end are defined, this is ignored. Use Integer.MAX_VALUE to not
     *  specify one.
     *  @param log what log to get (e.g. DELETION_LOG)
     *  @param user the user performing the action. Use null not to specify
     *  one.
     *  @param target the target of the action. Use "" not to specify one.
     *  @param namespace filters by namespace. Returns empty if namespace
     *  doesn't exist.
     *  @throws IOException if a network error occurs
     *  @throws IllegalArgumentException if start &lt; end or amount &lt; 1
     *  @return the specified log entries
     *  @since 0.08
     */
    public LogEntry[] getLogEntries(Calendar start, Calendar end, int amount, String log, User user, String target, int namespace) throws IOException
    {
        // construct the query url from the parameters given
        StringBuilder url = new StringBuilder(query);
        url.append("action=query&list=logevents&leprop=title|type|user|timestamp|comment|details");
        StringBuilder console = new StringBuilder("Successfully retrieved "); // logger statement
 
        // check for amount
        if (amount < 1)
            throw new IllegalArgumentException("Tried to retrieve less than one log entry!");
 
        // log type
        if (!log.equals(ALL_LOGS))
        {
            url.append("&letype=");
            url.append(log);
        }
 
        // specific log types
        if (log.equals(USER_CREATION_LOG))
            console.append("user creation");
        else if (log.equals(DELETION_LOG))
            console.append("deletion");
        else if (log.equals(PROTECTION_LOG))
            console.append("protection");
        else if (log.equals(USER_RIGHTS_LOG))
            console.append("user rights");
        else if (log.equals(USER_RENAME_LOG))
            console.append("user rename");
        else if (log.equals(BOT_STATUS_LOG))
            console.append("bot status");
        else
        {
            console.append(" ");
            console.append(log);
        }
        console.append(" log ");
 
        // check for user parameter
        if (user != null)
        {
            url.append("&leuser=");
            url.append(URLEncoder.encode(user.getUsername(), "UTF-8"));
            console.append("for ");
            console.append(user.getUsername());
            console.append(" ");
        }
 
        // check for target
        if (!target.equals(""))
        {
            url.append("&letitle=");
            url.append(URLEncoder.encode(target, "UTF-8"));
            console.append("on ");
            console.append(target);
            console.append(" ");
        }
 
        // Check for namespace. Should be done server-side.
//        if (namespace != ALL_NAMESPACES)
//        {
//            url.append("&lenamespace=");
//            url.append(namespace);
//        }
 
        // set maximum
        url.append("&lelimit=");
        url.append(amount > max || namespace != ALL_NAMESPACES ? max : amount);
 
        // check for start/end dates
        String lestart = ""; // we need to account for lestart being the continuation parameter too.
        if (start != null)
        {
            if (end != null && start.before(end)) //aargh
                throw new IllegalArgumentException("Specified start date is before specified end date!");
            lestart = new String(calendarToTimestamp(start));
            console.append("from ");
            console.append(start.getTime().toString());
            console.append(" ");
        }
        if (end != null)
        {
            url.append("&leend=");
            url.append(calendarToTimestamp(end));
            console.append("to ");
            console.append(end.getTime().toString());
            console.append(" ");
        }
 
        // only now we can actually start to retrieve the logs
        ArrayList<LogEntry> entries = new ArrayList<LogEntry>(6667); // should be enough
        while (entries.size() < amount && !lestart.equals("a"))
        {
            String str = url.toString() + "&lestart=" + lestart;
            logurl(str, "getLogEntries");
            checkLag("getLogEntries");
            URLConnection connection = new URL(str).openConnection();
            setCookies(connection, cookies);
            connection.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
            String line = in.readLine();
 
            // set start parameter to new value
            int ab = line.indexOf("lestart=\"") + 9;
            if (ab != 8)
                lestart = line.substring(ab, line.indexOf("\"", ab));
            else
                lestart = "a"; //set a signal so we know when to stop
 
            // parse xml. We need to repeat the test because the XML may contain more than the required amount.
            while (entries.size() < amount)
            {
                // find entry
                int a = line.indexOf("<item");
                if (a < 0)
                    break;
                // end may be " />" or "</item>", followed by next item
                int b = line.indexOf("><item", a);
                if (b < 0) // last entry
                    b = line.length();
                String entry = line.substring(a, b);
                line = line.substring(b);
 
                // page title
                a = entry.indexOf("title=\"") + 7;
                b = entry.indexOf("\" ", a);
                String title = decode(entry.substring(a, b));
 
                // log type
                String entryType;
                if (log.equals(ALL_LOGS))
                {
                    a = entry.indexOf("type=\"") + 6;
                    b = entry.indexOf("\" ", a);
                    entryType = entry.substring(a, b);
                }
                else
                    entryType = log;
 
                // action
                a = entry.indexOf("action=\"") + 8;
                b = entry.indexOf("\" ", a);
                String action = entry.substring(a, b);
 
                // user
                User performer;
                if (user == null)
                {
                    a = entry.indexOf("user=\"") + 6;
                    b = entry.indexOf("\" ", a);
                    performer = new User(entry.substring(a, b));
                }
                else
                    performer = user;
 
                // reason
                String reason;
                if (entryType.equals(USER_CREATION_LOG)) // there is no reason for creating a user
                    reason = "";
                else
                {
                    a = entry.indexOf("comment=\"") + 9;
                    b = entry.indexOf("\"", a);
                    reason = decode(entry.substring(a, b));
                }
 
                // timestamp
                a = entry.indexOf("timestamp=\"") + 11;
                b = a + 20;
                String timestamp = convertTimestamp(entry.substring(a, b));
 
                // details
                Object details = null;
                if (entryType.equals(MOVE_LOG))
                {
                    a = entry.indexOf("new_title=\"") + 11;
                    b = entry.indexOf("\" />", a);
                    details = decode(entry.substring(a, b)); // the new title
                }
                else if (entryType.equals(BLOCK_LOG))
                {
                    a = entry.indexOf("<block") + 7;
                    String s = entry.substring(a);
                    int c = s.indexOf("duration=") + 10;
                    int d = s.indexOf("\"", c);
                    details = new Object[]
                    {
                        new Boolean(s.indexOf("anononly") > -1), // anon-only
                        new Boolean(s.indexOf("nocreate") > -1), // account creation blocked
                        new Boolean(s.indexOf("noautoblock") > -1), // autoblock disabled
                        new Boolean(s.indexOf("noemail") > -1), // email disabled
                        s.substring(c, d) // duration
                    };
                }
                else if (entryType.equals(USER_RENAME_LOG))
                {
                    a = entry.indexOf("<param>") + 7;
                    b = entry.indexOf("</param>", a);
                        details = entry.substring(a, b); // the new username
                }
                else if (entryType.equals(USER_RIGHTS_LOG))
                {
                    a = entry.indexOf("new=\"") + 5;
                    b = entry.indexOf("\"", a);
                    String z = entry.substring(a, b);
                    int rights = 1; // no ips in user rights log
                    rights += (z.indexOf("sysop") != -1 ? ADMIN : 0); // sysop
                    rights += (z.indexOf("bureaucrat") != -1 ? BUREAUCRAT : 0); // bureaucrat
                    rights += (z.indexOf("steward") != -1 ? STEWARD : 0); // steward
                    rights += (z.indexOf("bot") != -1 ? BOT : 0); // bot
                    details = new Integer(rights);
                }
 
                // namespace processing
                if (namespace == ALL_NAMESPACES || namespace(title) == namespace)
                    entries.add(new LogEntry(entryType, action, reason, performer, title, timestamp.toString(), details));
            }
            in.close();
        }
 
        // log the success
        console.append(" (");
        console.append(entries.size());
        console.append(" entries)");
        log(Level.INFO, console.toString(), "getLogEntries");
        return entries.toArray(new LogEntry[0]);
    }
 
    /**
     *  Lists pages that start with a given prefix. Equivalent to
     *  [[Special:Prefixindex]].
     *
     *  @param prefix the prefix
     *  @return the list of pages with that prefix
     *  @throws IOException if a network error occurs
     *  @since 0.15
     */
    public String[] prefixIndex(String prefix) throws IOException
    {
        return listPages(prefix, NO_PROTECTION, ALL_NAMESPACES, -1, -1);
    }
 
    /**
     *  List pages below a certain size in the main namespace. Equivalent to
     *  [[Special:Shortpages]].
     *  @param cutoff the maximum size in bytes these short pages can be
     *  @return pages below that size
     *  @throws IOException if a network error occurs
     *  @since 0.15
     */
    public String[] shortPages(int cutoff) throws IOException
    {
        return listPages("", NO_PROTECTION, MAIN_NAMESPACE, -1, cutoff);
    }
 
    /**
     *  List pages below a certain size in any namespace. Equivalent to
     *  [[Special:Shortpages]].
     *  @param cutoff the maximum size in bytes these short pages can be
     *  @param namespace a namespace
     *  @throws IOException if a network error occurs
     *  @return pages below that size in that namespace
     *  @since 0.15
     */
    public String[] shortPages(int cutoff, int namespace) throws IOException
    {
        return listPages("", NO_PROTECTION, namespace, -1, cutoff);
    }
 
    /**
     *  List pages above a certain size in the main namespace. Equivalent to
     *  [[Special:Longpages]].
     *  @param cutoff the minimum size in bytes these long pages can be
     *  @return pages above that size
     *  @throws IOException if a network error occurs
     *  @since 0.15
     */
    public String[] longPages(int cutoff) throws IOException
    {
        return listPages("", NO_PROTECTION, MAIN_NAMESPACE, cutoff, -1);
    }
 
    /**
     *  List pages above a certain size in any namespace. Equivalent to
     *  [[Special:Longpages]].
     *  @param cutoff the minimum size in nbytes these long pages can be
     *  @param namespace a namespace
     *  @return pages above that size
     *  @throws IOException if a network error occurs
     *  @since 0.15
     */
    public String[] longPages(int cutoff, int namespace) throws IOException
    {
        return listPages("", NO_PROTECTION, namespace, cutoff, -1);
    }
 
    /**
     *  Lists pages with titles containing a certain prefix with a certain
     *  protection level and in a certain namespace. Equivalent to
     *  [[Special:Allpages]], [[Special:Prefixindex]], [[Special:Protectedpages]]
     *  and [[Special:Allmessages]] (if namespace == MEDIAWIKI_NAMESPACE).
     *  WARNING: Limited to 500 values (5000 for bots), unless a prefix is
     *  specified.
     *
     *  @param prefix the prefix of the title. Use "" to not specify one.
     *  @param level a protection level. Use NO_PROTECTION to not specify one.
     *  WARNING: it is not currently possible to specify a combination of both
     *  semi and move protection
     *  @param namespace a namespace
     *  @return the specified list of pages
     *  @since 0.09
     *  @throws IOException if a network error occurs
     */
    public String[] listPages(String prefix, int level, int namespace) throws IOException
    {
        return listPages(prefix, level, namespace, -1, -1);
    }
 
    /**
     *  Lists pages with titles containing a certain prefix with a certain
     *  protection level and in a certain namespace. Equivalent to
     *  [[Special:Allpages]], [[Special:Prefixindex]], [[Special:Protectedpages]]
     *  [[Special:Allmessages]] (if namespace == MEDIAWIKI_NAMESPACE),
     *  [[Special:Shortpages]] and [[Special:Longpages]]. WARNING: Limited to
     *  500 values (5000 for bots), unless a prefix or (max|min)imum size is
     *  specified.
     *
     *  @param prefix the prefix of the title. Use "" to not specify one.
     *  @param level a protection level. Use NO_PROTECTION to not specify one.
     *  WARNING: it is not currently possible to specify a combination of both
     *  semi and move protection
     *  @param namespace a namespace
     *  @param minimum the minimum size in bytes these pages can be. Use -1 to
     *  not specify one.
     *  @param maximum the maximum size in bytes these pages can be. Use -1 to
     *  not specify one.
     *  @return the specified list of pages
     *  @since 0.09
     *  @throws IOException if a network error occurs
     */
    public String[] listPages(String prefix, int level, int namespace, int minimum, int maximum) throws IOException
    {
        // @revised 0.15 to add short/long pages
        StringBuilder url = new StringBuilder(query);
        url.append("action=query&list=allpages&aplimit=");
        url.append(max);
        if (!prefix.equals("")) // prefix
        {
            url.append("&apprefix=");
            url.append(URLEncoder.encode(prefix, "UTF-8"));
          }
        if (namespace != ALL_NAMESPACES) // check for namespace
        {
            url.append("&apnamespace=");
            url.append(namespace);
        }
        switch (level) // protection level
        {
            case NO_PROTECTION: // squelch, this is the default
                break;
            case SEMI_PROTECTION:
                url.append("&apprlevel=autoconfirmed&apprtype=edit");
                break;
            case FULL_PROTECTION:
                url.append("&apprlevel=sysop&apprtype=edit");
                break;
            case MOVE_PROTECTION:
                url.append("&apprlevel=sysop&apprtype=move");
                break;
            case SEMI_AND_MOVE_PROTECTION: // squelch, not implemented
                break;
            default:
                throw new IllegalArgumentException("Invalid protection level!");
        }
                // max and min
        if (minimum != -1)
        {
            url.append("&apminsize=");
                        url.append(minimum);
        }
        if (maximum != -1)
        {
            url.append("&apmaxsize=");
                        url.append(maximum);
        }
 
        // parse
        ArrayList<String> pages = new ArrayList<String>(6667);
        String next = "";
        while (pages.size() < max)
        {
            // connect and read
            String s;
            if (next.equals("") && pages.size() != 0) // done
                break;
            else if (next.equals(""))
                s = url.toString();
            else
                s = url.toString() + "&apfrom=" + next;
            URLConnection connection = new URL(s).openConnection();
            logurl(s, "listPages");
            checkLag("listPages");
            setCookies(connection, cookies);
            connection.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
            String line = in.readLine();
 
            // find next value
            int a = line.indexOf("apfrom=\"");
            int b;
            if (a > -1)
            {
                a += 8;
                b = line.indexOf("\"", a);
                next = line.substring(a, b).replace(' ', '_');
            }
 
            // find the pages
            while (line.indexOf("<p ") > -1)
            {
                a = line.indexOf("title=\"") + 7;
                b = line.indexOf("\" />", a);
                pages.add(decode(line.substring(a, b)));
                line = line.substring(b, line.length());
            }
            in.close();
            if (pages.size() == 0)
                break; // short circuit
            if (prefix.equals(""))
                break; // break if prefix not specified
        }
 
        // tidy up
        log(Level.INFO, "Successfully retrieved page list (" + pages.size() + " pages)", "listPages");
        return pages.toArray(new String[0]);
    }
 
    // user methods
 
    /**
     *  Determines whether a specific user exists. Should evaluate to false
     *  for anons.
     *
     *  @param username a username
     *  @return whether the user exists
     *  @throws IOException if a network error occurs
     *  @since 0.05
     */
    public boolean userExists(String username) throws IOException
    {
        return allUsers(username, 1)[0].equals(username);
    }
 
    /**
     *  Gets the specified number of users (as a String) starting at the
     *  given string, in alphabetical order. Equivalent to [[Special:Listusers]].
     *
     *  @param start the string to start enumeration
     *  @param number the number of users to return
     *  @return a String[] containing the usernames
     *  @throws IOException if a network error occurs
     *  @since 0.05
     */
    public String[] allUsers(String start, int number) throws IOException
    {
        // sanitise
        String url = query + "action=query&list=allusers&aulimit=" + (number > max ? max : number) + "&aufrom=";
 
        // work around an arbitrary and silly limitation
        ArrayList<String> members = new ArrayList<String>(6667); // enough for most requests
        String next = URLEncoder.encode(start.replace(" ", "_"), "UTF-8");
        while ((members.size() % max) == 0 && members.size() < number)
        {
            next = URLEncoder.encode(next, "UTF-8");
            logurl(url + next, "allUsers");
            checkLag("allUsers");
            URLConnection connection = new URL(url + next).openConnection();
            setCookies(connection, cookies);
            connection.connect();
 
            // read the first line, as it is the only thing worth paying attention to
            BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
            String line = in.readLine();
 
            // parse
            next = line.substring(line.indexOf("aufrom=\"") + 8, line.indexOf("\" />"));
            while (line.indexOf("<u ") != -1 && members.size() < number)
            {
                int x = line.indexOf("name=");
                int y = line.indexOf(" />", x);
                members.add(line.substring(x + 6, y - 1));
                line = line.substring(y + 2, line.length());
            }
 
            // short circuit
            in.close();
            if (members.size() == 0)
                break;
        }
        log(Level.INFO, "Successfully retrieved user list (" + number + " users starting at " + start + ")", "allUsers");
        return members.toArray(new String[0]);
    }
 
    /**
     *  Gets the user with the given username. Returns null if it doesn't
     *  exist.
     *  @param username a username
     *  @return the user with that username
     *  @since 0.05
     *  @throws IOException if a network error occurs
     */
    public User getUser(String username) throws IOException
    {
        return userExists(username) ? new User(username) : null;
    }
 
    /**
     *  Gets the user we are currently logged in as. If not logged in, returns
     *  null.
     *  @return the current logged in user
     *  @since 0.05
     */
    public User getCurrentUser()
    {
        return user;
    }
 
    /**
     *  Subclass for wiki users.
     *  @since 0.05
     */
    public class User implements Cloneable
    {
        private String username;
        private int rights = -484; // cache for userRights()
        private int count; // cache for isBlocked()
        private boolean blocked; // cache for isBlocked()
 
        /**
         *  Creates a new user object. Does not create a new user on the
         *  wiki (we don't implement this for a very good reason). Shouldn't
         *  be called for anons.
         *
         *  @param username the username of the user
         *  @since 0.05
         */
        protected User(String username)
        {
            this.username = username;
        }
 
        /**
         *  Gets a user's rights. Returns a bitmark of the user's rights.
         *  See fields above (be aware that IP_USER = -1, but you shouldn't
         *  be calling from a null object anyway). Uses the cached value for
         *  speed.
         *
         *  @return a bitwise mask of the user's rights.
         *  @throws IOException if a network error occurs
         *  @since 0.05
         */
        public int userRights() throws IOException
        {
            return userRights(true);
        }
 
        /**
         *  Gets a user's rights. Returns a bitmark of the user's rights.
         *  See fields above (be aware that IP_USER = -1, but you
         *  shouldn't be calling from a null object anyway). The value
         *  returned is cached which is returned by default, specify
         *  <tt>cache = false</tt> to retrieve a new one.
         *
         *  @return a bitwise mask of the user's rights.
         *  @throws IOException if a network error occurs
         *  @param cache whether we should use the cached value
         *  @since 0.07
         */
        public int userRights(boolean cache) throws IOException
        {
            // retrieve cache (if valid)
            if (cache && rights != -484)
                return rights;
 
            // begin
            String url = query + "action=query&list=users&usprop=groups&ususers=" + URLEncoder.encode(username, "UTF-8");
            URLConnection connection = new URL(url).openConnection();
            logurl(url, "User.userRights");
            checkLag("User.userRights");
            setCookies(connection, cookies);
            connection.connect();
 
            // read the first line, as it is the only thing worth paying attention to
            BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
            String line = in.readLine();
 
            // parse
            ArrayList<String> members = new ArrayList<String>();
            while (line.indexOf("<g>") != -1)
            {
                int x = line.indexOf("<g>");
                int y = line.indexOf("</g>");
                members.add(line.substring(x + 3, y));
                line = line.substring(y + 4, line.length());
            }
            log(Level.INFO, "Successfully retrived user rights for " + username, "User.userRights");
 
            int ret = 1;
            if (members.indexOf("sysop") != -1)
                ret += 4;
            if (members.indexOf("bureaucrat") != -1)
                ret += 8;
            if (members.indexOf("steward") != -1)
                ret += 16;
            if (members.indexOf("bot") != -1)
                ret += 32;
 
            // store
            rights += (ret + 484);
            in.close();
            return ret;
        }
 
        /**
         *  Gets this user's username. (Should have implemented this earlier).
         *  @return this user's username
         *  @since 0.08
         */
        public String getUsername()
        {
            return username;
        }
 
        /**
         *  Returns a log of the times when the user has been blocked.
         *  @return records of the occasions when this user has been blocked
         *  @throws IOException if something goes wrong
         *  @since 0.08
         */
        public LogEntry[] blockLog() throws IOException
        {
            return getLogEntries(null, null, Integer.MAX_VALUE, BLOCK_LOG, null, "User:" + username, USER_NAMESPACE);
        }
 
        /**
         *  Determines whether this user is blocked by looking it up on the IP
         *  block list.
         *  @return whether this user is blocked
         *  @throws IOException if we cannot retrieve the IP block list
         *  @since 0.12
         */
        public boolean isBlocked() throws IOException
        {
            // if logged in as this user, it's only worth fetching this every 100 edits or so
            if (this != user || count > 100)
            {
                blocked = getIPBlockList(username, null, null, 1).length != 0;
                count = 0;
            }
            else
                count++;
            return blocked;
        }
 
        /**
         *  Fetches the internal edit count for this user, which includes all
         *  live edits and deleted edits after (I think) January 2007.
         *
         *  @return the user's edit count
         *  @throws IOException if a network error occurs
         *  @since 0.16
         */
        public int countEdits() throws IOException
        {
            String url = query + "action=query&list=users&usprop=editcount&ususers=" + URLEncoder.encode(username, "UTF-8");
            URLConnection connection = new URL(url).openConnection();
            logurl(url, "User.countEdits");
            checkLag("User.countEdits");
            setCookies(connection, cookies);
            connection.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "UTF-8"));
            String line = in.readLine();
            int a = line.indexOf("editcount=\"") + 11;
            int b = line.indexOf("\"", a);
            return Integer.parseInt(line.substring(a, b));
        }
 
        /**
         *  Copies this user object.
         *  @return the copy
         *  @since 0.08
         */
        public Object clone()
        {
            return new User(username);
        }
 
        /**
         *   Tests whether this user is equal to another one.
         *   @return whether the users are equal
         *   @since 0.08
         */
        public boolean equals(Object x)
        {
            if (!(x instanceof User))
                return false;
            return username.equals(((User)x).username);
        }
    }
 
    /**
     *  A wrapper class for an entry in a wiki log, which represents an action
     *  performed on the wiki.
     *
     *  @see #getLogEntries
     *  @since 0.08
     */
    public class LogEntry
    {
        // internal data storage
        private String type;
        private String action;
        private String reason;
        private User user;
        private String target;
        private Calendar timestamp;
        private Object details;
 
        /**
         *  Creates a new log entry. WARNING: does not perform the action
         *  implied. Use Wiki.class methods to achieve this.
         *
         *  @param type the type of log entry, one of USER_CREATION_LOG,
         *  DELETION_LOG, BLOCK_LOG, etc.
         *  @param action the type of action that was performed e.g. "delete",
         *  "unblock", "overwrite", etc.
         *  @param reason why the action was performed
         *  @param user the user who performed the action
         *  @param target the target of the action
         *  @param timestamp the local time when the action was performed.
         *  We will convert this back into a Calendar.
         *  @param details the details of the action (e.g. the new title of
         *  the page after a move was performed).
         *  @since 0.08
         */
        protected LogEntry(String type, String action, String reason, User user, String target, String timestamp, Object details)
        {
            this.type = type;
            this.action = action;
            this.reason = reason;
            this.user = user;
            this.target = target;
            this.timestamp = timestampToCalendar(timestamp);
            this.details = details;
        }
 
        /**
         *  Gets the type of log that this entry is in.
         *  @return one of DELETION_LOG, USER_CREATION_LOG, BLOCK_LOG, etc.
         *  @since 0.08
         */
        public String getType()
        {
            return type;
        }
 
        /**
         *  Gets a string description of the action performed, for example
         *  "delete", "protect", "overwrite", ...
         *  @return the type of action performed
         *  @since 0.08
         */
        public String getAction()
        {
            return action;
        }
 
        /**
         *  Gets the reason supplied by the perfoming user when the action
         *  was performed.
         *  @return the reason the action was performed
         *  @since 0.08
         */
        public String getReason()
        {
            return reason;
        }
 
        /**
         *  Gets the user object representing who performed the action.
         *  @return the user who performed the action.
         *  @since 0.08
         */
        public User getUser()
        {
            return user;
        }
 
        /**
         *  Gets the target of the action represented by this log entry.
         *  @return the target of this log entry
         *  @since 0.08
         */
        public String getTarget()
        {
            return target;
        }
 
        /**
         *  Gets the timestamp of this log entry.
         *  @return the timestamp of this log entry
         *  @since 0.08
         */
        public Calendar getTimestamp()
        {
            return timestamp;
        }
 
        /**
         *  Gets the details of this log entry. Return values are as follows:
         *
         *  <table>
         *  <tr><th>Log type <th>Return value
         *  <tr><td>MOVE_LOG <td>The new page title
         *  <tr><td>USER_RENAME_LOG <td>The new username
         *  <tr><td>BLOCK_LOG <td>new Object[] { boolean anononly, boolean nocreate, boolean noautoblock, boolean noemail, String duration }
         *  <tr><td>USER_RIGHTS_LOG <td>The new user rights (Integer)
         *  <tr><td>Others <td>null
         *  </table>
         *
         *  Note that the duration of a block may be given as a period of time
         *  (e.g. "31 hours") or a timestamp (e.g. 20071216160302). To tell
         *  these apart, feed it into <tt>Long.parseLong()</tt> and catch any
         *  resulting exceptions.
         *
         *  @return the details of the log entry
         *  @since 0.08
         */
        public Object getDetails()
        {
            return details;
        }
 
        /**
         *  Returns a string representation of this log entry. This is similar to
         *  on-wiki logs, which are of the form {{timestamp}} {{user}} {{action}}
         *  {{target}} ({{reason}}). <!-- yes, this is crude, fixing it will
         *  add about 200 lines of code -->
         *
         *  @return a string representation of this object
         *  @since 0.08
         */
        public String toString()
        {
            StringBuilder s = new StringBuilder(timestamp.getTime().toString());
            s.append(" ");
            s.append(user.getUsername());
            s.append(" ");
            s.append(action);
            s.append(" ");
            s.append(target);
            s.append(" (");
            s.append(reason);
            s.append(")");
            return s.toString();
        }
    }
 
    // internals
 
    // miscellany
 
    /**
     *  Strips entity references like &quot; from the supplied string.
     *  @param in the string to remove URL encoding from
     *  @return that string without URL encoding
     *  @since 0.11
     */
    private String decode(String in)
    {
        // Remove entity references. Oddly enough, URLDecoder doesn't nuke these.
        in = in.replace("&lt;", "<").replace("&gt;", ">"); // html tags
        in = in.replace("&amp;", "&");
        in = in.replace("&quot;", "\"");
        in = in.replace("&#039;", "'");
        return in;
    }
 
    /**
     *  Same as <tt>parse()</tt>, but also strips out unwanted crap.
     *
     *  @param in the string to parse
     *  @return that string without the crap
     *  @throws IOException if a network error occurs
     *  @since 0.14
     */
    private String parseAndCleanup(String in) throws IOException
    {
        String output = parse(in);
        output = output.replace("<p>", "").replace("</p>", ""); // remove paragraph tags
        output = output.replace("\n", ""); // remove new lines
 
        // strip out the parser report, which comes at the end
        int a = output.indexOf("<!--");
        return output.substring(0, a);
    }
 
    /**
     *  Finalizes the object on garbage collection.
     *  @since 0.14
     */
    protected void finalize()
    {
        // I have no idea why this is called when getLogEntries() needs to make four
        // queries. Silly Java.
        logout();
        namespaces = null;
    }
 
    // user rights methods
 
    /**
     *  Checks whether the currently logged on user has sufficient rights to
     *  edit/move a protected page (this includes
     *
     *  @param level a protection level
     *  @param move whether the action is a move
     *  @return whether the user can perform the specified action
     *  @throws IOException if we can't get the user rights
     *  @throws AccountLockedException if user is blocked
     *  @throws AssertionError if any defined assertions are false
     *  @since 0.10
     */
    private boolean checkRights(int level, boolean move) throws IOException, AccountLockedException
    {
        // check our assertions
        assertions();
 
        // check if logged out
        if (user == null)
            return level == NO_PROTECTION;
 
        // check if blocked
        if (user.isBlocked())
        {
            AccountLockedException ex = new AccountLockedException("Permission denied: Current user is blocked!");
            logger.logp(Level.SEVERE, "Wiki", "upload()", "[" + domain + "] Cannot upload - user is blocked!.", ex);
            throw ex;
        }
 
        // admins can do anything, this also covers FULL_PROTECTION
        if ((user.userRights() & ADMIN)  == ADMIN)
            return true;
        switch (level)
        {
            case NO_PROTECTION:
            case SEMI_PROTECTION:
                return true;
            case MOVE_PROTECTION:
            case SEMI_AND_MOVE_PROTECTION:
                return !move; // fall through is OK: the user cannot move a protected page
            // cases PROTECTED_DELETED_PAGE and FULL_PROTECTION are unnecessary
            default:
                return false;
        }
    }
 
    /**
     *  Checks the database lag and blocks for 30s if it exceeds our current
     *  maximum value. This method is thread safe as it allows us to block
     *  all requests (read and write) if <tt>maxlag</tt> is exceeded.
     *  See [[mw:Manual:Maxlag parameter]] for the server-side analog.
     *
     *  @param method what we are doing at the moment
     *  @throws IOException if we cannot retrieve the database lag
     *  @since 0.11
     */
    private void checkLag(String method) throws IOException
    {
        if (maxlag < 1) // disabled
            return;
        // only bother to check every 60 seconds
        if ((System.currentTimeMillis() - lastlagcheck) < 30000) // TODO: this really should be a preference
            return;
 
        try
        {
            // if we use this, this can block unrelated read requests while we edit a page
            synchronized(domain)
            {
                // update counter. We do this before the actual check, so that only one thread does the check.
                lastlagcheck = System.currentTimeMillis();
                int lag = getCurrentDatabaseLag();
                while (lag > maxlag)
                {
                    log(Level.WARNING, "Sleeping for 30s as current database lag exceeds the maximum allowed value of " + maxlag + " s", method);
                    Thread.sleep(30000);
                    lag = getCurrentDatabaseLag();
                }
            }
        }
        catch (InterruptedException ex)
        {
            // nobody cares
        }
    }
 
    /**
     *  Performs assertions.
     *  @throws AssertionError if any assertions are false
     *  @see #setAssertionMode
     *  @since 0.11
     */
    private void assertions()
    {
        try
        {
            if (assertion == ASSERT_NONE)
                return;
            if ((assertion & ASSERT_LOGGED_IN) == ASSERT_LOGGED_IN)
                assert (user != null && cookies.containsValue(user.getUsername())): "Not logged in";
            if ((assertion & ASSERT_BOT) == ASSERT_BOT)
                assert (user.userRights() & BOT) == BOT : "Not a bot";
            if ((assertion & ASSERT_NO_MESSAGES) == ASSERT_NO_MESSAGES)
                // it's best if we avoid expensive API queries if possible
                assert !hasNewMessages() : "User has new messages";
        }
        catch (IOException ex)
        {
            throw new AssertionError(ex);
        }
    }
 
    // cookie methods
 
    /**
     *  Sets cookies to an unconnected URLConnection and enables gzip
     *  compression of returned text.
     *  @param u an unconnected URLConnection
     *  @param map the cookie store
     */
    private void setCookies(URLConnection u, Map map)
    {
        Iterator i = map.entrySet().iterator();
        StringBuilder cookie = new StringBuilder(100);
        while (i.hasNext())
        {
            Map.Entry entry = (Map.Entry)i.next();
            cookie.append(entry.getKey());
            cookie.append("=");
            cookie.append(entry.getValue());
            cookie.append("; ");
        }
        u.setRequestProperty("Cookie", cookie.toString());
 
        // enable gzip compression
        u.setRequestProperty("Accept-encoding", "gzip");
    }
 
    /**
     *  Grabs cookies from the URL connection provided.
     *  @param u an unconnected URLConnection
     *  @param map the cookie store
     */
    private void grabCookies(URLConnection u, Map map)
    {
        // reset the cookie store
        map.clear();
        String headerName = null;
        for (int i = 1; (headerName = u.getHeaderFieldKey(i)) != null; i++)
            if (headerName.equals("Set-Cookie"))
            {
                String cookie = u.getHeaderField(i);
 
                // _session cookies are for cookies2, otherwise this causes problems
                if (cookie.indexOf("_session") != -1 && map == cookies)
                    continue;
 
                cookie = cookie.substring(0, cookie.indexOf(";"));
                String name = cookie.substring(0, cookie.indexOf("="));
                String value = cookie.substring(cookie.indexOf("=") + 1, cookie.length());
                map.put(name, value);
            }
    }
 
    // logging methods
 
    /**
     *  Logs a successful result.
     *  @param text string the string to log
     *  @param method what we are currently doing
     *  @param level the level to log at
     *  @since 0.06
     */
    private void log(Level level, String text, String method)
    {
        StringBuilder sb = new StringBuilder(100);
        sb.append('[');
        sb.append(domain);
        sb.append("] ");
        sb.append(text);
        sb.append('.');
        logger.logp(level, "Wiki", method + "()", sb.toString());
    }
 
    /**
     *  Logs a url fetch.
     *  @param url the url we are fetching
     *  @param method what we are currently doing
     *  @since 0.08
     */
    private void logurl(String url, String method)
    {
        logger.logp(Level.FINE, "Wiki", method + "()", "Fetching URL " + url);
    }
 
    // calendar/timestamp methods
 
    /**
     *  Turns a calendar into a timestamp of the format yyyymmddhhmmss.
     *  @param c the calendar to convert
     *  @return the converted calendar
     *  @see #timestampToCalendar
     *  @since 0.08
     */
    private String calendarToTimestamp(Calendar c)
    {
        StringBuilder x = new StringBuilder();
        x.append(c.get(Calendar.YEAR));
        int i = c.get(Calendar.MONTH) + 1; // January == 0!
        if (i < 10)
            x.append("0"); // add a zero if required
        x.append(i);
        i = c.get(Calendar.DATE);
        if (i < 10)
            x.append("0");
        x.append(i);
        i = c.get(Calendar.HOUR_OF_DAY);
        if (i < 10)
            x.append("0");
        x.append(i);
        i = c.get(Calendar.MINUTE);
        if (i < 10)
            x.append("0");
        x.append(i);
        i = c.get(Calendar.SECOND);
        if (i < 10)
            x.append("0");
        x.append(i);
        return x.toString();
    }
 
    /**
     *  Turns a timestamp of the format yyyymmddhhmmss into a Calendar object.
     *  @param timestamp the timestamp to convert
     *  @return the converted Calendar
     *  @see #calendarToTimestamp
     *  @since 0.08
     */
    private Calendar timestampToCalendar(String timestamp)
    {
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        int year = Integer.parseInt(timestamp.substring(0, 4));
        int month = Integer.parseInt(timestamp.substring(4, 6)) - 1; // January == 0!
        int day = Integer.parseInt(timestamp.substring(6, 8));
        int hour = Integer.parseInt(timestamp.substring(8, 10));
        int minute = Integer.parseInt(timestamp.substring(10, 12));
        int second = Integer.parseInt(timestamp.substring(12, 14));
        calendar.set(year, month, day, hour, minute, second);
        return calendar;
    }
 
    /**
     *  Converts a timestamp of the form used by the API
     *  (yyyy-mm-ddThh:mm:ssZ) to the form
     *  yyyymmddhhmmss, which can be fed into <tt>timestampToCalendar()</tt>.
     *
     *  @param timestamp the timestamp to convert
     *  @return the converted timestamp
     *  @see #timestampToCalendar
     *  @since 0.12
     */
    private String convertTimestamp(String timestamp)
    {
        StringBuilder ts = new StringBuilder(timestamp.substring(0, 4));
        ts.append(timestamp.substring(5, 7));
        ts.append(timestamp.substring(8, 10));
        ts.append(timestamp.substring(11, 13));
        ts.append(timestamp.substring(14, 16));
        ts.append(timestamp.substring(17, 19));
        return ts.toString();
    }
 
    // serialization
 
    /**
     *  Writes this wiki to a file.
     *  @param out an ObjectOutputStream to write to
     *  @throws IOException if there are local IO problems
     *  @since 0.10
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.writeObject(user.getUsername());
        out.writeObject(cookies);
        out.writeInt(throttle);
        out.writeInt(maxlag);
        out.writeInt(assertion);
        out.writeObject(scriptPath);
        out.writeObject(domain);
        out.writeObject(namespaces);
    }
 
    /**
     *  Reads a copy of a wiki from a file.
     *  @param in an ObjectInputStream to read from
     *  @throws IOException if there are local IO problems
     *  @throws ClassNotFoundException if we can't recognize the input
     *  @since 0.10
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        String z = (String)in.readObject();
        user = new User(z);
        cookies = (HashMap)in.readObject();
        throttle = in.readInt();
        maxlag = in.readInt();
        assertion = in.readInt();
        scriptPath = (String)in.readObject();
        domain = (String)in.readObject();
        namespaces = (HashMap)in.readObject();
 
        // various other intializations
        cookies2 = new HashMap(10);
        base = "http://" + domain + scriptPath + "/index.php?title=";
        query = "http://" + domain + scriptPath + "/api.php?format=xml&";
    }
}
