package com.Emanon.service;

import com.Emanon.entity.User;
import com.Emanon.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User createUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 👉 這裡正式環境建議加密！
        return userRepository.save(user);
    }


}
