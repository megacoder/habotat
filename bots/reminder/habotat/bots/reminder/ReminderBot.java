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

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.jdom.*;

import habotat.api.*;

/**
 * @author Mark Lindner
 */

public class ReminderBot extends Bot
  {
  private static final int MAX_REMINDERS = 10;
  private static final int STATE_TITLE = 1, STATE_MESSAGE = 2,
    STATE_DATES = 3, STATE_TIMES = 4, STATE_REPEATING = 5;
  private static final String NODE_USERS = "users", NODE_USER = "user",
    NODE_REMINDER = "reminder", ATTR_TITLE = "title",
    ATTR_MESSAGE = "message", ATTR_HOUR = "hour", ATTR_MINUTE = "minute",
    ATTR_DAYS = "days", ATTR_REPEATING = "repeating", ATTR_NAME="name";
  private int timer;
  private String helpText = null;
  private HashMap<String, ArrayList<Reminder>> reminderLists
    = new HashMap<String, ArrayList<Reminder>>();
  private int saveTimer;
  private HashMap<Integer, Reminder> timerMap
    = new HashMap<Integer, Reminder>();
  private Pattern patTime
    = Pattern.compile("([0-9]{1,2}):([0-9]{2})(\\s*([aApP])[Mm])?");

  /*
   */
  
  public ReminderBot()
    {
    }

  /*
   */

  private void buildHelpText()
    {
    StringBuffer sb = new StringBuffer();
    String kwd = getKeyword();

    sb.append("Commands:<br><b>");
    sb.append(kwd);
    sb.append(" list</b> - list all scheduled reminders<br><b>");
    sb.append(kwd);
    sb.append(" add</b> - schedule a new reminder<br><b>");
    sb.append(kwd);
    sb.append(" cancel</b> <i>n</i> - cancel reminder #<i>n</i><br><b>");
    sb.append(kwd);
    sb.append(" clear</b> - remove all reminders");

    helpText = sb.toString();    
    }

  /*
   */

  public void start()
    {
    buildHelpText();

    try
      {
      loadData();
      }
    catch(BotException ex)
      {
      logError("Unable to load data", ex);
      }

    Iterator<String> iter = reminderLists.keySet().iterator();
    while(iter.hasNext())
      {
      ArrayList<Reminder> v = reminderLists.get(iter.next());
      if(v == null)
        continue;

      Iterator<Reminder> iter2 = v.iterator();
      while(iter2.hasNext())
        {
        Reminder rem = iter2.next();

        try
          {
          activateReminder(rem);
          }
        catch(BotException ex)
          {
          logError("Couldn't activate reminder: " + rem.getTitle());
          }
        }
      }

    try
      {
      TimeSpec tspec = new TimeSpec();
      tspec.setAllHours();
      tspec.clearAllMinutes();
      tspec.setMinute(0);
      tspec.setAllDays();
      tspec.setAllMonths();
      tspec.setAllDaysOfWeek();

      saveTimer = createTimer(tspec);      
      }
    catch(BotException ex) { }
    
    logInfo("ReminderBot started!");
    }

  /*
   */
  
  public void stop()
    {
    try
      {
      destroyTimer(saveTimer);
      }
    catch(BotException ex) { }
    
    Iterator<String> iter = reminderLists.keySet().iterator();
    while(iter.hasNext())
      {
      ArrayList<Reminder> v = reminderLists.get(iter.next());
      if(v == null)
        continue;

      Iterator<Reminder> iter2 = v.iterator();
      while(iter2.hasNext())
        {
        Reminder rem = iter2.next();

        try
          {
          deactivateReminder(rem);
          }
        catch(BotException ex)
          {
          logError("Couldn't deactive timer: " + rem.getTitle());
          }
        }
      }

    try
      {
      saveData();
      }
    catch(BotException ex)
      {
      logError("Unable to save data", ex);
      }

    logInfo("ReminderBot stopped!");
    }

  /*
   */
  
  private ArrayList<Reminder> getList(String user)
    {
    ArrayList<Reminder> v = reminderLists.get(user);
    if(v == null)
      {
      v = new ArrayList<Reminder>();
      reminderLists.put(user, v);
      }

    return(v);
    }
  
  /*
   */

  protected void loadData(InputStream ins) throws BotException
    {
    logInfo("Loading data");

    try
      {
      Document data = readXML(ins);
      
      // now copy all info into objects

      reminderLists.clear();

      Element root = data.getRootElement();

      Element userNode = XMLUtils.getChildElem(root, NODE_USERS, true);
      List list = userNode.getChildren(NODE_USER);

      Iterator iter = list.iterator();
      
      while(iter.hasNext())
        {
        Element node = (Element)iter.next();

        String name = XMLUtils.getStringAttr(node, ATTR_NAME, null, true);
        ArrayList<Reminder> vec = getList(name);

        List list2 = node.getChildren(NODE_REMINDER);
        Iterator iter2 = list2.iterator();
        for(int i = 0; (i < MAX_REMINDERS) && iter2.hasNext(); i++)
          {
          Element node2 = (Element)iter2.next();

          String title = XMLUtils.getStringAttr(node2, ATTR_TITLE, null, true);
          String message = XMLUtils.getStringAttr(node2, ATTR_MESSAGE, null,
                                                  true);
          boolean repeating = XMLUtils.getBoolAttr(node2, ATTR_REPEATING);
          int hour = XMLUtils.getIntAttr(node2, ATTR_HOUR, 0, true);
          int min = XMLUtils.getIntAttr(node2, ATTR_MINUTE, 0, true);
          int days = XMLUtils.getIntAttr(node2, ATTR_DAYS, 0, true);

          vec.add(new Reminder(name, title, message, hour, min, days,
                               repeating));
          }
        }
      }
    catch(InvalidXMLException ex)
      {
      throw(new BotException("Invalid XML data: " + ex.getMessage()));
      }
    catch(IOException ex)
      {
      throw(new BotException("Unable to load data", ex));
      }
    }

  /*
   */

  protected void saveData(OutputStream outs) throws BotException
    {
    logInfo("Saving data");

    Element root = new Element("botdata");
    Document doc = new Document(root);

    Element usersNode = new Element(NODE_USERS);
    root.addContent(usersNode);

    Iterator<String> iter = reminderLists.keySet().iterator();
    while(iter.hasNext())
      {
      String name = iter.next();

      Element userNode = new Element(NODE_USER);
      XMLUtils.setStringAttr(userNode, ATTR_NAME, name);

      usersNode.addContent(userNode);

      ArrayList<Reminder> vec = reminderLists.get(name);
      if(vec == null)
        continue;

      Iterator<Reminder> iter2 = vec.iterator();
      while(iter2.hasNext())
        {
        Reminder rem = iter2.next();

        String message = rem.getMessage();
        int hour = rem.getHour();
        int minute = rem.getMinute();
        int days = rem.getDays();
        boolean repeating = rem.isRepeating();

        Element remNode = new Element(NODE_REMINDER);

        XMLUtils.setStringAttr(remNode, ATTR_TITLE, rem.getTitle());
        XMLUtils.setStringAttr(remNode, ATTR_MESSAGE, rem.getMessage());
        XMLUtils.setIntAttr(remNode, ATTR_HOUR, rem.getHour());
        XMLUtils.setIntAttr(remNode, ATTR_MINUTE, rem.getMinute());
        XMLUtils.setIntAttr(remNode, ATTR_DAYS, rem.getDays());
        XMLUtils.setBoolAttr(remNode, ATTR_REPEATING, rem.isRepeating());

        userNode.addContent(remNode);
        }
      }

    try
      {
      writeXML(doc, outs);
      }
    catch(IOException ex)
      {
      throw(new BotException("Unable to save data", ex));
      }
    }

  /*
   */

  public void handleEvent(MessageEvent mevt)
    {
    ArrayList<Reminder> v;

    try
      {
      BotSession session = fetchSession(mevt.getSender());

      if(session != null)
        {
        switch(session.getState())
          {
          case STATE_TITLE:
            {
            Reminder rem = (Reminder)session.getObject("reminder");
            rem.setTitle(mevt.getMessage().trim());
            sendMessage(mevt.getSender(),
                        "Now enter the reminder message.");
            session.setState(STATE_MESSAGE);
            break;
            }

          case STATE_MESSAGE:
            {
            Reminder rem = (Reminder)session.getObject("reminder");
            rem.setMessage(mevt.getMessage().trim());
            sendMessage(mevt.getSender(),
                        "On what day(s) do you wish to receive the reminder? <b>M</b>onday, <b>Tu</b>esday, <b>W</b>ednesday, <b>Th</b>ursday, <b>F</b>riday, <b>Sa</b>turday, <b>Su</b>nday, <b>D</b>aily, <b>Wee</b>kdays.");
            session.setState(STATE_DATES);
            break;
            }

          case STATE_DATES:
            {
            String s = mevt.getMessage().trim().toLowerCase();
            Reminder rem = (Reminder)session.getObject("reminder");

            if(parseDays(s, rem))
              {
              sendMessage(mevt.getSender(),
                          "At what time of day do you wish to receive the reminder? (Examples: <i>15:30</i>, <i>3:30 pm</i>.)");
              session.setState(STATE_TIMES);
              }
            else
              sendMessage(mevt.getSender(),
                          "That's an invalid response. Please try again.");

            break;
            }

          case STATE_TIMES:
            {
            String s = mevt.getMessage().trim().toLowerCase();
            Reminder rem = (Reminder)session.getObject("reminder");

            if(parseTime(s, rem))
              {
              sendMessage(mevt.getSender(),
                          "Is this a repeating reminder? (<b>Y</b>es or <b>N</b>o.)");
              session.setState(STATE_REPEATING);                
              }
            else
              sendMessage(mevt.getSender(),
                          "That's an invalid response. Please try again.");
              
            break;
            }

          case STATE_REPEATING:
            {
            String s = mevt.getMessage().trim().toLowerCase();
            Reminder rem = (Reminder)session.getObject("reminder");
            rem.setRepeating(s.startsWith("y"));

            releaseSession(mevt.getSender());

            v = getList(mevt.getSender());
            v.add(rem);
            activateReminder(rem);
            sendMessage(mevt.getSender(), "Reminder stored.");
              
            break;
            }
          }
        }
      else
        {
        String args[] = parseCommand(mevt.getMessage());

        if((args.length == 1) && "add".startsWith(args[0]))
          {
          v = getList(mevt.getSender());

          if(v.size() == MAX_REMINDERS)
            sendMessage(mevt.getSender(), "You have already reached your reminder limit.");
          else
            {
            sendMessage(mevt.getSender(), "Enter a brief title for this reminder.");
            BotSession sess = reserveSession(mevt.getSender());
            sess.setState(STATE_TITLE);
            Reminder rem = new Reminder(mevt.getSender());
            sess.putObject("reminder", rem);
            }
          }
        else if((args.length == 2) && "cancel".startsWith(args[0]))
          {
          int n = 0;
            
          try { n = Integer.parseInt(args[1]); }
          catch(NumberFormatException ex) { }

          v = getList(mevt.getSender());
            
          if(n < 1 || n > v.size())
            sendMessage(mevt.getSender(), "Invalid reminder number.");
          else
            {
            --n;
            Reminder rem = v.get(n);
            v.remove(n);
            deactivateReminder(rem);
            
            sendMessage(mevt.getSender(), "Reminder cancelled.");
            }
          }
        else if((args.length == 1) && "clear".startsWith(args[0]))
          {
          v = getList(mevt.getSender());
          v.clear();
          sendMessage(mevt.getSender(), "All reminders cancelled.");
          }
        else if((args.length == 1) && "list".startsWith(args[0]))
          { 
          v = getList(mevt.getSender());
            
          StringBuffer sb = new StringBuffer();

          sb.append("Reminder list:");

          if(v.size() == 0)
            sb.append(" <i>Empty</i>");
          else
            {
            Iterator<Reminder> iter = v.iterator();
              
            for(int ct = 1; iter.hasNext(); ct++)
              {
              sb.append("<br><b>");
              Reminder rem = iter.next();
              sb.append(String.valueOf(ct));
              sb.append("</b>. ");
                
              sb.append(rem.getTitle());
              sb.append(" (");
                
              sb.append(rem.getTimeAsString());
                
              if(rem.isRepeating())
                sb.append(", repeating");
                
              sb.append(')');
              }
            }

          sendMessage(mevt.getSender(), sb.toString());
          }
        else
          sendMessage(mevt.getSender(), helpText);
        }
        
      }
    catch(BotException ex)
      {
      logError(ex);
      }
    }
  
  /*
   */
  
  public void handleEvent(TimerEvent tevt)
    {
    int id = tevt.getTimerID();

    if(id == saveTimer)
      {
      try
        {
        saveData();
        }
      catch(BotException ex)
        {
        logError("Unable to save data", ex);
        }
      }
    else
      {
      Reminder rem = lookupReminder(id);
        
      logInfo("Got timer for reminder: " + rem.getTitle());
        
      StringBuffer sb = new StringBuffer();
      sb.append("Reminder:<br><i>");
      sb.append(rem.getMessage());
      sb.append("</i>");
        
      try
        {
        String msg = sb.toString();
        sendMessage(rem.getUser(), msg);
        }
      catch(BotException ex)
        {
        logError(ex);
        }

      if(! rem.isRepeating())
        {
        ArrayList<Reminder> v = getList(rem.getUser());
        v.remove(rem);
        logInfo("One-time reminder '" + rem.getTitle() + "' deleted.");
        }
      }
    }

  /*
   */

  private boolean parseDays(String s, Reminder rem)
    {
    if("monday".startsWith(s))
      rem.setDays(Reminder.MONDAY);
    else if("tuesday".startsWith(s))
      rem.setDays(Reminder.TUESDAY);
    else if("wednesday".startsWith(s))
      rem.setDays(Reminder.WEDNESDAY);
    else if("thursday".startsWith(s))
      rem.setDays(Reminder.THURSDAY);
    else if("friday".startsWith(s))
      rem.setDays(Reminder.FRIDAY);
    else if("saturday".startsWith(s))
      rem.setDays(Reminder.SATURDAY);
    else if("sunday".startsWith(s))
      rem.setDays(Reminder.SUNDAY);
    else if("daily".startsWith(s))
      rem.setDays(Reminder.DAILY);
    else if("weekdays".startsWith(s))
      rem.setDays(Reminder.WEEKDAYS);
    else
      return(false);

    return(true);
    }

  /*
   */

  private boolean parseTime(String s, Reminder rem)
    {
    Matcher matTime = patTime.matcher(s);

    if(!matTime.find())
      return(false);

    String shr = matTime.group(1);
    String smn = matTime.group(2);
    String ap = matTime.group(4);

    int hr = Integer.parseInt(shr);
    if((hr < 0) || (hr > 23))
      return(false);

    int mn = Integer.parseInt(smn);
    if((mn < 0) || (mn > 59))
      return(false);

    if("p".equalsIgnoreCase(ap))
      {
      if((hr < 1) || (hr > 12))
        return(false);
      
      if(hr < 12)
        hr += 12;
      }
    else if("a".equalsIgnoreCase(ap))
      {
      if((hr < 1) || (hr > 12))
        return(false);

      if(hr == 12)
        hr = 0;
      }

    rem.setHour(hr);
    rem.setMinute(mn);
    
    return(true);
    }

  /*
   */

  private Reminder lookupReminder(int timerID)
    {
    Integer key = new Integer(timerID);

    return(timerMap.get(key));
    }
  
  /*
   */

  private void activateReminder(Reminder rem) throws BotException
    {
    TimeSpec spec = new TimeSpec();

    logInfo("Activating reminder; " + rem.getTitle());
    
    spec.setHour(rem.getHour());
    spec.setMinute(rem.getMinute());

    int day = rem.getDays();
    if((day >= Reminder.SUNDAY) && (day <= Reminder.SATURDAY))
      spec.setDayOfWeek(day);
    else if(day == Reminder.DAILY)
      spec.setAllDaysOfWeek();
    else if(day == Reminder.WEEKDAYS)
      {
      for(int i = Reminder.SUNDAY; i <= Reminder.SATURDAY; i++)
        spec.setDayOfWeek(i);
      }

    spec.setAllDays();
    spec.setAllMonths();

    int timerID = createTimer(spec, rem.isRepeating());
    rem.setTimerID(timerID);

    // spec.dump();
    
    logInfo("Timer created: ID = " + timerID);

    timerMap.put(new Integer(timerID), rem);
    }

  /*
   */

  private void deactivateReminder(Reminder rem) throws BotException
    {
    int timerID = rem.getTimerID();
    destroyTimer(timerID);
    
    timerMap.remove(new Integer(timerID));    
    }

  }

/* end of source file */
