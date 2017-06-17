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

import org.jdom.*;

import habotat.api.*;

/**
 * @author Mark Lindner
 */

public class YahooIMAdapter extends IMAdapter
  {
  private static final String DEFAULT_HOST = "scs.msg.yahoo.com";
  private static final int DEFAULT_PORT = 5050;
  private static final int MAX_IM_LENGTH = 232; // pathetic.
  private PluginUI ui = null;

  /**
   */
  
  public YahooIMAdapter()
    {
    this(DEFAULT_HOST, DEFAULT_PORT);
    }

  /**
   */
  
  public YahooIMAdapter(String host)
    {
    this(host, DEFAULT_PORT);
    }

  /**
   */
  
  public YahooIMAdapter(String host, int port)
    {
    super(host, port);
    }

  /**
   */
  
  public IMConnection openConnection(String username, String password)
    throws IMException
    {
    Session session = new Session();

    try
      {
      session.login(username, password);
      }
    catch(Exception ex)
      {
      throw(new IMException(ex.getMessage()));
      }

    YahooIMConnection conn = new YahooIMConnection(session);

    return(conn);
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
      ui = new YahooUI(this);

    return(ui);
    }

  /**
   */

  public void writeSettings(Element element)
    {
    XMLUtils.setStringAttr(element, ATTR_HOST, host);
    XMLUtils.setIntAttr(element, ATTR_PORT, port);
    }

  /**
   */

  public void readSettings(Element element)
    {
    try
      {
      host = XMLUtils.getStringAttr(element, ATTR_HOST, DEFAULT_HOST);
      port = XMLUtils.getIntAttr(element, ATTR_PORT, DEFAULT_PORT);
      }
    catch(InvalidXMLException ex)
      {
      host = DEFAULT_HOST;
      port = DEFAULT_PORT;
      }
    }
  
  }

/* end of source file */
