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

import habotat.api.*;

import org.walluck.oscar.*;
import org.walluck.oscar.tools.*;
import org.walluck.oscar.handlers.*;
import org.walluck.oscar.channel.aolim.AOLIM;

/**
 * @author Mark Lindner
 */

public class AIMIMAdapter extends IMAdapter
  {
  private static final String DEFAULT_HOST = "login.oscar.aol.com";
  private static final int DEFAULT_PORT = 5190; // not used
  private static final int MAX_IM_LENGTH = 2048;
  private PluginUI ui = null;

  /**
   */
  
  public AIMIMAdapter()
    {
    this(DEFAULT_HOST, DEFAULT_PORT);
    }

  /**
   */
  
  public AIMIMAdapter(String host)
    {
    this(host, DEFAULT_PORT);
    }

  /**
   */
  
  public AIMIMAdapter(String host, int port)
    {
    super(host, port);
    }

  /**
   */
  
  public IMConnection openConnection(String username, String password)
    throws IMException
    {
    try
      {
      AIMSession session = new AIMSession();
      session.setPassword(password);
      session.setSN(username);

      /*
      ServiceHandler service = (ServiceHandler)session
        .getHandler(SNACFamily.AIM_CB_FAM_GEN);

      service.setAvailableMsg(session, "", null);
      */

      LoginTool logtool = (LoginTool)session
        .getTool(SNACFamily.AIM_CB_FAM_ATH);
      logtool.login();

      return(new AIMIMConnection(session));
      }
    catch(IOException ex)
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

  // on hold until we figure out how to make it work
  /*
  public PluginUI getUI()
    {
    if(ui == null)
      ui = new AIMUI(this);

    return(ui);
    }
  */
  
  }

/* end of source file */
