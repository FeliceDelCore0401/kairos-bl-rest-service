package it.sisal.kairos.blrest.config;

import it.sisal.kairos.blrest.service.BlRestServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartedEventListener implements ApplicationListener<ApplicationStartedEvent> {

    private final BlRestServiceImpl blRestService;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        blRestService.initChannels();

        log.info("Application started");
    }
}
