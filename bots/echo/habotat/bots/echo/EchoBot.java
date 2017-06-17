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

package habotat.bots.echo;

import habotat.api.*;

/**
 * @author Mark Lindner
 */

public class EchoBot extends Bot
  {
  /*
   */
  
  public EchoBot()
    {
    }

  /*
   */
  
  public void start()
    {
    logInfo("EchoBot started!");
    }

  /*
   */
  
  public void stop()
    {
    logInfo("EchoBot stopped!");
    }

  /*
   */
  
  public void handleEvent(MessageEvent mevt)
    {
    try
      {
      String msg = mevt.getMessage();
      
      if(msg.equals(""))
        sendMessage(mevt.getSender(), "Commands:<br><b>" + getKeyword()
                    + "</b> <i>text</i> - echoes <i>text</i> back to you.");
      else
        sendMessage(mevt.getSender(), mevt.getMessage());
      }
    catch(BotException ex)
      {
      logError(ex.toString());
      }
    }
  
  }

/* end of source file */
