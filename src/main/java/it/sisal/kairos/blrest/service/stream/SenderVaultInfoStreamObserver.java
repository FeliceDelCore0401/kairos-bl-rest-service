package it.sisal.kairos.blrest.service.stream;

import io.grpc.ManagedChannel;
import it.sisal.kairos.blrest.utils.DarpClientCaller;
import it.sisal.kairos.common.util.MsgConstants;
import it.sisal.kairos.grpc.SenderVaultServiceGrpc;
import lombok.extern.slf4j.Slf4j;

/**
 * StreamObserver for the sender-vault service: it will handle the responses from the sender-vault service, the communication errors and the channel shutdown.
 */
@Slf4j
public class SenderVaultInfoStreamObserver extends GenericResponseStreamObserver {

    private final SenderVaultServiceGrpc.SenderVaultServiceStub senderVaultStub;

    public SenderVaultInfoStreamObserver(ManagedChannel channel,
                                             SenderVaultServiceGrpc.SenderVaultServiceStub senderVaultStub,
                                             String podContainerId, DarpClientCaller darpClientCaller) {
        super(channel, log, podContainerId, darpClientCaller);
        this.senderVaultStub = senderVaultStub;
    }

    @Override
    protected void sendPong() {
        this.senderVaultStub.sendInfoMessage(this).onNext(createPongMessageRequest());
    }

    @Override
    protected String getInvolvedComponent() {
        return MsgConstants.COMPONENT_SENDER_VAULT;
    }

}