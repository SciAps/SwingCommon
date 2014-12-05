package com.sciaps.common.swing.utils;

import java.awt.BorderLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

/**
 *
 * @author sgowen
 */
public final class JDialogUtils
{
    public static JDialog createDialogWithMessage(JFrame jFrame, String message)
    {
        final JDialog progressDialog = new JDialog(jFrame, message, true);
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setIndeterminate(true);
        progressDialog.add(BorderLayout.CENTER, progressBar);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progressDialog.setSize(600, 160);
        progressDialog.setLocationRelativeTo(jFrame);

        return progressDialog;
    }
}