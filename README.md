# Product Hunt Viewer

Aplicativo Android para explorar lançamentos do Product Hunt, filtrar publicações e navegar pelos detalhes. O app solicita o developer token do Product Hunt na primeira utilização e o armazena localmente com proteção criptográfica do Android.

## APKs de release

Os APKs disponibilizados em `apk/release/` são builds de release para diferentes arquiteturas, além do APK universal. Para a maioria dos aparelhos Android, use `app-universal-release.apk`.

## Como compilar

1. Abra o projeto no Android Studio ou execute `./gradlew assembleDebug`.
2. Para gerar uma versão de release assinada com uma chave própria, configure `release.properties` localmente e coloque o arquivo de keystore na raiz. Esses arquivos não são versionados.

## Requisitos

- Android SDK compatível com `compileSdk 36`.
- JDK 17.
- Developer token válido do Product Hunt para consultar a API.

## Segurança

Não coloque tokens, senhas, arquivos de keystore ou propriedades de assinatura no repositório. O APK universal de release foi incluído para facilitar a instalação; valide sua origem antes de instalar.
