package it.sisal.kairos.blrest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import it.sisal.frost.manager.SnowflakeManager;
import it.sisal.kairos.blrest.config.GrpcDestination;
import it.sisal.kairos.blrest.config.GrpcDestinationConfig;
import it.sisal.kairos.blrest.model.GenericMsg;
import it.sisal.kairos.blrest.service.stream.MonitorInfoStreamObserver;
import it.sisal.kairos.blrest.service.stream.MonitorResponseStreamObserver;
import it.sisal.kairos.blrest.service.stream.SenderRestInfoStreamObserver;
import it.sisal.kairos.blrest.service.stream.SenderRestResponseStreamObserver;
import it.sisal.kairos.blrest.service.stream.SenderVaultInfoStreamObserver;
import it.sisal.kairos.blrest.service.stream.SenderVaultResponseStreamObserver;
import it.sisal.kairos.blrest.utils.DarpClientCaller;
import it.sisal.kairos.common.util.LogConstants;
import it.sisal.kairos.common.util.MsgConstants;
import it.sisal.kairos.common.util.StructUtils;
import it.sisal.kairos.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class BlRestServiceImpl implements BlRestService {

    private static final String LOG_PREPARED_CHANNEL = "Prepared communication channel toward {}";
    private static final String LOG_DISABLED_CHANNEL = "Communication channel toward {} not enabled";
    private static final String LOG_MSG_ON_DISABLED_CHANNEL = "Received {} message request but channel not enabled";

    private final String appName;
    private final String podContainerId;
    private final String daprStateStore;
    private final SnowflakeManager snowflakeManager;
    private final DarpClientCaller darpClientCaller;

    private MonitorServiceGrpc.MonitorServiceStub monitorStub;
    private SenderRestServiceGrpc.SenderRestServiceStub senderRestStub;
    private SenderVaultServiceGrpc.SenderVaultServiceStub senderVaultStub;

    private StreamObserver<AckResponse> monitorResponseObserver;
    private StreamObserver<AckResponse> senderRestResponseObserver;
    private StreamObserver<AckResponse> senderVaultResponseObserver;
    
    private StreamObserver<AckResponse> monitorInfoObserver;
    private StreamObserver<AckResponse> senderRestInfoStreamObserver;
    private StreamObserver<AckResponse> senderVaultInfoObserver;

    public BlRestServiceImpl(@Value("${spring.application.name}") String appName, @Value("${POD_ID}") String podId,
                             @Value("${dapr.state-store}") String daprStateStore, GrpcDestinationConfig grpcDestConfig,
                             SnowflakeManager snowflakeManager, DarpClientCaller darpClientCaller) {

        this.appName = appName;
        this.podContainerId = podId + ":" + appName;
        this.daprStateStore = daprStateStore;

        this.snowflakeManager = snowflakeManager;
        this.darpClientCaller = darpClientCaller;

        prepareChannels(grpcDestConfig);
    }

    private void prepareChannels(GrpcDestinationConfig grpcDestConfig) {
        log.info("Preparing channels");

        // monitor channel
        if (grpcDestConfig.getMonitor() != null && grpcDestConfig.getMonitor().isEnabled()) {
            prepareMonitorChannel(grpcDestConfig.getMonitor());
            logChannelPreparation(MsgConstants.COMPONENT_MONITOR);
        } else {
            logChannelDisabled(MsgConstants.COMPONENT_MONITOR);
        }

        // sender-rest channel
        if (grpcDestConfig.getSenderRest() != null && grpcDestConfig.getSenderRest().isEnabled()) {
            prepareSenderRestChannel(grpcDestConfig);
            logChannelPreparation(MsgConstants.COMPONENT_SENDER_REST);
        } else {
            logChannelDisabled(MsgConstants.COMPONENT_SENDER_REST);
        }

        // sender-vault channel
        if (grpcDestConfig.getSenderVault() != null && grpcDestConfig.getSenderVault().isEnabled()) {
            prepareSenderVaultChannel(grpcDestConfig);
            logChannelPreparation(MsgConstants.COMPONENT_SENDER_VAULT);
        } else {
            logChannelDisabled(MsgConstants.COMPONENT_SENDER_VAULT);
        }
    }

    private void prepareMonitorChannel(GrpcDestination monitorConfig) {
        ManagedChannel monitorChannel = createChannel(monitorConfig.getAddress());
        this.monitorStub = MonitorServiceGrpc.newStub(monitorChannel);
        this.monitorResponseObserver =
                new MonitorResponseStreamObserver(monitorChannel, this.monitorStub, this.podContainerId,
                        darpClientCaller);
        this.monitorInfoObserver =
                new MonitorInfoStreamObserver(monitorChannel, this.monitorStub, this.podContainerId,
                        darpClientCaller);
        
    }

    private void prepareSenderRestChannel(GrpcDestinationConfig grpcDestConfig) {
        ManagedChannel senderRestChannel = createChannel(grpcDestConfig.getSenderRest().getAddress());
        this.senderRestStub = SenderRestServiceGrpc.newStub(senderRestChannel);
        this.senderRestResponseObserver =
                new SenderRestResponseStreamObserver(senderRestChannel, this.senderRestStub, this.podContainerId,
                        darpClientCaller);
        this.senderRestInfoStreamObserver =
                new SenderRestInfoStreamObserver(senderRestChannel, this.senderRestStub, this.podContainerId,
                        darpClientCaller); 
        
    }

    private void prepareSenderVaultChannel(GrpcDestinationConfig grpcDestConfig) {
        ManagedChannel senderVaultChannel = createChannel(grpcDestConfig.getSenderVault().getAddress());
        this.senderVaultStub = SenderVaultServiceGrpc.newStub(senderVaultChannel);
        this.senderVaultResponseObserver =
                new SenderVaultResponseStreamObserver(senderVaultChannel, this.senderVaultStub, this.podContainerId,
                        darpClientCaller);
        this.senderVaultInfoObserver =
                new SenderVaultInfoStreamObserver(senderVaultChannel, this.senderVaultStub, this.podContainerId,
                        darpClientCaller);
    }

    private ManagedChannel createChannel(String address) {
        return ManagedChannelBuilder.forTarget(address).usePlaintext().build();
    }

    /**
     * Initialize the communication channels by sending a hello message to each service
     */
    public void initChannels() {
        log.info("Initializing channels");
        // create a MessageRequest for the hello message
        MessageRequest helloMessage =
                MessageRequest.newBuilder().setId(MsgConstants.MSG_ID_HELLO).setCaller(this.podContainerId).build();

        if (this.senderRestStub != null) {
            // send the hello message to the sender rest
        	logInitChannelMsgSending(helloMessage.getId(), MsgConstants.COMPONENT_SENDER_REST);
            this.senderRestStub.sendRegulatoryRestMessage(this.senderRestResponseObserver).onNext(helloMessage);
            this.senderRestStub.sendInfoMessage(this.senderRestInfoStreamObserver).onNext(helloMessage);
            
        }

        if (this.senderVaultStub != null) {
            // send the hello message to the sender vault
        	logInitChannelMsgSending(helloMessage.getId(), MsgConstants.COMPONENT_SENDER_VAULT);
            this.senderVaultStub.sendRegulatoryVaultMessage(this.senderVaultResponseObserver).onNext(helloMessage);
            this.senderVaultStub.sendRegulatoryVaultMessage(this.senderVaultInfoObserver).onNext(helloMessage);
        }

        if (this.monitorStub != null) {
            // send the hello message to the monitor
        	logInitChannelMsgSending(helloMessage.getId(), MsgConstants.COMPONENT_MONITOR);
            this.monitorStub.sendMonitorMessage(this.monitorResponseObserver).onNext(helloMessage);
            this.monitorStub.sendInfoMessage(this.monitorInfoObserver).onNext(helloMessage);
        }
    }

    /**
     * Send a message to the component specified by the type
     * @param type the type of the component to send the message to (0 -> monitor, 1 -> senderRest, 2 -> senderVault)
     * @param genericMsg the generic message
     */
    @Override
    public void sendGenericMessageRequest(int type, GenericMsg genericMsg) {
        // create a new snowflake id
        String snowflakeId = snowflakeManager.now().toString();

        // 0 -> monitor, 1 -> senderRest, 2 -> senderVault
        switch (type) {
            case 0 -> sendMsgRequestToMonitor(snowflakeId);
            case 1 -> sendMsgRequestToSenderRest(snowflakeId, genericMsg);
            case 2 -> sendMsgRequestToSenderVault(snowflakeId, genericMsg);
            default -> log.error("Unknown message type");
        }
    }

    private void sendMsgRequestToMonitor(String snowflakeId) {
        if (this.monitorStub == null) {
            logMsgDisabledChannel(MsgConstants.COMPONENT_MONITOR);
        } else {
            // prepare mock AlertObject. TODO: remove mock
            MessageRequest monitorMessage = MessageRequest.newBuilder()
                    .setData(StructUtils.convertObjectToStruct(Map.of("testAlert", "something strange is happening")))
                    .setId(snowflakeId).build();
            // TODO: save the envelope?
            // send the message
            logMsgSending(monitorMessage.getId(), MsgConstants.COMPONENT_MONITOR);
            this.monitorStub.sendMonitorMessage(this.monitorResponseObserver).onNext(monitorMessage);
        }
    }

    private void sendMsgRequestToSenderRest(String snowflakeId, GenericMsg genericMsg) {
        if (this.senderRestStub == null) {
            logMsgDisabledChannel(MsgConstants.COMPONENT_SENDER_REST);
        } else {
            // prepare mock rest RegulatoryMessage. TODO: remove mock
            MessageRequest regulatoryMessage =
                    MessageRequest.newBuilder().setData(StructUtils.convertObjectToStruct(genericMsg))
                            .setId(snowflakeId).setCaller(podContainerId).build();
       

            // saving in-memory
            this.darpClientCaller.customSaveState(this.daprStateStore, this.appName, snowflakeId, regulatoryMessage);

            // send the message
            logMsgSending(regulatoryMessage.getId(), MsgConstants.COMPONENT_SENDER_REST);
            this.senderRestStub.sendRegulatoryRestMessage(senderRestResponseObserver).onNext(regulatoryMessage);
        }
    }

    private void sendMsgRequestToSenderVault(String snowflakeId, GenericMsg genericMsg) {
        if (this.senderVaultStub == null) {
            logMsgDisabledChannel(MsgConstants.COMPONENT_SENDER_VAULT);
        } else {
            // prepare mock vault RegulatoryMessage. TODO: remove mock
            MessageRequest regulatoryMessage = MessageRequest.newBuilder().setCaller(this.podContainerId)
                    .setData(StructUtils.convertObjectToStruct(genericMsg)).setId(snowflakeId).build();

            // save the message
            this.darpClientCaller.customSaveState(this.daprStateStore, this.podContainerId, this.appName, regulatoryMessage);

            // send the message
            logMsgSending(regulatoryMessage.getId(), MsgConstants.COMPONENT_SENDER_VAULT);
            this.senderVaultStub.sendRegulatoryVaultMessage(senderVaultResponseObserver).onNext(regulatoryMessage);
        }
    }

    private void logChannelPreparation(String component) {
        log.info(LOG_PREPARED_CHANNEL, component);
    }

    private void logChannelDisabled(String component) {
        log.info(LOG_DISABLED_CHANNEL, component);
    }
    
    private void logInitChannelMsgSending(String msgId, String component) {
        log.info(LogConstants.LOG_KAIROS_STARTUP +" "+LogConstants.LOG_MSG_SENDING, msgId, component);
    }
    

    /**
     * Log the sending of a message
     * @param msgId the id of the message
     * @param component the destination component
     */
    private void logMsgSending(String msgId, String component) {
        log.info(LogConstants.LOG_MSG_SENDING, msgId, component);
    }

    private void logMsgDisabledChannel(String component) {
        log.error(LOG_MSG_ON_DISABLED_CHANNEL, component);
    }

}
