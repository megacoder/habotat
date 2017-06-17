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

import habotat.api.*;

import java.io.*;
import java.util.*;

import kiwi.event.*;
import kiwi.util.*;

/**
 *
 * @author Mark Lindner
 */

public class BotDef extends Def implements PluginReloadListener,
                                Comparable<BotDef>
  {
  private String name;
  private String keyword;
  private String description;
  private AccountDef account;
  private PluginDef plugin;
  private int maxSessions;
  private ArrayList<String> accessList = new ArrayList<String>();
  private boolean running = false;
  private Bot bot = null;
  private BotContext context = null;
  private boolean autoStart;
  private LoggingEndpoint log = null;

  /*
   */
  
  public BotDef()
    {
    }

  /*
   */
  
  public BotDef(String name, String keyword, String description,
                AccountDef account, PluginDef plugin, int maxSessions,
                boolean autoStart, String[] accessList)
    {
    this.name = name;
    this.keyword = keyword;
    this.description = description;
    this.account = account;
    this.plugin = plugin;
    this.autoStart = autoStart;
    this.maxSessions = maxSessions;

    for(int i = 0; i < accessList.length; i++)
      this.accessList.add(accessList[i]);

    this.plugin.getPlugin().addPluginReloadListener(this);
    }

  /*
   */

  void setContext(BotContext context)
    {
    this.context = context;
    }

  /*
   */

  void dispose()
    {
    stop();
    bot = null;
    }

  /*
   */
  
  public void createBot() throws BotException
    {
      try
        {
        bot = (Bot)plugin.getPlugin().newInstance();

//        System.out.println("context is: " + context);
        
        bot.setContext(context);

        if(bot.getName() != null)
          {
          bot.loadData();
          }
        }
      catch(Exception ex)
        {
        ex.printStackTrace();
        
        HabotatServer.logError("Error creating bot", ex);
        throw(new BotException(ex));
        }
    }

  /*
   */

  public void destroyBot()
    {
    if(bot != null)
      {
      try
        {
        bot.saveData();
        }
      catch(BotException ex)
        {
        HabotatServer.logError("Error while saving data", ex);
        }
      finally
        {
        bot.setContext(null);
        plugin.getPlugin().removePluginReloadListener(this);
        bot = null;
        }
      }
      
    }
  
  /*
   */

  public Bot getBot()
    {
    return(bot);
    }

  /*
   */

  public boolean isRunning()
    {
    return(running);
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

  public boolean getAutoStart()
    {
    return(autoStart);
    }

  /*
   */

  public void setAutoStart(boolean autoStart)
    {
    this.autoStart = autoStart;
    }

  /*
   */
  
  public String getKeyword()
    {
    return(keyword);
    }

  /*
   */
  
  public void setKeyword(String keyword)
    {
    this.keyword = keyword;
    }

  /*
   */
  
  public String getDescription()
    {
    return(description == null ? "" : description);
    }

  /*
   */
  
  public void setDescription(String description)
    {
    this.description = description;
    }

  /*
   */
  
  public AccountDef getAccount()
    {
    return(account);
    }

  /*
   */
  
  public void setAccount(AccountDef account)
    {
    this.account = account;
    }

  /*
   */
  
  public PluginDef getPlugin()
    {
    return(plugin);
    }

  /*
   */
  
  public void setPlugin(PluginDef plugin)
    {
    if(this.plugin != null)
      this.plugin.getPlugin().removePluginReloadListener(this);
    
    this.plugin = plugin;
    this.plugin.getPlugin().addPluginReloadListener(this);
    }

  /*
   */
  
  public int getMaxSessions()
    {
    return(maxSessions);
    }

  /*
   */
  
  public void setMaxSessions(int maxSessions)
    {
    this.maxSessions = maxSessions;
    }

  /*
   */
  
  public void clearAccessList()
    {
    accessList.clear();
    }

  /*
   */
  
  public void addUser(String user)
    {
    accessList.add(user);
    }

  /*
   */
  
  public boolean checkAccess(String user)
    {
    if(accessList.size() == 0)
      return(true);
    else
      return(accessList.contains(user));
    }

  /*
   */
  
  public String getType()
    {
    return(plugin.toString());
    }

  /*
   */
  
  public Iterator<String> getUsers()
    {
    return(accessList.iterator());
    }

  /*
   */
  
  public void start()
    {
    if(! running)
      {
      running = true;
      getBot().start(); 
      plugin.addUseReference();
      }
    }

  /*
   */

  public void stop()
    {
    if(running)
      {
      bot.stop();
      plugin.removeUseReference();
      running = false;
      }
    }

  /*
   */

  public void setLoggingEndpoint(LoggingEndpoint log)
    {
    this.log = log;
    }

  /*
   */

  public void pluginReloaded(PluginReloadEvent evt)
    {
    try
      {
      createBot();
      }
    catch(BotException ex)
      {
      HabotatServer.logError("Error creating bot", ex);
      }
    }

  /*
   */

  public void logInfo(String text)
    {
    if(log != null)
      log.logMessage(log.INFO, text);
    }

  /*
   */

  public void logWarning(String text)
    {
    if(log != null)
      log.logMessage(log.INFO, text);
    }

  /*
   */

  public void logError(String text)
    {
    if(log != null)
      log.logMessage(log.ERROR, text);
    }

  /*
   */

  public void saveData() throws BotException
    {
    bot.saveData();
    }

  /*
   */

  public int compareTo(BotDef other)
    {
    if(name == null)
      return(-1);
    
    return(name.compareTo(other.getName()));
    }
  
  }

/* end of source file */
