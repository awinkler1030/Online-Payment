package com.techelevator.view;


import com.techelevator.tenmo.models.AuthenticatedUser;
import com.techelevator.tenmo.models.Transfer;
import com.techelevator.tenmo.models.User;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ConsoleService {

    private PrintWriter out;
    private Scanner in;

    public ConsoleService(InputStream input, OutputStream output) {
        this.out = new PrintWriter(output, true);
        this.in = new Scanner(input);
    }

    public Object getChoiceFromOptions(Object[] options) {
        Object choice = null;
        while (choice == null) {
            displayMenuOptions(options);
            choice = getChoiceFromUserInput(options);
        }
        out.println();
        return choice;
    }

    private Object getChoiceFromUserInput(Object[] options) {
        Object choice = null;
        String userInput = in.nextLine();
        try {
            int selectedOption = Integer.valueOf(userInput);
            if (selectedOption > 0 && selectedOption <= options.length) {
                choice = options[selectedOption - 1];
            }
        } catch (NumberFormatException e) {
            // eat the exception, an error message will be displayed below since choice will be null
        }
        if (choice == null) {
            out.println(System.lineSeparator() + "*** " + userInput + " is not a valid option ***" + System.lineSeparator());
        }
        return choice;
    }

    private void displayMenuOptions(Object[] options) {
        out.println();
        for (int i = 0; i < options.length; i++) {
            int optionNum = i + 1;
            out.println(optionNum + ") " + options[i]);
        }
        out.print(System.lineSeparator() + "Please choose an option >>> ");
        out.flush();
    }

    public String getUserInput(String prompt) {
        out.print(prompt + ": ");
        out.flush();
        return in.nextLine();
    }

    public Integer getUserInputInteger(String prompt) {
        Integer result = null;
        do {
            out.print(prompt + ": ");
            out.flush();
            String userInput = in.nextLine();
            try {
                result = Integer.parseInt(userInput);
            } catch (NumberFormatException e) {
                out.println(System.lineSeparator() + "*** " + userInput + " is not valid ***" + System.lineSeparator());
            }
        } while (result == null);
        return result;
    }

    public void printAccountBalance(BigDecimal accountBalance) {

        System.out.println("Your current account balance is $" + accountBalance);

    }

    public int printUserList(User[] userArray, AuthenticatedUser currentUser, String userPrompt) {
        System.out.println("-------------------------------");
        System.out.println("Users");
        System.out.printf("%-10s %-10s %n", "ID", "Name");
        System.out.println("-------------------------------");
        for (User user : userArray) {
            if (!user.getId().equals(currentUser.getUser().getId())) {
                System.out.printf("%-10s %-10s %n", user.getId(), user.getUsername());
            }
        }
        System.out.println("-------------------------------");

        while (true) {
            Integer userChoice = getUserInputInteger(userPrompt);
            if (userChoice == 0) {
                return userChoice;
            } else {
                for (User user : userArray) {
                    if (user.getId().equals(userChoice)) {
                        return userChoice;
                    }
                }
            }
            System.out.println("\n****Invalid selection. Please select from the list above.****\n");
        }
    }

    public BigDecimal askUserForAmount() {
        while (true) {
            System.out.print("Enter amount: ");
            BigDecimal transferAmount;
            try {
                transferAmount = BigDecimal.valueOf(Float.parseFloat(in.nextLine()));
                return transferAmount;
            } catch (NumberFormatException ex) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }


    public int printTransferHistory(Transfer[] transferArray, AuthenticatedUser currentUser) {
        if (transferArray.length > 0) {
            System.out.println("----------------------------------------------------------");
            System.out.println("Transfers");
            System.out.printf("%-10s %-20s %10s     %-10s %n", "ID", "From/To", "Amount", "Status");
            System.out.println("----------------------------------------------------------");
            for (Transfer transfer : transferArray) {
                if (currentUser.getUser().getUsername().equals(transfer.getAccountFromUsername())) {
                    System.out.printf("%-10s %-20s %10s     %-10s %n", transfer.getTransferId(), "To: " + transfer.getAccountToUsername(), transfer.getAmount(), transfer.getTransferStatus());
                } else {
                    System.out.printf("%-10s %-20s %10s     %-10s %n", transfer.getTransferId(), "From: " + transfer.getAccountFromUsername(), transfer.getAmount(), transfer.getTransferStatus());
                }
            }
            System.out.println("----------------------------------------------------------");

            while (true) {
                Integer userChoice = getUserInputInteger("Please enter transfer ID to view details (0 to cancel)");
                if (userChoice == 0) {
                    return userChoice;
                }
                for (Transfer transfer : transferArray) {
                    if (transfer.getTransferId().equals(userChoice)) {
                        return userChoice;
                    }
                }
                System.out.println("\n****Invalid selection. Please select from the list above.****\n");
            }
        } else {
            System.out.println("No transfers found.");
            return 0;
        }
    }


    public int printPendingTransfers(Transfer[] transferArray, AuthenticatedUser currentUser) {
        if (transferArray.length > 0) {
            System.out.println("-----------------------------------------------");
            System.out.println("Pending Transfers");
            System.out.printf("%-10s %-20s %10s %n", "ID", "To", "Amount");
            System.out.println("-----------------------------------------------");
            for (Transfer transfer : transferArray) {
                if (currentUser.getUser().getUsername().equals(transfer.getAccountFromUsername())) {
                    System.out.printf("%-10s %-20s %10s %n", transfer.getTransferId(), transfer.getAccountToUsername(), transfer.getAmount());
                }
            }
            System.out.println("-----------------------------------------------");

            while (true) {
                Integer userChoice = getUserInputInteger("Please enter transfer ID to approve/reject (0 to cancel)");
                if (userChoice == 0) {
                    return userChoice;
                }
                for (Transfer transfer : transferArray) {
                    if (transfer.getTransferId().equals(userChoice)) {
                        return userChoice;
                    }
                }
                System.out.println("\n****Invalid selection. Please select from the list above.****\n");
            }
        } else {
            System.out.println("No transfers found.");
            return 0;
        }
    }

    public void printTransferDetails(Transfer transfer) {
        System.out.println("-----------------------------------------------");
        System.out.println("Transfer Details");
        System.out.println("-----------------------------------------------");
        System.out.println("Id: " + transfer.getTransferId());
        System.out.println("From: " + transfer.getAccountFromUsername());
        System.out.println("To: " + transfer.getAccountToUsername());
        System.out.println("Type: " + transfer.getTransferType());
        System.out.println("Status: " + transfer.getTransferStatus());
        System.out.println("Amount: $" + transfer.getAmount());
    }

    public Integer promptUserToApproveOrReject() {
        System.out.println("1: Approve");
        System.out.println("2: Reject");
        System.out.println("0: Don't approve or reject");
        System.out.println("-----------------------------------------------");

        while (true) {
            Integer userChoice = getUserInputInteger("Please choose an option");
            if (userChoice >= 0 && userChoice <=2) {
                return userChoice;
            }
            System.out.println("\n****Invalid selection. Please select from the list above.****\n");
        }
    }

    public void printMessage(String message) {
        System.out.println(message);
    }

}
