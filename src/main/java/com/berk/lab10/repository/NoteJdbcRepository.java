package com.berk.lab10.repository;

import com.berk.lab10.dto.NoteResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NoteJdbcRepository {

    private final JdbcTemplate jdbc;

    public NoteJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Prepared statement kullanır: ? placeholders
    public List<NoteResponse> findAllByUserId(Integer userId) {
        String sql = "SELECT id, title, content, created_at FROM notes WHERE user_id = ? ORDER BY id DESC";
        return jdbc.query(sql, (rs, rowNum) ->
                new NoteResponse(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("created_at")
                ), userId);
    }
}
