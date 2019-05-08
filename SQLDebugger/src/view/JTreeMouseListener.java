package view;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

//import org.slf4j.Logger;

//import logback.AreaAppender;
import model.relation.Relation;
import model.relation.Table;
import model.relation.View;

public class JTreeMouseListener implements MouseListener {
	private JTree tree;
	private MenuTree popupMenu;
	// private static final Logger logger =
	// AreaAppender.getLogger(JTreeMouseListener.class);

	public JTreeMouseListener(JTree tree, MenuTree stateMenu) {
		this.tree = tree;
		this.popupMenu = stateMenu;

	}

	@Override
	public void mouseClicked(MouseEvent e) {

		if (SwingUtilities.isRightMouseButton(e)) {

			int row = tree.getClosestRowForLocation(e.getX(), e.getY());
			TreePath path = tree.getPathForRow(row);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			Object value = node.getUserObject();

			if (value instanceof Table || value instanceof View) {
				Relation r = (Relation) value;

				tree.setSelectionRow(row);
				if (r.completelyDeployed())
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

}
