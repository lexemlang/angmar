package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class PropertyStyleObjectAnalyzerTest {
    @Test
    fun `test normal`() {
        val propName = "test"
        val text =
                "${PropertyStyleObjectNode.startToken}${PropertyStyleObjectBlockNode.startToken}$propName${PropertyStyleObjectBlockNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = PropertyStyleObjectNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef = analyzer.memory.popStack() as? LxmReference ?: throw Error("The result must be a LxmReference")
        val result =
                resultRef.dereferenceAs<LxmObject>(analyzer.memory) ?: throw Error("The result must be a LxmObject")

        val property = result.getPropertyValue(analyzer.memory, propName)
        Assertions.assertEquals(LxmLogic.True, property, "The property [$propName] is incorrect")
        Assertions.assertFalse(result.isImmutable, "The isImmutable property is incorrect")

        // Decrease the reference count.
        resultRef.decreaseReferenceCount(analyzer.memory)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test constant`() {
        val propName = "test"
        val text =
                "${PropertyStyleObjectNode.startToken}${PropertyStyleObjectNode.constantToken}${PropertyStyleObjectBlockNode.startToken}$propName${PropertyStyleObjectBlockNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = PropertyStyleObjectNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef = analyzer.memory.popStack() as? LxmReference ?: throw Error("The result must be a LxmReference")
        val result =
                resultRef.dereferenceAs<LxmObject>(analyzer.memory) ?: throw Error("The result must be a LxmObject")

        val property = result.getPropertyValue(analyzer.memory, propName)
        Assertions.assertEquals(LxmLogic.True, property, "The property [$propName] is incorrect")
        Assertions.assertTrue(result.isImmutable, "The isImmutable property is incorrect")

        // Decrease the reference count.
        resultRef.decreaseReferenceCount(analyzer.memory)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
