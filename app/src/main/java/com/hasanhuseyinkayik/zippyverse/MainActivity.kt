package com.hasanhuseyinkayik.zippyverse

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hasanhuseyinkayik.zippyverse.activities.*
import com.hasanhuseyinkayik.zippyverse.network.*
import com.hasanhuseyinkayik.openrouterdeneme.network.RetrofitInstance
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val apiKey = "API_KEY"

    private val DarkColorScheme = darkColorScheme(
        primary = Color.White,
        onPrimary = Color.Black,
        background = Color.Black,
        surface = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White
    )

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val api = RetrofitInstance.getInstance(apiKey)

        val etkinlikMesaji = intent.getStringExtra("etkinlikMesaji")

        setContent {
            MaterialTheme(colorScheme = DarkColorScheme) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("ZippyVerse", color = Color.White) },
                            colors = TopAppBarDefaults.mediumTopAppBarColors(
                                containerColor = Color.Black
                            )
                        )
                    }
                ) { paddingValues ->

                    val systemPrompt = Message(
                        "system", """
                        Sen akıllı, bilgili ve Türkçe konuşan bir sohbet asistanısın.  
                        • Tüm sorulara doğru ve anlaşılır Türkçe cevap ver.
                        • Asla Çince veya başka dilde yanıt verme.
                        • Kısa mesajlar ile cevap vermeye çalış.
                        • Kullanıcıyla sıcak bir diyalog sürdür; gerektiğinde kısa açıklamalar yap.
                        • Kod, öneri veya analiz sorulduğunda adım adım anlat.
                    """.trimIndent()
                    )
                    val messages = remember { mutableStateListOf(systemPrompt) }
                    var userInput by remember { mutableStateOf("") }
                    var isLoading by remember { mutableStateOf(false) }
                    val coroutineScope = rememberCoroutineScope()
                    var isInitialMessageHandled by remember { mutableStateOf(false) }

                    LaunchedEffect(etkinlikMesaji) {
                        if (!isInitialMessageHandled && etkinlikMesaji != null) {
                            messages += Message("user", etkinlikMesaji)
                            isLoading = true
                            isInitialMessageHandled = true

                            try {
                                val req = ChatRequest(
                                    model = "deepseek/deepseek-chat-v3-0324:free",
                                    messages = messages.toList()
                                )
                                val res = api.getChatResponse(req)
                                val botMsg = res.choices.firstOrNull()?.message?.content
                                    ?: "Üzgünüm, cevap alınamadı."
                                messages += Message("assistant", botMsg)
                            } catch (e: Exception) {
                                messages += Message("assistant", "Hata: ${e.message}")
                            } finally {
                                isLoading = false
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .imePadding()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (messages.count { it.role != "system" } == 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Size ne önerebilirim?",
                                    fontSize = 18.sp,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                reverseLayout = true
                            ) {
                                items(messages.filter { it.role != "system" }.reversed()) { msg ->
                                    val isUser = msg.role == "user"
                                    val align = if (isUser) Arrangement.End else Arrangement.Start
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = align
                                    ) {
                                        Card(modifier = Modifier.padding(4.dp)) {
                                            Text(text = msg.content, modifier = Modifier.padding(8.dp))
                                        }
                                    }
                                }
                            }
                        }

                        if (isLoading) {
                            TypingIndicator()
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(onClick = {
                                    startActivity(Intent(this@MainActivity, OyunActivity::class.java))
                                }) { Text("Oyun") }
                                Button(onClick = {
                                    startActivity(Intent(this@MainActivity, AcikHavaActivity::class.java))
                                }) { Text("Açık Hava Etkinliği") }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(onClick = {
                                    startActivity(Intent(this@MainActivity, SarkiActivity::class.java))
                                }) { Text("Şarkı") }
                                Button(onClick = {
                                    startActivity(Intent(this@MainActivity, EtkinlikActivity::class.java))
                                }) { Text("Etkinlik") }
                                Button(onClick = {
                                    startActivity(Intent(this@MainActivity, VideoActivity::class.java))
                                }) { Text("Video") }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = userInput,
                                onValueChange = { userInput = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Mesajını yaz...") }
                            )
                            Button(
                                enabled = userInput.isNotBlank() && !isLoading,
                                onClick = {
                                    messages += Message("user", userInput)
                                    isLoading = true
                                    val prompt = userInput
                                    userInput = ""
                                    coroutineScope.launch {
                                        try {
                                            val req = ChatRequest(
                                                model = "deepseek/deepseek-chat-v3-0324:free",
                                                messages = messages.toList()
                                            )
                                            val res = api.getChatResponse(req)
                                            val botMsg = res.choices.firstOrNull()?.message?.content
                                                ?: "Üzgünüm, cevap alınamadı."
                                            messages += Message("assistant", botMsg)
                                        } catch (e: Exception) {
                                            messages += Message("assistant", "Hata: ${e.message}")
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            ) {
                                Text("Gönder")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    var dotCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            dotCount = (dotCount + 1) % 4
        }
    }

    val dots = ".".repeat(dotCount)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "Yazıyor$dots",
            fontSize = 16.sp,
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall
        )
    }
}