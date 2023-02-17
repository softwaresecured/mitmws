package com.wsproxy.mvc.view.panels.manualtester;
import com.wsproxy.mvc.model.ManualTesterModel;
import javax.swing.*;
import java.awt.*;
import java.util.EventObject;

public class PnlConversationBuilder extends JPanel {
    private JPanel pnlWsConversation = null;
    public JTable tblWebsocketConversation = null;
    public PnlWsConversationToolbar pnlWsConversationToolbar = null;
    public PnlTestStepEditor pnlTestStepEditor = null;
    public JSplitPane spltConvBuilder;
    private ManualTesterModel manualTesterModel;
    public PnlConversationBuilder( ManualTesterModel manualTesterModel) {
        this.manualTesterModel = manualTesterModel;
        initLayout();
    }
    public void initLayout() {
        tblWebsocketConversation = new JTable(manualTesterModel.getWebsocketConversationTableModel()) {
            public boolean editCellAt(int row, int column, EventObject e) {
                return false;
            }
        };
        tblWebsocketConversation.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] wsConvoColWidths = { -1,-1,-1,75,75,25,35,35,35,35,35,90,70,70};
        for ( int i = 0; i < wsConvoColWidths.length; i++ ) {
            tblWebsocketConversation.getColumnModel().getColumn(i).setMinWidth(wsConvoColWidths[i]);
            tblWebsocketConversation.getColumnModel().getColumn(i).setMaxWidth(wsConvoColWidths[i]);
            tblWebsocketConversation.getColumnModel().getColumn(i).setPreferredWidth(wsConvoColWidths[i]);
        }
        // Conversation
        pnlTestStepEditor = new PnlTestStepEditor();
        pnlWsConversationToolbar = new PnlWsConversationToolbar();
        JScrollPane scrollConversationViewer = new JScrollPane(tblWebsocketConversation);
        scrollConversationViewer.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollConversationViewer.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        pnlWsConversation = new JPanel();
        pnlWsConversation.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0;
        gbc.weightx = 1;
        pnlWsConversation.add(pnlWsConversationToolbar, gbc);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.weightx = 1;
        pnlWsConversation.add(scrollConversationViewer, gbc);


        spltConvBuilder = new JSplitPane(JSplitPane.VERTICAL_SPLIT,pnlWsConversation,pnlTestStepEditor);
        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(spltConvBuilder, gbc);

        int height = (int)getHeight()/2;
        pnlWsConversation.setPreferredSize(new Dimension(getWidth(),height));
        pnlTestStepEditor.setPreferredSize(new Dimension(getWidth(),height));
        spltConvBuilder.setResizeWeight(0.5);
        spltConvBuilder.setDividerLocation(0.5);
    }
}
