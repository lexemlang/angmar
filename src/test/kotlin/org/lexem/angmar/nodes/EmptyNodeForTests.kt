package org.lexem.angmar.nodes

import org.lexem.angmar.LexemParser
import org.lexem.angmar.errors.AngmarException
import org.lexem.angmar.io.ITextReader
import org.lexem.angmar.io.ITextReaderCursor
import org.lexem.angmar.io.printer.TreeLikePrinter

/**
 * Only used to generate empty [ParserNode] for tests.
 */
internal class EmptyNodeForTests : ParserNode {

    constructor(from: Int, type: NodeType, parser: LexemParser) : super(type, parser) {
        this.from = object : ITextReaderCursor {
            override fun char(): Char? {
                throw AngmarException("This method is intentionally not implemented.")
            }

            override fun position(): Int {
                return from
            }

            override fun restore() {
                throw AngmarException("This method is intentionally not implemented.")
            }

            override fun getReader(): ITextReader {
                throw AngmarException("This method is intentionally not implemented.")
            }
        }
    }

    override fun toTree(printer: TreeLikePrinter) {
        throw AngmarException("This method is intentionally not implemented.")
    }
}