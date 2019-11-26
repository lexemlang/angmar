package org.lexem.angmar.analyzer.data.referenced

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*

internal class LxmObjectTest {
    @Test
    fun `test constructors`() {
        val old = LxmObject()

        Assertions.assertNull(old.oldObject, "The oldObject property is incorrect")
        Assertions.assertNull(old.prototypeReference, "The prototypeReference property is incorrect")

        val new = LxmObject(old)

        Assertions.assertEquals(old, new.oldObject, "The oldObject property is incorrect")
        Assertions.assertNull(new.prototypeReference, "The prototypeReference property is incorrect")
    }

    @Test
    fun `test constructors with prototype`() {
        val memory = LexemMemory()

        val old = LxmObject()
        val oldRef = memory.add(old)
        val oldCell = memory.lastNode.getCell(oldRef.position)

        Assertions.assertNull(old.oldObject, "The oldObject property is incorrect")
        Assertions.assertNull(old.prototypeReference, "The prototypeReference property is incorrect")
        Assertions.assertEquals(0, oldCell.referenceCount, "The referenceCount property is incorrect")

        val new1 = LxmObject(oldRef, memory)
        val new1Ref = memory.add(old)
        val new1Cell = memory.lastNode.getCell(new1Ref.position)

        Assertions.assertNull(new1.oldObject, "The oldObject property is incorrect")
        Assertions.assertEquals(oldRef, new1.prototypeReference, "The prototypeReference property is incorrect")
        Assertions.assertEquals(0, new1Cell.referenceCount, "The referenceCount property is incorrect")
        Assertions.assertEquals(1, oldCell.referenceCount, "The referenceCount property is incorrect")
    }

    @Test
    fun `test get property`() {
        val memory = LexemMemory()

        val prototype = LxmObject()
        val prototypeRef = memory.add(prototype)
        prototype.setProperty(memory, "prototype", LxmLogic.False)

        val old = LxmObject(prototypeRef, memory)
        old.setProperty(memory, "old", LxmLogic.True)

        val new = old.clone()
        new.setProperty(memory, "new", LxmInteger.Num1)

        Assertions.assertEquals(LxmLogic.True, new.getPropertyValue(memory, "old"), "The new property is incorrect")
        Assertions.assertEquals(LxmLogic.False, new.getPropertyValue(memory, "prototype"),
                "The prototype property is incorrect")
        Assertions.assertEquals(LxmInteger.Num1, new.getPropertyValue(memory, "new"), "The old property is incorrect")
    }

    @Test
    fun `test get removed property`() {
        val propName = "test"
        val memory = LexemMemory()

        val old = LxmObject()
        old.setProperty(memory, propName, LxmLogic.True)

        val new = LxmObject(old)
        new.removeProperty(memory, propName)

        val result = new.getPropertyValue(memory, propName)
        Assertions.assertNull(result, "The result is incorrect")
    }

    @Test
    fun `test get undefined property`() {
        val propName = "test"
        val analyzer = LexemAnalyzer(ParserNode.Companion.EmptyParserNode)
        val memory = analyzer.memory

        val obj = LxmObject()
        val result = obj.getPropertyValue(memory, propName)
        Assertions.assertNull(result, "The result is incorrect")
    }

    @Test
    fun `test get property descriptor`() {
        val memory = LexemMemory()

        val prototype = LxmObject()
        val prototypeRef = memory.add(prototype)
        prototype.setProperty(memory, "prototype", LxmLogic.False)

        val old = LxmObject(prototypeRef, memory)
        old.setProperty(memory, "old", LxmLogic.True)

        val new = old.clone()
        new.setProperty(memory, "new", LxmInteger.Num1)

        val oldDescriptor = new.getOwnPropertyDescriptor(memory, "old") ?: throw Error("The old property is incorrect")
        val newDescriptor = new.getOwnPropertyDescriptor(memory, "new") ?: throw Error("The new property is incorrect")

        Assertions.assertEquals(LxmInteger.Num1, newDescriptor.value, "The new property is incorrect")
        Assertions.assertNull(new.getOwnPropertyDescriptor(memory, "prototype"), "The prototype property is incorrect")
        Assertions.assertEquals(LxmLogic.True, oldDescriptor.value, "The old property is incorrect")
    }

