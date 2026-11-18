import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Stores and validates QuickChat messages.
 * Part 3 populates arrays for sent, disregarded and stored messages,
 * plus arrays of message hashes and IDs (no hard-coded test data).
 */
public class Message {
    private String messageID;
    private int messageNumber;
    private String recipient;
    private String messageText;
    private String sender;

    private static String currentSender = "";
    private static ArrayList<Message> sentMessages = new ArrayList<Message>();
    private static ArrayList<Message> disregardedMessages = new ArrayList<Message>();
    private static ArrayList<Message> storedMessages = new ArrayList<Message>();
    private static ArrayList<String> messageHashes = new ArrayList<String>();
    private static ArrayList<String> messageIDs = new ArrayList<String>();
    private static int totalMessagesSent = 0;
    private static int nextMessageNumber = 0;
    private static final String STORE_FILE = "storedMessages.json";

    /**
     * Creates a message and generates a random ten-digit Message ID.
     */
    public Message(String recipient, String messageText) {
        this.messageID = generateMessageID();
        this.messageNumber = nextMessageNumber;
        nextMessageNumber++;
        this.recipient = recipient;
        this.messageText = messageText;
        this.sender = currentSender;
    }

    /**
     * Creates a message with a supplied ID so unit tests can check the hash.
     */
    public Message(String messageID, String recipient, String messageText) {
        this(messageID, recipient, messageText, currentSender);
    }

    public Message(String messageID, String recipient, String messageText, String sender) {
        this.messageID = messageID;
        this.messageNumber = nextMessageNumber;
        nextMessageNumber++;
        this.recipient = recipient;
        this.messageText = messageText;
        this.sender = sender;
    }

    private Message(String messageID, String recipient, String messageText, String sender, int storedNumber) {
        this.messageID = messageID;
        this.messageNumber = storedNumber;
        this.recipient = recipient;
        this.messageText = messageText;
        this.sender = sender;
        if (storedNumber >= nextMessageNumber) {
            nextMessageNumber = storedNumber + 1;
        }
    }

    public static void setCurrentSender(String senderName) {
        currentSender = senderName;
    }

    public static void resetMessages() {
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        messageHashes.clear();
        messageIDs.clear();
        totalMessagesSent = 0;
        nextMessageNumber = 0;
        File file = new File(STORE_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * A ten-digit ID is built with a counted for loop (LU4).
     */
    private String generateMessageID() {
        Random random = new Random();
        String id = "";
        for (int count = 0; count < 10; count++) {
            id = id + random.nextInt(10);
        }
        return id;
    }

    private void captureIdAndHash() {
        messageIDs.add(messageID);
        messageHashes.add(createMessageHash());
    }

    /**
     * Ensures that the message ID is not more than ten characters.
     */
    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }

    /**
     * Ensures that the recipient cell number starts with an international code
     * and that the number after the code is no more than ten characters.
     * Regular expression adapted from ITU-T Recommendation E.164:
     * https://www.itu.int/rec/T-REC-E.164
     */
    public String checkRecipientCell() {
        return checkRecipientCell(recipient);
    }

    public static String checkRecipientCell(String recipientNumber) {
        if (recipientNumber != null) {
            Pattern saNumber = Pattern.compile("^\\+27[0-9]{1,10}$");
            if (saNumber.matcher(recipientNumber).matches()) {
                return "Cell phone number successfully captured.";
            }
        }
        return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    }

    /**
     * Returns the length check used by the unit tests.
     */
    public String checkMessageLength() {
        return checkMessageLength(messageText);
    }

    public static String checkMessageLength(String text) {
        if (text != null && text.length() <= 250) {
            return "Message ready to send.";
        }

        int extraCharacters = 250;
        if (text != null) {
            extraCharacters = text.length() - 250;
        }
        return "Message exceeds 250 characters by " + extraCharacters + "; please reduce the size.";
    }

    /**
     * Creates a hash: first two digits of the ID, the message number,
     * then the first and last words, all in uppercase.
     */
    public String createMessageHash() {
        String firstTwo = "00";
        if (messageID != null && messageID.length() >= 2) {
            firstTwo = messageID.substring(0, 2);
        }

        String firstWord = "";
        String lastWord = "";
        if (messageText != null && !messageText.trim().equals("")) {
            String[] words = messageText.trim().split("\\s+");
            firstWord = words[0].replaceAll("[^A-Za-z0-9]", "");
            lastWord = words[words.length - 1].replaceAll("[^A-Za-z0-9]", "");
        }

        return (firstTwo + ":" + messageNumber + ":" + firstWord + lastWord).toUpperCase();
    }

    /**
     * Lets the user choose to send, store or disregard the message (LU3 switch).
     */
    public String SentMessage() {
        Scanner input = new Scanner(System.in);
        System.out.println("Select an option:");
        System.out.println("1) Send Message");
        System.out.println("2) Disregard Message");
        System.out.println("3) Store Message to send later");
        System.out.print("Enter choice: ");
        int choice = Integer.parseInt(input.nextLine());
        return SentMessage(choice);
    }

    /**
     * Same send / store / disregard logic for unit tests.
     * These flags populate the Part 3 arrays; test data is not hard-coded.
     */
    public String SentMessage(int choice) {
        if (choice == 1) {
            sentMessages.add(this);
            captureIdAndHash();
            totalMessagesSent++;
            return "Message successfully sent.";
        } else if (choice == 2) {
            disregardedMessages.add(this);
            captureIdAndHash();
            return "Press 0 to delete the message.";
        } else if (choice == 3) {
            storedMessages.add(this);
            captureIdAndHash();
            storeMessage();
            return "Message successfully stored.";
        } else {
            return "Invalid option.";
        }
    }

    /**
     * Returns all messages sent while the program is running.
     */
    public String printMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent.";
        }

