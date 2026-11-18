import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Part 3 tests. Arrays are populated by sending, storing or disregarding
 * the assignment test data (not hard-coded into the arrays).
 */
public class MessageArrayTest {

    private Message message2;

    @BeforeEach
    public void setUp() {
        Message.resetMessages();
        populateTestData();
    }

    /**
     * Test data Messages 1 to 5 from the Part 3 brief.
     */
    private void populateTestData() {
        Message.setCurrentSender("Developer");

        Message message1 = new Message("0000000001", "+27834557896",
                "Did you get the cake?", "Developer");
        message1.SentMessage(1);

        message2 = new Message("0000000002", "+27838884567",
                "Where are you? You are late! I have asked you to be on time.", "Developer");
        message2.SentMessage(3);

        Message message3 = new Message("0000000003", "+27834484567",
                "Yohoooo, I am at your gate.", "Developer");
        message3.SentMessage(2);

        Message message4 = new Message("0838884567", "0838884567",
                "It is dinner time !", "Developer");
        message4.SentMessage(1);

        Message message5 = new Message("0000000005", "+27838884567",
                "Ok, I am leaving without you.", "Developer");
        message5.SentMessage(3);
    }

    @Test
    public void sentMessagesArrayCorrectlyPopulated() {
        assertEquals("\"Did you get the cake?\", \"It is dinner time !\"",
                Message.displaySentMessages());
        assertEquals(2, Message.getSentMessages().size());
    }

    @Test
    public void displayTheLongestMessage() {
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                Message.displayLongestMessage());
    }

    @Test
    public void searchForMessageID() {
        assertEquals("It is dinner time !", Message.searchByMessageID("0838884567"));
    }

    @Test
    public void searchAllMessagesSentOrStoredForRecipient() {
        assertEquals(
                "\"Where are you? You are late! I have asked you to be on time.\" \"Ok, I am leaving without you.\"",
                Message.searchByRecipient("+27838884567")
        );
    }

    @Test
    public void deleteMessageUsingMessageHash() {
        String hash = message2.createMessageHash();
        assertEquals(
                "Message: \"Where are you? You are late! I have asked you to be on time\" successfully deleted.",
                Message.deleteByHash(hash)
        );
        assertEquals(1, Message.getStoredMessages().size());
    }

    @Test
    public void displaySentReport() {
        String report = Message.displaySentReport();
        assertTrue(report.contains("Did you get the cake?"));
        assertTrue(report.contains("It is dinner time !"));
        assertTrue(report.contains("+27834557896"));
        assertTrue(report.contains("Message Hash:"));
        assertTrue(report.contains("Recipient:"));
        assertTrue(report.contains("Message:"));
    }

    @Test
    public void storedSendersAndRecipients() {
        String result = Message.displayStoredSendersAndRecipients();
        assertTrue(result.contains("Sender: Developer"));
        assertTrue(result.contains("+27838884567"));
    }

    @Test
    public void disregardedMessagesArrayPopulated() {
        assertEquals(1, Message.getDisregardedMessages().size());
        assertEquals("Yohoooo, I am at your gate.",
                Message.getDisregardedMessages().get(0).getMessageText());
    }

    @Test
    public void storedMessagesArrayPopulatedFromStoreAction() {
        assertEquals(2, Message.getStoredMessages().size());
    }

    @Test
    public void messageHashAndIdArraysPopulated() {
        assertEquals(5, Message.getMessageHashes().size());
        assertEquals(5, Message.getMessageIDs().size());
        assertTrue(Message.getMessageIDs().contains("0838884567"));
    }
}
