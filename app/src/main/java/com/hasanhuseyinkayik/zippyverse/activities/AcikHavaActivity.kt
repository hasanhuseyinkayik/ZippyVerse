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

class AcikHavaActivity : ComponentActivity() {

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
                AcikHavaOnerisiEkrani(openRouterApi = openRouterApi)
            }
        }
    }
}

@Composable
fun AcikHavaOnerisiEkrani(openRouterApi: com.hasanhuseyinkayik.zippyverse.network.OpenRouterApi) {
    var acikHavaOnerisi by remember { mutableStateOf<String?>(null) }
    var acikHavaAciklamasi by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val oncekiOneriler = remember { mutableStateListOf<String>() }
    val coroutineScope = rememberCoroutineScope()

    suspend fun fetchAcikHavaOnerisi() {
        isLoading = true
        acikHavaOnerisi = null
        acikHavaAciklamasi = null
        errorMessage = null

        try {
            var tekrarOnerildi = true
            var denemeSayisi = 0
            val maxDeneme = 5

            while (tekrarOnerildi && denemeSayisi < maxDeneme) {
                denemeSayisi++

                val systemPrompt = Message(
                    "system", """
                    Sen bir çocuklara yönelik açık hava etkinliği uzmanısın.
                    Tüm sorulara doğru ve anlaşılır Türkçe cevap ver.
                    Asla başka dilde yanıt verme.
                    Yalnızca tek bir açık hava etkinliği adı öner.
                    Ardından açık hava etkinliği hakkında 2 cümlelik kısa bir açıklama yap.
                    Bu açıklama açık hava etkinliğini hiç bilmeyen birine anlatabilecek düzeyde olsun.
                    Önerilen açık hava etkinliği, dışarıda ailecek yapılabilecek bir açık hava etkinliği olmalı.
                    Format şu şekilde olmalı:
                    Açık Hava Etkinliği Adı: [Açık Hava Etkinliği İsmi]
                    Açıklama: [2 cümlelik kısa bilgi]
                """.trimIndent()
                )

                val userPrompt = Message(
                    "user", "Ailecek dışarıda yapılabilecek, basit ve eğlenceli tek bir açık hava etkinliği önerisi verir misin?"
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
                    val acikHavaAdiSatiri = lines.firstOrNull { it.startsWith("Açık Hava Etkinliği Adı:") }
                    val aciklamaSatiri = lines.firstOrNull { it.startsWith("Açıklama:") }

                    val yeniAcikHavaAdi = acikHavaAdiSatiri?.removePrefix("Açık Hava Etkinliği Adı:")?.trim()

                    if (!yeniAcikHavaAdi.isNullOrBlank() && !oncekiOneriler.contains(yeniAcikHavaAdi)) {
                        acikHavaOnerisi = yeniAcikHavaAdi
                        acikHavaAciklamasi = aciklamaSatiri?.removePrefix("Açıklama:")?.trim()

                        oncekiOneriler.add(0, yeniAcikHavaAdi)
                        if (oncekiOneriler.size > 10) {
                            oncekiOneriler.removeAt(oncekiOneriler.lastIndex)
                        }

                        tekrarOnerildi = false
                    }
                }
            }

            if (tekrarOnerildi) {
                errorMessage = "Farklı bir açık hava etkinliği önerisi alınamadı. Lütfen tekrar deneyin."
            }

        } catch (e: Exception) {
            errorMessage = "İstek sırasında bir hata oluştu: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            fetchAcikHavaOnerisi()
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
                text = if (acikHavaOnerisi != null) "Sizin için etkinlik önerim:" else "Etkinlik Önerisi",
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
            } else if (acikHavaOnerisi != null) {
                Text(
                    text = acikHavaOnerisi!!,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                if (!acikHavaAciklamasi.isNullOrBlank()) {
                    Text(
                        text = acikHavaAciklamasi!!,
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
                        fetchAcikHavaOnerisi()
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