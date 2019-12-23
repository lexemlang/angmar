package org.lexem.angmar.compiler.literals

import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.literals.*
import kotlin.math.*

/**
 * Compiler for [NumberNode].
 */
internal object NumberCompiler {
    fun compile(parent: CompiledNode, parentSignal: Int, node: NumberNode): ConstantCompiled {
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
                throw AngmarCompilerException(AngmarCompilerExceptionType.NumberOverflow,
                        "The specified number cannot be saved in a 32-bit float or integer") {
                    val fullText = node.parser.reader.readAllText()
                    addSourceCode(fullText, node.parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(node.from.position(), node.to.position() - 1)
                    }
                }
            }

            // Add an integer or a float
            val resultPrimitive = if (result % 1 == 0.0f) {
                LxmInteger.from(result.toInt())
            } else {
                LxmFloat.from(result)
            }

            return ConstantCompiled(parent, parentSignal, node, resultPrimitive)
        } catch (e: NumberFormatException) {
            throw AngmarCompilerException(AngmarCompilerExceptionType.NumberOverflow,
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
