// HU02 - Realizar Logon | HU03 - Realizar Cadastro (busca de usuário por e-mail)
package dev.guilhermeluan.ongoing.user;

import dev.guilhermeluan.ongoing.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Test
    void findByEmail_ShouldReturnUser_WhenEmailExists() {
        String email = "john@example.com";
        User user = User.builder().id(1L).email(email).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User result = userService.findByEmail(email);

        assertEquals(user, result);
    }

    @Test
    void findByEmail_ShouldThrowNotFoundException_WhenEmailNotFound() {
        String email = "notfound@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.findByEmail(email));
    }
}
