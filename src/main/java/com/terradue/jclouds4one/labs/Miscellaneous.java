package com.terradue.jclouds4one.labs;

import static java.lang.System.getProperty;

public class Miscellaneous {
	
	public static void printVersionInfo()
	{
		System.out.printf( "%s %s%n",
				getProperty( "project.name" ),
				getProperty( "project.version" ) );
		System.out.printf( "Java version: %s, vendor: %s%n",
				getProperty( "java.version" ),
				getProperty( "java.vendor" ) );
	}
}
