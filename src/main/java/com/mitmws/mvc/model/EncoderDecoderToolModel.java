package com.mitmws.mvc.model;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;

public class EncoderDecoderToolModel {
    private byte[] content = null;
    private SwingPropertyChangeSupport eventEmitter;
    public EncoderDecoderToolModel() {
        eventEmitter = new SwingPropertyChangeSupport(this);
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
        eventEmitter.firePropertyChange("EncoderDecoderToolModel.content", null, content);
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }
}
