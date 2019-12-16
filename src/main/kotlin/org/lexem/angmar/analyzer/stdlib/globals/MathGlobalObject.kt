package org.lexem.angmar.analyzer.stdlib.globals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import kotlin.math.*


/**
 * Built-in Math global object.
 */
internal object MathGlobalObject {
    const val ObjectName = "Math"

    // Properties
    const val E = "E"
    const val Ln2 = "LN2"
    const val Ln10 = "LN10"
    const val Log10_E = "LOG10_E"
    const val Log10_2 = "LOG10_2"
    const val Log2_E = "LOG2_E"
    const val Log2_10 = "LOG2_10"
    const val Pi = "PI"
    const val Sqrt1_2 = "SQRT1_2"
    const val Sqrt2 = "SQRT2"
    const val Rad2Deg = "RAD2DEG"
    const val Deg2Rad = "DEG2RAD"

    // Methods
    const val Abs = "abs"
    const val Acos = "acos"
    const val Acosh = "acosh"
    const val Asin = "asin"
    const val Asinh = "asinh"
    const val Atan = "atan"
    const val Atanh = "atanh"
    const val Atan2 = "atan2"
    const val Cbrt = "cbrt"
    const val Ceil = "ceil"
    const val Clz32 = "clz32"
    const val Cos = "cos"
    const val Cosh = "cosh"
    const val Exp = "exp"
    const val Floor = "floor"
    const val Hypot = "hypot"
    const val Ln = "ln"
    const val Log10 = "log10"
    const val Log2 = "log2"
    const val Max = "max"
    const val Min = "min"
    const val Pow = "pow"
    const val Random = "random"
    const val Round = "round"
    const val Sign = "sign"
    const val Sin = "sin"
    const val Sinh = "sinh"
    const val Sqrt = "sqrt"
    const val Tan = "tan"
    const val Tanh = "tanh"
    const val Trunc = "trunc"

