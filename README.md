# ZippyVerse
ZippyVerse, ebeveynler ve çocukları için eğlenceli etkinlik fikirleri sunan yapay zeka tabanlı bir mobil uygulamadır. 

Kotlin ile geliştirilmiştir, yapay zeka modeli olarak "*deepseek-chat-v3-0324:free*" modeli kullanılmıştır.

<p align="center">
  <img src="app/src/main/res/drawable/screen1.jpeg" width="188" height="408">
  <img src="app/src/main/res/drawable/screen2.jpeg" width="188" height="408">
  <img src="app/src/main/res/drawable/screen3.jpeg" width="188" height="408">
  <img src="app/src/main/res/drawable/screen4.jpeg" width="188" height="408">
</p>

*Görsel 1*

## Kurulum
Öncelikle, uygulamayı çalıştırmak için Android Studio veya benzeri bir editörde kurulumu tamamlamalı ve [OpenRouter.ai](https://openrouter.ai/) web sitesinden bir API anahtarı almalısınız. API anahtarı almak için ücretsiz bir hesap 
oluşturmalısınız (Google hesabınızla da kayıt olabilirsiniz). 

Ardından kod dosyalarınız içinde yer alan *AcikHavaActivity.kt*, *EtkinlikActivity.kt*, *OyunActivity.kt*, *SarkiActivity.kt*, *VideoActivity.kt* ve *MainActivity.kt* dosyalarında "API_KEY" olarak tanımlanan 
değişkenlere keyinizi string olarak yapıştırın. (keyiniz genelde "*sk*" ile başlar.)

Uygulama içinde ücretsiz bir yapay zeka modeli kullanıldığı için herhangi bir ücret kesilmesi durumu olmayacaktır. Modeli değiştirmek isterseniz Ek Bilgiler & Düzenleme başlığına inceleyebilirsiniz.

![Görsel 2](app/src/main/res/drawable/image1.png)

*Görsel 2*

Bu noktada emülatörü çalıştırdığınızda uygulama çalışacaktır.

# Ek Bilgiler & Düzenleme
Kodlar yazılırken ücretsiz bir model olan "*deepseek-chat-v3-0324:free*" modeli kullanıldı, bu modelin dokümanlarına [buradan](https://openrouter.ai/deepseek/deepseek-chat-v3-0324:free) ulaşabilirsiniz.

Openrouter.ai üzerinden erişebileceğiniz birçok ücretli ve ücretsiz model bulunuyor, tüm modellerin listesine [buradan](https://openrouter.ai/models) ulaşabilirsiniz. 

Eğer kodlar üzerinde değişiklik yaparak modeli değiştirmek isterseniz; bunun için *AcikHavaActivity.kt*, *EtkinlikActivity.kt*, *OyunActivity.kt*, *SarkiActivity.kt*, *VideoActivity.kt* ve *MainActivity.kt* 
dosyalarının içinde yer alan "model" değişkenini ilgili modelin dokümanında yer alan kod satırı ile değiştirmeniz yeterli olacaktır. (*Görsel 3*'de gösterilmiştir.)

Ancak bu uygulama yazılırken "*deepseek-chat-v3-0324:free*" modeli kullanıldığı için çeşitli düzenlemeler yapmanız gerekebilir.

![Görsel 3](app/src/main/res/drawable/image2.png)

*Görsel 3*
