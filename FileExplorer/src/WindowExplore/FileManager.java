package WindowExplore;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import javax.swing.filechooser.FileSystemView;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Color;

public class FileManager {
	Thread t ;
	public JProgressBar bar;
	static String pathOfSearchFile = null;
	public static String nameOfSearchFile = null;
	public static final String APP_TITLE = "MY-FILE-EXPLORER"; // title of app
	private Desktop desktop; // create object desktop
	private FileSystemView fileSystemView; // create filesystemView
	@SuppressWarnings("unused")
	private File currentFile;
	private JPanel gui;
	private JTree tree;
	private DefaultTreeModel treeModel;
	private JTable table;
	private FileTableModel fileTableModel;
	private ListSelectionListener listSelectionListener;
	private boolean cellSizesSet = false;
	private int rowIconPadding = 6;
	private JTextField path;
	@SuppressWarnings("unused")
	private JPanel newFilePanel;
	@SuppressWarnings("unused")
	private JRadioButton newTypeFile;
	@SuppressWarnings("unused")
	private JTextField name;
	File file;
	JFileChooser fc;
	private JTextField tfSearch;
	List<String> filesListInDir = new ArrayList<String>();
	private File dirFrom;
	private File dirTo;
	private File back;
	private File next;
	private File previousPath;
	private String nameFile;
	private String curPath;
	private String prevPath[];
	private int i = 0;
	public Container getGui() {

		if (gui == null) {
			gui = new JPanel();
			gui.setBackground(Color.WHITE);
			gui.setMinimumSize(new Dimension(0, 0));
			gui.setBorder(new EmptyBorder(5, 5, 5, 5));

			fileSystemView = FileSystemView.getFileSystemView();
			desktop = Desktop.getDesktop();

			JPanel detailView = new JPanel();
			detailView.setBackground(Color.WHITE);
			detailView.setRequestFocusEnabled(false);
			detailView.setOpaque(false);
			
			detailView.setMinimumSize(new Dimension(600, 220));
			detailView.setIgnoreRepaint(true);
			detailView.setAutoscrolls(true);

			table = new JTable();
			table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			table.setAutoCreateRowSorter(true);
			table.setShowVerticalLines(false);
			listSelectionListener = new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent lse) {
					int row = table.getSelectionModel().getLeadSelectionIndex();
					setFileDetails(((FileTableModel) table.getModel()).getFile(row));
				}
			};
			table.getSelectionModel().addListSelectionListener(listSelectionListener);
			JScrollPane tableScroll = new JScrollPane(table);
			tableScroll.setBounds(0, 42, 746, 273);
			Dimension d = tableScroll.getPreferredSize();
			detailView.setLayout(null);

			JPanel fileMainDetails = new JPanel();
			fileMainDetails.setBackground(Color.WHITE);//37
			fileMainDetails.setBounds(0, 0, 746, 37);
			detailView.add(fileMainDetails);
			fileMainDetails.setBorder(new EmptyBorder(0, 6, 0, 6));
			fileMainDetails.setLayout(null);

			JPanel fileDetailsLabels = new JPanel();
			fileDetailsLabels.setBounds(6, 0, 0, 0);
			fileMainDetails.add(fileDetailsLabels);
			fileDetailsLabels.setLayout(null);

