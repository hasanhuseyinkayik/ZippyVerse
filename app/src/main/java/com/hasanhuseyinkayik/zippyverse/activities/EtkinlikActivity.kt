package com.hasanhuseyinkayik.zippyverse.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hasanhuseyinkayik.zippyverse.network.ChatRequest
import com.hasanhuseyinkayik.zippyverse.network.Message
import com.hasanhuseyinkayik.openrouterdeneme.network.RetrofitInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EtkinlikActivity : ComponentActivity() {

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
                EtkinlikOnerisiEkrani(openRouterApi = openRouterApi)
            }
        }
    }
}

@Composable
fun EtkinlikOnerisiEkrani(openRouterApi: com.hasanhuseyinkayik.zippyverse.network.OpenRouterApi) {
    var etkinlikOnerisi by remember { mutableStateOf<String?>(null) }
    var etkinlikAciklamasi by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Son 10 öneriyi tutan liste
    val oncekiOneriler = remember { mutableStateListOf<String>() }
    val coroutineScope = rememberCoroutineScope()

    // Öneri alma işlemi fonksiyon haline getirildi
    suspend fun fetchEtkinlikOnerisi() {
        isLoading = true
        etkinlikOnerisi = null
        etkinlikAciklamasi = null
        errorMessage = null

        try {
            var tekrarOnerildi = true
            var denemeSayisi = 0
            val maxDeneme = 5

            while (tekrarOnerildi && denemeSayisi < maxDeneme) {
                denemeSayisi++

                val systemPrompt = Message(
                    "system", """
                    Sen bir çocuk etkinlikleri uzmanısın.
                    Tüm sorulara doğru ve anlaşılır Türkçe cevap ver.
                    Asla başka dilde yanıt verme.
                    Yalnızca tek bir etkinlik adı öner.
                    Ardından etkinlik hakkında 2 cümlelik kısa bir açıklama yap.
                    Bu açıklama etkinliği hiç bilmeyen birine anlatabilecek düzeyde olsun.
                    Önerilen etkinlik dijital olmayan, dışarıda veya evde gerçekleştirilebilen bir etkinlik olmalı.
                    Format şu şekilde olmalı:
                    Etkinlik Adı: [Etkinlik İsmi]
                    Açıklama: [2 cümlelik kısa bilgi]
                """.trimIndent()
                )

                val userPrompt = Message(
                    "user", "Çocukların dışarıda veya evde oynayabileceği, dijital olmayan, basit ve eğlenceli tek bir etkinlik önerisi verir misin?"
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
                    val etkinlikAdiSatiri = lines.firstOrNull { it.startsWith("Etkinlik Adı:") }
                    val aciklamaSatiri = lines.firstOrNull { it.startsWith("Açıklama:") }

                    val yeniEtkinlikAdi = etkinlikAdiSatiri?.removePrefix("Etkinlik Adı:")?.trim()

                    if (!yeniEtkinlikAdi.isNullOrBlank() && !oncekiOneriler.contains(yeniEtkinlikAdi)) {
                        etkinlikOnerisi = yeniEtkinlikAdi
                        etkinlikAciklamasi = aciklamaSatiri?.removePrefix("Açıklama:")?.trim()

                        // Listeye yeni öneriyi ekle
                        oncekiOneriler.add(0, yeniEtkinlikAdi)
                        if (oncekiOneriler.size > 10) {
                            oncekiOneriler.removeAt(oncekiOneriler.lastIndex)
                        }

                        tekrarOnerildi = false
                    }
                }
            }

            if (tekrarOnerildi) {
                errorMessage = "Farklı bir etkinlik önerisi alınamadı. Lütfen tekrar deneyin."
            }

        } catch (e: Exception) {
            errorMessage = "İstek sırasında bir hata oluştu: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    // Sayfa açılır açılmaz öneri isteği
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            fetchEtkinlikOnerisi()
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
                text = if (etkinlikOnerisi != null) "Sizin için etkinlik önerim:" else "Etkinlik Önerisi",
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
            } else if (etkinlikOnerisi != null) {
                Text(
                    text = etkinlikOnerisi!!,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                if (!etkinlikAciklamasi.isNullOrBlank()) {
                    Text(
                        text = etkinlikAciklamasi!!,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
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
                        fetchEtkinlikOnerisi()
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
