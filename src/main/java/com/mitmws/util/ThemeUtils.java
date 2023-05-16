package com.mitmws.util;

import javax.swing.*;
/*
    - Text box hight must be consistent
    - Combobox height must match text box
    - In general: widths must be increased because the Nimbus theme is "fatter"
    - All column headers will need to be adjusted
    - Buttons must be the same height as textbox
    - Check boxes need space ( traffic tab )
    - Remove the frame on the upgrade http request
    - Highlighted rows have white font in tables
 */
public final class ThemeUtils {
    public static  void setLookAndFeel(JFrame frame) {
        // Try to get nimbus if available
        for ( UIManager.LookAndFeelInfo lookAndFeelInfo : UIManager.getInstalledLookAndFeels() ) {
            if ( lookAndFeelInfo.toString().matches("(?i).*nimbus.*")) {
                try {
                    UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
                    SwingUtilities.updateComponentTreeUI(frame);
                    break;
                } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
