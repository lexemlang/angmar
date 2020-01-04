package org.lexem.angmar.analyzer.data.primitives.setters

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.compiler.functional.expressions.modifiers.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*

/**
 * A setter for an element.
 */
internal class LxmIndexerSetter : LexemSetter {
    val element: LexemPrimitive
    val index: LexemPrimitive
    val node: IndexerCompiled

    // CONSTRUCTOR ------------------------------------------------------------

    constructor(memory: IMemory, element: LexemMemoryValue, index: LexemMemoryValue, node: IndexerCompiled) {
        this.element = element.getPrimitive()
        this.index = index.getPrimitive()
        this.node = node

        // Check the types.
        when (element) {
            is LxmString -> {
                if (index !is LxmInteger) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "${StringType.TypeName} indexers require the index expression be an ${IntegerType.TypeName}") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.parent!!.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.expression.from.position(), node.expression.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }
            }
            is LxmBitList -> {
                if (index !is LxmInteger) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "${BitListType.TypeName} indexers require the index expression be an ${IntegerType.TypeName}.") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.parent!!.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.expression.from.position(), node.expression.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }
            }
            is LxmList -> {
                if (index !is LxmInteger) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "${ListType.TypeName} indexers require the index expression be an ${IntegerType.TypeName}.") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.parent!!.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.expression.from.position(), node.expression.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }
            }
            is LxmObject -> {
                if (index !is LxmString) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "${ObjectType.TypeName} indexers require the index expression be a ${StringType.TypeName}.") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.parent!!.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.expression.from.position(), node.expression.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }
            }
            is LxmMap -> {
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.UndefinedIndexer,
                    "The indexer pattern is not applicable over the element.") {
                val fullText = node.parser.reader.readAllText()
                addSourceCode(fullText, node.parser.reader.getSource()) {
                    title = Consts.Logger.hintTitle
                    highlightSection(node.parent!!.from.position(), node.to.position() - 1)
                    message = "Review the returned values of both expressions"
                }
            }
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getSetterPrimitive(memory: IMemory): LexemPrimitive {
        val element = element.dereference(memory, toWrite = false)
        val index = index

        return when (element) {
            is LxmString -> {
                index as LxmInteger

                val primitive = if (index.primitive < 0) {
                    element.primitive.length + index.primitive
                } else {
                    index.primitive
                }

                if (primitive < 0 || primitive >= element.primitive.length) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds,
                            "Cannot access to the character at $primitive.") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.parent!!.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.expression.from.position(), node.expression.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }

                val char = element.primitive[primitive]
                LxmString.from("$char")
            }
            is LxmBitList -> {
                index as LxmInteger

                val primitive = if (index.primitive < 0) {
                    element.primitive.size + index.primitive
                } else {
                    index.primitive
                }

                if (primitive < 0 || primitive >= element.primitive.size) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds,
                            "Cannot access to the character at $primitive.") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.parent!!.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.expression.from.position(), node.expression.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }

                LxmLogic.from(element.primitive[primitive])
            }
            is LxmList -> {
                index as LxmInteger

                val primitive = if (index.primitive < 0) {
                    element.size + index.primitive
                } else {
                    index.primitive
                }

                if (primitive < 0 || primitive >= element.size) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds,
                            "Cannot access to the character at $primitive.") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.parent!!.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.expression.from.position(), node.expression.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }

                element.getCell(primitive)!!
            }
            is LxmObject -> {
                index as LxmString

                element.getPropertyValue(memory, index.primitive) ?: LxmNil
            }
            is LxmMap -> {
                element.getPropertyValue(index) ?: LxmNil
            }
            else -> throw AngmarUnreachableException()
        }
    }

    override fun setSetterValue(memory: IMemory, value: LexemMemoryValue) {
        val element = element.dereference(memory, toWrite = true)
        val index = index

        when (element) {
            is LxmList -> {
                index as LxmInteger

                val primitive = if (index.primitive < 0) {
                    element.size + index.primitive
                } else {
                    index.primitive
                }

                if (primitive < 0 || primitive >= element.size) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds,
                            "Cannot access to the character at $primitive.") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.parent!!.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.expression.from.position(), node.expression.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }

                element.setCell(memory, primitive, value)
            }
            is LxmObject -> {
                index as LxmString

                element.setProperty(memory, index.primitive, value)
            }
            is LxmMap -> {
                element.setProperty(memory, index, value)
            }
            else -> throw AngmarUnreachableException()
        }
    }

    override fun increaseReferences(memory: IMemory) {
        element.increaseReferences(memory)
        index.increaseReferences(memory)
    }

    override fun decreaseReferences(memory: IMemory) {
        element.decreaseReferences(memory)
        index.decreaseReferences(memory)
    }

    override fun spatialGarbageCollect(memory: IMemory, gcFifo: GarbageCollectorFifo) {
        element.spatialGarbageCollect(memory, gcFifo)
        index.spatialGarbageCollect(memory, gcFifo)
    }

    override fun toString() = "[Setter - Indexer] (element: $element, index: $index)"
}
