package mozilla.thunderbird;

import java.util.List;

import junit.framework.TestCase;

public class AddressBookMergeTest extends TestCase {

	public void testEmailMerge() throws Exception {
		AddressBook addressBookBase = new AddressBook();
		Address addressBase = addressBookBase.createAddress();
		addressBase.setLastName("Smith");
		addressBase.put("PrimaryEmail", "smith@acme.com");
		
		AddressBook addressBook1 = new AddressBook(addressBookBase);
		Address address1 = addressBook1.getAddresses().get(0);
		address1.put("PrimaryEmail", "smith@bar.com");

		AddressBook addressBook2 = new AddressBook(addressBookBase);
		Address address2 = addressBook2.getAddresses().get(0);
		address2.put("PrimaryEmail", "smith@foo.com");

		AddressBook mergedAddresses = new AddressBook();
		// TODO refactor so this method is available to us
//		mergeAddressesIntoAddressBook(mergedAddresses, address1, address2, addressBase);		
	
		List<Address> addresses = mergedAddresses.getAddresses();
	    assertEquals(1,addresses.size());
	    Address address = mergedAddresses.getAddresses().get(0);
	    assertEquals("smith@bar.com",address.get("PrimaryEmail"));
	    assertEquals("smith@foo.com",address.get("SecondEmail"));
	}
}
