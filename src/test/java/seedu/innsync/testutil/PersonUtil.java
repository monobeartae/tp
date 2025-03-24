package seedu.innsync.testutil;

import java.util.Set;

import seedu.innsync.logic.commands.AddCommand;
import seedu.innsync.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.innsync.model.person.Person;
import seedu.innsync.model.tag.BookingTag;
import seedu.innsync.model.tag.Tag;

import static seedu.innsync.logic.parser.CliSyntax.*;

/**
 * A utility class for Person.
 */
public class PersonUtil {

    /**
     * Returns an add command string for adding the {@code person}.
     */
    public static String getAddCommand(Person person) {
        return AddCommand.COMMAND_WORD + " " + getPersonDetails(person);
    }

    /**
     * Returns the part of command string for the given {@code person}'s details.
     */
    public static String getPersonDetails(Person person) {
        StringBuilder sb = new StringBuilder();
        sb.append(PREFIX_NAME + person.getName().fullName + " ");
        sb.append(PREFIX_PHONE + person.getPhone().value + " ");
        sb.append(PREFIX_EMAIL + person.getEmail().value + " ");
        sb.append(PREFIX_ADDRESS + person.getAddress().value + " ");
        sb.append(PREFIX_MEMO + person.getMemo().value + " ");
        person.getBookingTags().stream().forEach(
                s -> sb.append(PREFIX_BOOKINGTAG + s.bookingTagName + " ")
        );
        person.getTags().stream().forEach(
            s -> sb.append(PREFIX_TAG + s.tagName + " ")
        );
        return sb.toString();
    }

    /**
     * Returns the part of command string for the given {@code EditPersonDescriptor}'s details.
     */
    public static String getEditPersonDescriptorDetails(EditPersonDescriptor descriptor) {
        StringBuilder sb = new StringBuilder();
        descriptor.getName().ifPresent(name -> sb.append(PREFIX_NAME).append(name.fullName).append(" "));
        descriptor.getPhone().ifPresent(phone -> sb.append(PREFIX_PHONE).append(phone.value).append(" "));
        descriptor.getEmail().ifPresent(email -> sb.append(PREFIX_EMAIL).append(email.value).append(" "));
        descriptor.getAddress().ifPresent(address -> sb.append(PREFIX_ADDRESS).append(address.value).append(" "));
        descriptor.getMemo().ifPresent(memo -> sb.append(PREFIX_MEMO).append(memo.value).append(" "));
        if (descriptor.getBookingTags().isPresent()) {
            Set<BookingTag> bookingTags = descriptor.getBookingTags().get();
            if (bookingTags.isEmpty()) {
                sb.append(PREFIX_BOOKINGTAG).append(" ");
            } else {
                bookingTags.forEach(s -> sb.append(PREFIX_BOOKINGTAG).append(s.bookingTagName).append(" "));
            }
        }
        if (descriptor.getTags().isPresent()) {
            Set<Tag> tags = descriptor.getTags().get();
            if (tags.isEmpty()) {
                sb.append(PREFIX_TAG);
            } else {
                tags.forEach(s -> sb.append(PREFIX_TAG).append(s.tagName).append(" "));
            }
        }
        return sb.toString();
    }
}
