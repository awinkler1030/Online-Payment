package com.techelevator.tenmo;

import com.techelevator.tenmo.exceptions.AccountServiceException;
import com.techelevator.tenmo.models.AuthenticatedUser;
import com.techelevator.tenmo.models.Transfer;
import com.techelevator.tenmo.models.UserCredentials;
import com.techelevator.tenmo.services.TENMOService;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.view.ConsoleService;

import java.math.BigDecimal;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private static final String MENU_OPTION_EXIT = "Exit";
    private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
    private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
    private static final String[] LOGIN_MENU_OPTIONS = {LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT};
    private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
    private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
    private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
    private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
    private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
    private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
    private static final String[] MAIN_MENU_OPTIONS = {MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS, MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS, MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT};

    private AuthenticatedUser currentUser;
    private ConsoleService console;
    private AuthenticationService authenticationService;
    private TENMOService tenmoService;

    public static void main(String[] args) throws AccountServiceException {
        App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL), new TENMOService(API_BASE_URL));
        app.run();
    }

    public App(ConsoleService console, AuthenticationService authenticationService, TENMOService accountService) {
        this.console = console;
        this.authenticationService = authenticationService;
        this.tenmoService = accountService;
    }

    public void run() throws AccountServiceException {
        System.out.println("*********************");
        System.out.println("* Welcome to TEnmo! *");
        System.out.println("*********************");

        registerAndLogin();
        mainMenu();
    }

    private void mainMenu() throws AccountServiceException {
        while (true) {
            String choice = (String) console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
            if (MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
                viewCurrentBalance();
            } else if (MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
                viewTransferHistory();
            } else if (MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
                viewPendingRequests();
            } else if (MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
                sendBucks();
            } else if (MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
                requestBucks();
            } else if (MAIN_MENU_OPTION_LOGIN.equals(choice)) {
                login();
            } else {
                // the only other option on the main menu is to exit
                exitProgram();
            }
        }
    }

    private void viewCurrentBalance() throws AccountServiceException {
        console.printAccountBalance(tenmoService.retrieveBalance(currentUser.getUser().getId()));

    }

    private void viewTransferHistory() {
        int userChoice = console.printTransferHistory(tenmoService.getAllTransfersForUser(currentUser.getUser().getId()), currentUser);

        if (userChoice == 0) {
        } else {
            console.printTransferDetails(tenmoService.getTransferDetails(userChoice));
        }
    }

    private void viewPendingRequests() throws AccountServiceException {
        int userTransferChoice = console.printPendingTransfers(tenmoService.getPendingTransfers(currentUser.getUser().getId()), currentUser);

        if (userTransferChoice == 0) {
        } else {
            Transfer transfer = tenmoService.getTransferDetails(userTransferChoice);
            Integer approveRejectChoice = console.promptUserToApproveOrReject();
            if (approveRejectChoice == 0) {
            } else if (approveRejectChoice == 1) {
                if ((transfer.getAmount()).compareTo(tenmoService.retrieveBalance(currentUser.getUser().getId())) <= 0) {
                    tenmoService.approveTransfer(transfer);
                    console.printMessage("Your transfer has been approved.");
                } else {
                    console.printMessage("Not enough funds. Transfer not approved.");
                }
            } else if (approveRejectChoice == 2) {
                tenmoService.rejectTransfer(transfer);
                console.printMessage("Your transfer has been rejected.");
            }
            //if approved - update balances, change status to approved
            //if rejected - change status to rejected

        }


    }

    private void sendBucks() throws AccountServiceException {
        int userID = console.printUserList(tenmoService.getAllUsers(), currentUser, "Enter ID of user you are sending to (0 to cancel)");
        BigDecimal transferAmount = null;
        Transfer transfer = new Transfer();

        if (userID == 0) {
        } else {
            boolean isRunning = true;
            while (isRunning) {  //run until user provides valid input
                transferAmount = console.askUserForAmount();

                if (transferAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    console.printMessage("Amount must be greater than zero.\n");
                } else if (transferAmount.compareTo(tenmoService.retrieveBalance(currentUser.getUser().getId())) <= 0) {

                    transfer.setAccountToUserId(userID);
                    transfer.setAccountFromUserId(currentUser.getUser().getId());
                    transfer.setAmount(transferAmount);
                    tenmoService.sendTransfer(transfer);
                    console.printMessage("Your transfer was successful.");
                    isRunning = false;

                } else {
                    if (tenmoService.retrieveBalance(currentUser.getUser().getId()).compareTo(BigDecimal.ZERO) == 0) {
                        console.printMessage("Your balance is $0.00. Unable to make a transfer.");
                        isRunning = false;
                    } else {
                        console.printMessage("\n****Your balance is insufficient for this transfer.****\n" +
                                "Please enter an amount less than or equal to your available balance of $" + tenmoService.retrieveBalance(currentUser.getUser().getId()) + ".\n");
                    }
                }
            }
        }

    }

    private void requestBucks() throws AccountServiceException {
        int userId = console.printUserList(tenmoService.getAllUsers(), currentUser, "Enter ID of user you are requesting from (0 to cancel)");
        BigDecimal transferAmount = null;
        Transfer transfer = new Transfer();

        if (userId == 0) {
        } else {
            boolean isRunning = true;
            while (isRunning) {  //run until user provides valid input
                transferAmount = console.askUserForAmount();

                if (transferAmount.compareTo(BigDecimal.ZERO) == 0) {
                    console.printMessage("Amount must be greater than zero.\n");
                } else {
                    transfer.setAccountToUserId(currentUser.getUser().getId());
                    transfer.setAccountFromUserId(userId);
                    transfer.setAmount(transferAmount);
                    tenmoService.requestTransfer(transfer);
                    isRunning = false;
                }
            }
        }
    }

    private void exitProgram() {
        System.exit(0);
    }

    private void registerAndLogin() {
        while (!isAuthenticated()) {
            String choice = (String) console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
            if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
                login();
            } else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
                register();
            } else {
                // the only other option on the login menu is to exit
                exitProgram();
            }
        }
    }

    private boolean isAuthenticated() {
        return currentUser != null;
    }

    private void register() {
        console.printMessage("Please register a new user account");
        boolean isRegistered = false;
        while (!isRegistered) //will keep looping until user is registered
        {
            UserCredentials credentials = collectUserCredentials();
            try {
                authenticationService.register(credentials);
                isRegistered = true;
                console.printMessage("Registration successful. You can now login.");
            } catch (AuthenticationServiceException e) {
                console.printMessage("REGISTRATION ERROR: " + e.getMessage());
                console.printMessage("Please attempt to register again.");
            }
        }
    }

    private void login() {
        console.printMessage("Please log in");
        currentUser = null;
        while (currentUser == null) //will keep looping until user is logged in
        {
            UserCredentials credentials = collectUserCredentials();
            try {
                currentUser = authenticationService.login(credentials);
                tenmoService.setAUTH_TOKEN(currentUser.getToken());

            } catch (AuthenticationServiceException e) {
                console.printMessage("LOGIN ERROR: " + e.getMessage());
                console.printMessage("Please attempt to login again.");
            }
        }
    }

    private UserCredentials collectUserCredentials() {
        String username = console.getUserInput("Username");
        String password = console.getUserInput("Password");
        return new UserCredentials(username, password);
    }
}
