package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.slf4j.Logger;

import view.statusbar.StatusBar;
import view.statusbar.StatusBarMessage;
import model.connection.ConnectionData;
import model.relation.Database;
import model.relation.Relation;
import model.relation.Table;
import model.relation.View;
import model.tablepages.TableModelResultData;
import conf.AppConf;
import control.Controller;
import logback.AreaAppender;
import logback.TextFactory;

/**
 * Main application GUI
 * 
 * @author rafa
 *
 */
public class DebuggerFrame extends JFrame implements ViewInterface {

	private static final Logger logger = AreaAppender.getLogger(DebuggerFrame.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final Color SIDECOLOR = new Color(230, 230, 255);;
	private static String appname = "SBuggy";
	private static String appdesc = "Algorithmic SQL debugger";
	private static String title = "SQL Debugger";
	private Menu menu = new Menu("Menu");
	private ImageMenu imageMenu = new ImageMenu(menu);
	// right-click menu in views and tables
	private MenuTree stateMenu = new MenuTree("Node State");

	AppConf conf = new AppConf();
	// login form
	private LoginForm login = new LoginForm(null, true, conf);

	// filter form
	private FilterForm filter = new FilterForm(null, true);

	//private JDialog helpDialog;
	private AboutDialog aboutDialog;

	// this panel is for containing the trees
	JPanel west;
	// this for showing the query results
	DisplayRelation displayRelationPanel;
	JTabbedPane east;

	DataBaseJTree treeWithModel = null;
	JTree tree = null;
	JScrollPane spane = null;

	// status bar
	StatusBar statusBar = null;

	// Database
	Database db;
	String dbName;

	/**
	 * To display the source code of the view
	 */
	private final SyntaxDocument doc = new SyntaxDocument();
	private JTextPane textViewSourceCode;
	private static int SOURCECODETAB;

	/**
	 * Displays the contents of a relation
	 */
	DisplayResultSet drs;

	/**
	 * Constructs the main window
	 * 
	 * @throws HeadlessException
	 */
	public DebuggerFrame() throws HeadlessException {
		super();
		init();
	}

	private void init() {
		setTitle(title);

		initComponents();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {

			e.printStackTrace();
		}

		/*
		 * setLocationRelativeTo(null); setSize(640, 480);
		 * setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); pack();
		 * setVisible(true);
		 */

	}

	/**
	 * Prepare the main window
	 */
	private void initComponents() {
		// borderLayout as outer level
		JPanel panel = new JPanel(new BorderLayout());
		// panel.setBorder((Border) new EmptyBorder(new Insets(0, 5, 5, 5)));

		// menu
		JMenuBar menubar = new JMenuBar();
		JMenu file = new JMenu("File");

		menubar.add(file);

		JPanel top = topPanel();
		panel.add(top, BorderLayout.NORTH);
		JSplitPane center = centerPanel();
		panel.add(center, BorderLayout.CENTER);

		add(panel);

		statusBar = new StatusBar();
		// statusbar.setBorder(LineBorder.createGrayLineBorder());
		add(statusBar, BorderLayout.SOUTH);

	}

	private JSplitPane centerPanel() {
		west = westPanel();
		east = eastPanel();
		JSplitPane center = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, west, east);
		center.setDividerSize(6);
		return center;
	}

	/**
	 * Tabbed panel at the east
	 * 
	 * @return
	 */
	private JTabbedPane eastPanel() {

		east = new JTabbedPane();
		ImageIcon tableicon = createImageIcon("/resources/tableIcon.png");
		ImageIcon logicon = createImageIcon("/resources/log-icon.png");
		ImageIcon viewicon = createImageIcon("/resources/viewIcon.png");
		this.displayRelationPanel = new DisplayRelation();
		east.addTab("Relation", tableicon, displayRelationPanel, "Display selected relation");

		textViewSourceCode = new JTextPane(doc);
		JScrollPane stextView = new JScrollPane(textViewSourceCode);
		east.addTab("Source", viewicon, stextView, "View Declaration");
		SOURCECODETAB = 1;
		east.setEnabledAt(SOURCECODETAB, false);

		JTextPane text = new JTextPane();
		JScrollPane stext = new JScrollPane(text);
		east.addTab("Log", logicon, stext, "Logging information");
		TextFactory.setText(text);

		return east;

	}

	private JPanel westPanel() {

		JPanel west = new JPanel(new BorderLayout());
		west.setBorder((Border) new EmptyBorder(new Insets(1, 1, 1, 1)));
		west.setPreferredSize(new Dimension(250, -1));

		return west;

	}

