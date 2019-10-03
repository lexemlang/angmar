package org.lexem.angmar.analyzer

import org.lexem.angmar.*
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
        const val Prototype = "prototype"
        const val Key = "key"
        const val Value = "value"
        const val EntryPoint = "entryPoint"
        const val Exports = "exports"

        // Functions
        const val This = "this"
        const val Arguments = "arguments"
        const val ArgumentsPositional = "positional"
        const val ArgumentsNamed = "named"
        const val ArgumentsProperties = "properties"
        const val ToString = "toString"

        // Nodes
        const val Root = "root"
        const val Children = "children"

        // Hidden properties
        private const val HiddenPrefix = "\n"
        const val HiddenCurrentContext = "${HiddenPrefix}ctx"
        const val HiddenCallerContext = "${HiddenPrefix}callerCtx"
        const val HiddenInternalFunction = "${HiddenPrefix}fn"
        const val HiddenLastCodePoint = "${HiddenPrefix}lcp"
        const val HiddenLastModuleCodePoint = "${HiddenPrefix}lmcp"
        const val HiddenContextTag = "${HiddenPrefix}ctxTag"
        const val HiddenFilePath = "${HiddenPrefix}filePath"
        const val HiddenFileMap = "${HiddenPrefix}fileMap"
        const val HiddenLoopIndexName = "${HiddenPrefix}loopIdxName"
        const val HiddenLoopIndexValue = "${HiddenPrefix}loopIdxValue"
        const val HiddenLoopVariable = "${HiddenPrefix}loopIdxVariable"
        const val HiddenLoopIterator = "${HiddenPrefix}loopIdxIterator"
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

    object ContextTypes {
        const val Functional = "fun"
        const val Expression = "exp"
        const val Filter = "fil"
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
     * Gets the actual hierarchy of the context.
     */
    fun getCurrentContextHierarchy(memory: LexemMemory): List<LxmContext> {
        val list = mutableListOf<LxmContext>()
        var current: LxmContext? = getCurrentContext(memory)

        while (current != null) {
            list.add(current)

            current = current.getPrototype(memory) as? LxmContext
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
        lastContextReference.increaseReferenceCount(memory)
        stdLibContext.setProperty(memory, Identifiers.HiddenCurrentContext, callerContextReference)
        lastContext.removePropertyIgnoringConstants(memory, Identifiers.HiddenCallerContext)

        lastContextReference.decreaseReferenceCount(memory)
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
        currentContextReference.increaseReferenceCount(memory)

        stdLibContext.setProperty(memory, Identifiers.HiddenCurrentContext, lastContextRef)

        // Remove the property to unlink the previous context.
        currentContext.removePropertyIgnoringConstants(memory, Identifiers.HiddenCallerContext)

        currentContextReference.decreaseReferenceCount(memory)
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
     * Resolves the relative uri using main as root.
     */
    fun resolveRelativeUriToReader(analyzer: LexemAnalyzer, main: String, relative: String): ITextReader {
        // Root paths.
        if (relative.startsWith("root:")) {
            val relativeWithoutPrefix = relative.removePrefix("root:")
            val file = File(analyzer.rootFilePath).resolveSibling(relativeWithoutPrefix).canonicalFile
            try {
                return CustomStringReader.from(file)
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

                return CustomStringReader(uri.toString(), text)
            } catch (e: Throwable) {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.FileNotExist,
                        "The resolved url from '$relative' at '$main' does not exist. Actual: '$uri'") {}
            }
        } else {
            // Local paths.
            try {
                return CustomStringReader.from(File(uri.toString()))
            } catch (e: FileNotFoundException) {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.FileNotExist,
                        "The file '$uri' does not exist") {}
            }
        }
    }
}
