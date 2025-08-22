package com.quickjs.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quickjs.android.ui.theme.QuickJSAndroidAppTheme

class MainActivity : ComponentActivity() {
    
    companion object {
        init {
            System.loadLibrary("quickjs")
        }
    }
    
    private lateinit var quickJSBridge: QuickJSBridge
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        quickJSBridge = QuickJSBridge(this)
        
        setContent {
            QuickJSAndroidAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    QuickJSTestScreen(
                        quickJSBridge = quickJSBridge,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::quickJSBridge.isInitialized) {
            quickJSBridge.cleanup()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickJSTestScreen(
    quickJSBridge: QuickJSBridge,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Basic", "Advanced", "Remote JS", "Bytecode", "Examples")
    
    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        when (selectedTab) {
            0 -> BasicQuickJSTab(quickJSBridge)
            1 -> AdvancedQuickJSTab(quickJSBridge)
            2 -> RemoteJSTab(quickJSBridge)
            3 -> BytecodeTab(quickJSBridge)
            4 -> ExamplesTab(quickJSBridge)
        }
    }
}

@Composable
fun BasicQuickJSTab(quickJSBridge: QuickJSBridge) {
    var isQuickJSInitialized by remember { mutableStateOf(false) }
    var testResults by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var customScript by remember { mutableStateOf(getDefaultScript()) }
    var customResult by remember { mutableStateOf("") }
    var engineInfo by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "QuickJS JavaScript Engine",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "QuickJS Engine Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isQuickJSInitialized) "✅ Initialized" else "❌ Not Initialized",
                            color = if (isQuickJSInitialized) Color(0xFF4CAF50) else Color(0xFFF44336),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                isQuickJSInitialized = quickJSBridge.initialize()
                                engineInfo = quickJSBridge.getEngineInfo()
                            },
                            enabled = !isQuickJSInitialized,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Initialize")
                        }

                        Button(
                            onClick = {
                                quickJSBridge.cleanup()
                                isQuickJSInitialized = false
                                testResults = emptyMap()
                                customResult = ""
                                engineInfo = ""
                            },
                            enabled = isQuickJSInitialized,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cleanup")
                        }
                    }

                    if (engineInfo.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Engine Information",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = engineInfo)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Custom JavaScript Execution",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = customScript,
                        onValueChange = { customScript = it },
                        label = { Text("JavaScript Code (ES2023 supported)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                customResult = if (isQuickJSInitialized) {
                                    quickJSBridge.runJavaScript(customScript)
                                } else {
                                    "❌ Please initialize QuickJS first"
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Execute JavaScript")
                        }

                        Button(
                            onClick = {
                                customScript = getDefaultScript()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset to Default")
                        }
                    }

                    if (customResult.isNotEmpty()) {
                        ResultCard("Execution Result", customResult)
                    }
                }
            }
        }

        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "QuickJS Test Suite",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Button(
                        onClick = {
                            testResults = if (isQuickJSInitialized) {
                                quickJSBridge.runTestSuite()
                            } else {
                                mapOf("error" to "❌ Please initialize QuickJS first")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Run Complete Test Suite")
                    }

                    Button(
                        onClick = {
                            val memoryStats = if (isQuickJSInitialized) {
                                quickJSBridge.getMemoryStats()
                            } else {
                                "❌ Please initialize QuickJS first"
                            }
                            testResults = mapOf("memory_stats" to memoryStats)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Get Memory Statistics")
                    }

                    Button(
                        onClick = {
                            if (isQuickJSInitialized) {
                                val success = quickJSBridge.resetQuickJSContext()
                                testResults = mapOf("context_reset" to if (success) "✅ Context reset successful" else "❌ Context reset failed")
                            } else {
                                testResults = mapOf("context_reset" to "❌ Please initialize QuickJS first")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset Context")
                    }
                }
            }
        }

        items(testResults.toList()) { (testName, result) ->
            TestResultCard(testName, result)
        }
    }
}

