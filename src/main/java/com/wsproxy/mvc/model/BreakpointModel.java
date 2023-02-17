package com.wsproxy.mvc.model;

import com.wsproxy.httpproxy.BreakPointItem;
import com.wsproxy.httpproxy.websocket.WebsocketFrame;

import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.table.DefaultTableModel;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

public class BreakpointModel {
    private DefaultTableModel breakpointTableModel;
    private DefaultTableModel breakpointQueueTableModel;
    private ArrayList<BreakPointItem> breakpointItems = new ArrayList<BreakPointItem>();
    private BreakPointItem currentBreakpointItem = null;
    private HashMap<String,WebsocketFrame> trappedFrames = new HashMap<String,WebsocketFrame>();

    private SwingPropertyChangeSupport eventEmitter;
    public BreakpointModel() {
        breakpointTableModel = new DefaultTableModel();
        for ( String col: new String[] { "id","Name", "Conversation scope","Payload scope"}) {
            breakpointTableModel.addColumn(col);
        }
        breakpointQueueTableModel = new DefaultTableModel();
        for ( String col: new String[] { "id","Host", "Direction","Payload"}) {
            breakpointQueueTableModel.addColumn(col);
        }
        eventEmitter = new SwingPropertyChangeSupport(this);

    }

    public DefaultTableModel getBreakpointTableModel() {
        return breakpointTableModel;
    }

    public void setBreakpointTableModel(DefaultTableModel breakpointTableModel) {
        this.breakpointTableModel = breakpointTableModel;
    }

    public DefaultTableModel getBreakpointQueueTableModel() {
        return breakpointQueueTableModel;
    }

    public void setBreakpointQueueTableModel(DefaultTableModel breakpointQueueTableModel) {
        this.breakpointQueueTableModel = breakpointQueueTableModel;
    }

    // Checks if the frame matches any breakpoint rules
    public boolean checkBreakpoints (WebsocketFrame frame, String upgradeUrl ) {
        for ( BreakPointItem breakPointItem : breakpointItems ) {
            if ( breakPointItem.match(frame,upgradeUrl)) {
                return true;
            }
        }
        return false;
    }

    // Traps a message. Called from WebsocketSession
    public void frameTrap( WebsocketFrame frame ) {
        frame.setTrapped(true);
        trappedFrames.put(frame.getMessageUUID(),frame);
        eventEmitter.firePropertyChange("BreakpointModel.trappedFrame", null, frame.getMessageUUID());
    }

    // Releases a message. If it exists and the trap has been released. Called from WebsocketSession.
    public WebsocketFrame frameRelease( String id ) {
        WebsocketFrame releasedFrame = null;
        if ( trappedFrames.get(id) != null ) {
            if ( !trappedFrames.get(id).isTrapped() ) {
                releasedFrame = trappedFrames.get(id);
                trappedFrames.remove(id);
                eventEmitter.firePropertyChange("BreakpointModel.releasedFrame", null, id);
            }
        }
        return releasedFrame;
    }

    // Drops a frame ( mark for deletion ). Called from WebsocketSession
    public void dropFrame( String id ) {
        if ( trappedFrames.get(id) != null ) {
            if ( trappedFrames.get(id).isTrapped() && trappedFrames.get(id).isDropped()) {
                trappedFrames.remove(id);
                eventEmitter.firePropertyChange("BreakpointModel.droppedFrameRemoved", null, id);
            }
        }
    }

    // Drops a frame ( Called from UI )
    public void dropFrameById( String id ) {
        if ( trappedFrames.get(id) != null ) {
            if ( trappedFrames.get(id).isTrapped() && !trappedFrames.get(id).isDropped()) {
                trappedFrames.get(id).setDropped(true);
                eventEmitter.firePropertyChange("BreakpointModel.droppedFrame", null, id);
            }
        }
    }

    public HashMap<String,WebsocketFrame> getTrappedFrameMap() {
        return trappedFrames;
    }

    public ArrayList<WebsocketFrame> getTrappedFrames() {
        ArrayList<WebsocketFrame> frames = new ArrayList<WebsocketFrame>();
        for ( String id : trappedFrames.keySet() ) {
            frames.add(trappedFrames.get(id));
        }
        return frames;
    }

    public BreakPointItem getCurrentBreakpointItem() {
        return currentBreakpointItem;
    }

    public void setCurrentBreakpointItem(BreakPointItem currentBreakpointItem) {
        this.currentBreakpointItem = currentBreakpointItem;
        eventEmitter.firePropertyChange("BreakpointModel.currentBreakpointItem", null, currentBreakpointItem);
    }

    public ArrayList<BreakPointItem> getBreakpointItems() {
        return breakpointItems;
    }

    public void setBreakpointItems(ArrayList<BreakPointItem> breakpointItems) {
        this.breakpointItems = breakpointItems;
    }


    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }
}
