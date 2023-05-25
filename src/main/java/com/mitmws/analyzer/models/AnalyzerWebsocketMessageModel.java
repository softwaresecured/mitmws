package com.mitmws.analyzer.models;

import com.mitmws.httpproxy.trafficlogger.WebsocketDirection;
import com.mitmws.httpproxy.trafficlogger.WebsocketTrafficRecord;
import com.mitmws.tester.TargetLocator;
import com.mitmws.tester.TestTarget;
import com.mitmws.util.GuiUtils;
import com.mitmws.util.HashUtils;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class AnalyzerWebsocketMessageModel {
    private String id;
    private String tokenizedMessageHash = null;
    private String tokenizedMessage = null;
    private HashMap<String,AnalyzerParameterModel> parameters = new HashMap<>();
    private TargetLocator targetLocator = new TargetLocator();
    private ArrayList<WebsocketDirection> observedDirection = new ArrayList<WebsocketDirection>();
    public AnalyzerWebsocketMessageModel(WebsocketTrafficRecord websocketTrafficRecord ) throws AnalyzerException {
        id = UUID.randomUUID().toString();
        tokenizeMessage(websocketTrafficRecord);
        observedDirection.add(websocketTrafficRecord.getFrame().getDirection());
        try {
            tokenizedMessageHash = HashUtils.md5sum(tokenizedMessage.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new AnalyzerException("Failed to generate message hash");
        }
    }

    // A unique UUID for the message
    public String getId() {
        return id;
    }

    // An md5sum hash of the tokenized message
    public String getTokenizedMessageHash() {
        return tokenizedMessageHash;
    }

    // Merges another AnalyzerWebsocketMessageModel into this one
    private void merge( AnalyzerWebsocketMessageModel analyzerWebsocketMessageModel ) {

    }

    // Extracts parameters into a AnalyzerParameterModel using target locator
    private void extractParameters( WebsocketTrafficRecord websocketTrafficRecord ) {

    }

    // Generates a name for a parameter name
    private String getTokenName( String parameterName ) {
        return String.format("@__%s__@", parameterName);
    }

    // Generates a string representing the message
    private void tokenizeMessage(WebsocketTrafficRecord websocketTrafficRecord) throws AnalyzerException {
        if ( websocketTrafficRecord.getFrame().getPayloadUnmasked() != null ) {
            ArrayList<TestTarget> targets = targetLocator.getAllTargets(new String(websocketTrafficRecord.getFrame().getPayloadUnmasked()));
            if ( targets.size() > 0 ) {
                Collections.sort(targets, Comparator.comparing(TestTarget::getStartPos));
                tokenizedMessage = new String(websocketTrafficRecord.getFrame().getPayloadUnmasked());
                for ( int i = targets.size()-1; i >= 0; i--) {
                    tokenizedMessage = String.format("%s%s%s",
                            tokenizedMessage.substring(0,targets.get(i).getStartPos()),
                            getTokenName(targets.get(i).getTargetName()),
                            tokenizedMessage.substring(targets.get(i).getEndPos())
                    );
                }
                tokenizedMessage = GuiUtils.getBinPreviewStr(tokenizedMessage.getBytes(StandardCharsets.UTF_8));
            }
            else {
                throw new AnalyzerException("No targets");
            }

        }
        else {
            throw new AnalyzerException("No payload to analyze");
        }
    }

    public String getTokenizedMessage() {
        return tokenizedMessage;
    }
}
