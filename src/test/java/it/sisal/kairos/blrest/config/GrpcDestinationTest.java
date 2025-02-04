package it.sisal.kairos.blrest.config;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class GrpcDestinationTest {

    @Test
    void testDefaultConstructor() {
        GrpcDestination grpcDestination = new GrpcDestination();
        assertThat(grpcDestination.isEnabled()).isFalse();
        assertThat(grpcDestination.getAddress()).isNull();
    }

    @Test
    void testAllArgsConstructor() {
        GrpcDestination grpcDestination = new GrpcDestination(true, "localhost:50051");
        assertThat(grpcDestination.isEnabled()).isTrue();
        assertThat(grpcDestination.getAddress()).isEqualTo("localhost:50051");
    }

    @Test
    void testSetterAndGetter() {
        GrpcDestination grpcDestination = new GrpcDestination();
        grpcDestination.setEnabled(true);
        grpcDestination.setAddress("localhost:50051");
        assertThat(grpcDestination.isEnabled()).isTrue();
        assertThat(grpcDestination.getAddress()).isEqualTo("localhost:50051");
    }

    @Test
    void testToString() {
        GrpcDestination grpcDestination = new GrpcDestination(true, "localhost:50051");
        assertThat(grpcDestination).hasToString("GrpcDestination(enabled=true, address=localhost:50051)");
    }

    @Test
    void testEquality() {
        GrpcDestination grpcDestination1 = new GrpcDestination(true, "localhost:50051");
        GrpcDestination grpcDestination2 = new GrpcDestination(true, "localhost:50051");
        assertThat(grpcDestination1).isEqualTo(grpcDestination2);
    }
}