@Composable
fun ResultCard(title: String, content: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun TestResultCard(testName: String, result: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = testName.uppercase().replace("_", " "),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
            Text(
                text = result,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Example JavaScript code
fun getDefaultScript() = """const sum = (a, b) => a + b; sum(15, 27)"""

fun getAsyncScript() = """
// Async/await example
async function delay(ms) {
    return new Promise(resolve => {
        setTimeout(() => resolve('Done!'), ms);
    });
}

delay(100).then(result => result);
""".trimIndent()

fun getAdvancedScript() = """
// Advanced ES2023 features
const data = [
    { name: 'Alice', age: 30, city: 'New York' },
    { name: 'Bob', age: 25, city: 'San Francisco' },
    { name: 'Charlie', age: 35, city: 'Chicago' }
];

const result = data
    .filter(person => person.age > 25)
    .map(({ name, city }) => `${'$'}{name} from ${'$'}{city}`)
    .join(', ');

result;
""".trimIndent()

fun getHttpScript() = """
// HTTP fetch example (requires polyfill)
fetch('https://jsonplaceholder.typicode.com/posts/1')
    .then(response => response.json())
    .then(data => data.title)
    .catch(error => 'Error: ' + error.message);
""".trimIndent()

@Composable
fun AdvancedQuickJSTab(quickJSBridge: QuickJSBridge) {
    var isQuickJSInitialized by remember { mutableStateOf(false) }
    var customScript by remember { mutableStateOf(getAdvancedScript()) }
    var customResult by remember { mutableStateOf("") }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Advanced JavaScript Features",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { customScript = getAdvancedScript() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ES2023 Features")
                        }
                        
                        Button(
                            onClick = { customScript = getAsyncScript() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Async/Await")
                        }
                        
                        Button(
                            onClick = { customScript = getHttpScript() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("HTTP Fetch")
                        }
                    }
                    
                    OutlinedTextField(
                        value = customScript,
                        onValueChange = { customScript = it },
                        label = { Text("Advanced JavaScript Code") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 6
                    )
                    
                    Button(
                        onClick = {
                            customResult = if (quickJSBridge.isQuickJSInitialized()) {
                                quickJSBridge.runJavaScript(customScript)
                            } else {
                                "❌ Please initialize QuickJS first"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Execute Advanced Script")
                    }
                    
                    if (customResult.isNotEmpty()) {
                        ResultCard("Advanced Execution Result", customResult)
                    }
                }
            }
        }
    }
}

@Composable
fun RemoteJSTab(quickJSBridge: QuickJSBridge) {
    var isExecuting by remember { mutableStateOf(false) }
    var progressMessage by remember { mutableStateOf("") }
    var executionResults by remember { mutableStateOf<List<QuickJSBridge.RemoteExecutionResult>>(emptyList()) }
    var remoteUrl by remember { mutableStateOf("") }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Remote JavaScript Execution",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Popular JavaScript Libraries",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    quickJSBridge.getPopularJavaScriptUrls().forEach { (name, url) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = { remoteUrl = url },
                                enabled = !isExecuting
                            ) {
                                Text("Use")
                            }
                        }
                    }
                }
            }
        }
        
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Remote JavaScript URL",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    OutlinedTextField(
                        value = remoteUrl,
                        onValueChange = { remoteUrl = it },
                        label = { Text("JavaScript URL (HTTP/HTTPS)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isExecuting
                    )
                    
                    if (progressMessage.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(
                                text = progressMessage,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (remoteUrl.isNotBlank() && quickJSBridge.isQuickJSInitialized()) {
                                    isExecuting = true
                                    progressMessage = ""
                                    
                                    quickJSBridge.executeRemoteJavaScript(remoteUrl, object : QuickJSBridge.RemoteExecutionCallback {
                                        override fun onProgress(message: String) {
                                            progressMessage = message
                                        }
                                        
                                        override fun onSuccess(result: QuickJSBridge.RemoteExecutionResult) {
                                            isExecuting = false
                                            progressMessage = "✅ Execution completed successfully!"
                                            executionResults = quickJSBridge.getExecutionHistory()
                                        }
                                        
                                        override fun onError(url: String, error: String) {
                                            isExecuting = false
                                            progressMessage = "❌ $error"
                                        }
                                    })
                                }
                            },
                            enabled = !isExecuting && remoteUrl.isNotBlank() && quickJSBridge.isQuickJSInitialized(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isExecuting) "Executing..." else "Execute Remote JS")
                        }
                        
                        Button(
                            onClick = {
                                isExecuting = true
                                progressMessage = ""
                                
                                quickJSBridge.testRemoteExecution(object : QuickJSBridge.RemoteExecutionCallback {
                                    override fun onProgress(message: String) {
                                        progressMessage = message
                                    }
                                    
                                    override fun onSuccess(result: QuickJSBridge.RemoteExecutionResult) {
                                        isExecuting = false
                                        progressMessage = "✅ Test completed successfully!"
                                        executionResults = quickJSBridge.getExecutionHistory()
                                    }
                                    
                                    override fun onError(url: String, error: String) {
                                        isExecuting = false
                                        progressMessage = "❌ $error"
                                    }
                                })
                            },
                            enabled = !isExecuting && quickJSBridge.isQuickJSInitialized(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Run Test")
                        }
                    }
                }
            }
        }
        
        items(executionResults) { result ->
            RemoteExecutionResultCard(result)
        }
    }
}

