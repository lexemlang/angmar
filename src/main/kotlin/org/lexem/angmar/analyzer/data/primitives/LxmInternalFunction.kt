package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

/**
 * The Lexem value of the InternalFunction type.
 */
internal class LxmInternalFunction(
        val function: (analyzer: LexemAnalyzer, arguments: LxmArguments, signal: Int) -> Boolean) : LexemPrimitive,
        ExecutableValue {

    // OVERRIDE METHODS -------------------------------------------------------

    override val parserNode: ParserNode? = InternalFunctionCallNode

    override val parentContext: LxmReference? = null

    override fun getType(memory: LexemMemory) =
            AnalyzerCommons.getStdLibContextElement<LxmObject>(memory, FunctionType.TypeName)

    override fun getHashCode(memory: LexemMemory) = function.hashCode()

    override fun toString() = "[Internal Function]"

    // STATIC -----------------------------------------------------------------

    data class LxmObjectProperty(val position: Int, var isConstant: Boolean)
}
