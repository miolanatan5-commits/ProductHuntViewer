# ===================================================================
# 1. SEGURANÇA & CRIPTOGRAFIA (androidx.security.crypto / Tink)
# ===================================================================
# Silencia os erros de anotações ausentes que travaram o seu build anterior
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-dontwarn org.checkerframework.**

# --- [AQUI ESTAVA O PROBLEMA] ---
# O Tink referencia opcionalmente o Google HTTP Client e JodaTime para download de chaves.
# Como você não usa essa funcionalidade, precisamos dizer pro R8 não surtar com essas classes faltantes:
-dontwarn com.google.api.client.**
-dontwarn org.joda.time.**
# --------------------------------

# Mantém as classes internas do Tink intactas para o MasterKey/EncryptedSharedPreferences
-keep class com.google.crypto.tink.** { *; }
-keep interface com.google.crypto.tink.** { *; }

# Regra oficial do Tink (META-INF/proguard/protobuf.pro): o protobuf-lite
# shaded usado pelo Tink lê os campos das classes geradas via reflexão em
# tempo de execução, então precisa que os nomes dos campos sobrevivam ao R8 —
# sem isso o app cai com ExceptionInInitializerError assim que tenta ler/criar
# o EncryptedSharedPreferences.
-keep class * extends com.google.crypto.tink.shaded.protobuf.GeneratedMessageLite { *; }
-keepclassmembers class * extends com.google.crypto.tink.shaded.protobuf.GeneratedMessageLite {
    <fields>;
}
-dontwarn com.google.crypto.tink.shaded.protobuf.**

# ===================================================================
# 2. REDE (OkHttp & Okio)
# ===================================================================
# Evita avisos de dependências opcionais do OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**

# Mantém os nomes das classes de rede para que interceptores e seletores de cifra funcionem
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ===================================================================
# 3. MODELOS DE DADOS DO PRODUCT HUNT (Data Classes & Serialização)
# ===================================================================
# Impede que o R8 remova ou renomeie os campos do seu objeto Post ao ler o JSON/GraphQL
-keep class com.example.producthunt.data.** { *; }
-keepclassmembers class com.example.producthunt.data.** { *; }

# Preserve anotações de serialização (Gson/Moshi/Kotlinx Serialization se usar alguma)
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# ===================================================================
# 4. JETPACK COMPOSE & COIL
# ===================================================================
# Mantém os composables e o carregador de imagens do Coil sem sobressaltos
-keep class coil.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

