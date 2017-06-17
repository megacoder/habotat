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

import org.jdom.*;

import kiwi.ui.model.*;
import kiwi.util.*;
import kiwi.util.plugin.*;

import habotat.api.BotException;

/**
 *
 * @author Mark Lindner
 */

public class BotList extends DefaultKListModel<BotDef>
  {
  private Document document;
  private Element baseNode;
  private AccountList accounts;
  private PluginList plugins, adapters;
  private static final String NODE_BOTS = "bots", NODE_BOT = "bot",
    ATTR_NAME = "name", ATTR_KEYWORD = "keyword", ATTR_DESC = "description",
    ATTR_PLUGIN = "plugin", ATTR_ACCOUNT = "account", ATTR_ADAPTER = "adapter",
    ATTR_AUTOSTART ="autoStart", ATTR_MAX_SESSIONS = "maxSessions";
  private static final String columns[] = { " ", "Name", "Type", "Account" };
  private static final Class types[] = { Boolean.class, String.class,
                                         String.class, String.class };

  /*
   */
  
  BotList()
    {
    }

  /*
   */
  
  public void addItem(BotDef bdef)
    {
    PluginDef pdef = bdef.getPlugin();
    if(pdef != null)
      pdef.addReference();

    AccountDef adef = bdef.getAccount();
    if(adef != null)
      adef.addReference();

    int i;
    String name = bdef.getName();
    
    for(i = 0; i < getItemCount(); i++)
      {
      BotDef b = (BotDef)getItemAt(i);

      int d = b.getName().compareToIgnoreCase(name);
      if(d >= 0)
        break;
      }

    super.insertItemAt(bdef, i);
    }

  /*
   */

  public void removeItem(BotDef bdef)
    {
    PluginDef pdef = bdef.getPlugin();
    if(pdef != null)
      pdef.removeReference();

    AccountDef adef = bdef.getAccount();
    if(adef != null)
      adef.removeReference();
    
    super.removeItem(bdef);

    bdef.dispose();
    }
  
  /*
   */
  
  void read(Document document, AccountList accounts, PluginList plugins,
            PluginList adapters, BotManager manager)
    {
    this.document = document;
    Element root = document.getRootElement();
    baseNode = root.getChild(NODE_BOTS);

    this.accounts = accounts;
    this.plugins = plugins;
    this.adapters = adapters;

    if(baseNode == null)
      {
      baseNode = new Element(NODE_BOTS);
      root.addContent(baseNode);
      }
    
    clear();

    List list = baseNode.getChildren(NODE_BOT);
    Iterator iter = list.iterator();
    while(iter.hasNext())
      {
      Element node = (Element)iter.next();

      String name = node.getAttributeValue(ATTR_NAME);
      String keyword = node.getAttributeValue(ATTR_KEYWORD);
      String desc = node.getAttributeValue(ATTR_DESC);
      String pluginName = node.getAttributeValue(ATTR_PLUGIN);
      String accountName = node.getAttributeValue(ATTR_ACCOUNT);
      String adapterName = node.getAttributeValue(ATTR_ADAPTER);
      int maxSessions = Integer.parseInt(node.getAttributeValue(
                                           ATTR_MAX_SESSIONS));

      String val = node.getAttributeValue(ATTR_AUTOSTART);
      boolean autoStart = (val == null ? false : val.equalsIgnoreCase("true"));

      PluginDef adapter = adapters.findPlugin(adapterName);
      if(adapter == null)
        continue;
      
      AccountDef account = accounts.findAccount(accountName, adapter);
      PluginDef plugin = plugins.findPlugin(pluginName);

      String users[] = StringUtils.split(node.getText(), " \n");
      
      BotDef bot = new BotDef(name, keyword, desc, account, plugin,
                              maxSessions, autoStart, users);

      try
        {
        manager.createContext(bot);
        bot.createBot();

        addItem(bot);
        }
      catch(BotException ex)
        {
        HabotatServer.logError("Error creating bot " + name, ex);
        }
      }
    }

  /*
   */

  void write()
    {
    baseNode.getChildren().clear();

    Iterator<BotDef> iter = iterator();
    while(iter.hasNext())
      {
      BotDef bot = iter.next();

      Element node = new Element(NODE_BOT);
      node.setAttribute(ATTR_NAME, bot.getName());
      node.setAttribute(ATTR_KEYWORD, bot.getKeyword());
      node.setAttribute(ATTR_DESC, bot.getDescription());
      node.setAttribute(ATTR_PLUGIN, bot.getPlugin().getName());
      node.setAttribute(ATTR_ACCOUNT, bot.getAccount().getScreenName());
      node.setAttribute(ATTR_ADAPTER, bot.getAccount().getPlugin().getName());
      node.setAttribute(ATTR_MAX_SESSIONS,
                        String.valueOf(bot.getMaxSessions()));
      node.setAttribute(ATTR_AUTOSTART, bot.getAutoStart() ? "true" : "false");

      Iterator<String> iter2 = bot.getUsers();
      StringBuffer sb = new StringBuffer();
      while(iter2.hasNext())
        {
        sb.append(iter2.next());
        sb.append(' ');
        }

      node.setText(sb.toString());

      baseNode.addContent(node);
      }
    }

  /*
   */

  public BotDef findBot(String name)
    {
    Iterator<BotDef> iter = iterator();
    while(iter.hasNext())
      {
      BotDef bot = iter.next();
      if(bot.getName().equals(name))
        return(bot);
      }

    return(null);
    }
  
  /*
   */

  public Object getValueForProperty(BotDef bot, String property)
    {
    if(bot == null)
      {
      if(property.equals(COLUMN_NAMES_PROPERTY))
        return(columns);

      else if(property.equals(COLUMN_TYPES_PROPERTY))
        return(types);

      else
        return(null);
    }
    else
      {
      if(property.equals(columns[0]))
        return(bot.isRunning() ? Boolean.TRUE : Boolean.FALSE);
      else if(property.equals(columns[1]))
        return(bot.getName());
      else if(property.equals(columns[2]))
        return(bot.getType());
      else if(property.equals(columns[3]))
        return(bot.getAccount().toString());
      else
        return(null);
      }
    }
  }

/* end of source file */
