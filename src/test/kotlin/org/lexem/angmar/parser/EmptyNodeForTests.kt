package org.lexem.angmar.parser

import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.*

/**
 * Only used to generate empty [ParserNode] for tests.
 */
internal class EmptyNodeForTests : ParserNode {
    constructor(from: Int, parser: LexemParser) : super(parser, null) {
        this.from = object : ITextReaderCursor {
            override fun char() = throw AngmarUnreachableException()

            override fun position() = from

            override fun lineColumn() = Pair(1, 1)

            override fun restore() = throw AngmarUnreachableException()

            override fun getReader() = throw AngmarUnreachableException()
        }
    }

    override fun toTree() = throw AngmarUnreachableException()

    override fun compile(parent: CompiledNode, parentSignal: Int) = throw AngmarUnreachableException()
}
