/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2023 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ext.postgresql.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.postgresql.PostgreMessages;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreRole;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreTablespace;
import org.jkiss.dbeaver.model.runtime.AbstractJob;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.dialogs.BaseDialog;
import org.jkiss.dbeaver.utils.GeneralUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * PostgreCreateExtensionDialog
 */
public class PostgreCreateTablespaceDialog extends BaseDialog
{
    private static final String DIALOG_ID = "DBeaver.PostgreCreateTablespaceDialog";//$NON-NLS-1$

    private final PostgreTablespace newtablespace;
    private List<PostgreRole> allUsers;
    private PostgreRole owner;
    private String name = null;
    private String loc = null;
    private String options = null;

    public PostgreCreateTablespaceDialog(Shell parentShell, PostgreTablespace tablespace) {
        super(parentShell, PostgreMessages.dialog_create_tablespace_title, null);
        this.newtablespace = tablespace;
    }

    protected IDialogSettings getDialogBoundsSettings()
    {
        return UIUtils.getDialogSettings(DIALOG_ID);
    }

    private void checkEnabled() {
            getButton(IDialogConstants.OK_ID).setEnabled(name != null && !name.isEmpty() && loc != null && !loc.isEmpty()); 
    }

    @Override
    protected Composite createDialogArea(Composite parent) {
        final Composite composite = super.createDialogArea(parent);

        final Composite group = new Composite(composite, SWT.NONE);
        group.setLayout(new GridLayout(2, false));
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 600;
        gd.heightHint = 200;
        gd.verticalIndent = 0;
        gd.horizontalIndent = 0;
        group.setLayoutData(gd);

        UIUtils.createLabelText(group,PostgreMessages.dialog_create_tablespace_database, newtablespace.getDatabase().getName(), SWT.BORDER | SWT.READ_ONLY); //$NON-NLS-2$

        final Text nameText = UIUtils.createLabelText(group, PostgreMessages.dialog_create_tablespace_name, ""); //$NON-NLS-2$
        nameText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                name = nameText.getText().trim();
                checkEnabled();
            }
        });
        
        final Combo userCombo = UIUtils.createLabelCombo(group, PostgreMessages.dialog_create_tablespace_owner, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);

        userCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                owner = allUsers.get(userCombo.getSelectionIndex());
                checkEnabled();
            }
        });
          
        final Text locText = UIUtils.createLabelText(group, PostgreMessages.dialog_create_tablespace_loc, ""); //$NON-NLS-2$
        locText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                loc = locText.getText();
                checkEnabled();
            }
        });

        final Text optionsText = UIUtils.createLabelText(group, PostgreMessages.dialog_create_tablespace_options, ""); //$NON-NLS-2$
        optionsText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                options = optionsText.getText();
            }
        });

        
        new AbstractJob("Load users") {

            @Override
            protected IStatus run(DBRProgressMonitor monitor) {
                try {
                    final List<String> userNames = new ArrayList<>();
                    allUsers = new ArrayList<>(newtablespace.getDatabase().getUsers(monitor));
                    final PostgreRole dba = newtablespace.getDatabase().getDBA(monitor);
                    final String defUserName = dba == null ? "" : dba.getName(); //$NON-NLS-1$

                    UIUtils.syncExec(() -> {
                        for (PostgreRole authId : allUsers) {
                            String name = authId.getName();
                            userCombo.add(name);
                            if (name.equals(defUserName)) {
                                owner = authId;
                            }
                        }
                        userCombo.setText(defUserName);
                    });
                } catch (DBException e) {
                    return GeneralUtils.makeExceptionStatus(e);
                }
                return Status.OK_STATUS;
            }
        }.schedule();

        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent)
    {
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    public PostgreRole getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getLoc() {
        return loc;
    }

    protected String getOptions() {
        return options;
    }
    
    
    
}
