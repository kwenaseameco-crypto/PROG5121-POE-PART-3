import java.util.Scanner;

/**
 * QuickChat console application.
 * Part 1: registration and login.
 * Part 2: send, store or disregard messages.
 * Part 3: arrays, stored-message reports, search and delete.
 */
public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        Login login = new Login();

        registerUser(input, login);

        if (!login.checkUserName() || !login.checkPasswordComplexity() || !login.checkCellPhoneNumber()) {
            System.out.println("Registration failed. Please restart the application and try again.");
            input.close();
            return;
        }

        boolean loggedIn = loginUser(input, login);
        if (!loggedIn) {
            System.out.println("You must be logged in to send messages.");
            input.close();
            return;
        }

        Message.setCurrentSender(login.getFirstName() + " " + login.getLastName());
        Message.loadStoredMessagesFromJson();
        runQuickChat(input);
        input.close();
    }

    /**
     * Collects registration details and reports each validation message.
     */
    public static void registerUser(Scanner input, Login login) {
        System.out.println("=== Registration ===");
        System.out.print("Enter first name: ");
        login.setFirstName(input.nextLine());

        System.out.print("Enter last name: ");
        login.setLastName(input.nextLine());

        System.out.print("Enter username: ");
        login.setUsername(input.nextLine());
        System.out.println(login.getUsernameMessage());

        System.out.print("Enter password: ");
        login.setPassword(input.nextLine());
        System.out.println(login.getPasswordMessage());

        System.out.print("Enter South African cell phone number (include +27): ");
        login.setCellPhoneNumber(input.nextLine());
        System.out.println(login.getCellPhoneMessage());

        System.out.println();
        System.out.println(login.registerUser());
    }

    /**
     * Confirms that the same username and password were entered.
     */
    public static boolean loginUser(Scanner input, Login login) {
        System.out.println();
        System.out.println("==== Login ====");
        System.out.print("Enter username: ");
        login.setEnteredUsername(input.nextLine());

        System.out.print("Enter password: ");
        login.setEnteredPassword(input.nextLine());

        System.out.println(login.returnLoginStatus());
        return login.loginUser();
    }

    /**
     * Displays the QuickChat menu until the user chooses Quit.
     */
    public static void runQuickChat(Scanner input) {
        System.out.println();
        System.out.println("Welcome to QuickChat.");

        int choice;
        do {
            System.out.println();
            System.out.println("Please choose an option:");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Stored Messages");
            System.out.println("4) Quit");
            System.out.print("Enter option: ");
            choice = readInteger(input);

            switch (choice) {
                case 1:
                    sendMessages(input);
                    break;
                case 2:
                    System.out.println(Message.displaySentMessages());
                    break;
                case 3:
                    runStoredMessagesMenu(input);
                    break;
                case 4:
                    System.out.println("Goodbye.");
                    break;
                default:
                    System.out.println("Invalid option. Please enter 1, 2, 3 or 4.");
                    break;
            }
        } while (choice != 4);
    }

    /**
     * Stored Messages submenu uses the Part 3 arrays.
     */
    public static void runStoredMessagesMenu(Scanner input) {
        int choice;
        do {
            System.out.println();
            System.out.println("=== Stored Messages ===");
            System.out.println("1) Display sender and recipient of all stored messages");
            System.out.println("2) Display the longest stored message");
            System.out.println("3) Search for a message ID");
            System.out.println("4) Search by recipient");
            System.out.println("5) Delete a message using the message hash");
            System.out.println("6) Display a full report of stored messages");
            System.out.println("7) Back to main menu");
            System.out.print("Enter option: ");
            choice = readInteger(input);

            switch (choice) {
                case 1:
                    System.out.println(Message.displayStoredSendersAndRecipients());
                    break;
                case 2:
                    System.out.println(Message.displayLongestStoredMessage());
                    break;
                case 3:
                    System.out.print("Enter message ID: ");
                    System.out.println(Message.searchStoredByMessageID(input.nextLine()));
                    break;
                case 4:
                    System.out.print("Enter recipient number: ");
                    System.out.println(Message.searchStoredByRecipient(input.nextLine()));
                    break;
                case 5:
                    System.out.print("Enter message hash: ");
                    System.out.println(Message.deleteByHash(input.nextLine()));
                    break;
                case 6:
                    System.out.println(Message.displayStoredReport());
                    break;
                case 7:
                    break;
                default:
                    System.out.println("Invalid option. Please enter 1 to 7.");
                    break;
            }
        } while (choice != 7);
    }

    /**
     * The user chooses how many messages to enter. A for loop then allows
     * only that number of messages (definite repetition, LU4).
     */
    public static void sendMessages(Scanner input) {
        System.out.print("How many messages do you wish to enter? ");
        int numberOfMessages = readInteger(input);
        while (numberOfMessages < 1) {
            System.out.print("Please enter a number greater than 0: ");
            numberOfMessages = readInteger(input);
        }

        Message lastMessage = null;
        for (int count = 1; count <= numberOfMessages; count++) {
            System.out.println();
            System.out.println("Message " + count + " of " + numberOfMessages);

            String recipient = captureRecipient(input);
            String text = captureMessageText(input);

            Message message = new Message(recipient, text);
            lastMessage = message;

            System.out.println("Message ID generated: " + message.getMessageID());
            System.out.println(message.checkMessageLength());
            System.out.println(message.checkRecipientCell());

            String sendResult = message.SentMessage(readSendChoice(input));
            System.out.println(sendResult);

            if (sendResult.equals("Message successfully sent.")) {
                System.out.println(message.formatMessageDetails());
            }
        }

        int totalSent = 0;
        if (lastMessage != null) {
            totalSent = lastMessage.returnTotalMessages();
            System.out.println();
            System.out.println(lastMessage.printMessages());
        }
        System.out.println("Total number of messages sent: " + totalSent);
    }

    /**
     * while is used for validation because the number of attempts is unknown (LU4).
     */
    public static String captureRecipient(Scanner input) {
        System.out.print("Enter recipient cell number (include +27): ");
        String recipient = input.nextLine();

        while (!Message.checkRecipientCell(recipient).equals("Cell phone number successfully captured.")) {
            System.out.println(Message.checkRecipientCell(recipient));
            System.out.print("Enter recipient cell number (include +27): ");
            recipient = input.nextLine();
        }
        System.out.println(Message.checkRecipientCell(recipient));
        return recipient;
    }

    public static String captureMessageText(Scanner input) {
        System.out.print("Enter message (max 250 characters): ");
        String text = input.nextLine();

        while (!Message.checkMessageLength(text).equals("Message ready to send.")) {
            System.out.println("Please enter a message of less than 250 characters.");
            System.out.println(Message.checkMessageLength(text));
            System.out.print("Enter message (max 250 characters): ");
            text = input.nextLine();
        }
        return text;
    }

    public static int readSendChoice(Scanner input) {
        System.out.println("Select an option:");
        System.out.println("1) Send Message");
        System.out.println("2) Disregard Message");
        System.out.println("3) Store Message to send later");
        System.out.print("Enter choice: ");
        return readInteger(input);
    }

    /**
     * Keeps asking until the user enters a whole number (indefinite while loop).
     */
    public static int readInteger(Scanner input) {
        while (!input.hasNextInt()) {
            input.nextLine();
            System.out.print("Please enter a number: ");
        }
        int value = input.nextInt();
        input.nextLine();
        return value;
    }
}