@Composable
fun BytecodeTab(quickJSBridge: QuickJSBridge) {
    var testScript by remember { mutableStateOf("const factorial = (n) => n <= 1 ? 1 : n * factorial(n - 1); factorial(5)") }
    var compilationResult by remember { mutableStateOf("") }
    var executionResult by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Bytecode Compilation & Execution",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "JavaScript to Bytecode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    OutlinedTextField(
                        value = testScript,
                        onValueChange = { testScript = it },
                        label = { Text("JavaScript Code for Compilation") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (quickJSBridge.isQuickJSInitialized()) {
                                    isProcessing = true
                                    compilationResult = ""
                                    executionResult = ""
                                    
                                    quickJSBridge.runBytecodeTest(object : QuickJSBridge.RemoteExecutionCallback {
                                        override fun onProgress(message: String) {
                                            compilationResult = message
                                        }
                                        
                                        override fun onSuccess(result: QuickJSBridge.RemoteExecutionResult) {
                                            isProcessing = false
                                            executionResult = "✅ Bytecode test successful!\nResult: ${result.result}\nTime: ${result.executionTimeMs}ms"
                                        }
                                        
                                        override fun onError(url: String, error: String) {
                                            isProcessing = false
                                            executionResult = "❌ Bytecode test failed: $error"
                                        }
                                    })
                                } else {
                                    executionResult = "❌ Please initialize QuickJS first"
                                }
                            },
                            enabled = !isProcessing,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isProcessing) "Processing..." else "Test Bytecode")
                        }
                        
                        Button(
                            onClick = {
                                if (quickJSBridge.isQuickJSInitialized()) {
                                    val bytecode = quickJSBridge.compileJavaScriptToBytecode(testScript)
                                    if (bytecode != null) {
                                        compilationResult = "✅ Compiled to ${bytecode.size} bytes of bytecode"
                                        val result = quickJSBridge.executeCompiledBytecode(bytecode)
                                        executionResult = if (result.success) {
                                            "✅ Bytecode execution successful!\nResult: ${result.result}\nTime: ${result.executionTimeMs}ms"
                                        } else {
                                            "❌ Bytecode execution failed: ${result.error}"
                                        }
                                    } else {
                                        compilationResult = "❌ Compilation failed"
                                    }
                                } else {
                                    executionResult = "❌ Please initialize QuickJS first"
                                }
                            },
                            enabled = !isProcessing,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Manual Test")
                        }
                    }
                    
                    if (compilationResult.isNotEmpty()) {
                        ResultCard("Compilation Result", compilationResult)
                    }
                    
                    if (executionResult.isNotEmpty()) {
                        ResultCard("Execution Result", executionResult)
                    }
                }
            }
        }
    }
}

@Composable
fun ExamplesTab(quickJSBridge: QuickJSBridge) {
    var selectedExample by remember { mutableStateOf("") }
    var executionResult by remember { mutableStateOf("") }
    
    val examples = mapOf(
        "Arrow Functions" to "const sum = (a, b) => a + b; sum(15, 27)",
        "Destructuring" to "const [a, b] = [10, 20]; const obj = {name: 'QuickJS'}; obj.name + ': ' + (a + b)",
        "Template Literals" to "const name = 'QuickJS'; const version = '2025-04-26'; 'Hello ' + name + ' v' + version + '!'",
        "Array Methods" to "[1, 2, 3, 4, 5].filter(x => x % 2 === 0).map(x => x * x).reduce((a, b) => a + b, 0)",
        "Object Spread" to "const obj1 = {a: 1, b: 2}; const obj2 = {c: 3}; const merged = {...obj1, ...obj2}; JSON.stringify(merged)",
        "Classes" to "class Calculator { add(a, b) { return a + b; } multiply(a, b) { return a * b; } } const calc = new Calculator(); calc.add(5, calc.multiply(3, 4))",
        "Promises" to "const delay = (ms) => new Promise(resolve => setTimeout(() => resolve('Done!'), ms)); delay(100).then(result => result)",
        "JSON Operations" to "const data = {users: [{name: 'Alice', age: 30}, {name: 'Bob', age: 25}]}; data.users.map(u => u.name).join(', ')"
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "JavaScript Examples",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Select an Example",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    examples.forEach { (name, code) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = {
                                    selectedExample = code
                                    executionResult = if (quickJSBridge.isQuickJSInitialized()) {
                                        quickJSBridge.runJavaScript(code)
                                    } else {
                                        "❌ Please initialize QuickJS first"
                                    }
                                }
                            ) {
                                Text("Run")
                            }
                        }
                    }
                }
            }
        }
        
        if (selectedExample.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Selected Code:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedExample,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
        
        if (executionResult.isNotEmpty()) {
            item {
                ResultCard("Example Result", executionResult)
            }
        }
    }
}

@Composable
fun RemoteExecutionResultCard(result: QuickJSBridge.RemoteExecutionResult) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (result.success) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = result.fileName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (result.success) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = if (result.success) "✅" else "❌",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            
            Text(
                text = "URL: ${result.url}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Size: ${result.contentLength} chars | Time: ${result.executionTimeMs}ms",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (result.result.isNotEmpty()) {
                Text(
                    text = "Result: ${result.result.take(200)}${if (result.result.length > 200) "..." else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}