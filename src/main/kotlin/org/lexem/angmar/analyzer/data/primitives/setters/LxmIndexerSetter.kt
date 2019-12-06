package org.lexem.angmar.analyzer.data.primitives.setters

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

/**
 * A setter for an element.
 */
internal class LxmIndexerSetter : LexemSetter {
    val element: LexemPrimitive
    val index: LexemPrimitive
    val node: IndexerNode

    // CONSTRUCTOR ------------------------------------------------------------

    constructor(element: LexemPrimitive, index: LexemPrimitive, node: IndexerNode, memory: LexemMemory) {
        this.element = element
        this.index = index
        this.node = node

        if (element is LxmReference) {
            element.increaseReferences(memory)
        }

        if (index is LxmReference) {
            index.increaseReferences(memory)
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getPrimitive(memory: LexemMemory): LexemPrimitive {
        val element = element.dereference(memory)
        val index = index

        return when (element) {
            is LxmString -> {
                if (index !is LxmInteger) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "${StringType.TypeName} indexers require the index expression be an ${IntegerType.TypeName}. Actual value: ${index.dereference(
                                    memory)}") {
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

                val primitive = if (index.primitive < 0) {
                    element.primitive.length + index.primitive
                } else {
                    index.primitive
                }

                if (primitive < 0 || primitive >= element.primitive.length) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds,
                            "Cannot access to the character at $primitive. Actual value: $element") {
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
                if (index !is LxmInteger) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "${BitListType.TypeName} indexers require the index expression be an ${IntegerType.TypeName}. Actual value: ${index.dereference(
                                    memory)}") {
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

                val primitive = if (index.primitive < 0) {
                    element.size + index.primitive
                } else {
                    index.primitive
                }

                if (primitive < 0 || primitive >= element.size) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds,
                            "Cannot access to the character at $primitive. Actual value: $element") {
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
                if (index !is LxmInteger) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "${ListType.TypeName} indexers require the index expression be an ${IntegerType.TypeName}. Actual value: ${index.dereference(
                                    memory)}") {
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

                val primitive = if (index.primitive < 0) {
                    element.actualListSize + index.primitive
                } else {
                    index.primitive
                }

                if (primitive < 0 || primitive >= element.actualListSize) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds,
                            "Cannot access to the character at $primitive. Actual value: $element") {
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

                element.getCell(memory, primitive)!!
            }
            is LxmObject -> {
                if (index !is LxmString) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "${ObjectType.TypeName} indexers require the index expression be a ${StringType.TypeName}. Actual value: ${index.dereference(
                                    memory)}") {
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

                element.getPropertyValue(memory, index.primitive) ?: LxmNil
            }
            is LxmMap -> {
                element.getPropertyValue(memory, index) ?: LxmNil
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.UndefinedIndexer,
                    "The indexer getter is not applicable over the element ($element) with the index expression (${index.dereference(
                            memory)})") {
                val fullText = node.parser.reader.readAllText()
                addSourceCode(fullText, node.parser.reader.getSource()) {
                    title = Consts.Logger.hintTitle
                    highlightSection(node.parent!!.from.position(), node.to.position() - 1)
                    message = "Review the returned values of both expressions"
                }
            }
        }
    }

    override fun setPrimitive(memory: LexemMemory, value: LexemPrimitive) {
        val element = element.dereference(memory)
        val index = index

        when (element) {
            is LxmList -> {
                if (index !is LxmInteger) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "${ListType.TypeName} indexers require the index expression be an ${IntegerType.TypeName}. Actual value: ${index.dereference(
                                    memory)}") {
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

                val primitive = if (index.primitive < 0) {
                    element.actualListSize + index.primitive
                } else {
                    index.primitive
                }

                if (primitive < 0 || primitive >= element.actualListSize) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IndexOutOfBounds,
                            "Cannot access to the character at $primitive. Actual value: $element") {
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
                if (index !is LxmString) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "${ObjectType.TypeName} indexers require the index expression be a ${StringType.TypeName}. Actual value: ${index.dereference(
                                    memory)}") {
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

                element.setProperty(memory, index.primitive, value)
            }
            is LxmMap -> {
                element.setProperty(memory, index, value)
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.UndefinedIndexer,
                    "The indexer setter is not applicable over the element ($element) with the index expression (${index.dereference(
                            memory)})") {
                val fullText = node.parser.reader.readAllText()
                addSourceCode(fullText, node.parser.reader.getSource()) {
                    title = Consts.Logger.hintTitle
                    highlightSection(node.parent!!.from.position(), node.to.position() - 1)
                    message = "Review the returned values of both expressions"
                }
            }
        }
    }

    override fun increaseReferences(memory: LexemMemory) {
        element.increaseReferences(memory)
        index.increaseReferences(memory)
    }

    override fun decreaseReferences(memory: LexemMemory) {
        element.decreaseReferences(memory)
        index.decreaseReferences(memory)
    }

    override fun spatialGarbageCollect(memory: LexemMemory) {
        element.spatialGarbageCollect(memory)
        index.spatialGarbageCollect(memory)
    }

    override fun toString() = "LxmSetter(element: $element, index: $index)"
}
