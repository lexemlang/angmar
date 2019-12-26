package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.literals.*

/**
 * The Lexem value of the Object type.
 */
internal open class LxmObject : LexemReferenced {
    private var properties = hashMapOf<String, LxmObjectProperty>()
    private var isPropertiesCloned = false

    var prototypeReference: LxmReference?
    var isConstant = false
        private set
    var isWritable = true
        private set
    val size get() = properties.size

    private val isFinalInHierarchy get() = this is LxmAnyPrototype || (this is LxmContext && prototypeReference == null)

    // CONSTRUCTORS -----------------------------------------------------------

    protected constructor(memory: LexemMemory, oldVersion: LxmObject, dummy: Boolean = true) : super(memory,
            oldVersion) {
        prototypeReference = oldVersion.prototypeReference
        isConstant = oldVersion.isConstant
        isWritable = oldVersion.isWritable
        properties = oldVersion.properties
    }

    /**
     * Creates an object without explicit prototype.
     */
    constructor(memory: LexemMemory) : super(memory) {
        prototypeReference = null
        isPropertiesCloned = true
    }

    /**
     * Creates an object with explicit prototype.
     */
    constructor(memory: LexemMemory, prototype: LxmObject) : super(memory) {
        prototypeReference = prototype.getPrimitive()
        isPropertiesCloned = true
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the value of a property. It searches also in prototypes.
     */
    open fun getPropertyValue(memory: LexemMemory, identifier: String): LexemPrimitive? {
        val property = properties[identifier]

        if (property == null) {
            // Avoid infinite loops.
            if (isFinalInHierarchy) {
                return null
            }

            val prototype = getPrototypeAsObject(memory, toWrite = false)
            return prototype.getPropertyValue(memory, identifier)
        }

        return property.value
    }

    /**
     * Gets the property descriptor of the specified property.
     */
    fun getPropertyDescriptor(memory: LexemMemory, identifier: String): LxmObjectProperty? {
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
    inline fun <reified T : LexemMemoryValue> getDereferencedProperty(memory: LexemMemory, identifier: String,
            toWrite: Boolean): T? {
        val property = getPropertyValue(memory, identifier) ?: return null
        return property.dereference(memory, toWrite) as? T
    }

    /**
     * Sets a new value to the property or creates a new property with the specified value.p
     */
    fun setProperty(memory: LexemMemory, identifier: String, value: LexemMemoryValue, isConstant: Boolean = false,
            isIterable: Boolean = true, ignoreConstant: Boolean = false) {
        // Prevent modifications if the object is constant.
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The object is immutable therefore cannot be modified") {}
        }

        if (!ignoreConstant && this.isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObject,
                    "The object is constant therefore cannot be modified") {}
        }

        if (!isWritable) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyANonWritableObject,
                    "The object is non writable therefore cannot be modified") {}
        }

        cloneProperties()

        val valuePrimitive = value.getPrimitive()
        var property = properties[identifier]

        if (property == null) {
            property = LxmObjectProperty(this, isConstant,
                    isIterable || !identifier.startsWith(AnalyzerCommons.Identifiers.HiddenPrefix))
            properties[identifier] = property
            property.replaceValue(valuePrimitive)
        } else {
            if (!ignoreConstant && property.isConstant) {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObjectProperty,
                        "The object property called '$identifier' is constant therefore it cannot be modified") {}
            }

            if (property.belongsTo != this) {
                property = property.clone(this)
            }

            property.replaceValue(valuePrimitive)
            property.isConstant = isConstant
            property.isIterable = isIterable || !identifier.startsWith(AnalyzerCommons.Identifiers.HiddenPrefix)
        }
    }

    /**
     * Sets a new value to the property in any of the prototypes that have that property.
     */
    fun setPropertyAsContext(memory: LexemMemory, identifier: String, value: LexemMemoryValue,
            isConstant: Boolean = false) {
        // Prevent modifications if the object is constant.
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The object is immutable therefore cannot be modified") {}
        }

        if (this.isConstant) {
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
    fun containsOwnProperty(memory: LexemMemory, identifier: String) = properties[identifier] != null

    /**
     * Removes a property.
     */
    fun removeProperty(memory: LexemMemory, identifier: String, ignoreConstant: Boolean = false) {
        // Prevent modifications if the object is constant.
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The object is immutable therefore cannot be modified") {}
        }

        if (!ignoreConstant && isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObject,
                    "The object is constant therefore it cannot be modified") {}
        }

        val property = properties[identifier]
        if (property != null) {
            if (!ignoreConstant && property.isConstant) {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObjectProperty,
                        "The object property called '$identifier' is constant therefore it cannot be modified") {}
            }

            cloneProperties()

            // Remove property.
            properties.remove(identifier)
        }
    }

    /**
     * Makes the object constant.
     */
    fun makeConstant(memory: LexemMemory) {
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The object is immutable therefore cannot be modified") {}
        }

        isConstant = true
    }

    /**
     * Makes the list constant and not writable.
     */
    fun makeConstantAndNotWritable(memory: LexemMemory) {
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The list is immutable therefore cannot be modified") {}
        }

        isConstant = true
        isWritable = false
    }

    /**
     * Makes a property constant.
     */
    fun makePropertyConstant(memory: LexemMemory, identifier: String) {
        // Prevent modifications if the object is constant.
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The object is immutable therefore cannot be modified") {}
        }

        if (isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObject,
                    "The object is constant therefore it cannot be modified") {}
        }

        var property = properties[identifier] ?: throw AngmarAnalyzerException(
                AngmarAnalyzerExceptionType.UndefinedObjectProperty,
                "The object hasn't a property called '$identifier'") {}

        cloneProperties()

        if (property.belongsTo != this) {
            property = property.clone(this)
        }

        property.isConstant = true
    }

    /**
     * Gets all iterable properties of the object.
     */
    fun getAllIterableProperties() = properties.filterValues { it.isIterable }.toMutableMap()

    /**
     * Gets all properties of the object.
     */
    private fun getAllProperties() = properties.toMutableMap()

    /**
     * Clones the properties map.
     */
    private fun cloneProperties() {
        if (!isPropertiesCloned) {
            properties = HashMap(properties)
            isPropertiesCloned = true
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun memoryShift(memory: LexemMemory) = if (!isWritable) {
        this
    } else {
        LxmObject(memory, oldVersion = this)
    }

    override fun memoryDealloc(memory: LexemMemory) {
        if (isPropertiesCloned) {
            properties.clear()
        }

        prototypeReference = null
    }

    override fun spatialGarbageCollect(memory: LexemMemory, gcFifo: GarbageCollectorFifo) {
        for ((_, property) in properties) {
            property.value.spatialGarbageCollect(memory, gcFifo)
        }

        prototypeReference?.spatialGarbageCollect(memory, gcFifo)
    }

    override fun getType(memory: LexemMemory): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(memory, toWrite = false)
        return context.getPropertyValue(memory, ObjectType.TypeName) as LxmReference
    }

    override fun getPrototype(memory: LexemMemory) = prototypeReference ?: super.getPrototype(memory)

    override fun toLexemString(memory: LexemMemory) = LxmString.ObjectToString

    override fun toString() = StringBuilder().apply {
        if (isConstant) {
            append(ObjectNode.constantToken)
        }

        append(ObjectNode.startToken)

        val properties = getAllIterableProperties()
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

        if (properties.size > 4) {
            append(" and ${properties.size - 4} more")
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
            val prop = LxmObjectProperty(belongsTo, isConstant = isConstant, isIterable = isIterable)

            prop.value = value
            return prop
        }

        /**
         * Replaces the value handling the memory references.
         */
        fun replaceValue(newValue: LexemPrimitive) {
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
