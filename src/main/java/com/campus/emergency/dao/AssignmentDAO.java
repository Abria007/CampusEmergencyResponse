
package com.campus.emergency.dao;

import com.campus.emergency.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AssignmentDAO {

    public boolean assignResponderAtomically(
            int incidentId, int responderId) throws SQLException {

        String responderSql =
                "UPDATE responders " +
                        "SET is_available = 0 " +
                        "WHERE id = ? AND is_available = 1";

        String incidentSql =
                "UPDATE incidents " +
                        "SET assigned_responder_id = ?, status = 'ASSIGNED' " +
                        "WHERE id = ? AND status = 'REPORTED' " +
                        "AND assigned_responder_id IS NULL";

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                // Step 1: Reserve the responder only if still available.
                try (PreparedStatement responderStatement =
                             connection.prepareStatement(responderSql)) {

                    responderStatement.setInt(1, responderId);

                    if (responderStatement.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
                    }
                }

                // Step 2: Assign the responder only if the incident
                // is still reported and unassigned.
                try (PreparedStatement incidentStatement =
                             connection.prepareStatement(incidentSql)) {

                    incidentStatement.setInt(1, responderId);
                    incidentStatement.setInt(2, incidentId);

                    if (incidentStatement.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
                    }
                }

                // Both updates succeeded.
                connection.commit();
                return true;

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }
}