	private JPanel topPanel() {
		JPanel top = new JPanel(new BorderLayout());
		top.setBorder((Border) new EmptyBorder(new Insets(5, 5, 5, 5)));

		JPanel topLeft = new JPanel(new BorderLayout());
		topLeft.setBorder((Border) new EmptyBorder(new Insets(3, 3, 3, 3)));

		JPanel topLeftCenter = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		Color cTop = /* Color.WHITE; */ new Color(30, 50, 100);
		top.setBackground(cTop);
		topLeft.setBackground(cTop);
		topLeftCenter.setBackground(cTop);
		topRight.setBackground(cTop);
		JLabel labelName = new JLabel(appname);
		JLabel labelDesc = new JLabel(" -  " + appdesc);

		labelName.setBackground(new Color(30, 50, 100));
		labelName.setForeground(new Color(00, 250, 100));
		labelDesc.setBackground(new Color(30, 50, 100));
		labelDesc.setForeground(new Color(255, 255, 255));

		Font fontappname = new Font("SansSerif", Font.ITALIC + Font.BOLD, 20);
		Font fontappdesc = new Font("Serif", Font.ITALIC + Font.BOLD, 16);
		labelName.setFont(fontappname);
		labelDesc.setFont(fontappdesc);
		topLeftCenter.add(labelName);
		topLeftCenter.add(labelDesc);

		// ImageHopla image = new ImageHopla();
		// topLeft.add(image,BorderLayout.WEST);
		topLeft.add(topLeftCenter, BorderLayout.CENTER);
		topRight.add(imageMenu);

		top.add(topLeft, BorderLayout.WEST);
		top.add(topRight, BorderLayout.EAST);

		return top;
	}

	@Override
	public void setController(Controller control) {
		// display the menu associated to the icon
		imageMenu.addMouseListener(control);
		menu.setActionListener(control);
		login.setActionListener(control);
		filter.setActionListener(control);
		stateMenu.setActionListener(control);
		this.displayRelationPanel.mySetActionListener(control);

	}

	@Override
	public void start() {
		statusBar.setMessage("");
		setLocationRelativeTo(null);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(640, 480);
		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
		setVisible(true);
		conf = new AppConf();
		login.setVisible(true);

	}

	@Override
	public void showMenu() {
		// menu associated to the icon
		imageMenu.showMenu();

	}

	@Override
	public void status(StatusBarMessage msg) {
		this.statusBar.setMessage(msg);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see view.ViewInterface#setConnected(model.connection.ConnectionData,
	 * model.relation.Database)
	 */
	public void setConnected(ConnectionData props, Database db, TreeSelectionListener listener) {
		statusBar.setMessage(
				"Connected to database " + props.getDbName() + " by user " + props.getProps().getProperty("user"));
		menu.disableConnect();

		this.db = db;
		this.dbName = props.getDbName();

		if (spane != null)
			west.remove(spane);

		treeWithModel = new DataBaseJTree(dbName, db, stateMenu);
		tree = treeWithModel.getTree();
		tree.addTreeSelectionListener(listener);
		spane = new JScrollPane(tree);

		west.add(spane);
		west.updateUI();

	}

	@Override
	public ConnectionData getConectionData() {
		Properties props = new Properties();
		props.setProperty("user", login.getUser());
		props.setProperty("password", login.getPass());
		props.setProperty("ssl", login.getSsl());
		String url = login.getUrl();
		String database = login.getDatabase();
		ConnectionData cd = new ConnectionData(props, url, database);
		return cd;
	}

	@Override
	public void status(String msg) {
		statusBar.setMessage(msg);

	}

	@Override
	public void closeLoginForm() {
		login.setVisible(false);

	}

	@Override
	public void openLoginForm() {
		login.setVisible(true);

	}

	@Override
	public void displayError(String msg, Exception e) {
		JOptionPane.showMessageDialog(null, "Error: " + msg + (e == null ? "" : e.getMessage()), "Error",
				JOptionPane.ERROR_MESSAGE);

	}

	@Override
	public AppConf getLoginConf() {
		AppConf result = login.getConfig();

		return result;

	}

	/*
	 * A new relation has been selected. Display its rows (non-Javadoc)
	 * 
	 * @see view.ViewInterface#displayRelation(model.tablepages.TableModel)
	 */
	@Override
	public void displayRelation(TableModelResultData t) {
		if (drs != null) {
			drs.setTableModel(t);
			displayRelationPanel.update(drs);

		} else {
			// logger.error("Display result set object is null! ");
			drs = new DisplayResultSet();
			drs.setTableModel(t);
			displayRelationPanel.display(drs);

		}

	}

