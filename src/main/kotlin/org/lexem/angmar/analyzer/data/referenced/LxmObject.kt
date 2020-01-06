package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

/**
 * The Lexem value of the Object type.
 */
internal open class LxmObject : LexemReferenced {
    private var properties = hashMapOf<String, LxmObjectProperty>()
    private var isPropertiesCloned = true

    var prototypeReference: LxmReference?
    var isConstant = false
        private set
    var isWritable = true
        private set
    val size get() = properties.size

    private val isFinalInHierarchy get() = this is LxmAnyPrototype || (this is LxmContext && prototypeReference == null)

    // CONSTRUCTORS -----------------------------------------------------------

    /**
     * Only used to clone the object.
     */
    protected constructor(memory: IMemory, oldVersion: LxmObject) : super(memory, oldVersion) {
        prototypeReference = oldVersion.prototypeReference
        isConstant = oldVersion.isConstant
        isWritable = oldVersion.isWritable
        properties = oldVersion.properties
        isPropertiesCloned = false
    }

    /**
     * Creates an object without explicit prototype.
     */
    constructor(memory: IMemory) : super(memory) {
        prototypeReference = null
    }

    /**
     * Creates an object with explicit prototype.
     */
    constructor(memory: IMemory, prototype: LxmObject, dummy: Boolean = false) : super(memory) {
        prototypeReference = prototype.getPrimitive()
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the value of a property. It searches also in prototypes.
     */
    open fun getPropertyValue(memory: IMemory, identifier: String) = getPropertyDescriptor(memory, identifier)?.value

    /**
     * Gets the property descriptor of the specified property.
     */
    fun getPropertyDescriptor(memory: IMemory, identifier: String): LxmObjectProperty? {
        val property = properties[identifier]

        if (property == null) {
            // Avoid infinite loops.
            if (isFinalInHierarchy) {
                return null
            }

            val prototype = getPrototypeAsObject(memory, toWrite = false)
            return prototype.getPropertyDescriptor(memory, identifier)
        }

        return property
    }

    /**
     * Gets the final value of a property. It searches also in prototypes.
     */
    inline fun <reified T : LexemMemoryValue> getDereferencedProperty(memory: IMemory, identifier: String,
            toWrite: Boolean) = getPropertyValue(memory, identifier)?.dereference(memory, toWrite) as? T

    /**
     * Sets a new value to the property or creates a new property with the specified value.p
     */
    fun setProperty(memory: IMemory, identifier: String, value: LexemMemoryValue, isConstant: Boolean = false,
            ignoreConstant: Boolean = false) {
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The object is immutable therefore cannot be modified") {}
        }

        if (!isWritable || (!ignoreConstant && this.isConstant)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObject,
                    "The object is constant therefore cannot be modified") {}
        }

        cloneProperties()

        val valuePrimitive = value.getPrimitive()
        var property = properties[identifier]

        if (property == null) {
            property = LxmObjectProperty(this, isConstant,
                    !identifier.startsWith(AnalyzerCommons.Identifiers.HiddenPrefix))
            properties[identifier] = property
            property.replaceValue(memory, valuePrimitive)
        } else {
            if (!ignoreConstant && property.isConstant) {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObjectProperty,
                        "The object property called '$identifier' is constant therefore it cannot be modified") {}
            }

            if (property.belongsTo != this) {
                property = property.clone(this)
                properties[identifier] = property
            }

