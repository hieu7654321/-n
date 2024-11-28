package dev.hieu.mcenterproject.service;

import dev.hieu.mcenterproject.Eum.UsersRole;
import dev.hieu.mcenterproject.model.User;
import dev.hieu.mcenterproject.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }

//    public boolean registerUser(User user) {
//        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
//            return false;  // Username already exists
//        }
//
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        user.setRole(UsersRole.valueOf("STUDENT"));  // Default role is STUDENT
//        user.setActive(true);  // Set user as active
//        userRepository.save(user);
//        return true;
//    }

//    public void updateUserProfile(String username, String lastname, String surname, String dob, String gender, String address, String phone, String email) {
//        User user = findByUsername(username);
//        user.setSurname(surname);
//        user.setLastname(lastname);
//        user.setDob(dob);
//        user.setGender(gender);
//        user.setAddress(address);
//        user.setPhone(phone);
//        user.setEmail(email);
//        userRepository.save(user);  // Update user in the database
//    }

//    public List<User> findAllByRole(UsersRole role) {
//        return userRepository.findAllByRole(role);
//    }

    // Phương thức xác thực người dùng
    public Authentication authenticate(String username, String password) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        return authenticationManager.authenticate(token);
    }

    // Phương thức để lấy thông tin người dùng từ Authentication
//    public UserDetails loadUserByUsername(String username) {
//        return userDetailsService.loadUserByUsername(username);
//    }
}
