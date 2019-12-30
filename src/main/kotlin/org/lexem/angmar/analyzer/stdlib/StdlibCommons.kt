package org.lexem.angmar.analyzer.stdlib

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.globals.*
import org.lexem.angmar.analyzer.stdlib.prototypes.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*

/**
 * The common functions of the Lexem's standard library.
 */
internal object StdlibCommons {
    /**
     * All the global names used by the Lexem's standard library.
     */
    val GlobalNames =
            listOf(AnyType.TypeName, NilType.TypeName, LogicType.TypeName, IntegerType.TypeName, FloatType.TypeName,
                    StringType.TypeName, IntervalType.TypeName, BitListType.TypeName, ListType.TypeName,
                    SetType.TypeName, ObjectType.TypeName, MapType.TypeName, FunctionType.TypeName,
                    ExpressionType.TypeName, FilterType.TypeName, NodeType.TypeName, AnalyzerGlobalObject.ObjectName,
                    MathGlobalObject.ObjectName, ImportGlobalFunction.FunctionName, GlobalsGlobalObject.ObjectName,
                    DebugGlobalObject.ObjectName)

    // METHODS ----------------------------------------------------------------

    /**
     * Initializes the built-in types.
     */
    fun initTypesAndPrototypes(memory: LexemMemory) {
        val anyPrototypeReference = AnyPrototype.initPrototype(memory)
        AnyType.initType(memory, anyPrototypeReference)
        NilType.initType(memory, NilPrototype.initPrototype(memory))
        LogicType.initType(memory, LogicPrototype.initPrototype(memory))
        IntegerType.initType(memory, IntegerPrototype.initPrototype(memory))
        FloatType.initType(memory, FloatPrototype.initPrototype(memory))
        StringType.initType(memory, StringPrototype.initPrototype(memory))
        IntervalType.initType(memory, IntervalPrototype.initPrototype(memory))
        BitListType.initType(memory, BitListPrototype.initPrototype(memory))
        ListType.initType(memory, ListPrototype.initPrototype(memory))
        SetType.initType(memory, SetPrototype.initPrototype(memory))
        ObjectType.initType(memory, ObjectPrototype.initPrototype(memory, anyPrototypeReference))
        MapType.initType(memory, MapPrototype.initPrototype(memory))
        FunctionType.initType(memory, FunctionPrototype.initPrototype(memory))
        ExpressionType.initType(memory, ExpressionPrototype.initPrototype(memory))
        FilterType.initType(memory, FilterPrototype.initPrototype(memory))
        NodeType.initType(memory, NodePrototype.initPrototype(memory))
    }

    /**
     * Initializes the built-in global objects.
     */
    fun initGlobals(memory: LexemMemory) {
        AnalyzerGlobalObject.initObject(memory)
        MathGlobalObject.initObject(memory)
        ImportGlobalFunction.initFunction(memory)
        GlobalsGlobalObject.initObject(memory)
        DebugGlobalObject.initObject(memory)
    }

    /**
     * Calls a method with a list of positional arguments.
     */
    fun callMethod(analyzer: LexemAnalyzer, function: LxmFunction, positionalArguments: List<LexemPrimitive>,
            returnSignal: Int, functionName: String) {
        val arguments = LxmArguments(analyzer.memory)
        arguments.addNamedArgument(AnalyzerCommons.Identifiers.This, LxmNil)
        positionalArguments.forEach { arguments.addPositionalArgument(it) }

        AnalyzerNodesCommons.callFunction(analyzer, function, arguments, InternalFunctionCallCompiled,
                LxmCodePoint(InternalFunctionCallCompiled, returnSignal, callerNode = function.node,
                        callerContextName = "<Native function '$functionName'>"))
    }

    /**
     * Calls the toString method of a value.
     */
    fun callToString(analyzer: LexemAnalyzer, value: LexemMemoryValue, returnNode: CompiledNode, returnSignal: Int,
            functionName: String) {
        val prototype = value.dereference(analyzer.memory, toWrite = false)
                .getPrototypeAsObject(analyzer.memory, toWrite = false)
        val function = prototype.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.ToString)!!.dereference(
                analyzer.memory, toWrite = false) as LxmFunction

        val arguments = LxmArguments(analyzer.memory)
        arguments.addNamedArgument(AnalyzerCommons.Identifiers.This, value)

        AnalyzerNodesCommons.callFunction(analyzer, function, arguments, InternalFunctionCallCompiled,
                LxmCodePoint(returnNode, returnSignal, callerNode = function.node,
                        callerContextName = "<Native function '$functionName'>"))
    }
}
