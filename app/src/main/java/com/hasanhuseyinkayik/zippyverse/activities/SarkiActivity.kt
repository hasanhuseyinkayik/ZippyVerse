package com.hasanhuseyinkayik.zippyverse.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hasanhuseyinkayik.zippyverse.R
import com.hasanhuseyinkayik.zippyverse.network.ChatRequest
import com.hasanhuseyinkayik.zippyverse.network.Message
import com.hasanhuseyinkayik.openrouterdeneme.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SarkiActivity : ComponentActivity() {
    private val OPENROUTER_API_KEY = "API_KEY"
    private lateinit var openRouterApi: com.hasanhuseyinkayik.zippyverse.network.OpenRouterApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openRouterApi = RetrofitInstance.getInstance(OPENROUTER_API_KEY)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color.Black,
                    onBackground = Color.White
                )
            ) {
                SarkiOnerisiEkrani(openRouterApi = openRouterApi)
            }
        }
    }
}

@Composable
fun SarkiOnerisiEkrani(openRouterApi: com.hasanhuseyinkayik.zippyverse.network.OpenRouterApi) {
    var sarkiOnerisi by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val oncekiOneriler = remember { mutableStateListOf<String>() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    suspend fun fetchSarkiOnerisi() {
        isLoading = true
        sarkiOnerisi = null
        errorMessage = null

        try {
            var tekrarOnerildi = true
            var denemeSayisi = 0
            val maxDeneme = 5

            while (tekrarOnerildi && denemeSayisi < maxDeneme) {
                denemeSayisi++

                val systemPrompt = Message(
                    "system", """
                    Sen bir çocuk şarkıları uzmanısın.
                    Tüm sorulara doğru ve anlaşılır Türkçe cevap ver.
                    Asla başka dilde yanıt verme.
                    Yalnızca tek bir şarkı adı öner.
                    Format şu şekilde olmalı:
                    Şarkı Adı: [Şarkı İsmi]
                """.trimIndent()
                )

                val userPrompt = Message(
                    "user", "Çocuklara yönelik eğitici ve öğretici Türkçe tek bir şarkı önerisi verir misin?"
                )

                val request = ChatRequest(
                    model = "deepseek/deepseek-chat-v3-0324:free",
                    messages = listOf(systemPrompt, userPrompt)
                )

                val response = withContext(Dispatchers.IO) {
                    openRouterApi.getChatResponse(request)
                }

                val suggestedText = response.choices.firstOrNull()?.message?.content
                if (!suggestedText.isNullOrBlank()) {
                    val lines = suggestedText.trim().split("\n").map { it.trim() }
                    val sarkiAdiSatiri = lines.firstOrNull { it.startsWith("Şarkı Adı:") }
                    val yeniSarkiAdi = sarkiAdiSatiri?.removePrefix("Şarkı Adı:")?.trim()

                    if (!yeniSarkiAdi.isNullOrBlank() && !oncekiOneriler.contains(yeniSarkiAdi)) {
                        sarkiOnerisi = yeniSarkiAdi
                        oncekiOneriler.add(0, yeniSarkiAdi)
                        if (oncekiOneriler.size > 10) {
                            oncekiOneriler.removeAt(oncekiOneriler.lastIndex)
                        }
                        tekrarOnerildi = false
                    }
                }
            }

            if (tekrarOnerildi) {
                errorMessage = "Farklı bir şarkı önerisi alınamadı. Lütfen tekrar deneyin."
            }

        } catch (e: Exception) {
            errorMessage = "İstek sırasında bir hata oluştu: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            fetchSarkiOnerisi()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (sarkiOnerisi != null) "Sizin için şarkı önerim:" else "Şarkı Önerisi",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading) {
                Text(
                    text = "Yapay Zeka Sizin İçin Düşünüyor...",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
            } else if (sarkiOnerisi != null) {
                Text(
                    text = sarkiOnerisi!!,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // 🎬 YouTube Linki Kutusu
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.DarkGray)
                        .clickable {
                            val query = Uri.encode("${sarkiOnerisi!!} Çocuk Şarkısı")
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.youtube.com/results?search_query=$query")
                            )
                            context.startActivity(intent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // 🎥 YouTube Logosu
                        Icon(
                            painter = painterResource(id = R.drawable.youtube_logo),
                            contentDescription = "YouTube Logosu",
                            modifier = Modifier
                                .size(32.dp)
                                .padding(end = 8.dp),
                            tint = Color.Unspecified // or Color.White if PNG isn't colored
                        )

                        // 🎵 Metin
                        Text(
                            text = "YouTube'da Dinle",
                            color = Color.White,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

            } else if (errorMessage != null) {
                Text(
                    text = "Hata: ${errorMessage!!}",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        fetchSarkiOnerisi()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
            ) {
                Text(text = "Başka Bir Öneri Al", fontSize = 20.sp, color = Color.White)
            }
        }
    }
}
