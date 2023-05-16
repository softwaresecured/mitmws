package com.mitmws.mvc.controller;

import com.mitmws.mvc.model.MainModel;
import com.mitmws.mvc.view.frames.FrmProjectDataExplorer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectDataExplorerController implements PropertyChangeListener {

    private MainModel mainModel;
    private FrmProjectDataExplorer frmProjectDataExplorer;

    public ProjectDataExplorerController(MainModel mainModel, FrmProjectDataExplorer frmProjectDataExplorer) {
        this.mainModel = mainModel;
        this.frmProjectDataExplorer = frmProjectDataExplorer;
        this.mainModel.getProjectDataExplorerModel().addListener(this);
        loadTableList();
        initEventListeners();
    }

    public void initEventListeners() {
        frmProjectDataExplorer.btnQuery.addActionListener( actionEvent -> {
            try {
                mainModel.getProjectDataExplorerModel().setError(null);
                ResultSet rs = mainModel.getProjectModel().getProjectDataService().runRawQuery(frmProjectDataExplorer.jtxtSQL.getText());

                DefaultTableModel tableModel = new DefaultTableModel();
                for ( int i = 0; i < rs.getMetaData().getColumnCount(); i++ ) {
                    String colName = String.format("Col#%d", i);
                    if ( rs.getMetaData().getColumnName(i+1) != null ) {
                        colName = rs.getMetaData().getColumnName(i+1);
                    }
                    tableModel.addColumn(colName);
                }
                while ( rs.next() ) {
                    Object row[] = new Object[rs.getMetaData().getColumnCount()];
                    for ( int i = 0; i < rs.getMetaData().getColumnCount(); i++ ) {
                        String cell = "NULL";
                        if ( rs.getObject(i+1) != null ) {
                            cell = rs.getString(i+1);
                        }
                        row[i] = cell;
                    }
                    tableModel.addRow(row);
                }
                frmProjectDataExplorer.tblResults.setModel(tableModel);

            } catch (SQLException e) {
                mainModel.getProjectDataExplorerModel().setError(e.getMessage());
            }
        });
    }

    public void loadTableList() {
        String tables[] = mainModel.getProjectModel().getProjectDataService().getTableNames();
        for ( String table : tables ) {
            JLabel lbl = new JLabel(String.format("<html><b><u>[%s]</u></b></html>", table));
            lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    String pattern = "\\[(.*?)\\]";
                    Pattern r = Pattern.compile(pattern);
                    Matcher m = r.matcher(lbl.getText());

                    if (m.find( )) {

                        if ( frmProjectDataExplorer.jtxtSQL.getText().length() == 0 ) {
                            frmProjectDataExplorer.jtxtSQL.append("select * from ");
                        }

                        if ( !frmProjectDataExplorer.jtxtSQL.getText().endsWith(" ")) {
                            frmProjectDataExplorer.jtxtSQL.append(" ");
                        }
                        frmProjectDataExplorer.jtxtSQL.append(m.group(1));
                    }
                }
            });
            frmProjectDataExplorer.pnlTableList.add(lbl);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if ( "ProjectDataExplorerModel.currentError".equals(propertyChangeEvent.getPropertyName())) {
            if ( propertyChangeEvent.getNewValue() != null ) {
                frmProjectDataExplorer.pnlError.setVisible(true);
                frmProjectDataExplorer.jtxtError.setText((String) propertyChangeEvent.getNewValue());
            }
            else {
                frmProjectDataExplorer.pnlError.setVisible(false);
            }
        }
    }
}
