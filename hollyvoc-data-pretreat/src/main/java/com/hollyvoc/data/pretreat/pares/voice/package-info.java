/**
 * Created by zhaihw on 2016/3/9.
 *
 * 转译xml文件解析器重构
 *      使其性能更加高效，提前缓存系统默认的Unmarshaller
 *      使其更加易于使用，采用工厂+单例设计模式，使用的时候只需传入xml流即可，VoiceParserFactory.getInstance().getVoiceParse(is);
 *      扩展更加方便：如果接入新的转译厂家，只需实现IVoiceParse接口即可
 */
package com.hollyvoc.data.pretreat.pares.voice;