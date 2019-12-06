package org.lexem.angmar.analyzer

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.*
import org.lexem.angmar.io.readers.*
import java.io.*
import java.net.*

/**
 * Generic commons for the analyzer section.
 */
internal object AnalyzerCommons {
    object Identifiers {
        private const val HiddenPrefix = "\n"

        // Generic
        const val Prototype = "prototype"
        const val Key = "key"
        const val Keys = "keys"
        const val Index = "idx"
        const val Value = "value"
        const val EntryPoint = "entryPoint"
        const val Exports = "exports"
        const val List = "list"

        // Functions
        const val This = "this"
        const val Arguments = "arguments"
        const val ArgumentsPositional = "positional"
        const val ArgumentsNamed = "named"
        const val ArgumentsProperties = "properties"
        const val ToString = "toString"

        // Nodes
        const val Root = "root"
        const val Parent = "parent"
        const val Children = "children"
        const val Node = "node"
        const val Properties = "properties"
        const val Name = "name"
        const val From = "from"
        const val HiddenFrom = "${HiddenPrefix}from"
        const val To = "to"
        const val HiddenTo = "${HiddenPrefix}to"

        // Selector
        const val DefaultPropertyName = "it"
        const val Property = "property"

        // Stack
        const val Accumulator = "acc"
        const val Last = "last"
        const val Left = "left"
        const val Destructuring = "destructuring"
        const val Auxiliary = "aux"

        // Function calls
        const val Function = "fn"
        const val Parameters = "params"
        const val ReturnCodePoint = "retCodePoint"
        const val Control = "ctrl"
        const val HiddenFunction = "${HiddenPrefix}fn"

        // Expression calls
        const val LastNode = "lastNode"

        // Expressions
        const val HiddenPatternUnions = "${HiddenPrefix}patternUnions"

        // Filters
        const val FilterNode = "filterNode"
        const val FilterNodePosition = "filterNodePos"
        const val SavedFilterNodePosition = "savedFilterNodePos"
        const val Node2FilterParameter = "node2Filter"

        // Contexts
        const val HiddenCurrentContext = "${HiddenPrefix}ctx"
        const val HiddenCallerContext = "${HiddenPrefix}callerCtx"
        const val HiddenContextTag = "${HiddenPrefix}ctxTag"
        const val HiddenLastResultNode = "${HiddenPrefix}lastResNode"
        const val HiddenFilePath = "${HiddenPrefix}filePath"
        const val HiddenFileMap = "${HiddenPrefix}fileMap"
        const val HiddenAnalyzerText = "${HiddenPrefix}analyzerText"
        const val HiddenNode2Filter = "${HiddenPrefix}node2Filter"
        const val HiddenRollbackCodePoint = "${HiddenPrefix}rollbackCodePoint"

        // Loops
        const val LoopIndexName = "loopIdxName"
        const val LoopIndexValue = "loopIdxValue"
        const val LoopVariable = "loopVariable"
        const val LoopIterator = "loopIterator"
        const val LoopUnion = "loopUnion"

        // Selective
        const val SelectiveCondition = "selCondition"

        // Lexemes
        const val AtomicFirstIndex = "atomicFirstIndex"
        const val LexemeDataCapturingName = "lxmDataCapName"
        const val LexemeDataCapturingList = "lxmDataCapList"
        const val LexemeUnion = "lxmUnion"
        const val AnchorIsStart = "anchorIsStart"
        const val SelectorMethodIterator = "methodIterator"
    }

    object Properties {
        const val Capture = "capture"
        const val Children = "children"
        const val Property = "property"
        const val Insensible = "insensible"
        const val Backtrack = "backtrack"
        const val Reverse = "reverse"
        const val Consume = "consume"
    }

    object SelectorAtIdentifiers {
        const val Name = "name"
        const val Content = "content"
        const val Start = "start"
        const val End = "end"
    }

    object SelectorMethods {
        const val Root = "root"
        const val Empty = "empty"
        const val ChildCount = "child-count"
        const val FirstChild = "first-child"
        const val LastChild = "last-child"
        const val NthChild = "nth-child"
        const val Parent = "parent"
        const val AllChildren = "all-children"
        const val AnyChild = "any-child"
        const val Node = "node"

        val all = setOf(Root, Empty, ChildCount, FirstChild, LastChild, NthChild, Parent, AllChildren, AnyChild, Node)
        val allSimple = setOf(Root, Empty, FirstChild, LastChild)
        val allWithSelectors = setOf(Parent, AllChildren, AnyChild, Node)
        val AllWithCondition = setOf(ChildCount, NthChild, Parent, AllChildren, AnyChild, Node)
    }

    object Operators {
        const val RightParameterName = "right"
        val ParameterList = listOf(RightParameterName)

        const val LogicalNot = "not"
        const val ArithmeticNegation = "negate"
        const val ArithmeticAffirmation = "affirm"
        const val BitwiseNegation = "bitwiseNegation"

