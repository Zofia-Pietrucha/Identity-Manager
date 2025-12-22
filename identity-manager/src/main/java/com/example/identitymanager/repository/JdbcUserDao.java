package com.example.identitymanager.repository;

import com.example.identitymanager.model.Role;
import com.example.identitymanager.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class JdbcUserDao implements UserDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> findAllUsers() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    @Override
    public Optional<User> findUserById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), id);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), email);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    @Override
    public int countUsers() {
        String sql = "SELECT COUNT(*) FROM users";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public List<User> findUsersByPrivacyEnabled(boolean privacyEnabled) {
        String sql = "SELECT * FROM users WHERE is_privacy_enabled = ?";
        return jdbcTemplate.query(sql, new UserRowMapper(), privacyEnabled);
    }

    // RowMapper - converts database row to User object
    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setFirstName(rs.getString("first_name"));
            user.setLastName(rs.getString("last_name"));
            user.setPhone(rs.getString("phone"));
            user.setIsPrivacyEnabled(rs.getBoolean("is_privacy_enabled"));
            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

            // Initialize empty roles set (we don't load roles in JDBC for simplicity)
            user.setRoles(new HashSet<>());

            return user;
        }
    }
}