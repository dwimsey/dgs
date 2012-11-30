/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DGS.dgs4j.ProcessingEngine;

/**
 *
 * @author dwimsey
 */
public class ProcessingEngineSecurityManager extends SecurityManager {

	@Override
	public void checkPermission(java.security.Permission perm) throws SecurityException {
		try {
//            super.checkPermission(perm);
		} catch (SecurityException ex) {
			if (perm.getName().equals("setSecurityManager")) {
				return;
			}
			throw new SecurityException(ex);
		}
	}
}
