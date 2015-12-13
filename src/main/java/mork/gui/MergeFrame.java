package mork.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import mork.MorkWriter;
import mozilla.thunderbird.Address;
import mozilla.thunderbird.AddressBook;
import mozilla.thunderbird.AddressComparator;

public class MergeFrame extends JInternalFrame {

	private static final long serialVersionUID = 1L;
	
	private AddressBookControl baseBookPanel;
	private AddressBookControl bookPanel1;
	private AddressBookControl bookPanel2;

	private JList<Address> mergedListComponent;

	private JScrollPane scollableMergedListPanel;

	private AddressBook mergedBook = null;


	MergeFrame(String title, List<Throwable> modelExceptions) {
		super(title, true, true, true, true);
		setVisible(true);
		setSize(420, 340);

		JMorkProperties properties = new JMorkProperties();
		
		baseBookPanel = new AddressBookControl("baseFile", modelExceptions, properties);
		bookPanel1 = new AddressBookControl("file1", modelExceptions, properties);
		bookPanel2 = new AddressBookControl("file2", modelExceptions, properties);
		
		mergedListComponent = new JList<Address>();
		mergedListComponent.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index,
						isSelected, cellHasFocus);
				if (value instanceof Throwable) {
					Throwable throwable = (Throwable) value;
					setText("ERROR: " + throwable.getMessage());
				} else if (value instanceof Address) {
					Address address = (Address) value;
					String msg = String.format("%s, %s (%s) <%s>", address
							.getLastName(), address.getFirstName(), address
							.getDisplayName(), address.getPrimaryEmail());
					setText(msg);
				}
				return this;
			}
		});

		scollableMergedListPanel = new JScrollPane();
		scollableMergedListPanel.setViewportView(mergedListComponent);

		add(createThreeUpPanel(), BorderLayout.CENTER);
		add(createButtonLinePanel(), BorderLayout.SOUTH);

	    pack();
	}

	private JComponent createThreeUpPanel() {
		JComponent panel = new JPanel();
		
		GridLayout layout = new GridLayout(3,1);
		layout.setVgap(10);
		panel.setLayout(layout);

		panel.setBorder(new EmptyBorder (15, 15, 15, 15));

		panel.add(baseBookPanel);
		panel.add(createTwoVersionsAcrossPanel());
		panel.add(scollableMergedListPanel);

		return panel;
	}
	
	private JComponent createTwoVersionsAcrossPanel() {
		JComponent panel = new JPanel();

		GridLayout layout = new GridLayout(1,2);
		layout.setHgap(10);
		panel.setLayout(layout);

		panel.add(bookPanel1);
		panel.add(bookPanel2);

		return panel;
	}
	
	private JComponent createButtonLinePanel() {
		JComponent panel = new JPanel();
		panel.setLayout(new BorderLayout());

		panel.setBorder(new EmptyBorder (15, 15, 15, 15));

		panel.add(createButtonPanel(), BorderLayout.EAST);

		return panel;
	}
	
	private JComponent createButtonPanel() {
		JComponent panel = new JPanel();
		panel.setLayout(new GridLayout(1,0));
		
		JButton mergeButton = new JButton();
		mergeButton.setText("Do Merge");
		mergeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				mergedBook = doMerge(baseBookPanel.getBook(), bookPanel1.getBook(), bookPanel2.getBook());

				List<Address> addresses = new ArrayList<Address>(mergedBook.getAddresses());
				Collections.sort(addresses, new AddressComparator());

				mergedListComponent.setListData(addresses.toArray(new Address[0]));
			}
		});
		
		JButton outputButton = new JButton();
		outputButton.setText("Write to File...");
		outputButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				writeBook(mergedBook);
			}
		});

		panel.add(mergeButton);
		panel.add(outputButton);

		return panel;
	}
	
	protected void writeBook(AddressBook book) {
		MorkWriter writer = new MorkWriter(book.getMorkDocument());
		
		JFileChooser chooser = OpenAction.createFileChooserDialog();
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println("You chose to write to this file: "
					+ chooser.getSelectedFile().getName());
			File morkFile = chooser.getSelectedFile();
			writer.write(morkFile);
		}
	}

	/**
	 * Iterate through the properties applying all changes. If the same property
	 * has been changed in both versions and the new values are different then
	 * we apply the following rules:
	 * <UL>
	 * <LI>If one of the new values is null, use the non-null value.
	 * <LI>If the values are strings, and one of the strings is a subset of the
	 * other string, use the longer string. However this rule shall not apply to
	 * e-mail addresses because two e-mail addresses are different even if one
	 * is a sub-string of the other.
	 * <LI>If the property is a free-form property, e.g. notes, then put all three versions
	 * of the text with conflict markers.
	 * </UL>
	 * 
	 * If there is still a conflict then we create two entries. If the conflict
	 * occurs in a multi-valued property (e.g. e-mail address) then create a
	 * single address with both e-mail addresses. Otherwise duplicate the
	 * addresses.  If we duplicate then we still keep the merged values of any other properties, so the
	 * only differences between the duplicated addresses will be in the properties that
	 * conflicted.
	 * 
		 * <UL>
		 * <LI>If any address has properties that are a subset of the properties of an address
		 * in the other book, remove the address from our list.
		 * </UL>
	 * 
	 */
	protected AddressBook doMerge(AddressBook addressBookBase, AddressBook book1, AddressBook book2) {
		AddressBook mergedResult = new AddressBook();

		Map<String, Address> addressesMap1 = new HashMap<String, Address>();
		for (Address address1 : book1.getAddresses()) {
			addressesMap1.put(address1.getDisplayName(), address1);
		}

		Map<String, Address> addressesMap2 = new HashMap<String, Address>();
		for (Address address2 : book2.getAddresses()) {
			addressesMap2.put(address2.getDisplayName(), address2);
		}

		for (Address addressBase : addressBookBase.getAddresses()) {
			if (addressesMap1.containsKey(addressBase.getDisplayName())) {
				Address address1 = addressesMap1.remove(addressBase.getDisplayName());
				if (addressesMap2.containsKey(addressBase.getDisplayName())) {
					Address address2 = addressesMap2.remove(addressBase.getDisplayName());
					try {
						mergeAddressesIntoAddressBook(mergedResult, addressBase, address1, address2);
					} catch (UnmergeableDataException e) {
						// There are merge conflicts or other problems that can't be resolved.
						// Write both versions of the address to the merged address book.
						copyAddressIntoAddressBook(address1, mergedResult);
						copyAddressIntoAddressBook(address2, mergedResult);
					}
				} else {
					/*
					 * It's been deleted from book 2. If there are any
					 * 'significant' changes in book 1 then we include that
					 * address. This may or may not be what the user wants, but
					 * it is a lot easier for the user to delete the address
					 * again if the user wants the address deleted.
					 */
					if (significantChanges(addressBase, address1)) {
						copyAddressIntoAddressBook(address1, mergedResult);
					}					
				}
			} else {
				if (addressesMap2.containsKey(addressBase.getDisplayName())) {
					Address address2 = addressesMap2.remove(addressBase.getDisplayName());
					/*
					 * It's been deleted from book 1. If there are any
					 * 'significant' changes in book 2 then we include that
					 * address. This may or may not be what the user wants, but
					 * it is a lot easier for the user to delete the address
					 * again if the user wants the address deleted.
					 */
					if (significantChanges(addressBase, address2)) {
						copyAddressIntoAddressBook(address2, mergedResult);
					}					
				} else {
					// It's been deleted from both.
					// Nothing to do.
				}
			}
		}

		
		/*
		 * Now process the addresses that are deemed to be new.  That is, they did not
		 * exist in the ancestor book.
		 * 
		 * If any address has properties that are a subset of the properties of another address,
		 * we remove the address with the lesser amount of information.
		 * 
		 * Note that we also compare against addresses in the same address book.  One might think
		 * this is not necessary, indeed wrong.  However if we did not do that then we would have
		 * less predictability.  Consider book 1 with addresses A and C, and book 2 with address B.
		 * If A < B < C  (where < indicates subset) then we may or may not get left with A as well as
		 * C, depending on which subset was found first.
		 */
		
		List<Address> addresses = new ArrayList<Address>();
		addresses.addAll(addressesMap1.values());
		addresses.addAll(addressesMap2.values());
		for (Iterator<Address> iter = addresses.iterator(); iter.hasNext(); ) {
			Address address1 = iter.next();
			
			boolean superSetFound = false;
			for (Address address2 : addresses) {
				if (address1 != address2 && isSubsetOf(address1, address2)) {
					superSetFound = true;
					break;
				}
			}
			
			if (superSetFound) {
				iter.remove();
			}
		}
		
		/*
		 * Any addresses left, just add them.
		 */
		for (Address address : addresses) {
			copyAddressIntoAddressBook(address, mergedResult);
		}

		return mergedResult;
	}

	private boolean isSubsetOf(Address address1, Address address2) {
		for (String id : address1.getIds()) {
			String value1 = address1.get(id);
			String value2 = address2.get(id);
			if (!isSubstringOf(value1, value2)) {
				return false;
			};
		}
		return true;
	}

	private boolean significantChanges(Address addressBase, Address address2) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void copyAddressIntoAddressBook(Address sourceAddress,
			AddressBook addressBook) {
		Address destinationAddress = addressBook.createAddress();

		for (String id : sourceAddress.getIds()) {
			String value = sourceAddress.get(id);
			destinationAddress.put(id, value);
		}
	}

	private void mergeAddressesIntoAddressBook(AddressBook mergedAddressBook, Address addressBase,
			Address address1, Address address2) throws UnmergeableDataException {
		Address mergedAddress = mergedAddressBook.createAddress();

		Map<String, String> properties1 = new HashMap<>();
		for (String id : address1.getIds()) {
			properties1.put(id, address1.get(id));
		}

		Map<String, String> properties2 = new HashMap<>();
		for (String id : address2.getIds()) {
			properties2.put(id, address2.get(id));
		}

		Map<String, String> propertiesBase = new HashMap<>();
		for (String id : addressBase.getIds()) {
			propertiesBase.put(id, addressBase.get(id));
		}

		// Special processing for certain properties
		processLastModifiedDate(properties1, properties2, mergedAddress);
		processEmailAddresses(propertiesBase, properties1, properties2, mergedAddress);
		processWebPages(propertiesBase, properties1, properties2, mergedAddress);
		
		for (String id : addressBase.getIds()) {
			String baseValue = addressBase.get(id);
			if (properties1.containsKey(id)) {
				String value1 = properties1.remove(id);
				if (properties2.containsKey(id)) {
					String value2 = properties2.remove(id);
					String mergedValue = mergeValues(id, baseValue, value1, value2);
					mergedAddress.put(id, mergedValue);
				} else {
					/*
					 * 
					 * It's been deleted from book 2. If there are any
					 * 'significant' changes in book 1 then we include that
					 * address. This may or may not be what the user wants, but
					 * it is a lot easier for the user to delete the address
					 * again if the user wants the address deleted.
					 */
					if (significantChanges(baseValue, value1)) {
						mergedAddress.put(id, value1);
					}					
				}
			} else {
				if (properties2.containsKey(id)) {
					String value2 = properties2.remove(id);
					/*
					 * It's been deleted from book 1. If there are any
					 * 'significant' changes in book 2 then we include that
					 * address. This may or may not be what the user wants, but
					 * it is a lot easier for the user to delete the address
					 * again if the user wants the address deleted.
					 */
					if (significantChanges(baseValue, value2)) {
						mergedAddress.put(id, value2);
					}					
				} else {
					// It's been deleted from both.
					// Nothing to do.
				}
			}
		}


		/*
		 * Now process the properties that are deemed to be new.  That is, they did not
		 * exist in the ancestor address.
		 * 
		 * If any property value is a sub-string of the property value
		 * in the other address, use the longer property value.
		 */

		/*
		 * Any properties left, just add them.
		 */
		for (String id : properties1.keySet()) {
			String value1 = properties1.get(id);
			if (properties2.containsKey(id)) {
				// The property has been added as a new property in both versions.
				// If one is a sub-string of the other then we use the longer value,
				// otherwise we have a conflict that we resolve only by keeping two versions
				// of this address in the merged address book.

				String value2 = properties2.remove(id);

				if (isSubstringOf(value2, value1)) {
					// Use value1
					mergedAddress.put(id, value1);
				} else if (isSubstringOf(value1, value2)) {
					// Use value2
					mergedAddress.put(id, value2);
				} else {
					/*
					 * The values are in conflict. For properties that are
					 * free-form text, we can put both values in the property.
					 * For other properties, however, that might result in an
					 * invalid field. In the latter case, we fail to merge. This
					 * will require the caller to sort it out, generally by
					 * leaving both addresses in the merged address book as two
					 * different address records.
					 */
					switch (id) {
					case "JobTitle":
					case "Department":
					case "Company":
					case "SpouseName":
						mergedAddress.put(id, value1 + " / " + value2);
						break;
					default:
						throw new UnmergeableDataException();
					}
				}
			} else {
				// Property does not exist in address2, so easy, just use value from address1.
				mergedAddress.put(id, value1);
			}
		}

		/*
		 * Anything left in properties2 will be properties that did not exist in properties1,
		 * so we simply add those.
		 */
		for (String id : properties2.keySet()) {
			mergedAddress.put(id, properties2.get(id));
		}

	}

	/**
	 * The 'LastModifiedDate' property is a special case in that we will almost
	 * always get a conflict on this property because the address will have been
	 * last modified at different times on each machine.  We want to keep the
	 * latest date here.
	 * 
	 * @param properties1
	 * @param properties2
	 * @param mergedAddress
	 */
	private void processLastModifiedDate(Map<String, String> properties1,
			Map<String, String> properties2, Address mergedAddress) {
		String value1 = properties1.remove("LastModifiedDate");
		String value2 = properties2.remove("LastModifiedDate");
		
		if (value1 == null) {
			if (value2 != null) {
				mergedAddress.put("LastModifiedDate", value2);
			}
		} else {
			if (value2 == null) {
				mergedAddress.put("LastModifiedDate", value1);
			} else {
				// Use whichever version has the latest date
				if (value1.compareToIgnoreCase(value2) > 0) {
					mergedAddress.put("LastModifiedDate", value1);
				} else {
					mergedAddress.put("LastModifiedDate", value2);
				}
			}
		}
	}

	enum EmailAddressType {
		/** address was only found as primary e-mail. */
		primary, 
		/** address was only found as second e-mail. */
		second,
		/** address was primary in one version and second in the other version. */
		both
	}
	
	class EmailPair {
		
		EmailPair(String id, Map<String, String> properties, EmailAddressType type) throws UnmergeableDataException {
			email = properties.remove(id);
			lowercaseEmail = properties.remove("Lowercase" + id);
			
			this.type = type;
			
			if (email != null && lowercaseEmail != null && !email.toLowerCase().equals(lowercaseEmail)) {
				throw new UnmergeableDataException();
			}
		}
		
		String email;
		String lowercaseEmail;
		public EmailAddressType type;
		public void putEmailAddress(Address mergedAddress, String id) {
			if (email != null) {
				mergedAddress.put(id,  email);
			}
			if (lowercaseEmail != null) {
				mergedAddress.put("Lowercase" + id,  email);
			}
		}
		public String getKey() {
			return lowercaseEmail != null ? lowercaseEmail
					: email != null ? email.toLowerCase()
							: "";   // Is this empty string correct?
		}
		public void mergeIn(EmailPair otherEmail) {
			if (type != otherEmail.type) {
				type = EmailAddressType.both;
			}
			if (otherEmail.email != null) {
				email = otherEmail.email; 
			}
			if (otherEmail.lowercaseEmail != null) {
				lowercaseEmail = otherEmail.lowercaseEmail; 
			}
		}
	}
	
	/**
	 * Processing of e-mail addresses is a special case here because if there
	 * are conflicting primary e-mail addresses and no secondary e-mail addresses
	 * then we can move one of the e-mail addresses into the secondary e-mail address
	 * property.
	 * <P>
	 * It is also a special case because every e-mail is actually stored in two properties,
	 * 'PrimaryEmail' and 'LowercasePrimaryEmail', or in 'SecondEmail' and 'LowercaseSecondEmail'.
	 * We must ensure that there is no way that a merged file can end up containing a non-null 
	 * 'LowercasePrimaryEmail' property that is not in fact a lower case version of a non-null
	 * 'PrimaryEmail' property.
	 * 
	 * @param properties1
	 * @param properties2
	 * @param mergedAddress
	 * @throws UnmergeableDataException 
	 */
	private void processEmailAddresses(Map<String, String> addressBase, Map<String, String> properties1,
			Map<String, String> properties2, Address mergedAddress) throws UnmergeableDataException {
		
		// First figure out how many distinct e-mail addresses we have
		
		
		EmailPair primaryEmail1 = new EmailPair("PrimaryEmail", properties1, EmailAddressType.primary);
		EmailPair secondEmail1 = new EmailPair("SecondEmail", properties1, EmailAddressType.second);
		EmailPair primaryEmail2 = new EmailPair("PrimaryEmail", properties2, EmailAddressType.primary);
		EmailPair secondEmail2 = new EmailPair("SecondEmail", properties2, EmailAddressType.second);
		EmailPair primaryEmailBase = new EmailPair("PrimaryEmail", addressBase, null);
		EmailPair secondEmailBase = new EmailPair("SecondEmail", addressBase, null);

		// Any e-mail in base but not in either primary or second for at least one of the
		// versions is removed.
		for (EmailPair emailBase : new EmailPair [] { primaryEmailBase, secondEmailBase }) {
			if (emailBase != null) {
				if (!emailBase.equals(primaryEmail1)
						&& !emailBase.equals(secondEmail1)) {
					// This e-mail address was removed in version 1
					// So remove from version 2
					if (emailBase.equals(primaryEmail2)) {
						primaryEmail2 = null;
					}
					if (emailBase.equals(secondEmail2)) {
						secondEmail2 = null;
					}
				} else if (!emailBase.equals(primaryEmail2)
						&& !emailBase.equals(secondEmail2)) {
					// This e-mail address was removed in version 2
					// So remove from version 1
					if (emailBase.equals(primaryEmail1)) {
						primaryEmail1 = null;
					}
					if (emailBase.equals(secondEmail1)) {
						secondEmail1 = null;
					}
				}
			}
		}
		
		Map<String, EmailPair> allEmailAddresses = new HashMap<>();
		addEmailAddress(allEmailAddresses, primaryEmail1);
		addEmailAddress(allEmailAddresses, secondEmail1);
		addEmailAddress(allEmailAddresses, primaryEmail2);
		addEmailAddress(allEmailAddresses, secondEmail2);
		
		if (allEmailAddresses.size() > 2) {
			// More than two distinct e-mail addresses remain.
			// We can't store more than two.
			throw new UnmergeableDataException();
		}
		
		EmailPair[] emails = allEmailAddresses.values().toArray(new EmailPair[0]);
		
		switch (emails[0].type) {
				case primary:
					emails[0].putEmailAddress(mergedAddress, "PrimaryEmail");
					if (emails[1] != null) {
						emails[1].putEmailAddress(mergedAddress, "SecondEmail");
					}
					break;
				case second:
					emails[0].putEmailAddress(mergedAddress, "SecondEmail");
					if (emails[1] != null) {
						emails[1].putEmailAddress(mergedAddress, "PrimaryEmail");
					}
					break;
				case both:
					if (emails[1] != null && emails[1].type == EmailAddressType.primary) {
						emails[1].putEmailAddress(mergedAddress, "PrimaryEmail");
						emails[0].putEmailAddress(mergedAddress, "SecondEmail");
					} else {
						emails[0].putEmailAddress(mergedAddress, "PrimaryEmail");
						if (emails[1] != null) {
							emails[1].putEmailAddress(mergedAddress, "SecondEmail");
						}
					}
		}

	}

	private void addEmailAddress(Map<String, EmailPair> allEmailAddresses,
			EmailPair email) {
		if (email  != null) {
			EmailPair existing = allEmailAddresses.get(email.getKey());
			if (existing == null) {
				allEmailAddresses.put(email.getKey(), email);
			} else {
				existing.mergeIn(email);
			}
		}
	}

	/**
	 * Processing of e-mail addresses is a special case here because if there
	 * are conflicting primary e-mail addresses and no secondary e-mail addresses
	 * then we can move one of the e-mail addresses into the secondary e-mail address
	 * property.
	 * <P>
	 * It is also a special case because every e-mail is actually stored in two properties,
	 * 'PrimaryEmail' and 'LowercasePrimaryEmail', or in 'SecondEmail' and 'LowercaseSecondEmail'.
	 * We must ensure that there is no way that a merged file can end up containing a non-null 
	 * 'LowercasePrimaryEmail' property that is not in fact a lower case version of a non-null
	 * 'PrimaryEmail' property.
	 * 
	 * @param properties1
	 * @param properties2
	 * @param mergedAddress
	 * @throws UnmergeableDataException 
	 */
	private void processWebPages(Map<String, String> addressBase, Map<String, String> properties1,
			Map<String, String> properties2, Address mergedAddress) throws UnmergeableDataException {

//		String value1 = properties1.remove("WebPage1");
//		String value2 = properties2.remove("WebPage2");
//	
//		TODO
	}
	
	private String mergeValues(String id, String baseValue, String value1, String value2) {
		if (value1.equals(baseValue)) {
			return value2;
		}
		if (value2.equals(baseValue)) {
			return value1;
		}
		
		// TODO we can't really do this.  Might end with invalid data.
		return value1 + " / " + value2;
	}

	private boolean isSubstringOf(String value1, String value2) {
		return value2.contains(value1.trim());
	}

	private boolean significantChanges(String baseValue, String value2) {
		return !baseValue.trim().equalsIgnoreCase(value2.trim());
	}

}
