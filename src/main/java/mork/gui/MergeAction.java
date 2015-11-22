package mork.gui;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import mork.MorkWriter;

public class MergeAction extends AbstractAction {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;
	private final Controller controller;

	public MergeAction(Controller controller) {
		super("Merge...");
		this.controller = controller;
	}

	public void actionPerformed(ActionEvent e) {
		controller.openFilesForMerge();
//		ProfileLocator locator = new ProfileLocator();
//		String result = locator.locateFirstThunderbirdAddressbookPath();
//
//		JFileChooser chooser = new JFileChooser();
//		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
//
//		if (result != null) {
//			// We found a Thunderbird profile in the user app data
//			chooser.setCurrentDirectory(new File(result));
//		}
//
//		FileFilter filter = new FileFilter() {
//			@Override
//			public boolean accept(File f) {
//				if (f.isDirectory())
//					return true;
//				return f.getName().endsWith("mab");
//			}
//
//			@Override
//			public String getDescription() {
//				return "Mozilla Addressbook (*.mab)";
//			}
//		};
//		chooser.setFileFilter(filter);
//		chooser.setFileHidingEnabled(false);
//		
//		int returnVal1 = chooser.showOpenDialog(null);
//		if (returnVal1 == JFileChooser.APPROVE_OPTION) {
//			File file1 = chooser.getSelectedFile();
//			System.out.println("You chose to open this file: "
//					+ file1.getName());
//			int returnVal2 = chooser.showOpenDialog(null);
//			if (returnVal2 == JFileChooser.APPROVE_OPTION) {
//				File file2 = chooser.getSelectedFile();
//				System.out.println("You chose to open this file: "
//						+ file2.getName());
//				controller.openFilesForMerge(file1, file2);
//			}
//		}
	}

}
