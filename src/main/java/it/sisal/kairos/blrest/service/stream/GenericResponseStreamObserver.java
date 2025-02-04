package it.sisal.kairos.blrest.service.stream;

import org.slf4j.Logger;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import it.sisal.kairos.blrest.utils.DarpClientCaller;
import it.sisal.kairos.common.util.LogConstants;
import it.sisal.kairos.common.util.MsgConstants;
import it.sisal.kairos.grpc.AckResponse;
import it.sisal.kairos.grpc.MessageRequest;

public abstract class GenericResponseStreamObserver implements StreamObserver<AckResponse> {

    protected final ManagedChannel channel;
    protected final String podContainerId;
    protected final DarpClientCaller darpClientCaller;
    private final Logger logger;

    protected GenericResponseStreamObserver(ManagedChannel channel, Logger logger, String podContainerId,
    		DarpClientCaller darpClientCaller) {
        this.channel = channel;
        this.logger = logger;
        this.podContainerId = podContainerId;
        this.darpClientCaller = darpClientCaller;
    }

    protected abstract String getInvolvedComponent();

    protected abstract void sendPong();

    protected MessageRequest createPongMessageRequest() {
        return MessageRequest.newBuilder().setId(MsgConstants.MSG_ID_PONG).setCaller(podContainerId).build();
    }

    @Override
    public void onNext(AckResponse ackResponse) {
        // Handle the server's response
        LogConstants.logMessageReceiving(this.logger, ackResponse.getRecordId(), ackResponse.getSuccess(), null,
                getInvolvedComponent()); //TODO: replace null with the message
        // TODO: remove the pre-saved envelope (if any)

        // delete in-memory
        darpClientCaller.customDeleteState("redis-test", ackResponse.getRecordId());

        if (ackResponse.getRecordId().equals(MsgConstants.MSG_ID_PING)) {
            sendPong();
        }
    }

    @Override
    public void onError(Throwable throwable) {
        // Handle any errors
        LogConstants.logCommunicationError(this.logger, throwable, getInvolvedComponent());
    }

    @Override
    public void onCompleted() {
        // Complete the communication
        channel.shutdown();
    }

}
