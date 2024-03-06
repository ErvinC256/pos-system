import javax.swing.*; 
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.Stack;
import java.lang.Double;
import java.text.DecimalFormat;
import javax.swing.table.*;
import java.util.*;
import java.io.*; 
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

class productEntry { // for use in entryStack
    String id, name;
    int quantity;
    double subTotal;
    productEntry(String a, String b, int c, double d) {
        id = a;
        name = b;
        quantity = c;
        subTotal = d;
    }
}
public class POSSystem extends JFrame {
    private String currentSalesID; 
    private double sessionTotalSales; 
    private JButton sideBarButton1 = new JButton("Home");
    private JButton sideBarButton2 = new JButton("Point-of-Sales");
    private JButton sideBarButton3 = new JButton("Products");
    private JPanel sideBar;
    private JPanel homePanel = new JPanel();
    private JPanel posPanel = new JPanel();
    private JPanel productsPanel = new JPanel();
    private DefaultTableModel inventoryTableModel = new DefaultTableModel(new Object[]{"Product ID", "Product Name", "Unit Price", "Quantity"}, 0);
    private JTable inventoryTable = new JTable(inventoryTableModel);
    DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
    private Map<String, Integer> cart = new HashMap<String, Integer>();
    private Stack<productEntry> entryStack = new Stack<productEntry>(); // for stacking product entry in cart
    
