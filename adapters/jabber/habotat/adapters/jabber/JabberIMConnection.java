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

package habotat.adapters.jabber;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.filter.*;
import org.jivesoftware.smackx.packet.*;

import java.io.*;
import java.util.*;

import kiwi.text.*;

import habotat.api.*;

/**
 * @author Mark Lindner
 *
 * <html> ->  <body xmlns="http://www.w3.org/1999/xhtml">
 * <b> -> <strong>
 * <i> -> <em>
 * <u> -> <span style="text-decoration: underline;">
 */

public class JabberIMConnection extends IMConnection
  {
  private XMPPConnection xconn;
  private _Adapter adapter;
  private StringBuffer buf = new StringBuffer(256);

  /*
   */
  
  JabberIMConnection(XMPPConnection xconn)
    {
    this.xconn = xconn;

    adapter = new _Adapter();

    PacketFilter filter = new OrFilter(new PacketTypeFilter(Message.class),
                                       new PacketTypeFilter(Presence.class));
    
    xconn.addPacketListener(adapter, filter);
    xconn.addConnectionListener(adapter);
    }

  /**
   */
  
  public void close()
    {
    if(xconn != null)
      {
      xconn.removePacketListener(adapter);
      xconn.close();
      }
    
    xconn = null;
    }

  /**
   */
  
  public void addWatchedUser(String user) throws IMException
    {
    Presence p = new Presence(Presence.Type.SUBSCRIBE);
    p.setTo(user);

    xconn.sendPacket(p);
    }

  /**
   */
  
  public void removeWatchedUser(String user) throws IMException
    {
    Presence p = new Presence(Presence.Type.UNSUBSCRIBE);
    p.setTo(user);

    xconn.sendPacket(p);
    }

  /**
   */
  
  public void sendMessage(String user, String message) throws IMException
    {
    Message m = new Message(user);
    m.setBody("(XHTML content attached)");

    synchronized(buf)
      {
      buf.setLength(0);
      buf.append("<body xmlns=\"http://www.w3.org/1999/xhtml\">");
      HTMLParser parser = new HTMLParser(message, buf);
      try
        {
        parser.parse();
        }
      catch(IOException ex) { }
      buf.append("</body>");
      
      XHTMLExtension html = new XHTMLExtension();

      String msg = buf.toString();

      html.addBody(msg);
      m.addExtension(html);

      //System.out.println("sending: " + msg);
      
      parser.dispose();
      }

    xconn.sendPacket(m);
    }

  /*
   */

  private class _Adapter implements ConnectionListener, PacketListener
    {

    /*
     */
    
    public void connectionClosed()
      {
      fireConnectionLost();
      }

    /*
     */
    
    public void connectionClosedOnError(Exception ex)
      {
      fireConnectionLost();
      }

    /*
     */
    
    public void processPacket(Packet packet)
      {
      String from = packet.getFrom();

      //System.out.println(packet.getClass().getName() + " from " + from);

      int x = from.indexOf('/');
      if(x >= 0)
        from = from.substring(0, x);
      
      if(packet instanceof Message)
        {
        Message m = (Message)packet;

        Message.Type type = m.getType();

        if((type == Message.Type.CHAT) || (type == Message.Type.NORMAL))
          fireMessageReceived(from, m.getBody());

        /*
        Iterator iter = packet.getExtensions();
        while(iter.hasNext())
          {
          PacketExtension pe = (PacketExtension)iter.next();

          if(pe instanceof XHTMLExtension)
            {
            XHTMLExtension html = (XHTMLExtension)pe;

            Iterator iterb = html.getBodies();
            while(iterb.hasNext())
              {
              String body = (String)iterb.next();
              }

            break;
            }
          }
        */
        }

      else if(packet instanceof Presence)
        {
        Presence p = (Presence)packet;

        Presence.Type type = p.getType();

        if(type == Presence.Type.AVAILABLE)
          fireUserSignedOn(from);

        else if(type == Presence.Type.UNAVAILABLE)
          fireUserSignedOff(from);
        }

      else if(packet instanceof RosterPacket)
        {
        RosterPacket r = (RosterPacket)packet;

        Iterator iter = r.getRosterItems();
        while(iter.hasNext())
          {
          RosterPacket.Item ri = (RosterPacket.Item)iter.next();
          if(ri.getItemStatus()
             == RosterPacket.ItemStatus.SUBSCRIPTION_PENDING)
            {
            String user = ri.getUser();

            //System.out.println("subscription pending from: " + user);
            }
          }
        }
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
          buf.append("<strong>");
        else
          buf.append("</strong>");
        }

      else if(tag.equalsIgnoreCase("i"))
        {
        if(! end)
          buf.append("<em>");
        else
          buf.append("</em>");
        }

      else if(tag.equalsIgnoreCase("u"))
        {
        if(! end)
          buf.append("<span style=\"text-decoration: underline;\">");
        else
          buf.append("</span>");
        }

      else if(tag.equalsIgnoreCase("br"))
        {
        if(! end)
          buf.append("<br/>");
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
          
          buf.append("<span style=\"color: ");
          buf.append(color);
          buf.append(";\">");
          }
        else
          buf.append("</span>");
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
