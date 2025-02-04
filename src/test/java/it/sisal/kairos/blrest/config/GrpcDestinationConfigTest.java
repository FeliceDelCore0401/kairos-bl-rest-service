package it.sisal.kairos.blrest.config;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class GrpcDestinationConfigTest {

    @Test
    void testDefaultConstructor() {
        GrpcDestinationConfig config = new GrpcDestinationConfig();
        assertThat(config.getMonitor()).isNull();
        assertThat(config.getSenderRest()).isNull();
        assertThat(config.getSenderVault()).isNull();
    }

    @Test
    void testAllArgsConstructor() {
        GrpcDestination monitor = new GrpcDestination(true, "monitorAddress");
        GrpcDestination senderRest = new GrpcDestination(true, "senderRestAddress");
        GrpcDestination senderVault = new GrpcDestination(true, "senderVaultAddress");
        
        GrpcDestinationConfig config = new GrpcDestinationConfig(monitor, senderRest, senderVault);
        
        assertThat(config.getMonitor()).isEqualTo(monitor);
        assertThat(config.getSenderRest()).isEqualTo(senderRest);
        assertThat(config.getSenderVault()).isEqualTo(senderVault);
    }

    @Test
    void testSetterAndGetter() {
        GrpcDestination monitor = new GrpcDestination();
        GrpcDestination senderRest = new GrpcDestination();
        GrpcDestination senderVault = new GrpcDestination();
        
        GrpcDestinationConfig config = new GrpcDestinationConfig();
        config.setMonitor(monitor);
        config.setSenderRest(senderRest);
        config.setSenderVault(senderVault);
        
        assertThat(config.getMonitor()).isEqualTo(monitor);
        assertThat(config.getSenderRest()).isEqualTo(senderRest);
        assertThat(config.getSenderVault()).isEqualTo(senderVault);
    }

    @Test
    void testToString() {
        GrpcDestination monitor = new GrpcDestination(true, "monitorAddress");
        GrpcDestination senderRest = new GrpcDestination(true, "senderRestAddress");
        GrpcDestination senderVault = new GrpcDestination(true, "senderVaultAddress");
        
        GrpcDestinationConfig config = new GrpcDestinationConfig(monitor, senderRest, senderVault);
        
        assertThat(config).hasToString("GrpcDestinationConfig(monitor=GrpcDestination(enabled=true, address=monitorAddress), senderRest=GrpcDestination(enabled=true, address=senderRestAddress), senderVault=GrpcDestination(enabled=true, address=senderVaultAddress))");
    }
}
