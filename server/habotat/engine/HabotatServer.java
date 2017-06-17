/* ----------------------------------------------------------------------------
   Habotat
   Copyright (C) 2004-05 Mark A. Lindner

   This file is part of Habotat.
   
   This program is free software; you can redistribute it and/or modify it
   under the terms of the GNU General Public License as published by the
   Free Software Foundation; either version 2 of the License, or (at your
   option) any later version.

   This program is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.

   You should have received a copy of the GNU General Public License along
   with this program; if not, write to the Free Software Foundation, Inc.,
   59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 
   The author may be contacted at <mark_a_lindner@yahoo.com>
   ----------------------------------------------------------------------------
*/

package habotat.engine;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;

import kiwi.io.*;
import kiwi.ui.model.*;
import kiwi.util.*;
import kiwi.util.plugin.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

/**
 * @author Mark Lindner
 */

public class HabotatServer implements PluginContext
  {
  private ConfigFile config;
  private AccountList accountModel = new AccountList();
  private PluginList pluginModel = new PluginList(NODE_PLUGINS, NODE_PLUGIN,
                                                  "HabotatBot");
  private PluginList adapterModel = new PluginList(NODE_ADAPTERS,
                                                   NODE_ADAPTER,
                                                   "HabotatAdapter");
  private BotList botModel = new BotList();
  private Document data;
  private SAXBuilder xmlReader = new SAXBuilder();
  private static Format xmlFormat = Format.getPrettyFormat();
  private XMLOutputter xmlWriter = new XMLOutputter(xmlFormat);
  private File dataFile;
  private File dataDir, botDataDir;
  private BotManager botManager;
  private PluginLocator pluginLocator;
  private Element rootNode;
  private String name = "";
  private String comments = "";
  private String url = "";
  private ResourceManager resmgr;
  private static final String NODE_ROOT = "habotat", NODE_PLUGINS = "plugins",
    NODE_PLUGIN = "plugin", NODE_ADAPTERS = "adapters",
    NODE_ADAPTER = "adapter", ATTR_NAME = "name", ATTR_URL = "url",
    ATTR_COMMENTS = "comments", ATTR_SESSION_TIMEOUT = "sessionTimeout";
  private int sessionTimeout = 2;
  public static LoggingEndpoint log;

  static
    {
    xmlFormat.setLineSeparator(System.getProperty("line.separator"));
    }
  
  /*
   */

  public HabotatServer(File dataDir) throws IOException
    {
    this.dataDir = dataDir;

    botDataDir = new File(dataDir, "botdata");
    if(! botDataDir.exists())
      botDataDir.mkdir();
    else if(!(botDataDir.isDirectory() && botDataDir.canRead()
              && botDataDir.canWrite()))
      {
      throw new IOException(botDataDir.getAbsolutePath()
                            + " is not a readable & writable directory.");
      }

    pluginLocator = new PluginLocator(this);
    dataFile = new File(dataDir, "data.xml");
    
    try
      {
      data = xmlReader.build(dataFile);
      }
    catch(FileNotFoundException ex)
      {
      data = new Document();
      data.setRootElement(new Element(NODE_ROOT));
      }
    catch(Exception ex)
      {
      ex.printStackTrace();
      }

    botManager = new BotManager(this);

    // load the XSL's

    resmgr = new ResourceManager(getClass());

    try
      {
      InputStream ins = resmgr.getStream("xsl/rdf.xsl");
      String xsl = StreamUtils.readStreamToString(ins);
      ins.close();

      botManager.addSystemStyleSheet("rdf", xsl);

      ins = resmgr.getStream("xsl/rss.xsl");
      xsl = StreamUtils.readStreamToString(ins);
      ins.close();

      botManager.addSystemStyleSheet("rss", xsl);
      }
    catch(Exception ex)
      {
      ex.printStackTrace();
      }

    // load the data
      
    rootNode = data.getRootElement();
    if(rootNode == null)
      {
      rootNode = new Element(NODE_ROOT);
      data.setRootElement(rootNode);
      }
    
    name = rootNode.getAttributeValue(ATTR_NAME);
    if(name == null)
      name = "";
    
    url = rootNode.getAttributeValue(ATTR_URL);
    if(url == null)
      url = "";
    
    comments = rootNode.getAttributeValue(ATTR_COMMENTS);
    if(comments == null)
      comments = "";

    String val = rootNode.getAttributeValue(ATTR_SESSION_TIMEOUT);
    if(val != null)
      {
      int x = Integer.parseInt(val);
      if((x >= 0) && (x <= 30))
        sessionTimeout = x;
      }

    }

  public void loadData()
    {
    // load in adapters, accounts, plugins, and bots

    adapterModel.read(data, pluginLocator);
    accountModel.read(data, adapterModel);
    pluginModel.read(data, pluginLocator);
    botModel.read(data, accountModel, pluginModel, adapterModel, botManager);
    }

  /*
   */

  public PluginList getAdapters()
    {
    return(adapterModel);
    }
  
  /*
   */
  
  public AccountList getAccounts()
    {
    return(accountModel);
    }

  /*
   */
  
  public PluginList getPlugins()
    {
    return(pluginModel);
    }

  /*
   */
  
  public BotList getBots()
    {
    return(botModel);
    }

  /*
   */
  
  public String getName()
    {
    return(name);
    }

  /*
   */
  
  public void setName(String name)
    {
    this.name = name;
    }

  /*
   */
  
  public String getURL()
    {
    return(url);
    }

  /*
   */
  
  public void setURL(String url)
    {
    this.url = url;
    }

  /*
   */
  
  public String getComments()
    {
    return(comments);
    }

  /*
   */
  
  public void setComments(String comments)
    {
    this.comments = comments;
    }

  /*
   */

  public void setSessionTimeout(int sessionTimeout)
    {
    this.sessionTimeout = sessionTimeout;
    }

  /*
   */

  public int getSessionTimeout()
    {
    return(sessionTimeout);
    }
  
  /*
   */

  public void saveData()
    {
    adapterModel.write();
    accountModel.write();
    pluginModel.write();
    botModel.write();

    rootNode.setAttribute(ATTR_NAME, name);
    rootNode.setAttribute(ATTR_URL, url);
    rootNode.setAttribute(ATTR_COMMENTS, comments);
    rootNode.setAttribute(ATTR_SESSION_TIMEOUT,
                          String.valueOf(sessionTimeout));
    
    try
      {
      FileOutputStream fout = new FileOutputStream(dataFile);

      xmlWriter.output(data, fout);

      fout.close();
      }
    catch(IOException ex)
      {
      ex.printStackTrace();
      }
    }
  
  /*
   */

  public void showMessage(String text)
    {
    }

  /*
   */
  
  public void showStatus(String text)
    {
    }

  /*
   */
  
  public boolean showQuestion(String text)
    {
    return(false);
    }

  /*
   */
  
  public Plugin loadPlugin(File jarFile) throws FileNotFoundException,
    IOException, PluginException
    {
    return(pluginLocator.loadPlugin(jarFile, "HabotatBot"));
    }

  /*
   */

  public Plugin loadAdapter(File jarFile) throws FileNotFoundException,
    IOException, PluginException
    {
    return(pluginLocator.loadPlugin(jarFile, "HabotatAdapter"));
    }

  
  /*
   */
  
  public BotManager getBotManager()
    {
    return(botManager);
    }

  /*
   */
  
  public File getBotDataDirectory()
    {
    return(botDataDir);
    }

  /*
   */

  static void logInfo(String text)
    {
    if(log != null)
      log.logMessage(LoggingEndpoint.INFO, text);
    }

  static void logWarning(String text)
    {
    if(log != null)
      log.logMessage(LoggingEndpoint.WARNING, text);
    }
  
  static void logError(String text)
    {
    if(log != null)
      log.logMessage(LoggingEndpoint.ERROR, text);
    }

  static void logError(Throwable t)
    {
    logError(null, t);
    }

  static void logError(String text, Throwable t)
    {
    StringBuffer sb = new StringBuffer();
    if(text != null)
      {
      sb.append(text);
      sb.append('\n');
      }

    sb.append(t.getClass().getName());
    sb.append(':');

    String msg = t.getMessage();
    sb.append(msg == null ? "(no message)" : msg);

    sb.append('\n');
    sb.append(KiwiUtils.stackTraceToString(t));
    
    log.logMessage(LoggingEndpoint.ERROR, sb.toString());
    }

  }

/* end of source file */