    public POSSystem(String title) {
        super(title);
        init_Components();
        init_homeComponents();
        init_posComponents();
        init_productsComponents();
    }
    private void init_Components() {
        sideBarButton1.setFont(new Font("Arial", Font.BOLD, 20));
        sideBarButton2.setFont(new Font("Arial", Font.BOLD, 20));
        sideBarButton3.setFont(new Font("Arial", Font.BOLD, 20));
        sideBarButton1.setFocusable(false);
        sideBarButton2.setFocusable(false);
        sideBarButton3.setFocusable(false);
        sideBarButton1.setContentAreaFilled(false);
        sideBarButton2.setContentAreaFilled(false);
        sideBarButton3.setContentAreaFilled(false);
        sideBarButton1.setBorder(new EmptyBorder(0, 0, 0, 0));
        sideBarButton2.setBorder(new EmptyBorder(0, 0, 0, 0));
        sideBarButton3.setBorder(new EmptyBorder(0, 0, 0, 0));

        // sideBar
        sideBar = new JPanel();
        sideBar.setBackground(new Color(224, 255, 255));
        sideBarButton1.setBackground(sideBar.getBackground());
        sideBarButton2.setBackground(sideBar.getBackground());
        sideBarButton3.setBackground(sideBar.getBackground());
        sideBar.setLayout(new GridLayout(0,1));
        sideBar.add(sideBarButton1);
        sideBar.add(sideBarButton2);
        sideBar.add(sideBarButton3);

        // main panel
        final JPanel mainPanel = new JPanel(new CardLayout());
        mainPanel.add(homePanel, "card1");
        mainPanel.add(posPanel, "card2");
        mainPanel.add(productsPanel, "card3");
        
        // splitpane.left = sideBar --- splitpane.right = mainpanel (a stack of cards)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sideBar, mainPanel);
        splitPane.setDividerLocation(200);
        splitPane.setEnabled(false);
        splitPane.setOneTouchExpandable(true);

        add(splitPane); // adding splitpane to the frame

        sideBarButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                CardLayout cl = (CardLayout)(mainPanel.getLayout());
                cl.show(mainPanel, "card1");
            }
        });
        sideBarButton2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                CardLayout cl = (CardLayout)(mainPanel.getLayout());
                cl.show(mainPanel, "card2");
            }
        });
        sideBarButton3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                CardLayout cl = (CardLayout)(mainPanel.getLayout());
                cl.show(mainPanel, "card3");
            }
        });

        // init inventory table
        try {
            File file = new File("products.csv");
            Scanner sc;
            sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String[] line = sc.nextLine().split(",");
                inventoryTableModel.addRow(line);
            }
            sc.close();
        } 
        catch (FileNotFoundException fnfe) {
            JOptionPane.showMessageDialog(mainPanel, "The specified file is not found");
        } 
        catch (IOException ioe) {
            JOptionPane.showMessageDialog(mainPanel, "I/O error occurred");
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        // init salesID
        ArrayList<String> lines = new ArrayList<String>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File("sales.csv"));
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
            int lastIndex = lines.size() - 1;
            String lastLine = lines.get(lastIndex);
            String[] lastLineData = lastLine.split(",");
            int num = Integer.parseInt(lastLineData[0]);
            currentSalesID = Integer.toString(++num);
        }
        catch (FileNotFoundException fnfe) {
            JOptionPane.showMessageDialog(mainPanel, "The specified file is not found");
        } 
        catch (IOException ioe) {
            JOptionPane.showMessageDialog(mainPanel, "I/O error occurred");
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }
    private void init_homeComponents() { 
        Border border = BorderFactory.createTitledBorder("Home");
        ((TitledBorder)border).setTitleJustification(TitledBorder.CENTER);
        homePanel.setBorder(border);
        homePanel.setBackground(sideBar.getBackground());

        Image img = new ImageIcon("HOME.gif").getImage();
        JLabel label = new JLabel();
        label.setIcon(new ImageIcon(img));
        label.setHorizontalAlignment(JLabel.CENTER);
        homePanel.add(label);
    }
    private void init_posComponents() { 
        Border border = BorderFactory.createTitledBorder("Point-of-Sales");
        ((TitledBorder)border).setTitleJustification(TitledBorder.CENTER);
        posPanel.setBorder(border);
        
        for (int i = 0; i < inventoryTableModel.getRowCount(); i++) {
            comboBoxModel.addElement(inventoryTableModel.getValueAt(i, 1));
        }

        // initialize lowest-level components
        final int maxEntries = 30;
        final DefaultListModel listModel1 = new DefaultListModel(); // west
        final DefaultListModel listModel2 = new DefaultListModel();
        final JList itemList1 = new JList(listModel1);
        final JList itemList2 = new JList(listModel2);
        final JLabel editLabel1 = new JLabel("Last Entry ID"); // east
        final JLabel editLabel2 = new JLabel("Last Entry Quantity");
        final JLabel editLabel3 = new JLabel("Last Entry Subtotal");
        final JTextField editTextField1 = new JTextField(8);
        final JTextField editTextField2 = new JTextField(8);
        final JTextField editTextField3 = new JTextField(8);
        final JButton deleteButton = new JButton("Delete");
        final JButton clearButton = new JButton("Clear");
        final JButton newButton = new JButton("New");
        final JButton computeButton = new JButton("Compute");
        final JButton checkOutButton = new JButton("Checkout");
        final JLabel transAmtLabel = new JLabel("Amount");
        final JLabel transPaidLabel = new JLabel("Paid");
        final JLabel transChangeLabel = new JLabel("Change");
        final JTextField transAmtField = new JTextField();
        final JTextField transPaidField = new JTextField();
        final JTextField transChangeField = new JTextField();
        final JButton calButton = new JButton("Calculate");
        final JButton endTransButton = new JButton("End Trans.");
        final JLabel summaryLabel1 = new JLabel("Sales ID"); // east
        final JLabel summaryLabel2 = new JLabel("No. of Entries");
        final JLabel summaryLabel3 = new JLabel("Average Unit Price");
        final JLabel summaryLabel4 = new JLabel("Trans. Quantity");
        final JLabel summaryLabel5 = new JLabel("Trans. Subtotal");
        final JLabel summaryLabel6 = new JLabel("Tax");
        final JLabel summaryLabel7 = new JLabel("Trans. Total");
        final JTextField summaryTextField1 = new JTextField(10);
        final JTextField summaryTextField2 = new JTextField(10);
        final JTextField summaryTextField3 = new JTextField(10);
        final JTextField summaryTextField4 = new JTextField(10);
        final JTextField summaryTextField5 = new JTextField(10);
        final JTextField summaryTextField6 = new JTextField(10);
        final JTextField summaryTextField7 = new JTextField(10);
        final JLabel addLabel1 = new JLabel("Product"); // south
        final JLabel addLabel2 = new JLabel("Quantity");
        final JComboBox addComboBox = new JComboBox(comboBoxModel);
        final JSpinner addSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 100, 1));
        final JButton addCartButton = new JButton("Add");
        final JButton salesButton = new JButton("Check Sales");
        
        // format lowest-level components
        itemList1.setFont(new Font("", Font.PLAIN, 16)); // west
        itemList2.setFont(new Font("", Font.PLAIN, 16));
        itemList1.setPreferredSize(new Dimension(250,0));
        itemList2.setPreferredSize(new Dimension(250,0));
        editTextField1.setHorizontalAlignment(JTextField.CENTER); // east
        editTextField2.setHorizontalAlignment(JTextField.CENTER);
        editTextField3.setHorizontalAlignment(JTextField.CENTER);
        editTextField1.setText("-");
        editTextField2.setText("0");
        editTextField3.setText("0.0");
        editTextField1.setEditable(false);
        editTextField2.setEditable(false);
        editTextField3.setEditable(false);
        editTextField1.setBorder(BorderFactory.createLoweredBevelBorder());
        editTextField2.setBorder(BorderFactory.createLoweredBevelBorder());
        editTextField3.setBorder(BorderFactory.createLoweredBevelBorder());
        deleteButton.setBorder(BorderFactory.createRaisedBevelBorder());
        clearButton.setBorder(BorderFactory.createRaisedBevelBorder());
        newButton.setBorder(BorderFactory.createRaisedBevelBorder());
        computeButton.setBorder(BorderFactory.createRaisedBevelBorder());
        checkOutButton.setBorder(BorderFactory.createRaisedBevelBorder());
        deleteButton.setBackground(Color.lightGray);
        clearButton.setBackground(Color.lightGray);
        newButton.setBackground(Color.lightGray);
        computeButton.setBackground(Color.lightGray);
        checkOutButton.setBackground(Color.lightGray);
        transAmtField.setHorizontalAlignment(JTextField.CENTER);
        transPaidField.setHorizontalAlignment(JTextField.CENTER);
        transChangeField.setHorizontalAlignment(JTextField.CENTER);
        transAmtField.setEditable(false);
        transChangeField.setEditable(false);
        transAmtField.setBorder(BorderFactory.createLoweredBevelBorder());
        transPaidField.setBorder(BorderFactory.createLoweredBevelBorder());
        transChangeField.setBorder(BorderFactory.createLoweredBevelBorder());
        calButton.setBorder(BorderFactory.createRaisedBevelBorder());
        endTransButton.setBorder(BorderFactory.createRaisedBevelBorder());
        calButton.setBackground(Color.lightGray);
        endTransButton.setBackground(Color.lightGray);
        summaryTextField1.setHorizontalAlignment(JTextField.CENTER); // east
        summaryTextField2.setHorizontalAlignment(JTextField.CENTER);
        summaryTextField3.setHorizontalAlignment(JTextField.CENTER);
        summaryTextField4.setHorizontalAlignment(JTextField.CENTER);
        summaryTextField5.setHorizontalAlignment(JTextField.CENTER);
        summaryTextField6.setHorizontalAlignment(JTextField.CENTER);
        summaryTextField7.setHorizontalAlignment(JTextField.CENTER);
        summaryTextField1.setEditable(false);
        summaryTextField2.setEditable(false);
        summaryTextField3.setEditable(false);
        summaryTextField4.setEditable(false);
        summaryTextField5.setEditable(false);
        summaryTextField6.setEditable(false);
        summaryTextField7.setEditable(false);
        summaryTextField1.setBorder(BorderFactory.createLoweredBevelBorder());
        summaryTextField2.setBorder(BorderFactory.createLoweredBevelBorder());
        summaryTextField3.setBorder(BorderFactory.createLoweredBevelBorder());
        summaryTextField4.setBorder(BorderFactory.createLoweredBevelBorder());
        summaryTextField5.setBorder(BorderFactory.createLoweredBevelBorder());
        summaryTextField6.setBorder(BorderFactory.createLoweredBevelBorder());
        summaryTextField7.setBorder(BorderFactory.createLoweredBevelBorder());
        summaryTextField1.setText(currentSalesID);
        summaryTextField2.setText("0");     
        summaryTextField3.setText("0.0");   
        summaryTextField4.setText("0");  
        summaryTextField5.setText("0.0");
        addComboBox.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXX"); // south
        addComboBox.setFont(new Font("", Font.BOLD, 14));
        addSpinner.setFont(new Font("", Font.BOLD, 14));
        addSpinner.setPreferredSize(new Dimension(100,25));
        
        // set itemPanel - WEST on top-level panel
        JScrollPane scrollPane1 = new JScrollPane(itemList1); // left
        JScrollPane scrollPane2 = new JScrollPane(itemList2); // right
        scrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel itemPanel = new JPanel();
        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        itemPanel.setBorder(etchedBorder);
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.X_AXIS));
        itemPanel.add(scrollPane1);
        itemPanel.add(scrollPane2);
        
        // set centerPanel - CENTER on top-level panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        centerPanel.add(Box.createHorizontalStrut(10));
        centerPanel.add(new JSeparator(SwingConstants.VERTICAL));
        centerPanel.add(Box.createHorizontalStrut(10));

        // set controlPanel - EAST on top-level panel
        JPanel editPanel1 = new JPanel();
        editPanel1.setLayout(new GridLayout(0,1));
        editPanel1.add(deleteButton);
        editPanel1.add(clearButton);
        editPanel1.add(newButton);
        editPanel1.add(computeButton);
        editPanel1.add(checkOutButton);

        JPanel editPanel2 = new JPanel();
        editPanel2.setLayout(new BoxLayout(editPanel2, BoxLayout.Y_AXIS));
        editPanel2.add(transAmtLabel);
        editPanel2.add(transAmtField);
        editPanel2.add(transPaidLabel);
        editPanel2.add(transPaidField);
        editPanel2.add(Box.createRigidArea(new Dimension(0,20)));
        calButton.setMaximumSize(editPanel2.getMaximumSize());
        editPanel2.add(calButton);
        editPanel2.add(Box.createRigidArea(new Dimension(0,20)));
        editPanel2.add(transChangeLabel);
        editPanel2.add(transChangeField);
        editPanel2.add(Box.createRigidArea(new Dimension(0,20)));
        endTransButton.setMaximumSize(editPanel2.getMaximumSize());
        editPanel2.add(endTransButton);

        final JPanel editCardPanel = new JPanel(); // left (bottom)
        editCardPanel.setPreferredSize(new Dimension(0,450));         
        editCardPanel.setLayout(new CardLayout());
        editCardPanel.add(editPanel1, "card 1");
        editCardPanel.add(editPanel2, "card 2");

        JPanel editPanel = new JPanel(); // left (top)
        editPanel.setPreferredSize(new Dimension(160,0));     
        editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.Y_AXIS));
        editPanel.add(editLabel1);
        editPanel.add(editTextField1);
        editPanel.add(editLabel2);
        editPanel.add(editTextField2);
        editPanel.add(editLabel3);
        editPanel.add(editTextField3);

        JPanel editMainPanel = new JPanel(); // left
        editMainPanel.setLayout(new BoxLayout(editMainPanel, BoxLayout.Y_AXIS));
        editMainPanel.add(editPanel);
        editMainPanel.add(Box.createRigidArea(new Dimension(0,20)));
        editMainPanel.add(new JSeparator());
        editMainPanel.add(Box.createRigidArea(new Dimension(0,20)));
        editMainPanel.add(editCardPanel);

        JPanel summaryPanel = new JPanel(); // right
        summaryPanel.setPreferredSize(new Dimension(160,0));
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.add(summaryLabel1);
        summaryPanel.add(summaryTextField1);
        summaryPanel.add(summaryLabel2);
        summaryPanel.add(summaryTextField2);
        summaryPanel.add(summaryLabel3);
        summaryPanel.add(summaryTextField3);
        summaryPanel.add(summaryLabel4);
        summaryPanel.add(summaryTextField4);
        summaryPanel.add(summaryLabel5);
        summaryPanel.add(summaryTextField5);
        summaryPanel.add(summaryLabel6);
        summaryPanel.add(summaryTextField6);
        summaryPanel.add(summaryLabel7);
        summaryPanel.add(summaryTextField7);
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
        controlPanel.add(editMainPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(20,0)));
        controlPanel.add(summaryPanel);

        // set southPanel - SOUTH on top-level panel
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        southPanel.add(addLabel1);
        southPanel.add(addComboBox);
        southPanel.add(addLabel2);
        southPanel.add(addSpinner);
        southPanel.add(addCartButton);
        southPanel.add(Box.createRigidArea(new Dimension(310,0)));
        southPanel.add(salesButton);

        // add to top-level panel
        posPanel.setLayout(new BorderLayout());
        posPanel.add(itemPanel, BorderLayout.WEST);
        posPanel.add(centerPanel, BorderLayout.CENTER);
        posPanel.add(controlPanel, BorderLayout.EAST);
        posPanel.add(southPanel, BorderLayout.SOUTH);

        addCartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if(listModel1.getSize() >= maxEntries) {
                    JOptionPane.showMessageDialog(posPanel, "Reached limit of entries!");
                }
                else if((Integer) addSpinner.getValue() == 0) {}
                else {
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");

                    // fetch values of current entry
                    int idColumnIndex = inventoryTable.getColumnModel().getColumnIndex("Product ID");
                    int nameColumnIndex = inventoryTable.getColumnModel().getColumnIndex("Product Name");
                    int priceColumnIndex = inventoryTable.getColumnModel().getColumnIndex("Unit Price");
                    int quantityColumnIndex = inventoryTable.getColumnModel().getColumnIndex("Quantity");
                    
                    String selectedItem = (String) addComboBox.getSelectedItem();
                    int selectedQuantity = (Integer) addSpinner.getValue();
                    String selectedID = "";
                    double selectedPrice = 0.0;
                    int selectedInventory = 0;
                    for (int row = 0; row < inventoryTable.getRowCount(); row++) {
                        String name = (String) inventoryTable.getValueAt(row, nameColumnIndex);
                        if (name.equals(selectedItem)) {
                            Object idObj = inventoryTable.getValueAt(row, idColumnIndex);
                            selectedID = idObj.toString();
                            Object priceObj = inventoryTable.getValueAt(row, priceColumnIndex);
                            String str1 = priceObj.toString();
                            selectedPrice = Double.parseDouble(str1);
                            Object invenObj = inventoryTable.getValueAt(row, quantityColumnIndex);
                            String str2 = invenObj.toString();
                            selectedInventory = Integer.parseInt(str2);
                            break;
                        }
                    }

                    // fetch values from current transaction state and compute 
                    int numberofEntries = Integer.parseInt(summaryTextField2.getText());
                    numberofEntries++;
                    int transactionQuantity = Integer.parseInt(summaryTextField4.getText());
                    transactionQuantity += selectedQuantity;
                    double transactionSub = Double.parseDouble(summaryTextField5.getText());
                    transactionSub += selectedQuantity * selectedPrice;
                    
                    if(selectedQuantity > selectedInventory) { // check selected quantity against inventory                
                        JOptionPane.showMessageDialog(posPanel, "Out of stock!");
                    }
                    else { 
                        // check total quantity of the product in the cart against inventory 
                        int selectedTotalQuantity = cart.containsKey(selectedItem) ? cart.get(selectedItem) + selectedQuantity : selectedQuantity;
                        if(selectedTotalQuantity > selectedInventory) { 
                            JOptionPane.showMessageDialog(posPanel, "Out of stock!");
                        }
                        else {
                            // set new transaction state
                            listModel1.addElement(selectedItem);
                            listModel2.addElement(selectedQuantity + "  X  RM " + decimalFormat.format(selectedPrice) + "  =  RM " + decimalFormat.format(selectedQuantity*selectedPrice));
                            summaryTextField2.setText(Integer.toString(numberofEntries));
                            summaryTextField3.setText(decimalFormat.format(transactionSub/transactionQuantity));
                            summaryTextField4.setText(Integer.toString(transactionQuantity));
                            summaryTextField5.setText(decimalFormat.format(transactionSub));

                            editTextField1.setText(selectedID);
                            editTextField2.setText(Integer.toString(selectedQuantity));
                            editTextField3.setText(decimalFormat.format(selectedQuantity*selectedPrice));

                            // push new entry into stack and cart
                            productEntry newEntry = new productEntry(selectedID, selectedItem, selectedQuantity, selectedQuantity*selectedPrice);
                            entryStack.push(newEntry);
                            cart.put(selectedItem, selectedTotalQuantity);
                        }
                    }    
                }
            }   
        });
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                int lastIndex = listModel1.getSize() - 1;
                if (lastIndex >= 0) {
                    listModel1.removeElementAt(lastIndex);
                    listModel2.removeElementAt(lastIndex);
                    productEntry lastEntry = entryStack.pop();

                    DecimalFormat decimalFormat = new DecimalFormat("0.00");

                    // fetch values from current transaction state and compute
                    int numberofEntries = Integer.parseInt(summaryTextField2.getText());
                    numberofEntries--;
                    int transactionQuantity = Integer.parseInt(summaryTextField4.getText());
                    transactionQuantity -= lastEntry.quantity;
                    double transactionSub = Double.parseDouble(summaryTextField5.getText());
                    transactionSub -= lastEntry.subTotal;

                    int selectedTotalQuantity = cart.containsKey(lastEntry.name) ? cart.get(lastEntry.name) - lastEntry.quantity : lastEntry.quantity;

                    // set new transaction state
                    summaryTextField2.setText(Integer.toString(numberofEntries));
                    summaryTextField3.setText(decimalFormat.format(transactionSub/transactionQuantity));
                    summaryTextField4.setText(Integer.toString(transactionQuantity));
                    summaryTextField5.setText(decimalFormat.format(transactionSub));

                    // reset the entry's quantity in cart and peek from stack
                    cart.put(lastEntry.name, selectedTotalQuantity);
                    if(entryStack.isEmpty()) {
                        clearButton.doClick();
                    }
                    else {
                        lastEntry = entryStack.peek();
                        editTextField1.setText(lastEntry.id);
                        editTextField2.setText(Integer.toString(lastEntry.quantity));
                        editTextField3.setText(decimalFormat.format(lastEntry.subTotal));
                    }
                }
            }
        });
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                listModel1.removeAllElements();
                listModel2.removeAllElements();

                editTextField1.setText("-");
                editTextField2.setText("0");
                editTextField3.setText("0.0");
                summaryTextField2.setText("0");
                summaryTextField3.setText("0.0");
                summaryTextField4.setText("0");
                summaryTextField5.setText("0.0");
                summaryTextField6.setText("");
                summaryTextField7.setText("");
                transAmtField.setText("");
                transPaidField.setText("");
                transChangeField.setText("");

                // clear stack and cart
                reset();
            }
        });
        newButton.addActionListener(new ActionListener() { // new transaction page
            public void actionPerformed(ActionEvent evt) {
                clearButton.doClick();
                int newSalesID = Integer.parseInt(currentSalesID);
                currentSalesID = Integer.toString(++newSalesID);
                summaryTextField1.setText(currentSalesID);
            }
        });
        computeButton.addActionListener(new ActionListener() { // compute tax into subtotal to get total
            public void actionPerformed(ActionEvent evt) {
                double subTotal = Double.parseDouble(summaryTextField5.getText());
                if(subTotal == 0.0) {} 
                else {
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");

                    subTotal = Double.parseDouble(summaryTextField5.getText());
                    double taxAmt = subTotal * 0.1;
                    summaryTextField6.setText(decimalFormat.format(taxAmt));
                    summaryTextField7.setText(decimalFormat.format(subTotal+taxAmt));
                }
            }
        });
        checkOutButton.addActionListener(new ActionListener() { // 
            public void actionPerformed(ActionEvent evt) {
                if(summaryTextField7.getText().trim().isEmpty()) {}
                else {
                    CardLayout cl = (CardLayout)(editCardPanel.getLayout());
                    cl.next(editCardPanel);
                    transAmtField.setText(summaryTextField7.getText());
                }
            }
        });
        calButton.addActionListener(new ActionListener() { // calculate balance from paid amount
            public void actionPerformed(ActionEvent evt) {
                if(transPaidField.getText().trim().isEmpty()) {}
                else {
                    try {
                        DecimalFormat decimalFormat = new DecimalFormat("0.00");

                        double paid = Double.parseDouble(transPaidField.getText());
                        double amt = Double.parseDouble(transAmtField.getText());
                        if(paid < amt) {}
                        else {
                            transChangeField.setText(decimalFormat.format(paid-amt));
                        }
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(posPanel, "Enter a number!");
                    }
                }
            }
        });
        endTransButton.addActionListener(new ActionListener() { // end transaction
            public void actionPerformed(ActionEvent evt) {
                if(transChangeField.getText().trim().isEmpty()) {}
                else {
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");

                    int nameColumnIndex = inventoryTable.getColumnModel().getColumnIndex("Product Name");
                    int quantityColumnIndex = inventoryTable.getColumnModel().getColumnIndex("Quantity");
            
                    for (Map.Entry<String, Integer> entry : cart.entrySet()) {
                        String key = entry.getKey();
                        Integer value = entry.getValue();
                        for (int row = 0; row < inventoryTable.getRowCount(); row++) {
                            String name = (String) inventoryTable.getValueAt(row, nameColumnIndex);
                            if (name.equals(key)) {
                                Object invenObj = inventoryTable.getValueAt(row, quantityColumnIndex);
                                String str2 = invenObj.toString();
                                int selectedInventory = Integer.parseInt(str2);
                                selectedInventory -= value;
                                invenObj = Integer.valueOf(selectedInventory);
                                inventoryTableModel.setValueAt(invenObj, row, quantityColumnIndex);
                                break;
                            }
                        }
                    }
                    sessionTotalSales += Double.parseDouble(transAmtField.getText());
                    Calendar calendar = Calendar.getInstance();
                    Date currentDate = calendar.getTime();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedDate = dateFormat.format(currentDate);
                    try {
                        FileWriter writer = new FileWriter("sales.csv", true);
                        writer.append(currentSalesID);
                        writer.append(",");
                        writer.append(transAmtField.getText());
                        writer.append(",");
                        writer.append(formattedDate);
                        writer.append("\n");
                        writer.flush();
                        writer.close();
                    } 
                    catch (FileNotFoundException fnfe) {
                        JOptionPane.showMessageDialog(posPanel, "The specified file is not found");
                    } 
                    catch (IOException ioe) {
                        JOptionPane.showMessageDialog(posPanel, "I/O error occurred");
                    }
                    catch (Exception e) {
                        JOptionPane.showMessageDialog(posPanel, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    CardLayout cl = (CardLayout)(editCardPanel.getLayout());
                    cl.next(editCardPanel);
                    newButton.doClick();
                }
            }
        });
        salesButton.addActionListener(new ActionListener() { // check total sales
            public void actionPerformed(ActionEvent evt) {
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                String message = "RM " + decimalFormat.format(sessionTotalSales);
                JOptionPane.showMessageDialog(posPanel, message, "Session Total Sales", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }
    private void init_productsComponents() { 
        Border border = BorderFactory.createTitledBorder("Products");
        ((TitledBorder)border).setTitleJustification(TitledBorder.CENTER);
        productsPanel.setBorder(border);
        
        // init tables
        final DefaultTableModel statusTableModel = new DefaultTableModel(new Object[]{"Product Name", "Quantity", "Status"}, 0);
        final JTable statusTable = new JTable(statusTableModel);
        inventoryTable.setDefaultEditor(Object.class, null);
        statusTable.setDefaultEditor(Object.class, null);

        // init lowest-level components
        final JButton addStockButton = new JButton("Add Stock");
        final JButton deleteStockButton = new JButton("Delete Stock");
        final JButton addItemButton = new JButton("Add Item"); 
        addItemButton.setBackground(Color.lightGray);
        final JButton deleteItemButton = new JButton("Delete Item");
        deleteItemButton.setBackground(Color.lightGray);
        final JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        quantitySpinner.setFont(new Font("", Font.BOLD, 14));
        quantitySpinner.setPreferredSize(new Dimension(100,25));

        //  prodManagePanel  
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        actionPanel.add(addStockButton);
        actionPanel.add(deleteStockButton);
        actionPanel.add(addItemButton);
        actionPanel.add(deleteItemButton);
        actionPanel.add(Box.createRigidArea(new Dimension(365,0)));
        actionPanel.add(quantitySpinner);
        
        JPanel prodManagePanel = new JPanel();
        prodManagePanel.setLayout(new BoxLayout(prodManagePanel, BoxLayout.Y_AXIS));
        prodManagePanel.add(actionPanel);
        prodManagePanel.add(new JScrollPane(inventoryTable));
        prodManagePanel.add(new JScrollPane(statusTable));
        
        // add to top-level panel
        productsPanel.setLayout(new BorderLayout()); 
        productsPanel.add(prodManagePanel, BorderLayout.CENTER);
        
        addStockButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (inventoryTable.getSelectedRow() == -1) {}
                else {
                    int nameColumnIndex = inventoryTable.getColumnModel().getColumnIndex("Product Name");
                    int quantityColumnIndex = inventoryTable.getColumnModel().getColumnIndex("Quantity");

                    // get current state
                    int selectedRow = inventoryTable.getSelectedRow();
                    int incrementQuantity =  (Integer) quantitySpinner.getValue();
                    Object selectedProduct = inventoryTableModel.getValueAt(selectedRow, nameColumnIndex);
                    Object currentQuantity = inventoryTableModel.getValueAt(selectedRow, quantityColumnIndex);

                    // set new state
                    String str = currentQuantity.toString();
                    int num = Integer.parseInt(str);
                    int newQuantity = num + incrementQuantity;
                    Object obj = Integer.valueOf(newQuantity);
                    inventoryTableModel.setValueAt(obj, selectedRow, quantityColumnIndex);
                    String productName = selectedProduct.toString();
                    statusTableModel.addRow(new Object[]{productName, Integer.toString(incrementQuantity), "Added"});
                }
            }
        });
        deleteStockButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {       
                if (inventoryTable.getSelectedRow() == -1) {}
                else {
                    int nameColumnIndex = inventoryTable.getColumnModel().getColumnIndex("Product Name");
                    int quantityColumnIndex = inventoryTable.getColumnModel().getColumnIndex("Quantity");

                    // get current state
                    int selectedRow = inventoryTable.getSelectedRow();
                    int decrementQuantity =  (Integer) quantitySpinner.getValue();
                    Object selectedProduct = inventoryTableModel.getValueAt(selectedRow, nameColumnIndex);
                    Object currentQuantity = inventoryTableModel.getValueAt(selectedRow, quantityColumnIndex);

                    // set new state
                    String str = currentQuantity.toString();
                    int num = Integer.parseInt(str);
                    if (decrementQuantity > num) {
                        decrementQuantity = num;
                    }
                    int newQuantity = num - decrementQuantity;
                    Object obj = Integer.valueOf(newQuantity);
                    inventoryTableModel.setValueAt(obj, selectedRow, quantityColumnIndex);
                    if(newQuantity == 0){
                        inventoryTableModel.removeRow(selectedRow);
                        comboBoxModel.removeElement(selectedProduct); // remove item from selection in pos 
                    }
                    String productName = selectedProduct.toString();
                    statusTableModel.addRow(new Object[]{productName, Integer.toString(decrementQuantity), "Deleted"});
                }        
            }           
        });      
        addItemButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                final JFrame newFrame = new JFrame();
                newFrame.setSize(305,305);
                newFrame.setResizable(false);
                final JPanel newFramePanel = new JPanel();
                Border emptyBorder = BorderFactory.createEmptyBorder(20, 20, 20, 20);
                newFramePanel.setBorder(emptyBorder);
                newFramePanel.setLayout(new GridLayout(5,2,10,10));
                
                JLabel idLabel = new JLabel("Product ID:");
                final JTextField idField = new JTextField(10);
                newFramePanel.add(idLabel);
                newFramePanel.add(idField);

                JLabel nameLabel = new JLabel("Product Name:");
                final JTextField nameField = new JTextField(10);
                newFramePanel.add(nameLabel);
                newFramePanel.add(nameField);

                JLabel priceLabel = new JLabel("Unit Price:");
                final JTextField priceField = new JTextField(10);
                newFramePanel.add(priceLabel);
                newFramePanel.add(priceField);

                JLabel quantityLabel = new JLabel("Quantity:");
                final JTextField quantityField = new JTextField(10);
                newFramePanel.add(quantityLabel);
                newFramePanel.add(quantityField);

                JButton submitButton = new JButton("Submit");
                newFramePanel.add(submitButton);

                newFrame.add(newFramePanel);
                newFrame.setLocationRelativeTo(productsPanel);
                newFrame.setVisible(true);

                submitButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt){
                        try {
                            String id = idField.getText();
                            String name = nameField.getText();
                            double price = Double.parseDouble(priceField.getText());
                            int quantity = Integer.parseInt(quantityField.getText());

                            inventoryTableModel.addRow(new Object[]{id, name, price, quantity});
                            comboBoxModel.addElement(name); // add new item to selection in pos
                            newFrame.dispose();
                        } catch(NumberFormatException e) {
                            JOptionPane.showMessageDialog(productsPanel, "Enter number for price and quantity!");
                        }
                    }
                });        
            }
        }); 
        deleteItemButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (inventoryTable.getSelectedRow() == -1) {}
                else {
                    int response = JOptionPane.showConfirmDialog(productsPanel, "Proceed?", "Confirm", JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        int nameColumnIndex = inventoryTable.getColumnModel().getColumnIndex("Product Name");
                        int selectedRow = inventoryTable.getSelectedRow();

                        Object nameObj = inventoryTableModel.getValueAt(selectedRow, nameColumnIndex);
                        String name = nameObj.toString();

                        inventoryTableModel.removeRow(selectedRow);
                        comboBoxModel.removeElement(name); // remove item from selection in pos
                    } else if (response == JOptionPane.NO_OPTION) {}
                }
            }           
        });
    }
    public void reset() {
        entryStack.clear();
        cart.clear();
    }
    private static void createAndShowGUI() {
        Font font = new Font("Arial", Font.BOLD, 16);
        UIManager.put("Button.font", font);
        UIManager.put("Label.font", font);
        UIManager.put("TextField.font", font);
        UIManager.put("TextArea.font", font);

        POSSystem frame = new POSSystem("POS SYSTEM");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(1200, 900);
        frame.setBackground(Color.PINK);
        frame.setVisible(true);
        frame.setResizable(false);
    }
    public static void main(String[] args) {
        createAndShowGUI();
    }
}
