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

package habotat.gui;

import java.awt.*;
import java.io.*;

import habotat.engine.*;
import habotat.api.*;

import gnu.getopt.*;

import kiwi.io.*;
import kiwi.ui.*;
import kiwi.ui.dialog.*;
import kiwi.util.*;

/**
 *
 * @author Mark Lindner
 */

public class HabotatGUI
  {
  static HabotatFrame appFrame;
  static HabotatServer server;
  static ResourceManager resmgr = new ResourceManager(HabotatGUI.class);
  static LoggingEndpoint log = new RawLoggingEndpoint();
  static ConfigFile config;
  private static final String CONFIG_FILE = ".habotat.cfg";
  static final String CK_APPX = "habotat.gui.x", CK_APPY = "habotat.gui.y",
    CK_APPWIDTH = "habotat.gui.width", CK_APPHEIGHT = "habotat.gui.height",
    CK_SPLASHSCREEN = "habotat.gui.show_splash",
    CK_CONSOLE = "habotat.gui.open_console",
    CK_DATADIR = "habotat.gui.data_dir", CK_TEXTURE = "habotat.gui.texture",
    CK_NATIVEPLAF = "habotat.gui.native_plaf";
  static DialogSet dialogs = DialogSet.getInstance();
  private static LocaleData loc
    = HabotatGUI.resmgr.getResourceBundle("habotat");

  /**
   */
  
  public static void main(String args[])
    {
    Getopt opts = new Getopt("habotat", args, "vhN");
    int c;
    String arg;
    boolean err = false, nativeplaf_sw = false;
    File dataDir = null;

    while((c = opts.getopt()) != -1)
      {
      switch(c)
        {
        case 'v':
          break;

        case 'h':
          break;

        case 'N':
          nativeplaf_sw = true;
          break;

        case '?':
          err = true;
          break;
        }
      }

    if(err)
      {
      System.err.println(loc.getMessage("error.usage"));
      System.exit(1);
      }

    File cf = new File(System.getProperty("user.home", ".")
                       + File.separator + CONFIG_FILE);
    config = new ConfigFile(cf);

    try
      {
      config.load();
      }
    catch(FileNotFoundException ex)
      {
      if(nativeplaf_sw)
        KiwiUtils.setNativeLookAndFeel();
      
      SetupDialog wd = new SetupDialog();

      KiwiUtils.centerWindow(wd);
      wd.setVisible(true);

      if(wd.isCancelled())
        System.exit(0);

      saveConfig();
      }
    catch(IOException ex)
      {
      System.err.println(
        loc.getMessage(
          "error.config_load_failed",
          new Object[] { cf.getAbsolutePath(), ex.getMessage() }));
      System.exit(1);
      }

    if(config.getBoolean(CK_SPLASHSCREEN, true))
      {
      SplashScreen splash = new SplashScreen(resmgr.getImage("botlogo.gif"),
                                             loc.getMessage("message.splash"));
      
      splash.setBackground(Color.white);
      splash.setDelay(3);
      splash.setVisible(true);
      }
    
    dataDir = config.getFile(CK_DATADIR, new File("."));

    try
      {
      server = new HabotatServer(dataDir);
      }
    catch(Exception ex)
      {
      ex.printStackTrace();
      System.exit(1);
      }

    boolean nativeplaf = config.getBoolean(CK_NATIVEPLAF, nativeplaf_sw);
    config.putBoolean(CK_NATIVEPLAF, nativeplaf);
    
    if(nativeplaf)
      {
      UIChangeManager.getInstance().setDefaultTexture(null);
      KiwiUtils.setNativeLookAndFeel();
      }
    else
      {
      KiwiUtils.setDefaultLookAndFeel();
      UIChangeManager.getInstance().setDefaultTexture(
        KiwiUtils.getResourceManager().getTexture(
          config.getString(CK_TEXTURE, "clouds.jpg")));
      }

    appFrame = new HabotatFrame();
    Point loc = appFrame.getLocation();        
    appFrame.setSize(config.getInt(CK_APPWIDTH, 400),
                     config.getInt(CK_APPHEIGHT, 375));
    appFrame.setLocation(new Point(config.getInt(CK_APPX, loc.x),
                                   config.getInt(CK_APPY, loc.y)));

    appFrame.setVisible(true);
    }

  /*
   */
  
  static void shutdown()
    {
    appFrame.setVisible(false);
    appFrame.dispose();

    server.saveData();

    Point loc = appFrame.getLocation();
    Dimension dim = appFrame.getSize();
    config.putInt(CK_APPX, loc.x);
    config.putInt(CK_APPY, loc.y);
    config.putInt(CK_APPWIDTH, dim.width);
    config.putInt(CK_APPHEIGHT, dim.height);

    saveConfig();

    System.exit(0);
    }

  /*
   */

  static void saveConfig()
    {
    try
      {
      config.store();
      }
    catch(IOException ex)
      {
      System.err.println(loc.getMessage("error.config_save_failed",
                                        ex.getMessage()));
      }
    }

  /*
   */

  static void logInfo(String text)
    {
    log.logMessage(log.INFO, text);
    }

  /*
   */

  static void logWarning(String text)
    {
    log.logMessage(log.WARNING, text);
    }

  /*
   */

  static void logError(String text)
    {
    log.logMessage(log.ERROR, text);
    }

  }

/* end of source file */