        const val Multiplication = "multiply"
        const val Division = "divide"
        const val IntegerDivision = "intDivide"
        const val Reminder = "reminder"
        const val Add = "add"
        const val Sub = "sub"
        const val LogicalAnd = "logicalAnd"
        const val LogicalOr = "logicalOr"
        const val LogicalXor = "logicalXor"
        const val LeftShift = "leftShift"
        const val RightShift = "rightShift"
        const val LeftRotate = "leftRotate"
        const val RightRotate = "rightRotate"
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the standard library context from the memory.
     */
    fun getStdLibContext(memory: LexemMemory) = memory.get(LxmReference.StdLibContext) as LxmContext

    /**
     * Gets the current context reference.
     */
    fun getCurrentContextReference(memory: LexemMemory): LxmReference =
            getStdLibContext(memory).getPropertyValue(memory, Identifiers.HiddenCurrentContext) as LxmReference

    /**
     * Gets the current context from the memory.
     */
    fun getCurrentContext(memory: LexemMemory) =
            getStdLibContext(memory).getDereferencedProperty<LxmContext>(memory, Identifiers.HiddenCurrentContext)!!

    /**
     * Gets the actual call hierarchy.
     */
    fun getCallHierarchy(memory: LexemMemory): List<LxmCodePoint> {
        val list = mutableListOf<LxmCodePoint>()

        try {
            while (true) {
                val lastCodePoint = memory.getFromStack(Identifiers.ReturnCodePoint) as LxmCodePoint
                list.add(lastCodePoint)
            }
        } catch (e: AngmarAnalyzerException) {
        }

        list.reverse()

        return list
    }


    /**
     * Gets an element from the current context.
     */
    inline fun <reified T : LexemMemoryValue> getCurrentContextElement(memory: LexemMemory, identifier: String) =
            getCurrentContext(memory).getDereferencedProperty<T>(memory, identifier)!!

    /**
     * Creates a new context and assigns it as the current context.
     */
    fun createAndAssignNewContext(memory: LexemMemory) {
        val stdLibContext = getStdLibContext(memory)
        val lastContextReference = getCurrentContextReference(memory)
        val newContext = LxmContext(lastContextReference, memory)
        val newContextReference = memory.add(newContext)

        stdLibContext.setProperty(memory, Identifiers.HiddenCurrentContext, newContextReference)
    }

    /**
     * Removes the current context and assigns the previous as the current context.
     */
    fun removeCurrentContextAndAssignPrevious(memory: LexemMemory) {
        val stdLibContext = getStdLibContext(memory)
        val lastContext = getCurrentContext(memory)

        stdLibContext.setProperty(memory, Identifiers.HiddenCurrentContext, lastContext.prototypeReference!!)
    }

    /**
     * Creates a new function context and assigns it as the current context.
     */
    fun createAndAssignNewFunctionContext(memory: LexemMemory, higherContext: LxmReference) {
        val stdLibContext = getStdLibContext(memory)
        val lastContextReference = getCurrentContextReference(memory)
        val newContext = LxmContext(higherContext, memory)
        val newContextReference = memory.add(newContext)

        newContext.setProperty(memory, Identifiers.HiddenCallerContext, lastContextReference, isConstant = true)
        stdLibContext.setProperty(memory, Identifiers.HiddenCurrentContext, newContextReference)
    }

    /**
     * Removes the current function context and assigns the previous as the current context.
     */
    fun removeCurrentFunctionContextAndAssignPrevious(memory: LexemMemory) {
        val stdLibContext = getStdLibContext(memory)
        val lastContext = getCurrentContext(memory)
        val lastContextReference =
                stdLibContext.getPropertyValue(memory, Identifiers.HiddenCurrentContext) as LxmReference
        val callerContextReference =
                lastContext.getPropertyValue(memory, Identifiers.HiddenCallerContext) as LxmReference

        // Done like this to avoid premature removal.
        lastContextReference.increaseReferences(memory)
        stdLibContext.setProperty(memory, Identifiers.HiddenCurrentContext, callerContextReference)
        lastContext.removePropertyIgnoringConstants(memory, Identifiers.HiddenCallerContext)

        lastContextReference.decreaseReferences(memory)
    }

    /**
     * Names the previous context if it has no name so statements can know the name of its blocks.
     */
    fun namePreviousContextIfItHasNoName(memory: LexemMemory, name: LxmString) {
        val currentContext = getCurrentContext(memory)
        val previousContext = memory.get(currentContext.prototypeReference ?: return) as LxmContext

        val previousName = previousContext.getOwnPropertyDescriptor(memory, Identifiers.HiddenContextTag)
        if (previousName == null) {
            previousContext.setProperty(memory, Identifiers.HiddenContextTag, name)
        }
    }

