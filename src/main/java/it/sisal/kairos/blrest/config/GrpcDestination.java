package it.sisal.kairos.blrest.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GrpcDestination {

    private boolean enabled;
    private String address;

}
