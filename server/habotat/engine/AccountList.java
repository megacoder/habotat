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

import habotat.api.*;

/**
 *
 * @author Mark Lindner
 */

public class AccountList extends DefaultKListModel<AccountDef>
  {
  private Document document;
  private Element baseNode;
  private PluginList adapters;
  private static final String NODE_ACCOUNTS = "accounts",
    NODE_ACCOUNT = "account", NODE_CONFIG = "config";
  private static final String ATTR_SCREEN_NAME = "screenName",
    ATTR_PASSWORD = "password", ATTR_ADAPTER = "adapter", ATTR_INFO = "info";
  private static final String columns[] = { " ", "Screen Name", "Protocol" };
  private static final Class types[] = { Boolean.class, String.class,
                                         String.class };
  
  /*
   */
  
  AccountList()
    {
    }

  /*
   */

  public void addItem(AccountDef adef)
    {
    int i;
    String name = adef.getScreenName();
    
    for(i = 0; i < getItemCount(); i++)
      {
      AccountDef a = (AccountDef)getItemAt(i);

      int d = a.getScreenName().compareToIgnoreCase(name);
      if(d >= 0)
        break;
      }

    super.insertItemAt(adef, i);
    }

  /*
   */

  public void removeItem(AccountDef adef)
    {
    adef.setPlugin(null);
    
    super.removeItem(adef);
    }

  /*
   */

  void read(Document document, PluginList adapters)
    {
    this.document = document;
    Element root = document.getRootElement();
    baseNode = root.getChild(NODE_ACCOUNTS);

    this.adapters = adapters;

    if(baseNode == null)
      {
      baseNode = new Element(NODE_ACCOUNTS);
      root.addContent(baseNode);
      }
    
    clear();

    List list = baseNode.getChildren(NODE_ACCOUNT);
    Iterator iter = list.iterator();
    while(iter.hasNext())
      {
      Element node = (Element)iter.next();

      String screenName = node.getAttributeValue(ATTR_SCREEN_NAME);
      String password = node.getAttributeValue(ATTR_PASSWORD);
      String info = node.getAttributeValue(ATTR_INFO);
      String pluginName = node.getAttributeValue(ATTR_ADAPTER);

      PluginDef adapter = adapters.findPlugin(pluginName);

      AccountDef account = new AccountDef(adapter, screenName, password, info);

      try
        {
        Element cfgNode = XMLUtils.getChildElem(node, NODE_CONFIG);
        if(cfgNode != null)
          account.getAdapter().readSettings(cfgNode);
        }
      catch(InvalidXMLException ex) {}

      addItem(account);
      }
    }

  /*
   */

  void write()
    {
    baseNode.getChildren().clear();

    Iterator<AccountDef> iter = iterator();
    while(iter.hasNext())
      {
      AccountDef account = iter.next();

      Element node = new Element(NODE_ACCOUNT);
      XMLUtils.setStringAttr(node, ATTR_SCREEN_NAME, account.getScreenName());
      XMLUtils.setStringAttr(node, ATTR_PASSWORD, account.getPassword());
      XMLUtils.setStringAttr(node, ATTR_ADAPTER,
                             account.getPlugin().getName());

      String info = account.getComment();
      if(info != null)
        XMLUtils.setStringAttr(node, ATTR_INFO, info);

      Element cfgNode = new Element(NODE_CONFIG);
      account.getAdapter().writeSettings(cfgNode);
      node.addContent(cfgNode);

      baseNode.addContent(node);
      }
    }

  /*
   */

  public AccountDef findAccount(String screenName, PluginDef adapter)
    {
    Iterator<AccountDef> iter = iterator();
    while(iter.hasNext())
      {
      AccountDef account = iter.next();
      if(account.getScreenName().equals(screenName)
         && (account.getPlugin() == adapter))
        return(account);
      }
    return(null);
    }

  /*
   */

  public Object getValueForProperty(AccountDef account, String property)
    {
    if(account == null)
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
      if(property.equals(" "))
        return(account.isConnected() ? Boolean.TRUE : Boolean.FALSE);
      else if(property.equals("Screen Name"))
        return(account.getScreenName());
      else if(property.equals("Protocol"))
        return(account.getPlugin().getName());
      else
        return(null);
      }
    }
    
  }

/* end of source file */
