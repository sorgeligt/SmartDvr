package com.lutalic.smartdvr

fun getCurrentDateWithoutLastSecond(): Long {
    return System.currentTimeMillis() / 10000 * 10000
}
