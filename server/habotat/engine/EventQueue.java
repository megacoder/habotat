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
import java.util.concurrent.*;

import habotat.api.*;

/**
 * @author Mark Lindner
 */

class EventQueue implements Runnable
  {
  private static final int QUEUE_DEPTH = 30;
  
  private ArrayBlockingQueue<EventNode> queue
    = new ArrayBlockingQueue<EventNode>(QUEUE_DEPTH);
  private Thread eventThread;

  private class EventNode
    {
    BotEvent event;
    BotDef target;

    EventNode(BotEvent event, BotDef target)
      {
      this.event = event;
      this.target = target;
      }

    void deliverEvent()
      {
      if(target.isRunning())
        target.getBot().processEvent(event);
      }
    }

  /*
   */
  
  EventQueue()
    {
    eventThread = new Thread(this);
    eventThread.setDaemon(true);
    eventThread.start();
    }

  /*
   */
  
  void postEvent(BotEvent event, BotDef target)
    {
    try
      {
      queue.put(new EventNode(event, target));
      }
    catch(InterruptedException ex)
      {
      }
    }

  /*
   */
  
  public void run()
    {
    try
      {
      for(;;)
        {
        EventNode node = queue.take();
        node.deliverEvent();
        }
      }
    catch(InterruptedException ex)
      {
      }
    }
  
  }

/* end of source file */
