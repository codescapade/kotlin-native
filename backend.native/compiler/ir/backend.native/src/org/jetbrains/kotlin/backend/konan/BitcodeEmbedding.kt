package org.jetbrains.kotlin.backend.konan

import org.jetbrains.kotlin.backend.konan.llvm.Int8
import org.jetbrains.kotlin.backend.konan.llvm.Llvm
import org.jetbrains.kotlin.konan.target.CompilerOutputKind
import org.jetbrains.kotlin.konan.target.Family

object BitcodeEmbedding {

    enum class Mode {
        NONE, MARKER, FULL
    }

    internal fun getLinkerOptions(config: KonanConfig): List<String> = when (config.bitcodeEmbeddingMode) {
        Mode.NONE -> emptyList()
        Mode.MARKER -> listOf("-bitcode_bundle", "-bitcode_process_mode", "marker")
        Mode.FULL -> listOf("-bitcode_bundle")
    }

    internal fun getClangOptions(config: KonanConfig): List<String> = when (config.bitcodeEmbeddingMode) {
        BitcodeEmbedding.Mode.NONE -> listOf("-fembed-bitcode=off")
        BitcodeEmbedding.Mode.MARKER -> listOf("-fembed-bitcode=marker")
        BitcodeEmbedding.Mode.FULL -> listOf("-fembed-bitcode=all")
    }

    private fun KonanConfig.shouldForceBitcodeEmbedding() =
            target.family == Family.TVOS

    private val KonanConfig.bitcodeEmbeddingMode: Mode
        get() = if (shouldForceBitcodeEmbedding()) {
            if (debug) Mode.MARKER else Mode.FULL
        } else {
            configuration.get(KonanConfigKeys.BITCODE_EMBEDDING_MODE)!!
        }.also {
            require(it == Mode.NONE || this.produce == CompilerOutputKind.FRAMEWORK) {
                "${it.name.toLowerCase()} bitcode embedding mode is not supported when producing ${this.produce.name.toLowerCase()}"
            }
        }
}