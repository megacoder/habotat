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

import kiwi.util.*;

import habotat.api.*;

/**
 *
 * @author Mark Lindner
 */

class Scheduler implements Runnable
  {
  private int timerID = 1;
  private Thread thread;
  private HashMap<BotDef, ArrayList<TimerDef>> events;
  private static final int MAX_TIMERS = 10;
  private EventQueue eventQueue;

  /*
   */

  private class TimerDef
    {
    BotDef bot;
    TimeSpec timeSpec;
    int id;
    boolean once;

    /*
     */
    
    TimerDef(BotDef bot, TimeSpec timeSpec,  boolean once, int id)
      {
      this.bot = bot;
      this.timeSpec = timeSpec;
      this.once = once;
      this.id = id;
      }

    /*
     */
    
    TimeSpec getTimeSpec()
      {
      return(timeSpec);
      }

    /*
     */
    
    BotDef getBot()
      {
      return(bot);
      }

    /*
     */
    
    int getID()
      {
      return(id);
      }
    
    }
  
  /*
   */
  
  Scheduler(EventQueue eventQueue)
    {
    events = new HashMap<BotDef, ArrayList<TimerDef>>();

    this.eventQueue = eventQueue;

    thread = new Thread(this);
    thread.setDaemon(true);
    thread.start();
    }

  /*
   */

  int addTimer(BotDef bot, TimeSpec timeSpec, boolean repeating)
    throws BotException
    {
    synchronized(events)
      {
      int id = -1;
      
      ArrayList<TimerDef> v = events.get(bot);
      if(v == null)
        {
        v = new ArrayList<TimerDef>();
        events.put(bot, v);
        }
      
      if(v.size() == MAX_TIMERS)
        throw(new BotException("maximum number of timers allocated"));
      else
        {
        id = timerID++;
        
        v.add(new TimerDef(bot, timeSpec, !repeating, id));
        }
      
      return(id);
      }
    }

  /*
   */
  
  void removeTimer(BotDef bot, int id) throws BotException
    {
    synchronized(events)
      {
      ArrayList<TimerDef> v = events.get(bot);
      if(v == null)
        throw(new BotException("No such timer."));
      else
        {
        Iterator<TimerDef> iter = v.iterator();
        
        while(iter.hasNext())
          {
          TimerDef tdef = iter.next();
          
          if(tdef.getID() == id)
            {
            iter.remove();
            return;
            }
          }
        }
      }
    }

  /*
   */
  
  void removeAllTimers(BotDef bot)
    {
    synchronized(events)
      {             
      ArrayList<TimerDef> v = events.get(bot);
      
      if(v != null)
        v.clear();
      }
    }

  /*
   */
  
 public void run()
    {
    // sweep & sleep loop
    
    for(;;)
      {
      // sweep

      Calendar now = Calendar.getInstance();

      synchronized(events)
        {
        Iterator<BotDef> iter = events.keySet().iterator();
        while(iter.hasNext())
          {
          BotDef bot = iter.next();

          ArrayList<TimerDef> v = events.get(bot);

          Iterator<TimerDef> iter2 = v.iterator();
          
          while(iter2.hasNext())
            {

            TimerDef tdef = iter2.next();

            if(tdef.getTimeSpec().match(now))
              {
              TimerEvent evt = new TimerEvent(this, tdef.getID());
              eventQueue.postEvent(evt, bot);
              
              if(tdef.once)
                iter2.remove();
              }
            }
          }
        }

      // sleep

      now = Calendar.getInstance();
      KiwiUtils.sleep(60 - now.get(Calendar.SECOND));
      }
    }
  
  }

/* end of source file */
