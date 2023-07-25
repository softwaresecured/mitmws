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
    private String associatedHttpMessageModelId = null;
    private String tokenizedMessageHash = null;
    private String tokenizedMessage = null;
    private String samplePayloadText = null;
    private HashMap<String,AnalyzerParameterModel> parameters = new HashMap<>();
    private TargetLocator targetLocator = new TargetLocator();
    private ArrayList<TestTarget> targets = null;
    private ArrayList<WebsocketDirection> observedDirection = new ArrayList<WebsocketDirection>();
    public AnalyzerWebsocketMessageModel(WebsocketTrafficRecord websocketTrafficRecord, String associatedHttpMessageModelId ) throws AnalyzerException {
        id = UUID.randomUUID().toString();
        this.associatedHttpMessageModelId = associatedHttpMessageModelId;
        analyzeMessage(websocketTrafficRecord);
        extractParameters();
        observedDirection.add(websocketTrafficRecord.getFrame().getDirection());
        try {
            tokenizedMessageHash = HashUtils.md5sum(String.format("%s%s", observedDirection.toString(),tokenizedMessage).getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new AnalyzerException("Failed to generate message hash");
        }
    }

    // A unique UUID for the message
    public String getId() {
        return id;
    }

    // The associated HTTP upgrade message model id
    public String getAssociatedHttpMessageModelId() {
        return associatedHttpMessageModelId;
    }

    // An md5sum hash of the tokenized message
    public String getTokenizedMessageHash() {
        return tokenizedMessageHash;
    }

    // Merges another AnalyzerWebsocketMessageModel into this one
    public void merge( AnalyzerWebsocketMessageModel analyzerWebsocketMessageModel ) {
        // Merge the parameters from analyzerWebsocketMessageModel into this one
        for ( String parameter : getParameters().keySet() ) {
            if ( parameters.get(parameter) == null ) {
                parameters.put(parameter,analyzerWebsocketMessageModel.getParameters().get(parameter));
            }
            else {
                parameters.get(parameter).merge(analyzerWebsocketMessageModel.getParameters().get(parameter));
            }
        }
    }

    // Extracts parameters into a AnalyzerParameterModel using target locator
    private void extractParameters() {
        if ( targets != null ) {
            for ( TestTarget target : targets ) {
                String value = samplePayloadText.substring(target.getStartPos(),target.getEndPos());
                AnalyzerParameterModel parameterModel = new AnalyzerParameterModel(target.getTargetName(),value);
                AnalyzerParameterModel existingParameterModel = parameters.get(target.getTargetName());
                if ( existingParameterModel == null ) {
                    parameters.put(target.getTargetName(),existingParameterModel);
                }
                else {
                    parameters.get(target.getTargetName()).merge(parameterModel);
                }

            }
        }
    }

    // Generates a name for a parameter name
    private String getTokenName( String parameterName ) {
        return String.format("@__%s__@", parameterName);
    }

    // Generates a string representing the message
    private void analyzeMessage(WebsocketTrafficRecord websocketTrafficRecord) throws AnalyzerException {
        if ( websocketTrafficRecord.getFrame().getPayloadUnmasked() != null ) {
            targets = targetLocator.getAllTargets(new String(websocketTrafficRecord.getFrame().getPayloadUnmasked()));
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
                samplePayloadText = new String(websocketTrafficRecord.getFrame().getPayloadUnmasked());
            }
            else {
                throw new AnalyzerException("No targets");
            }

        }
        else {
            throw new AnalyzerException("No payload to analyze");
        }
    }

    public HashMap<String, AnalyzerParameterModel> getParameters() {
        return parameters;
    }

    public ArrayList<WebsocketDirection> getObservedDirection() {
        return observedDirection;
    }

    public String getTokenizedMessage() {
        return tokenizedMessage;
    }
}
