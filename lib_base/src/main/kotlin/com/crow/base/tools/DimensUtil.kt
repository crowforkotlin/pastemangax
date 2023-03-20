package com.crow.base

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

/**
 * @Description: Dimens生成器
 * @author lei
 * @date 2021/11/15 6:06 下午
 */

//需要创建的目录
private val mDirs = arrayOf(
    "0", "720", "800", "960", "1024", "1080", "1220", "1280", "1440"
)

//最大生成多少dp 默认从0.5dp-450dp
private const val MAX_DP = 450

fun main() {
    //创建目录
    createdDirs()
}

/**
 * 创建对应目录
 */
private fun createdDirs() {
    try {
        for (mDir in mDirs) {
            val file = if (mDir == "0") {
                File("./lib_base/src/main/res/values")
            } else {
                File("./lib_base/src/main/res/values-w" + mDir + "dp")
            }
            println(file)
            if (file.mkdir()) {
            }
            generateDimenXml(mDir.toInt()) /* 创建目录后生成dimens.xml文件 */
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/** 生成dimens.xml文件
 * @param currentSize 当前屏幕尺寸 单位dp
 * */
private fun generateDimenXml(currentSize: Int) {
    val stringBuilder = StringBuilder()
    var value: Double
    val out: PrintWriter
    val benchMarkSize = 1080.0 // 标准尺寸，改为设计图的尺寸，一般是360dp
    val screenStr = "" + currentSize // 当前设备尺寸 dpvalues-wXXXdp中的XXX  设置生成dimens.xml的目录名
    try {

        stringBuilder.append("""<?xml version="1.0" encoding="utf-8"?><resources>""")

        // 生成dp标签
        value = 0.5 * currentSize / benchMarkSize
        if (currentSize != 0)
            stringBuilder.append("\t<dimen name=\"base_dp0_5" + "\">").append(value).append("dp</dimen>\r\n")
        else
            stringBuilder.append("\t<dimen name=\"base_dp0_5" + "\">").append(0.5).append("dp</dimen>\r\n")
        for (i in 0 until MAX_DP) {
            //这里控制对应转换的值，如果是标准尺寸就一对一转换 目前尺寸/标准尺寸  例如目前设备屏幕为420dp 设计用的标准尺寸为375
            value = currentSize / benchMarkSize
            if (currentSize != 0 ) {
                stringBuilder.append("\t<dimen name=\"base_dp").append(i + 1).append("\">")
                    .append((i + 1) * value).append("dp</dimen>\r\n")
            } else {
                stringBuilder.append("\t<dimen name=\"base_dp").append(i + 1).append("\">")
                    .append((i + 1)).append("dp</dimen>\r\n")
            }
            if (i == 0) {
                if (currentSize / benchMarkSize == 1.0) {
                    stringBuilder.append("""<dimen name="base_dp1_5">1.5dp</dimen>""")
                } else {
                    if (currentSize != 0) {
                        stringBuilder.append("\t<dimen name=\"base_dp1_5" + "\">").append(value * 1.5)
                            .append("dp</dimen>\r\n")
                    } else {
                        stringBuilder.append("\t<dimen name=\"base_dp1_5" + "\">").append(1.5)
                            .append("dp</dimen>\r\n")
                    }
                }
            } else {
                if (currentSize / benchMarkSize == 1.0) {
                    stringBuilder.append("\t<dimen name=\"base_dp").append(i + 1).append("_5")
                        .append("\">").append(i + 1.5).append("dp</dimen>\r\n")
                } else {
                    if (currentSize != 0) {
                        stringBuilder.append("\t<dimen name=\"base_dp").append(i + 1).append("_5")
                            .append("\">").append(value * (i + 1.5)).append("dp</dimen>\r\n")
                    } else {
                        stringBuilder.append("\t<dimen name=\"base_dp").append(i + 1).append("_5")
                            .append("\">").append(i + 1.5).append("dp</dimen>\r\n")
                    }
                }
            }
        }

        //生成sp标签
        for (i in 6..50) {
            //这里控制对应转换的值，如果是标准尺寸就一对一转换 目前尺寸/标准尺寸  例如目前设备屏幕为420dp 设计用的标准尺寸为360
            value = i.toDouble() * currentSize / benchMarkSize
            if (currentSize != 0) {
                stringBuilder.append("\t<dimen name=\"base_sp").append(i).append("\">").append(value)
                    .append("sp</dimen>\r\n")
            } else {
                stringBuilder.append("\t<dimen name=\"base_sp").append(i).append("\">").append(i)
                    .append("sp</dimen>\r\n")
            }
        }

        stringBuilder.append("</resources>")
        //这里是文件名，1 注意修改 sw 后面的值，和转换值一一对应
        val fileDef = if (currentSize != 0) "./lib_base/src/main/res/values-w" + screenStr + "dp/dimens.xml" else "./lib_base/src/main/res/values/dimens.xml"
        //文件写入流
        out = PrintWriter(BufferedWriter(FileWriter(fileDef)))
        //写入文件
        out.println(stringBuilder.toString())
        //关闭io流
        out.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