			JPanel fileDetailsValues = new JPanel();
			fileDetailsValues.setBackground(Color.WHITE);
			fileDetailsValues.setBounds(0, 2, 742, 35);
			fileMainDetails.add(fileDetailsValues);
			fileDetailsValues.setLayout(null);
			path = new JTextField(29);
			path.setBounds(28, 6, 424, 22);
			fileDetailsValues.add(path);
			path.setHorizontalAlignment(SwingConstants.LEFT);
			tfSearch = new JTextField();
			tfSearch.setToolTipText("Create new file/folder");
			tfSearch.setBounds(464, 6, 164, 22);
			tfSearch.setHorizontalAlignment(SwingConstants.LEFT);
			fileDetailsValues.add(tfSearch);
			tfSearch.setColumns(13);
			JButton btnSearch = new JButton("Search");
			btnSearch.addActionListener(new ActionListener() {	
				public void actionPerformed(ActionEvent arg0) {
					nameOfSearchFile = tfSearch.getText();
					File DirSearch = new File(path.getText());
					ListFile(DirSearch);
					if(pathOfSearchFile==null)
					{
						JOptionPane.showMessageDialog(fileDetailsValues, "File or directory is don't exist!!!");
					}
					else {
						File f = new File(pathOfSearchFile);
						try {
							if(f.isDirectory())
							{
								
								File f2 = new File(f.getParent());
								DefaultMutableTreeNode node = new DefaultMutableTreeNode(f2);
								showChildren(node);
							}
							else {
								File f1 = new File(f.getParent());
								DefaultMutableTreeNode node = new DefaultMutableTreeNode(f1);
								showChildren(node);
//								setSelectedFile(f1);
//								 final JList list = SwingUtils.getDescendantOfType(JList.class, this, "Enabled", true);
//								list.requestFocus();
							}
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
					
				
				}
			});
			btnSearch.setBounds(640, 5, 102, 25);
			fileDetailsValues.add(btnSearch);

			int count = fileDetailsLabels.getComponentCount();
			tableScroll.setPreferredSize(new Dimension((int) d.getWidth(), (int) d.getHeight() / 2));
			detailView.add(tableScroll);

			DefaultMutableTreeNode root = new DefaultMutableTreeNode();
			treeModel = new DefaultTreeModel(root);

			TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent tse) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) tse.getPath().getLastPathComponent();
					showChildren(node);
					setFileDetails((File) node.getUserObject());
				}
			};

			File[] roots = fileSystemView.getRoots();

			for (File fileSystemRoot : roots) {
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(fileSystemRoot);
				root.add(node);
				File[] files = fileSystemView.getFiles(fileSystemRoot, true);
				for (File file : files) {
					if (file.isDirectory()) {
						node.add(new DefaultMutableTreeNode(file));
					}
				}
			}
			tree = new JTree(treeModel);
			tree.setMinimumSize(new Dimension(70, 16));
			tree.setMaximumSize(new Dimension(70, 16));
			tree.setRootVisible(false);
			tree.addTreeSelectionListener(treeSelectionListener);
			tree.setCellRenderer(new FileTreeCellRenderer());
			tree.expandRow(0);
			JScrollPane treeScroll = new JScrollPane(tree);
			treeScroll.setMinimumSize(new Dimension(100, 27));

			tree.setVisibleRowCount(15);

			Dimension preferredSize = treeScroll.getPreferredSize();
			Dimension widePreferred = new Dimension(200, (int) preferredSize.getHeight());
			treeScroll.setPreferredSize(widePreferred);
			for (int ii = 0; ii < count; ii++) {
				fileDetailsLabels.getComponent(ii).setEnabled(false);
			}

			JPanel fileView = new JPanel();
			fileView.setBounds(0, 315, 746, 51);
			fileView.setLayout(null);

			detailView.add(fileView);

			JToolBar toolBar = new JToolBar();
			//toolBar.setBackground(Color.red);//746,48
			toolBar.setBounds(0, 10, 700, 40);
			fileView.add(toolBar);
			toolBar.setFloatable(false);
			
			JButton btnOpen = new JButton("Open");
			btnOpen.setBounds(0, 10, 500, 48);
			btnOpen.setMnemonic(KeyEvent.VK_O);
			btnOpen.setIcon(
					new ImageIcon(FileManager.class.getResource("/icons/open-file-icon.png")));
			//btnOpen.setBackground(Color.RED);
			btnOpen.setMaximumSize(new Dimension(100, 25));
			btnOpen.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					File open = new File(path.getText());
					try {
						if (open.isFile()) {
							openFile(open);
						} else if (open.isDirectory()) {
							DefaultMutableTreeNode node = new DefaultMutableTreeNode(open);
							showChildren(node);
						}

					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			});
			toolBar.add(btnOpen);
			
			JButton btnCopy = new JButton("Copy");
			btnCopy.setIcon(
					new ImageIcon(FileManager.class.getResource("/icons/copy-icon.png")));
			btnCopy.setMnemonic(KeyEvent.VK_C);
			//btnCopy.setBackground(Color.ORANGE);
			btnCopy.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// dir From file hien tai
					dirFrom = new File(path.getText());
					String name = path.getText();
					String[] words = name.split("\\\\");
					for (String w : words) {
						nameFile = w;
					}
					refresh();
				}
			});
			btnCopy.setMaximumSize(new Dimension(100, 25));
			toolBar.add(btnCopy);
			JButton btnPaste = new JButton("Paste");
			btnPaste.setIcon(
					new ImageIcon(FileManager.class.getResource("/icons/Paste-icon.png")));
			btnPaste.setMnemonic(KeyEvent.VK_V);
			//btnPaste.setBackground(Color.BLUE);
			btnPaste.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (dirFrom.isDirectory()) {
						dirTo = new File(path.getText() + "\\" + nameFile);
					    FileManager.copyFolder(dirFrom, dirTo);
					} else if (dirFrom.isFile()) {
						dirTo = new File(path.getText() + "\\" + nameFile);
						// System.out.println(dirTo);
						try {
							copyFile(dirFrom, dirTo);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					refresh();
					JOptionPane.showMessageDialog(fileView, "Successfully");
					
				}
			});
			btnPaste.setMaximumSize(new Dimension(100, 25));
			toolBar.add(btnPaste);
			
			
