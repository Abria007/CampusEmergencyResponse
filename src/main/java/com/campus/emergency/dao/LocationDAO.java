
package com.campus.emergency.dao;

import com.campus.emergency.database.DatabaseConnection;
import com.campus.emergency.model.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LocationDAO {

    public Location findById(int id) throws SQLException {
        String sql = "SELECT id, building, floor, room, latitude, longitude " +
                "FROM locations WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Location(
                            resultSet.getInt("id"),
                            resultSet.getString("building"),
                            resultSet.getString("floor"),
                            resultSet.getString("room"),
                            resultSet.getDouble("latitude"),
                            resultSet.getDouble("longitude")
                    );
                }
            }
        }

        return null;
    }
}
