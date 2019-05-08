package model;

import java.util.HashMap;

/**
 * Processes deploying nodes in the current database
 * 
 * @author rafa
 *
 */
public class Deploys {

	// processes deploying nodes
	HashMap<String, DeployInBackGround> processes;

	public Deploys() {
		processes = new HashMap<String, DeployInBackGround>();
	}

	public void reset() {
		processes = new HashMap<String, DeployInBackGround>();
	}

}
