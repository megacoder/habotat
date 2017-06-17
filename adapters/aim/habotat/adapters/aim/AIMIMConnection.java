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

package habotat.adapters.aim;

import java.io.*;
import java.util.*;

import kiwi.text.*;

import habotat.api.*;

import org.walluck.oscar.*;
import org.walluck.oscar.client.*;
import org.walluck.oscar.tools.*;
import org.walluck.oscar.handlers.*;
import org.walluck.oscar.handlers.icq.*;
import org.walluck.oscar.requests.*;
import org.walluck.oscar.channel.aolim.AOLIM;


/**
 * @author Mark Lindner
 *
 */

public class AIMIMConnection extends IMConnection
  {
  private _Adapter adapter;
  private StringBuffer buf = new StringBuffer(256);
  private AIMSession session;
//  private AIMConnection conn;
  private ICBMTool msgtool;
  private LoginTool logtool;
  private BuddyListTool budtool;
  private static final String DEFAULT_GROUP = "Buddies";
  private ArrayList buddyGroups = new ArrayList();
  private HashMap<String, Boolean> watchList;

  /*
   */
  
  AIMIMConnection(AIMSession session)
    {
    this.session = session;
    
    adapter = new _Adapter();
    
    msgtool = (ICBMTool) session.getTool(SNACFamily.AIM_CB_FAM_MSG);
    msgtool.addListener(adapter);
    
    logtool = (LoginTool) session.getTool(SNACFamily.AIM_CB_FAM_ATH);
//    logtool.addLoginListener(adapter);
    
    budtool = (BuddyListTool) session.getTool(SNACFamily.AIM_CB_FAM_BUD);
    budtool.addListener(adapter);

    watchList = new HashMap<String, Boolean>();
    }

  /**
   */
  
  public void close()
    {
    session.kill();
    session = null;
    }

  /**
   */

  private synchronized int findGroupInBuddyList(String group)
    {
    int j = 0;
    
    for (Iterator i = buddyGroups.iterator(); i.hasNext(); j++)
      {
      BuddyGroup buddyGroup = (BuddyGroup)i.next();
      
      if (AIMUtil.snCmp(buddyGroup.getName(), group))
        return j;
      }
    
    return(-1);
    }
  
  /*
   */
  
  public void addWatchedUser(String user) throws IMException
    {
    if(watchList.containsKey(user))
      return;

    watchList.put(user, Boolean.FALSE);
    
    try
      {
      SSIHandler ssi = (SSIHandler)session.getHandler(
        SNACFamily.AIM_CB_FAM_SSI);

      if(session.getSSI().getReceivedData()
         && (ssi.itemListExists(session.getSSI().getLocal(), user) == null))
        {
        ssi.addBuddy(session, user, DEFAULT_GROUP, (String) null,
                     (String) null, (String) null, false);
        }
      }
    catch(IOException ex)
      {
      throw(new IMException(ex.getMessage()));
      }
    }

  /**
   */
  
  public void removeWatchedUser(String user) throws IMException
    {
    if(! watchList.containsKey(user))
      return;

    watchList.remove(user);

    if(session == null)
      return;
    
    try
      {
      SSIHandler ssi = (SSIHandler)session
        .getHandler(SNACFamily.AIM_CB_FAM_SSI);

      if(session.getSSI().getReceivedData())
        {
        ssi.delBuddy(session, user, DEFAULT_GROUP);
        }
      }
    catch(Exception ex)
      {
      throw(new IMException(ex.getMessage()));
      }
    }

  /**
   */
  
  public void sendMessage(String user, String message) throws IMException
    {
    try
      {
      msgtool.sendIM(user, message, 0);
      }
    catch(IOException ex)
      {
      throw(new IMException(ex.getMessage()));
      }
    }

  /*
   */

  private class _Adapter implements DaimMsgListener, DaimBuddyListener,
                         DaimLoginListener
    {

    public void incomingIM(Buddy buddy, UserInfo from, AOLIM args)
      {
      String text = args.getMsg();

//      System.out.println("message came in: " + text);

      if(text.indexOf('<') >= 0)
        {
        StringBuffer buf = new StringBuffer();
        HTMLStripper s = new HTMLStripper(text, buf);
        
        try
          {
          s.parse();
          }
        catch(IOException ex) { }
        
        s.dispose();
        text = buf.toString();
        }

      fireMessageReceived(from.getSN(), text);
      }

    public void joinRoomRequest(JoinRoomRequest jrr)
      {
      }
    
    public void typingNotification(String sn, short typing)
      {
      }

    public void incomingICQ(UserInfo from, int uin, int args, String message)
      {
      }

    public void receivedURL(UserInfo from, int uin, String url,
                            String description, boolean massmessage)
      {
      }

    public void receivedContacts(UserInfo from, int uin, Map contact,
                                 boolean massmessage)
      {
      }

    public void receivedICQSMS(UserInfo from, int uin, ICQSMSMessage message,
                               boolean massmessage)
      {
      }

    public void buddyOffline(String sn, Buddy buddy)
      {
      if(watchList.get(sn) == Boolean.TRUE)
        {
        watchList.put(sn, Boolean.FALSE);
        fireUserSignedOff(sn);
        // System.out.println("user signed off: " + sn);
        }
      }
    
    public void buddyOnline(String sn, Buddy buddy)
      {
      if(watchList.get(sn) == Boolean.FALSE)
        {
        watchList.put(sn, Boolean.TRUE);
        fireUserSignedOn(sn);
        // System.out.println("User signed on: " + sn);
        }
      }

    public void newBuddyList(Buddy[] buddies)
      {
      }

    public void loginDone(DaimLoginEvent dle)
      {
      }

    public void loginError(DaimLoginEvent dle)
      {
      fireConnectionLost();
      }

    public void newUIN(DaimLoginEvent dle)
      {
      }

    }

  /*
   */

  private class HTMLStripper extends XMLParser
    {
    private StringBuffer buf;

    HTMLStripper(String text, StringBuffer buf)
      {
      super(new StringReader(text), false);
      
      this.buf = buf;
      }

    protected void consumeElement(XMLElement e)
      {
      // discard
      }

    protected void consumeEntity(String entity)
      {
      if(entity.equalsIgnoreCase("amp"))
        buf.append('&');
      else if(entity.equalsIgnoreCase("quot"))
        buf.append('\"');
      else if(entity.equalsIgnoreCase("lt"))
        buf.append('<');
      else if(entity.equalsIgnoreCase("gt"))
        buf.append('>');
      else if(entity.equalsIgnoreCase("mdash"))
        buf.append("--");
      else if(entity.equalsIgnoreCase("ndash"))
        buf.append('-');
      else if(entity.equalsIgnoreCase("middot"))
        buf.append('*');
      else if(entity.equalsIgnoreCase("apos"))
        buf.append('\'');
      else
        {      
        buf.append('&');
        buf.append(entity);
        buf.append(';');
        }
      }

    protected void consumeText(String text)
      {
      buf.append(text);
      }

    void dispose()
      {
      buf = null;
      }
    
    }
  
  /*
   */
  
  private class HTMLParser extends XMLParser
    {
    private StringBuffer buf;
    
    HTMLParser(String text, StringBuffer buf)
      {
      super(new StringReader(text), false);
      
      this.buf = buf;
      }

    protected void consumeElement(XMLElement e)
      {
      String tag = e.getTag();
      boolean end = e.isEnd();

      if(tag.equalsIgnoreCase("b"))
        {
        if(! end)
          buf.append("<b>");
        else
          buf.append("</b>");
        }

      else if(tag.equalsIgnoreCase("i"))
        {
        if(! end)
          buf.append("<i>");
        else
          buf.append("</i>");
        }

      else if(tag.equalsIgnoreCase("u"))
        {
        if(! end)
          buf.append("<u>");
        else
          buf.append("</u>");
        }

      else if(tag.equalsIgnoreCase("br"))
        {
        if(! end)
          buf.append("<br>");
        }

      else if(tag.equalsIgnoreCase("a"))
        {
        if(! end)
          {
          buf.append("<a href=\"");
          String ref = e.getAttributeValue("href");
          if(ref == null)
            ref = e.getAttributeValue("HREF");
          
          if(ref != null)
            buf.append(ref);
          buf.append("\">");
          }
        else
          buf.append("</a>");
        }

      else if(tag.equalsIgnoreCase("font"))
        {
        if(! end)
          {
          String color = e.getAttributeValue("color");
          if(color == null)
            color = e.getAttributeValue("COLOR");

          if(color == null)
            color = "black";
          
          buf.append("<font color=\"");
          buf.append(color);
          buf.append("\">");
          }
        else
          buf.append("</font>");
        }
      }

    protected void consumeEntity(String entity)
      {
      buf.append('&');
      buf.append(entity);
      buf.append(';');
      }

    protected void consumeText(String text)
      {
      buf.append(text);
      }

    void dispose()
      {
      buf = null;
      }
    }

  }

/* end of source file */
