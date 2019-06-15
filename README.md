<h1 align="center">
</br>
</br>
Reactive Crypto 
</br>
</br>
</h1>

<p align="center">
<a href="http://kotlinlang.org"><img src="https://img.shields.io/badge/kotlin-1.3.x-blue.svg" alt="Kotlin"></a>
<a href="https://circleci.com/gh/namjug-kim/reactive-crypto"><img src="https://circleci.com/gh/namjug-kim/reactive-crypto.svg?style=shield&circle-token=aa6aa4ebd3956dd3e1a767d938c7e73869ffd6ab" alt="CircleCI"></a>
<a href="https://codecov.io/gh/namjug-kim/reactive-crypto"><img src="https://codecov.io/gh/namjug-kim/reactive-crypto/branch/master/graph/badge.svg" alt="codecov"></a>
<a href="https://jitpack.io/#namjug-kim/reactive-crypto"><img src="https://jitpack.io/v/namjug-kim/reactive-crypto.svg" alt="jitpack"></a>
<a href="https://ktlint.github.io/"><img src="https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg" alt="ktlint"></a>
</p>

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
| ![bitmax](https://user-images.githubusercontent.com/16334718/57548356-b082d480-739b-11e9-9539-b27c60877fb6.jpg)       | Bitmax      | BITMAX         | v1.2   | [ws](https://github.com/bitmax-exchange/api-doc/blob/master/bitmax-api-doc-v1.2.md) |
| ![idax](https://user-images.githubusercontent.com/16334718/58029691-128bc880-7b58-11e9-9aaa-a331f394c8bd.jpg)         | Idax        | IDAX           | *      | [ws](https://github.com/idax-exchange/idax-official-api-docs/blob/master/open-ws_en.md) |
| ![coineal](https://user-images.githubusercontent.com/16334718/58037062-7d90cb80-7b67-11e9-9278-e8b03c5ddd86.jpg)      | Coineal     | COINEAL        | ⚠️     | ⚠️ |
| ![poloniex](https://user-images.githubusercontent.com/16334718/59551277-335a0900-8fb2-11e9-9d1e-4ab2a7574148.jpg)     | Poloniex    | POLONIEX       | *      | [ws](https://docs.poloniex.com/#websocket-api) |

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
    <version>LATEST</version>
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
    implementation 'com.github.namjug-kim.reactive-crypto:reactive-crypto-{exchange-name}:LATEST'
}
```
Step 2. Add the dependency

## Usage

### Kotlin

```kotlin
fun websocketTickDataExample() {
    // create websocketClient for each crypto currency exchange
    val websocketClient = ExchangeClientFactory.websocket(ExchangeVendor.BINANCE)
    
    websocketClient.createTradeWebsocket(listOf(CurrencyPair(BTC, USDT)))
                   .doOnNext { log.info { "new tick data $it" } }
                   .subscribe()
}

fun httpLimitOrderExample() {
    val orderPlaceResult = ExchangeClientFactory.http(ExchangeVendor.BINANCE)
                .privateApi("accessKey", "secretKey")
                .order()
                .limitOrder(
                    CurrencyPair(Currency.BTC, Currency.KRW),
                    TradeSideType.BUY,
                    BigDecimal.valueOf(10000000.0),
                    BigDecimal.valueOf(10.0)
                )
                .block()

    log.info { orderPlaceResult }
}

```

### Java

```java
class SampleClass {
    public void websocketTickDataExample() {
        // create websocketClient for each crypto currency exchange
        ExchangeWebsocketClient exchangeWebsocketClient = ExchangeClientFactory.websocket(ExchangeVendor.BINANCE);
         
        List<CurrencyPair> targetPairs = Collections.singletonList(CurrencyPair.parse("BTC", "USDT"));
        exchangeWebsocketClient.createTradeWebsocket(targetPairs)
                               .doOnNext(tickData -> log.info("new tick data {}", tickData))
                               .subscribe();
    }

    public void httpLimitOrderExample() {
        OrderPlaceResult orderPlaceResult = ExchangeClientFactory.http(ExchangeVendor.BINANCE)
                        .privateApi("accessKey", "secretKey")
                        .order()
                        .limitOrder(
                            CurrencyPair(Currency.BTC, Currency.KRW),
                            TradeSideType.BUY,
                            BigDecimal.valueOf(10000000.0),
                            BigDecimal.valueOf(10.0)
                        )
                        .block();

        log.info("{}", orderPlaceResult);
    }

}
```

## 💬 Contributing
* [Commit convention](https://github.com/namjug-kim/reactive-crypto/blob/master/docs/COMMIT_MESSAGE_CONVENTION.md)

## 📜 License
* Reactive Crypto is Open Source Software released under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