    /**
     * Creates a new module context and assigns it as the current context.
     */
    fun createAndAssignNewModuleContext(memory: LexemMemory, source: String) {
        val stdLibContext = getStdLibContext(memory)
        val lastContextReference = getCurrentContextReference(memory)

        val newContext = LxmContext(LxmReference.StdLibContext, memory)
        val newContextReference = memory.add(newContext)
        val fileMap = newContext.getDereferencedProperty<LxmObject>(memory, Identifiers.HiddenFileMap)!!
        val exports = LxmObject()
        val exportsRef = memory.add(exports)
        newContext.setProperty(memory, Identifiers.HiddenCallerContext, lastContextReference)
        newContext.setProperty(memory, Identifiers.Exports, exportsRef, isConstant = true)
        newContext.setProperty(memory, Identifiers.HiddenFilePath, LxmString.from(source), isConstant = true)
        fileMap.setProperty(memory, source, exportsRef)

        stdLibContext.setProperty(memory, Identifiers.HiddenCurrentContext, newContextReference)
    }

    /**
     * Removes the current module context and assigns the previous as the current context.
     */
    fun removeModuleContextAndAssignPrevious(memory: LexemMemory) {
        val stdLibContext = getStdLibContext(memory)
        val currentContext = getCurrentContext(memory)
        val currentContextReference = getCurrentContextReference(memory)
        val lastContextRef = currentContext.getPropertyValue(memory, Identifiers.HiddenCallerContext) as LxmReference

        // Done like this to avoid premature removal.
        currentContextReference.increaseReferences(memory)

        stdLibContext.setProperty(memory, Identifiers.HiddenCurrentContext, lastContextRef)

        // Remove the property to unlink the previous context.
        currentContext.removePropertyIgnoringConstants(memory, Identifiers.HiddenCallerContext)

        currentContextReference.decreaseReferences(memory)
    }

    /**
     * Gets an element from the standard library context.
     */
    inline fun <reified T : LexemMemoryValue> getStdLibContextElement(memory: LexemMemory, identifier: String) =
            getStdLibContext(memory).getDereferencedProperty<T>(memory, identifier)!!

    /**
     * Returns the current context tag.
     */
    fun getCurrentContextTag(memory: LexemMemory): String? {
        val context = getCurrentContext(memory)
        val tag = context.getDereferencedProperty<LxmString>(memory, Identifiers.HiddenContextTag)

        return tag?.primitive
    }

    /**
     * Returns the property object of the current node.
     */
    fun getCurrentNodeProps(memory: LexemMemory): LxmObject {
        val context = getCurrentContext(memory)
        val node = context.getDereferencedProperty<LxmNode>(memory, Identifiers.Node)!!

        return node.getProperties(memory)
    }

    /**
     * Resolves the relative uri using main as root.
     */
    fun resolveRelativeUriToReader(analyzer: LexemAnalyzer, main: String, relative: String): ITextReader {
        // Root paths.
        if (relative.startsWith("root:")) {
            val relativeWithoutPrefix = relative.removePrefix("root:")
            val file = File(analyzer.rootFilePath).resolveSibling(relativeWithoutPrefix).canonicalFile
            try {
                return IOStringReader.from(file)
            } catch (e: FileNotFoundException) {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.FileNotExist,
                        "The resolved file from '$relative' at '${main}' does not exist. Actual: '${file.canonicalPath}'") {}
            }
        }

        // Relative and absolute paths.
        val uri: URI
        try {

            val mainUri = URI(main)
            val relativeUri = URI(relative)
            uri = mainUri.resolve(relativeUri).normalize()
        } catch (e: Throwable) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.FileNotExist,
                    "The resolved url from '$relative' at '$main' is malformed.") {}
        }

        // Remote paths.
        if (uri.scheme in listOf("http", "https")) {
            try {
                val text = uri.toURL().readText()

                return IOStringReader(uri.toString(), text)
            } catch (e: Throwable) {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.FileNotExist,
                        "The resolved url from '$relative' at '$main' does not exist. Actual: '$uri'") {}
            }
        } else {
            // Local paths.
            try {
                return IOStringReader.from(File(uri.toString()))
            } catch (e: FileNotFoundException) {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.FileNotExist,
                        "The file '$uri' does not exist") {}
            }
        }
    }

    /**
     * Creates a new reader from the specified primitive.
     */
    fun createReaderFrom(primitive: LexemPrimitive) = when (primitive) {
        is LxmString -> {
            IOStringReader.from(primitive.primitive)
        }
        is LxmBitList -> {
            IOBinaryReader("", primitive.primitive, primitive.size)
        }
        else -> {
            throw AngmarException("Angmar only accept textual or binary readers.")
        }
    }

    /**
     * Gets a subsequence of a reader.
     */
    fun substringReader(reader: IReader, from: IReaderCursor, to: IReaderCursor) = when (reader) {
        is ITextReader -> {
            LxmString.from(reader.substring(from as ITextReaderCursor, to as ITextReaderCursor))
        }
        is IBinaryReader -> {
            val size = to.position() - from.position()
            LxmBitList(size, reader.subSet(from as IBinaryReaderCursor, to as IBinaryReaderCursor))
        }
        else -> {
            throw AngmarException("Angmar only accept textual or binary readers.")
        }
    }
}
