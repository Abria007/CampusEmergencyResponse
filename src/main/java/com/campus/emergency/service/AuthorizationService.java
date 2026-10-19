
package com.campus.emergency.service;

import com.campus.emergency.model.User;

import java.util.Arrays;

public class AuthorizationService {

    public void requireActiveUser(User user) {
        if (user == null) {
            throw new SecurityException(
                    "Authentication is required."
            );
        }

        if (!user.isActive()) {
            throw new SecurityException(
                    "This account is inactive."
            );
        }
    }

    public void requireRole(User user, String... allowedRoles) {
        requireActiveUser(user);

        String userRole = user.getRole();

        if (userRole == null ||
                Arrays.stream(allowedRoles)
                        .noneMatch(role ->
                                role.equalsIgnoreCase(userRole))) {
            throw new SecurityException(
                    "You do not have permission to perform this operation."
            );
        }
    }
}
