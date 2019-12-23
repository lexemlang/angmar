package org.lexem.angmar.compiler

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*

/**
 * Generic object for compiled nodes.
 */
internal abstract class CompiledNode(var parent: CompiledNode?, var parentSignal: Int, var parserNode: ParserNode) {
    var parser = parserNode.parser
    var from = parserNode.from
    var to = parserNode.to

    val content by lazy {
        parser.reader.substring(from, to)
    }

    /**
     * Executes this code.
     */
    open fun analyze(analyzer: LexemAnalyzer, signal: Int): Unit = throw AngmarUnreachableException()

    /**
     * Re-links the [CompiledNode] to other nodes.
     */
    fun linkTo(parent: CompiledNode?, parentSignal: Int, parserNode: ParserNode): CompiledNode {
        this.parent = parent
        this.parentSignal = parentSignal
        this.parserNode = parserNode

        return this
    }

    /**
     * Changes the parent associated to the [CompiledNode].
     */
    fun changeParent(parent: CompiledNode?): CompiledNode {
        this.parent = parent

        return this
    }

    /**
     * Changes the parent signal associated to the [CompiledNode].
     */
    fun changeParentSignal(parentSignal: Int): CompiledNode {
        this.parentSignal = parentSignal

        return this
    }

    /**
     * Changes the [ParserNode] associated to the [CompiledNode].
     */
    fun changeParserNode(parserNode: ParserNode): CompiledNode {
        this.parserNode = parserNode

        return this
    }

    // STATIC -----------------------------------------------------------------

    companion object {
        object EmptyCompiledNode : CompiledNode(null, 0, ParserNode.Companion.EmptyParserNode) {
            override fun analyze(analyzer: LexemAnalyzer, signal: Int) {
                when (signal) {
                    // Propagate the control signal.
                    AnalyzerNodesCommons.signalExitControl, AnalyzerNodesCommons.signalNextControl, AnalyzerNodesCommons.signalRedoControl -> {
                        throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.TestControlSignalRaised, "") {}
                    }
                    AnalyzerNodesCommons.signalRestartControl, AnalyzerNodesCommons.signalReturnControl -> {
                        throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.TestControlSignalRaised, "") {}
                    }
                }

                return analyzer.nextNode(null)
            }
        }
    }
}
