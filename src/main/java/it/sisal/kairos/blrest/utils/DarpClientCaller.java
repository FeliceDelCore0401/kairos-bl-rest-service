package it.sisal.kairos.blrest.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dapr.client.DaprClient;
import it.sisal.kairos.common.util.StructUtils;
import it.sisal.kairos.grpc.MessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class DarpClientCaller {

    private final DaprClient daprClient;
    private final String springConfigName;

    public DarpClientCaller(DaprClient daprClient, @Value("${SPRING_CONFIG_NAME:#{null}}") String springConfigName) {
        this.daprClient = daprClient;
        this.springConfigName = springConfigName;
    }

    public boolean customSaveState(String storeName, String podContainerId, String appName,
                                   MessageRequest messageRequest) {
        boolean result = false;
        if (isRunningOnK8s()) {
            log.info("Saving msg: {}", messageRequest);
            String dbKey = appName + ".id[" + messageRequest.getId() + "]";
            Map<String, Object> dbMap = new HashMap<>();
            dbMap.put("caller", podContainerId);
            dbMap.put("data", StructUtils.convertStructToObject(messageRequest.getData(), Map.class));
            String dbValue = "";
            try {
                dbValue = new ObjectMapper().writeValueAsString(dbMap);
            } catch (JsonProcessingException e) {
                log.error("Error in converting message details to string: {}", e.getMessage());
            }

            daprClient.saveState(storeName, dbKey, dbValue).block();
            log.info("Request msg saved with key [{}]: {}", dbKey, dbMap);
            result = true;
        }
        return result;
    }

    public boolean customDeleteState(String storeName, String key) {
        boolean result = false;
        if (isRunningOnK8s()) {
            daprClient.deleteState(storeName, key).block();
            result = true;
        }
        return result;
    }

    public boolean isRunningOnK8s() {
        return springConfigName != null;
    }
}