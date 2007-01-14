/*
 * JiuInfo
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.apps;

/**
 * Contains several constants with information on JIU.
 * @author Marco Schmidt
 */
public interface JiuInfo
{
	/** 
	 * Three int values for the JIU version, in order (major, minor, patch).
	 */
	int[] JIU_NUMERICAL_VERSION = {0, 13, 0};

	/**
	 * Version as String, created from {@link #JIU_NUMERICAL_VERSION}.
	 */
	String JIU_VERSION = 
		JIU_NUMERICAL_VERSION[0] + "." + 
		JIU_NUMERICAL_VERSION[1] + "." +
		JIU_NUMERICAL_VERSION[2];

	/**
	 * URL of the homepage of the JIU project.
	 */
	String JIU_HOMEPAGE = "http://schmidt.devlib.org/jiu/";

	/**
	 * Address for feedback on JIU.
	 */
	String JIU_FEEDBACK_ADDRESS = "http://schmidt.devlib.org/jiu/feedback.html";
}
