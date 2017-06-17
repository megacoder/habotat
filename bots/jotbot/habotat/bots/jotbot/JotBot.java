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

package habotat.bots.jotbot;

import java.io.*;
import java.util.*;

import org.jdom.*;

import habotat.api.*;

/**
 * @author Mark Lindner
 */

public class JotBot extends Bot
  {
  private String helpText = null;
  private static final int MAX_JOTS = 10;
  private static final String NODE_USERS = "users", NODE_USER = "user",
    NODE_NOTE = "note";
  private static final String ATTR_NAME = "name", ATTR_TITLE = "title";
  private int saveTimer;
  private HashMap<String, ArrayList<Note>> noteLists;
  private static final int STATE_NONE = 0, STATE_TITLE = 1, STATE_NOTE = 2;

  /*
   */
  
  private class Note
    {
    private String title;
    private String note;

    Note(String title, String note)
      {
      this.title = title;
      this.note = note;
      }

    String getTitle()
      {
      return(title);
      }

    void setTitle(String title)
      {
      this.title = title;
      }

    String getNote()
      {
      return(note);
      }

    void setNote(String note)
      {
      this.note = note;
      }
    }

  /*
   */
  
  public JotBot()
    {
    noteLists = new HashMap<String, ArrayList<Note>>();
    }

  /*
   */
  
  public void start()
    {
    logInfo("JotBot started!");

    try
      {
      loadData();
      }
    catch(BotException ex)
      {
      logError("Unable to load data", ex);
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

    buildHelpText();
    }

  /*
   */
  
  public void stop()
    {
    try
      {
      saveData();
      }
    catch(BotException ex)
      {
      logError("Unable to save data", ex);
      }

    try
      {
      destroyTimer(saveTimer);
      }
    catch(BotException ex)
      {
      }

    logInfo("JotBot stopped!");
    }

  /*
   */

  private ArrayList<Note> getList(String user)
    {
    ArrayList<Note> v = noteLists.get(user);
    if(v == null)
      {
      v = new ArrayList<Note>();
      noteLists.put(user, v);
      }

    return(v);
    }

  /*
   */
  
  private void buildHelpText()
    {
    StringBuffer sb = new StringBuffer();
    String kwd = getKeyword();

    sb.append("Commands:<br><b>");
    sb.append(kwd);
    sb.append(" list</b> - list all notes<br><b>");
    sb.append(kwd);
    sb.append(" add</b> - add a new note<br><b>");
    sb.append(kwd);
    sb.append(" view</b> <i>n</i> - view note #<i>n</i><br><b>");
    sb.append(kwd);
    sb.append(" erase</b> <i>n</i> - erase note #<i>n</i><br><b>");
    sb.append(kwd);
    sb.append(" purge</b> - erase all notes");

    helpText = sb.toString();
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

      noteLists.clear();

      Element root = data.getRootElement();

      Element userNode = XMLUtils.getChildElem(root, NODE_USERS, true);
      List list = userNode.getChildren(NODE_USER);

      Iterator iter = list.iterator();
      while(iter.hasNext())
        {
        Element node = (Element)iter.next();

        try
          {
          
          String name = XMLUtils.getStringAttr(node, ATTR_NAME, null, true);
          ArrayList<Note> vec = getList(name);
          
          List list2 = node.getChildren(NODE_NOTE);
          Iterator iter2 = list2.iterator();
          for(int i = 0; (i < MAX_JOTS) && iter2.hasNext(); i++)
            {
            Element node2 = (Element)iter2.next();
            
            String title = XMLUtils.getStringAttr(node2, ATTR_TITLE, null,
                                                  true);
            String note = XMLUtils.getTextContent(node2);
            
            vec.add(new Note(title, note));
            }
          }
        catch(InvalidXMLException ex)
          {
          // skip it
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
      
    Iterator<String> iter = noteLists.keySet().iterator();
    while(iter.hasNext())
      {
      String name = iter.next();
      Element userNode = new Element(NODE_USER);

      XMLUtils.setStringAttr(userNode, ATTR_NAME, name);

      usersNode.addContent(userNode);

      ArrayList<Note> vec = noteLists.get(name);
      if(vec == null)
        continue;

      Iterator<Note> iter2 = vec.iterator();
      while(iter2.hasNext())
        {
        Note note = iter2.next();

        Element noteNode = new Element(NODE_NOTE);

        XMLUtils.setStringAttr(noteNode, ATTR_TITLE, note.getTitle());
        noteNode.setText(note.getNote());

        userNode.addContent(noteNode);
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
    ArrayList<Note> v;
      
    try
      {
      BotSession session = fetchSession(mevt.getSender());
      
      if(session != null)
        {
        switch(session.getState())
          {
          case STATE_TITLE:
            {
            session.putString("title", mevt.getMessage().trim());
            sendMessage(mevt.getSender(),
                        "Now enter the text of your note.");
            session.setState(STATE_NOTE);
            break;
            }
              
          case STATE_NOTE:
            {
            String title = session.getString("title");
            Note note = new Note(title, mevt.getMessage());
            v = getList(mevt.getSender());
            v.add(note);
            sendMessage(mevt.getSender(), "Note stored.");
            releaseSession(mevt.getSender());
            break;
            }
          }
        }
      else
        {
        String args[] = parseCommand(mevt.getMessage());
          
        if((args.length == 1) && "list".startsWith(args[0]))
          {
          v = getList(mevt.getSender());
          StringBuffer sb = new StringBuffer();
          sb.append("Note List:");
            
          if(v.size() == 0)
            sb.append(" <i>empty</i>");
          else
            {
            int i = 0;
            Iterator<Note> iter = v.iterator();
            while(iter.hasNext())
              {
              Note note = iter.next();

              sb.append("<br><b>");
              sb.append(String.valueOf(++i));
              sb.append("</b>. <font color=\"blue\">");
              sb.append(note.getTitle());
              sb.append("</font>");
              }
            }
            
          sendMessage(mevt.getSender(), sb.toString());
          }
        else if((args.length == 1) && "add".startsWith(args[0]))
          {
          v = getList(mevt.getSender());
          if(v.size() == MAX_JOTS)
            sendMessage(mevt.getSender(),
                        "The maximum number of notes have already been stored.");
          else
            {
            sendMessage(mevt.getSender(),
                        "Enter a brief title for the note.");
            session = reserveSession(mevt.getSender());
            session.setState(STATE_TITLE);
            }
          }
        else if((args.length == 2) && "view".startsWith(args[0]))
          {
          int n = 0;
            
          try { n = Integer.parseInt(args[1]); }
          catch(NumberFormatException ex) { }
            
          v = getList(mevt.getSender());
          if(n < 1 || n > v.size())
            sendMessage(mevt.getSender(), "Invalid note number.");
          else
            {
            StringBuffer sb = new StringBuffer();
            Note note = v.get(--n);
              
            sb.append("<font color=\"blue\">");
            sb.append(note.getTitle());
            sb.append("</font><br>");
            sb.append(note.getNote());
              
            sendMessage(mevt.getSender(), sb.toString());
            }
          }
        else if((args.length == 2) && "erase".startsWith(args[0]))
          {
          int n = 0;
            
          try { n = Integer.parseInt(args[1]); }
          catch(NumberFormatException ex) { }
            
          v = getList(mevt.getSender());
          if(n < 1 || n > v.size())
            sendMessage(mevt.getSender(), "Invalid note number.");
          else
            {
            v.remove(--n);
            sendMessage(mevt.getSender(), "Note erased.");
            }
          }
        else if((args.length == 1) && "purge".startsWith(args[0]))
          {
          v = getList(mevt.getSender());
          v.clear();
          sendMessage(mevt.getSender(), "All notes removed.");
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
    }
  }

/* end of source file */
