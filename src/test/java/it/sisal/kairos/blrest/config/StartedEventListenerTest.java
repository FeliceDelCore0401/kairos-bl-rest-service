package it.sisal.kairos.blrest.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationStartedEvent;

import it.sisal.kairos.blrest.service.BlRestServiceImpl;

@ExtendWith(MockitoExtension.class)
class StartedEventListenerTest {

    @Mock
    private BlRestServiceImpl blRestService;

    @InjectMocks
    private StartedEventListener startedEventListener;

    @Test
    void testOnApplicationEvent() {
        ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);
        startedEventListener.onApplicationEvent(event);
        verify(blRestService, times(1)).initChannels();
    }
}
