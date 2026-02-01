package dev.guilhermeluan.ongoing.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusServiceTest {

    @Mock
    private StatusRepository repository;

    @InjectMocks
    private StatusService service;

    @Test
    void shouldReturnStatusResponseWithDatabaseInfo() {
        when(repository.getMaxConnections()).thenReturn(100);
        when(repository.getOpenedConnections()).thenReturn(5);
        when(repository.getVersion()).thenReturn("16.0");

        StatusResponse response = service.getStatus();

        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        assertThat(response.getDependencies()).isNotNull();
        assertThat(response.getDependencies().getDatabase()).isNotNull();
        assertThat(response.getDependencies().getDatabase().getMaxConnections()).isEqualTo(100);
        assertThat(response.getDependencies().getDatabase().getOpenedConnections()).isEqualTo(5);
        assertThat(response.getDependencies().getDatabase().getVersion()).isEqualTo("16.0");
    }
}