    @Test
    fun `test get dereferenced property`() {
        val memory = LexemMemory()

        val other = LxmObject()
        val otherRef = memory.add(other)

        val obj = LxmObject()
        obj.setProperty(memory, "a", LxmLogic.False)
        obj.setProperty(memory, "b", otherRef)

        val derefPrimitive = obj.getDereferencedProperty<LxmLogic>(memory, "a")
        val derefOther = obj.getDereferencedProperty<LxmObject>(memory, "b")

        Assertions.assertEquals(LxmLogic.False, derefPrimitive, "The a property is incorrect")
        Assertions.assertEquals(other, derefOther, "The b property is incorrect")

        Assertions.assertNull(obj.getDereferencedProperty<LxmObject>(memory, "a"), "The a property is incorrect")
        Assertions.assertNull(obj.getDereferencedProperty<LxmLogic>(memory, "b"), "The b property is incorrect")
    }

    @Test
    fun `test set property`() {
        val analyzer = LexemAnalyzer(ParserNode.Companion.EmptyParserNode)
        val memory = analyzer.memory

        val prototype = LxmObject()
        val prototypeRef = memory.add(prototype)
        prototype.setProperty(memory, "prototype", LxmLogic.False)

        val old = LxmObject(prototypeRef, memory)
        old.setProperty(memory, "old", LxmLogic.True)
        Assertions.assertEquals(LxmLogic.True, old.getPropertyValue(memory, "old"), "The old property is incorrect")


        val new = old.clone()
        new.setProperty(memory, "old", LxmInteger.Num1)

        Assertions.assertEquals(LxmLogic.True, old.getPropertyValue(memory, "old"), "The old property is incorrect")
        Assertions.assertEquals(LxmInteger.Num1, new.getPropertyValue(memory, "old"), "The old property is incorrect")
        Assertions.assertNull(prototype.getPropertyValue(memory, "old"), "The old property is incorrect")

        new.setProperty(memory, "old", LxmInteger.Num10)

        Assertions.assertEquals(LxmLogic.True, old.getPropertyValue(memory, "old"), "The old property is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, new.getPropertyValue(memory, "old"), "The old property is incorrect")
        Assertions.assertNull(prototype.getPropertyValue(memory, "old"), "The old property is incorrect")

        new.setProperty(memory, "prototype", LxmInteger.Num1)

        Assertions.assertEquals(LxmInteger.Num1, new.getPropertyValue(memory, "prototype"),
                "The prototype property is incorrect")
        Assertions.assertEquals(LxmLogic.False, prototype.getPropertyValue(memory, "prototype"),
                "The prototype property is incorrect")
    }

    @Test
    fun `test set property updating references`() {
        val prop1Name = "test1"
        val prop2Name = "test2"
        val memory = LexemMemory()

        val obj1 = LxmObject()
        val obj1Ref = memory.add(obj1)
        obj1Ref.increaseReferences(memory)
        val obj1Cell = memory.lastNode.getCell(obj1Ref.position)

        val obj2 = LxmObject()
        val obj2Ref = memory.add(obj2)
        obj2Ref.increaseReferences(memory)
        val obj2Cell = memory.lastNode.getCell(obj2Ref.position)

        val obj = LxmObject()
        val objRef = memory.add(obj)
        objRef.increaseReferences(memory)
        val objCell = memory.lastNode.getCell(objRef.position)

        Assertions.assertEquals(1, obj1Cell.referenceCount, "The referenceCount property is incorrect")
        Assertions.assertEquals(1, obj2Cell.referenceCount, "The referenceCount property is incorrect")

        obj.setProperty(memory, prop1Name, LxmLogic.True)
        obj.setProperty(memory, prop2Name, obj1Ref)

        Assertions.assertEquals(2, obj1Cell.referenceCount, "The referenceCount property is incorrect")
        Assertions.assertEquals(1, obj2Cell.referenceCount, "The referenceCount property is incorrect")

        obj.setProperty(memory, prop1Name, obj2Ref)

        Assertions.assertEquals(2, obj1Cell.referenceCount, "The referenceCount property is incorrect")
        Assertions.assertEquals(2, obj2Cell.referenceCount, "The referenceCount property is incorrect")

        obj.setProperty(memory, prop2Name, LxmLogic.True)

        Assertions.assertEquals(1, obj1Cell.referenceCount, "The referenceCount property is incorrect")
        Assertions.assertEquals(2, obj2Cell.referenceCount, "The referenceCount property is incorrect")

        obj.setProperty(memory, prop1Name, obj1Ref)

        Assertions.assertEquals(2, obj1Cell.referenceCount, "The referenceCount property is incorrect")
        Assertions.assertEquals(1, obj2Cell.referenceCount, "The referenceCount property is incorrect")

        obj.setProperty(memory, prop1Name, obj1Ref)

        Assertions.assertEquals(2, obj1Cell.referenceCount, "The referenceCount property is incorrect")
        Assertions.assertEquals(1, obj2Cell.referenceCount, "The referenceCount property is incorrect")
    }

    @Test
    fun `test set property as context`() {
        val analyzer = LexemAnalyzer(ParserNode.Companion.EmptyParserNode)
        val memory = analyzer.memory

        val prototype = LxmObject()
        val prototypeRef = memory.add(prototype)
        prototype.setPropertyAsContext(memory, "prototype", LxmLogic.False)

        val old = LxmObject(prototypeRef, memory)
        old.setPropertyAsContext(memory, "old", LxmLogic.True)

        val new = old.clone()
        new.setPropertyAsContext(memory, "old", LxmInteger.Num1)

        Assertions.assertEquals(LxmLogic.True, old.getPropertyValue(memory, "old"), "The old property is incorrect")
        Assertions.assertEquals(LxmInteger.Num1, new.getPropertyValue(memory, "old"), "The old property is incorrect")
        Assertions.assertNull(prototype.getPropertyValue(memory, "old"), "The old property is incorrect")

        new.setPropertyAsContext(memory, "old", LxmInteger.Num10)

        Assertions.assertEquals(LxmLogic.True, old.getPropertyValue(memory, "old"), "The old property is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, new.getPropertyValue(memory, "old"), "The old property is incorrect")
        Assertions.assertNull(prototype.getPropertyValue(memory, "old"), "The old property is incorrect")

        new.setPropertyAsContext(memory, "prototype", LxmInteger.Num1)

        Assertions.assertEquals(LxmInteger.Num1, new.getPropertyValue(memory, "prototype"),
                "The prototype property is incorrect")
        Assertions.assertEquals(LxmInteger.Num1, prototype.getPropertyValue(memory, "prototype"),
                "The prototype property is incorrect")
    }

    @Test
    fun `test set property ignoring constant`() {
        val analyzer = LexemAnalyzer(ParserNode.Companion.EmptyParserNode)
        val memory = analyzer.memory
        val propName = "test"

        val obj = LxmObject()
        obj.makeConstant(analyzer.memory)

        obj.setProperty(analyzer.memory, propName, LxmInteger.Num10, ignoringConstant = true)

        Assertions.assertEquals(LxmInteger.Num10, obj.getPropertyValue(memory, propName),
                "The $propName property is incorrect")
    }

    @Test
    fun `test set property changing the inner attributes`() {
        val analyzer = LexemAnalyzer(ParserNode.Companion.EmptyParserNode)
        val memory = analyzer.memory
        val propName = "test"

        val obj = LxmObject()
        obj.setProperty(analyzer.memory, propName, LxmInteger.Num10)

        var cell = obj.getOwnPropertyDescriptor(analyzer.memory, propName)!!

        Assertions.assertFalse(cell.isConstant, "The isConstant property is incorrect")
        Assertions.assertTrue(cell.isIterable, "The isIterable property is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, obj.getPropertyValue(memory, propName),
                "The $propName property is incorrect")

        obj.setProperty(analyzer.memory, propName, LxmLogic.True, isIterable = false, isConstant = true)

        cell = obj.getOwnPropertyDescriptor(analyzer.memory, propName)!!

        Assertions.assertTrue(cell.isConstant, "The isConstant property is incorrect")
        Assertions.assertFalse(cell.isIterable, "The isIterable property is incorrect")
        Assertions.assertEquals(LxmLogic.True, obj.getPropertyValue(memory, propName),
                "The $propName property is incorrect")
    }

    @Test
    fun `test set property changing the inner attributes recursively`() {
        val analyzer = LexemAnalyzer(ParserNode.Companion.EmptyParserNode)
        val memory = analyzer.memory
        val propName = "test"

        val old = LxmObject()
        old.setProperty(analyzer.memory, propName, LxmInteger.Num10)

        var cellOld = old.getOwnPropertyDescriptor(analyzer.memory, propName)!!

        Assertions.assertFalse(cellOld.isConstant, "The isConstant property is incorrect")
        Assertions.assertTrue(cellOld.isIterable, "The isIterable property is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, old.getPropertyValue(memory, propName),
                "The $propName property is incorrect")


        val new = old.clone()
        new.setProperty(analyzer.memory, propName, LxmLogic.True, isIterable = false, isConstant = true)

        cellOld = old.getOwnPropertyDescriptor(analyzer.memory, propName)!!
        val cellNew = new.getOwnPropertyDescriptor(analyzer.memory, propName)!!

        Assertions.assertFalse(cellOld.isConstant, "The isConstant property is incorrect")
        Assertions.assertTrue(cellOld.isIterable, "The isIterable property is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, old.getPropertyValue(memory, propName),
                "The $propName property is incorrect")
        Assertions.assertTrue(cellNew.isConstant, "The isConstant property is incorrect")
        Assertions.assertFalse(cellNew.isIterable, "The isIterable property is incorrect")
        Assertions.assertEquals(LxmLogic.True, new.getPropertyValue(memory, propName),
                "The $propName property is incorrect")
    }

    @Test
    fun `test contains own property`() {
        val memory = LexemMemory()

        val prototype = LxmObject()
        val prototypeRef = memory.add(prototype)
        prototype.setProperty(memory, "prototype", LxmLogic.False)

        val old = LxmObject(prototypeRef, memory)
        old.setProperty(memory, "old", LxmLogic.True)

        val new = old.clone()
        new.setProperty(memory, "new", LxmLogic.False)

        Assertions.assertTrue(new.containsOwnProperty(memory, "old"), "The old property is incorrect")
        Assertions.assertTrue(new.containsOwnProperty(memory, "new"), "The old property is incorrect")
        Assertions.assertFalse(new.containsOwnProperty(memory, "prototype"), "The old property is incorrect")
    }

    @Test
    fun `test contains own removed property`() {
        val memory = LexemMemory()

        val prototype = LxmObject()
        val prototypeRef = memory.add(prototype)

        val old = LxmObject(prototypeRef, memory)
        old.setProperty(memory, "old", LxmLogic.True)

        val new = old.clone()
        new.removeProperty(memory, "old")

        Assertions.assertFalse(new.containsOwnProperty(memory, "old"), "The old property is incorrect")
    }

    @Test
    fun `test remove property from current`() {
        val propName = "property"
        val memory = LexemMemory()

        val obj = LxmObject()
        obj.setProperty(memory, propName, LxmLogic.True)

        obj.removeProperty(memory, propName)

        Assertions.assertNull(obj.getOwnPropertyDescriptor(memory, propName), "The $propName property mustn't be got")
    }

    @Test
    fun `test remove property existing in old`() {
        val propName = "property"
        val memory = LexemMemory()

        val prototype = LxmObject()
        val prototypeRef = memory.add(prototype)
        prototype.setProperty(memory, propName, LxmLogic.False)

        val old = LxmObject(prototypeRef, memory)
        old.setProperty(memory, propName, LxmLogic.True)

        val new = old.clone()
        new.setProperty(memory, propName, LxmInteger.Num10)

        new.removeProperty(memory, propName)

        Assertions.assertEquals(LxmLogic.True, old.getOwnPropertyDescriptor(memory, propName)?.value,
                "The $propName property is incorrect")
        Assertions.assertEquals(LxmLogic.False, prototype.getOwnPropertyDescriptor(memory, propName)?.value,
                "The $propName property is incorrect")

        val descriptor =
                new.getOwnPropertyDescriptor(memory, propName) ?: throw Error("The $propName property is incorrect")
        Assertions.assertFalse(descriptor.isIterable, "The isIterable property is incorrect")
        Assertions.assertFalse(descriptor.isConstant, "The isConstant property is incorrect")
        Assertions.assertEquals(LxmNil, descriptor.value, "The value property is incorrect")
    }

    @Test
    fun `test make constant`() {
        val memory = LexemMemory()

        val obj = LxmObject()
        Assertions.assertFalse(obj.isImmutable, "The isImmutable property is incorrect")

        obj.makeConstant(memory)
        Assertions.assertTrue(obj.isImmutable, "The isImmutable property is incorrect")
    }

    @Test
    fun `test make property constant`() {
        val propName = "test"
        val memory = LexemMemory()

        val obj = LxmObject()
        obj.setProperty(memory, propName, LxmLogic.True)

        val descriptorPre = obj.getOwnPropertyDescriptor(memory, propName)!!
        Assertions.assertFalse(descriptorPre.isConstant, "The isConstant property is incorrect")
        Assertions.assertTrue(descriptorPre.isIterable, "The isIterable property is incorrect")
        Assertions.assertFalse(descriptorPre.isRemoved, "The isRemoved property is incorrect")
        Assertions.assertEquals(LxmLogic.True, descriptorPre.value, "The value property is incorrect")

        obj.makePropertyConstant(memory, propName)
        val descriptorPost = obj.getOwnPropertyDescriptor(memory, propName)!!
        Assertions.assertTrue(descriptorPost.isConstant, "The isConstant property is incorrect")
        Assertions.assertTrue(descriptorPost.isIterable, "The isIterable property is incorrect")
        Assertions.assertFalse(descriptorPost.isRemoved, "The isRemoved property is incorrect")
        Assertions.assertEquals(LxmLogic.True, descriptorPost.value, "The value property is incorrect")
    }

    @Test
    fun `test get all iterable properties`() {
        val oldPropsCount = 1
        val protoPropsCount = 3
        val newPropsCount = 6
        val memory = LexemMemory()

        val prototype = LxmObject()
        val prototypeRef = memory.add(prototype)
        for (i in 0 until protoPropsCount) {
            prototype.setProperty(memory, "prototype$i", LxmLogic.False)
        }

        val old = LxmObject(prototypeRef, memory)
        for (i in 0 until oldPropsCount) {
            old.setProperty(memory, "old$i", LxmLogic.True)
        }

        val new = old.clone()
        for (i in 0 until newPropsCount) {
            new.setProperty(memory, "new$i", LxmInteger.Num1)
        }

        val properties = new.getAllIterableProperties()

        Assertions.assertEquals(newPropsCount + oldPropsCount, properties.size, "The number of properties is incorrect")

        for (i in properties) {
            when {
                i.key.startsWith("new") -> Assertions.assertEquals(LxmInteger.Num1, i.value.value,
                        "The new property is incorrect")
                i.key.startsWith("old") -> Assertions.assertEquals(LxmLogic.True, i.value.value,
                        "The old property is incorrect")
                else -> throw Error("The key property is incorrect")
            }
        }
    }

    @Test
    fun `test clone`() {
        val memory = LexemMemory()

        val old = LxmObject()
        val cloned = old.clone()

        Assertions.assertEquals(old, cloned.oldObject, "The oldObject is incorrect")

        val oldConst = LxmObject()
        oldConst.makeConstant(memory)
        val clonedConst = oldConst.clone()

        Assertions.assertTrue(clonedConst.isImmutable, "The isImmutable is incorrect")
        Assertions.assertEquals(oldConst, clonedConst, "The clonedConst is incorrect")
    }

    @Test
    fun `test memory dealloc`() {
        val memory = LexemMemory()

        val obj = LxmObject()
        val objRef = memory.add(obj)
        objRef.increaseReferences(memory)
        val objCell = memory.lastNode.getCell(objRef.position)

        val prototype = LxmObject()
        val prototypeRef = memory.add(prototype)
        prototypeRef.increaseReferences(memory)
        val prototypeCell = memory.lastNode.getCell(prototypeRef.position)

        val old = LxmObject(prototypeRef, memory)
        old.setProperty(memory, "test-old", LxmLogic.True)
        old.setProperty(memory, "testObj-old", objRef)
        val oldRef = memory.add(old)
        oldRef.increaseReferences(memory)
        val oldCell = memory.lastNode.getCell(oldRef.position)

        val new = old.clone()
        old.setProperty(memory, "test-new", LxmLogic.False)
        old.setProperty(memory, "testObj-new", objRef)
        val newRef = memory.add(old)
        val newCell = memory.lastNode.getCell(newRef.position)

        Assertions.assertEquals(0, newCell.referenceCount, "The referenceCount property is incorrect")
        Assertions.assertEquals(3, objCell.referenceCount, "The referenceCount property is incorrect")
        Assertions.assertEquals(1, oldCell.referenceCount, "The referenceCount property is incorrect")
        Assertions.assertEquals(2, prototypeCell.referenceCount, "The referenceCount property is incorrect")

        new.memoryDealloc(memory)

        Assertions.assertEquals(0, newCell.referenceCount, "The referenceCount property is incorrect")
        Assertions.assertEquals(1, objCell.referenceCount, "The referenceCount property is incorrect")
        Assertions.assertEquals(1, oldCell.referenceCount, "The referenceCount property is incorrect")
        Assertions.assertEquals(1, prototypeCell.referenceCount, "The referenceCount property is incorrect")
    }

    @Test
    fun `test get type`() {
        val analyzer = LexemAnalyzer(ParserNode.Companion.EmptyParserNode)
        val memory = analyzer.memory

        val obj = LxmObject()
        val type = obj.getType(memory)
        val context = AnalyzerCommons.getCurrentContext(memory)
        val objectType = context.getDereferencedProperty<LxmObject>(memory, ObjectType.TypeName)!!
        Assertions.assertEquals(objectType, type, "The type is incorrect")
    }

    @Test
    fun `test get prototype`() {
        val analyzer = LexemAnalyzer(ParserNode.Companion.EmptyParserNode)
        val memory = analyzer.memory

        val prototype1 = LxmObject()
        val prototype1Ref = memory.add(prototype1)

        val obj1 = LxmObject(prototype1Ref, memory)
        val result1 = obj1.getPrototype(memory)
        Assertions.assertEquals(prototype1, result1, "The result is incorrect")

        val obj2 = LxmObject()
        val result2 = obj2.getPrototype(memory)
        val context = AnalyzerCommons.getCurrentContext(memory)
        val objectType = context.getDereferencedProperty<LxmObject>(memory, ObjectType.TypeName)!!
        val prototype = objectType.getDereferencedProperty<LxmObject>(memory, AnalyzerCommons.Identifiers.Prototype)!!
        Assertions.assertEquals(prototype, result2, "The result is incorrect")
    }

    @Test
    @Incorrect
    fun `set property in a constant object`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObject) {
            val analyzer = LexemAnalyzer(ParserNode.Companion.EmptyParserNode)
            val obj = LxmObject()

            obj.makeConstant(analyzer.memory)

            val testName = "test"
            val testValue = LxmLogic.True

            obj.setProperty(analyzer.memory, testName, testValue)
        }
    }

