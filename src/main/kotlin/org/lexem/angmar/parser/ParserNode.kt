package org.lexem.angmar.parser

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.io.readers.*

/**
 * Generic object for parser nodes.
 */
internal abstract class ParserNode(val parser: LexemParser, var parent: ParserNode?, var parentSignal: Int) :
        ITreeLikePrintable {
    var from: ITextReaderCursor = ITextReaderCursor.Empty
    var to: ITextReaderCursor = ITextReaderCursor.Empty

    val content by lazy {
        parser.reader.substring(from, to)
    }

    /**
     * Executes this code.
     */
    abstract fun analyze(analyzer: LexemAnalyzer, signal: Int)

    // STATIC -----------------------------------------------------------------

    companion object {
        object EmptyParserNode : ParserNode(LexemParser(CustomStringReader.from("")), null, 0) {
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

            override fun toTree(): JsonObject {
                val result = super.toTree()

                throw AngmarUnreachableException()
            }
        }
    }
}
