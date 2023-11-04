package net.pointlessgames.libs.bps;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

import net.pointlessgames.libs.bps.nested.IInnerType;
import net.pointlessgames.libs.bps.nested.IOuterType;


public class RoundtripTest {
	@Test
	public void testNonNullObject() throws IOException {
		TypeRegistry registry = new TypeRegistry();
		registry.register(0, SimpleType.class, SimpleType.SERIALIZER);
		
		List<SimpleType> returned = roundtripObjectList(closure(() -> {
			List<SimpleType> original = new ArrayList<RoundtripTest.SimpleType>();
			original.add(new SimpleType(0, 1f, -1f));
			original.add(new SimpleType(5, 0.5f, 0.75f));
			original.add(new SimpleType(100, 100f, 100.25f));
			return original;
		}), registry);
		
		Assert.assertEquals(3, returned.size());
		SimpleType.assertEquals(new SimpleType(0, 1f, -1f), returned.get(0));
		SimpleType.assertEquals(new SimpleType(5, 0.5f, 0.75f), returned.get(1));
		SimpleType.assertEquals(new SimpleType(100, 100f, 100.25f), returned.get(2));
		
		expectException(() -> {
			roundtripObjectList(closure(() -> {
				List<SimpleType> original = new ArrayList<RoundtripTest.SimpleType>();
				original.add(new SimpleType(0, 1f, -1f));
				original.add(null);
				original.add(new SimpleType(100, 100f, 100.25f));
				return original;
			}), registry);
		}, IllegalArgumentException.class);
	}
	
	@Test
	public void testWithNullObject() throws IOException {
		TypeRegistry registry = new TypeRegistry();
		registry.register(0, SimpleType.class, SimpleType.SERIALIZER);
		registry.registerNull(1);
		
		List<SimpleType> returned = roundtripObjectList(closure(() -> {
			List<SimpleType> original = new ArrayList<RoundtripTest.SimpleType>();
			SimpleType simpleType = new SimpleType(5, 0.5f, 0.75f);
			original.add(new SimpleType(0, 1f, -1f));
			original.add(simpleType);
			original.add(null);
			original.add(null);
			original.add(new SimpleType(100, 100f, 100.25f));
			original.add(simpleType);
			original.add(null);
			original.add(simpleType);
			original.add(new SimpleType(50, 10.125f, 0f));
			return original;
		}), registry);
		
		Assert.assertEquals(9, returned.size());
		SimpleType.assertEquals(new SimpleType(0, 1f, -1f), returned.get(0));
		SimpleType.assertEquals(new SimpleType(5, 0.5f, 0.75f), returned.get(1));
		Assert.assertNull(returned.get(2));
		Assert.assertNull(returned.get(3));
		SimpleType.assertEquals(new SimpleType(100, 100f, 100.25f), returned.get(4));
		Assert.assertSame(returned.get(1), returned.get(5));
		Assert.assertNull(returned.get(6));
		Assert.assertSame(returned.get(1), returned.get(7));
		SimpleType.assertEquals(new SimpleType(50, 10.125f, 0f), returned.get(8));
	}
	
