/**
 * 
 */
package com.aeasycredit.deployplugin.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PasswordDialog extends Dialog {
    private Text txtUser;
    private Text txtPassword;
    private Button checkBox;
    private String user = "";
    private String password = "";
    private Boolean check = true;

    public PasswordDialog(Shell parentShell) {
        super(parentShell);
    }
    
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Please input your gitlab username and password:");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginRight = 5;
        layout.marginLeft = 10;
        container.setLayout(layout);

        Label lblUser = new Label(container, SWT.NONE);
        lblUser.setText("User:");

        txtUser = new Text(container, SWT.BORDER);
        txtUser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
                1, 1));
        txtUser.setText(user);
        txtUser.addModifyListener(e -> {
            Text textWidget = (Text) e.getSource();
            String userText = textWidget.getText();
            user = userText;
        });

        Label lblPassword = new Label(container, SWT.NONE);
        GridData gridDataPasswordLabel = new GridData(SWT.LEFT, SWT.CENTER, false,false);
        gridDataPasswordLabel.horizontalIndent = 1;
        lblPassword.setLayoutData(gridDataPasswordLabel);
        lblPassword.setText("Password:");

        txtPassword = new Text(container, SWT.BORDER| SWT.PASSWORD);
        txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        txtPassword.setText(password);
        txtPassword.addModifyListener(e -> {
            Text textWidget = (Text) e.getSource();
            String passwordText = textWidget.getText();
            password = passwordText;
        });

        Label lblRemember = new Label(container, SWT.NONE);
        GridData gridDataRememberLabel = new GridData(SWT.LEFT, SWT.CENTER, false,false);
        gridDataRememberLabel.horizontalIndent = 1;
        lblRemember.setLayoutData(gridDataRememberLabel);
        lblRemember.setText("Remember:");
        txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        checkBox = new Button(container,SWT.CHECK);
        checkBox.setSelection(check);
        checkBox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                Button btn = (Button) event.getSource();
                check = btn.getSelection();
            }
        });
        return container;
    }

    // override method to use "Login" as label for the OK button
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "Login", true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected Point getInitialSize() {
        return new Point(450, 300);
    }

    @Override
    protected void okPressed() {
        user = txtUser.getText();
        password = txtPassword.getText();
        super.okPressed();
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

	public Boolean getCheck() {
		return check;
	}

	public void setCheck(Boolean check) {
		this.check = check;
	}

}