    @Test
    @Incorrect
    fun `set a constant property`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObjectProperty) {
            val analyzer = LexemAnalyzer(ParserNode.Companion.EmptyParserNode)
            val obj = LxmObject()

            val testName = "test"
            val testValue = LxmLogic.True

            obj.setProperty(analyzer.memory, testName, testValue, isConstant = true)

            obj.setProperty(analyzer.memory, testName, testValue)
        }
    }

    @Test
    @Incorrect
    fun `remove property in a constant object`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObject) {
            val analyzer = LexemAnalyzer(ParserNode.Companion.EmptyParserNode)
            val obj = LxmObject()

            obj.makeConstant(analyzer.memory)

            val testName = "test"

            obj.removeProperty(analyzer.memory, testName)
        }
    }

    @Test
    @Incorrect
    fun `remove a constant property`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObjectProperty) {
            val analyzer = LexemAnalyzer(ParserNode.Companion.EmptyParserNode)
            val obj = LxmObject()
            val testName = "test"
            val testValue = LxmLogic.True

            obj.setProperty(analyzer.memory, testName, testValue, isConstant = true)

            obj.removeProperty(analyzer.memory, testName)
        }
    }

    @Test
    @Incorrect
    fun `make an undefined property constant`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.UndefinedObjectProperty) {
            val analyzer = LexemAnalyzer(ParserNode.Companion.EmptyParserNode)
            val obj = LxmObject()

            obj.makePropertyConstant(analyzer.memory, "test")
        }
    }

    @Test
    @Incorrect
    fun `make a property constant in a constant object`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantObject) {
            val analyzer = LexemAnalyzer(ParserNode.Companion.EmptyParserNode)
            val obj = LxmObject()

            obj.makeConstant(analyzer.memory)
            obj.makePropertyConstant(analyzer.memory, "test")
        }
    }

    @Test
    fun `check the empty object is constant`() {
        Assertions.assertTrue(LxmObject.Empty.isImmutable, "The LxmObject.Empty must be constant")
    }
}
