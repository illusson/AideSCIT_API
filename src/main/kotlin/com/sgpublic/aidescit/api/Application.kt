package com.sgpublic.aidescit.api

import com.sgpublic.aidescit.api.core.spring.CurrentConfig
import com.sgpublic.aidescit.api.core.util.ArgumentReader
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Import

fun main(args: Array<String>) {
    runApplication<Application>(*setup(args))
}

private var debug = false

@SpringBootApplication
@Import(CurrentConfig::class)
class Application {
    companion object {
        /** 是否为 Debug 环境 */
        @JvmStatic
        val DEBUG: Boolean get() = debug
    }
}

class ServletInitializer : SpringBootServletInitializer() {
    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {
        return application.sources(Application::class.java)
    }
}

/** 初始化参数 */
private fun setup(args: Array<String>): Array<String> {
    val argsCurrent = arrayListOf<String>()
    argsCurrent.addAll(args)
    val reader = ArgumentReader(args)
    debug = reader.containsItem("--debug")
    if (reader.getString("--spring.profiles.active", null) == null) {
        val arg = StringBuilder("--spring.profiles.active=")
        if (debug){
            arg.append("dev")
        } else {
            arg.append("pro")
        }
        argsCurrent.add(arg.toString())
    }
    return Array(argsCurrent.size) {
        return@Array argsCurrent[it]
    }
}