package mork.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import mork.ExceptionHandler;
import mozilla.thunderbird.Address;
import mozilla.thunderbird.AddressBook;
import mozilla.thunderbird.AddressComparator;

public class AddressBookControl extends JComponent {

	private static final long serialVersionUID = 1L;
	private JLabel filenameLabel;
	private JList<Address> list;
	private AddressBook book = null;

	public AddressBookControl(final String propertyKey, List<Throwable> modelExceptions, final JMorkProperties properties) {
		
		list = new JList<Address>();
		list.setCellRenderer(new DefaultListCellRenderer() {
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

		this.setLayout(new BorderLayout());


		JScrollPane scrollingPane = new JScrollPane();
		scrollingPane.setViewportView(list);

		JComponent topLine = new JPanel();
		topLine.setLayout(new BorderLayout());
		
		filenameLabel = new JLabel();
		topLine.add(filenameLabel, BorderLayout.CENTER);
		JButton fileChoiceButton = new JButton();
		topLine.add(fileChoiceButton, BorderLayout.EAST);
		fileChoiceButton.setText("Browse");
		fileChoiceButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				JFileChooser chooser = OpenAction.createFileChooserDialog();
				int returnVal = chooser.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					System.out.println("You chose to open this file: "
							+ chooser.getSelectedFile().getName());
					File morkFile = chooser.getSelectedFile();
					showBookInUi(morkFile);
					try {
						properties.putProperty(propertyKey, morkFile.getCanonicalPath());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		});

		String filename = properties.getProperty(propertyKey);
		if (filename != null) {
			File morkFile = new File(filename);
			showBookInUi(morkFile);
		}

		add(topLine, BorderLayout.NORTH);
		add(scrollingPane, BorderLayout.CENTER);

		setBorder(BorderFactory.createLineBorder(Color.black));
		
		list.revalidate();
	}

	public void showBookInUi(File morkFile) {
		try {
			filenameLabel.setText(morkFile.getCanonicalPath());

			final List<Throwable> modelExceptions = new ArrayList<Throwable>();
			book = openBook(morkFile, modelExceptions);

			List<Address> addresses = new ArrayList<Address>(book.getAddresses());
			Collections.sort(addresses, new AddressComparator());
			list.setListData(addresses.toArray(new Address[0]));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private AddressBook openBook(File morkFile,
			final List<Throwable> modelExceptions) throws FileNotFoundException {
		System.out.println("Reading " + morkFile.getAbsolutePath());

		AddressBook book = new AddressBook();
		book.setExceptionHandler(new ExceptionHandler() {
			public void handle(Throwable t) {
				modelExceptions.add(t);
				// Does not rethrow, so parsing continues
			}
		});
		book.load(new FileInputStream(morkFile));
		return book;
	}

	public AddressBook getBook() {
		return book;
	}

}
