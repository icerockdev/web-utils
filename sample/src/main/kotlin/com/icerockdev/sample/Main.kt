/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.sample

import com.icerockdev.webserver.NettyEngine

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        NettyEngine.start(args)

        Runtime.getRuntime().addShutdownHook(Thread(Runnable {
            NettyEngine.stop(1000L, 2000L)
        }))
    }
}