	@Test
	public void testCircularDependencies() throws IOException {
		TypeRegistry registry = new TypeRegistry();
		registry.register(0, Node.class, Node.SERIALIZER);
		
		Node returned = roundtripObject(closure(() -> {
			Node a = new Node("a");
			Node b = new Node("b");
			Node c = new Node("c");
			Node d = new Node("d");
			Node e = new Node("e");
			Node f = new Node("f");
			Node g = new Node("g");
			Node h = new Node("h");
			Node i = new Node("i");
			a.children.add(b);
			a.children.add(c);
			a.children.add(f);
			
			b.children.add(c);
			b.children.add(d);
			
			c.children.add(d);
			c.children.add(b);
			
			d.children.add(g);
			d.children.add(e);
			d.children.add(h);
			
			e.children.add(f);
			e.children.add(d);
			e.children.add(a);
			e.children.add(i);
			
			f.children.add(i);
			f.children.add(g);
			
			g.children.add(e);
			g.children.add(h);
			
			h.children.add(i);
			
			i.children.add(g);
			
			return a;
		}), registry);
		
		class NodeVisitor {
			private final Set<Node> visitedNodes = new HashSet<>();
			public void visit(Node node) {
				Assert.assertNotNull(node);
				if(!visitedNodes.contains(node)) {
					visitedNodes.add(node);
					for(Node child : node.children) {
						visit(child);
					}
				}
			}
		}
		
		Assert.assertEquals("a", returned.data);
		Assert.assertEquals(3, returned.children.size());
		
		NodeVisitor nodeVisitor = new NodeVisitor();
		nodeVisitor.visit(returned);
		
		Assert.assertEquals(9, nodeVisitor.visitedNodes.size());
		List<Node> sortedNodes = new ArrayList<>(nodeVisitor.visitedNodes);
		Collections.sort(sortedNodes, (n1, n2) -> {
			return n1.data.compareTo(n2.data);
		});
		Assert.assertEquals("a", sortedNodes.get(0).data);
		Assert.assertEquals("b", sortedNodes.get(1).data);
		Assert.assertEquals("c", sortedNodes.get(2).data);
		Assert.assertEquals("d", sortedNodes.get(3).data);
		Assert.assertEquals("e", sortedNodes.get(4).data);
		Assert.assertEquals("f", sortedNodes.get(5).data);
		Assert.assertEquals("g", sortedNodes.get(6).data);
		Assert.assertEquals("h", sortedNodes.get(7).data);
		Assert.assertEquals("i", sortedNodes.get(8).data);
		
		Assert.assertEquals(3, sortedNodes.get(0).children.size());
		Assert.assertEquals(2, sortedNodes.get(1).children.size());
		Assert.assertEquals(2, sortedNodes.get(2).children.size());
		Assert.assertEquals(3, sortedNodes.get(3).children.size());
		Assert.assertEquals(4, sortedNodes.get(4).children.size());
		Assert.assertEquals(2, sortedNodes.get(5).children.size());
		Assert.assertEquals(2, sortedNodes.get(6).children.size());
		Assert.assertEquals(1, sortedNodes.get(7).children.size());
		Assert.assertEquals(1, sortedNodes.get(8).children.size());
		
		Assert.assertEquals("g", sortedNodes.get(3).children
				.get(1).children
				.get(2).children
				.get(2).children
				.get(0).children
				.get(0).data);
	}
	
