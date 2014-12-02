package com.sciaps.utils;

/**
 *
 * @author sgowen
 */
public final class JFrameUtils
{
    /**
     * This method sets the icon image of the frame according to the best
     * imageIcon size requirements for the system's appearance settings. This
     * method should only be called after pack() or show() has been called for
     * the Frame.
     *
     * @param frame The Frame to set the image icon for.
     * @param image20x20 An image, 20 pixels wide by 20 pixels high
     * @param image26x26 An image, 26 pixels wide by 26 pixels high
     */
    public static void setFrameIconImage(java.awt.Frame frame, java.awt.Image image20x20, java.awt.Image image26x26)
    {
        java.awt.Insets insets = frame.getInsets();
        int titleBarHeight = insets.top;
        if (titleBarHeight == 32 && image26x26 != null)
        {
            frame.setIconImage(image26x26);
        }
        else if (image20x20 != null)
        {
            frame.setIconImage(image20x20);
        }
    }
}