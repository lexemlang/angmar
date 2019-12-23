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
 * Compiler for [UnicodeIntervalElementNode].
 */
internal class UnicodeIntervalElementCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: UnicodeIntervalElementNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var left: CompiledNode
    var right: CompiledNode? = null
    var constantValue: IntegerRange? = null

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            UnicodeIntervalElementAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: UnicodeIntervalElementNode): CompiledNode {
            val result = UnicodeIntervalElementCompiled(parent, parentSignal, node)

            val left =
                    node.left?.compile(result, UnicodeIntervalElementAnalyzer.signalEndLeft) ?: ConstantCompiled(result,
                            UnicodeIntervalElementAnalyzer.signalEndLeft, node, LxmInteger.from(node.leftChar.toInt()))
            val right = node.right?.compile(result, UnicodeIntervalElementAnalyzer.signalEndRight)
                    ?: if (node.rightChar != ' ') {
                        ConstantCompiled(result, UnicodeIntervalElementAnalyzer.signalEndLeft, node,
                                LxmInteger.from(node.rightChar.toInt()))
                    } else {
                        null
                    }

            result.left = left
            result.right = right

            if (left is ConstantCompiled && (right == null || right is ConstantCompiled)) {
                // Check left.
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
                            highlightSection(node.left!!.from.position(), node.left!!.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }

                result.constantValue = if (right is ConstantCompiled) {
                    // Check right.
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

                    IntegerRange.new(left.value.primitive, right.value.primitive)
                } else {
                    IntegerRange.new(left.value.primitive)
                }
            }

            return result
        }
    }
}
