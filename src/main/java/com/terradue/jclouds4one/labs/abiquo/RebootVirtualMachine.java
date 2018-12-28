package com.terradue.jclouds4one.labs.abiquo;

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

import static java.lang.System.exit;

import com.terradue.jclouds4one.labs.BaseTool;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import org.jclouds.ContextBuilder;
import org.jclouds.abiquo.AbiquoContext;
import org.jclouds.abiquo.domain.cloud.VirtualDatacenter;
import org.jclouds.abiquo.predicates.cloud.VirtualAppliancePredicates;
import org.jclouds.abiquo.predicates.cloud.VirtualDatacenterPredicates;
import org.jclouds.abiquo.predicates.cloud.VirtualMachinePredicates;
import org.jclouds.abiquo.domain.cloud.VirtualMachine;
import org.jclouds.abiquo.domain.cloud.VirtualAppliance;

@Parameters( commandDescription = "Reboot an existing Virtual Machine." )
public final class RebootVirtualMachine
	extends BaseTool {    
    
    @Parameter( names = {  "--datacenter" }, description = "The Virtual Data Center Name", required = true )
    private String virtualDataCenterName;
    
    @Parameter( names = { "--appliance" }, description = "The Virtual Appliance Name", required = true )    
    private String virtualApplianceName;
    
    @Parameter( names = { "--internalName" }, description = "The Virtual Machine Internal Name", required = true )
    private String virtualMachineInternalName;  
    
    public static void main( String[] args ) {
    	
        exit( new RebootVirtualMachine().execute( args ) );
    }
       
    @Override
    public void execute() throws Exception {
    	
    	AbiquoContext context = ContextBuilder.newBuilder( "abiquo" )
                                              .endpoint( serviceApi )
                                              .credentials( identity , credential )
                                              .buildView( AbiquoContext.class );
    	    	
    	logger.info( "Rebooting Virtual Machine..." );
        
        VirtualDatacenter vdc = context.getCloudService().findVirtualDatacenter(
        		VirtualDatacenterPredicates.name( virtualDataCenterName ) );
        
        VirtualAppliance vapp = vdc.findVirtualAppliance( 
        		VirtualAppliancePredicates.name( virtualApplianceName ) );
        
        VirtualMachine vm = vapp.findVirtualMachine( 
        		VirtualMachinePredicates.internalName( virtualMachineInternalName ) );
        
        if ( vm != null ) {
        	
        	org.jclouds.abiquo.domain.task.AsyncTask task = vm.reboot();
        	
        	if ( task.getState().name().equals("FINISHED_UNSUCCESSFULLY") ) {
        		
        		throw new Exception("Unsuccessful reboot, please contact service provider");
        	}
        }
        else {
        	
        	throw new NullPointerException("The specified Virtual Machine Internal Name does not exists.");
        }   	   	
    }   

}