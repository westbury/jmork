package mork.gui;

/**
 * A checked exception that is throw if the merger finds data
 * in an address that prevents addresses from being merged.
 * <P>
 * The typical way of handling this exception is to create two
 * addresses in the merged address book.
 * 
 * @author Nigel Westbury
 */
public class UnmergeableDataException extends Exception {

	private static final long serialVersionUID = 1L;

}
