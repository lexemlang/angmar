package org.lexem.angmar.analyzer.nodes.functional.expressions

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import java.util.*

internal class PrefixOperatorAnalyzerTest {
    @Test
    fun `test not`() {
        val text = "${PrefixOperatorNode.notOperator}${LogicNode.trueLiteral}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = PrefixExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.False, analyzer.memory.popStack(),
                "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test affirmation`() {
        val text = "${PrefixOperatorNode.affirmationOperator}3"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = PrefixExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(3, result.primitive, "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test negation`() {
        val text = "${PrefixOperatorNode.negationOperator}3"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = PrefixExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(-3, result.primitive, "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test bitwise negation`() {
        val text =
                "${PrefixOperatorNode.bitwiseNegationOperator}${BitlistNode.binaryPrefix}${BitlistNode.startToken}0110${BitlistNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = PrefixExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue =
                analyzer.memory.popStack() as? LxmBitList ?: throw Error("The value of the stack must be a LxmBitList")
        val resultValue = BitSet()
        resultValue[0] = true
        resultValue[3] = true
        Assertions.assertEquals(4, stackValue.size, "The size property of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, stackValue.primitive, "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
