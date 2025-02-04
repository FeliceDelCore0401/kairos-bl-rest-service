package it.sisal.kairos.blrest.service.stream;

import io.dapr.client.DaprClient;
import io.grpc.ManagedChannel;
import it.sisal.kairos.blrest.utils.DarpClientCaller;
import it.sisal.kairos.common.util.MsgConstants;
import it.sisal.kairos.grpc.MonitorServiceGrpc;
import lombok.extern.slf4j.Slf4j;

/**
 * StreamObserver for the monitor service: it will handle the responses from the monitor service, the communication errors and the channel shutdown.
 */
@Slf4j
public class MonitorResponseStreamObserver extends GenericResponseStreamObserver {

    private final MonitorServiceGrpc.MonitorServiceStub monitorStub;

    public MonitorResponseStreamObserver(ManagedChannel channel, MonitorServiceGrpc.MonitorServiceStub monitorStub,
                                         String podContainerId, DarpClientCaller darpClientCaller) {
        super(channel, log, podContainerId, darpClientCaller);
        this.monitorStub = monitorStub;
    }

    @Override
    protected void sendPong() {
        this.monitorStub.sendMonitorMessage(this).onNext(createPongMessageRequest());
    }

    @Override
    protected String getInvolvedComponent() {
        return MsgConstants.COMPONENT_MONITOR;
    }

}
