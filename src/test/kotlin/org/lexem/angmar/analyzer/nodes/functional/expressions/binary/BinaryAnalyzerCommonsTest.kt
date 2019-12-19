package org.lexem.angmar.analyzer.nodes.functional.expressions.binary

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.AnalyzerCommons.getCurrentContext
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class BinaryAnalyzerCommonsTest {
    @Test
    fun `test get operator function`() {
        val text = "${LogicNode.falseLiteral} ${LogicalExpressionNode.xorOperator} ${LogicNode.trueLiteral}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = LogicalExpressionNode.Companion::parse)

        val operand = (analyzer.grammarRootNode as LogicalExpressionNode).expressions[0]

        val function =
                BinaryAnalyzerCommons.getOperatorFunction(analyzer, LxmLogic.True, analyzer.grammarRootNode, operand,
                        LogicalExpressionNode.xorOperator, AnalyzerCommons.Operators.LogicalXor)

        val directFunction =
                getCurrentContext(analyzer.memory, toWrite = false).getDereferencedProperty<LxmObject>(analyzer.memory,
                        LogicType.TypeName, toWrite = false)
                        ?.getDereferencedProperty<LxmObject>(analyzer.memory, AnalyzerCommons.Identifiers.Prototype,
                                toWrite = false)
                        ?.getPropertyValue(analyzer.memory, AnalyzerCommons.Operators.LogicalXor)
        Assertions.assertEquals(directFunction, function.getPrimitive(), "The function is incorrect")
    }

    @Test
    @Incorrect
    fun `test get undefined operator function`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.UndefinedObjectProperty) {
            val text = "${NilNode.nilLiteral} ${LogicalExpressionNode.xorOperator} ${LogicNode.trueLiteral}"
            val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = LogicalExpressionNode.Companion::parse)

            val operand = (analyzer.grammarRootNode as LogicalExpressionNode).expressions[0]

            BinaryAnalyzerCommons.getOperatorFunction(analyzer, LxmNil, analyzer.grammarRootNode, operand,
                    LogicalExpressionNode.xorOperator, AnalyzerCommons.Operators.LogicalXor)
        }
    }

    @Test
    @Incorrect
    fun `test get not callable operator function`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType) {
            val text = "${LogicNode.trueLiteral} ${LogicalExpressionNode.xorOperator} ${LogicNode.trueLiteral}"
            val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = LogicalExpressionNode.Companion::parse)

            val logicTypeRef = getCurrentContext(analyzer.memory, toWrite = false).getPropertyValue(analyzer.memory,
                    LogicType.TypeName)!! // Trick: change to a type
            val operand = (analyzer.grammarRootNode as LogicalExpressionNode).expressions[0]

            BinaryAnalyzerCommons.getOperatorFunction(analyzer, logicTypeRef, analyzer.grammarRootNode, operand,
                    LogicalExpressionNode.xorOperator,
                    AnalyzerCommons.Identifiers.Prototype) // Trick: use .prototype instead of the actual function name
        }
    }

    @Test
    fun `test create arguments`() {
        val text = LogicNode.falseLiteral
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = LogicNode.Companion::parse)

        val left = LxmNil
        val right = LxmLogic.True

        val arguments = BinaryAnalyzerCommons.createArguments(analyzer, left, right)
        val map = arguments.mapArguments(analyzer.memory, AnalyzerCommons.Operators.ParameterList)

        Assertions.assertEquals(2, map.size, "The number of parameters is incorrect")
        Assertions.assertEquals(left, map[AnalyzerCommons.Identifiers.This],
                "The ${AnalyzerCommons.Identifiers.This} parameter is incorrect")
        Assertions.assertEquals(right, map[AnalyzerCommons.Operators.RightParameterName],
                "The right parameter is incorrect")
    }
}
