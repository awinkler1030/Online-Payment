package com.techelevator.tenmo.dao;


import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Component
public class JdbcTransferDAO implements TransferDAO {

    private JdbcTemplate jdbcTemplate;

    public JdbcTransferDAO(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    @Override
    public Transfer sendTransfer(Transfer transfer) {

        String sql = "INSERT INTO transfers (transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount) VALUES (DEFAULT, ?, ?, (SELECT account_id from accounts WHERE user_id = ?), (SELECT account_id from accounts WHERE user_id = ?), ?) RETURNING transfer_id;\n";

        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, 2, 2, transfer.getAccountFromUserId(), transfer.getAccountToUserId(), transfer.getAmount());

        int newTransferId = 0;
        if (result.next()) {
            newTransferId = result.getInt("transfer_id");
        }
        transfer.setTransferId(newTransferId);

        withdrawDepositAmount(transfer);

        return transfer;
    }

    @Override
    public Transfer requestTransfer(Transfer transfer) {

        String sql = "INSERT INTO transfers (transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount) VALUES (DEFAULT, ?, ?, (SELECT account_id from accounts WHERE user_id = ?), (SELECT account_id from accounts WHERE user_id = ?), ?) RETURNING transfer_id;\n";

        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, 1, 1, transfer.getAccountFromUserId(), transfer.getAccountToUserId(), transfer.getAmount());

        int newTransferId = 0;
        if (result.next()) {
            newTransferId = result.getInt("transfer_id");
        }
        transfer.setTransferId(newTransferId);

        return transfer;

    }

    @Override
    public List<Transfer> getAllTransfersForUser(long userId) {

        List<Transfer> transferList = new ArrayList<>();

        String sql = "SELECT transfer_id, t.transfer_type_id, transfer_type_desc, t.transfer_status_id, transfer_status_desc, account_from, u1.username as user_from, u1.user_id as user_from_id, account_to, u2.username as user_to, u2.user_id as user_to_id, amount \n" +
                "FROM transfers t\n" +
                "JOIN transfer_types tt ON t.transfer_type_id = tt.transfer_type_id\n" +
                "JOIN transfer_statuses ts ON t.transfer_status_id = ts.transfer_status_id\n" +
                "JOIN accounts a1 ON t.account_from = a1.account_id\n" +
                "JOIN accounts a2 ON t.account_to = a2.account_id\n" +
                "JOIN users u1 ON a1.user_id = u1.user_id\n" +
                "JOIN users u2 ON a2.user_id = u2.user_id\n" +
                "WHERE u1.user_id = ? OR u2.user_id = ?";

        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId, userId);

        while (results.next()) {

            Transfer transfer = mapRowToTransfer(results);
            transferList.add(transfer);

        }

        return transferList;
    }

    @Override
    public List<Transfer> getPendingTransfers(long userId) {
        List<Transfer> pendingTransferList = new ArrayList<>();

        String sql = "SELECT transfer_id, t.transfer_type_id, transfer_type_desc, t.transfer_status_id, transfer_status_desc, account_from, u1.username as user_from, u1.user_id as user_from_id, account_to, u2.username as user_to, u2.user_id as user_to_id, amount \n" +
                "FROM transfers t\n" +
                "JOIN transfer_types tt ON t.transfer_type_id = tt.transfer_type_id\n" +
                "JOIN transfer_statuses ts ON t.transfer_status_id = ts.transfer_status_id\n" +
                "JOIN accounts a1 ON t.account_from = a1.account_id\n" +
                "JOIN accounts a2 ON t.account_to = a2.account_id\n" +
                "JOIN users u1 ON a1.user_id = u1.user_id\n" +
                "JOIN users u2 ON a2.user_id = u2.user_id\n" +
                "WHERE (u1.user_id = ? OR u2.user_id = ?) AND t.transfer_status_id = 1";

        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId, userId);

        while (results.next()) {

            Transfer transfer = mapRowToTransfer(results);
            pendingTransferList.add(transfer);

        }
        return pendingTransferList;
    }

    @Override
    public void approveTransfer(Transfer transfer) {
        String sql = "UPDATE transfers SET transfer_status_id = 2 WHERE transfer_id = ?";
        jdbcTemplate.update(sql, transfer.getTransferId());
        withdrawDepositAmount(transfer);
    }

    @Override
    public void rejectTransfer(Transfer transfer) {
        String sql = "UPDATE transfers SET transfer_status_id = 3 WHERE transfer_id = ?";
        jdbcTemplate.update(sql, transfer.getTransferId());
    }

//    @Override
//    public void updateTransferStatus(Transfer transfer, Integer approveRejectChoice) {
//        int newTransferStatusId = 0;
//        if (approveRejectChoice == 1) {
//            newTransferStatusId = 2;
//            withdrawDepositAmount(transfer);
//        } else if (approveRejectChoice == 2) {
//            newTransferStatusId = 3;
//        }
//        String sql = "UPDATE transfers SET transfer_status_id = ? WHERE transfer_id = ?";
//        jdbcTemplate.update(sql, newTransferStatusId, transfer.getTransferId());
//    }

    private void withdrawDepositAmount(Transfer transfer) {
        String sqlTo = "UPDATE accounts SET balance = balance + ? WHERE account_id = (SELECT account_id from accounts WHERE user_id = ?)";
        jdbcTemplate.update(sqlTo, transfer.getAmount(), transfer.getAccountToUserId());

        String sqlFrom = "UPDATE accounts SET balance = balance - ? WHERE account_id = (SELECT account_id from accounts WHERE user_id = ?)";
        jdbcTemplate.update(sqlFrom, transfer.getAmount(), transfer.getAccountFromUserId());
    }


    @Override
    public Transfer getTransferDetails(int transferId) {

        Transfer transfer = null;
        String sql = "SELECT transfer_id, t.transfer_type_id, transfer_type_desc, t.transfer_status_id, transfer_status_desc, account_from, u1.username as user_from, u1.user_id as user_from_id, account_to, u2.username as user_to, u2.user_id as user_to_id, amount \n" +
                "FROM transfers t\n" +
                "JOIN transfer_types tt ON t.transfer_type_id = tt.transfer_type_id\n" +
                "JOIN transfer_statuses ts ON t.transfer_status_id = ts.transfer_status_id\n" +
                "JOIN accounts a1 ON t.account_from = a1.account_id\n" +
                "JOIN accounts a2 ON t.account_to = a2.account_id\n" +
                "JOIN users u1 ON a1.user_id = u1.user_id\n" +
                "JOIN users u2 ON a2.user_id = u2.user_id\n" +
                "WHERE transfer_id = ?";

        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transferId);

        if (results.next()) {
            transfer = mapRowToTransfer(results);

        }
        return transfer;
    }

    public Transfer mapRowToTransfer(SqlRowSet results) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(results.getInt("transfer_id"));
        transfer.setTransferTypeId(results.getInt("transfer_type_id"));
        transfer.setTransferType(results.getString("transfer_type_desc"));
        transfer.setTransferStatusId(results.getInt("transfer_status_id"));
        transfer.setTransferStatus(results.getString("transfer_status_desc"));
        transfer.setAccountFromId(results.getInt("account_from"));
        transfer.setAccountFromUsername(results.getString("user_from"));
        transfer.setAccountFromUserId(results.getLong("user_from_id"));
        transfer.setAccountToId(results.getInt("account_to"));
        transfer.setAccountToUsername(results.getString("user_to"));
        transfer.setAccountToUserId(results.getLong("user_to_id"));
        transfer.setAmount(results.getBigDecimal("amount"));

        return transfer;
    }
}
