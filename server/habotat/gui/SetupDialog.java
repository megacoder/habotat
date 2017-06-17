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
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

import kiwi.ui.*;
import kiwi.ui.dialog.*;
import kiwi.util.*;

/**
 *
 * @author Mark Lindner
 */

class SetupDialog extends WizardDialog
  {
  private static LocaleData loc
    = HabotatGUI.resmgr.getResourceBundle("habotat");

  /*
   */
  
  SetupDialog()
    {
    super(KiwiUtils.getPhantomFrame(),
          loc.getMessage("window.title.setup"), true,
          buildWizardView());

    setResizable(false);
    }

  /*
   */
  
  private static WizardView buildWizardView()
    {
    WizardPanelSequence seq = new WizardPanelSequence(HabotatGUI.config);

    seq.addPanel(new IntroPanel());
    seq.addPanel(new DataDirectoryPanel());
    seq.addPanel(new OptionsPanel());

    WizardView wv = new WizardView(seq);
    wv.setIcon(HabotatGUI.resmgr.getIcon("botpanel.gif"));
    
    return(wv);
    }

  /*
   */
  
  private static class IntroPanel extends WizardPanel
    {
    IntroPanel()
      {
      setTitle(loc.getMessage("heading.welcome"));
      }

    protected Component buildUI()
      {
      KPanel p = new KPanel();

      p.setLayout(new GridLayout(1, 0));

      KLabelArea la = new KLabelArea(5, 30);
      la.setText(loc.getMessage("comment.wizard_welcome"));
      p.add(la);

      return(p);
      }

    public void syncData()
      {
      // no-op
      }

    public void syncUI()
      {
      // no-op
      }
    
    }

  /*
   */
  
  private static class DataDirectoryPanel extends WizardPanel
    {
    private FileChooserField t_file;
    private KFileChooserDialog d_chooser;
    
    DataDirectoryPanel()
      {
      setTitle(loc.getMessage("heading.data_dir"));
      }

    protected Component buildUI()
      {
      KPanel p = new KPanel();

      p.setLayout(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();

      gbc.anchor = gbc.WEST;
      gbc.gridwidth = gbc.REMAINDER;

      KLabelArea la = new KLabelArea(3, 30);
      la.setText(loc.getMessage("comment.specify_data_dir"));
      
      gbc.insets = KiwiUtils.lastInsets;
      p.add(la, gbc);

      d_chooser = new KFileChooserDialog(
        KiwiUtils.getPhantomFrame(), loc.getMessage("window.title.select_dir"),
        KFileChooser.OPEN_DIALOG);
      d_chooser.setFileSelectionMode(KFileChooser.DIRECTORIES_ONLY);
      d_chooser.getFileChooser().setApproveButtonText(
        loc.getMessage("button.choose"));
      d_chooser.getFileChooser().setFileHidingEnabled(false);
      
      t_file = new FileChooserField(25, 1024, d_chooser);
      t_file.addChangeListener(new ChangeListener()
          {
          public void stateChanged(ChangeEvent evt)
            {
            fireChangeEvent();
            }
          });

      gbc.insets = KiwiUtils.lastBottomInsets;
      gbc.anchor = gbc.NORTHWEST;
      gbc.weighty = 1;
      gbc.fill = gbc.HORIZONTAL;
      gbc.gridwidth = gbc.REMAINDER;
      p.add(t_file, gbc);

      return(p);
      }

    public void syncData()
      {
      File f = t_file.getFile();

      if(f == null)
        return;

      config.putFile(HabotatGUI.CK_DATADIR, f);
      }

    public void syncUI()
      {
      File f = config.getFile(HabotatGUI.CK_DATADIR, null);
      if(f == null)
        t_file.clear();
      else
        t_file.setFile(f);
      }

    public void beginFocus()
      {
      t_file.requestFocus();
      }

    
    public boolean accept()
      {
      File f = t_file.getFile();

      if(f == null)
        return(false);

      if(f.exists())
        {
        if(! f.isDirectory())
          {
          HabotatGUI.dialogs.showMessageDialog(
            loc.getMessage("message.file_not_dir"));
          
          return(false);
          }

        if(! (f.canRead() && f.canWrite()))
          {
          HabotatGUI.dialogs.showMessageDialog(
            loc.getMessage("message.dir_access"));
          
          return(false);
          }
        }
      else
        {
        if(! HabotatGUI.dialogs.showQuestionDialog(
             loc.getMessage("message.dir_create_confirm")))
          return(false);
        else
          {
          if(! f.mkdirs())
            {
            HabotatGUI.dialogs.showMessageDialog(
              loc.getMessage("message.dir_create_failed"));

            return(false);
            }

          }
        }

      return(true);
      }

    public boolean canMoveForward()
      {
      return(t_file.getFile() != null);      
      }

    }

  /*
   */
  
  private static class OptionsPanel extends WizardPanel
    {
    private KCheckBox b_splash, b_console;
    
    OptionsPanel()
      {
      setTitle(loc.getMessage("heading.customization_opts"));
      }

    protected Component buildUI()
      {
      KPanel p = new KPanel();

      p.setLayout(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();

      gbc.anchor = gbc.WEST;
      gbc.gridwidth = gbc.REMAINDER;
      
      KLabelArea la = new KLabelArea(3, 30);
      la.setText(loc.getMessage("comment.customize"));
      
      gbc.insets = KiwiUtils.lastInsets;
      p.add(la, gbc);

      b_splash = new KCheckBox(loc.getMessage("button.show_splash"));
      p.add(b_splash, gbc);

      b_console = new KCheckBox(loc.getMessage("button.show_console"));
      gbc.insets = KiwiUtils.lastBottomInsets;
      gbc.anchor = gbc.NORTHWEST;
      gbc.weighty = 1;
      p.add(b_console, gbc);

      return(p);
      }

    public void syncData()
      {
      config.putBoolean(HabotatGUI.CK_SPLASHSCREEN, b_splash.isSelected());
      config.putBoolean(HabotatGUI.CK_CONSOLE, b_console.isSelected());
      }

    public void syncUI()
      {
      b_splash.setSelected(config.getBoolean(HabotatGUI.CK_SPLASHSCREEN,
                                             true));
      b_console.setSelected(config.getBoolean(HabotatGUI.CK_CONSOLE, false));
      }

    public void beginFocus()
      {
      b_splash.requestFocus();
      }
    
    }
  
  }

/* end of source file */
