package com.techelevator.tenmo.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;

@Component
public class JdbcAccountDAO implements AccountDAO {

    private JdbcTemplate jdbcTemplate;


    public JdbcAccountDAO(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    @Override
    public BigDecimal retrieveBalance(long userId) {

    BigDecimal accountBalance = null;

    String sql = "SELECT balance FROM accounts WHERE user_id = ?";

    SqlRowSet result = jdbcTemplate.queryForRowSet(sql, userId);

    if(result.next()) {

        accountBalance = result.getBigDecimal("balance");
    }
        return accountBalance;
    }
}
