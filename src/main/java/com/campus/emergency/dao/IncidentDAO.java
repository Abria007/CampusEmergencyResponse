
package com.campus.emergency.dao;

import com.campus.emergency.database.DatabaseConnection;
import com.campus.emergency.model.Incident;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class IncidentDAO {

    public boolean createIncident(Incident incident) throws SQLException {
        String sql = "INSERT INTO incidents "
                + "(reported_by, category, description, location_id, "
                + "priority, status, assigned_responder_id, resolution_notes) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, incident.getReportedBy());
            statement.setString(2, incident.getCategory());
            statement.setString(3, incident.getDescription());
            statement.setInt(4, incident.getLocationId());
            statement.setString(5, incident.getPriority());
            statement.setString(6, incident.getStatus());

            if (incident.getAssignedResponderId() == null) {
                statement.setNull(7, Types.INTEGER);
            } else {
                statement.setInt(7, incident.getAssignedResponderId());
            }

            statement.setString(8, incident.getResolutionNotes());

            return statement.executeUpdate() == 1;
        }
    }

    public Incident findById(int id) throws SQLException {
        String sql = "SELECT * FROM incidents WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapIncident(resultSet);
                }
            }
        }

        return null;
    }

    public List<Incident> findAll() throws SQLException {
        List<Incident> incidents = new ArrayList<>();

        String sql = "SELECT * FROM incidents ORDER BY id DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                incidents.add(mapIncident(resultSet));
            }
        }

        return incidents;
    }

    private Incident mapIncident(ResultSet resultSet) throws SQLException {
        int responderId = resultSet.getInt("assigned_responder_id");

        Integer assignedResponderId = resultSet.wasNull()
                ? null : responderId;

        return new Incident(
                resultSet.getInt("id"),
                resultSet.getInt("reported_by"),
                resultSet.getString("category"),
                resultSet.getString("description"),
                resultSet.getInt("location_id"),
                resultSet.getString("priority"),
                resultSet.getString("status"),
                assignedResponderId,
                resultSet.getString("resolution_notes")
        );
    }

    public boolean assignResponder(int incidentId, int responderId)
            throws SQLException {

        String sql = "UPDATE incidents " +
                "SET assigned_responder_id = ?, status = 'ASSIGNED' " +
                "WHERE id = ? AND status = 'REPORTED' " +
                "AND assigned_responder_id IS NULL";

        try (Connection connection =
                     com.campus.emergency.database.DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, responderId);
            statement.setInt(2, incidentId);

            return statement.executeUpdate() == 1;
        }
    }


    public List<Incident> findUnresolvedByPriority()
            throws SQLException {

        String sql = "SELECT * FROM incidents " +
                "WHERE status != 'RESOLVED' " +
                "ORDER BY CASE priority " +
                "WHEN 'CRITICAL' THEN 1 " +
                "WHEN 'HIGH' THEN 2 " +
                "WHEN 'MEDIUM' THEN 3 " +
                "WHEN 'LOW' THEN 4 " +
                "ELSE 5 END, reported_at ASC";

        List<Incident> incidents = new ArrayList<>();

        try (Connection connection =
                     com.campus.emergency.database.DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                incidents.add(mapIncident(resultSet));
            }
        }

        return incidents;
    }

}
