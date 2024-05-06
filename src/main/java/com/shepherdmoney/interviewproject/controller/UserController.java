package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;



@RestController
public class UserController {

    // TODO: wire in the user repository (~ 1 line)
    private UserRepository userRepository;
    
    @PutMapping("/user")
    public ResponseEntity<Integer> createUser(@RequestBody CreateUserPayload payload) {
        // TODO: Create an user entity with information given in the payload, store it in the database
        //       and return the id of the user in 200 OK response
        
        // Create a new user entity
        User newUser = new User();
        newUser.setName(payload.getName());
        newUser.setEmail(payload.getEmail());

        // Save the user entity to the database
        User savedUser = userRepository.save(newUser);

        // Return the ID of the newly created user in a 200 OK response
        return ResponseEntity.ok(savedUser.getId());
    }

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestParam int userId) {
        // TODO: Return 200 OK if a user with the given ID exists, and the deletion is successful
        //       Return 400 Bad Request if a user with the ID does not exist
        //       The response body could be anything you consider appropriate

        // Check if the user exists in the database
        if (userRepository.existsById(userId)) {
            // Delete the user if it exists
            userRepository.deleteById(userId);
            // Return 200 OK if deletion is successful
            return ResponseEntity.ok("User with ID " + userId + " deleted successfully.");
        } else {
            // Return 400 Bad Request if user does not exist
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with ID " + userId + " does not exist.");
        }
    }
}