    // Method arguments
    private val AbsArgs = listOf("number")
    private val AcosArgs = AbsArgs
    private val AcoshArgs = AbsArgs
    private val AsinArgs = AbsArgs
    private val AsinhArgs = AbsArgs
    private val AtanArgs = AbsArgs
    private val AtanhArgs = AbsArgs
    private val Atan2Args = listOf("y", "x")
    private val CbrtArgs = AbsArgs
    private val CeilArgs = AbsArgs
    private val Clz32Args = AbsArgs
    private val CosArgs = AbsArgs
    private val CoshArgs = AbsArgs
    private val ExpArgs = AbsArgs
    private val FloorArgs = AbsArgs
    private val LnArgs = AbsArgs
    private val Log10Args = AbsArgs
    private val Log2Args = AbsArgs
    private val PowArgs = listOf("base", "exponent")
    private val RoundArgs = AbsArgs
    private val SignArgs = AbsArgs
    private val SinArgs = AbsArgs
    private val SinhArgs = AbsArgs
    private val SqrtArgs = AbsArgs
    private val TanArgs = AbsArgs
    private val TanhArgs = AbsArgs
    private val TruncArgs = AbsArgs

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the global object.
     */
    fun initObject(memory: LexemMemory) {
        val objectValue = LxmObject(memory)
        val reference = memory.add(objectValue)
        AnalyzerCommons.getCurrentContext(memory, toWrite = true)
                .setProperty(memory, ObjectName, reference, isConstant = true)

        // Properties
        objectValue.setProperty(memory, E, LxmFloat.fromOrNil(Math.E.toFloat()), isConstant = true)
        objectValue.setProperty(memory, Ln2, LxmFloat.fromOrNil(ln(2.0f)), isConstant = true)
        objectValue.setProperty(memory, Ln10, LxmFloat.fromOrNil(ln(10.0f)), isConstant = true)
        objectValue.setProperty(memory, Log10_E, LxmFloat.fromOrNil(log10(Math.E.toFloat())), isConstant = true)
        objectValue.setProperty(memory, Log10_2, LxmFloat.fromOrNil(log10(2.0f)), isConstant = true)
        objectValue.setProperty(memory, Log2_E, LxmFloat.fromOrNil(log(Math.E.toFloat(), 2.0f)), isConstant = true)
        objectValue.setProperty(memory, Log2_10, LxmFloat.fromOrNil(log(10.0f, 2.0f)), isConstant = true)
        objectValue.setProperty(memory, Pi, LxmFloat.fromOrNil(Math.PI.toFloat()), isConstant = true)
        objectValue.setProperty(memory, Sqrt1_2, LxmFloat.fromOrNil(sqrt(0.5f)), isConstant = true)
        objectValue.setProperty(memory, Sqrt2, LxmFloat.fromOrNil(sqrt(2.0f)), isConstant = true)
        objectValue.setProperty(memory, Rad2Deg, LxmFloat.fromOrNil(180f / Math.PI.toFloat()), isConstant = true)
        objectValue.setProperty(memory, Deg2Rad, LxmFloat.fromOrNil(Math.PI.toFloat() / 180f), isConstant = true)

        // Methods
        objectValue.setProperty(memory, Abs, memory.add(LxmFunction(memory, ::absFunction)), isConstant = true)
        objectValue.setProperty(memory, Acos, memory.add(LxmFunction(memory, ::acosFunction)), isConstant = true)
        objectValue.setProperty(memory, Acosh, memory.add(LxmFunction(memory, ::acoshFunction)), isConstant = true)
        objectValue.setProperty(memory, Asin, memory.add(LxmFunction(memory, ::asinFunction)), isConstant = true)
        objectValue.setProperty(memory, Asinh, memory.add(LxmFunction(memory, ::asinhFunction)), isConstant = true)
        objectValue.setProperty(memory, Atan, memory.add(LxmFunction(memory, ::atanFunction)), isConstant = true)
        objectValue.setProperty(memory, Atanh, memory.add(LxmFunction(memory, ::atanhFunction)), isConstant = true)
        objectValue.setProperty(memory, Atan2, memory.add(LxmFunction(memory, ::atan2Function)), isConstant = true)
        objectValue.setProperty(memory, Cbrt, memory.add(LxmFunction(memory, ::cbrtFunction)), isConstant = true)
        objectValue.setProperty(memory, Ceil, memory.add(LxmFunction(memory, ::ceilFunction)), isConstant = true)
        objectValue.setProperty(memory, Clz32, memory.add(LxmFunction(memory, ::clz32Function)), isConstant = true)
        objectValue.setProperty(memory, Cos, memory.add(LxmFunction(memory, ::cosFunction)), isConstant = true)
        objectValue.setProperty(memory, Cosh, memory.add(LxmFunction(memory, ::coshFunction)), isConstant = true)
        objectValue.setProperty(memory, Exp, memory.add(LxmFunction(memory, ::expFunction)), isConstant = true)
        objectValue.setProperty(memory, Floor, memory.add(LxmFunction(memory, ::floorFunction)), isConstant = true)
        objectValue.setProperty(memory, Hypot, memory.add(LxmFunction(memory, ::hypotFunction)), isConstant = true)
        objectValue.setProperty(memory, Ln, memory.add(LxmFunction(memory, ::lnFunction)), isConstant = true)
        objectValue.setProperty(memory, Log10, memory.add(LxmFunction(memory, ::log10Function)), isConstant = true)
        objectValue.setProperty(memory, Log2, memory.add(LxmFunction(memory, ::log2Function)), isConstant = true)
        objectValue.setProperty(memory, Max, memory.add(LxmFunction(memory, ::maxFunction)), isConstant = true)
        objectValue.setProperty(memory, Min, memory.add(LxmFunction(memory, ::minFunction)), isConstant = true)
        objectValue.setProperty(memory, Pow, memory.add(LxmFunction(memory, ::powFunction)), isConstant = true)
        objectValue.setProperty(memory, Random, memory.add(LxmFunction(memory, ::randomFunction)), isConstant = true)
        objectValue.setProperty(memory, Round, memory.add(LxmFunction(memory, ::roundFunction)), isConstant = true)
        objectValue.setProperty(memory, Sign, memory.add(LxmFunction(memory, ::signFunction)), isConstant = true)
        objectValue.setProperty(memory, Sin, memory.add(LxmFunction(memory, ::sinFunction)), isConstant = true)
        objectValue.setProperty(memory, Sinh, memory.add(LxmFunction(memory, ::sinhFunction)), isConstant = true)
        objectValue.setProperty(memory, Sqrt, memory.add(LxmFunction(memory, ::sqrtFunction)), isConstant = true)
        objectValue.setProperty(memory, Tan, memory.add(LxmFunction(memory, ::tanFunction)), isConstant = true)
        objectValue.setProperty(memory, Tanh, memory.add(LxmFunction(memory, ::tanhFunction)), isConstant = true)
        objectValue.setProperty(memory, Trunc, memory.add(LxmFunction(memory, ::truncFunction)), isConstant = true)
    }

