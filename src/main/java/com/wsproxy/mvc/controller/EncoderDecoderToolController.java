package com.wsproxy.mvc.controller;

import com.wsproxy.mvc.model.EncoderDecoderToolModel;
import com.wsproxy.mvc.view.frames.FrmEncoderDecoderToolView;
import com.wsproxy.tester.PayloadEncoding;
import com.wsproxy.util.TestUtil;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.charset.StandardCharsets;

public class EncoderDecoderToolController implements PropertyChangeListener {

    private EncoderDecoderToolModel encoderDecoderToolModel;
    private FrmEncoderDecoderToolView frmEncoderDecoderToolView;

    public EncoderDecoderToolController(EncoderDecoderToolModel encoderDecoderToolModel, FrmEncoderDecoderToolView frmEncoderDecoderToolView) {
        this.encoderDecoderToolModel = encoderDecoderToolModel;
        this.frmEncoderDecoderToolView = frmEncoderDecoderToolView;
        this.encoderDecoderToolModel.addListener(this);
        initEventListeners();
    }

    void initEventListeners() {
        frmEncoderDecoderToolView.jtxtInputRaw.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                encoderDecoderToolModel.setContent(frmEncoderDecoderToolView.jtxtInputRaw.getText().getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                encoderDecoderToolModel.setContent(frmEncoderDecoderToolView.jtxtInputRaw.getText().getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                encoderDecoderToolModel.setContent(frmEncoderDecoderToolView.jtxtInputRaw.getText().getBytes(StandardCharsets.UTF_8));
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if ( "EncoderDecoderToolModel.content".equals(propertyChangeEvent.getPropertyName())) {
            frmEncoderDecoderToolView.pnlEncode.jtxtBase64.setText(TestUtil.encodePayload(PayloadEncoding.BASE64, (byte[]) propertyChangeEvent.getNewValue()));
            frmEncoderDecoderToolView.pnlEncode.jtxtURL.setText(TestUtil.encodePayload(PayloadEncoding.URL, (byte[]) propertyChangeEvent.getNewValue()));
            frmEncoderDecoderToolView.pnlEncode.jtxtURLFull.setText(TestUtil.encodePayload(PayloadEncoding.URLFULL, (byte[]) propertyChangeEvent.getNewValue()));
            frmEncoderDecoderToolView.pnlEncode.jtxtSlashX.setText(TestUtil.encodePayload(PayloadEncoding.HESCAPE, (byte[]) propertyChangeEvent.getNewValue()));
            frmEncoderDecoderToolView.pnlEncode.jtxtSlashU.setText(TestUtil.encodePayload(PayloadEncoding.UESCAPE, (byte[]) propertyChangeEvent.getNewValue()));
            frmEncoderDecoderToolView.pnlEncode.jtxtHTML.setText(TestUtil.encodePayload(PayloadEncoding.HTML, (byte[]) propertyChangeEvent.getNewValue()));
            frmEncoderDecoderToolView.pnlEncode.jtxtJavascript.setText(TestUtil.encodePayload(PayloadEncoding.JAVASCRIPT, (byte[]) propertyChangeEvent.getNewValue()));

            frmEncoderDecoderToolView.pnlDecode.jtxtBase64.setText(TestUtil.decodePayload(PayloadEncoding.BASE64, (byte[]) propertyChangeEvent.getNewValue()));
            frmEncoderDecoderToolView.pnlDecode.jtxtURL.setText(TestUtil.decodePayload(PayloadEncoding.URL, (byte[]) propertyChangeEvent.getNewValue()));
            frmEncoderDecoderToolView.pnlDecode.jtxtURLFull.setText(TestUtil.decodePayload(PayloadEncoding.URLFULL, (byte[]) propertyChangeEvent.getNewValue()));
            frmEncoderDecoderToolView.pnlDecode.jtxtSlashX.setText(TestUtil.decodePayload(PayloadEncoding.HESCAPE, (byte[]) propertyChangeEvent.getNewValue()));
            frmEncoderDecoderToolView.pnlDecode.jtxtSlashU.setText(TestUtil.decodePayload(PayloadEncoding.UESCAPE, (byte[]) propertyChangeEvent.getNewValue()));
            frmEncoderDecoderToolView.pnlDecode.jtxtHTML.setText(TestUtil.decodePayload(PayloadEncoding.HTML, (byte[]) propertyChangeEvent.getNewValue()));
            frmEncoderDecoderToolView.pnlDecode.jtxtJavascript.setText(TestUtil.decodePayload(PayloadEncoding.JAVASCRIPT, (byte[]) propertyChangeEvent.getNewValue()));
        }
    }
}
