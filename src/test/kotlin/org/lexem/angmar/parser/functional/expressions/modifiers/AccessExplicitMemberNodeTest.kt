package org.lexem.angmar.parser.functional.expressions.modifiers

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*

internal class AccessExplicitMemberNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${AccessExplicitMemberNode.accessToken}${IdentifierNodeTest.testExpression}"

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is AccessExplicitMemberNode, "The node is not a AccessExplicitMemberNode")
            node as AccessExplicitMemberNode

            IdentifierNodeTest.checkTestExpression(node.identifier)
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = ["${AccessExplicitMemberNode.accessToken}${IdentifierNodeTest.testExpression}"])
    fun `parse correct access explicit member`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = AccessExplicitMemberNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as AccessExplicitMemberNode

        Assertions.assertNotNull(res.identifier, "The identifier property cannot be null")
        IdentifierNodeTest.checkTestExpression(res.identifier)

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = AccessExplicitMemberNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

