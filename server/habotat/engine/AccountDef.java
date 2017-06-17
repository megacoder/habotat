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
import javax.swing.event.*;

import kiwi.util.*;
import kiwi.util.plugin.*;

import habotat.api.*;


/**
 * @author Mark Lindner
 */

public class AccountDef extends Def implements IMConnectionListener
  {
  private String screenName;
  private String password;
  private String comment = "";
  private IMAdapter im;
  private IMConnection conn = null;
  private boolean connected = false;
  private EventListenerList listeners = new EventListenerList();
  private RefSet watchList = new RefSet();
  private PluginDef adapter;

  /*
   */
  
  public AccountDef()
    {
    this(null, null, null, null);
    }

  /*
   */
  
  public AccountDef(PluginDef adapter, String screenName, String password,
                    String comment)
    {
    this.screenName = screenName;
    this.password = password;
    this.comment = comment;

    setPlugin(adapter);
    }

  /*
   */

  public String getScreenName()
    {
    return(screenName);
    }

  /*
   */
  
  public void setScreenName(String screenName)
    {
    this.screenName = screenName;
    }

  /*
   */
  
  public String getPassword()
    {
    return(password);
    }

  /*
   */
  
  public void setPassword(String password)
    {
    this.password = password;
    }

  /*
   */
  
  public String getComment()
    {
    return(comment);
    }

  /*
   */
  
  public void setComment(String comment)
    {
    this.comment = comment;
    }

  /*
   */

  public PluginDef getPlugin()
    {
    return(adapter);
    }

  /*
   */
  
  public void setPlugin(PluginDef adapter)
    {
    if(adapter != this.adapter)
      {
      if(this.adapter != null)
        this.adapter.removeUseReference();
      
      this.adapter = adapter;
      
      if(adapter != null)
        {
        try
          {
          im = (IMAdapter)adapter.getPlugin().newInstance();
          
          adapter.addUseReference();
          }
        catch(PluginException ex)
          {
          ex.printStackTrace();
          }
        }
      }
    }

  /*
   */
  
  public boolean isConnected()
    {
    return(connected);
    }

  /*
   */
  
  public void connect() throws IOException
    {
    if(! isConnected())
      {
      try
        {
        if(adapter == null)
          throw(new IOException("no adapter!"));

        HabotatServer.logInfo("connecting to IM service");
        conn = im.openConnection(screenName, password);
        
        HabotatServer.logInfo("Connected");
        conn.addIMConnectionListener(this);
        connected = true;
        }
      catch(IMException ex)
        {
        throw(new IOException("Unable to connect to IM service"));
        }
      }
    }

  /*
   */
  
  public void disconnect()
    {
    if(isConnected())
      {
      HabotatServer.logInfo("disconnecting");
      
      conn.removeIMConnectionListener(this);
      conn.close();
      conn = null;          
      connected = false;
      }
    }

  /*
   */

  public boolean reconnect()
    {
    disconnect();

    boolean ok = false;
    
    for(int i = 1, pause = 20; i < 6; i++, pause *= 2)
      // don't try indefinitely.
      {
      HabotatServer.logWarning("Sleep " + pause + "...");
      KiwiUtils.sleep(pause);
      HabotatServer.logWarning("Trying to reconnect... try #" + i);

      try
        {
        connect();
        ok = true;
        break;
        }
      catch(IOException ex)
        {
        HabotatServer.logError("I/O Error while connecting", ex);
        }
      }

    return(ok);
    }

  /*
   */

  public void sendMessage(String recipient, String message) throws IOException
    {
    try
      {
      conn.sendMessage(recipient, message);
      }
    catch(IMException ex)
      {
      throw(new IOException(ex.getMessage()));
      }
    }

  /*
   */

  public void addIMListener(IMListener listener)
    {
    listeners.add(IMListener.class, listener);
    }

  /*
   */

  public void removeIMListener(IMListener listener)
    {
    listeners.remove(IMListener.class, listener);
    }

  /*
   */

  private void fireIMReceived(String sender, String message)
    {
    IMEvent evt = null;

    Object[] list = listeners.getListenerList();
 
    for(int i = list.length - 2; i >= 0; i -= 2)
      {
      if(list[i] == IMListener.class)
        {
        // Lazily create the event:
        if(evt == null)
          evt = new IMEvent(this, sender, message);
        ((IMListener)list[i + 1]).messageReceived(evt);
        }
      }
    }

  /*
   */

  private void fireUserSignOn(String user, boolean online)
    {
    SignOnEvent evt = null;

    Object[] list = listeners.getListenerList();
 
    for(int i = list.length - 2; i >= 0; i -= 2)
      {
      if(list[i] == SignOnListener.class)
        {
        // Lazily create the event:
        if(evt == null)
          evt = new SignOnEvent(this, user, online);

        ((SignOnListener)list[i + 1]).userStatusChanged(evt);
        }
      }    
    }

  /*
   */

  public void addWatchedUser(String user)
    {
    if(watchList.insert(user))
      {
      if(conn != null)
        {
        try
          {
          conn.addWatchedUser(user);
          }
        catch(IMException ex)
          {
          HabotatServer.logError("Error adding watched user", ex);
          }
        }
      }
    }

  /*
   */

  public void removeWatchedUser(String user)
    {
    if(watchList.remove(user))
      {
      if(conn != null)
        {
        try
          {
          conn.removeWatchedUser(user);
          }
        catch(IMException ex)
          {
          HabotatServer.logError("Error removing watched user", ex);
          }
        }
      }
    }

  /*
   */

  public String toString()
    {
    return(screenName + " (" + adapter.toString() + ")");
    }

  /*
   */

  public void connectionLost()
    {
    HabotatServer.logWarning("Connection lost!");
    reconnect();      
    }

  /*
   */
  
  public void messageReceived(String from, String message)
    {
    HabotatServer.logInfo("Message received from " + from);
    fireIMReceived(from, message);
    }

  /*
   */
  
  public void userSignedOff(String user)
    {
    fireUserSignOn(user, false);
    }

  /*
   */
  
  public void userSignedOn(String user)
    {
    fireUserSignOn(user, true);
    }

  /*
   */

  public IMAdapter getAdapter()
    {
    return(im);
    }
  
  }

/* end of source file */
