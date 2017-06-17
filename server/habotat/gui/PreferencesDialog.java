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
import javax.swing.border.*;

import kiwi.ui.*;
import kiwi.ui.dialog.*;
import kiwi.util.*;

/**
 * @author Mark Lindner
 */

class PreferencesDialog extends ComponentDialog implements ActionListener
  {
  private JCheckBox b_splash, b_console, b_plaf;
  private ButtonGroup group;
  private TextureSwatch textures[];
  private String textureNames[];
  private static Dimension size = new Dimension(75, 75);
  private static LocaleData loc
    = HabotatGUI.resmgr.getResourceBundle("habotat");
   
  /**
   */
   
  PreferencesDialog(Frame parent)
    {
    super(parent, loc.getMessage("window.title.preferences"), true);
    setResizable(false);

    pack();
    }

  /**
   */
   
  protected Component buildDialogUI()
    {
    setComment(loc.getMessage("window.comment.preferences"));
      
    KPanel main = new KPanel();
    GridBagLayout gb = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    main.setLayout(gb);
      
    gbc.anchor = gbc.WEST;
    gbc.fill = gbc.HORIZONTAL;
    gbc.gridwidth = gbc.REMAINDER;
    gbc.insets = KiwiUtils.lastInsets;
    main.add(new JSeparator(), gbc);

    KLabelArea la = new KLabelArea(2, 15);
    la.setText(loc.getMessage("comment.settings_take_effect"));
    main.add(la, gbc);
    
    KLabel label;

    b_console = new JCheckBox(loc.getMessage("button.show_console"));
    b_console.setOpaque(false);
    main.add(b_console, gbc);
      
    b_splash = new JCheckBox(loc.getMessage("button.show_splash"));
    b_splash.setOpaque(false);

    gbc.fill = gbc.NONE;
    gbc.insets = KiwiUtils.lastInsets;
    main.add(b_splash, gbc);

    label = new KLabel(loc.getMessage("label.texture"));

    main.add(label, gbc);
      
    textureNames = new String[] { "clouds.jpg", "steelplates2.gif", "gray.jpg",
                                  "rain.gif", "sand.jpg", "coolgray.gif",
                                  "water2.jpg", "blue.jpg", "greymarble.jpg" };
      
    textures = new TextureSwatch[textureNames.length];
    for(int i = 0; i < textures.length; i++)
      {
      textures[i] = new TextureSwatch(textureNames[i]);
      }

    KPanel p_textures = new KPanel();
    p_textures.setLayout(new GridLayout(0, 3, 15, 5));

    group = new ButtonGroup();
    for(int i = 0; i < textures.length; i++)
      {
      p_textures.add(textures[i]);
      group.add(textures[i].button);
      }

    main.add(p_textures, gbc);

    b_plaf = new JCheckBox(loc.getMessage("button.native_plaf"));
    b_plaf.setOpaque(false);
    main.add(b_plaf, gbc);

    gbc.insets = KiwiUtils.lastBottomInsets;
    gbc.fill = gbc.HORIZONTAL;
    main.add(new JSeparator(), gbc);
      
    return(main);
    }

  /**
   */
  
  protected boolean accept()
    {
    HabotatGUI.config.putBoolean(HabotatGUI.CK_SPLASHSCREEN,
                                 b_splash.isSelected());

    HabotatGUI.config.putBoolean(HabotatGUI.CK_CONSOLE,
                                 b_console.isSelected());
      
    for(int i = 0; i < textures.length; i++)
      {
      if(textures[i].button.isSelected())
        {
        HabotatGUI.config.putString(HabotatGUI.CK_TEXTURE,
                                    textureNames[i]);
        break;
        }
      }

    HabotatGUI.config.putBoolean(HabotatGUI.CK_NATIVEPLAF,
                                 b_plaf.isSelected());
      
    return(true);
    }
   
  /**
   */
   
  public void setVisible(boolean flag)
    {
    if(flag)
      {
      b_console.setSelected(HabotatGUI.config
                            .getBoolean(HabotatGUI.CK_CONSOLE, true));
         
      b_splash.setSelected(HabotatGUI.config
                           .getBoolean(HabotatGUI.CK_SPLASHSCREEN, true));

      String texture = HabotatGUI.config.getString(
        HabotatGUI.CK_TEXTURE, null);

      if(texture == null)
        textures[0].button.setSelected(true);
      else
        {
        for(int i = 0; i < textureNames.length; i++)
          {
          if(textureNames[i].equals(texture))
            {
            textures[i].button.setSelected(true);
            break;
            }
          }
        }

      b_console.requestFocus();
      }

    b_plaf.setSelected(HabotatGUI.config
                       .getBoolean(HabotatGUI.CK_NATIVEPLAF, false));
      
    super.setVisible(flag);
    }
   
  /**
   */
   
  public void actionPerformed(ActionEvent evt)
    {
    Object o = evt.getSource();
    }
   
  /**
   */

  private class TextureSwatch extends KPanel
    {
    KRadioButton button;
      
    TextureSwatch(String texture)
      {
      setLayout(new BorderLayout(0, 0));

      KPanel swatch = new KPanel(KiwiUtils.getResourceManager()
                                 .getTexture(texture));
      swatch.setMinimumSize(size);
      swatch.setPreferredSize(size);
      swatch.setSize(size);
      swatch.setBorder(new LineBorder(Color.black));
      add("Center", swatch);

      button = new KRadioButton("");
      button.setOpaque(false);

      add("West", button);
      }
    }
   
  }

/* end of source file */