            property.replaceValue(memory, valuePrimitive)
            property.isConstant = isConstant
        }
    }

    /**
     * Sets a new value to the property in any of the prototypes that have that property.
     */
    fun setPropertyAsContext(memory: IMemory, identifier: String, value: LexemMemoryValue,
            isConstant: Boolean = false) {
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The object is immutable therefore cannot be modified") {}
        }

        if (!isWritable || this.isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObject,
                    "The object is constant therefore cannot be modified") {}
        }

        // Avoid infinite loops.
        if (isFinalInHierarchy) {
            return setProperty(memory, identifier, value, isConstant)
        }

        // Check the current object and the prototype.
        val currentProperty = properties[identifier]

        if (currentProperty == null) {
            var prototype = getPrototypeAsObject(memory, toWrite = false)
            while (true) {
                val prototypeProperty = prototype.properties[identifier]

                // Set in prototype.
                if (prototypeProperty != null) {
                    prototype = prototype.getPrimitive().dereferenceAs(memory, toWrite = true)!!
                    return prototype.setProperty(memory, identifier, value, isConstant)
                }

                // Avoid infinite loops.
                if (prototype.isFinalInHierarchy) {
                    break
                }

                prototype = prototype.getPrototypeAsObject(memory, toWrite = false)
            }
        }


        // Set in current.
        return setProperty(memory, identifier, value, isConstant)
    }

    /**
     * Returns whether the object contains a property or not.
     */
    fun containsOwnProperty(identifier: String) = properties[identifier] != null

    /**
     * Removes a property.
     */
    fun removeProperty(memory: IMemory, identifier: String, ignoreConstant: Boolean = false) {
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The object is immutable therefore cannot be modified") {}
        }

        if (!isWritable || (!ignoreConstant && isConstant)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObject,
                    "The object is constant therefore it cannot be modified") {}
        }

        val property = properties[identifier] ?: return

        if (!ignoreConstant && property.isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObjectProperty,
                    "The object property called '$identifier' is constant therefore it cannot be modified") {}
        }

        cloneProperties()

        // Remove property.
        properties.remove(identifier)
    }

    /**
     * Makes the object constant.
     */
    fun makeConstant(memory: IMemory) {
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The object is immutable therefore cannot be modified") {}
        }

        if (!isWritable || isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantSet,
                    "The set is constant therefore cannot be modified") {}
        }

        isConstant = true
    }

    /**
     * Makes the list constant and not writable.
     */
    fun makeConstantAndNotWritable(memory: IMemory) {
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The object is immutable therefore cannot be modified") {}
        }

        if (!isWritable || isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantSet,
                    "The set is constant therefore cannot be modified") {}
        }

        isConstant = true
        isWritable = false
    }

    /**
     * Makes a property constant.
     */
    fun makePropertyConstant(memory: IMemory, identifier: String) {
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The object is immutable therefore cannot be modified") {}
        }

        if (!isWritable || isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObject,
                    "The object is constant therefore it cannot be modified") {}
        }

        var property = properties[identifier] ?: throw AngmarAnalyzerException(
                AngmarAnalyzerExceptionType.UndefinedObjectProperty,
                "The object hasn't a property called '$identifier'") {}

        cloneProperties()

        if (property.belongsTo != this) {
            property = property.clone(this)
            properties[identifier] = property
        }

        property.isConstant = true
    }

    /**
     * Gets all properties of the object.
     */
    fun getAllProperties() = properties.asSequence()

    /**
     * Gets all iterable properties of the object.
     */
    fun getAllIterableProperties() = getAllProperties().filter { it.value.isIterable }

    /**
     * Clones the properties map.
     */
    private fun cloneProperties() {
        if (!isPropertiesCloned) {
            properties = properties.toHashMap()
            isPropertiesCloned = true
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun memoryClone(memory: IMemory) = if (!isWritable) {
        this
    } else {
        LxmObject(memory, this)
    }

    override fun spatialGarbageCollect(memory: IMemory, gcFifo: GarbageCollectorFifo) {
        getAllProperties().map { it.value.value }.forEach { it.spatialGarbageCollect(memory, gcFifo) }

        prototypeReference?.spatialGarbageCollect(memory, gcFifo)
    }

    override fun getType(memory: IMemory): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(memory, toWrite = false)
        return context.getPropertyValue(memory, ObjectType.TypeName) as LxmReference
    }

    override fun getPrototype(memory: IMemory) = prototypeReference ?: super.getPrototype(memory)

    override fun toLexemString(memory: IMemory) = LxmString.ObjectToString

    override fun toString() = StringBuilder().apply {
        if (isConstant) {
            append(ObjectNode.constantToken)
        }

        append(ObjectNode.startToken)

        val text = properties.asSequence().take(4).joinToString("${ObjectNode.elementSeparator} ") {
            val const = if (it.value.isConstant && !isConstant) {
                ObjectElementNode.constantToken
            } else {
                ""
            }

            "$const${it.key}${ObjectElementNode.keyValueSeparator} ${it.value.value}"
        }

        append(text)
        append(ObjectNode.endToken)

        if (size > 4) {
            append(" and ${size - 4} more")
        }
    }.toString()

    // STATIC -----------------------------------------------------------------

    /**
     * A property of [LxmObject]s.
     */
    class LxmObjectProperty(val belongsTo: LxmObject, var isConstant: Boolean, var isIterable: Boolean) {
        var value: LexemPrimitive = LxmNil
            private set

        // METHODS ------------------------------------------------------------

        /**
         * Clones the object property only if it is not constant.
         */
        fun clone(belongsTo: LxmObject): LxmObjectProperty {
            val prop = LxmObjectProperty(belongsTo, isConstant, isIterable)

            prop.value = value
            return prop
        }

        /**
         * Replaces the value handling the memory references.
         */
        fun replaceValue(memory: IMemory, newValue: LexemPrimitive) {
            if (belongsTo.bigNodeId != memory.getBigNodeId()) {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                        "The object property is immutable therefore cannot be modified") {}
            }

            value = newValue
        }

        override fun toString() = StringBuilder().apply {
            if (isConstant) {
                append('#')
            }

            if (isIterable) {
                append("[]")
            }

            append(" ")

            append(value)
        }.toString()
    }
}
