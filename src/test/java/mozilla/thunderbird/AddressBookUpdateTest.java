package mozilla.thunderbird;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;
import mork.MorkWriter;

public class AddressBookUpdateTest extends TestCase {

	public void testUpdate() throws Exception {
		AddressBook addressBook = new AddressBook(getClass().getResourceAsStream("/simple.mab"));

		List<Address> addresses = addressBook.getAddresses();
	    assertEquals(1,addresses.size());
	       
	    Address address = addresses.get(0);
	    address.setLastName("Smith");
	    assertEquals("Smith",address.getLastName());
	}
	
	public void testReadWriteRead() throws Exception {
		AddressBook addressBook = new AddressBook(getClass().getResourceAsStream("/simple.mab"));

		List<Address> addresses = addressBook.getAddresses();
	    assertEquals(1,addresses.size());
	       
	    AddressBook addressBook2 = writeAndReadBack(addressBook);
		
		List<Address> addresses2 = addressBook2.getAddresses();
	    assertEquals(1,addresses2.size());
	       
	}
	
	public void testUpdateWriteAndRead() throws Exception {
		AddressBook addressBook = new AddressBook(getClass().getResourceAsStream("/simple.mab"));

		List<Address> addresses = addressBook.getAddresses();
	    assertEquals(1,addresses.size());
	       
	    Address address = addresses.get(0);
	    address.setLastName("Smith");
	    assertEquals("Smith",address.getLastName());

	    AddressBook addressBook2 = writeAndReadBack(addressBook);
		
		// Check the update is still there
		
		List<Address> addresses2 = addressBook2.getAddresses();
	    assertEquals(1,addresses2.size());
	       
	    Address address2 = addresses2.get(0);
	    assertEquals("Smith",address2.getLastName());
	}

	public void testSetToNull() throws Exception {
		AddressBook addressBook = new AddressBook(getClass().getResourceAsStream("/simple.mab"));

		List<Address> addresses = addressBook.getAddresses();
	    assertEquals(1, addresses.size());
	       
	    Address address = addresses.get(0);
	    assertEquals("Hackworth", address.get("sn"));
	    address.put("sn", null);   
	    	    
	    AddressBook addressBook2 = writeAndReadBack(addressBook);
		
		// Check the update is still there
		
		List<Address> addresses2 = addressBook2.getAddresses();
	    assertEquals(1, addresses2.size());
	       
	    Address address1 = addresses2.get(0);
	    assertEquals(null, address1.get("sn"));
	}

	public void testInsertAddress() throws Exception {
		AddressBook addressBook = new AddressBook(getClass().getResourceAsStream("/simple.mab"));

		List<Address> addresses = addressBook.getAddresses();
	    assertEquals(1,addresses.size());
	       
	    Address address = addressBook.createAddress();
	    address.setLastName("Clover");

	    AddressBook addressBook2 = writeAndReadBack(addressBook);
		
		// Check the update is still there
		
		List<Address> addresses2 = addressBook2.getAddresses();
	    assertEquals(2,addresses2.size());
	       
	    Address address1 = addresses2.get(0);
	    assertEquals(null,address1.getLastName());
	       
	    Address address2 = addresses2.get(1);
	    assertEquals("Clover",address2.getLastName());

	}

	public void testDeleteAddress() throws Exception {
		AddressBook addressBook = new AddressBook(getClass().getResourceAsStream("/abook_JMORK-3.mab"));

		List<Address> addresses = addressBook.getAddresses();
	    assertEquals(409,addresses.size());

	    addressBook.deleteAddress(addresses.get(1));
	    
	    AddressBook addressBook2 = writeAndReadBack(addressBook);
		
		// Check the address has gone
		
		List<Address> addresses2 = addressBook2.getAddresses();
	    assertEquals(408,addresses2.size());
	       
	    // And the other addresses are unaffected
	    

	}

	public void testNewAddressBook() throws Exception {
		AddressBook addressBook = new AddressBook();

		List<Address> addresses = addressBook.getAddresses();
	    assertEquals(0, addresses.size());
	       
	    Address address = addressBook.createAddress();
	    address.setLastName("Clover");

	    AddressBook addressBook2 = writeAndReadBack(addressBook);
		
		// Check the update is still there
		
		List<Address> addresses2 = addressBook2.getAddresses();
	    assertEquals(1, addresses2.size());
	       
	    Address address1 = addresses2.get(0);
	    assertEquals("Clover",address1.getLastName());

	}

	/**
	 * This is a test helper method that writes an address book and reads it back.
	 * This method is used to ensure updates to the address book have been fully done.
	 * 
	 * @param addressBook
	 * @return
	 * @throws IOException
	 */
	private AddressBook writeAndReadBack(AddressBook addressBook)
			throws IOException {
		// Write to stream
		ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		MorkWriter writer = new MorkWriter(addressBook.getMorkDocument());
		writer.write(ostream);
		
		System.out.println(ostream.toString());
		
		// Read back from stream
		InputStream reader = new ByteArrayInputStream(ostream.toByteArray());
		AddressBook addressBook2 = new AddressBook(reader);
		
		return addressBook2;
	}
	
}
