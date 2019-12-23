package org.lexem.angmar.compiler.others

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.data.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.literals.*

/**
 * Compiled node that holds a [LxmInterval] and an interval operator.
 */
internal class IntervalConstantCompiled(parent: CompiledNode?, parentSignal: Int, parserNode: ParserNode,
        val operator: IntervalSubIntervalNode.Operator, val interval: IntegerInterval) :
        CompiledNode(parent, parentSignal, parserNode) {

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = throw AngmarUnreachableException()
}
