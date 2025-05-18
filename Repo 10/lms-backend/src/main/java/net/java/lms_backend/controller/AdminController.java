package net.java.lms_backend.controller;

import net.java.lms_backend.entity.User;
import net.java.lms_backend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*") // Allow requests from frontend
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ✅ Get all users in the system
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = adminService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // ✅ Promote a user to admin
    @PostMapping("/promote/{userId}")
    public ResponseEntity<?> promoteToAdmin(@PathVariable Long userId) {
        try {
            User promotedUser = adminService.promoteToAdmin(userId);
            if (promotedUser != null) {
                return ResponseEntity.ok(promotedUser);
            } else {
                return ResponseEntity.badRequest().body("User not found or cannot be promoted.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error promoting user: " + e.getMessage());
        }
    }

    // ✅ Assign a specific role to a user
    @PostMapping("/assign-role/{userId}")
    public ResponseEntity<String> assignRole(@PathVariable Long userId, @RequestParam String role) {
        try {
            if (!adminService.isValidRole(role)) {
                return ResponseEntity.badRequest().body("Invalid role: " + role);
            }
            boolean success = adminService.assignRole(userId, role);
            if (success) {
                return ResponseEntity.ok("Role assigned successfully.");
            }
            return ResponseEntity.badRequest().body("Failed to assign role. User may not exist.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error assigning role: " + e.getMessage());
        }
    }

    // ✅ Revoke a role from a user
    @PostMapping("/revoke-role/{userId}")
    public ResponseEntity<String> revokeRole(@PathVariable Long userId, @RequestParam String role) {
        try {
            if (!adminService.isValidRole(role)) {
                return ResponseEntity.badRequest().body("Invalid role: " + role);
            }
            boolean success = adminService.revokeRole(userId, role);
            if (success) {
                return ResponseEntity.ok("Role revoked successfully.");
            }
            return ResponseEntity.badRequest().body("Failed to revoke role. User may not exist.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error revoking role: " + e.getMessage());
        }
    }

    // ✅ Deactivate a user account
    @PutMapping("/deactivate/{userId}") // Using PUT for update
    public ResponseEntity<String> deactivateUser(@PathVariable Long userId) {
        try {
            boolean success = adminService.deactivateUser(userId);
            if (success) {
                return ResponseEntity.ok("User deactivated successfully.");
            }
            return ResponseEntity.badRequest().body("Failed to deactivate user. User not found.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deactivating user: " + e.getMessage());
        }
    }
}
