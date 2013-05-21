package ch.specchio.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import java.util.ListIterator;


import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import jxl.Cell;
import jxl.Sheet;
import jxl.read.biff.BiffException;

import ch.specchio.client.SPECCHIOClient;
import ch.specchio.client.SPECCHIOClientException;
import ch.specchio.metadata.MetaDataFromTabController;
import ch.specchio.metadata.MetaDataFromTabModel;
import ch.specchio.types.Category;
import ch.specchio.types.attribute;

public class MetaDataFromTabView extends JFrame implements ActionListener, TreeSelectionListener, DocumentListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int SKIP_PARAMETER = 0;
	public static final int DELETE_EXISTING_AND_INSERT_NEW = 1;
	public static final int INSERT = 2;
	GridBagConstraints constraints;
	
	SpectralDataBrowser sdb;
	SPECCHIOClient specchio_client;
	
	MetaDataFromTabController controller = new MetaDataFromTabController();
	
	MetaDataFromTabModel model = controller.getModel();
	
	JScrollPane mdMatchingScrollPane;
	JPanel mdMatchingPanel = new JPanel();
	GridbagLayouter mdMatchingPanel_l = new GridbagLayouter(mdMatchingPanel);

	JPanel mdAssignmentInfoPanel = new JPanel();
	GridbagLayouter mdAssignmentInfoPanel_l = new GridbagLayouter(mdAssignmentInfoPanel);

	
	private JPanel matching_details_panel = null;
	GridbagLayouter matching_details_panel_l;	
	
	private JPanel regex_panel = null;
	GridbagLayouter regex_panel_l;	
	
	JTextField regex_start = new JTextField(10);
	JTextField regex_end = new JTextField(10);
	
	JLabel regex_example = new JLabel("REGEX example: ");
	
	JButton insert;
	
	ArrayList<JComboBox> matching_columns_combos = new ArrayList<JComboBox>();
	ArrayList<JComboBox> category_columns_combos = new ArrayList<JComboBox>();
	ArrayList<JComboBox> attribute_columns_combos = new ArrayList<JComboBox>();
	
	private boolean combobox_user_update = true; // used to control the handling of events when combo values are modified internally by the controller
	private boolean model_element_update = true;
	
	public MetaDataFromTabView() throws SPECCHIOClientException {
		super("Metadata Augmentation from Tabular Data");	

		// get a reference to the application's client object
		this.specchio_client = SPECCHIOApplication.getInstance().getClient();
		
		controller.setView(this);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		constraints = new GridBagConstraints();
		
		constraints.gridwidth = 1;
		constraints.insets = new Insets(4, 4, 4, 4);
		constraints.gridheight = 1;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.fill = GridBagConstraints.BOTH;
		
		JPanel control_panel = new JPanel();
		GridbagLayouter control_panel_l = new GridbagLayouter(control_panel);	
		
		
		// action listeners for regex fields
		regex_start.getDocument().addDocumentListener(this);
		regex_start.getDocument().putProperty("owner", regex_start);
		
		regex_end.getDocument().addDocumentListener(this);
		regex_end.getDocument().putProperty("owner", regex_end);		
		
		// set border layout for this dialog
		setLayout(new BorderLayout());
		this.setLayout(new BorderLayout());

		// create browser and add to control panel			
		sdb = new SpectralDataBrowser(specchio_client, true);
			
		// load currently selected campaign
		sdb.build_tree();
					
		JPanel sdb_panel = new JPanel();
		sdb_panel.add(sdb);
		constraints.gridy = 0;
		constraints.gridx = 0;
		control_panel_l.insertComponent(sdb_panel, constraints);

			// add tree listener
		sdb.tree.addTreeSelectionListener(this);	
		
		insert = new JButton("Insert Selected Metadata");	
		insert.addActionListener(this);
		insert.setActionCommand("Insert Selected Metadata");
		
		constraints.gridy++;
		control_panel_l.insertComponent(insert, constraints);
		
		// menu
		JMenu menu;
		JMenuItem menuItem;

		//Create the menu bar.
		JMenuBar menuBar = new JMenuBar();
		menu = new JMenu("File");

		menuItem = new JMenuItem("Open File...");
		menuItem.addActionListener(this);
		menu.add(menuItem);	
		
		menuItem = new JMenuItem("Open Column-Element Matching File...");
		menuItem.addActionListener(this);
		menu.add(menuItem);			
		
		menuBar.add(menu);

		setJMenuBar(menuBar);
		
		
		mdMatchingScrollPane = new JScrollPane();
		mdMatchingScrollPane.getViewport().add(mdMatchingPanel);
		mdMatchingScrollPane.setPreferredSize(new Dimension(600, 300));
		mdMatchingScrollPane.getVerticalScrollBar().setUnitIncrement(10);
		mdMatchingScrollPane.getHorizontalScrollBar().setUnitIncrement(10);
		
		add("West", new JScrollPane(control_panel));	
		add("Center", new JScrollPane(mdMatchingScrollPane));
		
		pack();
		

		
		setVisible(true);		
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		if(arg0.getActionCommand().equals("Open File..."))
		{
			
			// file selection
			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new TypeOfFile()); 
			
			int returnVal = fc.showOpenDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					File file = fc.getSelectedFile();
								
					// let controller load the file
					controller.loadFile(file);
				
					buildMatchingGUI();
				}
				catch (IOException ex) {
					JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not open file", JOptionPane.ERROR_MESSAGE);
				}
				catch (BiffException ex) {
					JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid file", JOptionPane.ERROR_MESSAGE);
				}
			}	    				

		}	
		
		if(arg0.getActionCommand().equals("Open Column-Element Matching File..."))
		{			
			// file selection
			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new TypeOfFile()); 
			
			int returnVal = fc.showOpenDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				
				controller.load_matching_table(file);
				
				buildMatchingGUI();
			}
		}
		
		
		if(arg0.getActionCommand().equals("Apply Auto-Matching"))
		{
			
			try {
				controller.autoMatching();
				
				model_element_update  = false;
				updateCategoryComboboxes();
				updateElementComboxes();
				model_element_update = true;

			} catch (SPECCHIOClientException ex) {
				ErrorDialog error = new ErrorDialog(this, "Error", ex.getUserMessage(), ex);
				error.setVisible(true);
			}			
			
		}
		
		
		if(arg0.getActionCommand().equals("Change Matching Column") && combobox_user_update)
		{
			try {
				combobox_user_update = false;
				
				JComboBox source = (JComboBox) arg0.getSource();
				
				int col = (Integer) source.getClientProperty("Column Number");
				
				int index = source.getSelectedIndex();			
				
				controller.setMatchingColumn(col, index);	
				
				updateMatchingColumnCombos();		
				
				updateMatchingInfoPanel();
				updateAssignmentInfoPanel();
			
				combobox_user_update = true;
			}
			catch (SPECCHIOClientException ex) {
				ErrorDialog error = new ErrorDialog(this, "Error", ex.getUserMessage(), ex);
				error.setVisible(true);
			}
		}
		
		
		if(arg0.getActionCommand().equals("Change Category for Data Column")  && combobox_user_update)
		{
			combobox_user_update = false;
			
			JComboBox source = (JComboBox) arg0.getSource();
			
//			ActionListener[] listeners = source.getActionListeners();
			
			System.out.println("category change");
			
			int col = (Integer) source.getClientProperty("Column Number");
			
			int index = source.getSelectedIndex();
			
			controller.setCategory(col, index);
			
			if(model_element_update) // switch to prevent overwriting of elements when automatching is applied
			{
				JComboBox element_combo = this.attribute_columns_combos.get(col);
			
				updateMetadataElementCombo(element_combo, model.getCategoryOfColumn(col).name);
				
				controller.setAttributeAtColumn(col, (combo_table_data) element_combo.getItemAt(0));
			}
			
			updateAssignmentInfoPanel();
		
			combobox_user_update = true;
		}

		
		if(arg0.getActionCommand().equals("Change Attribute for Data Column")  && combobox_user_update)
		{
			try {
				JComboBox source = (JComboBox) arg0.getSource();
				
				System.out.println("attribute change");
			
				int col = (Integer) source.getClientProperty("Column Number");
				controller.setAttributeAtColumn(col, (combo_table_data) source.getSelectedItem());
					
				if(col == model.getMatching_col()) updateMatchingInfoPanel();
				
				updateAssignmentInfoPanel();
			}
			catch (SPECCHIOClientException ex) {
				ErrorDialog error = new ErrorDialog(this, "Error", ex.getUserMessage(), ex);
				error.setVisible(true);
			}
		}
		
		if(arg0.getActionCommand().equals("Insert Selected Metadata"))
		{
			boolean success = controller.insert();
			
			//if(success) JOptionPane.showMessageDialog(null,"Metadata insert has finished.");
		}
		
		
		

	}
	
	
	
	private void updateCategoryComboboxes() {
		int i = 0;
		
		
		
		for(JComboBox combo : this.category_columns_combos)
		{
			Category cat = model.getCategoryOfColumn(i);

			updateSelectionOfCategoriesCombo(combo, cat.category_id);	
			
			// update the values of the related element combo
			JComboBox element_combo = attribute_columns_combos.get(i);
			updateMetadataElementCombo(element_combo, cat.name);
				
			i++;
		}		

		
	}

	private void updateMatchingInfoPanel() throws SPECCHIOClientException {
		
		if(matching_details_panel != null)
		{
		
			int r = 0;

			matching_details_panel.removeAll();

			// create table
			DefaultTableModel table_model = new DefaultTableModel();

			// Create column
			table_model.addColumn("Spectrum IDs");
			table_model.addColumn("DB Value");
			table_model.addColumn("Table Value");

			// get ids from spectral data browser
			ArrayList<Integer> ids;
			
			ids = model.getSpectrum_ids();

			if(ids != null)
			{
				for(Integer id : ids)
				{
					table_model.addRow(new Object[]{});
	
					table_model.setValueAt(id, r++, 0);
	
				}
				
				// get values for these spectra, if a matching column is selected
				if(model.matchingColumnIsSet() && ids.size()>0  && model.validAttribute(model.getMatching_col()))	
				{
				
					ArrayList<Object> values = controller.getDbValuesforMatchingCol();
									
					r = 0;
					for(Object value : values)
					{
						table_model.setValueAt(value, r++, 1);
					}
					
					
					// initiate matching and display matching row data
					controller.match();
					ArrayList<Object> matched_table_values = controller.getMatchedTableValues();
					
					r = 0;
					for(Object value : matched_table_values)
					{
						if(value != null)
							table_model.setValueAt(value, r++, 2);
						else
							r++;
					}
					
				
				}

			}

			JTable table = new JTable(table_model);
			
			int maxWidths = getMaximalRequiredColumnWidth(table, 0);
			
			Dimension size = table.getPreferredScrollableViewportSize();
			table.setPreferredScrollableViewportSize(new Dimension((int) Math.round(maxWidths * 3.0), size.height));		
			
			// create summary panel
			//JPanel sum_p = new JPanel();
			constraints.gridy = 0;
			constraints.gridx = 0;

			
		
			
			matching_details_panel_l.insertComponent(new JLabel("Number of spectra: " + model.getNumberOfSpectra()), constraints);
			constraints.gridy++;
			matching_details_panel_l.insertComponent(new JLabel("Number of matches: " + model.getNumber_of_matches()), constraints);
			constraints.gridy++;
//			matching_details_panel_l.insertComponent(new JLabel("Number of missed matches: " + model.getNumber_of_missing_matches()), constraints);
//			constraints.gridy++;
			
			//matching_details_panel.add(sum_p);
			matching_details_panel_l.insertComponent(new JScrollPane(table), constraints);

			matching_details_panel.revalidate();
			matching_details_panel.repaint();		

		}
	}

	private void updateMatchingColumnCombos() {
		
		if(model.matchingColumnIsSet())
		{
		
			int selected_col = model.getMatching_col();
			int selected_index = matching_columns_combos.get(selected_col).getSelectedIndex();

			// reset all to NIL
			for(JComboBox combo : this.matching_columns_combos)
			{
				combo.setSelectedIndex(0);
			}

			matching_columns_combos.get(selected_col).setSelectedIndex(selected_index);
		}
	}
	
	
	private void updateElementComboxes()
	{
		int i = 0;
		for(JComboBox combo : this.attribute_columns_combos)
		{
			int attr_id = model.getAttributeOfColumn(i).getId();
			this.updateSelectionOfMetadataElementCombo(combo, attr_id);			
			i++;
		}		
		
	}

	private void buildMatchingGUI() {
		
		combobox_user_update = true; // reset in case the GUI broke ...
		
		Sheet sheet = model.getCurrentSheet(); 
		
		mdMatchingPanel.removeAll(); // clear any existing components from the panel
		
		Border blackline = BorderFactory.createLineBorder(Color.black);
		
		matching_columns_combos.clear();
		category_columns_combos.clear();
		attribute_columns_combos.clear();
		
		
		
		// create control panel for sheet columns
		// --------------------------------------
		try {		
			
			JPanel sheet_control_panel = new JPanel();
			TitledBorder tb = BorderFactory.createTitledBorder(blackline, "Matching & Element Assignment Control");
			sheet_control_panel.setBorder(tb);			

			GridbagLayouter sheet_control_panel_l = new GridbagLayouter(sheet_control_panel);
			
			constraints.gridwidth = 1;
			constraints.gridx = 0;
			constraints.gridy = 1;

			for(int i=0;i<sheet.getColumns();i++)
			{
				JComboBox box = new JComboBox();
				combo_table_data item = new combo_table_data("NIL", 0);
				box.addItem(item);
				item = new combo_table_data("Matching Column", 1);
				box.addItem(item);
				box.addActionListener(this);
				box.putClientProperty("Column Number", i);
				box.setActionCommand("Change Matching Column");
				
				matching_columns_combos.add(box);

				sheet_control_panel_l.insertComponent(box, constraints);

				constraints.gridx++;
			}

	
			// create category drop downs for all sheet columns
			constraints.gridy++;
			constraints.gridx = 0;


			// get a list with available categories
			ArrayList<Category> categories =  model.getPossible_categories();
			
			ArrayList<Category> categories_init = new ArrayList<Category>();

			for(int i=0;i<sheet.getColumns();i++)
			{
				JComboBox box = getCategoriesCombo(categories, i);
				
				sheet_control_panel_l.insertComponent(box, constraints);
				constraints.gridx++;		
				
				// fill initial category setting for model
				categories_init.add(categories.get(0));

			}
			
			// init categories per column
			model.setCategories_of_columns(categories_init);

			
			// create metadata element drop downs for all sheet columns
			constraints.gridy++;
			constraints.gridx = 0;
			
			attribute NIL_attribute = new attribute();
			NIL_attribute.name = "NIL";
			
			ArrayList<attribute> attributes_init = new ArrayList<attribute>();
			
			for(int i=0;i<sheet.getColumns();i++)
			{
				JComboBox box = getMetadataElementCombo(categories.get(0).name, i); // use first element by default
				box.addActionListener(this);
				
//				ActionListener[] listeners = box.getActionListeners();
				
				sheet_control_panel_l.insertComponent(box, constraints);
				constraints.gridx++;					
				
				attributes_init.add(NIL_attribute);
			}

			model.setAttribute_of_columns(attributes_init);	
			
			// create sheet panel 
			// ------------------
			JPanel sheet_panel = new JPanel();
			tb = BorderFactory.createTitledBorder(blackline, "Tabular Data");
			sheet_panel.setBorder(tb);			

			
			constraints.gridy++;
			constraints.gridx = 0;

			
			for(int i=0;i<sheet.getColumns();i++)
			{
				JTable table = getTabularDataTable(sheet, i);

				sheet_control_panel_l.insertComponent(new JScrollPane(table), constraints);
				constraints.gridx++;
			}
			
			
			// create matching details panel
			matching_details_panel = new JPanel();
			tb = BorderFactory.createTitledBorder(blackline, "Matching Details");
			matching_details_panel.setBorder(tb);	
			matching_details_panel_l = new GridbagLayouter(matching_details_panel);	
			
			
			updateMatchingInfoPanel();
			
			// create assignment panel			
			tb = BorderFactory.createTitledBorder(blackline, "Assignment Details");
			mdAssignmentInfoPanel.setBorder(tb);	
			
			updateAssignmentInfoPanel();			
			
			// create regex panel
			regex_panel = new JPanel();
			tb = BorderFactory.createTitledBorder(blackline, "REGEX");
			regex_panel.setBorder(tb);	
			regex_panel_l = new GridbagLayouter(regex_panel);	
			
			constraints.gridy = 0;
			constraints.gridx = 0;			
			regex_panel_l.insertComponent(new JLabel("REGEX start"), constraints);
			constraints.gridx++;		
			regex_panel_l.insertComponent(regex_start, constraints);
			
			constraints.gridy++;
			constraints.gridx = 0;
			regex_panel_l.insertComponent(new JLabel("REGEX end"), constraints);
			constraints.gridx++;	
			regex_panel_l.insertComponent(regex_end, constraints);

			constraints.gridy++;
			constraints.gridx = 0;
			constraints.gridwidth = 2;
			regex_panel_l.insertComponent(regex_example, constraints);
			
			// create automated matching control and info panel 
			JPanel auto_matching_control_panel = buildAutoMatchingControlPanel();			

			
			// put the info panels into one panel
			JPanel info_panel = new JPanel();
			GridbagLayouter info_panel_l = new GridbagLayouter(info_panel);	
			
			constraints.gridwidth = 1;
			constraints.gridy = 0;
			constraints.gridx = 0;
			info_panel_l.insertComponent(regex_panel, constraints);
			constraints.gridy++;	
			info_panel_l.insertComponent(auto_matching_control_panel, constraints);
			constraints.gridy++;	
			info_panel_l.insertComponent(mdAssignmentInfoPanel, constraints);
			constraints.gridy++;
			info_panel_l.insertComponent(matching_details_panel, constraints);
			
			
			// create matching panel 
			// ---------------------
			
			constraints.gridy = 0;
			constraints.gridx = 0;
			
			mdMatchingPanel_l.insertComponent(info_panel, constraints);
			
			constraints.gridx++;

			//mdMatchingPanel_l.insertComponent(new JScrollPane(sheet_control_panel), constraints);
			mdMatchingPanel_l.insertComponent(sheet_control_panel, constraints);
			
			mdMatchingPanel.revalidate();
			mdMatchingPanel.repaint();

			// stretch the dialogue to a sensible size
			Dimension info_panel_preferred_size = info_panel.getPreferredSize();
			Insets insets = mdMatchingScrollPane.getInsets();
			Dimension sheet_control_preferred_size = sheet_control_panel.getPreferredSize();
			Dimension mdMatchingViewportSize = new Dimension();
			mdMatchingViewportSize.width = sheet_control_preferred_size.width + insets.left + insets.right;
			if (sheet.getColumns() > 3) {
				// make the panel wide enough to show the first three columns without scrolling
				mdMatchingViewportSize.width += info_panel_preferred_size.width * 3 / sheet.getColumns();
			} else {
				// show all columns
				mdMatchingViewportSize.width += info_panel_preferred_size.width;
			}
			mdMatchingViewportSize.height = info_panel_preferred_size.height + insets.top + insets.bottom;
			mdMatchingScrollPane.setPreferredSize(mdMatchingViewportSize);
			pack();
		
		} catch (SPECCHIOClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	
	private JPanel buildAutoMatchingControlPanel() {
		
		JPanel auto_matching_control_panel = new JPanel();
		GridbagLayouter sheet_auto_matching_control_panel_l = new GridbagLayouter(auto_matching_control_panel);
		
		Border blackline = BorderFactory.createLineBorder(Color.black);
		TitledBorder tb = BorderFactory.createTitledBorder(blackline, "Element - Column Auto-Matching");
		auto_matching_control_panel.setBorder(tb);			
		
		constraints.gridy = 0;
		constraints.gridx = 0;
		constraints.gridwidth = 3;
		
		String filename = "NIL";
		JButton apply = new JButton("Apply Auto-Matching");
		apply.addActionListener(this);
		
		if(model.getElement_column_matching_file() != null) filename = model.getElement_column_matching_file();
		else apply.setEnabled(false);
		
		sheet_auto_matching_control_panel_l.insertComponent(new JLabel("Matching File: " + filename), constraints);
		constraints.gridy++;
		
		
		
		constraints.gridwidth = 1;
		sheet_auto_matching_control_panel_l.insertComponent(apply, constraints);
		constraints.gridy++;
		
		
		return auto_matching_control_panel;
	}

	private void updateAssignmentInfoPanel() {
		
		mdAssignmentInfoPanel.removeAll();
		
		constraints.gridy = 0;
		constraints.gridx = 0;

		mdAssignmentInfoPanel_l.insertComponent(new JLabel("Number of assignable columns: " + model.getNumberOfAssignableColumns()), constraints);
		constraints.gridy++;

		
		mdAssignmentInfoPanel_l.insertComponent(new JLabel("Number of assigned columns: " + model.getNumberOfAssignedColumns()), constraints);
		constraints.gridy++;

		mdAssignmentInfoPanel.revalidate();
		mdAssignmentInfoPanel.repaint();			
		
	}

	private JComboBox getCategoriesCombo(ArrayList<Category> categories, int col)
	{
		JComboBox box = new JComboBox();
		ListIterator<Category> li = categories.listIterator();
	
		
		while(li.hasNext())
		{
			Category c = li.next();						
			combo_table_data item = new combo_table_data(c.name, c.category_id);
			box.addItem(item);	
		}
		
		box.putClientProperty("Column Number", col);
		box.setActionCommand("Change Category for Data Column");
		this.category_columns_combos.add(box);		
		box.addActionListener(this);
		
		return box;
	}
	
	
	private void updateSelectionOfCategoriesCombo(JComboBox box, int cat_id)
	{
		
		int cnt = box.getItemCount();
		
		for(int i=0;i<cnt;i++)
		{
			combo_table_data item = (combo_table_data) box.getItemAt(i);
			
			if(item.id == cat_id)
			{
				box.setSelectedIndex(i);
			}
		}
		
		
	}	

	private JComboBox getMetadataElementCombo(String category_name, int col) throws SPECCHIOClientException
	{
		JComboBox box = new JComboBox();
		
		fillMetadataElementCombo(box, category_name);
		
		attribute_columns_combos.add(box);
		
		box.addActionListener(this);
		box.putClientProperty("Column Number", col);
		box.setActionCommand("Change Attribute for Data Column");			
		
		return box;
	}
	
	
	private void updateSelectionOfMetadataElementCombo(JComboBox box, int attr_id)
	{
		
		int cnt = box.getItemCount();
		
		for(int i=0;i<cnt;i++)
		{
			combo_table_data item = (combo_table_data) box.getItemAt(i);
			
			if(item.id == attr_id)
			{
				box.setSelectedIndex(i);
			}
		}
		
		
	}
	
	private void updateMetadataElementCombo(JComboBox box, String category_name)
	{
		combobox_user_update = false;
		
		box.removeAllItems();
		
		try {
			fillMetadataElementCombo(box, category_name);
		} catch (SPECCHIOClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		combobox_user_update = true;
	}
	
	private void fillMetadataElementCombo(JComboBox box, String category_name) throws SPECCHIOClientException
	{
		attribute[] attr_array = specchio_client.getAttributesForCategory(category_name);
		
		combo_table_data item = new combo_table_data("NIL", 0);
		box.addItem(item);	
		
		for (attribute attr : attr_array) {			
			item = new combo_table_data(attr.name, attr.id);
			box.addItem(item);		
		}	
	
		
	}
	
	private JTable getTabularDataTable(Sheet sheet)
	{
		
		// create table and add to panel
		
		DefaultTableModel table_model = new DefaultTableModel();
		
		// Create columns
		for(int i=0;i<sheet.getColumns();i++)
		{
			Cell column_name_cell = sheet.getCell(i, 0);
			table_model.addColumn(column_name_cell.getContents());			
		}
		
		// fill values
		for(int r=1;r<sheet.getRows();r++)
		{
			for(int c=0;c<sheet.getColumns();c++)
			{
				
				table_model.addRow(new Object[]{});
				
				Cell cell = sheet.getCell(c, r);
				
				table_model.setValueAt(cell.getContents(), r-1, c);
			}
		}
		
		JTable table = new JTable(table_model);
		
		
		return table;
		
	}
	
	private JTable getTabularDataTable(Sheet sheet, int column)
	{
		
		// create table and add to panel
		
		DefaultTableModel table_model = new DefaultTableModel();
		
		// Create column
		Cell column_name_cell = sheet.getCell(column, 0);
		table_model.addColumn(column_name_cell.getContents());			

		
		// fill values
		for(int r=1;r<sheet.getRows();r++)
		{
				table_model.addRow(new Object[]{});
				
				Cell cell = sheet.getCell(column, r);
				
				table_model.setValueAt(cell.getContents(), r-1, 0);
		}
		
		JTable table = new JTable(table_model);
				
		
		int maxWidths = getMaximalRequiredColumnWidth(table, 0);
		
		Dimension size = table.getPreferredScrollableViewportSize();
		table.setPreferredScrollableViewportSize(new Dimension((int) Math.round(maxWidths * 1.0), size.height));		
		
		
		return table;
		
	}	
	
	
	
	private static int getMaximalRequiredColumnWidth(JTable table, int columnIndex) {
		
		JTableHeader tableHeader = table.getTableHeader();

		FontMetrics headerFontMetrics = tableHeader.getFontMetrics(tableHeader.getFont());

		
		int headerWidth = headerFontMetrics.stringWidth(table.getColumnName(columnIndex));

		
		int maxWidth = headerWidth;

		TableColumn column = table.getColumnModel().getColumn(columnIndex);

		TableCellRenderer cellRenderer = column.getCellRenderer();

		if(cellRenderer == null) {
			cellRenderer = new DefaultTableCellRenderer();
		}

		for(int row = 0; row < table.getModel().getRowCount(); row++) {
			Component rendererComponent = cellRenderer.getTableCellRendererComponent(table,
					table.getModel().getValueAt(row, columnIndex),
					false,
					false,
					row,
					columnIndex);

			double valueWidth = rendererComponent.getPreferredSize().getWidth();

			maxWidth = (int) Math.max(maxWidth, valueWidth);
		}

		return maxWidth;
	}
	
	

	//Class TypeOfFile (from: http://java2everyone.blogspot.com.au/2009/01/set-file-type-in-jfilechooser.html)
	class TypeOfFile extends FileFilter  
	{  
	 //Type of file that should be display in JFileChooser will be set here  
	 //We choose to display only directory and text file  
	 public boolean accept(File f)  
	 {  
	  return f.isDirectory() || f.getName().toLowerCase().endsWith(".xls");  
	 }  
	  
	 //Set description for the type of file that should be display  
	 public String getDescription()  
	 {  
	  return ".XLS files";  
	 }  
	}



	@Override
	public void valueChanged(TreeSelectionEvent arg0) {
		
		try {
			this.controller.setSpectrumIds(this.sdb.get_selected_spectrum_ids());
			updateMatchingInfoPanel();
		} catch (SPECCHIOClientException ex) {
			ErrorDialog error = new ErrorDialog(this, "Error", ex.getUserMessage(), ex);
			error.setVisible(true);
		}
		
		
		
		
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {
		JComponent owner = (JComponent) arg0.getDocument().getProperty("owner");
		
		if(owner == this.regex_start)
		{
			model.setRegex_start(regex_start.getText());			
		}
		else
		{
			model.setRegex_end(regex_end.getText());
		}

		try {
			updateMatchingInfoPanel();
			updateAssignmentInfoPanel();
			updateRegexInfoPanel();
		}
		catch (SPECCHIOClientException ex) {
			ErrorDialog error = new ErrorDialog(this, "Error", ex.getUserMessage(), ex);
			error.setVisible(true);
		}
						
	}

	private void updateRegexInfoPanel() {
	
		regex_example.setText("REGEX example: " + model.getRegexExample());	
		
		regex_panel.revalidate();
		regex_panel.repaint();
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		changedUpdate(arg0);		
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		changedUpdate(arg0);		
	}

	public int get_user_decision_on_existing_fields(String attribute_name) {
		
		
		Object[] options = new Object[3];
		
		options[0] = "Insert anyway";
		options[1] = "Delete existing and insert new values";
		options[2] = "Skip this parameter";
		
		int default_selection = 2;
		int decision;

		Object selectedValue = JOptionPane.showInputDialog(null,
				"You are about to insert a new metaparameter of the type " + attribute_name
				 + ".\n Some of the selected records already feature a metaparameter of the type " + attribute_name + " \n" +
				"Please select one of the following actions:",
				"Insert of existing metaparameter " +  attribute_name,
				JOptionPane.INFORMATION_MESSAGE,
				null,
				options,
				options[default_selection]);

		if(selectedValue == null || selectedValue.equals(options[2]))
		{
			decision = MetaDataFromTabView.SKIP_PARAMETER;
		}
		else if(selectedValue.equals(options[1]))
		{
			decision = MetaDataFromTabView.DELETE_EXISTING_AND_INSERT_NEW;
		}
		else
			decision = MetaDataFromTabView.INSERT;
		
		return decision;
		
	}  
	
	
}