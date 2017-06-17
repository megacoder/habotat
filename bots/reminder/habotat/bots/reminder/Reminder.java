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

package habotat.bots.reminder;

import habotat.api.*;

/**
 * @author Mark Lindner
 */

class Reminder
  {
  static final int SUNDAY = 0, MONDAY = 1, TUESDAY = 2, WEDNESDAY = 3,
    THURSDAY = 4, FRIDAY = 5, SATURDAY = 6, DAILY = 7, WEEKDAYS = 8;

  private static final String dayStrings[] = new String[]
    { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Daily", "Mon-Fri" };

  private String user;
  private String title = null;
  private String message = null;
  private boolean repeating;
  private int hour = 0, minute = 0, days = 0;
  private int timerID = -1;

  /*
   */
  
  Reminder(String user)
    {
    this.user = user;
    }

  /*
   */
  
  Reminder(String user, String title, String message, int hour, int minute,
           int days, boolean repeating)
    {
    this.user = user;
    this.title = title;
    this.message = message;
    this.hour = hour;
    this.minute = minute;
    this.days = days;
    this.repeating = repeating;
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

  void setHour(int hour)
    {
    this.hour = hour;
    }
 
  /*
   */

  int getHour()
    {
    return(hour);
    }

  /*
   */
  
  void setMinute(int minute)
    {
    this.minute = minute;
    }

  /*
   */

  int getMinute()
    {
    return(minute);
    }

  /*
   */
  
  void setDays(int days)
    {
    this.days = days;
    }

  /*
   */
  
  int getDays()
    {
    return(days);
    }

  /*
   */
  
  void setTitle(String title)
    {
    this.title = title;
    }

  /*
   */
  
  String getTitle()
    {
    return(title);
    }

  /*
   */
  
  void setMessage(String message)
    {
    this.message = message;
    }

  /*
   */
  
  String getMessage()
    {
    return(message);
    }

  /*
   */
  
  void setRepeating(boolean repeating)
    {
    this.repeating = repeating;
    }

  /*
   */
  
  boolean isRepeating()
    {
    return(repeating);
    }

  /*
   */
  
  int getTimerID()
    {
    return(timerID);
    }

  /*
   */
  
  void setTimerID(int id)
    {
    this.timerID = id;
    }

  /*
   */
  
  String getTimeAsString()
    {
    StringBuffer sb = new StringBuffer(20);
    sb.append(dayStrings[days]);
    sb.append(" at ");
    boolean pm = false;
    int hr = hour;
    if(hr >= 12)
      {
      pm = true;
      hr -= 12;
      }

    sb.append(String.valueOf(hr));
    sb.append(':');
    if(minute < 10)
      sb.append('0');
    sb.append(String.valueOf(minute));

    sb.append(' ');
    sb.append(pm ? "pm" : "am");

    return(sb.toString());
    }
  
  }

/* end of source file */
