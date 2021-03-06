package io.github.sgpublic.aidescit.api.controller

import io.github.sgpublic.aidescit.api.module.PublicKeyModule
import io.github.sgpublic.aidescit.api.result.SuccessResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.random.Random

@RestController
class PublicKeyController {
    @Autowired
    private lateinit var public: PublicKeyModule

    @RequestMapping("/aidescit/public_key")
    fun getKey(sign: String): Map<String, Any?>{
        val hash = StringBuilder().apply {
            for (i in 0 until 8){
                append(Integer.toHexString(Random.nextInt(16)))
            }
        }.toString()
        return SuccessResult(
            "key" to public.getPublicKey(),
            "hash" to hash
        )
    }
}