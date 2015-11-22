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
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
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
				mergedBook = doMerge(baseBookPanel.getBook(), bookPanel1.getBook(), bookPanel1.getBook());

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
		writer.write(new File("c:\\out.mab"));
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
		final List<Address> results = new ArrayList<Address>();

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
					mergeAddressesIntoAddressBook(mergedResult, addressBase, address1, address2);
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
		 * If any address has properties that are a subset of the properties of an address
		 * in the other book, remove the address from our list.  Note that we copy the list
		 * of addresses first.  This must be done because we are removing addresses from the maps
		 * as we go, so we can't iterate the maps.  However another reason for doing this is
		 * that is makes the process commutative on the address books and more predictable because
		 * both address books are compared against the original list from the other address book.
		 *
		 */
		
		List<Address> addresses1 = new ArrayList<Address>(addressesMap1.values());
		List<Address> addresses2 = new ArrayList<Address>(addressesMap2.values());
		removeSubsetAddresses(addressesMap1, addresses1, addresses2);
		removeSubsetAddresses(addressesMap2, addresses2, addresses1);
		
		/*
		 * Any addresses left, just add them.
		 */
		for (Address address1 : addressesMap1.values()) {
			copyAddressIntoAddressBook(address1, mergedResult);
		}
		for (Address address2 : addressesMap2.values()) {
			copyAddressIntoAddressBook(address2, mergedResult);
		}

		return mergedResult;
	}

	public void removeSubsetAddresses(Map<String, Address> addressesMap1,
			List<Address> addresses1, List<Address> addresses2) {
		for (Address address1 : addresses1) {
			for (Address address2 : addresses2) {
				if (isSubsetOf(address1, address2)) {
					addressesMap1.remove(address1.getDisplayName());
				}
			}
		}
	}

	private boolean isSubsetOf(Address address1, Address address2) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean significantChanges(Address addressBase, Address address2) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void copyAddressIntoAddressBook(Address sourceAddress,
			AddressBook addressBook) {
		Address destinationAddress = addressBook.createNewAddress();

		for (String id : sourceAddress.getIds()) {
			String value = sourceAddress.get(id);
			destinationAddress.put(id, value);
		}
	}

	private void mergeAddressesIntoAddressBook(AddressBook mergedAddressBook, Address addressBase,
			Address address1, Address address2) {
		Address mergedAddress = mergedAddressBook.createNewAddress();

		// TODO

	}

}
