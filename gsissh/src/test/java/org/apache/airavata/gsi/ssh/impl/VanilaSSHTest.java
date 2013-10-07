/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.gsi.ssh.impl;

import com.jcraft.jsch.UserInfo;
import org.apache.airavata.gsi.ssh.api.*;
import org.apache.airavata.gsi.ssh.api.authentication.AuthenticationInfo;
import org.apache.airavata.gsi.ssh.api.job.JobDescriptor;
import org.apache.airavata.gsi.ssh.config.ConfigReader;
import org.apache.airavata.gsi.ssh.impl.authentication.DefaultPasswordAuthenticationInfo;
import org.apache.airavata.gsi.ssh.impl.authentication.DefaultPublicKeyFileAuthentication;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 9/20/13
 * Time: 12:05 PM
 */

public class VanilaSSHTest {

    private String userName;
    private String password;
    private String passPhrase;
    private String hostName;

    @BeforeTest
    public void setUp() throws Exception {


        this.hostName = "bigred2.uits.iu.edu";
        System.setProperty("my.ssh.user", "lginnali");
        System.setProperty("my.ssh.user.password","");
//        System.setProperty("my.ssh.user","i want to be free");
        this.userName = System.getProperty("my.ssh.user");
        this.password = System.getProperty("my.ssh.user.password");
//        this.passPhrase = System.getProperty("my.ssh.user.pass.phrase");

        if (this.userName == null || this.password == null) {
            System.out.println("########### In order to run tests you need to set password and " +
                    "passphrase ###############");
            System.out.println("Use -Dmy.ssh.user=xxx -Dmy.ssh.user.password=yyy -Dmy.ssh.user.pass.phrase=zzz");
        }

    }


    @Test
    public void testSimpleCommand1() throws Exception{

        System.out.println("Starting vanila SSH test ....");

        AuthenticationInfo authenticationInfo = new DefaultPasswordAuthenticationInfo(this.password);

        // Create command
        CommandInfo commandInfo = new RawCommandInfo("/opt/torque/torque-4.2.3.1/bin/qstat");

        // Server info
        ServerInfo serverInfo = new ServerInfo(this.userName, this.hostName);

        // Output
        CommandOutput commandOutput = new SystemCommandOutput();

        // Execute command
        CommandExecutor.executeCommand(commandInfo, serverInfo, authenticationInfo, commandOutput, new ConfigReader());


    }

    @Test
    public void testSimpleCommand2() throws Exception{

        System.out.println("Starting vanila SSH test ....");

        String privateKeyFile = "/Users/lahirugunathilake/.ssh/id_dsa";
        String publicKeyFile = "/Users/lahirugunathilake/.ssh/id_dsa.pub";

        AuthenticationInfo authenticationInfo = new DefaultPublicKeyFileAuthentication(publicKeyFile, privateKeyFile,
                this.passPhrase);

        // Create command
        CommandInfo commandInfo = new RawCommandInfo("/opt/torque/torque-4.2.3.1/bin/qstat");

        // Server info
        ServerInfo serverInfo = new ServerInfo(this.userName, this.hostName);

        // Output
        CommandOutput commandOutput = new SystemCommandOutput();

        // Execute command
        CommandExecutor.executeCommand(commandInfo, serverInfo, authenticationInfo, commandOutput, new ConfigReader());


    }

    @Test
    public void testSimplePBSJob() throws Exception{

        AuthenticationInfo authenticationInfo = new DefaultPasswordAuthenticationInfo(this.password);
         // Server info
        ServerInfo serverInfo = new ServerInfo(this.userName, this.hostName);
        Cluster pbsCluster = new PBSCluster(serverInfo, authenticationInfo, "/opt/torque/torque-4.2.3.1/bin/");


        // Execute command
        String workingDirectory = File.separator + "N" + File.separator + "u" + File.separator +
                "lginnali" + File.separator + "BigRed2" + File.separator + "myjob";
        // constructing the job object
        JobDescriptor jobDescriptor = new JobDescriptor();
        jobDescriptor.setWorkingDirectory(workingDirectory);
        jobDescriptor.setShellName("/bin/bash");
        jobDescriptor.setJobName("GSI_SSH_SLEEP_JOB");
        jobDescriptor.setExecutablePath("/bin/echo");
        jobDescriptor.setAllEnvExport(true);
        jobDescriptor.setMailOptions("n");
        jobDescriptor.setStandardOutFile(workingDirectory + File.separator + "application.out");
        jobDescriptor.setStandardErrorFile(workingDirectory + File.separator + "application.err");
        jobDescriptor.setNodes(1);
        jobDescriptor.setProcessesPerNode(1);
        jobDescriptor.setQueueName("normal");
        jobDescriptor.setMaxWallTime("5");
        jobDescriptor.setJobSubmitter("aprun -n 1");
        List<String> inputs = new ArrayList<String>();
        inputs.add("Hello World");
        jobDescriptor.setInputValues(inputs);
        //finished construction of job object
        System.out.println(jobDescriptor.toXML());
        String jobID = pbsCluster.submitBatchJob(jobDescriptor);
        System.out.println("JobID returned : " + jobID);

//        Cluster cluster = sshApi.getCluster(serverInfo, authenticationInfo);
        Thread.sleep(1000);
        JobDescriptor jobById = pbsCluster.getJobDescriptorById(jobID);

        //printing job data got from previous call
        AssertJUnit.assertEquals(jobById.getJobId(), jobID);
        System.out.println(jobById.getAcountString());
        System.out.println(jobById.getAllEnvExport());
        System.out.println(jobById.getCompTime());
        System.out.println(jobById.getExecutablePath());
        System.out.println(jobById.getEllapsedTime());
        System.out.println(jobById.getQueueName());
        System.out.println(jobById.getExecuteNode());
        System.out.println(jobById.getJobName());
        System.out.println(jobById.getCTime());
        System.out.println(jobById.getSTime());
        System.out.println(jobById.getMTime());
        System.out.println(jobById.getCompTime());
        System.out.println(jobById.getOwner());
        System.out.println(jobById.getQTime());
        System.out.println(jobById.getUsedCPUTime());
        System.out.println(jobById.getUsedMemory());
        System.out.println(jobById.getVariableList());
    }

}