	@Test
	public void testComplexWithInner() throws IOException {
		TypeRegistry registry = new TypeRegistry();
		registry.register(0, Graph.class, Graph.SERIALIZER);
		registry.registerInnerType(1);
		
		List<Object> returned = roundtripObjectList(closure(() -> {
			Graph eats = new Graph();
			GraphNode matte = eats.createNode("matte", null);
			GraphNode cow = eats.createNode("cow", null);
			GraphNode lion = eats.createNode("lion", null);
			GraphNode bear = eats.createNode("bear", null);
			GraphNode salmon = eats.createNode("salmon", null);
			GraphNode grass = eats.createNode("grass", null);
			GraphNode monkey = eats.createNode("monkey", null);
			GraphNode pig = eats.createNode("pig", null);
			GraphNode apple = eats.createNode("apple", null);
			matte.children.add(cow);
			matte.children.add(bear);
			matte.children.add(salmon);
			matte.children.add(pig);
			matte.children.add(apple);
			cow.children.add(grass);
			lion.children.add(matte);
			lion.children.add(cow);
			lion.children.add(monkey);
			lion.children.add(pig);
			bear.children.add(matte);
			bear.children.add(salmon);
			bear.children.add(monkey);
			bear.children.add(pig);
			bear.children.add(apple);
			monkey.children.add(apple);
			pig.children.add(apple);
			
			Graph graph2 = new Graph();
			List<GraphNode> allGraph2Nodes = new ArrayList<>();
			allGraph2Nodes.add(graph2.createNode("A", null));
			allGraph2Nodes.add(graph2.createNode("B", null));
			allGraph2Nodes.add(graph2.createNode("C", null));
			allGraph2Nodes.add(graph2.createNode("D", null));
			allGraph2Nodes.add(graph2.createNode("E", null));
			for(GraphNode n1 : allGraph2Nodes) {
				for(GraphNode n2 : allGraph2Nodes) {
					n1.children.add(n2);
				}
			}
			
			return Arrays.asList(matte, eats, graph2, allGraph2Nodes.get(2));
		}), registry);
		
		Assert.assertEquals(4, returned.size());
		
		class NodeVisitor {
			private final Graph graph;
			private final Set<GraphNode> visitedNodes = new HashSet<>();
			
			public NodeVisitor(Graph graph) {
				this.graph = graph;
			}
			
			public void visitNodes() {
				for(GraphNode node : graph.nodes) {
					visit(node);
				}
			}

			public void visit(GraphNode node) {
				Assert.assertNotNull(node);
				Assert.assertSame(graph, node.graph);
				if(!visitedNodes.contains(node)) {
					visitedNodes.add(node);
					for(GraphNode child : node.children) {
						visit(child);
					}
				}
			}
		}
		
		NodeVisitor eatVisitor = new NodeVisitor((Graph) returned.get(1));
		eatVisitor.visitNodes();
		Assert.assertEquals(9, eatVisitor.visitedNodes.size());
		Assert.assertTrue(eatVisitor.graph.nodes.contains(returned.get(0)));
		Assert.assertFalse(eatVisitor.graph.nodes.contains(returned.get(3)));
		
		NodeVisitor fullGraphVisitor = new NodeVisitor((Graph) returned.get(2));
		fullGraphVisitor.visitNodes();
		Assert.assertEquals(5, fullGraphVisitor.visitedNodes.size());
		Assert.assertTrue(fullGraphVisitor.graph.nodes.contains(returned.get(3)));
	}
	
	@Test
	public void testCrossWithInners() throws IOException {
		TypeRegistry registry = new TypeRegistry();
		registry.registerInnerType(0);
		registry.register(1, Human.class, Human.SERIALIZER);
		registry.register(2, Book.class, Book.SERIALIZER);
		
		List<Human> returned = roundtripObjectList(closure(() -> {
			Human matte = new Human("Matte");
			Human natalie = new Human("Natalie");
			matte.grabWithRightArm(natalie.getLeftArm());
			natalie.grabWithLeftArm(matte.getRightArm());
			natalie.grabWithRightArm(new Book("Color of magic"));
			Human weirdo = new Human("weirdo");
			weirdo.grabWithLeftArm(matte.getRightArm());
			weirdo.grabWithRightArm(natalie.getLeftArm());
			Human weirdo2 = new Human("weirdo2");
			weirdo2.grabWithLeftArm(weirdo.getLeftArm());
			weirdo2.grabWithRightArm(weirdo2.getLeftArm());
			Human weirdo3 = new Human("weirdo3");
			weirdo3.grabWithRightArm(weirdo2.getLeftArm());
			weirdo3.grabWithLeftArm(new Book("Demonomicon"));
			matte.grabWithLeftArm(weirdo3.getLeftArm());
			
			return Arrays.asList(matte, natalie);
		}), registry);
		
		Assert.assertEquals(2, returned.size());
		Human matte = returned.get(0);
		Human natalie = returned.get(1);
		Assert.assertEquals("weirdo3's left arm", matte.leftArm.getHeld().get().describe());
		Human weirdo3 = (Human) ((Human.Arm) matte.leftArm.getHeld().get()).getOuterObject();
		Assert.assertEquals("A book titled Color of magic", natalie.rightArm.getHeld().get().describe());
		Assert.assertEquals("A book titled Demonomicon", weirdo3.leftArm.getHeld().get().describe());
		Human weirdo2 = (Human) ((Human.Arm) weirdo3.rightArm.getHeld().get()).getOuterObject();
		Assert.assertEquals("weirdo2's left arm", weirdo2.rightArm.getHeld().get().describe());
		Assert.assertEquals("weirdo's left arm", weirdo2.leftArm.getHeld().get().describe());
		Human weirdo = (Human) ((Human.Arm) weirdo2.leftArm.getHeld().get()).getOuterObject();
		Assert.assertEquals("Matte's right arm", weirdo.leftArm.getHeld().get().describe());
		Assert.assertEquals("Natalie's left arm", weirdo.rightArm.getHeld().get().describe());
	}
	
