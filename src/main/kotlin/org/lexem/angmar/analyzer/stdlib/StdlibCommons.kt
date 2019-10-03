package org.lexem.angmar.analyzer.stdlib

import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.globals.*
import org.lexem.angmar.analyzer.stdlib.prototypes.*
import org.lexem.angmar.analyzer.stdlib.types.*

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
                    MathGlobalObject.ObjectName, ImportGlobalFunction.FunctionName, GlobalsGlobalObject.ObjectName)

    // METHODS ----------------------------------------------------------------

    /**
     * Initializes the built-in types.
     */
    fun initTypesAndPrototypes(memory: LexemMemory) {
        AnyType.initType(memory, AnyPrototype.initPrototype(memory))
        NilType.initType(memory, NilPrototype.initPrototype(memory))
        LogicType.initType(memory, LogicPrototype.initPrototype(memory))
        IntegerType.initType(memory, IntegerPrototype.initPrototype(memory))
        FloatType.initType(memory, FloatPrototype.initPrototype(memory))
        StringType.initType(memory, StringPrototype.initPrototype(memory))
        IntervalType.initType(memory, IntervalPrototype.initPrototype(memory))
        BitListType.initType(memory, BitListPrototype.initPrototype(memory))
        ListType.initType(memory, ListPrototype.initPrototype(memory))
        SetType.initType(memory, SetPrototype.initPrototype(memory))
        ObjectType.initType(memory, ObjectPrototype.initPrototype(memory))
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
    }
}
