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

import java.io.*;
import java.net.URL;

import kiwi.util.plugin.*;

/**
 *
 * @author Mark Lindner
 */

public class PluginDef extends Def
  {
  private File file;
  private Plugin plugin = null;
  private int useRefCount = 0;
  private Object instance = null;

  /*
   */
  
  public PluginDef(File file, String className)
    {
    this.file = file;
    }

  /*
   */
  
  public PluginDef(File file, Plugin plugin)
    {
    this.file = file;
    this.plugin = plugin;
    }

  /*
   */
  
  public String getName()
    {
    return(plugin.getName());
    }

  /*
   */
  
  public File getFile()
    {
    return(file);
    }

  /*
   */
  
  public void reload() throws PluginException
    {
    plugin.reload();
    instance = null;
    }

  /*
   */
  
  public Plugin getPlugin()
    {
    return(plugin);
    }

  /*
   */

  public String getVersion()
    {
    return(plugin.getVersion());
    }

  /*
   */

  public URL getHelpURL()
    {
    return(plugin.getHelpURL());
    }
  
  /*
   */
  
  public String toString()
    {
    return(plugin.getName());
    }

  /*
   */

  public void addUseReference()
    {
    useRefCount++;
    }

  /*
   */
  
  public void removeUseReference()
    {
    if(useRefCount > 0)
      useRefCount--;
    }

  /*
   */

  public boolean isInUse()
    {
    return(useRefCount != 0);
    }
  
  }

/* end of source file */
