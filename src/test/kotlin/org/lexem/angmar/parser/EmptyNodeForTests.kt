package org.lexem.angmar.parser

import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.*
import org.lexem.angmar.io.printer.*

/**
 * Only used to generate empty [ParserNode] for tests.
 */
internal class EmptyNodeForTests : ParserNode {
    constructor(from: Int, parser: LexemParser) : super(parser) {
        this.from = object : ITextReaderCursor {
            override fun char(): Char? {
                throw AngmarUnimplementedException()
            }

            override fun position(): Int {
                return from
            }

            override fun restore() {
                throw AngmarUnimplementedException()
            }

            override fun getReader(): ITextReader {
                throw AngmarUnimplementedException()
            }
        }
    }

    override fun toTree(printer: TreeLikePrinter) {
        throw AngmarUnimplementedException()
    }
}
