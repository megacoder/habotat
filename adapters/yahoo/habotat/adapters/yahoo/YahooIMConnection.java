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

package habotat.adapters.yahoo;

import ymsg.network.*;
import ymsg.network.event.*;

import java.io.*;
import java.util.*;

import kiwi.text.*;

import habotat.api.*;

/**
 * @author Mark Lindner
 */

public class YahooIMConnection extends IMConnection
  {
  private _Adapter adapter;
  private StringBuffer buf = new StringBuffer(256);
  private Session session;
  private static final String GROUP = "default";

  /*
   */
  
  YahooIMConnection(Session session)
    {
    this.session = session;

    adapter = new _Adapter();
    session.addSessionListener(adapter);
    }

  /**
   */
  
  public void close()
    {
    try
      {
      session.logout();
      }
    catch(Exception ex)
      {
      }
    }

  /**
   */
  
  public void addWatchedUser(String user) throws IMException
    {
    try
      {
      session.addFriend(user, GROUP);
      }
    catch(Exception ex)
      {
      throw(new IMException(ex.getMessage()));
      }
    }

  /**
   */
  
  public void removeWatchedUser(String user) throws IMException
    {
    try
      {
      session.removeFriend(user, GROUP);
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
      buf.setLength(0);
      HTMLParser parser = new HTMLParser(message, buf);
      try
        {
        parser.parse();
        }
      catch(IOException ex) { }

      String msg = buf.toString();
      //System.out.println("sending: " + msg);
      session.sendMessage(user, msg);

      parser.dispose();
      }
    catch(Exception ex)
      {
      throw(new IMException(ex.getMessage()));
      }
    }

  /*
   */

  private class _Adapter extends SessionAdapter
    {
    public void messageReceived(SessionEvent evt)
      {
      fireMessageReceived(evt.getFrom(), evt.getMessage());
      }

    public void connectionClosed(SessionEvent evt)
      {
      fireConnectionLost();
      }

    public void friendsUpdateReceived(SessionFriendEvent evt)
      {
      YahooUser users[] = evt.getFriends();

      for(int i = 0; i < users.length; i++)
        {
        String user = users[i].getId();
        boolean online = users[i].isLoggedIn();

        if(online)
          fireUserSignedOn(user);
        else
          fireUserSignedOff(user);
        }
      }
    
    }

  /*
   */

  private class HTMLParser extends XMLParser
    {
    private StringBuffer buf;
    private String href = null;
    
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
          buf.append('\n');
        }

      else if(tag.equalsIgnoreCase("a"))
        {
        if(! end)
          {
          href = e.getAttributeValue("href");
          if(href == null)
            href = e.getAttributeValue("HREF");
          }
        else
          {
          // YIM is unbelievably lame...
          
          if(href != null)
            {
            buf.append(" [ ");
            buf.append(href);
            buf.append(" ]");
            href = null;
            }
          }
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
