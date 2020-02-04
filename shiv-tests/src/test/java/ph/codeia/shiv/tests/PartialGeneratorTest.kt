package ph.codeia.shiv.tests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import ph.codeia.shiv.LateBound
import java.lang.reflect.Modifier


/*
 * This file is a part of the Shiv project.
 */

class PartialGeneratorTest {
	@Test
	fun `it works with top level classes`() {
		val foo = PartialFoo { "foo" }.bind(123)
		assertEquals("foo", foo.str)
		assertEquals(123, foo.num)
	}

	@Test
	fun `it works with nested classes`() {
		// TODO: better name for generated class
		val bar = PartialBar().bind(true)
		assert(bar.bool)
	}

	@Test
	fun `it works with multiple late-bound params`() {
		val baz = PartialBaz { "baz" }.bind(32, true)
		assertEquals("baz", baz.str)
		assertEquals(32, baz.num)
		assert(baz.bool)
	}

	@Test
	fun `it works with alternating late-bound and injectable params`() {
		val xs = doubleArrayOf(1.0, 2.0, 3.0)
		val lorem = PartialLorem({ "lorem ipsum" }, { xs }).bind(1024, true)
		assertEquals("lorem ipsum", lorem.s)
		assertEquals(1024, lorem.n)
		assert(lorem.b)
		assertSame(xs, lorem.xs)
	}

	@Test
	fun `has the same visibility as target constructor`() {
		val ipsum: Ipsum = PartialIpsum().bind("ipsum")
		assertEquals("ipsum", ipsum.s)
		assert(Modifier.isProtected(PartialIpsum::class.java.declaredConstructors[0].modifiers))
	}
}

class Foo(val str: String, @LateBound val num: Int) {
	class Bar(@LateBound val bool: Boolean)
}

class Baz(val str: String, @LateBound val num: Int, @LateBound val bool: Boolean)

class Lorem(@LateBound val n: Int, val s: String, @LateBound val b: Boolean, val xs: DoubleArray)

class Ipsum protected constructor(@LateBound val s: String)