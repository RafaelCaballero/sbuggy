

import java.awt.EventQueue;

import control.Controller;
import model.Model;
import view.DebuggerFrame;
import view.ViewInterface;

public class SQLDebugger {

	private static Model mod = null; // the model in the MVC pattern
	private static ViewInterface view = null; // the view in the MVC pattern
	private static Controller control = null; // the control in the MVC pattern

	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				mvc();

			}
		});
	}

	public static void mvc() {
		// the model
		mod = new Model();
		// view
		view = new DebuggerFrame();
		// controller
		control = new Controller(view, mod);
		// configura la vista
		view.setController(control);
		// y arranca la interfaz (vista):
		view.start();
	}

}
