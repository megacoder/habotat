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

package habotat.api;

import javax.swing.*;

/** An interface representing a user interface component for
 * configuring a plugin (a Bot or an IM Adapter). For Bots, this
 * component appears in the Control Panel tab of the Bot Properties
 * dialog in the Habotat UI.  For IM Adapters, it appears in the
 * Adapter Configuration tab of the Account Properties dialog in the
 * Habotat UI.
 *
 * @author Mark Lindner
 */

public interface PluginUI
  {

  /** Get the actual UI component. */
  
  public JComponent getComponent();

  /** Called when the input should be validated. */
  
  public boolean accept();

  /** Called when the input should be applied/committed. */

  public void commit();
  
  /** Called when changes should be discarded. */
  
  public void cancel();

  }

/* end of source file */