//			JButton btnSaveAs = new JButton("Cut");
//			btnSaveAs.setMaximumSize(new Dimension(100, 25));
//			toolBar.add(btnSaveAs);
//			btnSaveAs.addActionListener(new ActionListener() {
//				
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					// dir From file hien tai
//					dirFrom = new File(path.getText());
//					String name = path.getText();
//					String[] words = name.split("\\\\");
//					for (String w : words) {
//						nameFile = w;
//					}
//					if (dirFrom.isDirectory()) {
//						dirTo = new File(path.getText() + "\\" + nameFile);
//					    FileManager.copyFolder(dirFrom, dirTo);
//					} else if (dirFrom.isFile()) {
//						dirTo = new File(path.getText() + "\\" + nameFile);
//						// System.out.println(dirTo);
//						try {
//							copyFile(dirFrom, dirTo);
//							if(dirFrom.isDirectory())
//							{
//								deleteFolder(dirFrom);
//							}
//							else {
//								dirFrom.delete();
//							}
//							
//						} catch (IOException e1) {
//							e1.printStackTrace();
//						}
//						
//					}
//					
//					
//				}
//			});
			
			
			JButton btnDelete = new JButton("Delete");
			btnDelete.setIcon(
					new ImageIcon(FileManager.class.getResource("/icons/delete-file-icon.png")));
			btnDelete.setMnemonic(KeyEvent.VK_D);
			//btnDelete.setBackground(Color.red);
			btnDelete.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String pathDel;
					System.out.println("You clicked delete button");
					dirFrom = new File(path.getText());
					String name = path.getText();
					pathDel = name;
					pathDel = pathDel.replace("\\", "\\\\");
					System.out.println(dirFrom.getAbsolutePath());
					System.out.println(pathDel);
					//String[] words = name.split("\\\\");
					// current I have namefile now
					File del = new File(pathDel);
					if(del.isDirectory()) {
						deleteFolder(del);
					}
					else {
						del.delete();
					}
					refresh2();
					JOptionPane.showMessageDialog(fileMainDetails, "Delete successfully");
					// 
					
				}
			});
			
			
			btnDelete.setMaximumSize(new Dimension(100, 25));
			toolBar.add(btnDelete);
			
			JButton btnRefresh = new JButton("Refresh");
			btnRefresh.setIcon(
					new ImageIcon(FileManager.class.getResource("/icons/Button-Refresh-icon.png")));
			//btnRefresh.setBackground(Color.GREEN);
			btnRefresh.setMaximumSize(new Dimension(100, 25));
			toolBar.add(btnRefresh);
			btnRefresh.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					refresh();
					
				}
			});
			
			
			
			JButton btnRename = new JButton("Rename");
			btnRename.setIcon(
					new ImageIcon(FileManager.class.getResource("/icons/Rename-icon.png")));
			//btnRename.setBackground(Color.LIGHT_GRAY);
			btnRename.setMaximumSize(new Dimension(100, 25));
			toolBar.add(btnRename);
			btnRename.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					String inputName = JOptionPane.showInputDialog("Type the name you want to change");
					File f = new File(path.getText());
					if(f.isDirectory())
					{
					    File newDir = new File(f.getParent() + '\\' + inputName);
					    f.renameTo(newDir);
					}
					else {
						inputName = '\\' + inputName;
						//System.out.println(inputName);
						String path_file1 = path.getText();
						//System.out.println(path_file1);
						String path_file2 = getSourceFileReName(path_file1);
						//System.out.println(path_file2);
						String path_file3 = getTypeFile(path_file1);
						//System.out.println(path_file3);
						String result = path_file2 + inputName + path_file3;
						//System.out.println(result);
						File f_is_file = new File(result);
						f.renameTo(f_is_file);
					}			
					refresh();
				}
			});
			JButton btnCreateFile = new JButton("Create");
			btnCreateFile.setMaximumSize(new Dimension(100, 25));
			btnCreateFile.setIcon(
					new ImageIcon(FileManager.class.getResource("/icons/Create-New-icon.png")));
			toolBar.add(btnCreateFile);
			btnCreateFile.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					String result = "";
					JPanel panel = new JPanel();
			        panel.setBackground(Color.white);
			        panel.setSize(new Dimension(350, 32));
			        panel.setLayout(null);

			        JTextField text = new JTextField();
			        text.setForeground(Color.BLACK);
			        text.setHorizontalAlignment(SwingConstants.CENTER);
			        text.setFont(new Font("Arial", Font.BOLD, 11));
			        text.setBounds(0, 0, 250, 32);
			        panel.add(text);

			        UIManager.put("OptionPane.minimumSize",new Dimension(270, 120));

			        Object[] options = {".txt","folder",".docx",".xlsx",".pptx"};
			        Object l = JOptionPane.showInputDialog(null, panel, 
			                "Choosing type and input name", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			        String nameFile = text.getText();
			        String typeFile = l.toString();
			        String pathCurrent = path.getText();
			        
			        if(typeFile!="folder")
			        {
			        	result = nameFile+typeFile;
			        	
			        	String finalFileName = pathCurrent+"\\"+result;
				        finalFileName = finalFileName.replace("\\", "\\\\");
				        System.out.println(finalFileName);
				        File f = new File(finalFileName);
				        try {
							if(f.createNewFile())
							{
								JOptionPane.showMessageDialog(panel, "successfully");
							}
							else {
								JOptionPane.showMessageDialog(panel, "Failed");
								
							}
						} catch (HeadlessException e1) {
							
							e1.printStackTrace();
						} catch (IOException e1) {
							
							e1.printStackTrace();
						}
			        }
			        else {
			        	result = pathCurrent+"\\"+nameFile;
			        	File f = new File(result);
			        	try {
							if(f.mkdir())
							{
								JOptionPane.showMessageDialog(panel, "successfully");
							}
							else {
								JOptionPane.showMessageDialog(panel, "Failed");
								
							}
						} catch (HeadlessException e1) {
							
							e1.printStackTrace();
						}
			        	
			        }
			        
			        refresh();
			        panel.setVisible(true);				
				}
			});
			
			
			
			
			gui.repaint();
			gui.setLayout(null);

			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, detailView);
			splitPane.setBackground(Color.WHITE);

			JPanel panel = new JPanel();
			panel.setBackground(Color.WHITE);
			treeScroll.setColumnHeaderView(panel);
			panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

			JButton btnNewButton_1 = new JButton("Back");
			btnNewButton_1.setSize(100,100);
			btnNewButton_1.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 1) {
						back = new File(path.getText());
						if (back.getParent() == null) {
							back = new File(path.getText());
						} else {
							previousPath = new File(back.getParent());
							DefaultMutableTreeNode node = new DefaultMutableTreeNode(previousPath);
							showChildren(node);
							path.setText(back.getParent());
						}
					}
				}
			});
			btnNewButton_1.setIcon(
					new ImageIcon(FileManager.class.getResource("/icons/Go-back-icon.png")));
			btnNewButton_1.setHorizontalAlignment(SwingConstants.LEFT);
			panel.add(btnNewButton_1);

			JButton btnNewButton = new JButton("Next");
			btnNewButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					// if(i>0) {
					// next = new File(prevPath[i]);1
					// if(next.isDirectory()) {
					// DefaultMutableTreeNode node = new DefaultMutableTreeNode(next);
					// showChildren(node);
					// }
					// i--;
					// }
					// next = new File(prevPath[i]);
					String name = path.getText();
					next = new File(name);
					if (next.isDirectory()) {
						DefaultMutableTreeNode node = new DefaultMutableTreeNode(next);
						showChildren(node);
					}
				}
			});
			btnNewButton.setIcon(
					new ImageIcon(FileManager.class.getResource("/icons/Button-Next-icon.png")));
			panel.add(btnNewButton);
			splitPane.setBounds(5, 0, 965, 373);
			gui.add(splitPane);

		}
		return gui;
	}
	public static void ListFile(File dir)
	{
		File [] list = dir.listFiles();
		if (list == null) {
	        // throw an exception, return or do any other error handling here
	        return;
	    }
		for (File file : list) {
			
			if(file.isDirectory())
				{
					if(file.getName().equals(nameOfSearchFile))
					{
						pathOfSearchFile = file.getAbsolutePath();
						
						
					}
					ListFile(file);
					System.out.println(file.getName());
					
				}
			else {
				String extension = "";
				String fileName = file.getName();

				int i = fileName.lastIndexOf('.');
				if (i > 0) {
				    extension = fileName.substring(0,i);
				}
				System.out.println("file : "+extension);
				
				if(extension.equals(nameOfSearchFile))
				{
					System.out.println(file.getName());
					pathOfSearchFile = file.getAbsolutePath();
				}
				
			}
		}
	}
	
	
	public static String getSourceFileReName(String s)
	{
		String result;
		int temp = 0 ;
		for (int i = s.length()-1 ; i >=0 ; i--)
		{
			if(s.charAt(i)=='\\')
			{
				temp = i;
				break;
			}
		}
		result = s.substring(0, temp);
		return result;
	}
	public static String getTypeFile(String s)
	{
		String result;
		int temp = 0 ;
		for (int i = s.length()-1 ; i >=0 ; i--)
		{
			if(s.charAt(i)=='.')
			{
				temp = i;
			}
		}
		result = s.substring(temp, s.length());
		return result;
	}

	public static void copyFile(File oldLocation, File newLocation) throws IOException {
		if (oldLocation.exists()) {
			BufferedInputStream reader = new BufferedInputStream(new FileInputStream(oldLocation));
			BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(newLocation, false));
			try {
				byte[] buff = new byte[8192];
				int numChars;
				while ((numChars = reader.read(buff, 0, buff.length)) != -1) {
					writer.write(buff, 0, numChars);
				}
			} catch (IOException ex) {
				throw new IOException(
						"IOException when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
			} finally {
				try {
					if (reader != null) {
						writer.close();
						reader.close();
					}
				} catch (IOException ex) {
					// Log.e(TAG, "Error closing files when transferring " + oldLocation.getPath() +
					// " to " + newLocation.getPath() );
				}
			}
		} else {
			throw new IOException("Old location does not exist when transferring " + oldLocation.getPath() + " to "
					+ newLocation.getPath());
		}
	}

	public static void copyFolder(File source, File destination) {
		if (source.isDirectory()) {
			if (!destination.exists()) {
				destination.mkdirs();
			}

			String files[] = source.list();

			for (String file : files) {
				File srcFile = new File(source, file);
				File destFile = new File(destination, file);

				copyFolder(srcFile, destFile);
			}
		} else {
			InputStream in = null;
			OutputStream out = null;

			try {
				in = new FileInputStream(source);
				out = new FileOutputStream(destination);

				byte[] buffer = new byte[1024];

				int length;
				while ((length = in.read(buffer)) > 0) {
					out.write(buffer, 0, length);
				}
			} catch (Exception e) {
				try {
					in.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				try {
					out.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
//	if (file.isFile()) {
//		openFile(file);
//	} else
	
	protected void refresh() {
		back = new File(path.getText());
		if (back.getParent() == null) {
			back = new File(path.getText());
		} else {
			previousPath = new File(back.getParent());
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(back);
			showChildren(node);
			path.setText(back.getParent());
		}
	    
	}
	protected void refresh2() {
		back = new File(path.getText());
		if (back.getParent() == null) {
			back = new File(path.getText());
		} else {
			previousPath = new File(back.getParent());
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(previousPath);
			showChildren(node);
			path.setText(back.getParent());
		}
	    
	}
	static void deleteFolder(File file){
	      for (File subFile : file.listFiles()) {
	         if(subFile.isDirectory()) {
	            deleteFolder(subFile);
	         } else {
	            subFile.delete();
	         }
	      }
	      file.delete();
	   }

	public static void openFile(File path) throws IOException {
		if (!Desktop.isDesktopSupported()) {
			System.out.println("Desktop is not supported");
			return;
		}

		Desktop desktop = Desktop.getDesktop();
		if (path.exists())
			desktop.open(path);
	}

	void deleteDir(File file) {
		File[] contents = file.listFiles();
		if (contents != null) {
			for (File f : contents) {
				if (!Files.isSymbolicLink(f.toPath())) {
					deleteDir(f);
				}
			}
		}
		file.delete();
	}

	public static void deleteFile(File element) {
		if (element.isDirectory()) {
			for (File sub : element.listFiles()) {
				deleteFile(sub);
			}
		}
		element.delete();
	}

	public void showRootFile() {
		tree.setSelectionInterval(0, 0);
	}

	@SuppressWarnings("unused")
	private TreePath findTreePath(File find) {

		for (int ii = 0; ii < tree.getRowCount(); ii++) {
			TreePath treePath = tree.getPathForRow(ii);
			Object object = treePath.getLastPathComponent();
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) object;
			File nodeFile = (File) node.getUserObject();

			if (nodeFile == find) {
				return treePath;
			}
		}

		return null;
	}

	@SuppressWarnings("unused")
	private void showErrorMessage(String errorMessage, String errorTitle) {
		JOptionPane.showMessageDialog(gui, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);
	}

	@SuppressWarnings("unused")
	private void showThrowable(Throwable t) {
		t.printStackTrace();
		JOptionPane.showMessageDialog(gui, t.toString(), t.getMessage(), JOptionPane.ERROR_MESSAGE);
		gui.repaint();
	}

	private void setTableData(final File[] files) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (fileTableModel == null) {
					fileTableModel = new FileTableModel();
					table.setModel(fileTableModel);
				}
				table.getSelectionModel().removeListSelectionListener(listSelectionListener);
				fileTableModel.setFiles(files);
				table.getSelectionModel().addListSelectionListener(listSelectionListener);
				if (!cellSizesSet) {
					Icon icon = fileSystemView.getSystemIcon(files[0]);

					table.setRowHeight(icon.getIconHeight() + rowIconPadding);

					setColumnWidth(0, -1);
					setColumnWidth(3, 60);
					table.getColumnModel().getColumn(3).setMaxWidth(120);

					cellSizesSet = true;
				}

			}
		});
	}

	private void setColumnWidth(int column, int width) {
		TableColumn tableColumn = table.getColumnModel().getColumn(column);
		if (width < 0) {
			JLabel label = new JLabel((String) tableColumn.getHeaderValue());
			Dimension preferred = label.getPreferredSize();
			width = (int) preferred.getWidth() + 14;
		}
		tableColumn.setPreferredWidth(width);
		tableColumn.setMaxWidth(width);
		tableColumn.setMinWidth(width);
	}

	private void showChildren(DefaultMutableTreeNode node) {
		tree.setEnabled(false);

		SwingWorker<Void, File> worker = new SwingWorker<Void, File>() {
			@Override
			public Void doInBackground() {
				File file = (File) node.getUserObject();
				if (file.isDirectory()) {
					File[] files = fileSystemView.getFiles(file, true); // !!
					if (node.isLeaf()) {
						for (File child : files) {
							if (child.isDirectory()) {
								publish(child);
							}
						}
					}
					setTableData(files);
				}
				return null;
			}

			@Override
			protected void process(List<File> chunks) {
				for (File child : chunks) {
					node.add(new DefaultMutableTreeNode(child));
				}
			}

			@Override
			protected void done() {
				tree.setEnabled(true);
			}
		};

		worker.execute();
	}

	@SuppressWarnings("unused")
	private void setFileDetails(File file) {
		currentFile = file;
		Icon icon = fileSystemView.getSystemIcon(file);
		path.setText(file.getPath());

		JFrame f = (JFrame) gui.getTopLevelAncestor();
		if (f != null) {
			f.setTitle(APP_TITLE + " :: " + fileSystemView.getSystemDisplayName(file));
		}

		gui.repaint();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
//					com.sun.java.swing.plaf.gtk.GTKLookAndFeel
//					com.sun.java.swing.plaf.motif.MotifLookAndFeel
//					com.sun.java.swing.plaf.windows.WindowsLookAndFeel
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
//				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
//					UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
//					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception weTried) {
				}
				JFrame f = new JFrame(APP_TITLE);
				f.setForeground(Color.WHITE);
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.setTitle("FILE-EXPLORER");

				FileManager FileBrowser = new FileManager();
				f.setContentPane(FileBrowser.getGui());

				try {
					URL urlBig = FileBrowser.getClass().getResource("fb-icon-32x32.png");
					URL urlSmall = FileBrowser.getClass().getResource("fb-icon-16x16.png");
					ArrayList<Image> images = new ArrayList<Image>();
					images.add(ImageIO.read(urlBig));
					images.add(ImageIO.read(urlSmall));
					f.setIconImages(images);
				} catch (Exception weTried) {
				}

				f.pack();
				f.setLocation(200, 200);
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.setMinimumSize(new Dimension(1000, 420));
				f.setVisible(true);

				FileBrowser.showRootFile();
			}
		});
	}

	@SuppressWarnings("serial")
	class FileTableModel extends AbstractTableModel {

		private File[] files;
		private FileSystemView fileSystemView = FileSystemView.getFileSystemView();
		private String[] columns = { "Icon", "File", "Path/name", "Size",

		};

		FileTableModel() {
			this(new File[0]);
		}

		FileTableModel(File[] files) {
			this.files = files;
		}

		public Object getValueAt(int row, int column) {
			File file = files[row];
			switch (column) {
			case 0:
				return fileSystemView.getSystemIcon(file);
			case 1:
				return fileSystemView.getSystemDisplayName(file);
			case 2:
				return file.getPath();
			case 3:
				return file.length();
			default:
				System.err.println("Logic Error");
			}
			return "";
		}

		public int getColumnCount() {
			return columns.length;
		}

		public Class<?> getColumnClass(int column) {
			switch (column) {
			case 0:
				return ImageIcon.class;
			case 3:
				return Long.class;

			}
			return String.class;
		}

		public String getColumnName(int column) {
			return columns[column];
		}

		public int getRowCount() {
			return files.length;
		}

		public File getFile(int row) {
			return files[row];
		}

		public void setFiles(File[] files) {
			this.files = files;
			fireTableDataChanged();
		}
	}

	@SuppressWarnings("serial")
	class FileTreeCellRenderer extends DefaultTreeCellRenderer {

		private FileSystemView fileSystemView;

		private JLabel label;
		//

		FileTreeCellRenderer() {
			label = new JLabel();
			label.setOpaque(true);
			fileSystemView = FileSystemView.getFileSystemView();
		}
		
		//pathOfSearchFile
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			File file = (File) node.getUserObject();
			label.setIcon(fileSystemView.getSystemIcon(file));
			label.setText(fileSystemView.getSystemDisplayName(file));
			label.setToolTipText(file.getPath());
			//label.setBackground(backgroundSelectionColor);
			

			if (selected) {
				label.setBackground(backgroundSelectionColor);
				label.setForeground(textSelectionColor);
			}
			else {
				label.setBackground(backgroundNonSelectionColor);
				label.setForeground(textNonSelectionColor);
			}

			return label;
		}
		
	}
	
}