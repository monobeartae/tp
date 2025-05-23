package seedu.innsync.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import seedu.innsync.commons.exceptions.DataLoadingException;
import seedu.innsync.model.ReadOnlyAddressBook;
import seedu.innsync.model.ReadOnlyUserPrefs;
import seedu.innsync.model.UserPrefs;

/**
 * API of the Storage component
 */
public interface Storage extends AddressBookStorage, UserPrefsStorage {

    @Override
    Optional<UserPrefs> readUserPrefs() throws DataLoadingException;

    @Override
    void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException;

    @Override
    Path getAddressBookFilePath();

    @Override
    Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException;

    @Override
    void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException;

}
