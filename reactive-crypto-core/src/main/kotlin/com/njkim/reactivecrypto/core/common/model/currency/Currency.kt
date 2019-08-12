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

data class Currency(val symbol: String) {
    companion object {
        private val mapCache: MutableMap<String, Currency> = HashMap()

        @JvmField
        val BTC = getInstance("BTC")
        @JvmField
        val XRP = getInstance("XRP")
        @JvmField
        val ETC = getInstance("ETC")
        @JvmField
        val ETH = getInstance("ETH")
        @JvmField
        val EOS = getInstance("EOS")
        @JvmField
        val ADX = getInstance("ADX")
        @JvmField
        val OST = getInstance("OST")
        @JvmField
        val COSM = getInstance("COSM")
        @JvmField
        val ADA = getInstance("ADA")
        @JvmField
        val POLY = getInstance("POLY")
        @JvmField
        val ADY = getInstance("ADY")
        @JvmField
        val GRS = getInstance("GRS")
        @JvmField
        val ENJ = getInstance("ENJ")
        @JvmField
        val ZRX = getInstance("ZRX")
        @JvmField
        val XLM = getInstance("XLM")
        @JvmField
        val TRX = getInstance("TRX")
        @JvmField
        val DMT = getInstance("DMT")
        @JvmField
        val BYY = getInstance("BYY")
        @JvmField
        val QKC = getInstance("QKC")
        @JvmField
        val KNC = getInstance("KNC")
        @JvmField
        val ICX = getInstance("ICX")
        @JvmField
        val BAT = getInstance("BAT")
        @JvmField
        val TFUEL = getInstance("TFUEL")
        @JvmField
        val ADT = getInstance("ADT")
        @JvmField
        val CPT = getInstance("CPT")
        @JvmField
        val BTT = getInstance("BTT")
        @JvmField
        val THETA = getInstance("THETA")
        @JvmField
        val QTUM = getInstance("QTUM")
        @JvmField
        val EDR = getInstance("EDR")
        @JvmField
        val ONG = getInstance("ONG")
        @JvmField
        val RFR = getInstance("RFR")
        @JvmField
        val IOST = getInstance("IOST")
        @JvmField
        val SRN = getInstance("SRN")
        @JvmField
        val UPP = getInstance("UPP")
        @JvmField
        val NEO = getInstance("NEO")
        @JvmField
        val STORJ = getInstance("STORJ")
        @JvmField
        val MOC = getInstance("MOC")
        @JvmField
        val ZIL = getInstance("ZIL")
        @JvmField
        val MEDX = getInstance("MEDX")
        @JvmField
        val STEEM = getInstance("STEEM")
        @JvmField
        val IOTA = getInstance("IOTA")
        @JvmField
        val MFT = getInstance("MFT")
        @JvmField
        val ELF = getInstance("ELF")
        @JvmField
        val STORM = getInstance("STORM")
        @JvmField
        val MTL = getInstance("MTL")
        @JvmField
        val ONT = getInstance("ONT")
        @JvmField
        val EMC2 = getInstance("EMC2")
        @JvmField
        val LTC = getInstance("LTC")
        @JvmField
        val IQ = getInstance("IQ")
        @JvmField
        val POWR = getInstance("POWR")
        @JvmField
        val GNT = getInstance("GNT")
        @JvmField
        val OMG = getInstance("OMG")
        @JvmField
        val IGNIS = getInstance("IGNIS")
        @JvmField
        val ARK = getInstance("ARK")
        @JvmField
        val GTO = getInstance("GTO")
        @JvmField
        val WAVES = getInstance("WAVES")
        @JvmField
        val SNT = getInstance("SNT")
        @JvmField
        val ARDR = getInstance("ARDR")
        @JvmField
        val STRAT = getInstance("STRAT")
        @JvmField
        val GAS = getInstance("GAS")
        @JvmField
        val MER = getInstance("MER")
        @JvmField
        val CVC = getInstance("CVC")
        @JvmField
        val VTC = getInstance("VTC")
        @JvmField
        val SC = getInstance("SC")
        @JvmField
        val LOOM = getInstance("LOOM")
        @JvmField
        val WAX = getInstance("WAX")
        @JvmField
        val PIVX = getInstance("PIVX")
        @JvmField
        val ZEC = getInstance("ZEC")
        @JvmField
        val SBD = getInstance("SBD")
        @JvmField
        val MCO = getInstance("MCO")
        @JvmField
        val XEM = getInstance("XEM")
        @JvmField
        val XMR = getInstance("XMR")
        @JvmField
        val BTG = getInstance("BTG")
        @JvmField
        val KMD = getInstance("KMD")
        @JvmField
        val REP = getInstance("REP")
        @JvmField
        val LSK = getInstance("LSK")
        @JvmField
        val DASH = getInstance("DASH")
        @JvmField
        val DCR = getInstance("DCR")
        @JvmField
        val RVN = getInstance("RVN")
        @JvmField
        val TTC = getInstance("TTC")
        @JvmField
        val ANKR = getInstance("ANKR")
        @JvmField
        val GO = getInstance("GO")
        @JvmField
        val CTXC = getInstance("CTXC")
        @JvmField
        val DOGE = getInstance("DOGE")
        @JvmField
        val SPND = getInstance("SPND")
        @JvmField
        val SOLVE = getInstance("SOLVE")
        @JvmField
        val XVG = getInstance("XVG")
        @JvmField
        val DTA = getInstance("DTA")
        @JvmField
        val XHV = getInstance("XHV")
        @JvmField
        val BLT = getInstance("BLT")
        @JvmField
        val PAL = getInstance("PAL")
        @JvmField
        val MANA = getInstance("MANA")
        @JvmField
        val META = getInstance("META")
        @JvmField
        val DENT = getInstance("DENT")
        @JvmField
        val GAME = getInstance("GAME")
        @JvmField
        val OCN = getInstance("OCN")
        @JvmField
        val BCPT = getInstance("BCPT")
        @JvmField
        val DGB = getInstance("DGB")
        @JvmField
        val PAY = getInstance("PAY")
        @JvmField
        val NPXS = getInstance("NPXS")
        @JvmField
        val RLC = getInstance("RLC")
        @JvmField
        val IOTX = getInstance("IOTX")
        @JvmField
        val PMA = getInstance("PMA")
        @JvmField
        val EXP = getInstance("EXP")
        @JvmField
        val LRC = getInstance("LRC")
        @JvmField
        val VIB = getInstance("VIB")
        @JvmField
        val BTS = getInstance("BTS")
        @JvmField
        val BSD = getInstance("BSD")
        @JvmField
        val RDD = getInstance("RDD")
        @JvmField
        val XZC = getInstance("XZC")
        @JvmField
        val NXS = getInstance("NXS")
        @JvmField
        val TX = getInstance("TX")
        @JvmField
        val TUBE = getInstance("TUBE")
        @JvmField
        val RCN = getInstance("RCN")
        @JvmField
        val BOXX = getInstance("BOXX")
        @JvmField
        val CRO = getInstance("CRO")
        @JvmField
        val MEME = getInstance("MEME")
        @JvmField
        val CMCT = getInstance("CMCT")
        @JvmField
        val ENG = getInstance("ENG")
        @JvmField
        val ION = getInstance("ION")
        @JvmField
        val NGC = getInstance("NGC")
        @JvmField
        val BITB = getInstance("BITB")
        @JvmField
        val MUE = getInstance("MUE")
        @JvmField
        val BNT = getInstance("BNT")
        @JvmField
        val CLOAK = getInstance("CLOAK")
        @JvmField
        val DYN = getInstance("DYN")
        @JvmField
        val FCT = getInstance("FCT")
        @JvmField
        val LBA = getInstance("LBA")
        @JvmField
        val UP = getInstance("UP")
        @JvmField
        val SYS = getInstance("SYS")
        @JvmField
        val RVR = getInstance("RVR")
        @JvmField
        val SPHR = getInstance("SPHR")
        @JvmField
        val IOP = getInstance("IOP")
        @JvmField
        val PRO = getInstance("PRO")
        @JvmField
        val EDG = getInstance("EDG")
        @JvmField
        val PART = getInstance("PART")
        @JvmField
        val GUP = getInstance("GUP")
        @JvmField
        val XDN = getInstance("XDN")
        @JvmField
        val IHT = getInstance("IHT")
        @JvmField
        val BKX = getInstance("BKX")
        @JvmField
        val FSN = getInstance("FSN")
        @JvmField
        val SWT = getInstance("SWT")
        @JvmField
        val JNT = getInstance("JNT")
        @JvmField
        val XEL = getInstance("XEL")
        @JvmField
        val BTU = getInstance("BTU")
        @JvmField
        val BLOCK = getInstance("BLOCK")
        @JvmField
        val NXT = getInstance("NXT")
        @JvmField
        val NAV = getInstance("NAV")
        @JvmField
        val PTOY = getInstance("PTOY")
        @JvmField
        val EXCL = getInstance("EXCL")
        @JvmField
        val XNK = getInstance("XNK")
        @JvmField
        val SYNX = getInstance("SYNX")
        @JvmField
        val BFT = getInstance("BFT")
        @JvmField
        val GNO = getInstance("GNO")
        @JvmField
        val SPC = getInstance("SPC")
        @JvmField
        val GBYTE = getInstance("GBYTE")
        @JvmField
        val AMP = getInstance("AMP")
        @JvmField
        val MOBI = getInstance("MOBI")
        @JvmField
        val VEE = getInstance("VEE")
        @JvmField
        val LBC = getInstance("LBC")
        @JvmField
        val MONA = getInstance("MONA")
        @JvmField
        val HMQ = getInstance("HMQ")
        @JvmField
        val UBQ = getInstance("UBQ")
        @JvmField
        val QNT = getInstance("QNT")
        @JvmField
        val ANT = getInstance("ANT")
        @JvmField
        val DCT = getInstance("DCT")
        @JvmField
        val EMC = getInstance("EMC")
        @JvmField
        val AID = getInstance("AID")
        @JvmField
        val UKG = getInstance("UKG")
        @JvmField
        val BTM = getInstance("BTM")
        @JvmField
        val VITE = getInstance("VITE")
        @JvmField
        val RADS = getInstance("RADS")
        @JvmField
        val SIB = getInstance("SIB")
        @JvmField
        val VRC = getInstance("VRC")
        @JvmField
        val FTC = getInstance("FTC")
        @JvmField
        val DRGN = getInstance("DRGN")
        @JvmField
        val NCASH = getInstance("NCASH")
        @JvmField
        val BURST = getInstance("BURST")
        @JvmField
        val KORE = getInstance("KORE")
        @JvmField
        val LUN = getInstance("LUN")
        @JvmField
        val OK = getInstance("OK")
        @JvmField
        val CRW = getInstance("CRW")
        @JvmField
        val QRL = getInstance("QRL")
        @JvmField
        val SHIFT = getInstance("SHIFT")
        @JvmField
        val SERV = getInstance("SERV")
        @JvmField
        val VIA = getInstance("VIA")
        @JvmField
        val HYDRO = getInstance("HYDRO")
        @JvmField
        val NMR = getInstance("NMR")
        @JvmField
        val DNT = getInstance("DNT")
        @JvmField
        val BLK = getInstance("BLK")
        @JvmField
        val BAY = getInstance("BAY")
        @JvmField
        val NKN = getInstance("NKN")
        @JvmField
        val ZEN = getInstance("ZEN")
        @JvmField
        val MET = getInstance("MET")
        @JvmField
        val PI = getInstance("PI")
        @JvmField
        val VBK = getInstance("VBK")
        @JvmField
        val AERGO = getInstance("AERGO")
        @JvmField
        val HST = getInstance("HST")
        @JvmField
        val BNB = getInstance("BNB")
        @JvmField
        val XBT = getInstance("XBT")
        @JvmField
        val HT = getInstance("HT")
        @JvmField
        val BCH = getInstance("BCH")
        @JvmField
        val BSV = getInstance("BSV")
        @JvmField
        val PCI = getInstance("PCI")
        @JvmField
        val IT = getInstance("IT")
        @JvmField
        val ABBC = getInstance("ABBC")
        @JvmField
        val DPN = getInstance("DPN")
        @JvmField
        val FNB = getInstance("FNB")
        @JvmField
        val DEX = getInstance("DEX")
        @JvmField
        val MIC = getInstance("MIC")
        @JvmField
        val DLO = getInstance("DLO")
        @JvmField
        val APIS = getInstance("APIS")
        @JvmField
        val TAS = getInstance("TAS")
        @JvmField
        val SECRET = getInstance("SECRET")
        @JvmField
        val PHR = getInstance("PHR")
        @JvmField
        val QTCON = getInstance("QTCON")
        @JvmField
        val ARN = getInstance("ARN")
        @JvmField
        val PPY = getInstance("PPY")
        @JvmField
        val BAAS = getInstance("BAAS")
        @JvmField
        val XET = getInstance("XET")
        @JvmField
        val EDC = getInstance("EDC")
        @JvmField
        val AE = getInstance("AE")
        @JvmField
        val NULS = getInstance("NULS")
        @JvmField
        val AEN = getInstance("AEN")
        @JvmField
        val ILC = getInstance("ILC")
        @JvmField
        val LOCUS = getInstance("LOCUS")
        @JvmField
        val HQT = getInstance("HQT")
        @JvmField
        val NPX = getInstance("NPX")
        @JvmField
        val SWC = getInstance("SWC")
        @JvmField
        val LINDA = getInstance("LINDA")
        @JvmField
        val FML = getInstance("FML")
        @JvmField
        val GUNTHY = getInstance("GUNTHY")
        @JvmField
        val VIN = getInstance("VIN")
        @JvmField
        val GOB = getInstance("GOB")
        @JvmField
        val EVY = getInstance("EVY")
        @JvmField
        val MGO = getInstance("MGO")
        @JvmField
        val FUNDZ = getInstance("FUNDZ")
        @JvmField
        val UQC = getInstance("UQC")
        @JvmField
        val CTC = getInstance("CTC")
        @JvmField
        val MRPH = getInstance("MRPH")
        @JvmField
        val PCCM = getInstance("PCCM")
        @JvmField
        val PXL = getInstance("PXL")
        @JvmField
        val IOTW = getInstance("IOTW")
        @JvmField
        val NRC = getInstance("NRC")
        @JvmField
        val ZAT = getInstance("ZAT")
        @JvmField
        val POE = getInstance("POE")
        @JvmField
        val APL = getInstance("APL")
        @JvmField
        val VEX = getInstance("VEX")
        @JvmField
        val VITAE = getInstance("VITAE")
        @JvmField
        val FTX = getInstance("FTX")
        @JvmField
        val ABTC = getInstance("ABTC")
        @JvmField
        val TSL = getInstance("TSL")
        @JvmField
        val SCRL = getInstance("SCRL")
        @JvmField
        val NIX = getInstance("NIX")
        @JvmField
        val TMC = getInstance("TMC")
        @JvmField
        val MCAN = getInstance("MCAN")
        @JvmField
        val LGD = getInstance("LGD")
        @JvmField
        val ZEST = getInstance("ZEST")
        @JvmField
        val ALP = getInstance("ALP")
        @JvmField
        val CXP = getInstance("CXP")
        @JvmField
        val ZING = getInstance("ZING")
        @JvmField
        val UCN = getInstance("UCN")
        @JvmField
        val RET = getInstance("RET")
        @JvmField
        val WIRE = getInstance("WIRE")
        @JvmField
        val GUBI = getInstance("GUBI")
        @JvmField
        val LAD = getInstance("LAD")
        @JvmField
        val EVNS = getInstance("EVNS")
        @JvmField
        val CAPP = getInstance("CAPP")
        @JvmField
        val ZBB = getInstance("ZBB")
        @JvmField
        val KZE = getInstance("KZE")
        @JvmField
        val DBX = getInstance("DBX")
        @JvmField
        val NTY = getInstance("NTY")
        @JvmField
        val GFC = getInstance("GFC")
        @JvmField
        val GMB = getInstance("GMB")
        @JvmField
        val FLC = getInstance("FLC")
        @JvmField
        val BTD = getInstance("BTD")
        @JvmField
        val SKM = getInstance("SKM")
        @JvmField
        val MALL = getInstance("MALL")
        @JvmField
        val RFOX = getInstance("RFOX")
        @JvmField
        val FN = getInstance("FN")
        @JvmField
        val IZI = getInstance("IZI")
        @JvmField
        val NVL = getInstance("NVL")
        @JvmField
        val YTA = getInstance("YTA")
        @JvmField
        val IPWT = getInstance("IPWT")
        @JvmField
        val AT = getInstance("AT")
        @JvmField
        val HVCC = getInstance("HVCC")
        @JvmField
        val CL = getInstance("CL")
        @JvmField
        val OA = getInstance("OA")
        @JvmField
        val VSS = getInstance("VSS")
        @JvmField
        val STCC = getInstance("STCC")
        @JvmField
        val CTA = getInstance("CTA")
        @JvmField
        val CPX = getInstance("CPX")
        @JvmField
        val KDH = getInstance("KDH")
        @JvmField
        val LAMB = getInstance("LAMB")
        @JvmField
        val IFX = getInstance("IFX")
        @JvmField
        val GCS = getInstance("GCS")
        @JvmField
        val BRC = getInstance("BRC")
        @JvmField
        val O2P = getInstance("O2P")
        @JvmField
        val BHD = getInstance("BHD")
        @JvmField
        val IVO = getInstance("IVO")
        @JvmField
        val BTMC = getInstance("BTMC")
        @JvmField
        val BKBT = getInstance("BKBT")
        @JvmField
        val TCLB = getInstance("TCLB")
        @JvmField
        val NEAL = getInstance("NEAL")
        @JvmField
        val ONE = getInstance("ONE")
        @JvmField
        val MATIC = getInstance("MATIC")
        @JvmField
        val LINK = getInstance("LINK")
        @JvmField
        val CELR = getInstance("CELR")
        @JvmField
        val VET = getInstance("VET")
        @JvmField
        val ATOM = getInstance("ATOM")
        @JvmField
        val FET = getInstance("FET")
        @JvmField
        val PHB = getInstance("PHB")
        @JvmField
        val WAN = getInstance("WAN")
        @JvmField
        val HOT = getInstance("HOT")
        @JvmField
        val HC = getInstance("HC")
        @JvmField
        val WTC = getInstance("WTC")
        @JvmField
        val QLC = getInstance("QLC")
        @JvmField
        val KEY = getInstance("KEY")
        @JvmField
        val AST = getInstance("AST")
        @JvmField
        val AION = getInstance("AION")
        @JvmField
        val EVX = getInstance("EVX")
        @JvmField
        val SNM = getInstance("SNM")
        @JvmField
        val AGI = getInstance("AGI")
        @JvmField
        val APPC = getInstance("APPC")
        @JvmField
        val DLT = getInstance("DLT")
        @JvmField
        val SKY = getInstance("SKY")
        @JvmField
        val MITH = getInstance("MITH")
        @JvmField
        val BCC = getInstance("BCC")
        @JvmField
        val HSR = getInstance("HSR")
        @JvmField
        val OAX = getInstance("OAX")
        @JvmField
        val ICN = getInstance("ICN")
        @JvmField
        val ALGO = getInstance("ALGO")
        @JvmField
        val EDO = getInstance("EDO")
        @JvmField
        val WINGS = getInstance("WINGS")
        @JvmField
        val TRIG = getInstance("TRIG")
        @JvmField
        val YOYO = getInstance("YOYO")
        @JvmField
        val BTCB = getInstance("BTCB")
        @JvmField
        val SNGLS = getInstance("SNGLS")
        @JvmField
        val BQX = getInstance("BQX")
        @JvmField
        val VTHO = getInstance("VTHO")
        @JvmField
        val DOCK = getInstance("DOCK")
        @JvmField
        val ATD = getInstance("ATD")
        @JvmField
        val MEETONE = getInstance("MEETONE")
        @JvmField
        val ADD = getInstance("ADD")
        @JvmField
        val EON = getInstance("EON")
        @JvmField
        val FUN = getInstance("FUN")
        @JvmField
        val WPR = getInstance("WPR")
        @JvmField
        val POA = getInstance("POA")
        @JvmField
        val BLZ = getInstance("BLZ")
        @JvmField
        val NEBL = getInstance("NEBL")
        @JvmField
        val BRD = getInstance("BRD")
        @JvmField
        val ETF = getInstance("ETF")
        @JvmField
        val DATA = getInstance("DATA")
        @JvmField
        val TNB = getInstance("TNB")
        @JvmField
        val BCX = getInstance("BCX")
        @JvmField
        val SBTC = getInstance("SBTC")
        @JvmField
        val WABI = getInstance("WABI")
        @JvmField
        val LEND = getInstance("LEND")
        @JvmField
        val CND = getInstance("CND")
        @JvmField
        val CMT = getInstance("CMT")
        @JvmField
        val PPT = getInstance("PPT")
        @JvmField
        val DGD = getInstance("DGD")
        @JvmField
        val BCD = getInstance("BCD")
        @JvmField
        val FUEL = getInstance("FUEL")
        @JvmField
        val QSP = getInstance("QSP")
        @JvmField
        val GXS = getInstance("GXS")
        @JvmField
        val CVT = getInstance("CVT")
        @JvmField
        val AMB = getInstance("AMB")
        @JvmField
        val RDN = getInstance("RDN")
        @JvmField
        val MOD = getInstance("MOD")
        @JvmField
        val MTH = getInstance("MTH")
        @JvmField
        val MDA = getInstance("MDA")
        @JvmField
        val CTR = getInstance("CTR")
        @JvmField
        val CDT = getInstance("CDT")
        @JvmField
        val TNT = getInstance("TNT")
        @JvmField
        val SALT = getInstance("SALT")
        @JvmField
        val SUB = getInstance("SUB")
        @JvmField
        val REQ = getInstance("REQ")
        @JvmField
        val VEN = getInstance("VEN")
        @JvmField
        val GVT = getInstance("GVT")
        @JvmField
        val VIBE = getInstance("VIBE")
        @JvmField
        val CHAT = getInstance("CHAT")
        @JvmField
        val INS = getInstance("INS")
        @JvmField
        val NANO = getInstance("NANO")
        @JvmField
        val RPX = getInstance("RPX")
        @JvmField
        val BCN = getInstance("BCN")
        @JvmField
        val NAS = getInstance("NAS")
        @JvmField
        val PHX = getInstance("PHX")
        @JvmField
        val BCHABC = getInstance("BCHABC")
        @JvmField
        val BCHSV = getInstance("BCHSV")
        @JvmField
        val REN = getInstance("REN")
        @JvmField
        val FTM = getInstance("FTM")
        @JvmField
        val ATP = getInstance("ATP")
        @JvmField
        val AUTO = getInstance("AUTO")
        @JvmField
        val HYC = getInstance("HYC")
        @JvmField
        val AAC = getInstance("AAC")
        @JvmField
        val EGT = getInstance("EGT")
        @JvmField
        val OSC = getInstance("OSC")
        @JvmField
        val EOSC = getInstance("EOSC")
        @JvmField
        val TOSC = getInstance("TOSC")
        @JvmField
        val ISR = getInstance("ISR")
        @JvmField
        val BF = getInstance("BF")
        @JvmField
        val ABLD = getInstance("ABLD")
        @JvmField
        val ABYSS = getInstance("ABYSS")
        @JvmField
        val ACDC = getInstance("ACDC")
        @JvmField
        val ADF = getInstance("ADF")
        @JvmField
        val AIC = getInstance("AIC")
        @JvmField
        val ATMI = getInstance("ATMI")
        @JvmField
        val BEAM = getInstance("BEAM")
        @JvmField
        val BRZ = getInstance("BRZ")
        @JvmField
        val BU = getInstance("BU")
        @JvmField
        val BWXX = getInstance("BWXX")
        @JvmField
        val CARD = getInstance("CARD")
        @JvmField
        val DACC = getInstance("DACC")
        @JvmField
        val DOT = getInstance("DOT")
        @JvmField
        val BAS = getInstance("BAS")
        @JvmField
        val FIN = getInstance("FIN")
        @JvmField
        val BNS = getInstance("BNS")
        @JvmField
        val MIN = getInstance("MIN")
        @JvmField
        val WIN = getInstance("WIN")
        @JvmField
        val LEVL = getInstance("LEVL")
        @JvmField
        val XSC = getInstance("XSC")
        @JvmField
        val FYS = getInstance("FYS")
        @JvmField
        val KGT = getInstance("KGT")
        @JvmField
        val XCON = getInstance("XCON")
        @JvmField
        val KBC = getInstance("KBC")
        @JvmField
        val GTP = getInstance("GTP")
        @JvmField
        val PDATA = getInstance("PDATA")
        @JvmField
        val KICKS = getInstance("KICKS")
        @JvmField
        val BOK = getInstance("BOK")
        @JvmField
        val DPH = getInstance("DPH")
        @JvmField
        val TOR = getInstance("TOR")
        @JvmField
        val AFDT = getInstance("AFDT")
        @JvmField
        val BHT = getInstance("BHT")
        @JvmField
        val CXC = getInstance("CXC")
        @JvmField
        val DRINK = getInstance("DRINK")
        @JvmField
        val GRIN = getInstance("GRIN")
        @JvmField
        val LAMBS = getInstance("LAMBS")
        @JvmField
        val YANT = getInstance("YANT")

        @JvmField
        val M19 = getInstance("M19")

        @JvmField
        val KRW = getInstance("KRW")
        @JvmField
        val USD = getInstance("USD")
        @JvmField
        val JPY = getInstance("JPY")

        @JvmField
        val USDT = getInstance("USDT")
        @JvmField
        val TUSD = getInstance("TUSD")
        @JvmField
        val USDC = getInstance("USDC")
        @JvmField
        val PAX = getInstance("PAX")
        @JvmField
        val USDSB = getInstance("USDSB")
        @JvmField
        val USDS = getInstance("USDS")

        val FIAT_CURRENCIES: List<Currency> = listOf(
            KRW,
            USD,
            USDT,
            USDC,
            USDSB,
            TUSD,
            PAX,
            BTC,
            ETH,
            HT,
            JPY
        )

        @JvmStatic
        fun getInstance(symbol: String): Currency {
            return mapCache.computeIfAbsent(symbol) { Currency(symbol) }
        }

        @JvmStatic
        fun values(): Collection<Currency> {
            return mapCache.values
        }
    }

    override fun toString(): String {
        return symbol
    }
}
