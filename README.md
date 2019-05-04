# Reactive CryptoCurrency
[![Kotlin](https://img.shields.io/badge/kotlin-1.3.x-blue.svg)](http://kotlinlang.org) [![CircleCI](https://circleci.com/gh/namjug-kim/reactive-crypto.svg?style=shield&circle-token=aa6aa4ebd3956dd3e1a767d938c7e73869ffd6ab)](https://circleci.com/gh/namjug-kim/reactive-crypto) [![codecov](https://codecov.io/gh/namjug-kim/reactive-crypto/branch/master/graph/badge.svg)](https://codecov.io/gh/namjug-kim/reactive-crypto) [![](https://jitpack.io/v/namjug-kim/reactive-crypto.svg)](https://jitpack.io/#namjug-kim/reactive-crypto)

A Kotlin library for cryptocurrency trading.

## Supported Exchanges

### Websocket
Support public market feature (tickData, orderBook)

| Exchange       | ver | doc |
|----------------|---|---|
| Binance        | * | [ws](https://github.com/binance-exchange/binance-official-api-docs/blob/master/web-socket-streams.md)| 
| Upbit          | v1.0.3 | [ws](https://docs.upbit.com/docs/upbit-quotation-websocket) | 
| HuobiKorea     | * | [ws](https://github.com/alphaex-api/BAPI_Docs_ko/wiki) | 
| Okex           | v3 | [ws](https://www.okex.com/docs/en/#spot_ws-all) | 
| Bithumb⚠️      | - | - |

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

```
repositories {
	...
	maven { url 'https://jitpack.io' }
}
```
Step 1. Add jitpack repository

```
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
public void binanceTickDataExample() {
    // create websocketClient for each crypto currency exchange
    ExchangeWebsocketClient exchangeWebsocketClient = ExchangeClientFactory.Companion.getInstance(ExchangeVendor.BINANCE);
     
    List<CurrencyPair> targetPairs = Collections.singletonList(CurrencyPair.parse("BTC", "USDT"));
    exchangeWebsocketClient.createTradeWebsocket(targetPairs)
                           .doOnNext(tickData -> log.info("new tick data {}", tickData))
                           .subscribe();
}
```
