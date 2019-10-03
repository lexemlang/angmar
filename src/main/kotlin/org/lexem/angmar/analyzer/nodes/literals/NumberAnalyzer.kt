package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.literals.*
import kotlin.math.*


/**
 * Analyzer for Number literals.
 */
internal object NumberAnalyzer {
    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: NumberNode) {
        try {
            // Parse the integer
            var result = node.integer.toInt(node.radix).toFloat()

            // Parse the decimal
            if (node.decimal != null) {
                val decimal = node.decimal!!.toInt(node.radix)
                result += decimal * node.radix.toFloat().pow(-node.decimal!!.length)
            }

            // Parse the exponent
            if (node.exponent != null) {
                var exponent = node.exponent!!.toInt(node.radix)

                if (!node.exponentSign) {
                    exponent *= -1
                }

                result *= node.radix.toFloat().pow(exponent)
            }

            if (!result.isFinite()) {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.NumberOverflow,
                        "The specified number cannot be saved in a 32-bit float or integer") {
                    val fullText = node.parser.reader.readAllText()
                    addSourceCode(fullText, node.parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(node.from.position(), node.to.position() - 1)
                    }
                }
            }

            // Add an integer or a float
            if (result % 1 == 0.0f) {
                analyzer.memory.pushStack(LxmInteger.from(result.toInt()))
            } else {
                analyzer.memory.pushStack(LxmFloat.from(result))
            }

            return analyzer.nextNode(node.parent, node.parentSignal)
        } catch (e: NumberFormatException) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.NumberOverflow,
                    "The specified number cannot be saved in a 32-bit float or integer") {
                val fullText = node.parser.reader.readAllText()
                addSourceCode(fullText, node.parser.reader.getSource()) {
                    title = Consts.Logger.codeTitle
                    highlightSection(node.from.position(), node.to.position() - 1)
                }
            }
        }
    }
}
