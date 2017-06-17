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

package habotat.bots.pubs;

import java.io.*;
import java.net.*;
import java.util.*;

import kiwi.ui.model.*;

import org.jdom.*;

import habotat.api.*;

/**
 * @author Mark Lindner
 */

public class PubsBot extends Bot
  {
  private int refreshTimer = 0;
  private RSSFeedCache<String> cache = new RSSFeedCache<String>();
  private String pubList = "";
  private DefaultKListModel feedModel;
  private PluginUI ui = null;
  private int refreshHours = 1;
  private int refreshMinutes = -1;
  private int refreshMinutesPast = 0;
  private static final String NODE_FEEDS = "feeds", NODE_FEED = "feed",
    NODE_REFRESH = "refresh", ATTR_KEYWORD = "keyword", ATTR_TITLE = "title",
    ATTR_URL = "url", ATTR_RSSTYPE = "rssType", ATTR_MINUTES = "minutes",
    ATTR_HOURS = "hours", ATTR_MIN_PAST = "minPast";
  private boolean running = false;

  /*
   */
  
  public PubsBot()
    {
    super();

    feedModel = new FeedList();

    try
      {
      feedModel.addItem(
        new RSSFeed(RSSFeed.RSS_OLD, "Slashdot", "slashdot",
                    new URL("http://slashdot.org/slashdot.rss")));

      feedModel.addItem(
        new RSSFeed(RSSFeed.RSS_NEW, "LA Weekly", "la-weekly",
                    new URL("http://interglacial.com/rss/la_weekly.rss")));

      feedModel.addItem(
        new RSSFeed(RSSFeed.RSS_NEW, "NOW with Bill Moyers", "bill-moyers",
                    new URL("http://interglacial.com/rss/now_with_bill_moyers.rss")));

      feedModel.addItem(
        new RSSFeed(RSSFeed.RSS_NEW, "The Smoking Gun", "smoking-gun",
                    new URL("http://rss.thesmokinggun.com/rss.asp")));

      feedModel.addItem(
        new RSSFeed(RSSFeed.RSS_NEW, "Homestar Runner", "strongbad",
                    new URL("http://interglacial.com/rss/homestar.rss")));

      feedModel.addItem(
        new RSSFeed(RSSFeed.RSS_NEW, "GameSpot PS2 News", "ps2",
                    new URL("http://www.gamespot.com/misc/rss/gamespot_updates_playstation_2.xml")));

      feedModel.addItem(
        new RSSFeed(RSSFeed.RSS_NEW, "CNET News.com", "cnet",
                    new URL("http://news.com.com/2547-1_3-0-5.xml")));
      }
    catch(Exception ex)
      {
      logError(ex);
      }

    updatePubsList();
    }

  /*
   */

  private void resetTimer()
    {
    try
      {
      if(refreshTimer != 0)
        destroyTimer(refreshTimer);

      TimeSpec tspec = new TimeSpec();
      tspec.clearAllHours();

      if(refreshHours != -1)
        {
        for(int h = 0; h < 24; h += refreshHours)
          tspec.setHour(h);

        tspec.setMinute(refreshMinutesPast);
        }

      tspec.clearAllMinutes();
      
      if(refreshMinutes != -1)
        {
        for(int m = 0; m < 60; m += refreshMinutes)
          tspec.setMinute(m);
        }
      else
        {
        tspec.setMinute(0);
        tspec.setAllHours();
        }

      tspec.setAllDays();
      tspec.setAllMonths();
      tspec.setAllDaysOfWeek();

      refreshTimer = createTimer(tspec);

      logInfo("refresh timer recreated");
      }
    catch(BotException ex)
      {
      logError(ex);
      }
    }

  /*
   */

  void updatePubsList()
    {
    Iterator iter = feedModel.getItems();
    while(iter.hasNext())
      {
      RSSFeed feed = (RSSFeed)iter.next();

      cache.put(feed.getKeyword(), feed);
      }

    StringBuffer sb = new StringBuffer();
    sb.append("Available Publications:");

    iter = feedModel.getItems();
    while(iter.hasNext())
      {
      sb.append("<br>");
      RSSFeed feed = (RSSFeed)iter.next();
      sb.append("<b>");
      sb.append(feed.getKeyword());
      sb.append("</b> - ");
      sb.append(feed.getTitle());
      }

    pubList = sb.toString();
    }
  
  /*
   */

  void setRefreshInterval(int refreshHours, int refreshMinutes,
                          int refreshMinutesPast)
    {
    this.refreshHours = refreshHours;
    this.refreshMinutes = refreshMinutes;
    this.refreshMinutesPast = refreshMinutesPast;

    if(running)
      resetTimer();
    }

  /*
   */
  
  int getRefreshHours()
    {
    return(refreshHours);
    }
  
  /*
   */
  
  int getRefreshMinutes()
    {
    return(refreshMinutes);
    }

  /*
   */
  
  int getRefreshMinutesPast()
    {
    return(refreshMinutesPast);
    }

  /**
   */

  protected void loadData(InputStream ins) throws BotException
    {
    logInfo("Loading data");
    
    String val;
    
    try
      {
      Document data = readXML(ins);
      Element root = data.getRootElement();

      int hours = -1, minutes = -1, minPast = 0;

      Element refreshNode = XMLUtils.getChildElem(root, NODE_REFRESH, true);

      hours = XMLUtils.getIntAttr(refreshNode, ATTR_HOURS, -1);
      minutes = XMLUtils.getIntAttr(refreshNode, ATTR_MINUTES, -1);
      minPast = XMLUtils.getIntAttr(refreshNode, ATTR_MIN_PAST, 0, true);

      setRefreshInterval(hours, minutes, minPast);
      
      feedModel.clear();

      Element feedsNode = XMLUtils.getChildElem(root, NODE_FEEDS, true);
      List list = feedsNode.getChildren(NODE_FEED);
      Iterator iter = list.iterator();
      while(iter.hasNext())
        {
        Element node = (Element)iter.next();

        String keyword = XMLUtils.getStringAttr(node, ATTR_KEYWORD, null,
                                                true);
        String title = XMLUtils.getStringAttr(node, ATTR_TITLE, null, true);
        String url = XMLUtils.getStringAttr(node, ATTR_URL, null, true);

        int type = XMLUtils.getIntAttr(node, ATTR_RSSTYPE, 0, true);

        try
          {
          RSSFeed feed = new RSSFeed(type, title, keyword, new URL(url));
          feedModel.addItem(feed);
          }
        catch(MalformedURLException ex) { /* ignore */ }
        }
      }
    catch(InvalidXMLException ex)
      {
      throw(new BotException("Invalid XML data: " + ex.getMessage()));
      }
    catch(IOException ex)
      {
      logError("Unable to load data", ex);
      }

    updatePubsList();
    }

  /**
   */

  protected void saveData(OutputStream outs) throws BotException
    {
    logInfo("Saving data");
    
    Element root = new Element("botdata");
    Document doc = new Document(root);

    Element refreshNode = new Element(NODE_REFRESH);
    if(refreshHours != -1)
      XMLUtils.setIntAttr(refreshNode, ATTR_HOURS, refreshHours);

    if(refreshMinutes != -1)
      XMLUtils.setIntAttr(refreshNode, ATTR_MINUTES, refreshMinutes);

    XMLUtils.setIntAttr(refreshNode, ATTR_MIN_PAST, refreshMinutesPast);
                               
    root.addContent(refreshNode);
    
    Element feedsNode = new Element(NODE_FEEDS);
    root.addContent(feedsNode);

    Iterator iter = feedModel.getItems();
    while(iter.hasNext())
      {
      RSSFeed feed = (RSSFeed)iter.next();

      Element node = new Element(NODE_FEED);

      XMLUtils.setStringAttr(node, ATTR_KEYWORD, feed.getKeyword());
      XMLUtils.setStringAttr(node, ATTR_TITLE, feed.getTitle());
      XMLUtils.setStringAttr(node, ATTR_URL, feed.getURL().toString());
      XMLUtils.setIntAttr(node, ATTR_RSSTYPE, feed.getType());

      feedsNode.addContent(node);
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

  /**
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

    running = true;

    resetTimer();
    }

  /**
   */
  
  public void stop()
    {
    try
      {
      if(refreshTimer != 0)
        destroyTimer(refreshTimer);
      }
    catch(BotException ex) { }

    try
      {
      saveData();
      }
    catch(BotException ex)
      {
      logError("Unable to save data", ex);
      }

    running = false;
    }

  /**
   */

  public PluginUI getUI()
    {
    if(ui == null)
      ui = new PubsBotUI(this);
    
    return(ui);
    }

  /*
   */
  
  private void sendNews(String target, String name) throws BotException
    {
    RSSFeed feed = cache.get(name);

    if(feed == null)
      sendMessage(target, "Sorry; I don't know anything about that. :-(");
    else
      {
      String news = feed.getContent();
      if(news == null)
        {
        logInfo("Fetching feed: " + feed.getTitle());
        String rawNews = fetchURL(feed.getURL());

        int type = feed.getType();
        String xsl = null;
        if(type == RSSFeed.RSS_OLD)
          xsl = "rdf";
        else
          xsl = "rss";
        
        news = applyTransform(xsl, rawNews);

        feed.setContent(news);
        }

      sendMessage(target, news);
      }
    }

  /*
   */

  void flushCache()
    {
    logInfo("Clearing news cache.");
    Iterator iter = feedModel.getItems();
    while(iter.hasNext())
      {
      RSSFeed feed = (RSSFeed)iter.next();
      feed.setContent(null);
      }
    }

  /*
   */
  
  public void handleEvent(MessageEvent mevt)
    {
    try
      {
      String msg = mevt.getMessage().trim();
      if(msg.equals(""))
        {
        sendMessage(mevt.getSender(), "Commands:<br><b>" + getKeyword()
                    + "</b> <i>publication</i> - Fetch headlines for <i>publication</i><br><b>"
                    + getKeyword() + " ?</b> - List available publications");
        }
      else if(msg.equals("?"))
        sendMessage(mevt.getSender(), pubList);
      else
        sendNews(mevt.getSender(), msg);
      }
    catch(Exception ex)
      {
      logError(ex);
      }
    }

  /*
   */
  
  public void handleEvent(TimerEvent tevt)
    {
    if(tevt.getTimerID() == refreshTimer)
      flushCache();
    }
  
  /*
   */
  
  KListModel getFeeds()
    {
    return(feedModel);
    }
  
  }

/* end of source file */
