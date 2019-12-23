package org.lexem.angmar.compiler.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.descriptive.lexemes.*

/**
 * Compiler for [QuantifiedGroupModifierNode].
 */
internal class QuantifiedGroupModifierCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: QuantifiedGroupModifierNode) : CompiledNode(parent, parentSignal, parserNode) {
    var minimum: CompiledNode? = null
    var hasMaximum: Boolean = false
    var maximum: CompiledNode? = null

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            QuantifiedGroupModifierAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: QuantifiedGroupModifierNode): CompiledNode {
            val result = QuantifiedGroupModifierCompiled(parent, parentSignal, node)
            result.hasMaximum = node.hasMaximum
            result.minimum = node.minimum?.compile(result, QuantifiedGroupModifierAnalyzer.signalEndMinimum)
            result.maximum = node.maximum?.compile(result, QuantifiedGroupModifierAnalyzer.signalEndMaximum)

            val minimum = result.minimum
            val maximum = result.maximum
            if (minimum == null) {
                val minimumValue = LxmInteger.Num_1

                if (maximum is ConstantCompiled) {
                    val maximumValue = maximum.value as? LxmInteger ?: throw AngmarCompilerException(
                            AngmarCompilerExceptionType.IncompatibleType,
                            "The maximum value of a quantifier must be of type ${IntegerType.TypeName}.") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.maximum!!.from.position(), node.maximum!!.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }

                    if (maximumValue.primitive < 0) {
                        throw AngmarCompilerException(AngmarCompilerExceptionType.IncorrectQuantifierBounds,
                                "The maximum value cannot be lower than 0. Actual: $maximum") {
                            val fullText = node.parser.reader.readAllText()
                            addSourceCode(fullText, node.parser.reader.getSource()) {
                                title = Consts.Logger.codeTitle
                                highlightSection(node.from.position(), node.to.position() - 1)
                            }
                            addSourceCode(fullText) {
                                title = Consts.Logger.hintTitle
                                highlightSection(node.maximum!!.from.position(), node.maximum!!.to.position() - 1)
                                message = "Review the returned value of this expression"
                            }
                        }
                    }

                    return ConstantCompiled(parent, parentSignal, node,
                            LxmQuantifier(minimumValue.primitive, maximumValue.primitive))
                }
            } else if (minimum is ConstantCompiled) {
                val minimumValue = minimum.value as? LxmInteger ?: throw AngmarCompilerException(
                        AngmarCompilerExceptionType.IncompatibleType,
                        "The minimum value of a quantifier must be of type ${IntegerType.TypeName}.") {
                    val fullText = node.parser.reader.readAllText()
                    addSourceCode(fullText, node.parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(node.from.position(), node.to.position() - 1)
                    }
                    addSourceCode(fullText) {
                        title = Consts.Logger.hintTitle
                        highlightSection(node.minimum!!.from.position(), node.minimum!!.to.position() - 1)
                        message = "Review the returned value of this expression"
                    }
                }

                if (minimumValue.primitive < 0) {
                    throw AngmarCompilerException(AngmarCompilerExceptionType.IncorrectQuantifierBounds,
                            "The minimum value of a quantifier cannot be negative. Actual: $minimum") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.minimum!!.from.position(), node.minimum!!.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }

                if (maximum == null) {
                    val quantifierValue = if (node.hasMaximum) {
                        LxmQuantifier(minimumValue.primitive, -1)
                    } else {
                        LxmQuantifier(minimumValue.primitive)
                    }

                    return ConstantCompiled(parent, parentSignal, node, quantifierValue)
                } else if (maximum is ConstantCompiled) {
                    val maximumValue = maximum.value as? LxmInteger ?: throw AngmarCompilerException(
                            AngmarCompilerExceptionType.IncompatibleType,
                            "The maximum value of a quantifier must be of type ${IntegerType.TypeName}.") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.maximum!!.from.position(), node.maximum!!.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }

                    if (maximumValue.primitive < minimumValue.primitive) {
                        throw AngmarCompilerException(AngmarCompilerExceptionType.IncorrectQuantifierBounds,
                                "The maximum value cannot be lower than the minimum. Actual: {min: $minimum, max: $maximum}") {
                            val fullText = node.parser.reader.readAllText()
                            addSourceCode(fullText, node.parser.reader.getSource()) {
                                title = Consts.Logger.codeTitle
                                highlightSection(node.from.position(), node.to.position() - 1)
                            }
                            addNote(Consts.Logger.hintTitle, "Review the returned values of both expressions")
                        }
                    }

                    return ConstantCompiled(parent, parentSignal, node,
                            LxmQuantifier(minimumValue.primitive, maximumValue.primitive))
                }
            }

            return result
        }
    }
}
