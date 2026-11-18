import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MessageTest {

    @BeforeEach
    public void setUp() {
        Message.resetMessages();
    }

    @Test
    public void messageLengthSuccess() {
        Message message = new Message("0000000000", "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");
        assertEquals("Message ready to send.", message.checkMessageLength());
    }

    @Test
    public void messageLengthFailure() {
        String longText = "";
        for (int count = 0; count < 260; count++) {
            longText = longText + "a";
        }
        Message message = new Message("0000000000", "+27718693002", longText);
        assertEquals("Message exceeds 250 characters by 10; please reduce the size.",
                message.checkMessageLength());
    }

    @Test
    public void recipientNumberCorrectlyFormatted() {
        Message message = new Message("0000000000", "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");
        assertEquals("Cell phone number successfully captured.", message.checkRecipientCell());
    }

    @Test
    public void recipientNumberIncorrectlyFormatted() {
        Message message = new Message("0000000000", "08575975889",
                "Hi Keegan, did you receive the payment?");
        assertEquals(
                "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.",
                message.checkRecipientCell()
        );
    }

    @Test
    public void messageHashIsCorrectForTestCase1() {
        Message message = new Message("0000000000", "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");
        assertEquals("00:0:HITONIGHT", message.createMessageHash());
    }

    @Test
    public void remainingMessageHashesAreCreatedInALoop() {
        Message first = new Message("0000000000", "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");
        Message second = new Message("0012345678", "+27718693002",
                "Hi Keegan, did you receive the payment?");

        String[] expected = {
                "00:0:HITONIGHT",
                "00:1:HIPAYMENT"
        };
        Message[] messages = {first, second};

        for (int index = 0; index < messages.length; index++) {
            assertEquals(expected[index], messages[index].createMessageHash());
        }
    }

    @Test
    public void messageIDIsCreated() {
        Message message = new Message("+27718693002",
                "Hi Mike, can you join us for dinner tonight?");
        assertTrue(message.checkMessageID());
        assertEquals(10, message.getMessageID().length());
        System.out.println("Message ID generated: " + message.getMessageID());
    }

    @Test
    public void sendMessageOption() {
        Message message = new Message("0000000000", "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");
        assertEquals("Message successfully sent.", message.SentMessage(1));
        assertEquals(1, message.returnTotalMessages());
    }

    @Test
    public void disregardMessageOption() {
        Message message = new Message("0000000000", "08575975889",
                "Hi Keegan, did you receive the payment?");
        assertEquals("Press 0 to delete the message.", message.SentMessage(2));
        assertEquals(0, message.returnTotalMessages());
    }

    @Test
    public void storeMessageOption() {
        Message message = new Message("0000000000", "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");
        assertEquals("Message successfully stored.", message.SentMessage(3));
        assertEquals(0, message.returnTotalMessages());
    }

    @Test
    public void twoMessagesOneSentOneDiscarded() {
        Message first = new Message("0000000000", "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");
        Message second = new Message("0012345678", "08575975889",
                "Hi Keegan, did you receive the payment?");

        assertEquals("Message successfully sent.", first.SentMessage(1));
        assertEquals("Press 0 to delete the message.", second.SentMessage(2));
        assertEquals(1, first.returnTotalMessages());
        assertTrue(first.printMessages().contains("Hi Mike, can you join us for dinner tonight?"));
        assertFalse(first.printMessages().contains("Hi Keegan, did you receive the payment?"));
    }
}
