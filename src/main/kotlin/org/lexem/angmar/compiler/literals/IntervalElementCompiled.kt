package org.lexem.angmar.compiler.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.config.*
import org.lexem.angmar.data.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.literals.*

/**
 * Compiler for [IntervalElementNode].
 */
internal class IntervalElementCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: IntervalElementNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var left: CompiledNode
    var right: CompiledNode? = null
    var constantValue: IntegerRange? = null

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            IntervalElementAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: IntervalElementNode): CompiledNode {
            val result = IntervalElementCompiled(parent, parentSignal, node)

            val left = node.left.compile(result, IntervalElementAnalyzer.signalEndLeft)
            val right = node.right?.compile(result, IntervalElementAnalyzer.signalEndRight)

            result.left = left
            result.right = right

            if (left is ConstantCompiled) {
                if (left.value !is LxmInteger) {
                    val msg = if (node.right != null) {
                        "The returned value by the left expression must be an ${IntegerType.TypeName}."
                    } else {
                        "The returned value by the expression must be an ${IntegerType.TypeName}."
                    }

                    throw AngmarCompilerException(AngmarCompilerExceptionType.IncompatibleType, msg) {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.left.from.position(), node.left.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }

                if (right is ConstantCompiled) {
                    if (right.value !is LxmInteger) {
                        throw AngmarCompilerException(AngmarCompilerExceptionType.IncompatibleType,
                                "The returned value by the right expression must be an ${IntegerType.TypeName}.") {
                            val fullText = node.parser.reader.readAllText()
                            addSourceCode(fullText, node.parser.reader.getSource()) {
                                title = Consts.Logger.codeTitle
                                highlightSection(node.from.position(), node.to.position() - 1)
                            }
                            addSourceCode(fullText) {
                                title = Consts.Logger.hintTitle
                                highlightSection(node.right!!.from.position(), node.right!!.to.position() - 1)
                                message = "Review the returned value of this expression"
                            }
                        }
                    }

                    if (left.value.primitive > right.value.primitive) {
                        throw AngmarCompilerException(AngmarCompilerExceptionType.IncorrectRangeBounds,
                                "The left value must be lower or equal than the right value. Actual: {left: ${left.value.primitive}, right: ${right.value.primitive}}") {
                            val fullText = node.parser.reader.readAllText()
                            addSourceCode(fullText, node.parser.reader.getSource()) {
                                title = Consts.Logger.codeTitle
                                highlightSection(node.from.position(), node.to.position() - 1)
                            }
                        }
                    }

                    result.constantValue = IntegerRange.new(left.value.primitive, right.value.primitive)
                } else if (right == null) {
                    result.constantValue = IntegerRange.new(left.value.primitive, left.value.primitive)
                }
            }

            return result
        }
    }
}
