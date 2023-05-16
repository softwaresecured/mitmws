package com.mitmws.mvc.view.panels.trafficpanel;
import com.mitmws.mvc.model.WebsocketFrameModel;
import com.mitmws.mvc.view.panels.PnlWebsocketFrameViewerToolbar;
import com.mitmws.mvc.view.panels.PnlWsFrameEditorToolbar;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/*
    Display formats:
        - UTF-8 String
        - Hex
 */

public class PnlWebsocketFrameView extends JPanel {

    /*
        Editor
     */
    public class HexCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JTextField component = new JTextField();
        private boolean isEditable;
        public HexCellEditor ( boolean isEditable ) {
            this.isEditable = isEditable;
        }
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                     int rowIndex, int vColIndex) {

            component.setText((String) table.getValueAt(rowIndex, vColIndex));
            component.setBackground(Color.YELLOW);
            component.setEditable(isEditable);
            component.setBorder(BorderFactory.createEmptyBorder());
            component.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    // TODO: This causes the cell's last value to be loaded into the cell that focus changed to... weird.
                    //component.setCaretPosition(0);
                }

                @Override
                public void focusLost(FocusEvent e) {
                }
            });
            /*
                Overwrite by default
                Each cell must have only 2 chars
             */
            AbstractDocument document = (AbstractDocument) component.getDocument();
            document.setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                    int length = string != null ? string.length() : 0;
                    replace(fb, offset, length, string, attr);
                }
                @Override
                public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
                    if ( isEditable && string.matches("(?i)[a-f0-9]+")) {
                        if ( component.getSelectedText() != null && component.getSelectedText().length() > 0 ) {
                            super.replace(fb, component.getSelectionStart(), 1, string, attrs);
                        }
                        else {
                            super.replace(fb, offset, length+1, string, attrs);
                        }
                    }
                }

                @Override
                public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                    super.remove(fb, offset, length);
                }
            });

            return ( JComponent ) component;
        }
        public Object getCellEditorValue() {
            return component.getText().toUpperCase();
        }
    }


    public PnlWsFrameEditorToolbar pnlWsFrameEditorToolbar = new PnlWsFrameEditorToolbar();
    public PnlWebsocketFrameViewerToolbar pnlWebsocketFrameViewerToolbar = null;

    private WebsocketFrameModel websocketFrameModel;
    public JTextArea jtxtFramePayload = null; // Text
    public JTextArea jtxtFramePayloadHexStr;
    public JTable tblPayloadHex;

    public JScrollPane scrollTextFrameViewer;
    public JScrollPane scrollTextFrameHexStr;
    public JScrollPane scrollTextFrameHex;
    public JPanel pnlHexViewer;
    public JPanel pnlTextViewer;
    public JLabel lblDirection = new JLabel("<>");
    public JLabel lblPayloadLen = new JLabel(" ( 0 bytes )");

    private byte[] rawPayload = null;
    public PnlWebsocketFrameView( WebsocketFrameModel websocketFrameModel ) {
        this.websocketFrameModel = websocketFrameModel;
        initLayout();
    }

    public void resetUi() {

    }


    public void initLayout() {
        /*
            The table cells go into edit mode when selected
         */
        tblPayloadHex = new JTable();
        for ( int i = 0; i < tblPayloadHex.getColumnCount(); i++ ) {
            tblPayloadHex.getColumnModel().getColumn(i).setCellEditor(new HexCellEditor(websocketFrameModel.isEditable()));
        }
        //tblPayloadHex.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tblPayloadHex.setModel(websocketFrameModel.getPayloadHexModel());
        tblPayloadHex.setSelectionBackground(Color.YELLOW);
        tblPayloadHex.setSelectionForeground(Color.BLACK);
        tblPayloadHex.setCellSelectionEnabled(true);



        tblPayloadHex.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPayloadHex.setTableHeader(null);
        jtxtFramePayload = new JTextArea();
        jtxtFramePayloadHexStr = new JTextArea();
        jtxtFramePayload.setEditable(websocketFrameModel.isEditable());
        jtxtFramePayloadHexStr.setEditable(false);

        /*
            Hex viewer
         */
        jtxtFramePayloadHexStr.setLineWrap(true);
        scrollTextFrameHexStr = new JScrollPane(jtxtFramePayloadHexStr);
        scrollTextFrameHexStr.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTextFrameHexStr.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollTextFrameHex = new JScrollPane(tblPayloadHex);
        scrollTextFrameHex.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTextFrameHex.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        pnlHexViewer = new JPanel();
        pnlHexViewer.setVisible(false);
        pnlHexViewer.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH ;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        pnlHexViewer.add(scrollTextFrameHexStr, gbc);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        pnlHexViewer.add(scrollTextFrameHex, gbc);
        scrollTextFrameHex.setPreferredSize(new Dimension(websocketFrameModel.HEX_VIEWER_COLS*30,scrollTextFrameHex.getHeight()));
        /*
            Text viewer
         */
        jtxtFramePayload.setLineWrap(true);
        scrollTextFrameViewer = new JScrollPane(jtxtFramePayload);
        scrollTextFrameViewer.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTextFrameViewer.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        pnlTextViewer = new JPanel();
        pnlTextViewer.setVisible(false);
        pnlTextViewer.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        pnlTextViewer.add(scrollTextFrameViewer, gbc);


        /*
            Layout
         */
        pnlWebsocketFrameViewerToolbar = new PnlWebsocketFrameViewerToolbar(websocketFrameModel);
        this.setMinimumSize(new Dimension(jtxtFramePayload.getWidth(),100));
        pnlWsFrameEditorToolbar.setVisible(websocketFrameModel.isEditable());
        lblDirection.setVisible(false);
        lblPayloadLen.setVisible(false);
        // combine the toolbars they look ugly when stacked vertically
        JPanel pnlToolbar = new JPanel();
        pnlToolbar.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0;
        gbc.weightx = 0;
        pnlToolbar.add(pnlWsFrameEditorToolbar, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weighty = 0;
        gbc.weightx = 0;
        pnlToolbar.add(lblDirection, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weighty = 0;
        gbc.weightx = 0;
        pnlToolbar.add(lblPayloadLen, gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weighty = 0;
        gbc.weightx = 1;
        pnlToolbar.add(new JPanel(), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.weighty = 0;
        gbc.weightx = 0;
        pnlToolbar.add(pnlWebsocketFrameViewerToolbar, gbc);

        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0;
        gbc.weightx = 1;
        add(pnlToolbar, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(pnlTextViewer,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(pnlHexViewer,gbc);
    }
}
