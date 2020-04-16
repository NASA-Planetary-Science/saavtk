package edu.jhuapl.saavtk.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public abstract class AuthorizorSwingUtil
{
    public static AuthorizorSwingUtil of(Path passwordFilePath)
    {
        return new AuthorizorSwingUtil() {

            private final Authorizor authorizor = new Authorizor(passwordFilePath) {
                @Override
                public String getDefaultUserName()
                {
                    return "public";
                }

                @Override
                protected char[] getDefaultPassword()
                {
                    return "wide-open".toCharArray();
                }
            };

            @Override
            public Authorizor getAuthorizor()
            {
                return authorizor;
            }

        };
    }

    protected AuthorizorSwingUtil()
    {

    }

    public boolean setUpAuthorization()
    {
        Authorizor authorizor = getAuthorizor();

        UrlStatus urlStatus = authorizor.loadCredentials().getStatus();

        boolean result = false;
        if (urlStatus == UrlStatus.ACCESSIBLE)
        {
            result = true;
        }
        else if (urlStatus == UrlStatus.NOT_AUTHORIZED)
        {            
            result = updateCredentials();
        }

        return result;
    }

    public boolean updateCredentials()
    {
        if (Configuration.isHeadless())
        {
            return false;
        }

        AtomicBoolean result = new AtomicBoolean(false);
        try
        {
            Configuration.runAndWaitOnEDT(() -> {

                if (!ServerSettingsManager.instance().update().isServerAccessible())
                {
                    JOptionPane.showMessageDialog(null, "Unable to update password at this time. Please try later. See console for more details.", "Unable to connect", JOptionPane.ERROR_MESSAGE);
                    return;
                }
   
                Authorizor authorizor = getAuthorizor();

                JPanel mainPanel = new JPanel();
                mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
                JLabel promptLabel = new JLabel("<html>The Small Body Mapping Tool will work without a password, but data for some models is restricted.<br>If you have credentials to access restricted models, enter them here.</html>");
                JLabel requestAccess = new JLabel("<html><br>(Email sbmt@jhuapl.edu to request access)</html>@");

                JPanel namePanel = new JPanel();
                namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
                namePanel.add(new JLabel("Username:"));
                JTextField nameField = new JTextField(15);
                namePanel.add(nameField);

                JPanel passwordPanel = new JPanel();
                passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.X_AXIS));
                passwordPanel.add(new JLabel("Password:"));
                JPasswordField passwordField = new JPasswordField(15);
                passwordPanel.add(passwordField);

                JCheckBox rememberPasswordCheckBox = new JCheckBox("Do not prompt for a password in the future (save/clear credentials).");
                rememberPasswordCheckBox.setSelected(true);

                mainPanel.add(promptLabel);
                mainPanel.add(requestAccess);
                mainPanel.add(namePanel);
                mainPanel.add(passwordPanel);
                mainPanel.add(rememberPasswordCheckBox);

                boolean rememberPassword = false;
                String userName = null;
                char[] password = null;

                try
                {
                    boolean promptUser = true;
                    while (promptUser)
                    {
                        promptUser = false;
                        int selection = JOptionPane.showConfirmDialog(null, mainPanel, "Small Body Mapping Tool: Optional Password", JOptionPane.OK_CANCEL_OPTION);
                        rememberPassword = rememberPasswordCheckBox.isSelected();
                        userName = nameField.getText().trim();
                        password = passwordField.getPassword();
                        if (selection == JOptionPane.OK_OPTION)
                        {
                            if (userName.isEmpty())
                            {
                                userName = authorizor.getDefaultUserName();
                                Authorizor.clearArray(password);
                                password = authorizor.getDefaultPassword();
                            }

                            UrlState state = authorizor.applyCredentials(userName, password);
                            UrlStatus status = state.getStatus();
                            if (!state.wasCheckedOnline())
                            {
                                JOptionPane.showMessageDialog(null, "Unable to verify credentials at this time. Please try later. See console for more details.", "Unable to connect", JOptionPane.ERROR_MESSAGE);
                            }
                            else if (status == UrlStatus.NOT_AUTHORIZED)
                            {
                                // Try again.
                                promptLabel.setText("<html>Invalid user name or password. Try again, or click \"Cancel\" to continue without password. Some models may not be available.</html>");
                                promptUser = true;
                            }
                            else if (status != UrlStatus.ACCESSIBLE)
                            {
                                // Try again.
                                promptLabel.setText("<html>Server problem. Try again, or click \"Cancel\" to continue without password. If this persists, contact sbmt.jhuapl.edu. Some models may not be available without a password.</html>");
                                promptUser = true;
                            }
                            else
                            {
                                result.set(true);
                                
                                if (rememberPassword)
                                {
                                    try
                                    {
                                        authorizor.saveCredentials();
                                        JOptionPane.showMessageDialog(null, "Password saved.", "Password changes saved", JOptionPane.INFORMATION_MESSAGE);
                                    }
                                    catch (IOException e)
                                    {
                                        e.printStackTrace();
                                        JOptionPane.showMessageDialog(null, "Unable to update password. See console for more details.", "Failed to save password", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                                else
                                {
                                    JOptionPane.showMessageDialog(null, "Password updated for this session.", "Password updated", JOptionPane.INFORMATION_MESSAGE);
                                }
                            }
                        }
                    }
                }
                finally
                {
                    Authorizor.clearArray(password);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return result.get();
    }

    public abstract Authorizor getAuthorizor();

}
