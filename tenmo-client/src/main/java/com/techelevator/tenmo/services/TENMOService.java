package com.techelevator.tenmo.services;

import com.techelevator.tenmo.exceptions.AccountServiceException;
import com.techelevator.tenmo.models.Transfer;
import com.techelevator.tenmo.models.User;
import io.cucumber.java.bs.A;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TENMOService {

    private final String BASE_SERVICE_URL;
    public String AUTH_TOKEN = "";
    private RestTemplate restTemplate = new RestTemplate();

    public TENMOService(String baseUrl) {
        this.BASE_SERVICE_URL = baseUrl;
    }



    public BigDecimal retrieveBalance (long userId) throws AccountServiceException {

        BigDecimal accountBalance = null;
        try {
        accountBalance = restTemplate.exchange(BASE_SERVICE_URL + "/accounts", HttpMethod.GET, makeAuthEntity(), BigDecimal.class).getBody();
        }

        catch (RestClientResponseException ex) {
                throw new AccountServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
        }
        return accountBalance;
    }

    public User[] getAllUsers () throws AccountServiceException {
        User[] userList = null;
        try {
            userList = restTemplate.exchange(BASE_SERVICE_URL + "/users", HttpMethod.GET, makeAuthEntity(), User[].class).getBody();
        }
        catch (RestClientResponseException ex){
            throw new AccountServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
        }
        return userList;
    }


    public Transfer sendTransfer (Transfer transfer) throws AccountServiceException  {
        if(transfer == null) {
            throw new AccountServiceException("Invalid Transfer");
        }
        try {
            restTemplate.exchange(BASE_SERVICE_URL + "/transfers", HttpMethod.POST, makeTransferEntity(transfer), Transfer.class);
        }
        catch (RestClientResponseException ex) {
            throw new AccountServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
        }
    return transfer;

    }

    public Transfer requestTransfer (Transfer transfer) throws AccountServiceException  {
        if(transfer == null) {
            throw new AccountServiceException("Invalid Transfer");
        }
        try {
            restTemplate.exchange(BASE_SERVICE_URL + "/transfers/pending", HttpMethod.POST, makeTransferEntity(transfer), Transfer.class);
        }
        catch (RestClientResponseException ex) {
            throw new AccountServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
        }
        return transfer;

    }

    public Transfer[] getAllTransfersForUser(long userId) {
        Transfer[] transferArray = null;

        transferArray = restTemplate.exchange(BASE_SERVICE_URL + "/transfers/users", HttpMethod.GET, makeAuthEntity(), Transfer[].class).getBody();

        return transferArray;
    }

    public Transfer[] getPendingTransfers(long userId) {
        Transfer[] pendingTransferArray = null;

        pendingTransferArray = restTemplate.exchange(BASE_SERVICE_URL + "/transfers/pending/users", HttpMethod.GET, makeAuthEntity(), Transfer[].class).getBody();

        return pendingTransferArray;
    }

    public void approveTransfer(Transfer transfer) {
        restTemplate.exchange(BASE_SERVICE_URL + "/transfers/pending/approve", HttpMethod.PUT, makeTransferEntity(transfer), Transfer.class).getBody();
    }

    public void rejectTransfer(Transfer transfer) {
        restTemplate.exchange(BASE_SERVICE_URL + "/transfers/pending/reject", HttpMethod.PUT, makeTransferEntity(transfer), Transfer.class).getBody();
    }

    public Transfer getTransferDetails(int transferId) {
        Transfer transfer = restTemplate.exchange(BASE_SERVICE_URL + "/transfers/" + transferId, HttpMethod.GET, makeAuthEntity(), Transfer.class).getBody();
    return transfer;
    }


    public void setAUTH_TOKEN(String aUTH_TOKEN) {
        AUTH_TOKEN = aUTH_TOKEN;
    }


    private HttpEntity<Transfer> makeTransferEntity(Transfer transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(AUTH_TOKEN);
        HttpEntity<Transfer> entity = new HttpEntity<>(transfer, headers);
        return entity;
    }

    private HttpEntity makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(AUTH_TOKEN);
        HttpEntity entity = new HttpEntity<>(headers);
        return entity;
    }

}
