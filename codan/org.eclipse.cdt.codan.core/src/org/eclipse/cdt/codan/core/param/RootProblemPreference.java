/*******************************************************************************
 * Copyright (c) 2009,2010 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.param;


/**
 * Common problem preference root for most of the codan problems
 *
 * @noextend This class is not intended to be extended by clients.
 * @since 2.0
 */
public class RootProblemPreference extends MapProblemPreference {
	/**
	 * name of top level preference
	 */
	public static final String KEY = PARAM;

	/**
	 *  Default constructor
	 */
	public RootProblemPreference() {
		super(KEY, ""); //$NON-NLS-1$
		addChildDescriptor(new FileScopeProblemPreference());
		addChildDescriptor(new LaunchModeProblemPreference());
	}

	/**
	 * @return scope preference
	 */
	public FileScopeProblemPreference getScopePreference() {
		return (FileScopeProblemPreference) getChildDescriptor(
				FileScopeProblemPreference.KEY);
	}
	/**
	 * @return launch mode
	 */
	public LaunchModeProblemPreference getLaunchModePreference() {
		return (LaunchModeProblemPreference) getChildDescriptor(LaunchModeProblemPreference.KEY);

	}
}