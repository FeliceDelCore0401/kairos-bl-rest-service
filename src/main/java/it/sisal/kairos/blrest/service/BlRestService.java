package it.sisal.kairos.blrest.service;

import it.sisal.kairos.blrest.model.GenericMsg;

public interface BlRestService {
    void sendGenericMessageRequest(int type, GenericMsg details);
}
