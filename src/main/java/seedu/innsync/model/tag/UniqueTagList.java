package seedu.innsync.model.tag;

import static java.util.Objects.requireNonNull;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.innsync.model.tag.exceptions.DuplicateTagException;
import seedu.innsync.model.tag.exceptions.TagNotFoundException;

public class UniqueTagList {

    private final ObservableList<Tag> internalList = FXCollections.observableArrayList();
    private final ObservableList<Tag> internalUnmodifiableList =
            FXCollections.unmodifiableObservableList(internalList);

    /**
     * Returns true if the list contains an equivalent tag as the given argument.
     */
    public boolean contains(Tag toCheck) {
        requireNonNull(toCheck);
        return internalList.stream().anyMatch(toCheck::equals);
    }

    /**
     * Gets tag with the same name as the given argument.
     * Returns null if no such tag exists.
     */
    public Tag getTag(String name) {
        requireNonNull(name);
        return internalList.stream()
                .filter(tag -> tag.getTagName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets tag from list if it exists.
     * Returns null if no such tag exists.
     */
    public Tag getTag(Tag tag) {
        requireNonNull(tag);
        return internalList.stream()
                .filter(t -> t.equals(tag))
                .findFirst()
                .orElse(null);
    }

    /**
     * Adds a tag to the list.
     * The tag must not already exist in the list.
     */
    public void add(Tag toAdd) {
        requireNonNull(toAdd);
        if (contains(toAdd)) {
            throw new DuplicateTagException();
        }
        internalList.add(toAdd);
    }

    public void remove(Tag toRemove) {
        requireNonNull(toRemove);
        if (!internalList.remove(toRemove)) {
            throw new TagNotFoundException();
        }
    }

    /**
     * Returns the backing list as an unmodifiable {@code ObservableList}.
     */
    public ObservableList<Tag> asUnmodifiableObservableList() {
        return internalUnmodifiableList;
    }

    @Override
    public String toString() {
        return internalList.stream()
                .map(Tag::toString)
                .collect(Collectors.joining(", "));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (!(other instanceof UniqueTagList)) {
            return false;
        } else {
            UniqueTagList otherList = (UniqueTagList) other;
            return internalList.equals(otherList.internalList);
        }
    }
}
