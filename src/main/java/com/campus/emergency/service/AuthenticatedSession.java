
package com.campus.emergency.service;

import com.campus.emergency.model.User;

public final class AuthenticatedSession {

    private final User user;

    // Only classes in this package can create sessions directly.
    AuthenticatedSession(User user) {
        if (user == null || !user.isActive()) {
            throw new SecurityException(
                    "An active authenticated user is required."
            );
        }

        this.user = user;
    }

    public int getUserId() {
        return user.getId();
    }

    public String getUsername() {
        return user.getUsername();
    }

    public String getRole() {
        return user.getRole();
    }

    public boolean isActive() {
        return user.isActive();
    }

    User getUser() {
        return user;
    }
}
