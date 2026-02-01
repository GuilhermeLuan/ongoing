package dev.guilhermeluan.ongoing.user;

import dev.guilhermeluan.ongoing.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException(HttpStatus.NOT_FOUND, "User not found with email: " + email));
    }
}
