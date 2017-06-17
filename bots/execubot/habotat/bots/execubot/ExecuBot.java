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

package habotat.bots.execubot;

import java.io.*;
import java.util.*;

import org.jdom.*;

import habotat.api.*;

/**
 * @author Mark Lindner
 */

public class ExecuBot extends Bot
  {
  private String cmdvec[];
  private ArrayList<String> args;
  private ExecuBotUI ui = null;
  private File command;
  private static final String NODE_COMMAND = "command", NODE_ARG = "arg";
  
  /*
   */
  
  public ExecuBot()
    {
    args = new ArrayList<String>();
    }

  /*
   */
  
  public void start()
    {
    try
      {
      loadData();
      }
    catch(BotException ex)
      {
      logError("Unable to load data", ex);
      }
    
    logInfo("ExecuBot started!");
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
    
    logInfo("ExecuBot stopped!");
    }

  /*
   */
  
  protected void loadData(InputStream ins) throws BotException
    {
    logInfo("Loading data");

    try
      {
      Document data = readXML(ins);

      Element root = data.getRootElement();

      Element cmdNode = XMLUtils.getChildElem(root, NODE_COMMAND, true);

      String cmd = XMLUtils.getTextContent(cmdNode, true);
      command = new File(cmd);
      
      List list = root.getChildren(NODE_ARG);
      Iterator iter = list.iterator();
      
      args.clear();
      
      while(iter.hasNext())
        {
        Element node = (Element)iter.next();

        String arg = XMLUtils.getTextContent(node);
        if(arg != null)
          args.add(arg);
        }
      }
    catch(InvalidXMLException ex)
      {
      throw(new BotException("Invalid XML data: " + ex.getMessage()));
      }
    catch(IOException ex)
      {
      throw(new BotException("Error reading data"));
      }
    }

  /*
   */

  protected void saveData(OutputStream outs) throws BotException
    {
    logInfo("Saving data");

    Element root = new Element("botdata");
    Document doc = new Document(root);

    if(command != null)
      {
      Element cmdNode = new Element(NODE_COMMAND);

      XMLUtils.setTextContent(cmdNode, command.getAbsolutePath());
      root.addContent(cmdNode);

      for(int i = 0; i < args.size(); i++)
        {
        String arg = args.get(i);
        
        Element node = new Element(NODE_ARG);

        XMLUtils.setTextContent(node, arg);

        root.addContent(node);
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
    try
      {
      if(cmdvec == null)
        {
        // rebuild it
        
        cmdvec = new String[args.size() + 1];
        cmdvec[0] = command.getAbsolutePath();

        int i = 1;
        for(Iterator<String> iter = args.iterator();
            iter.hasNext();
            i++)
          {
          cmdvec[i] = iter.next();
          }
        }
      
      StringBuffer sb = new StringBuffer();
      sb.append("<br>");
      
      int r = -1;
      int max = getMaxMessageLength();

      r = ProcessUtils.execute(cmdvec, sb, max);
      
      if(r == 0)
        {
        String text = sb.toString().replace("\n", "<br>");
        sendMessage(mevt.getSender(), text);
        }
      }
    catch(IOException ex)
      {
      logError(ex);
      }
    catch(BotException ex)
      {
      logError(ex.toString());
      }
    }

  /*
   */

  public PluginUI getUI()
    {
    if(ui == null)
      ui = new ExecuBotUI(this);
    
    return(ui);
    }  
  
  /*
   */

  void setCommand(File command)
    {
    this.command = command;
    cmdvec = null;
    }

  void setArguments(List<String> arguments)
    {
    args.clear();
    args.addAll(arguments);
    cmdvec = null;
    }

  File getCommand()
    {
    return(command);
    }

  List<String> getArguments()
    {
    ArrayList<String> list = new ArrayList<String>();
    list.addAll(args);

    return(list);
    }
  
  }

/* end of source file */
