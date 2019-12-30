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
    private var properties = mutableMapOf<String, LxmObjectProperty>()
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
    protected constructor(bigNode: BigNode, oldVersion: LxmObject) : super(bigNode, oldVersion) {
        prototypeReference = oldVersion.prototypeReference
        isConstant = oldVersion.isConstant
        isWritable = oldVersion.isWritable
        properties = oldVersion.properties
        isPropertiesCloned = false
    }

    /**
     * Creates an object without explicit prototype.
     */
    constructor(memory: LexemMemory) : super(memory) {
        prototypeReference = null
    }

    /**
     * Creates an object with explicit prototype.
     */
    constructor(memory: LexemMemory, prototype: LxmObject) : super(memory) {
        prototypeReference = prototype.getPrimitive()
        prototypeReference!!.increaseReferences(memory.lastNode)
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the value of a property. It searches also in prototypes.
     */
    open fun getPropertyValue(identifier: String) = getPropertyDescriptor(identifier)?.value

    /**
     * Gets the property descriptor of the specified property.
     */
    fun getPropertyDescriptor(identifier: String): LxmObjectProperty? {
        val property = properties[identifier]

        if (property == null) {
            // Avoid infinite loops.
            if (isFinalInHierarchy) {
                return null
            }

            val prototype = getPrototypeAsObject(bigNode, toWrite = false)
            return prototype.getPropertyDescriptor(identifier)
        }

        return property
    }

    /**
     * Gets the final value of a property. It searches also in prototypes.
     */
    inline fun <reified T : LexemMemoryValue> getDereferencedProperty(identifier: String, toWrite: Boolean) =
            getPropertyValue(identifier)?.dereference(bigNode, toWrite) as? T

    /**
     * Sets a new value to the property or creates a new property with the specified value.p
     */
    fun setProperty(identifier: String, value: LexemMemoryValue, isConstant: Boolean = false,
            ignoreConstant: Boolean = false) {
        if (!ignoreConstant && this.isConstant) {
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
            property.replaceValue(valuePrimitive)
        } else {
            if (!ignoreConstant && property.isConstant) {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObjectProperty,
                        "The object property called '$identifier' is constant therefore it cannot be modified") {}
            }

            if (property.belongsTo != this) {
                property = property.clone(this)
                properties[identifier] = property
            }

            property.replaceValue(valuePrimitive)
            property.isConstant = isConstant
        }
    }

    /**
     * Sets a new value to the property in any of the prototypes that have that property.
     */
    fun setPropertyAsContext(identifier: String, value: LexemMemoryValue, isConstant: Boolean = false) {
        if (this.isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObject,
                    "The object is constant therefore cannot be modified") {}
        }

        // Avoid infinite loops.
        if (isFinalInHierarchy) {
            return setProperty(identifier, value, isConstant)
        }

        // Check the current object and the prototype.
        val currentProperty = properties[identifier]

        if (currentProperty == null) {
            var prototype = getPrototypeAsObject(bigNode, toWrite = false)
            while (true) {
                val prototypeProperty = prototype.properties[identifier]

                // Set in prototype.
                if (prototypeProperty != null) {
                    prototype = prototype.getPrimitive().dereferenceAs(bigNode, toWrite = true)!!
                    return prototype.setProperty(identifier, value, isConstant)
                }

                // Avoid infinite loops.
                if (prototype.isFinalInHierarchy) {
                    break
                }

                prototype = prototype.getPrototypeAsObject(bigNode, toWrite = false)
            }
        }


        // Set in current.
        return setProperty(identifier, value, isConstant)
    }

    /**
     * Returns whether the object contains a property or not.
     */
    fun containsOwnProperty(identifier: String) = properties[identifier] != null

    /**
     * Removes a property.
     */
    fun removeProperty(identifier: String, ignoreConstant: Boolean = false) {
        if (!ignoreConstant && isConstant) {
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

        // Decrease references.
        property.replaceValue(LxmNil)
    }

    /**
     * Makes the object constant.
     */
    fun makeConstant() {
        if (isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantSet,
                    "The set is constant therefore cannot be modified") {}
        }

        isConstant = true
    }

    /**
     * Makes the list constant and not writable.
     */
    fun makeConstantAndNotWritable() {
        if (isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantSet,
                    "The set is constant therefore cannot be modified") {}
        }

        isConstant = true
        isWritable = false
    }

    /**
     * Makes a property constant.
     */
    fun makePropertyConstant(identifier: String) {
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
            properties = properties.toMutableMap()
            isPropertiesCloned = true
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun memoryClone(bigNode: BigNode) = LxmObject(bigNode, this)

    override fun memoryDealloc() {
        getAllProperties().map { it.value.value }.forEach { it.decreaseReferences(bigNode) }

        prototypeReference?.decreaseReferences(bigNode)
        prototypeReference = null
    }

    override fun spatialGarbageCollect(gcFifo: GarbageCollectorFifo) {
        getAllProperties().map { it.value.value }.forEach { it.spatialGarbageCollect(gcFifo) }

        prototypeReference?.spatialGarbageCollect(gcFifo)
    }

    override fun getType(bigNode: BigNode): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(bigNode, toWrite = false)
        return context.getPropertyValue(ObjectType.TypeName) as LxmReference
    }

    override fun getPrototype(bigNode: BigNode) = prototypeReference ?: super.getPrototype(bigNode)

    override fun toLexemString(bigNode: BigNode) = LxmString.ObjectToString

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
        fun replaceValue(newValue: LexemPrimitive) {
            // Keep this to replace the elements before possibly remove the references.
            val oldValue = value
            value = newValue
            MemoryUtils.replacePrimitives(belongsTo.bigNode, oldValue, newValue)
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
