package seedu.innsync.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.innsync.commons.util.ToStringBuilder;
import seedu.innsync.logic.Messages;
import seedu.innsync.logic.commands.exceptions.CommandException;
import seedu.innsync.model.Model;
import seedu.innsync.model.person.Person;
import seedu.innsync.model.tag.BookingTag;

/**
 * Finds and lists persons in address book whose details match the given keywords.
 * Supports searching by multiple fields simultaneously: name, phone, email, address, tags, booking tags, memo.
 * Uses OR logic between different search types - a person matches if they match ANY of the search criteria.
 * Uses OR logic between keywords of the same type - a person matches a search type if they match ANY of its keywords.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";
    private static final Logger logger = Logger.getLogger(FindCommand.class.getName());

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds persons by one or more fields simultaneously. \n"
            + "Parameters: \n"
            + "  By Name: n/KEYWORD [MORE_KEYWORDS]...\n"
            + "  By Phone: p/KEYWORD [MORE_KEYWORDS]...\n"
            + "  By Email: e/KEYWORD [MORE_KEYWORDS]...\n"
            + "  By Address: a/KEYWORD [MORE_KEYWORDS]...\n"
            + "  By Tag: t/KEYWORD [MORE_KEYWORDS]...\n"
            + "  By Memo: m/KEYWORD [MORE_KEYWORDS]...\n"
            + "  By BookingTag (Date): bd/DATE [MORE_DATES]...\n"
            + "  By BookingTag (Property): bp/PROPERTY [MORE_KEYWORDS]...\n"
            + "Examples: \n"
            + "  " + COMMAND_WORD + " John\n"
            + "  " + COMMAND_WORD + " n/John\n"
            + "  " + COMMAND_WORD + " p/91234567\n"
            + "  " + COMMAND_WORD + " t/friend\n"
            + "  " + COMMAND_WORD + " m/breakfast \n"
            + "  " + COMMAND_WORD + " bd/2024-10-15\n"
            + "  " + COMMAND_WORD + " bp/BeachHouse\n"
            + "  " + COMMAND_WORD + " n/John a/Clementi\n"
            + "  " + COMMAND_WORD + " n/John p/91234567 t/friend m/breakfast";


    private final Map<SearchType, List<String>> searchCriteria;

    /**
     * Enum to represent the type of search being performed
     */
    public enum SearchType {
        NAME, PHONE, EMAIL, ADDRESS, TAG, BOOKING_DATE, BOOKING_PROPERTY, MEMO
    }

    /**
     * Constructor for searching by multiple fields
     */
    public FindCommand(Map<SearchType, List<String>> searchCriteria) {
        this.searchCriteria = searchCriteria;
        logger.info("FindCommand initialized with search criteria: " + searchCriteria);
    }

    public Map<SearchType, List<String>> getSearchCriteria() {
        return this.searchCriteria;
    }

    /**
     * Returns a predicate based on the search criteria.
     * This method is primarily used for testing purposes.
     * @return a predicate that can be used to filter persons based on the search criteria
     * @throws IllegalStateException if search criteria map is null or empty
     */
    public Predicate<Person> getPredicate() {
        logger.fine("Getting predicate from search criteria");
        return createCombinedPredicate();
    }

    /**
     * Creates a combined predicate based on all search criteria
     * Uses OR logic between different search types - a person matches if they match ANY of the search criteria
     * @throws IllegalStateException if search criteria map is null or empty
     */
    private Predicate<Person> createCombinedPredicate() {
        validateSearchCriteria();
        logger.info("Creating combined predicate from search criteria with OR logic between fields");
        return person -> matchesAnySearchCriteria(person);
    }

    /**
     * Validates that the search criteria is not null or empty
     */
    private void validateSearchCriteria() {
        // Assert search criteria is not null
        assert searchCriteria != null : "Search criteria map cannot be null";

        // Throw IllegalStateException if map is empty
        if (searchCriteria.isEmpty()) {
            logger.warning("Search criteria map is empty");
            throw new IllegalStateException("At least one search criterion must be provided");
        }
    }

    /**
     * Checks if a person matches any of the search criteria (OR logic between different search types)
     */
    private boolean matchesAnySearchCriteria(Person person) {
        return searchCriteria.entrySet().stream().anyMatch(entry -> {
            SearchType type = entry.getKey();
            List<String> keywords = entry.getValue();

            assert type != null : "Search type cannot be null";
            assert keywords != null : "Keywords list cannot be null for search type: " + type;

            boolean matches = matchesAnyKeyword(person, type, keywords);

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Person " + person.getName().fullName
                        + " match for criteria type " + type + ": " + matches);
            }

            return matches;
        });
    }

    /**
     * Checks if a person matches any of the keywords for a specific field type
     */
    private boolean matchesAnyKeyword(Person person, SearchType type, List<String> keywords) {
        return keywords.stream()
                .map(keyword -> {
                    // Assert each keyword is not null
                    assert keyword != null : "Keyword cannot be null for search type: " + type;
                    return keyword.toLowerCase();
                })
                .anyMatch(keyword -> matchField(person, keyword, type));
    }

    /**
     * Matches a person against a keyword for a specific search type
     * @throws IllegalArgumentException if person or keyword is null or searchType is invalid
     */
    private boolean matchField(Person person, String keyword, SearchType searchType) {
        validateMatchFieldParameters(person, keyword, searchType);
        switch (searchType) {
        case NAME:
            return matchNameField(person, keyword);
        case PHONE:
            return matchPhoneField(person, keyword);
        case EMAIL:
            return matchEmailField(person, keyword);
        case ADDRESS:
            return matchAddressField(person, keyword);
        case TAG:
            return matchTagField(person, keyword);
        case BOOKING_DATE:
            return matchBookingDateField(person, keyword);
        case BOOKING_PROPERTY:
            return matchBookingPropertyField(person, keyword);
        case MEMO:
            return matchMemoField(person, keyword);
        default:
            logger.warning("Unsupported search type encountered: " + searchType);
            throw new IllegalArgumentException("Unsupported search type: " + searchType);
        }
    }

    /**
     * Validates parameters for matchField method
     */
    private void validateMatchFieldParameters(Person person, String keyword, SearchType searchType) {
        // Validate parameters
        if (person == null) {
            logger.warning("Null person provided for matching");
            throw new IllegalArgumentException("Person cannot be null");
        }
        if (keyword == null) {
            logger.warning("Null keyword provided for matching");
            throw new IllegalArgumentException("Keyword cannot be null");
        }
        if (searchType == null) {
            logger.warning("Null searchType provided for matching");
            throw new IllegalArgumentException("SearchType cannot be null");
        }

        // Assert parameters are valid
        assert !keyword.isEmpty() : "Keyword cannot be empty";
        assert person.getName() != null : "Person name cannot be null";
    }

    /**
     * Matches a person's name against a keyword
     */
    private boolean matchNameField(Person person, String keyword) {
        boolean matches = person.getName().fullName.toLowerCase().contains(keyword);
        if (matches && logger.isLoggable(Level.FINE)) {
            logger.fine("Name match found: " + person.getName().fullName + " contains '" + keyword + "'");
        }
        return matches;
    }

    /**
     * Matches a person's phone against a keyword
     */
    private boolean matchPhoneField(Person person, String keyword) {
        boolean matches = person.getPhone() != null
                && person.getPhone().value.toLowerCase().contains(keyword);
        if (matches && logger.isLoggable(Level.FINE)) {
            logger.fine("Phone match found: " + person.getPhone().value + " contains '" + keyword + "'");
        }
        return matches;
    }

    /**
     * Matches a person's email against a keyword
     */
    private boolean matchEmailField(Person person, String keyword) {
        boolean matches = person.getEmail() != null
                && person.getEmail().value.toLowerCase().contains(keyword);
        if (matches && logger.isLoggable(Level.FINE)) {
            logger.fine("Email match found: " + person.getEmail().value + " contains '" + keyword + "'");
        }
        return matches;
    }

    /**
     * Matches a person's address against a keyword
     */
    private boolean matchAddressField(Person person, String keyword) {
        boolean matches = person.getAddress() != null
                && person.getAddress().value.toLowerCase().contains(keyword);
        if (matches && logger.isLoggable(Level.FINE)) {
            logger.fine("Address match found: " + person.getAddress().value + " contains '" + keyword + "'");
        }
        return matches;
    }

    /**
     * Matches a person's tags against a keyword
     */
    private boolean matchTagField(Person person, String keyword) {
        // Verify tags collection is not null before attempting to stream
        if (person.getTags() == null) {
            return false;
        }

        boolean matches = person.getTags().stream()
                .filter(tag -> tag != null) // Filter out any null tags for robustness
                .anyMatch(tag -> tag.tagName != null && tag.tagName.toLowerCase().contains(keyword));

        if (matches && logger.isLoggable(Level.FINE)) {
            logger.fine("Tag match found for person: " + person.getName().fullName
                    + " with keyword: '" + keyword + "'");
        }
        return matches;
    }

    /**
     * Matches a person's booking tags based on date against a keyword
     * @throws IllegalArgumentException if person or keyword is null
     */
    private boolean matchBookingDateField(Person person, String keyword) {
        try {
            if (person == null) {
                logger.warning("Null person provided for booking date matching");
                throw new IllegalArgumentException("Person cannot be null when matching booking date");
            }
            if (keyword == null) {
                logger.warning("Null keyword provided for booking date matching");
                throw new IllegalArgumentException("Keyword cannot be null when matching booking date");
            }

            assert !keyword.isEmpty() : "Booking date keyword cannot be empty";

            if (person.getBookingTags() == null) {
                return false;
            }

            boolean matches = person.getBookingTags().stream()
                    .filter(tag -> tag != null) // Filter out any null booking tags for robustness
                    .anyMatch(bookingTag -> isDateInBookingPeriod(keyword, bookingTag));

            if (matches && logger.isLoggable(Level.FINE)) {
                logger.fine("Booking date match found for person: " + person.getName().fullName
                        + " with date: '" + keyword + "'");
            }

            return matches;
        } catch (Exception e) {
            logger.warning("Error in matchBookingDateField: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Matches a person's booking tags based on property name against a keyword
     * @throws IllegalArgumentException if person or keyword is null
     */
    private boolean matchBookingPropertyField(Person person, String keyword) {
        try {
            if (person == null) {
                logger.warning("Null person provided for booking property matching");
                throw new IllegalArgumentException("Person cannot be null when matching booking property");
            }
            if (keyword == null) {
                logger.warning("Null keyword provided for booking property matching");
                throw new IllegalArgumentException("Keyword cannot be null when matching booking property");
            }

            assert !keyword.isEmpty() : "Booking property keyword cannot be empty";

            if (person.getBookingTags() == null) {
                return false;
            }
            boolean matches = person.getBookingTags().stream()
                    .filter(tag -> tag != null)
                    .anyMatch(bookingTag -> bookingTag.bookingTag != null
                            && bookingTag.bookingTag.toLowerCase().contains(keyword));

            if (matches && logger.isLoggable(Level.FINE)) {
                logger.fine("Booking property match found for person: " + person.getName().fullName
                        + " with property: '" + keyword + "'");
            }
            return matches;
        } catch (Exception e) {
            logger.warning("Error in matchBookingPropertyField: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Matches a person's memo against a keyword
     * @throws IllegalArgumentException if person or keyword is null
     */
    private boolean matchMemoField(Person person, String keyword) {
        try {
            if (person == null) {
                logger.warning("Null person provided for memo matching");
                throw new IllegalArgumentException("Person cannot be null when matching memo");
            }
            if (keyword == null) {
                logger.warning("Null keyword provided for memo matching");
                throw new IllegalArgumentException("Keyword cannot be null when matching memo");
            }

            assert !keyword.isEmpty() : "Memo keyword cannot be empty";

            boolean matches = person.getMemo() != null
                    && person.getMemo().value != null
                    && !person.getMemo().value.isEmpty()
                    && person.getMemo().value.toLowerCase().contains(keyword);

            if (matches && logger.isLoggable(Level.FINE)) {
                logger.fine("Memo match found for person: " + person.getName().fullName
                        + " with keyword: '" + keyword + "'");
            }
            return matches;
        } catch (Exception e) {
            logger.warning("Error in matchMemoField: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Checks if a date is within a booking period
     * @param dateString the date string to check in yyyy-MM-dd format
     * @param bookingTag the booking tag to check against
     * @return true if the date is within the booking period, false otherwise
     * @throws IllegalArgumentException if dateString or bookingTag is null
     */
    private boolean isDateInBookingPeriod(String dateString, BookingTag bookingTag) {
        try {
            if (dateString == null) {
                logger.warning("Null date string provided for booking period check");
                throw new IllegalArgumentException("Date string cannot be null");
            }
            if (bookingTag == null) {
                logger.warning("Null booking tag provided for booking period check");
                throw new IllegalArgumentException("Booking tag cannot be null");
            }

            assert !dateString.isEmpty() : "Date string cannot be empty";

            LocalDateTime date = LocalDateTime.parse(dateString + "T00:00:00");

            if (bookingTag.startDate == null || bookingTag.endDate == null) {
                logger.fine("Booking tag has null start or end date for date check: " + dateString);
                return false;
            }

            boolean inPeriod = !date.isBefore(bookingTag.startDate) && !date.isAfter(bookingTag.endDate);

            if (inPeriod && logger.isLoggable(Level.FINE)) {
                logger.fine("Date '" + dateString + "' is in booking period: "
                        + bookingTag.startDate + " to " + bookingTag.endDate);
            }

            return inPeriod;
        } catch (Exception e) {
            logger.warning("Error parsing date '" + dateString + "': " + e.getMessage());
            return false;
        }
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        logger.info("Executing FindCommand with criteria: " + searchCriteria);

        try {
            Predicate<Person> combinedPredicate = createCombinedPredicate();
            model.updateFilteredPersonList(combinedPredicate);

            int resultCount = model.getPersonList().size();
            logger.info("FindCommand execution complete. Found " + resultCount + " matching persons.");

            return new CommandResult(
                    String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, resultCount));
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warning("FindCommand execution failed with validation error: " + e.getMessage());
            throw new CommandException(e.getMessage());
        } catch (Exception e) {
            logger.severe("Unexpected error in FindCommand execution: " + e.getMessage());
            throw new CommandException(e.getMessage());
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof FindCommand)) {
            return false;
        }

        FindCommand otherFindCommand = (FindCommand) other;
        return this.searchCriteria.equals(otherFindCommand.searchCriteria);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("searchCriteria", searchCriteria)
                .toString();
    }
}

