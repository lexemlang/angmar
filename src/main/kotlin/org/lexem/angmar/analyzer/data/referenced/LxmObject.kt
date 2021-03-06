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
    val oldObject: LxmObject?
    val prototypeReference: LxmReference?
    private var isConstant = false
    protected var properties = mutableMapOf<String, LxmObjectProperty>()

    // CONSTRUCTORS -----------------------------------------------------------

    constructor() {
        oldObject = null
        prototypeReference = null
    }

    constructor(oldObject: LxmObject) {
        this.oldObject = oldObject
        prototypeReference = oldObject.prototypeReference
    }

    constructor(prototypeReference: LxmReference, memory: LexemMemory) {
        oldObject = null
        this.prototypeReference = prototypeReference
        prototypeReference.increaseReferences(memory)
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the value of a property. It searches also in prototypes.
     */
    open fun getPropertyValue(memory: LexemMemory, identifier: String): LexemPrimitive? {
        val property = getOwnPropertyDescriptor(memory, identifier)

        if (property == null) {
            val prototype = getPrototype(memory)

            // Avoid infinite loops.
            if (prototype == this) {
                return null
            }

            return prototype.getPropertyValue(memory, identifier)
        }

        if (property.isRemoved) {
            return null
        }

        return property.value
    }

    /**
     * Gets the property descriptor of the specified property inside the current object.
     */
    fun getOwnPropertyDescriptor(memory: LexemMemory, identifier: String): LxmObjectProperty? =
            properties[identifier] ?: oldObject?.getOwnPropertyDescriptor(memory, identifier)

    /**
     * Gets the property descriptor of the specified property.
     */
    fun getPropertyDescriptor(memory: LexemMemory, identifier: String): LxmObjectProperty? {
        val property = getOwnPropertyDescriptor(memory, identifier)

        if (property == null) {
            val prototype = getPrototype(memory)

            // Avoid infinite loops.
            if (prototype == this) {
                return null
            }

            return prototype.getPropertyDescriptor(memory, identifier)
        }

        return property
    }

    /**
     * Gets the final value of a property. It searches also in prototypes.
     */

    inline fun <reified T : LexemMemoryValue> getDereferencedProperty(memory: LexemMemory, identifier: String): T? {
        val property = getPropertyValue(memory, identifier) ?: return null
        return property.dereference(memory) as? T
    }

    /**
     * Sets a new value to the property or creates a new property with the specified value.
     */
    fun setProperty(memory: LexemMemory, identifier: String, value: LexemPrimitive, isConstant: Boolean = false,
            isIterable: Boolean = true, ignoringConstant: Boolean = false) {
        // Prevent modifications if the object is constant.
        if (!ignoringConstant && this.isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObject,
                    "The object is constant therefore cannot be modified") {}
        }

        val currentProperty = properties[identifier]
        val lastProperty = oldObject?.getOwnPropertyDescriptor(memory, identifier)

        when {
            // No property.
            currentProperty == null && lastProperty == null -> {
                val property = LxmObjectProperty(isConstant, isIterable, false)
                property.replaceValue(memory, value)
                properties[identifier] = property
            }

            // Current property.
            currentProperty != null -> {
                if (!ignoringConstant && currentProperty.isConstant) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObjectProperty,
                            "The object property called '$identifier' is constant therefore it cannot be modified") {}
                }

                currentProperty.replaceValue(memory, value)
                currentProperty.isConstant = isConstant
                currentProperty.isIterable = isIterable
                currentProperty.isRemoved = false
            }

            // Property in past version of the object.
            lastProperty != null -> {
                if (!ignoringConstant && lastProperty.isConstant) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObjectProperty,
                            "The object property called '$identifier' is constant therefore it cannot be modified") {}
                }

                val property = lastProperty.clone(isConstant = isConstant, isIterable = isIterable, isRemoved = false)
                property.replaceValue(memory, value)
                properties[identifier] = property
            }
        }
    }

    /**
     * Sets a new value to the property in any of the prototypes that have that property.
     */
    fun setPropertyAsContext(memory: LexemMemory, identifier: String, value: LexemPrimitive,
            isConstant: Boolean = false) {
        // Prevent modifications if the object is constant.
        if (this.isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObject,
                    "The object is constant therefore cannot be modified") {}
        }

        // Check the current object and the prototype.
        val currentProperty = getOwnPropertyDescriptor(memory, identifier)
        val prototype = getPrototype(memory)
        val prototypeProperty = prototype.getPropertyValue(memory, identifier)

        // Set in prototype.
        if (currentProperty == null && prototypeProperty != null) {
            return prototype.setPropertyAsContext(memory, identifier, value, isConstant)
        }

        // Set in current.
        return setProperty(memory, identifier, value, isConstant)
    }

    /**
     * Returns whether the object contains a property or not.
     */
    fun containsOwnProperty(memory: LexemMemory, identifier: String): Boolean {
        val prop = getOwnPropertyDescriptor(memory, identifier)
        return prop != null && !prop.isRemoved
    }

    /**
     * Removes a property.
     */
    fun removeProperty(memory: LexemMemory, identifier: String) {
        // Prevent modifications if the object is constant.
        if (isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObject,
                    "The object is constant therefore it cannot be modified") {}
        }

        val currentProperty = properties[identifier]
        val lastProperty = oldObject?.getOwnPropertyDescriptor(memory, identifier)

        when {
            // Current property
            currentProperty != null -> {
                if (currentProperty.isConstant) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObjectProperty,
                            "The object property called '$identifier' is constant therefore it cannot be modified") {}
                }

                if (lastProperty != null) {
                    // Set property to remove.
                    currentProperty.isIterable = false
                    currentProperty.isRemoved = true
                    currentProperty.replaceValue(memory, LxmNil)
                } else {
                    // Remove property.
                    properties.remove(identifier)
                    currentProperty.replaceValue(memory, LxmNil)
                }
            }

            // Property in past version of the object
            lastProperty != null -> {
                if (lastProperty.isConstant) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObjectProperty,
                            "The object property called '$identifier' is constant therefore it cannot be modified") {}
                }

                // Set property to remove.
                val cloned = lastProperty.clone(isIterable = false, isRemoved = true)
                cloned.replaceValue(memory, LxmNil)
                properties[identifier] = cloned
            }
        }
    }

    /**
     * Makes the object constant.
     */
    fun makeConstant(memory: LexemMemory) {
        isConstant = true
    }

    /**
     * Makes a property constant.
     */
    fun makePropertyConstant(memory: LexemMemory, identifier: String) {
        // Prevent modifications if the object is constant.
        if (isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObject,
                    "The object is constant therefore it cannot be modified") {}
        }

        val currentProperty = properties[identifier]
        val lastProperty = oldObject?.getOwnPropertyDescriptor(memory, identifier)

        when {
            // No property
            currentProperty == null && lastProperty == null -> {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.UndefinedObjectProperty,
                        "The object hasn't a property called '$identifier'") {}
            }

            // Current property
            currentProperty != null -> {
                currentProperty.isConstant = true
            }

            // Property in past version of the object
            lastProperty != null -> {
                // Avoid to shift if it is constant.
                if (lastProperty.isConstant) {
                    return
                }

                properties[identifier] = lastProperty.clone(isConstant = true)
            }
        }
    }

    /**
     * Gets all iterable properties of the object.
     */
    fun getAllIterableProperties() = getAllProperties().filter { it.value.isIterable }

    /**
     * Gets all properties of the object.
     */
    private fun getAllProperties(): Map<String, LxmObjectProperty> {
        val result = mutableMapOf<String, LxmObjectProperty>()

        var currentObject: LxmObject? = this
        while (currentObject != null) {
            for ((key, value) in currentObject.properties) {
                result.putIfAbsent(key, value)
            }

            currentObject = currentObject.oldObject
        }

        return result
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override val isImmutable: Boolean
        get() = isConstant

    override fun clone() = if (isImmutable) {
        this
    } else {
        LxmObject(this)
    }

    override fun memoryDealloc(memory: LexemMemory) {
        for (i in getAllProperties()) {
            val reference = i.value.value
            if (reference is LxmReference) {
                reference.decreaseReferences(memory)
            }
        }

        prototypeReference?.decreaseReferences(memory)
        properties.clear()
    }

    override fun spatialGarbageCollect(memory: LexemMemory) {
        for ((key, _) in getAllProperties()) {
            getPropertyValue(memory, key)!!.spatialGarbageCollect(memory)
        }

        prototypeReference?.spatialGarbageCollect(memory)
    }

    override fun getType(memory: LexemMemory): LxmObject =
            AnalyzerCommons.getStdLibContextElement(memory, ObjectType.TypeName)

    override fun getPrototype(memory: LexemMemory) = if (prototypeReference != null) {
        prototypeReference.dereference(memory) as LxmObject
    } else {
        super.getPrototype(memory)
    }

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
    class LxmObjectProperty(var isConstant: Boolean, var isIterable: Boolean, var isRemoved: Boolean) {
        var value: LexemPrimitive = LxmNil

        // METHODS ------------------------------------------------------------

        /**
         * Clones the object property only if it is not constant.
         */
        fun clone(isConstant: Boolean? = null, isIterable: Boolean? = null,
                isRemoved: Boolean? = null): LxmObjectProperty {
            val prop = LxmObjectProperty(isConstant = isConstant ?: this.isConstant,
                    isIterable = isIterable ?: this.isIterable, isRemoved = isRemoved ?: this.isRemoved)

            prop.value = this.value
            return prop
        }

        /**
         * Replaces the value handling the memory references.
         */
        fun replaceValue(memory: LexemMemory, newValue: LexemPrimitive) {
            memory.replacePrimitives(value, newValue)
            value = newValue
        }

        override fun toString() = StringBuilder().apply {
            if (isRemoved) {
                append("REMOVED")
            } else {
                if (isConstant) {
                    append('#')
                }

                if (isIterable) {
                    append("[]")
                }

                append(" ")

                append(value)
            }
        }.toString()
    }

    companion object {
        val Empty = LxmObject()

        init {
            Empty.isConstant = true
        }
    }
}
