package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.config.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.literals.*
import java.text.*
import kotlin.math.*


/**
 * The Lexem values of the Float type.
 */
internal class LxmFloat private constructor(val primitive: Float) : LexemPrimitive {
    val symbols = DecimalFormatSymbols().apply {
        decimalSeparator = '*' // Cannot assign a string so a placeholder is used to replace it latter.
        exponentSeparator = NumberNode.decimalExponentSeparator[0].toString()
        minusSign = '-' // Cannot assign a string so a placeholder is used to replace it latter.
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Returns the number in the specified radix.
     */
    fun toLexemString(radix: Int): LxmString {
        val absPrimitive = abs(primitive)
        val integer = truncate(absPrimitive).toInt()
        val decimal = absPrimitive - integer

        val decimalTxt = StringBuilder().apply {
            var decimalAcc = decimal
            var maxSteps = Consts.Float.maxStepsDuringToString

            while (decimalAcc != 0f && maxSteps > 0) {
                decimalAcc *= radix
                val index = truncate(decimalAcc).toInt()

                when (radix) {
                    2 -> append(NumberNode.binaryDigits[index])
                    8 -> append(NumberNode.octalDigits[index])
                    10 -> append(NumberNode.decimalDigits[index])
                    16 -> append(NumberNode.hexadecimalDigits[index])
                }

                decimalAcc -= index
                maxSteps -= 1
            }
        }.toString()

        var result = if (decimalTxt.isEmpty()) {
            integer.toString(radix)
        } else {
            "${integer.toString(radix)}${NumberNode.decimalSeparator}$decimalTxt"
        }

        if (primitive < 0) {
            result = "${PrefixOperatorNode.negationOperator}$result"
        }
        return LxmString.from(result)
    }

    /**
     * Returns a string representation of the current number in exponential notation.
     */
    fun toExponentialLexemString(precision: Int): LxmString {
        val result = if (precision == 0) {
            val decimalFormat = DecimalFormat("0.E0", symbols)
            decimalFormat.format(primitive)!!.replace(symbols.decimalSeparator.toString(), "")

        } else {
            val decimalFormat = DecimalFormat("0.${"#".repeat(precision)}E0", symbols)
            decimalFormat.format(primitive)!!
        }

        return LxmString.from(replaceSymbols(result))
    }

    /**
     * Returns a string representation of the current number in fixed precision notation.
     */
    fun toFixedLexemString(precision: Int): LxmString {
        val result = if (precision == 0) {
            val decimalFormat = DecimalFormat("0", symbols)
            decimalFormat.format(primitive)!!.replace(symbols.decimalSeparator.toString(), "")

        } else {
            val decimalFormat = DecimalFormat("0.${"0".repeat(precision)}", symbols)
            decimalFormat.format(primitive)!!
        }

        return LxmString.from(replaceSymbols(result))
    }

    /**
     * Replaces a formatted string with the Lexem symbols.
     */
    private fun replaceSymbols(number: String) =
            number.replace(symbols.decimalSeparator.toString(), NumberNode.decimalSeparator).replace(
                    symbols.minusSign.toString(), PrefixOperatorNode.negationOperator)

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getType(bigNode: BigNode): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(bigNode, toWrite = false)
        return context.getPropertyValue(FloatType.TypeName) as LxmReference
    }

    override fun getHashCode() = primitive.hashCode()

    override fun toLexemString(bigNode: BigNode) = toLexemString(10)

    override fun toString() = primitive.toString()

    // STATIC -----------------------------------------------------------------

    companion object {
        val Num_1 = LxmFloat(-1.0f)
        val Num0 = LxmFloat(0.0f)
        val Num0_5 = LxmFloat(0.5f)
        val Num1 = LxmFloat(1.0f)

        /**
         * Returns the [LxmFloat] equivalent of the specified float value.
         */
        fun from(value: Float) = when (value) {
            Num_1.primitive -> Num_1
            Num0.primitive -> Num0
            Num0_5.primitive -> Num0_5
            Num1.primitive -> Num1
            else -> LxmFloat(value)
        }

        /**
         * Returns the [LxmFloat] equivalent of the specified float value or [LxmNil] if the value is NaN.
         */
        fun fromOrNil(value: Float) = if (value.isNaN()) {
            LxmNil
        } else {
            from(value)
        }
    }
}
