package com.terradue.jclouds4one.labs;

/*
 *  Copyright 2013 Terradue srl
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static java.lang.System.currentTimeMillis;
import static java.lang.Runtime.getRuntime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Formatter;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public abstract class BaseTool
	implements Tool {

	protected final Logger logger = LoggerFactory.getLogger( BaseTool.class );

	@Parameter( names = { "-h", "--help" }, description = "Display help information" )
	private boolean printHelp;

	@Parameter( names = { "-v", "--version" }, description = "Display version information" )
	private boolean showVersion;

	@Parameter( names = { "-X", "--debug" }, description = "Produce execution debug output" )
	private boolean debug;

	@Parameter( names = { "--identity" }, description = "The Cloud Provider identity (username)", required = true )
	protected String identity;

	@Parameter( names = { "--credential" }, description = "The Cloud Provider credential (password)", required = true )
	protected String credential;

	@Parameter( names = { "--api" }, description = "The Cloud Provider API URI", required = true )
	protected String serviceApi;
	

	public final int execute( String... args ) {
		
		final JCommander commander = new JCommander( this );
		commander.setProgramName( getProperty( "app.name" ) );

		try {
			commander.parse( args );
		}
		catch ( ParameterException e ) {
			System.out.println( e.getMessage() );
			System.out.println( "Please type `"+ getProperty( "app.name" ) + " -h` for the usage." );
		}
		
		if ( printHelp ) {
			commander.usage();
			return -1;
		}

		if ( showVersion ) {
			Miscellaneous.printVersionInfo();
			return -1;
		}

		if ( debug ) {
			setProperty( "log.level", "DEBUG" );
		}
		else {
			setProperty( "log.level", "INFO" );
		}
		
		logger.info( "" );
		logger.info( "------------------------------------------------------------------------" );
		logger.info( "{}", getProperty( "app.name" ) );
		logger.info( "------------------------------------------------------------------------" );
		logger.info( "" );
		
		int exit = 0;
		Throwable error = null;
		
		long start = currentTimeMillis();
		
		try {
			
			execute();
			
		} catch ( Throwable t ) {
			
			exit = -1;
            error = t;
		}
		finally {
			
			logger.info( "" );
            
            if ( exit < 0 ) {

                if ( debug ) {
                    logger.error( "", error );
                }
                else {
                    logger.error( "{}", error.getMessage() );
                }

                logger.info( "" );
            }
		
            logger.info( "------------------------------------------------------------------------" );
            logger.info( "{}", ( exit < 0 ) ? "FAILURE" : "SUCCESS" );
			logger.info( "" );
			
			@SuppressWarnings("resource")
			Formatter uptime = new Formatter().format( "Total time:" );
	
	        long uptimeInSeconds = ( currentTimeMillis() - start ) / 1000;
	        final long hours = uptimeInSeconds / 3600;
	
	        if ( hours > 0 ) {
	        	
	            uptime.format( " %s hour%s", hours, ( hours > 1 ? "s" : "" ) );
	        }
	
	        uptimeInSeconds = uptimeInSeconds - ( hours * 3600 );
	        final long minutes = uptimeInSeconds / 60;
	
	        if ( minutes > 0 ) {
	        	
	            uptime.format( " %s minute%s", minutes, ( minutes > 1 ? "s" : "" ) );
	        }
	
	        uptimeInSeconds = uptimeInSeconds - ( minutes * 60 );
	
	        if ( uptimeInSeconds > 0 ) {
	        	
	            uptime.format( " %s second%s", uptimeInSeconds, ( uptimeInSeconds > 1 ? "s" : "" ) );
	        }
	
	        logger.info( uptime.toString() );
	        logger.info( "Finished at: {}", new Date() );
	
	        final Runtime runtime = getRuntime();
	        final int megaUnit = 1024 * 1024;
	        logger.info( "Final Memory: {}M/{}M", ( runtime.totalMemory() - runtime.freeMemory() ) / megaUnit,
	                     runtime.totalMemory() / megaUnit );
			
			logger.info( "------------------------------------------------------------------------" );
			
		}			
		
		return exit;
	}

	protected abstract void execute()
			throws Exception;
}
