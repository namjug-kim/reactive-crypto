# Reactive CryptoCurrency
[![Kotlin](https://img.shields.io/badge/kotlin-1.3.x-blue.svg)](http://kotlinlang.org) [![CircleCI](https://circleci.com/gh/namjug-kim/reactive-crypto.svg?style=shield&circle-token=aa6aa4ebd3956dd3e1a767d938c7e73869ffd6ab)](https://circleci.com/gh/namjug-kim/reactive-crypto) [![codecov](https://codecov.io/gh/namjug-kim/reactive-crypto/branch/master/graph/badge.svg)](https://codecov.io/gh/namjug-kim/reactive-crypto) [![](https://jitpack.io/v/namjug-kim/reactive-crypto.svg)](https://jitpack.io/#namjug-kim/reactive-crypto)

A Kotlin library for cryptocurrency trading.

## Supported Exchanges

### Websocket
Support public market feature (tickData, orderBook)

| logo                                                                                                                  | name        | ExchangeVendor | ver | doc |
| --------------------------------------------------------------------------------------------------------------------- | ----------- | ---------------- |--------|---|
| ![binance](https://user-images.githubusercontent.com/16334718/57194951-e5e88600-6f87-11e9-918e-74de5c58e883.jpg)      | Binance     | BINANCE        | *      | [ws](https://github.com/binance-exchange/binance-official-api-docs/blob/master/web-socket-streams.md) | 
| ![upbit](https://user-images.githubusercontent.com/16334718/57194949-e54fef80-6f87-11e9-85b3-67b8f82db564.jpg)        | Upbit       | UPBIT          | v1.0.3 | [ws](https://docs.upbit.com/docs/upbit-quotation-websocket) | 
| ![huobi korea](https://user-images.githubusercontent.com/16334718/57194946-e4b75900-6f87-11e9-940a-08ceb98193e4.jpg)  | HuobiKorea  | HUOBI_KOREA    | *      | [ws](https://github.com/alphaex-api/BAPI_Docs_ko/wiki) | 
| ![okex](https://user-images.githubusercontent.com/16334718/57195022-90f93f80-6f88-11e9-8aaa-f6a515d300ae.jpg)         | Okex        | OKEX           | v3     | [ws](https://www.okex.com/docs/en/#spot_ws-all) | 
| ![bithumb](https://user-images.githubusercontent.com/16334718/57194948-e54fef80-6f87-11e9-90d8-41f108789c77.jpg)      | Bithumb     | BITHUMB        | ⚠️     | ⚠️ |
| ![hubi](https://user-images.githubusercontent.com/16334718/57194945-e4b75900-6f87-11e9-8fea-889fc93a7ba4.jpg)         | Hubi        | HUBI           | *      | [ws](https://www.hubi.com/docs/index-en.pdf) |
| ![bitmex](https://user-images.githubusercontent.com/16334718/57194950-e54fef80-6f87-11e9-8b54-3f2192012306.jpg)       | Bitmex      | BITMEX         | *      | [ws](https://www.bitmex.com/app/wsAPI) |
| ![kraken](https://user-images.githubusercontent.com/16334718/57220400-2dc5e680-7036-11e9-803c-18b14e82921a.jpg)       | Kraken      | KRAKEN         | 0.1.1  | [ws](https://www.kraken.com/features/websocket-api) |

⚠️ : Uses endpoints that are used by the official web. This is not an official api and should be used with care.

### Api
| Exchange       | ver | doc |
|----------------|---|---|
| | |

## Install

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
Step 1. Add jitpack repository

```xml
<dependency>
    <groupId>com.github.namjug-kim.reactive-crypto</groupId>
    <artifactId>reactive-crypto-{exchange-name}</artifactId>
    <version>v0.1.0.RELEASE</version>
</dependency>
```
Step 2. Add the dependency

### Gradle

``` groovy
repositories {
	...
	maven { url 'https://jitpack.io' }
}
```
Step 1. Add jitpack repository

``` groovy
dependencies {
    implementation 'com.github.namjug-kim.reactive-crypto:reactive-crypto-{exchange-name}:v0.1.0.RELEASE'
}
```
Step 2. Add the dependency

## Usage

### Kotlin

```kotlin
fun binanceTickDataExample() {
    // create websocketClient for each crypto currency exchange
    val websocketClient = ExchangeClientFactory.getInstance(ExchangeVendor.BINANCE)
    
    websocketClient.createTradeWebsocket(listOf(CurrencyPair(BTC, USDT)))
                   .doOnNext { log.info { "new tick data $it" } }
                   .subscribe()
}

```

### Java

```java
class SampleClass {
    public void binanceTickDataExample() {
        // create websocketClient for each crypto currency exchange
        ExchangeWebsocketClient exchangeWebsocketClient = ExchangeClientFactory.getInstance(ExchangeVendor.BINANCE);
         
        List<CurrencyPair> targetPairs = Collections.singletonList(CurrencyPair.parse("BTC", "USDT"));
        exchangeWebsocketClient.createTradeWebsocket(targetPairs)
                               .doOnNext(tickData -> log.info("new tick data {}", tickData))
                               .subscribe();
    }
}
```
