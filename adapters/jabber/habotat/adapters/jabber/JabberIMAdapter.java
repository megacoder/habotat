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

import org.jdom.*;

import habotat.api.*;

/**
 * @author Mark Lindner
 */

public class JabberIMAdapter extends IMAdapter
  {
  private static final String DEFAULT_HOST = "talk.google.com";
  private static final int DEFAULT_PORT = 5222;
  private static final String DEFAULT_SERVICE = "gmail.com";
  private static final String RESOURCE = "Habotat";
  private static final int MAX_IM_LENGTH = 4096;
  private static final String ATTR_SERVICE = "imService";
  private String service;
  private PluginUI ui = null;

  /**
   */
  
  public JabberIMAdapter()
    {
    this(DEFAULT_HOST, DEFAULT_PORT);
    }

  /**
   */
  
  public JabberIMAdapter(String host)
    {
    this(host, DEFAULT_PORT);
    }

  /**
   */
  
  public JabberIMAdapter(String host, int port)
    {
    super(host, port);

    service = DEFAULT_SERVICE;
    }

  /**
   */
  
  public IMConnection openConnection(String username, String password)
    throws IMException
    {
    try
      {
      XMPPConnection xconn = new XMPPConnection(host, port, service);
//      GoogleTalkConnection xconn = new GoogleTalkConnection();
      xconn.login(username, password, RESOURCE);

      return(new JabberIMConnection(xconn));
      }
    catch(XMPPException ex)
      {
      ex.printStackTrace();
      
      throw(new IMException("connection failed: " + ex.getMessage()));
      }
    }

  /**
   */

  public int getMaxMessageLength()
    {
    return(MAX_IM_LENGTH);
    }

  /**
   */

  public PluginUI getUI()
    {
    if(ui == null)
      ui = new JabberUI(this);

    return(ui);
    }

  /**
   */

  public void writeSettings(Element element)
    {
    XMLUtils.setStringAttr(element, ATTR_HOST, host);
    XMLUtils.setIntAttr(element, ATTR_PORT, port);
    XMLUtils.setStringAttr(element, ATTR_SERVICE, service);
    }

  /**
   */

  public void readSettings(Element element)
    {
    try
      {
      host = XMLUtils.getStringAttr(element, ATTR_HOST, DEFAULT_HOST);
      port = XMLUtils.getIntAttr(element, ATTR_PORT, DEFAULT_PORT);
      service = XMLUtils.getStringAttr(element, ATTR_SERVICE, DEFAULT_SERVICE);
      }
    catch(InvalidXMLException ex)
      {
      host = DEFAULT_HOST;
      port = DEFAULT_PORT;
      service = DEFAULT_SERVICE;
      }
    }

  /*
   */
  
  void setService(String service)
    {
    this.service = service;
    }

  /*
   */
  
  String getService()
    {
    return(service);
    }
  
  }

/* end of source file */
