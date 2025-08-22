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
    var isQuickJSInitialized by remember { mutableStateOf(false) }
    var testResults by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var customScript by remember { mutableStateOf(getDefaultScript()) }
    var customResult by remember { mutableStateOf("") }
    var engineInfo by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
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