package it.sisal.kairos.blrest.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "grpc-destinations")
public class GrpcDestinationConfig {

    private GrpcDestination monitor;
    private GrpcDestination senderRest;
    private GrpcDestination senderVault;

}
