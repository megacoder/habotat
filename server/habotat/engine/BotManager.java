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
import java.net.*;
import java.util.*;

import kiwi.event.*;
import kiwi.io.*;
import kiwi.ui.model.*;
import kiwi.util.*;
import kiwi.util.plugin.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

import habotat.api.*;

/**
 * @author Mark Lindner
 */

public class BotManager implements SessionTimeoutObserver, IMListener,
                        SignOnListener
  {
  private HashMap<AccountDef, ArrayList<BotDef>> botMap
    = new HashMap<AccountDef, ArrayList<BotDef>>();
  private EventQueue eventQueue = new EventQueue();
  private Scheduler scheduler = new Scheduler(eventQueue);
  private XSLManager xslManager = new XSLManager();
  private HabotatServer server;
  private static final int FOCUS_TIMEOUT = 60000; // 1 min.
  private File botDataDir;
  private SAXBuilder xmlReader = new SAXBuilder();
  private static Format xmlFormat = Format.getPrettyFormat();
  private XMLOutputter xmlWriter = new XMLOutputter(xmlFormat);
  private KListModel bots;
  private HashMap<BotDef, SessionCache> sessionMap
    = new HashMap<BotDef, SessionCache>();
  private HashMap<String, BotDef> focusMap = new HashMap<String, BotDef>();
  private HashMap<String, ArrayList<BotDef>> signonMap
    = new HashMap<String, ArrayList<BotDef>>();
  private Thread sessionThread;

  static
    {
    xmlFormat.setLineSeparator(System.getProperty("line.separator"));
    }

  /*
   */
  
  BotManager(HabotatServer server)
    {
    this.server = server;

    botDataDir = server.getBotDataDirectory();

    bots = server.getBots();
    
    sessionThread = new Thread(new Runnable()
        {
        public void run()
          {
          for(;;)
            {
            KiwiUtils.sleep(60);
            long now = System.currentTimeMillis();
            
            Iterator<SessionCache> iter = sessionMap.values().iterator();
            while(iter.hasNext())
              {
              SessionCache cache = iter.next();
              cache.expire(now);
              }
            }
          }
        });

    sessionThread.setDaemon(true);
    sessionThread.start();
    }

  /*
   */

  private ArrayList<BotDef> getBotsForAccount(AccountDef acct)
    {
    ArrayList<BotDef> v = null;
    
    synchronized(botMap)
      {
      v = botMap.get(acct);
      if(v == null)
        {
        v = new ArrayList<BotDef>();
        botMap.put(acct, v);
        }
      }

    return(v);
    }

  /*
   */
  
  public void startBot(BotDef bot) throws IOException
    {
    AccountDef acct = bot.getAccount();

    if(! acct.isConnected())
      {
      acct.connect();
      acct.addIMListener(this);
      }

    synchronized(botMap)
      {
      ArrayList<BotDef> v = getBotsForAccount(acct);

      if(! v.contains(bot))
        v.add(bot);
      }

    // clear out the bots sessions

    synchronized(sessionMap)
      {
      SessionCache cache = sessionMap.get(bot);
      if(cache != null)
        cache.reset();
      else
        sessionMap.put(bot, new SessionCache(bot.getMaxSessions(),
                                             server.getSessionTimeout(),
                                             this));
      }

    // start the bot

    bot.start();
    }

  /*
   */
  
  public void stopBot(BotDef bot)
    {
    AccountDef acct = bot.getAccount();

    synchronized(botMap)
      {
      ArrayList<BotDef> v = getBotsForAccount(acct);
      v.remove(bot);
      
      bot.stop();
      
      scheduler.removeAllTimers(bot);
      }

    // clear out the bot sessions

    synchronized(sessionMap)
      {
      SessionCache cache = sessionMap.get(bot);
      if(cache != null)
        cache.reset();
      }
    }

  /*
   */

  public void startBots(AccountDef account) throws IOException
    {
    account.connect();
    account.addIMListener(this);
    
    KListModel bots = server.getBots();
    Iterator<BotDef> iter = bots.iterator();
    while(iter.hasNext())
      {
      BotDef bot = iter.next();
      if((bot.getAccount() == account) && bot.getAutoStart())
        startBot(bot);
      }
    }

  /**
   */
  
  public void stopBots(AccountDef account)
    {
    KListModel bots = server.getBots();

    Iterator<BotDef> iter = bots.iterator();
    while(iter.hasNext())
      {
      BotDef bot = iter.next();
      if((bot.getAccount() == account) && bot.getAutoStart())
        stopBot(bot);
      }

    account.disconnect();
    account.removeIMListener(this);
    }
  
  /*
   */

  public void stopAllBots()
    {
    KListModel bots = server.getBots();

    Iterator<BotDef> iter = bots.iterator();
    while(iter.hasNext())
      {
      BotDef bot = (BotDef)iter.next();
      stopBot(bot);
      }
    }

  /*
   */

  public void createContext(BotDef bot)
    {
    bot.setContext(new BotContextImpl(this, bot));
    }
  
  /*
   */

  public void sendMessage(BotDef bot, String target, String message)
    throws BotException
    {
    verifyBotRunning(bot);

    try
      {
      bot.getAccount().sendMessage(target, message);
      }
    catch(IOException ex)
      {
      HabotatServer.logError("Error sending IM", ex);
      throw(new BotException("Communication error"));
      }
    }

  /*
   */

  public int getMaxMessageLength(BotDef bot)
    {
    AccountDef acct = bot.getAccount();
    IMAdapter imadapter = acct.getAdapter();
      
    return(imadapter.getMaxMessageLength());
    }

  /*
   */
  
  public int createTimer(BotDef bot, TimeSpec spec, boolean repeating)
    throws BotException
    {
    verifyBotRunning(bot);
    
    return(scheduler.addTimer(bot, spec, repeating));
    }

  /*
   */

  public int createTimer(BotDef bot, Calendar time)
    throws BotException
    {
    verifyBotRunning(bot);

    TimeSpec spec = new TimeSpec(time);

    return(scheduler.addTimer(bot, spec, false));
    }

  /*
   */
  
  public void destroyTimer(BotDef bot, int id) throws BotException
    {
    verifyBotRunning(bot);
    
    scheduler.removeTimer(bot, id);
    }

  /*
   */

  public void destroyAllTimers(BotDef bot) throws BotException
    {
    verifyBotRunning(bot);
    
    scheduler.removeAllTimers(bot);
    }

  /*
   */
  
  private BotDef findBot(String keyword, ArrayList<BotDef> bots)
    {
    Iterator<BotDef> iter = bots.iterator();
    while(iter.hasNext())
      {
      BotDef bdef = iter.next();
      if(bdef.getKeyword().equalsIgnoreCase(keyword))
        return(bdef);
      }

    return(null);
    }

  /*
   */

  public void addStyleSheet(BotDef bot, String name, String data)
    throws BotException
    {
    xslManager.addStyleSheet(bot, name, data);    
    }

  /*
   */

  void addSystemStyleSheet(String name, String data)
    {
    xslManager.addSystemStyleSheet(name, data);
    }

  /*
   */

  public void removeStyleSheet(BotDef bot, String name)
    throws BotException
    {
    xslManager.removeStyleSheet(bot, name);
    }

  /*
   */

  public String applyStyleSheet(BotDef bot, String name, String data)
    throws BotException
    {
    verifyBotRunning(bot);
    
    return(xslManager.applyStyleSheet(bot, name, data,
                                      getMaxMessageLength(bot)));
    }

  /*
   */

  private void checkDataDir(File dir) throws IOException
    {
    if(! dir.exists())
      {
      if(! dir.mkdir())
        throw(new IOException("Unable to create data directory."));
      }
    else if(! (dir.isDirectory() && dir.canRead() && dir.canWrite()))
      throw(new IOException("Data path refers to a non-directory, or is not readable & writable."));
    }
  
  /*
   */

  InputStream getDataInputStream(BotDef bot) throws BotException, IOException
    {
    File botDir = new File(botDataDir, bot.getName());
    checkDataDir(botDir);

    InputStream ins = null;
    
    File botFile = new File(botDir, "data.hab");
    if(botFile.exists())
      ins = new BufferedInputStream(new FileInputStream(botFile));

    return(ins);
    }

  /*
   */
  
  OutputStream getDataOutputStream(BotDef bot) throws IOException
    {
    File botDir = new File(botDataDir, bot.getName());
    checkDataDir(botDir);

    File botFile = new File(botDir, "data.hab");
    if(! botFile.exists())
      if(! botFile.createNewFile())
        throw(new IOException("Unable to create data file"));

    OutputStream outs = new BackupFileOutputStream(botFile);

    return(outs);
    }
  
  /*
   */

  Document readXML(InputStream input) throws IOException, BotException
    {
    Document doc = null;

    try
      {
      doc = xmlReader.build(input);
      }
    catch(JDOMException ex)
      {
      throw(new BotException("Malformed XML data"));
      }

    return(doc);
    }

  /*
   */

  void writeXML(Document doc, OutputStream output) throws IOException
    {
    xmlWriter.output(doc, output);
    }

  /*
   */

  BotSession reserveSession(BotDef bot, String user) throws BotException
    {
    synchronized(sessionMap)
      {
      SessionCache cache = sessionMap.get(bot);
      return((BotSession)cache.reserve(user));
      }
    }

  /*
   */

  void releaseSession(BotDef bot, String user)
    {
    synchronized(sessionMap)
      {
      SessionCache cache = sessionMap.get(bot);
      cache.release(user);
      }
    }

  /*
   */

  BotSession fetchSession(BotDef bot, String user)
    {
    synchronized(sessionMap)
      {
      SessionCache cache = sessionMap.get(bot);

      return((BotSession)cache.fetch(user));
      }
    }
  
  /*
   */

  public void sessionTimedOut(String user)
    {
    HabotatServer.logInfo("User session for \"" + user + "\" timed out");

    BotDef lastBot = focusMap.get(user);
    if(lastBot != null)
      {
      try
        {
        sendMessage(lastBot, user, lastBot.getName() + " <i>- timed out.</i>");
        }
      catch(BotException ex) { }

      SessionTimeoutEvent sevt = new SessionTimeoutEvent(this, user);
      eventQueue.postEvent(sevt, lastBot);
      }
    }

  /*
   */

  public void messageReceived(IMEvent evt)
    {
    AccountDef acct = (AccountDef)evt.getSource();
    String from = evt.getSender();
    String msg = evt.getMessage();

    // dispatch to appropriate bot

    BotDef bot = null;
    ArrayList<BotDef> bots = getBotsForAccount(acct);
    
    String rest;

    BotDef lastBot = focusMap.get(from);
    if(lastBot != null)
      {
      synchronized(sessionMap)
        {
        SessionCache cache = sessionMap.get(lastBot);
        if(cache.contains(from))
          bot = lastBot;
        }
      }

    if(bot != null)
      {
      rest = msg;
      }
    else
      {
      // find the first whitespace character in the string, and then the
      // first non-whitespace character after that.
      
      int w1 = 0, w2 = 0;
      String keyword = "";
      
      rest = "";
      
      for(int i = 0; i < msg.length(); i++)
        if(Character.isWhitespace(msg.charAt(i)))
          {
          w1 = i;
          break;
          }
      
      if(w1 == 0)
        keyword = msg;
      else
        {
        for(int i = w1 + 1; i < msg.length(); i++)
          if(! Character.isWhitespace(msg.charAt(i)))
            {
            w2 = i;
            break;
            }
        
        keyword = msg.substring(0, w1);
        rest = msg.substring(w2);
        }
      
      /* extract the first word from the message. if it is 'help' or
       * '?', handle it internally. otherwise, find the bot whose
       * keyword matches that word, and route the rest of the message
       * to that bot.
       */

      bot = findBot(keyword, bots);
      
      if(bot != null)
        {
        focusMap.put(from, bot);
        }
      else
        {
        if(keyword.equals("?"))
          {
          StringBuffer sb = new StringBuffer();
          String surl = server.getURL();
          boolean hasURL = (! surl.equals(""));
          
          if(hasURL)
            {
            sb.append("<a href=\"");
            sb.append(surl);
            sb.append("\">");
            }
          sb.append(server.getName());
          if(hasURL)
            sb.append("</a>");
          
          sb.append("<br><i>");
          sb.append(server.getComments());
          sb.append("</i><br><br>Available services:");

          Iterator<BotDef> iter2 = bots.iterator();
          while(iter2.hasNext())
            {
            BotDef bd = iter2.next();
            sb.append("<br><b>");
            
            URL url = bd.getPlugin().getHelpURL();
            if(url != null)
              {
              sb.append("<a href=\"");
              sb.append(url.toString());
              sb.append("\">");
              }
            
            sb.append(bd.getKeyword());
            
            if(url != null)
              sb.append("</a>");
            
            sb.append("</b>");
            
            String desc = bd.getDescription();
            if(desc != null && ! desc.equals(""))
              {
              sb.append(" - ");
              sb.append(desc);
              }
            }
          
          String reply = sb.toString();
          
          try
            {
            acct.sendMessage(from, reply);
            }
          catch(IOException ex)
            {
            HabotatServer.logError(ex);
            }
          
          }
        else
          {
          try
            {
            acct.sendMessage(from, "Type <b>?</b> for a list of services provided by this IM gateway. To interact with a specific service, type its name.");
            }
          catch(IOException ex)
            {
            HabotatServer.logError(ex);
            }
          }
        
        return;
        }
      }
    
    if(! bot.checkAccess(from))
      {
      HabotatServer.logWarning(bot.getName() + " ignored IM from " + from);
      return; // ignore the message
      }
    
    MessageEvent bevt = new MessageEvent(this, rest, from);
    eventQueue.postEvent(bevt, bot);
    }

  /*
   */

  synchronized void addWatchedUser(BotDef bot, String user)
    throws BotException
    {
    verifyBotRunning(bot);

    AccountDef acct = bot.getAccount();
    
    acct.addWatchedUser(user);

    ArrayList<BotDef> v = signonMap.get(user);
    if(v == null)
      v = new ArrayList<BotDef>();

    v.add(bot);
    Collections.sort(v); // wrong; should insert based on keyword ordering.
    }

  /*
   */

  synchronized void removeWatchedUser(BotDef bot, String user)
    throws BotException
    {
    verifyBotRunning(bot);

    AccountDef acct = bot.getAccount();
    
    acct.removeWatchedUser(user);

    ArrayList<BotDef> v = signonMap.get(user);
    if(v != null)
      v.remove(bot);
    
    }

  /*
   */
  
  public void userStatusChanged(SignOnEvent evt)
    {
    String user = evt.getUser();

    ArrayList<BotDef> v = signonMap.get(user);
    if(v != null)
      {
      boolean online = evt.isOnline();
      
      Iterator<BotDef> iter = v.iterator();
      while(iter.hasNext())
        {
        BotDef bot = iter.next();

        UserStatusEvent uevt = new UserStatusEvent(this, user, online);
        eventQueue.postEvent(uevt, bot);
        }
      }
    }

  /*
   */

  private void verifyBotRunning(BotDef bot) throws BotException
    {
    if(! bot.isRunning())
      throw(new BotException("Bot is not running."));
    }
  
  }

/* end of source file */
