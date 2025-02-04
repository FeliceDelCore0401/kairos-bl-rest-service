package it.sisal.kairos.blrest.service.stream;

import io.dapr.client.DaprClient;
import io.grpc.ManagedChannel;
import it.sisal.kairos.blrest.utils.DarpClientCaller;
import it.sisal.kairos.common.util.MsgConstants;
import it.sisal.kairos.grpc.SenderRestServiceGrpc;
import lombok.extern.slf4j.Slf4j;

/**
 * StreamObserver for the sender-rest service: it will handle the responses from the sender-rest service, the communication errors and the channel shutdown.
 */
@Slf4j
public class SenderRestInfoStreamObserver extends GenericResponseStreamObserver {

    private final SenderRestServiceGrpc.SenderRestServiceStub senderRestStub;

    public SenderRestInfoStreamObserver(ManagedChannel channel,
                                            SenderRestServiceGrpc.SenderRestServiceStub senderRestStub,
                                            String podContainerId, DarpClientCaller darpClientCaller) {
        super(channel, log, podContainerId, darpClientCaller);
        this.senderRestStub = senderRestStub;
    }

    @Override
    protected void sendPong() {
        this.senderRestStub.sendInfoMessage(this).onNext(createPongMessageRequest());
    }

    @Override
    protected String getInvolvedComponent() {
        return MsgConstants.COMPONENT_SENDER_REST;
    }

}
