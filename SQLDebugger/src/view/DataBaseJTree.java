package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.slf4j.Logger;

import logback.AreaAppender;
import model.relation.Database;
import model.relation.Relation;
import model.relation.Schema;
import model.relation.Table;
import model.relation.View;

public class DataBaseJTree {

	private JTree tree;
	private DefaultMutableTreeNode root;
	private static final Logger logger = AreaAppender.getLogger(DataBaseJTree.class);

	private DatabaseTreeCellRenderer renderer;

	public DataBaseJTree(String dbname, Database db, MenuTree stateMenu) {
		init(dbname, db);
		// mouse listener
		JTreeMouseListener mouseListener = new JTreeMouseListener(tree, stateMenu);
		tree.addMouseListener(mouseListener);
	}

	/**
	 * This method is call after deployment new nodes. A precondition is that
	 * the tree already contains all the schemata and all the relations in each
	 * schema. The only possible novelties are that some relation nodes now have
	 * children.
	 * 
	 * @param dbname
	 *            Database name
	 * @param db
	 *            Database
	 * @return true if the tree model has changed, false otherwise
	 */
	public boolean recreateModel(String dbname, Database db) {
		int nchanges = 0;
		TreeModel tm = tree.getModel();
		DefaultMutableTreeNode theRoot = (DefaultMutableTreeNode) tm.getRoot();
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> schemata = theRoot.children();
		// iterate schemata
		while (schemata.hasMoreElements()) {
			DefaultMutableTreeNode schemaNode = schemata.nextElement();
			// iterate relations inside
			@SuppressWarnings("unchecked")
			Enumeration<DefaultMutableTreeNode> relations = schemaNode.children();
			// iterate relations in the schema
			while (relations.hasMoreElements()) {
				DefaultMutableTreeNode relationNode = relations.nextElement();
				// look for schema and name
				Relation r = (Relation) relationNode.getUserObject();
				// expand the node if necessary
				// the condition indicates a discrepancy: the gui node has no
				// children, but the relation has subrelations
				if (r != null && relationNode.getChildCount() == 0 && r.getSubrelations().size() > 0) {
					Set<Relation> children = r.getSubrelations();
					for (Relation rs : children) {
						addRelation(relationNode, rs);
						nchanges++;
					}
				}
			}
		}

		if (nchanges > 0) {
			logger.trace("Changes in the tree. {} new nodes added", nchanges);
			tree.setModel(tm);
		}
		return nchanges > 0;
	}

	public void initModel(String dbname, Database db) {
		root = new DefaultMutableTreeNode(dbname);

		// sort databasase
		TreeMap<String, Schema> sortedDatabase = new TreeMap<String, Schema>(db);
		// now traverse the TreeMap
		Iterator<String> keySetIterator = sortedDatabase.keySet().iterator();

		while (keySetIterator.hasNext()) {
			String key = keySetIterator.next();
			Schema s = sortedDatabase.get(key);
			DefaultMutableTreeNode node = new DefaultMutableTreeNode("Schema " + key);

			// sort the schemata
			TreeMap<String, Relation> sortedSchema = new TreeMap<String, Relation>(s);
			Iterator<String> schemaIterator = sortedSchema.keySet().iterator();
			while (schemaIterator.hasNext()) {
				String relName = schemaIterator.next();
				Relation r = sortedSchema.get(relName);
				addRelation(node, r);
			}
			root.add(node);
			// System.out.println("key: " + key + " value: " +
			// sortedDatabase.get(key));
		}

	}

	private void addRelation(DefaultMutableTreeNode node, Relation r) {
		DefaultMutableTreeNode nodeR = new DefaultMutableTreeNode(r);
		node.add(nodeR);
		Set<Relation> srs = r.getSubrelations();
		for (Relation sr : srs) {
			logger.trace("Adding relation {} to relation {}", sr.getName(), r.getName());
			addRelation(nodeR, sr);
		}
	}

	private void init(String dbname, Database db) {
		initModel(dbname, db);
		tree = new JTree(root);
		//tree.setRootVisible(false);

	
		// displaying icons
		if (tree.getCellRenderer() instanceof DefaultTreeCellRenderer) {

			renderer = new DatabaseTreeCellRenderer();
			tree.setCellRenderer(renderer);

		} else {
			System.err.println("Sorry, no special colors today.");
		}

		// only leaves can be selected
		tree.setSelectionModel(new LeafOnlyTreeSelectionModel());

		// select just one node
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

	}

	public JTree getTree() {
		return tree;
	}

	public void setTrusted(View trusted) {
		this.renderer.setTrusted(trusted);
	}

}

class DatabaseTreeCellRenderer /* implements TreeCellRenderer */ extends DefaultTreeCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel label;
	@SuppressWarnings("unused")
	private View trusted;
	private static final Color colorFiltered = new Color(50, 50, 150);

	DatabaseTreeCellRenderer() {
		label = new JLabel();
		trusted = null;
		
		//  this.setBackgroundNonSelectionColor(Color.YELLOW);
		  this.setBackgroundSelectionColor(Color.ORANGE);
//		  this.setTextNonSelectionColor(Color.BLACK);
		  this.setTextSelectionColor(Color.BLUE);
//		*/

	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		Object o = ((DefaultMutableTreeNode) value).getUserObject();
		label.setForeground(Color.BLACK);
		Font font = label.getFont();
		Font fontappname = new Font(font.getName(), Font.PLAIN, font.getSize());
		if (selected) {				
			label.setBackground(new Color(240,120,70));
			label.setOpaque(true);
		} else {
			label.setBackground(Color.WHITE);
			
		}
		
		label.setFont(fontappname);
		label.repaint();

		if (o instanceof Table) {
			Table table = (Table) o;
			ImageIcon im = new ImageIcon(getClass().getResource(table.getIcon()));
			label.setIcon(im);
			label.setText(table.getName());
			if (table.getFilter() != null) {
				label.setForeground(colorFiltered);
			}

		} else if (o instanceof View) {
			View view = (View) o;
			label.setIcon((Icon) new ImageIcon(getClass().getResource(view.getIcon())));
			label.setText(view.getName());
			if (view.getFilter() != null)
				label.setForeground(colorFiltered);

		} else {
			label.setIcon(null);
			label.setText("" + value);
		}
		
		return label;
	}

	/**
	 * Indicates that a view is a Trusted specification
	 */
	public void setTrusted(View trusted) {
		this.trusted = trusted;
	}
}

/**
 * Only paths with length > 2 can be selected. This avoids selecting schemata,
 * only tables and views are allowed
 * 
 * @author rafa
 *
 */
class LeafOnlyTreeSelectionModel extends DefaultTreeSelectionModel {
	private static final long serialVersionUID = 1L;

	private TreePath[] augmentPaths(TreePath[] pPaths) {
		ArrayList<TreePath> paths = new ArrayList<TreePath>();

		for (int i = 0; i < pPaths.length; i++) {

			// if (((DefaultMutableTreeNode)
			// pPaths[i].getLastPathComponent()).isLeaf()) {
			if (pPaths[i] != null && pPaths[i].getPath() != null && pPaths[i].getPath().length > 2)
				paths.add(pPaths[i]);

		}

		return paths.toArray(pPaths);
	}

	@Override
	public void setSelectionPaths(TreePath[] pPaths) {
		super.setSelectionPaths(augmentPaths(pPaths));
	}

	@Override
	public void addSelectionPaths(TreePath[] pPaths) {
		super.addSelectionPaths(augmentPaths(pPaths));
	}

}