	@Override
	public boolean saveLogin(AppConf conf) throws IOException {
		conf.store();
		return true;

	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = DebuggerFrame.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	@Override
	public void refreshTree() {
		logger.trace("Refresh tree");
		TreePath selected = tree.getSelectionPath();

		boolean changed = treeWithModel.recreateModel(dbName, db);

		tree.setSelectionPath(selected);
		/*
		 * tree.revalidate(); tree.repaint();
		 */
		if (changed)
			logger.trace("Tree model changed");

	}

	@Override
	public Relation getSelectedRelation() {
		Relation r = null;
		TreePath path = tree.getSelectionPath();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		Object value = node.getUserObject();
		if (value instanceof Table || value instanceof View)
			r = (Relation) value;
		return r;
	}

	@Override
	public DefaultTreeModel getTreeModel() {

		return (DefaultTreeModel) tree.getModel();
	}

	@Override
	public void setTreeModel(DefaultTreeModel treemodel) {
		TreePath path = tree.getSelectionPath();
		tree.setModel(treemodel);
		tree.setSelectionPath(path);
		tree.revalidate();
		tree.repaint();

	}

	@Override
	public void informBuggy(Relation buggy) {
		if (buggy.isView()) {
			logger.info("Incorrect view found: {}", buggy.fullName());
			JOptionPane.showMessageDialog(this, "Incorrect view found: " + buggy.fullName(), "Bug Found!",
					JOptionPane.PLAIN_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this, "Incorrect table found: " + buggy.fullName(), "Bug Found!",
					JOptionPane.PLAIN_MESSAGE);
			logger.info("Incorrect table found: {}", buggy.fullName());

		}
	}

	@Override
	public void setTrusted(View trusted) {
		treeWithModel.setTrusted(trusted);
		tree.revalidate();
		tree.repaint();
	}

	@Override
	public void showSource(String string) {
		textViewSourceCode.setText(string);
		if (string != null && string.length() > 0)
			east.setEnabledAt(SOURCECODETAB, true);
		else {
			east.setEnabledAt(SOURCECODETAB, false);
			if (east.getSelectedIndex() == SOURCECODETAB)
				east.setSelectedIndex(0);
		}

	}

	@Override
	public void openFilterForm(String fullName, String currentFilter, String currentOrderBy) {
		filter.setTitle("Filter & Order by - Relation " + fullName);
		filter.setFilter(currentFilter);
		filter.setOrderBy(currentOrderBy);
		filter.setVisible(true);

	}

	@Override
	public void closeFilterForm() {
		filter.setVisible(false);

	}

	@Override
	public String getFilter() {
		String filter = this.filter.getFilter();
		return filter;
	}

	@Override
	public String getOrderBy() {
		String orderBy = this.filter.getOrderBy();
		return orderBy;
	}
/*
	private void initHelp() {
		if (helpDialog == null) {
			HelpDetailedPanel hd = new HelpDetailedPanel(new HelpFile().toString());
			//hd.setSize(640, 400);
			JScrollPane scrollPane = new JScrollPane(hd);
			helpDialog = new JDialog(this, "SBuggy - Help", false);
			helpDialog.setResizable(true);
			helpDialog.getContentPane().add(scrollPane);
			helpDialog.setSize(640, 400);
			helpDialog.pack();
			Dimension Size = Toolkit.getDefaultToolkit().getScreenSize();
			helpDialog.setLocation(new Double((Size.getWidth() / 2) - (helpDialog.getWidth() / 2)).intValue(),
					new Double((Size.getHeight() / 2) - (helpDialog.getHeight() / 2)).intValue());
		}

	}
*/
	@Override
	public void help() {
		//initHelp();
		//helpDialog.setVisible(true);
		//new HelpFile();
		String url = conf.getProperty("helpURL");
			try {
				openWebpage(new URL(url).toURI());
			} catch (MalformedURLException e) {

				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		

	}

	@Override
	public void about() {
		if (aboutDialog==null) {
			aboutDialog = new AboutDialog(this);
		}
		aboutDialog.setVisible(true);
	}

	
	public static void openWebpage(URI uri) {
	    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	            desktop.browse(uri);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}

	@Override
	public JFrame getFrame() {

		return this;
	}

}
