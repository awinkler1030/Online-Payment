package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDAO {

    public List<Transfer> getAllTransfersForUser(long userId);

    public List<Transfer> getPendingTransfers(long userId);

    public Transfer getTransferDetails(int transferId);

    public Transfer sendTransfer (Transfer transfer);

    public Transfer requestTransfer(Transfer transfer);

//    public void updateTransferStatus(Transfer transfer, Integer approveRejectChoice);

    public void approveTransfer(Transfer transfer);

    public void rejectTransfer(Transfer transfer);

}
