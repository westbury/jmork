package mozilla.thunderbird;

import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

public class AddressBookTest extends TestCase {

	public void testNullInputStream() throws Exception {
		try {
			new AddressBook((InputStream)null);
			fail("Exception expected");
		} catch (Exception expected) {
		}
	}
	
	public void testRows() throws Exception {
		AddressBook addressBook = new AddressBook(getClass().getResourceAsStream("/simple.mab"));
	}
	
	public void testUrlInGroup() throws Exception {
		AddressBook addressBook = new AddressBook(getClass().getResourceAsStream("/abook_urlingroup.mab"));
	}
	
    public void testAddressBookReaderUmlauts() throws Exception {
       AddressBook addressBook = new AddressBook(getClass().getResourceAsStream("/abook_umlauts.mab"));
       
       List<Address> addresses = addressBook.getAddresses();
       assertEquals(1,addresses.size());
       
     Address address = addresses.get(0);
     // öäüß
     assertEquals("\u00F6\u00E4\u00FC\u00DF",address.getFirstName());
   }	
	
	public void testAddressBookReader() throws Exception {
		AddressBook addressBook = new AddressBook(getClass().getResourceAsStream("/abook_single.mab"));
		
		List<Address> addresses = addressBook.getAddresses();
		assertEquals(1,addresses.size());
		
		Address address = addresses.get(0);
		assertEquals("mike.haller@smartwerkz.com",address.getPrimaryEmail());
		assertEquals("Mike",address.getFirstName());
		assertEquals("Haller",address.getLastName());
		assertEquals("Mike Haller",address.getDisplayName());
	}
	
	public void testAddressBookNoAtomDatabaseFound() throws Exception {
		AddressBook addressBook = new AddressBook(getClass().getResourceAsStream("/abook_noatomdb.mab"));
	}

}
