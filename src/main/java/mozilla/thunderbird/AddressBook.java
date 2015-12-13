package mozilla.thunderbird;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import mork.ExceptionHandler;
import mork.MorkDocument;
import mork.Row;
import mork.Table;

/**
 * An address book is a container for addresses loaded from a Mozilla
 * Thunderbird address book, which is stored in the Mork file format.
 * 
 * @author mhaller
 */
public class AddressBook {

	/** Internal container for Addresses */
	private final List<Address> addresses = new LinkedList<Address>();
	private ExceptionHandler exceptionHandler;

	private MorkDocument morkDocument;
	
	/**
	 * Constructs an empty address book.
	 * <P>
	 * The address book is mutable.  Addresses may be added to this address
	 * book and it can be saved.
	 * 
	 */
	public AddressBook() {
		morkDocument = new MorkDocument();
	}
	
	/**
	 * Constructs an address book by reading data in Mork format from the
	 * given input stream.
	 * <P>
	 * The address book is mutable.  Addresses may be added, removed, or edited
	 * and this address book can be written back out to an output stream.
	 * 
	 */
	public AddressBook(InputStream inputStream) {
		load(inputStream);
	}

	/**
	 * Constructs an address book by reading data in Mork format from the
	 * given input stream.
	 * <P>
	 * The address book is mutable.  Addresses may be added, removed, or edited
	 * and this address book can be written back out to an output stream.
	 * <P>
	 * This form of the constructor allows an exception handler to be passed.
	 * This allows parsing to continue so all parse exceptions can be obtained.
	 */
	public AddressBook(InputStream inputStream, ExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
		load(inputStream);
	}
	
	public AddressBook(AddressBook sourceAddressBook) {
		// TODO deep copy addresses
		morkDocument = new MorkDocument(sourceAddressBook.morkDocument);
	}

	/**
	 * Loads a Mork database from the given input and parses it as being a
	 * Mozilla Thunderbird Address Book. The file is usually called abook.mab
	 * and is located in the Thunderbird user profile.
	 * 
	 * If additional address books are loaded into the same Address Book
	 * instance, the addresses get collected into the same address book.
	 * 
	 * @param inputStream
	 *            the stream to load the address book from.
	 */
	private void load(final InputStream inputStream) {
		if (inputStream == null) {
			throw new IllegalArgumentException("InputStream must not be null");
		}
		morkDocument = new MorkDocument(
				new InputStreamReader(inputStream), exceptionHandler);
		for (Row row : morkDocument.getRows()) {
			final Address address = new Address(row);
			addresses.add(address);
		}
		for (Table table : morkDocument.getTables()) {
			for (Row row : table.getRows()) {
				if (row.getValue("DisplayName") != null) {
					final Address address = new Address(row);
					addresses.add(address);
				}
			}
		}
	}

	/**
	 * Returns an unmodifiable list of {@link Address}es.
	 * 
	 * @return an unmodifiable list of {@link Address}es, might be empty, never
	 *         null.
	 */
	public List<Address> getAddresses() {
		return Collections.unmodifiableList(addresses);
	}

	public MorkDocument getMorkDocument() {
		return morkDocument;
	}

	/**
	 * Creates a new address in this address book.  The address is created
	 * empty, i.e. no properties are set.
	 * 
	 * @return an address with no properties set
	 */
	public Address createAddress() {
		Row row = morkDocument.createRow();
		Address address = new Address(row);
		addresses.add(address);
		return address;
	}

	public void deleteAddress(Address address) {
		boolean found = addresses.remove(address);
		if (!found) {
			throw new IllegalArgumentException("Address not in address book: " + address);
		}
		
		morkDocument.deleteRow(address.row);
	}

}
