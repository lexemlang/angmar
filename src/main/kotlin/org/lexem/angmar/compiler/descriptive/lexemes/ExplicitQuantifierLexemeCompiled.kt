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
 * Compiler for [ExplicitQuantifierLexemeNode].
 */
internal class ExplicitQuantifierLexemeCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: ExplicitQuantifierLexemeNode) : CompiledNode(parent, parentSignal, parserNode) {
    var hasMaximum: Boolean = false
    var maximum: CompiledNode? = null
    lateinit var minimum: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ExplicitQuantifierLexemeAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: ExplicitQuantifierLexemeNode): CompiledNode {
            val result = ExplicitQuantifierLexemeCompiled(parent, parentSignal, node)
            result.hasMaximum = node.hasMaximum
            result.maximum = node.maximum?.compile(result, ExplicitQuantifierLexemeAnalyzer.signalEndMaximum)
            result.minimum = node.minimum.compile(result, ExplicitQuantifierLexemeAnalyzer.signalEndMinimum)

            val minimum = result.minimum
            val maximum = result.maximum
            if (minimum is ConstantCompiled) {
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
                        highlightSection(node.minimum.from.position(), node.minimum.to.position() - 1)
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
                            highlightSection(node.minimum.from.position(), node.minimum.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }

                if (maximum == null) {
                    return ConstantCompiled(parent, parentSignal, node,
                            LxmQuantifier(minimumValue.primitive, isInfinite = result.hasMaximum))
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
