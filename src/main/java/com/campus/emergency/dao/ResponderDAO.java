
package com.campus.emergency.dao;

import com.campus.emergency.database.DatabaseConnection;
import com.campus.emergency.model.Responder;
import com.campus.emergency.model.ResponderFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResponderDAO {

    public Responder findById(int id) throws SQLException {
        String sql = "SELECT * FROM responders WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResponder(resultSet);
                }
            }
        }

        return null;
    }

    public List<Responder> findAllAvailable() throws SQLException {
        List<Responder> responders = new ArrayList<>();

        String sql = "SELECT * FROM responders WHERE is_available = 1";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                responders.add(mapResponder(resultSet));
            }
        }

        return responders;
    }

    public boolean updateAvailability(int responderId, boolean available)
            throws SQLException {

        String sql = "UPDATE responders SET is_available = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, available ? 1 : 0);
            statement.setInt(2, responderId);

            return statement.executeUpdate() == 1;
        }
    }


    private Responder mapResponder(ResultSet resultSet)
            throws SQLException {
        return ResponderFactory.createResponder(
                resultSet.getInt("id"),
                resultSet.getInt("user_id"),
                resultSet.getString("name"),
                resultSet.getString("responder_type"),
                resultSet.getInt("location_id"),
                resultSet.getInt("is_available") == 1
        );
    }

}
