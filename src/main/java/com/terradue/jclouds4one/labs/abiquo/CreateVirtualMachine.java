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

import java.util.List;

import com.terradue.jclouds4one.labs.BaseTool;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.collect.Lists;

import org.jclouds.ContextBuilder;
import org.jclouds.abiquo.AbiquoContext;
import org.jclouds.abiquo.domain.cloud.VirtualDatacenter;
import org.jclouds.abiquo.predicates.cloud.VirtualAppliancePredicates;
import org.jclouds.abiquo.predicates.cloud.VirtualDatacenterPredicates;
import org.jclouds.abiquo.predicates.cloud.VirtualMachineTemplatePredicates;
import org.jclouds.abiquo.predicates.network.NetworkPredicates;
import org.jclouds.abiquo.domain.cloud.VirtualMachine;
import org.jclouds.abiquo.domain.cloud.VirtualAppliance;
import org.jclouds.abiquo.domain.cloud.VirtualMachineTemplate;
import org.jclouds.abiquo.domain.enterprise.Enterprise;
import org.jclouds.abiquo.domain.infrastructure.Datacenter;
import org.jclouds.abiquo.domain.network.ExternalIp;
import org.jclouds.abiquo.domain.network.ExternalNetwork;
import org.jclouds.abiquo.domain.network.Ip;
import org.jclouds.abiquo.domain.network.PrivateIp;
import org.jclouds.abiquo.domain.network.PrivateNetwork;

@Parameters( commandDescription = "Creates a new Virtual Machine." )
public final class CreateVirtualMachine
	extends BaseTool {    
    
    @Parameter( names = {  "--datacenter" }, description = "The Virtual Data Center Name", required = true )
    private String virtualDataCenterName;
    
    @Parameter( names = { "--appliance" }, description = "The Virtual Appliance Name", required = true )    
    private String virtualApplianceName;
    
    @Parameter( names = { "--template" }, description = "The Virtual Machine Template Name", required = true )
    private String virtualMachineTemplateName;
    
    @Parameter( names = { "--label" }, description = "The Virtual Machine Label", required = true )
    private String virtualMachineLabel;
    
    @Parameter( names = { "--cpu" }, description = "The number of virtual CPUs", required = true )
    private Integer cpu;
    
    @Parameter( names = { "--memory" }, description = "The RAM amount (in Mb)", required = true )
    private Integer memory;
    
    @Parameter( names = { "--externalnetwork" }, description = "The External Network Name", required = true )
    private String externalnetwork;
    
    @Parameter( names = { "--privatenetwork" }, description = "The Private Network Name" )
    private String privatenetwork = "";
    
    
    public static void main( String[] args ) {
    	
        exit( new CreateVirtualMachine().execute( args ) );
    }
       
    @Override
    public void execute() throws Exception {
    	
    	AbiquoContext context = ContextBuilder.newBuilder( "abiquo" )
    										  .endpoint( serviceApi )
                                              .credentials( identity , credential )
                                              .buildView( AbiquoContext.class );
    	    	
    	logger.info( "Creating Virtual Machine..." );
        
        // Get the virtual datacenter where the virtual machine will be deployed
        VirtualDatacenter vdc = context.getCloudService().findVirtualDatacenter(
        		VirtualDatacenterPredicates.name( virtualDataCenterName ) );
        
        if ( vdc == null ) {
        	
        	throw new NullPointerException("The specified Virtual Data Center does not exists.");
        }
        
        VirtualAppliance vapp;
               
        // If already created, just find it
        vapp = vdc.findVirtualAppliance(VirtualAppliancePredicates.name( virtualApplianceName ) );
        
        if (vapp == null) {
        	// Create a new virtual appliance to group the desired virtual machines
            vapp = VirtualAppliance.builder(context.getApiContext(), vdc)
            		                                .name( virtualApplianceName ) // The name for the virtual appliance
            		                                .build();
            vapp.save();
        }
        	
        // Get the template to use from the templates available to the virtual datacenter
        VirtualMachineTemplate template = vdc.findAvailableTemplate(
            VirtualMachineTemplatePredicates.name( virtualMachineTemplateName ) );
        
        if ( template == null ) {
        	
        	throw new NullPointerException("The specified Virtual Machine Template does not exists.");
        }
        
        // Create the virtual machine
        VirtualMachine vm = VirtualMachine.builder(context.getApiContext(), vapp, template)
        		.nameLabel( virtualMachineLabel ) // The label of the virtual machine
            	.cpu( cpu )          // The number of CPUs
            	.ram( memory )       // The amount of RAM in MB
            	.build();
        
        vm.save();
        
        
        List<Ip<?, ?>> ips = Lists.<Ip< ? , ? >> newArrayList();
        
        // Get an unused IP from the external network
	    Enterprise enterprise = vdc.getEnterprise();
	    Datacenter datacenter = vdc.getDatacenter();
	    ExternalNetwork extNet = enterprise.findExternalNetwork(datacenter,
	            NetworkPredicates.<ExternalIp> name(externalnetwork));
	    ExternalIp extIp = extNet.listUnusedIps().get(0);
	    	    	    
	    ips.add(extIp);
	    
	    // Get an unused IP from the private network, if specified
	    
	    if ( !privatenetwork.isEmpty() ) {
	    	
	    	PrivateNetwork privNet = vdc.findPrivateNetwork(
	    		    NetworkPredicates.<PrivateIp> name(privatenetwork));
	    	PrivateIp privIp = privNet.listUnusedIps().get(0);
	    	ips.add(privIp);
	    }
	    	    
	    vm.setNics( ips );
        
        vm.deploy();
        
        logger.info( "" );
        logger.info( "Internal Name: " + vm.getInternalName() );
        
        // At this point a deployment job has been started asynchronously and the 
        // virtual machine will be deployed in the background    	   	
    }   

}