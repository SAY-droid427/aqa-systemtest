/*******************************************************************************
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

package net.adoptopenjdk.stf;

import net.adoptopenjdk.loadTest.InventoryData;
import net.adoptopenjdk.loadTest.TimeBasedLoadTest;
import net.adoptopenjdk.stf.extensions.core.StfCoreExtension;
import net.adoptopenjdk.stf.extensions.core.StfCoreExtension.Echo;
import net.adoptopenjdk.stf.processes.ExpectedOutcome;
import net.adoptopenjdk.stf.processes.definitions.JavaProcessDefinition;
import net.adoptopenjdk.stf.processes.definitions.LoadTestProcessDefinition;
import net.adoptopenjdk.stf.runner.modes.HelpTextGenerator;

public class ConcurrentLoadTest extends TimeBasedLoadTest {
	public void help(HelpTextGenerator help) throws StfException {
		help.outputSection("ConcurrentLoadTest runs concurrency unit tests");
		help.outputText("");
	}

	public void execute(StfCoreExtension test) throws StfException {
		String inventoryFile = "/openjdk.test.load/config/inventories/concurrent/concurrent.xml";
		int totalTests = InventoryData.getNumberOfTests(test, inventoryFile);
		int cpuCount = Runtime.getRuntime().availableProcessors();

		LoadTestProcessDefinition loadTestInvocation = test.createLoadTestSpecification()
				.addPrereqJarToClasspath(JavaProcessDefinition.JarId.JUNIT)
				.addPrereqJarToClasspath(JavaProcessDefinition.JarId.HAMCREST)
				.addProjectToClasspath("openjdk.test.concurrent");
		
		if (isTimeBasedLoadTest) { 
			loadTestInvocation = loadTestInvocation.setTimeLimit(timeLimit); // If it's a time based test, stop execution after given time duration
		}
		
		loadTestInvocation = loadTestInvocation.setAbortIfOutOfMemory(false)
				.addSuite("concurrent")
				.setSuiteThreadCount(cpuCount - 2, 20)	  	// Leave 1 cpu for the JIT. i for GC and set min 20
				.setSuiteInventory(inventoryFile);			// Point at the file which lists the tests
				
		if (!isTimeBasedLoadTest) { 		
			loadTestInvocation = loadTestInvocation.setSuiteNumTests(totalTests * 20); // Run for about 2 minutes with no -X options
		}
		
		loadTestInvocation = loadTestInvocation.setSuiteRandomSelection(); // Randomly pick the next test each time
		
		test.doRunForegroundProcess("Run concurrency unit tests", "LT", Echo.ECHO_ON,
				ExpectedOutcome.cleanRun().within(finalTimeout), 
				loadTestInvocation);
	}
}
