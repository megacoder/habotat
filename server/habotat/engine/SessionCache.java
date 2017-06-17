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
 * @author Mark Lindner
 */

class SessionCache
  {
  private Stack<BotSessionImpl> freeList = new Stack<BotSessionImpl>();
  private HashMap<String, BotSessionImpl> sessionMap
    = new HashMap<String, BotSessionImpl>();
  private int size;
  private int sessionTimeout;
  private SessionTimeoutObserver observer;

  /*
   */
  
  SessionCache(int size, int sessionTimeout,
               SessionTimeoutObserver observer)
    {
    this.size = size;
    this.observer = observer;
    this.sessionTimeout = sessionTimeout;

    for(int i = 0; i < size; i++)
      freeList.push(new BotSessionImpl(sessionTimeout * 60));
    }

  /*
   */
  
  synchronized BotSessionImpl fetch(String name)
    {
    return(sessionMap.get(name));
    }

  /*
   */

  synchronized BotSessionImpl reserve(String name) throws BotException
    {
    BotSessionImpl sess = sessionMap.get(name);
    if(sess == null)
      {
      if(freeList.size() == 0)
        throw(new BotException("No free sessions."));

      sess = freeList.pop();
      sess.reset();
      sess.setTimestamp(System.currentTimeMillis());
      sessionMap.put(name, sess);
      }

    return(sess);
    }

  /*
   */

  synchronized boolean contains(String name)
    {
    return(sessionMap.get(name) != null);
    }

  /*
   */

  synchronized void release(String name)
    {
    BotSessionImpl sess = sessionMap.get(name);

    if(sess != null)
      {
      sessionMap.remove(name);
      sess.reset();
      freeList.push(sess);
      }
    }
  
  /*
   */

  synchronized void reset()
    {
    Iterator<BotSessionImpl> iter = freeList.iterator();
    while(iter.hasNext())
      iter.next().reset();
    }

  /*
   */

  synchronized void expire(long now)
    {
    Iterator<String> iter = sessionMap.keySet().iterator();
    while(iter.hasNext())
      {
      String name = iter.next();
      BotSessionImpl sess = sessionMap.get(name);
      if(sess.isTimedOut(now))
        {
        sessionMap.remove(name);
        freeList.push(sess);
        observer.sessionTimedOut(name);
        }
      }
    }
  
  }

/* end of source file */
