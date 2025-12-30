package com.example.identitymanager.repository;

import com.example.identitymanager.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcUserDao implements UserDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ==================== SELECT OPERATIONS ====================

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

    // ==================== INSERT OPERATION ====================

    @Override
    public int insertUser(User user) {
        String sql = "INSERT INTO users (email, password, first_name, last_name, phone, is_privacy_enabled, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowsAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"}); // FIXED: specify column name
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFirstName());
            ps.setString(4, user.getLastName());
            ps.setString(5, user.getPhone());
            ps.setBoolean(6, user.getIsPrivacyEnabled() != null ? user.getIsPrivacyEnabled() : false);
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, keyHolder);

        // FIXED: Get ID from keyHolder properly
        if (keyHolder.getKeys() != null && keyHolder.getKeys().containsKey("id")) {
            user.setId(((Number) keyHolder.getKeys().get("id")).longValue());
        }

        return rowsAffected;
    }

    // ==================== UPDATE OPERATION ====================

    @Override
    public int updateUser(User user) {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, phone = ?, is_privacy_enabled = ?, updated_at = ? " +
                "WHERE id = ?";

        return jdbcTemplate.update(sql,
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getIsPrivacyEnabled(),
                Timestamp.valueOf(LocalDateTime.now()),
                user.getId()
        );
    }

    // ==================== DELETE OPERATION ====================

    @Override
    public int deleteUserById(Long id) {
        // First delete from user_roles junction table (foreign key constraint)
        String deleteMappingSql = "DELETE FROM user_roles WHERE user_id = ?";
        jdbcTemplate.update(deleteMappingSql, id);

        // Then delete the user
        String deleteUserSql = "DELETE FROM users WHERE id = ?";
        return jdbcTemplate.update(deleteUserSql, id);
    }

    @Override
    public void executeUpdate(String sql, Object... params) {
        jdbcTemplate.update(sql, params);
    }

    // ==================== ROW MAPPER ====================

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