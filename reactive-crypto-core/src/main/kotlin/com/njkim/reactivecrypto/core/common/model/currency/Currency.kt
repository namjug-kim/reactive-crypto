/*
 * Copyright 2019 namjug-kim
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.njkim.reactivecrypto.core.common.model.currency

enum class Currency {
    BTC, XRP, ETC, ETH, EOS, ADX, OST, COSM, ADA, POLY, ADY, GRS, ENJ, ZRX,
    XLM, TRX, DMT, BYY, QKC, KNC, ICX, BAT, TFUEL, ADT, CPT, BTT, THETA, QTUM, EDR,
    ONG, RFR, IOST, SRN, UPP, NEO, STORJ, MOC, ZIL, MEDX, STEEM, IOTA, MFT, ELF, STORM,
    MTL, ONT, EMC2, LTC, IQ, POWR, GNT, OMG, IGNIS, ARK, GTO, WAVES,
    SNT, ARDR, STRAT, GAS, MER, CVC, VTC, SC, LOOM, WAX, PIVX, ZEC, SBD, MCO, XEM,
    XMR, BTG, KMD, REP, LSK, DASH, DCR, RVN, TTC, ANKR, GO, CTXC, DOGE, SPND, SOLVE, XVG,
    DTA, XHV, BLT, PAL, MANA, META, DENT, GAME, OCN, BCPT, DGB, PAY, NPXS, RLC, IOTX, PMA,
    EXP, LRC, VIB, BTS, BSD, RDD, XZC, NXS, TX, TUBE, RCN, BOXX, CRO, MEME, CMCT, ENG,
    ION, NGC, BITB, MUE, BNT, CLOAK, DYN, FCT, LBA, UP, SYS, RVR, SPHR, IOP, PRO, EDG, PART,
    GUP, XDN, IHT, BKX, FSN, SWT, JNT, XEL, BTU, BLOCK, NXT, NAV, PTOY, EXCL, XNK, SYNX, BFT,
    GNO, SPC, GBYTE, AMP, MOBI, VEE, LBC, MONA, HMQ, UBQ, QNT, ANT, DCT, EMC, AID, UKG, PAX,
    BTM, VITE, RADS, SIB, VRC, FTC, DRGN, NCASH, BURST, KORE, LUN, OK, CRW, QRL, SHIFT,
    SERV, VIA, HYDRO, NMR, DNT, BLK, BAY, NKN, ZEN, MET, PI, VBK, AERGO, HST, BNB,
    XBT,

    M19,

    KRW, USD,

    USDT, TUSD;

    companion object {
        val FIAT_CURRENCIES: List<Currency> = listOf(
            KRW,
            USD,
            USDT,
            TUSD,
            BTC,
            ETH
        )
    }

}