        String allMessages = "";
        for (int index = 0; index < sentMessages.size(); index++) {
            Message current = sentMessages.get(index);
            allMessages = allMessages + current.formatMessageDetails();
            if (index < sentMessages.size() - 1) {
                allMessages = allMessages + "\n";
            }
        }
        return allMessages;
    }

    /**
     * Returns the total number of messages sent.
     */
    public int returnTotalMessages() {
        return totalMessagesSent;
    }

    /**
     * Stores the message in a JSON file for sending later.
     * JSON object format follows the common key/value structure described at:
     * https://www.json.org/json-en.html
     */
    public String storeMessage() {
        writeStoredMessagesToJson();
        return "Message successfully stored.";
    }

    /**
     * Reads storedMessages.json back into the stored-messages array.
     * JSON parsing approach based on https://www.json.org/json-en.html
     */
    public static void loadStoredMessagesFromJson() {
        File file = new File(STORE_FILE);
        if (!file.exists() || file.length() == 0) {
            return;
        }

        try {
            Scanner reader = new Scanner(file);
            String contents = "";
            while (reader.hasNextLine()) {
                contents = contents + reader.nextLine();
            }
            reader.close();

            contents = contents.trim();
            if (contents.startsWith("[")) {
                contents = contents.substring(1);
            }
            if (contents.endsWith("]")) {
                contents = contents.substring(0, contents.length() - 1);
            }

            storedMessages.clear();
            String[] objects = contents.split("\\},\\s*\\{");
            for (int index = 0; index < objects.length; index++) {
                String object = objects[index].trim();
                if (object.equals("")) {
                    continue;
                }
                object = object.replace("{", "").replace("}", "");
                String id = readJsonValue(object, "messageID");
                String recipientNumber = readJsonValue(object, "recipient");
                String text = readJsonValue(object, "message");
                String senderName = readJsonValue(object, "sender");
                String numberText = readJsonValue(object, "messageNumber");
                int storedNumber = 0;
                if (!numberText.equals("")) {
                    storedNumber = Integer.parseInt(numberText);
                }
                if (!id.equals("") || !text.equals("")) {
                    Message loaded = new Message(id, recipientNumber, text, senderName, storedNumber);
                    storedMessages.add(loaded);
                    messageIDs.add(loaded.getMessageID());
                    messageHashes.add(loaded.createMessageHash());
                }
            }
        } catch (IOException error) {
            System.out.println("Stored messages could not be loaded.");
        }
    }

    private static void writeStoredMessagesToJson() {
        try {
            FileWriter writer = new FileWriter(STORE_FILE, false);
            writer.write("[\n");
            for (int index = 0; index < storedMessages.size(); index++) {
                Message current = storedMessages.get(index);
                writer.write(current.toJsonObject());
                if (index < storedMessages.size() - 1) {
                    writer.write(",\n");
                }
            }
            writer.write("\n]");
            writer.close();
        } catch (IOException error) {
            System.out.println("Stored messages could not be saved.");
        }
    }

    private String toJsonObject() {
        return "{\"messageID\":\"" + escapeJson(messageID)
                + "\",\"messageHash\":\"" + escapeJson(createMessageHash())
                + "\",\"messageNumber\":\"" + messageNumber
                + "\",\"recipient\":\"" + escapeJson(recipient)
                + "\",\"message\":\"" + escapeJson(messageText)
                + "\",\"sender\":\"" + escapeJson(sender) + "\"}";
    }

    private static String readJsonValue(String object, String key) {
        String search = "\"" + key + "\":\"";
        int start = object.indexOf(search);
        if (start < 0) {
            return "";
        }
        start = start + search.length();
        String value = "";
        for (int index = start; index < object.length(); index++) {
            char current = object.charAt(index);
            if (current == '\\' && index + 1 < object.length()) {
                value = value + object.charAt(index + 1);
                index++;
            } else if (current == '"') {
                break;
            } else {
                value = value + current;
            }
        }
        return value;
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public String formatMessageDetails() {
        return "Message ID: " + messageID
                + "\nMessage Hash: " + createMessageHash()
                + "\nRecipient: " + recipient
                + "\nMessage: " + messageText;
    }

    /**
     * Sent-message texts used to check that the SentMessage array is populated.
     */
    public static String displaySentMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent.";
        }
        String result = "";
        for (int index = 0; index < sentMessages.size(); index++) {
            result = result + "\"" + sentMessages.get(index).getMessageText() + "\"";
            if (index < sentMessages.size() - 1) {
                result = result + ", ";
            }
        }
        return result;
    }

    /**
     * Longest message among sent, stored and disregarded messages.
     */
    public static String displayLongestMessage() {
        Message longest = longestInList(sentMessages);
        longest = longerMessage(longest, longestInList(storedMessages));
        longest = longerMessage(longest, longestInList(disregardedMessages));
        if (longest == null) {
            return "No messages found.";
        }
        return longest.getMessageText();
    }

    public static String displayLongestStoredMessage() {
        Message longest = longestInList(storedMessages);
        if (longest == null) {
            return "No stored messages found.";
        }
        return longest.getMessageText();
    }

    private static Message longestInList(ArrayList<Message> list) {
        if (list.isEmpty()) {
            return null;
        }
        Message longest = list.get(0);
        for (int index = 1; index < list.size(); index++) {
            if (list.get(index).getMessageText().length() > longest.getMessageText().length()) {
                longest = list.get(index);
            }
        }
        return longest;
    }

    private static Message longerMessage(Message first, Message second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        if (second.getMessageText().length() > first.getMessageText().length()) {
            return second;
        }
        return first;
    }

    /**
     * Search sent or stored messages by Message ID.
     */
    public static String searchByMessageID(String id) {
        Message found = findById(sentMessages, id);
        if (found == null) {
            found = findById(storedMessages, id);
        }
        if (found == null) {
            return "Message ID not found.";
        }
        return found.getMessageText();
    }

    public static String searchStoredByMessageID(String id) {
        Message found = findById(storedMessages, id);
        if (found == null) {
            return "Message ID not found.";
        }
        return "Recipient: " + found.getRecipient() + "\nMessage: " + found.getMessageText();
    }

    private static Message findById(ArrayList<Message> list, String id) {
        for (int index = 0; index < list.size(); index++) {
            if (list.get(index).getMessageID().equals(id)) {
                return list.get(index);
            }
        }
        return null;
    }

    /**
     * Search all messages sent or stored for a recipient.
     */
    public static String searchByRecipient(String recipientNumber) {
        ArrayList<Message> matches = new ArrayList<Message>();
        addRecipientMatches(sentMessages, recipientNumber, matches);
        addRecipientMatches(storedMessages, recipientNumber, matches);

        if (matches.isEmpty()) {
            return "No messages found for that recipient.";
        }

        String result = "";
        for (int index = 0; index < matches.size(); index++) {
            result = result + "\"" + matches.get(index).getMessageText() + "\"";
            if (index < matches.size() - 1) {
                result = result + " ";
            }
        }
        return result;
    }

    public static String searchStoredByRecipient(String recipientNumber) {
        ArrayList<Message> matches = new ArrayList<Message>();
        addRecipientMatches(storedMessages, recipientNumber, matches);
        if (matches.isEmpty()) {
            return "No stored messages found for that recipient.";
        }
        String result = "";
        for (int index = 0; index < matches.size(); index++) {
            result = result + matches.get(index).formatMessageDetails();
            if (index < matches.size() - 1) {
                result = result + "\n";
            }
        }
        return result;
    }

    private static void addRecipientMatches(ArrayList<Message> source, String recipientNumber,
                                            ArrayList<Message> matches) {
        for (int index = 0; index < source.size(); index++) {
            if (source.get(index).getRecipient().equals(recipientNumber)) {
                matches.add(source.get(index));
            }
        }
    }

    /**
     * Deletes a stored message using its hash.
     */
    public static String deleteByHash(String hash) {
        for (int index = 0; index < storedMessages.size(); index++) {
            Message current = storedMessages.get(index);
            if (current.createMessageHash().equalsIgnoreCase(hash)) {
                String text = current.getMessageText();
                if (text.endsWith(".")) {
                    text = text.substring(0, text.length() - 1);
                }
                storedMessages.remove(index);
                writeStoredMessagesToJson();
                return "Message: \"" + text + "\" successfully deleted.";
            }
        }
        return "Message hash not found.";
    }

    /**
     * Report of all sent messages: hash, recipient and message.
     */
    public static String displaySentReport() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent.";
        }
        String report = "";
        for (int index = 0; index < sentMessages.size(); index++) {
            Message current = sentMessages.get(index);
            report = report + "Message Hash: " + current.createMessageHash()
                    + "\nRecipient: " + current.getRecipient()
                    + "\nMessage: " + current.getMessageText();
            if (index < sentMessages.size() - 1) {
                report = report + "\n";
            }
        }
        return report;
    }

    /**
     * Full details of all stored messages.
     */
    public static String displayStoredReport() {
        if (storedMessages.isEmpty()) {
            return "No messages have been stored.";
        }
        String report = "";
        for (int index = 0; index < storedMessages.size(); index++) {
            report = report + storedMessages.get(index).formatStoredDetails();
            if (index < storedMessages.size() - 1) {
                report = report + "\n";
            }
        }
        return report;
    }

    public static String displayStoredSendersAndRecipients() {
        if (storedMessages.isEmpty()) {
            return "No messages have been stored.";
        }
        String result = "";
        for (int index = 0; index < storedMessages.size(); index++) {
            Message current = storedMessages.get(index);
            result = result + "Sender: " + current.getSender()
                    + "\nRecipient: " + current.getRecipient();
            if (index < storedMessages.size() - 1) {
                result = result + "\n";
            }
        }
        return result;
    }

    public String formatStoredDetails() {
        return "Sender: " + sender
                + "\nMessage ID: " + messageID
                + "\nMessage Hash: " + createMessageHash()
                + "\nRecipient: " + recipient
                + "\nMessage: " + messageText;
    }

    public String getMessageID() {
        return messageID;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getSender() {
        return sender;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public static ArrayList<Message> getSentMessages() {
        return sentMessages;
    }

    public static ArrayList<Message> getDisregardedMessages() {
        return disregardedMessages;
    }

    public static ArrayList<Message> getStoredMessages() {
        return storedMessages;
    }

    public static ArrayList<String> getMessageHashes() {
        return messageHashes;
    }

    public static ArrayList<String> getMessageIDs() {
        return messageIDs;
    }
}