    /**
     * Gets the integer part of a number.
     */
    private fun absFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, AbsArgs)

        val number = parserArguments[AbsArgs[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(LxmInteger.from(kotlin.math.abs(number.primitive)))
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(kotlin.math.abs(number.primitive)))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Abs' method requires the parameter called '${AbsArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the acos function of the number.
     */
    private fun acosFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, AcosArgs)

        val number = parserArguments[AcosArgs[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(acos(number.primitive.toFloat())))
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(acos(number.primitive)))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Acos' method requires the parameter called '${AcosArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the acosh function of the number.
     */
    private fun acoshFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, AcoshArgs)

        val number = parserArguments[AcoshArgs[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(acosh(number.primitive.toFloat())))
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(acosh(number.primitive)))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Acosh' method requires the parameter called '${AcoshArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the asin function of the number.
     */
    private fun asinFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, AsinArgs)

        val number = parserArguments[AsinArgs[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(asin(number.primitive.toFloat())))
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(asin(number.primitive)))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Asin' method requires the parameter called '${AsinArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the asinh function of the number.
     */
    private fun asinhFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, AsinhArgs)

        val number = parserArguments[AsinhArgs[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(asinh(number.primitive.toFloat())))
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(asinh(number.primitive)))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Asinh' method requires the parameter called '${AsinhArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the atan function of the number.
     */
    private fun atanFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, AtanArgs)

        val number = parserArguments[AtanArgs[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(atan(number.primitive.toFloat())))
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(atan(number.primitive)))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Atan' method requires the parameter called '${AtanArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the atanh function of the number.
     */
    private fun atanhFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, AtanhArgs)

        val number = parserArguments[AtanhArgs[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(atanh(number.primitive.toFloat())))
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(atanh(number.primitive)))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Atanh' method requires the parameter called '${AtanhArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the atan2 function of the number.
     */
    private fun atan2Function(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, Atan2Args)

        val y = parserArguments[Atan2Args[0]] ?: LxmNil
        val x = parserArguments[Atan2Args[1]] ?: LxmNil

        val yNum = when (y) {
            is LxmInteger -> y.primitive.toFloat()
            is LxmFloat -> y.primitive
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Atan2' method requires the parameter called '${Atan2Args[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }
        val xNum = when (x) {
            is LxmInteger -> x.primitive.toFloat()
            is LxmFloat -> x.primitive
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Atan2' method requires the parameter called '${Atan2Args[1]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(atan2(yNum, xNum)))

        return true
    }

    /**
     * Returns the cbrt function of the number.
     */
    private fun cbrtFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, CbrtArgs)

        val number = parserArguments[CbrtArgs[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(number.primitive.toFloat().pow(1.0f / 3.0f)))
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(number.primitive.pow(1.0f / 3.0f)))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Cbrt' method requires the parameter called '${CbrtArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Rounds the number to the upper integer number.
     */
    private fun ceilFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, CeilArgs)

        val number = parserArguments[CeilArgs[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(number)
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmInteger.from(ceil(number.primitive).toInt()))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Ceil' method requires the parameter called '${CeilArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the clz32 function of the number.
     */
    private fun clz32Function(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, Clz32Args)

        val number = parserArguments[Clz32Args[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                if (number.primitive == 0) {
                    analyzer.memory.addToStackAsLast(LxmInteger.from(Int.SIZE_BITS))
                } else {
                    var num = number.primitive
                    var res = 0
                    while (num and (1 shl Int.SIZE_BITS - 1) == 0) {
                        num = num shl 1
                        res += 1
                    }
                    analyzer.memory.addToStackAsLast(LxmInteger.from(res))
                }
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Clz32' method requires the parameter called '${Clz32Args[0]}' be an ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the cos function of the number.
     */
    private fun cosFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, CosArgs)

        val number = parserArguments[CosArgs[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(cos(number.primitive.toFloat())))
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(cos(number.primitive)))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Cos' method requires the parameter called '${CosArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the cosh function of the number.
     */
    private fun coshFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, CoshArgs)

        val number = parserArguments[CoshArgs[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(cosh(number.primitive.toFloat())))
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(cosh(number.primitive)))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Cosh' method requires the parameter called '${CoshArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the exp function of the number.
     */
    private fun expFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, ExpArgs)

        val number = parserArguments[ExpArgs[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(exp(number.primitive.toFloat())))
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(exp(number.primitive)))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Exp' method requires the parameter called '${ExpArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Rounds the number to the lower integer number.
     */
    private fun floorFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, FloorArgs)

        val number = parserArguments[FloorArgs[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(number)
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmInteger.from(floor(number.primitive).toInt()))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Floor' method requires the parameter called '${FloorArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the hypot function of the number.
     */
    private fun hypotFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, emptyList(), spreadPositional)

        if (spreadPositional.isEmpty()) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Hypot' method requires at least one parameter.") {}
        }

        var acc = 0.0f
        for (number in spreadPositional) {
            when (number) {
                is LxmInteger -> {
                    acc += number.primitive * number.primitive
                }
                is LxmFloat -> {
                    acc += number.primitive * number.primitive
                }
                else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                        "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Hypot' method requires all its parameters be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
            }
        }

        analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(sqrt(acc)))

        return true
    }

    /**
     * Returns the ln function of the number.
     */
    private fun lnFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, LnArgs)

        val number = parserArguments[LnArgs[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(ln(number.primitive.toFloat())))
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(ln(number.primitive)))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Ln' method requires the parameter called '${LnArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the log10 function of the number.
     */
    private fun log10Function(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, Log10Args)

        val number = parserArguments[Log10Args[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(log10(number.primitive.toFloat())))
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(log10(number.primitive)))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Log10' method requires the parameter called '${Log10Args[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the log2 function of the number.
     */
    private fun log2Function(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, Log2Args)

        val number = parserArguments[Log2Args[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(log2(number.primitive.toFloat())))
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(log2(number.primitive)))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Log2' method requires the parameter called '${Log2Args[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the max function of all numbers.
     */
    private fun maxFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, emptyList(), spreadPositional)

        if (spreadPositional.isEmpty()) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Max' method requires at least one parameter.") {}
        }

        var result: LexemPrimitive? = null
        var maxValue = -Double.MIN_VALUE
        for (number in spreadPositional) {
            when (number) {
                is LxmInteger -> {
                    if (number.primitive > maxValue) {
                        result = number
                        maxValue = number.primitive.toDouble()
                    }
                }
                is LxmFloat -> {
                    if (number.primitive > maxValue) {
                        result = number
                        maxValue = number.primitive.toDouble()
                    }
                }
                else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                        "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Max' method requires all its parameters be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
            }
        }

        analyzer.memory.addToStackAsLast(result!!)

        return true
    }

    /**
     * Returns the min function of all numbers.
     */
    private fun minFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val spreadPositional = mutableListOf<LexemPrimitive>()
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, emptyList(), spreadPositional)

        if (spreadPositional.isEmpty()) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Min' method requires at least one parameter.") {}
        }

        var result: LexemPrimitive? = null
        var maxValue = Double.MAX_VALUE
        for (number in spreadPositional) {
            when (number) {
                is LxmInteger -> {
                    if (number.primitive < maxValue) {
                        result = number
                        maxValue = number.primitive.toDouble()
                    }
                }
                is LxmFloat -> {
                    if (number.primitive < maxValue) {
                        result = number
                        maxValue = number.primitive.toDouble()
                    }
                }
                else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                        "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Min' method requires all its parameters be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
            }
        }

        analyzer.memory.addToStackAsLast(result!!)

        return true
    }

    /**
     * Returns the pow function of the number.
     */
    private fun powFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, PowArgs)

        val base = parserArguments[PowArgs[0]] ?: LxmNil
        val exponent = parserArguments[PowArgs[1]] ?: LxmNil

        val baseNum = when (base) {
            is LxmInteger -> base.primitive.toFloat()
            is LxmFloat -> base.primitive
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Pow' method requires the parameter called '${PowArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }
        val exponentNum = when (exponent) {
            is LxmInteger -> exponent.primitive.toFloat()
            is LxmFloat -> exponent.primitive
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Pow' method requires the parameter called '${PowArgs[1]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(baseNum.pow(exponentNum)))

        return true
    }

    /**
     * Generates a random value.
     */
    private fun randomFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, emptyList())

        analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(kotlin.random.Random.nextFloat()))

        return true
    }

    /**
     * Rounds the number to the near integer number.
     */
    private fun roundFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, RoundArgs)

        val number = parserArguments[RoundArgs[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(number)
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmInteger.from(kotlin.math.round(number.primitive).toInt()))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Round' method requires the parameter called '${RoundArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the sign of the number.
     */
    private fun signFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, SignArgs)

        val number = parserArguments[SignArgs[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                val result = when {
                    number.primitive < 0 -> LxmInteger.Num_1
                    number.primitive > 0 -> LxmInteger.Num1
                    else -> LxmInteger.Num0
                }

                analyzer.memory.addToStackAsLast(result)
            }
            is LxmFloat -> {
                val result = when {
                    number.primitive < 0.0f -> LxmInteger.Num_1
                    number.primitive > 0.0f -> LxmInteger.Num1
                    else -> LxmInteger.Num0
                }

                analyzer.memory.addToStackAsLast(result)
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Sign' method requires the parameter called '${SignArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the sin function of the number.
     */
    private fun sinFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, SinArgs)

        val number = parserArguments[SinArgs[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(sin(number.primitive.toFloat())))
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(sin(number.primitive)))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Sin' method requires the parameter called '${SinArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the sinh function of the number.
     */
    private fun sinhFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, SinhArgs)

        val number = parserArguments[SinhArgs[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(sinh(number.primitive.toFloat())))
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(sinh(number.primitive)))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Sinh' method requires the parameter called '${SinhArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the sqrt function of the number.
     */
    private fun sqrtFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, SqrtArgs)

        val number = parserArguments[SqrtArgs[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(sqrt(number.primitive.toFloat())))
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(sqrt(number.primitive)))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Sqrt' method requires the parameter called '${SqrtArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the tan function of the number.
     */
    private fun tanFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, TanArgs)

        val number = parserArguments[TanArgs[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(tan(number.primitive.toFloat())))
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(tan(number.primitive)))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Tan' method requires the parameter called '${TanArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Returns the tanh function of the number.
     */
    private fun tanhFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, TanhArgs)

        val number = parserArguments[TanhArgs[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(tanh(number.primitive.toFloat())))
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmFloat.fromOrNil(tanh(number.primitive)))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Tanh' method requires the parameter called '${TanhArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }

    /**
     * Gets the integer part of a number.
     */
    private fun truncFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parserArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, TruncArgs)

        val number = parserArguments[TruncArgs[0]] ?: LxmNil

        when (number) {
            is LxmInteger -> {
                analyzer.memory.addToStackAsLast(number)
            }
            is LxmFloat -> {
                analyzer.memory.addToStackAsLast(LxmInteger.from(truncate(number.primitive).toInt()))
            }
            else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                    "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Trunc' method requires the parameter called '${TruncArgs[0]}' be a ${FloatType.TypeName} or ${IntegerType.TypeName}") {}
        }

        return true
    }
}
