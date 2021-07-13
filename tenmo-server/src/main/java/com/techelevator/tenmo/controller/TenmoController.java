package com.techelevator.tenmo.controller;


import com.techelevator.tenmo.dao.AccountDAO;
import com.techelevator.tenmo.dao.TransferDAO;
import com.techelevator.tenmo.dao.UserDAO;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.math.BigDecimal;

@RestController
@PreAuthorize("isAuthenticated()")

public class TenmoController {

    @Autowired
    private TransferDAO transferDAO;

    @Autowired
    private AccountDAO accountDAO;

    @Autowired
    private UserDAO userDAO;


//    @RequestMapping(path = "/accounts/{userId}", method = RequestMethod.GET)
//    public BigDecimal retrieveBalance(@PathVariable long userId) {
//        return accountDAO.retrieveBalance(userId);
//    }

    @RequestMapping(path = "/accounts", method = RequestMethod.GET)
    public BigDecimal retrieveBalance(Principal principal) {
        return accountDAO.retrieveBalance(userDAO.findIdByUsername(principal.getName()));
    }

    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public List<User> getUserList() {
        return userDAO.findAll();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/transfers", method = RequestMethod.POST)
    public Transfer sendTransfer(@RequestBody Transfer transfer) {
        return transferDAO.sendTransfer(transfer);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/transfers/pending", method = RequestMethod.POST)
    public Transfer requestTransfer(@RequestBody Transfer transfer) {
        return transferDAO.requestTransfer(transfer);
    }

    @RequestMapping(path = "/transfers/users", method = RequestMethod.GET)
    public List<Transfer> getAllTransfersForUser(Principal principal) {
        return transferDAO.getAllTransfersForUser(userDAO.findIdByUsername(principal.getName()));
    }

    @RequestMapping(path = "/transfers/pending/users", method = RequestMethod.GET)
    public List<Transfer> getPendingTransfers(Principal principal) {
        return transferDAO.getPendingTransfers(userDAO.findIdByUsername(principal.getName()));
    }

    @RequestMapping(path = "/transfers/pending/approve", method = RequestMethod.PUT)
    public void approveTransfer(@RequestBody Transfer transfer) {
        transferDAO.approveTransfer(transfer);
    }

    @RequestMapping(path = "/transfers/pending/reject", method = RequestMethod.PUT)
    public void rejectTransfer(@RequestBody Transfer transfer) {
        transferDAO.rejectTransfer(transfer);
    }

    @RequestMapping(path = "/transfers/{transferId}", method = RequestMethod.GET)
    public Transfer getTransferDetails(@PathVariable int transferId) {
        return transferDAO.getTransferDetails(transferId);
    }
}