	private static <T> T closure(Supplier<T> supplier) {
		return supplier.get();
	}
	
	private static <T extends Throwable> void expectException(UnsafeRunnable<T> runnable, Class<? extends Throwable> expected) {
		try {
			runnable.run();
		} catch (Throwable e) {
			if(expected.isInstance(e)) {
				return;
			}
			if(e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		}
		throw new AssertionError("Expected "+expected.getSimpleName()+" to be thrown, but it wasn't");
	}
	
	@FunctionalInterface
	public interface UnsafeRunnable<E extends Throwable> {
		void run() throws E;
	}
	
	@SuppressWarnings("unchecked")
	private static <T> List<T> roundtripObjectList(List<T> list, ISerializer<Object> objectSerializer) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new SerializationContext(baos, objectSerializer).writeObjectList(list);
		return (List<T>) new DeserializationContext(new ByteArrayInputStream(baos.toByteArray()), objectSerializer).readObjectList(Object.class, new ArrayList<>());
	}
	
	@SuppressWarnings({"unchecked" })
	private static <T> T roundtripObject(T object, ISerializer<Object> objectSerializer) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new SerializationContext(baos, objectSerializer).writeObject(object);
		return (T) new SimplePointer<>(new DeserializationContext(new ByteArrayInputStream(baos.toByteArray()), objectSerializer).readObject(Object.class)).get();
	}
	
	private static class Node {
		public static final ISerializer<Node> SERIALIZER = new ISerializer<Node>() {
			@Override
			public Node deserialize(IDeserializationContext context) throws IOException {
				Node object = new Node(context.readString());
				context.readObjectList(Node.class, object.children);
				return object;
			}
			
			@Override
			public void serialize(ISerializationContext context, Node object) throws IOException {
				context.writeString(object.data);
				context.writeObjectList(object.children);
			}
		}; 
		private String data;
		private final List<Node> children = new ArrayList<>();
		
		public Node(String data) {
			this.data = data;
		}
	}
	
	private static class Graph implements IOuterType {
		public static final ISerializer<Graph> SERIALIZER = new ISerializer<RoundtripTest.Graph>() {
			@Override
			public Graph deserialize(IDeserializationContext context) throws IOException {
				Graph object = new Graph();
				context.readObjectList(GraphNode.class, object.nodes);
				return object;
			}
			
			@Override
			public void serialize(ISerializationContext context, Graph object) throws IOException {
				context.writeObjectList(object.nodes);
			}
		};
		private final List<GraphNode> nodes = new ArrayList<>();
		
		public GraphNode createNode(String data, BiConsumer<Graph, GraphNode> builder) {
			GraphNode node = new GraphNode(this, data);
			nodes.add(node);
			if(builder != null) {
				builder.accept(this, node);
			}
			return node;
		}

		@Override
		public void serializeInner(ISerializationContext context, IInnerType inner) throws IOException {
			GraphNode graphNode = (GraphNode) inner;
			context.writeString(graphNode.data);
			context.writeObjectList(graphNode.children);
		}

		@Override
		public IInnerType deserializeInner(IDeserializationContext context) throws IOException {
			GraphNode graphNode = new GraphNode(this, context.readString());
			context.readObjectList(GraphNode.class, graphNode.children);
			return graphNode;
		}
	}
	
	private static class GraphNode implements IInnerType {
		private final Graph graph;
		
		private String data;
		private final List<GraphNode> children = new ArrayList<>();
		
		public GraphNode(Graph graph, String data) {
			this.graph = graph;
			this.data = data;
		}

		@Override
		public IOuterType getOuterObject() {
			return graph;
		}
	}
	
	private static class SimpleType {
		public static final ISerializer<SimpleType> SERIALIZER = new ISerializer<RoundtripTest.SimpleType>() {
			@Override
			public SimpleType deserialize(IDeserializationContext context) throws IOException {
				return new SimpleType(context.readInt(), context.readFloat(), context.readFloat());
			}
			
			@Override
			public void serialize(ISerializationContext context, SimpleType object) throws IOException {
				context.writeInt(object.i);
				context.writeFloat(object.x);
				context.writeFloat(object.y);
			}
		};
		
		private final int i;
		private final float x;
		private final float y;
		
		public SimpleType(int i, float x, float y) {
			this.i = i;
			this.x = x;
			this.y = y;
		}

		public static void assertEquals(SimpleType expected, SimpleType actual) {
			Assert.assertEquals(expected.i, actual.i);
			Assert.assertEquals(expected.x, actual.x, 0f);
			Assert.assertEquals(expected.y, actual.y, 0f);
		}
	}
	
	private interface IHoldable {
		String describe();
	}
	
	private interface IHolder {
		Optional<IHoldable> getHeld();
	}
	
	private static class Book implements IHoldable {
		private static final ISerializer<Book> SERIALIZER =  Serializers.map(Serializers.STRING, Book::new, o -> o.title);
		
		private final String title;

		public Book(String title) {
			this.title = title;
		}

		@Override
		public String describe() {
			return "A book titled "+title;
		}
	}
	
	private static class Human implements IOuterType {
		private static final ISerializer<Human> SERIALIZER = new ISerializer<RoundtripTest.Human>() {
			@Override
			public Human deserialize(IDeserializationContext context) throws IOException {
				Human object = new Human(context.readString(), null, null);
				context.readObject(Arm.class, v -> object.leftArm = v);
				context.readObject(Arm.class, v -> object.rightArm = v);
				return object;
			}
			
			@Override
			public void serialize(ISerializationContext context, Human object) throws IOException {
				context.writeString(object.name);
				context.writeObject(object.leftArm);
				context.writeObject(object.rightArm);
			}
		};
		private final String name;
		private Arm leftArm;
		private Arm rightArm;
		
		private Human(String name, Arm leftArm, Arm rightArm) {
			this.name = name;
			this.leftArm = leftArm;
			this.rightArm = rightArm;
		}

		public Human(String name) {
			this.name = name;
			this.leftArm = new Arm("left");
			this.rightArm = new Arm("right");
		}
		
		public void grabWithLeftArm(IHoldable holdable) {
			leftArm.held = Optional.of(holdable);
		}
		
		public void grabWithRightArm(IHoldable holdable) {
			rightArm.held = Optional.of(holdable);
		}
		
		public IHoldable getLeftArm() {
			return leftArm;
		}
		
		public IHoldable getRightArm() {
			return rightArm;
		}
		
		@Override
		public void serializeInner(ISerializationContext context, IInnerType inner) throws IOException {
			Arm arm = (Arm) inner;
			context.writeString(arm.which);
			context.writeOptionalObject(arm.held);
		}
		
		@Override
		public IInnerType deserializeInner(IDeserializationContext context) throws IOException {
			Arm arm = new Arm(context.readString());
			context.readOptionalObject(IHoldable.class, v -> arm.held = v);
			return arm;
		}

		private class Arm implements IHolder, IHoldable, IInnerType {
			private final String which;
			private Optional<IHoldable> held = Optional.empty();
			
			public Arm(String which) {
				this.which = which;
			}

			@Override
			public String describe() {
				return Human.this.name+"'s "+which+" arm";
			}

			@Override
			public Optional<IHoldable> getHeld() {
				return held;
			}

			@Override
			public IOuterType getOuterObject() {
				return Human.this;
			}
		}
	}
}
