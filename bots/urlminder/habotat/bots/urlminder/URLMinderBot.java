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

package habotat.bots.urlminder;

import java.io.*;
import java.net.*;
import java.util.*;

import org.jdom.*;

import habotat.api.*;

/**
 * @author Mark Lindner
 */

public class URLMinderBot extends Bot
  {
  private int saveTimer;
  private HashMap<String, ArrayList<WebLink>> urlLists;
  public static final int MAX_URLS = 20;
  private String helpText = "";
  private static final String NODE_USERS = "users", NODE_USER = "user",
    ATTR_NAME = "name", NODE_LINK = "link", ATTR_URL = "url",
    ATTR_TITLE = "title";

  /*
   */
  
  public URLMinderBot()
    {
    urlLists = new HashMap<String, ArrayList<WebLink>>();
    }

  /*
   */
  
  public void start()
    {
    try
      {
      loadData();
      }
    catch(BotException ex)
      {
      logError("Unable to load data", ex);
      }
    
    String kwd = getKeyword();

    helpText = "Commands:<br>"
      + "<b>" + kwd + " list</b> - list all URLs<br>"
      + "<b>" + kwd + " add</b> <i>url</i> [<i>title</i>] - add URL<br>"
      + "<b>" + kwd + " remove</b> <i>n</i> - remove URL #<i>n</i><br>"
      + "<b>" + kwd + " title</b> <i>n title</i> - set title for URL #<i>n</i><br>"
      + "<b>" + kwd + " purge</b> - remove all URLs";

    try
      {
      TimeSpec tspec = new TimeSpec();
      tspec.setAllHours();
      tspec.clearAllMinutes();
      tspec.setMinute(0);
      tspec.setAllDays();
      tspec.setAllMonths();
      tspec.setAllDaysOfWeek();

      saveTimer = createTimer(tspec);
      }
    catch(BotException ex)
      {
      }
    }

  /*
   */
  
  public void stop()
    {
    try
      {
      destroyTimer(saveTimer);
      }
    catch(BotException ex)
      {
      }
    
    try
      {
      saveData();
      }
    catch(BotException ex)
      {
      logError("Unable to save data", ex);
      }
    }

  /*
   */

  private ArrayList<WebLink> getList(String user)
    {
    ArrayList<WebLink> v = urlLists.get(user);
    if(v == null)
      {
      v = new ArrayList<WebLink>();
      urlLists.put(user, v);
      }

    return(v);
    }

  /*
   */
  
  protected void loadData(InputStream ins) throws BotException
    {
    logInfo("Loading data");
    
    try
      {
      Document data = readXML(ins);

      urlLists.clear();
      
      Element root = data.getRootElement();
      Element userNode = XMLUtils.getChildElem(root, NODE_USERS, true);
      List list = userNode.getChildren(NODE_USER);
      Iterator iter = list.iterator();
      while(iter.hasNext())
        {
        Element node = (Element)iter.next();

        String name = XMLUtils.getStringAttr(node, ATTR_NAME, null, true);
        ArrayList<WebLink> vec = getList(name);

        List list2 = node.getChildren(NODE_LINK);
        Iterator iter2 = list2.iterator();
        for(int i = 0; (i < MAX_URLS) && iter2.hasNext(); i++)
          {
          Element node2 = (Element)iter2.next();

          String url = XMLUtils.getStringAttr(node2, ATTR_URL, null, true);
          String title = XMLUtils.getStringAttr(node2, ATTR_TITLE);

          try
            {
            WebLink link = new WebLink(url, title);
            vec.add(link);
            }
          catch(MalformedURLException ex)
            { /* ignore! */ }
          }
        }
      }
    catch(InvalidXMLException ex)
      {
      throw(new BotException("Invalid XML data: " + ex.getMessage()));
      }
    catch(IOException ex)
      {
      throw(new BotException("Unable to load data", ex));
      }
    }

  /*
   */

  protected void saveData(OutputStream outs) throws BotException
    {
    logInfo("Saving data");

    Element root = new Element("botdata");
    Document doc = new Document(root);

    Element usersNode = new Element(NODE_USERS);
    root.addContent(usersNode);

    Iterator<String> iter = urlLists.keySet().iterator();
    while(iter.hasNext())
      {
      String name = iter.next();
      Element userNode = new Element(NODE_USER);
      XMLUtils.setStringAttr(userNode, ATTR_NAME, name);

      usersNode.addContent(userNode);

      ArrayList<WebLink> vec = urlLists.get(name);
      if(vec == null)
        continue;

      Iterator<WebLink> iter2 = vec.iterator();
      while(iter2.hasNext())
        {
        WebLink link = iter2.next();

        Element linkNode = new Element(NODE_LINK);
        XMLUtils.setStringAttr(linkNode, ATTR_URL, link.getURL().toString());

        String title = link.getTitle();
        if(title != null)
          XMLUtils.setStringAttr(linkNode, ATTR_TITLE, title);

        userNode.addContent(linkNode);
        }
      }

    try
      {
      writeXML(doc, outs);
      }
    catch(IOException ex)
      {
      throw(new BotException("Unable to save data", ex));
      }
    }

  /*
   */
  
  public void handleEvent(MessageEvent mevt)
    {
    try
      {
      String args[] = parseCommand(mevt.getMessage());

      if((args.length == 1) && "list".startsWith(args[0]))
        {
        ArrayList<WebLink> v = getList(mevt.getSender());
        StringBuffer sb = new StringBuffer();
        sb.append("URL List:");

        if(v.size() == 0)
          sb.append(" <i>empty</i>");
        else
          {
          int i = 0;
          Iterator<WebLink> iter = v.iterator();
          while(iter.hasNext())
            {
            sb.append("<br><b>");
            sb.append(String.valueOf(++i));
            sb.append("</b>. ");
            WebLink link = iter.next();
            sb.append(link.toHTML());
            }
          }
          
        sendMessage(mevt.getSender(), sb.toString());
        }
      else if((args.length == 2) && "remove".startsWith(args[0]))
        {
        int n = 0;

        try { n = Integer.parseInt(args[1]); }
        catch(NumberFormatException ex) { }

        ArrayList<WebLink> v = getList(mevt.getSender());
        if(n < 1 || n > v.size())
          sendMessage(mevt.getSender(), "Invalid URL number.");
        else
          {
          v.remove(--n);
          sendMessage(mevt.getSender(), "URL removed.");
          }
        }
      else if(((args.length == 2) || (args.length == 3))
              && "add".startsWith(args[0]))
        {
        ArrayList<WebLink> v = getList(mevt.getSender());
        if(v.size() == MAX_URLS)
          sendMessage(mevt.getSender(), "Sorry; at most " + MAX_URLS
                      + " can be in your list.");
        else
          {
          try
            {
            WebLink link = new WebLink(args[1],
                                       (args.length == 3) ? args[2] : null);
            v.add(link);
            sendMessage(mevt.getSender(), "URL added.");
            }
          catch(MalformedURLException ex)
            {
            sendMessage(mevt.getSender(), "That is not a valid URL.");
            }
          }
        }
      else if((args.length == 1) && "purge".startsWith(args[0]))
        {
        ArrayList<WebLink> v = getList(mevt.getSender());
        v.clear();
        sendMessage(mevt.getSender(), "All URLs removed.");
        }
      else if((args.length == 3) && "title".startsWith(args[0]))
        {
        int n = Integer.parseInt(args[1]);

        ArrayList<WebLink> v = getList(mevt.getSender());
        if(n < 1 || n > v.size())
          sendMessage(mevt.getSender(), "Invalid URL number.");
        else
          {
          WebLink link = v.get(--n);
          link.setTitle(args[2]);
            
          sendMessage(mevt.getSender(), "URL title changed.");
          }          
        }
        
      else
        sendMessage(mevt.getSender(), helpText);        
      }
    catch(BotException ex)
      {
      logError(ex);
      }
    }

  /*
   */
  
  public void handleEvent(TimerEvent tevt)
    {
    if(tevt.getTimerID() == saveTimer)
      {
      try
        {
        saveData();
        }
      catch(BotException ex)
        {
        logError("Unable to save data", ex);
        }
      }
    }
  
  }

/* end of source file */
