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

import java.util.*;

import habotat.api.*;

/**
 *
 * @author Mark Lindner
 */

class BotSessionImpl extends HashMap implements BotSession
  {
  private int state;
  private long timestamp;
  private String user;
  private int ttl;

  /*
   */
  
  BotSessionImpl()
    {
    this(60);
    }

  /*
   */
  
  BotSessionImpl(int ttl)
    {
    this.ttl = ttl;
    }

  /*
   */
  
  void setUser(String user)
    {
    this.user = user;
    }

  /*
   */
  
  String getUser()
    {
    return(user);
    }

  /*
   */
  
  boolean isTimedOut(long now)
    {
    return(now > (this.timestamp + (1000 * ttl)));
    }

  /*
   */
  
  void setTimestamp(long timestamp)
    {
    this.timestamp = timestamp;
    }

  /*
   */
  
  long getTimestamp()
    {
    return(timestamp);
    }

  /*
   */
  
  public void setState(int state)
    {
    this.state = state;
    }

  /*
   */
  
  public int getState()
    {
    return(state);
    }

  /*
   */
  
  public void putString(String key, String value)
    {
    put(key, value);
    }

  /*
   */
  
  public String getString(String key)
    {
    return((String)get(key));
    }

  /*
   */
  
  public void putInt(String key, int value)
    {
    put(key, new Integer(value));
    }

  /*
   */
  
  public int getInt(String key)
    {
    Integer value = (Integer)get(key);

    return(value.intValue());
    }

  /*
   */
  
  public void putFloat(String key, float value)
    {
    put(key, new Float(value));
    }

  /*
   */
  
  public float getFloat(String key)
    {
    Float value = (Float)get(key);

    return(value.floatValue());
    }

  /*
   */
  
  public void putBoolean(String key, boolean value)
    {
    put(key, value ? Boolean.TRUE : Boolean.FALSE);
    }

  /*
   */
  
  public boolean getBoolean(String key)
    {
    Boolean value = (Boolean)get(key);

    return(value.booleanValue());
    }

  /*
   */
  
  public void putObject(String key, Object value)
    {
    put(key, value);
    }

  /*
   */
  
  public Object getObject(String key)
    {
    return(get(key));
    }

  /*
   */
  
  void reset()
    {
    clear();
    user = null;
    state = 0;
    }
  
  }

/* end of source file */
