package com.crow.base.tools.extensions


fun String.removeWhiteSpace() = filterNot { it.isWhitespace() }