# BPS - BytePackingSerialization!

BPS is a minimalistic java library for easy serialization.

## Features

* Fits almost any programming style!
* Simple and straight to the point
* Offers full control over how objects are serialized

## Include BPS in your project as a Gradle dependency

To include BPS in your java project. Add the following to your `build.gradle`:
```gradle
repositories {
	maven { url 'https://jitpack.io' }
}

dependencies {
	implementation 'com.github.SystemInvecklare:BytePackingSerialization:1.0'
}

```

## Concepts

By *serialization* we mean the process of converting java objects to raw data to be stored in a file or sent over a network.

### Serializers

A `ISerializer<T>` for a class `T` describes a way to serialize and deserialize an object of class `T`. 

### TypeRegistry

A `TypeRegistry` is essentially a dictionary that binds together:
1. A `Class<T>`
2. An id for a class (typically an `int`)
3. A `ISerializer<T>` to facilitate serialization and deserialization of said `Class<T>`

This allows for objects to be serialized "as objects" and not "raw data". This means that references will be kept.

## Examples

### Level 1 - Just serialize some data.
```java
public class BpsExample {

	public static void main(String[] args) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		SerializationContext context = new SerializationContext(baos, new TypeRegistry());
		context.writeString("Hello world!");
		context.writeFloat(3.14f);
		context.writeInt(2023);
		
		StringBuilder stringBuilder = new StringBuilder();
		for(byte b : baos.toByteArray()) {
			if(stringBuilder.length() != 0) {
				stringBuilder.append(",");
			}
			stringBuilder.append(b);
		}
		System.out.println(stringBuilder.toString());
	}
}
```
Outputs:
```
0,12,72,101,108,108,111,32,119,111,114,108,100,33,64,72,-11,-61,-121,-25
```

If we create a byte array containing these bytes we can recover the stored information:
```java
public class BpsExample {

	public static void main(String[] args) throws IOException {
		// Data from output above
		byte[] data = new byte[] {0,12,72,101,108,108,111,32,119,111,114,108,100,33,64,72,-11,-61,-121,-25};
		
		DeserializationContext context = new DeserializationContext(new ByteArrayInputStream(data), new TypeRegistry());
		System.out.println(context.readString());
		System.out.println(context.readFloat());
		System.out.println(context.readInt());
	}
}
```
Outputs:
```
Hello world!
3.14
2023
```

### Level 2 - Serialize simple data

```java
public class BpsExample {
	
	public static void main(String[] args) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        SerializationContext context = new SerializationContext(baos, new TypeRegistry());
        context.write(ComplexNumber.SERIALIZER, new ComplexNumber(1, 3));
        
        StringBuilder stringBuilder = new StringBuilder();
        for(byte b : baos.toByteArray()) {
            if(stringBuilder.length() != 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append(b);
        }
        System.out.println(stringBuilder.toString());
	}
	
	private static class ComplexNumber {
		public static final ISerializer<ComplexNumber> SERIALIZER = new ISerializer<BpsExample.ComplexNumber>() {
			@Override
			public ComplexNumber deserialize(IDeserializationContext context) throws IOException {
				return new ComplexNumber(context.readFloat(), context.readFloat());
			}
			
			@Override
			public void serialize(ISerializationContext context, ComplexNumber object) throws IOException {
				context.writeFloat(object.real);
				context.writeFloat(object.imag);
			}
		};
		
		private final float real;
		private final float imag;
		
		public ComplexNumber(float real, float imag) {
			this.real = real;
			this.imag = imag;
		}

		public float getReal() {
			return real;
		}
		
		public float getImag() {
			return imag;
		}
		
		@Override
		public String toString() {
			return real+" + "+imag+" i";
		}
	}
}
```
Outputs:
```
0,63,-128,0,0,64,64,0,0
```
And if we go the other way, we can deserialize this data to get back our `ComplexNumber`.
```java
public class BpsExample {
	
	public static void main(String[] args) throws IOException {
		// Data from output above
		byte[] data = new byte[] {0,63,-128,0,0,64,64,0,0};
		
		DeserializationContext context = new DeserializationContext(new ByteArrayInputStream(data), new TypeRegistry());
		ComplexNumber complexNumber = context.read(ComplexNumber.SERIALIZER);
		System.out.println("Real part: "+complexNumber.getReal());
		System.out.println("Imaginary part: "+complexNumber.getImag());
		System.out.println(complexNumber);
	}
	
	private static class ComplexNumber {
		public static final ISerializer<ComplexNumber> SERIALIZER = new ISerializer<BpsExample.ComplexNumber>() {
			@Override
			public ComplexNumber deserialize(IDeserializationContext context) throws IOException {
				return new ComplexNumber(context.readFloat(), context.readFloat());
			}
			
			@Override
			public void serialize(ISerializationContext context, ComplexNumber object) throws IOException {
				context.writeFloat(object.real);
				context.writeFloat(object.imag);
			}
		};
		
		private final float real;
		private final float imag;
		
		public ComplexNumber(float real, float imag) {
			this.real = real;
			this.imag = imag;
		}

		public float getReal() {
			return real;
		}
		
		public float getImag() {
			return imag;
		}
		
		@Override
		public String toString() {
			return real+" + "+imag+" i";
		}
	}
}
```
Outputs:
```
Real part: 1.0
Imaginary part: 3.0
1.0 + 3.0 i
```

### Level 3 - More serializers!

```java
public class BpsExample {
	
	public static void main(String[] args) throws IOException {
		List<Ball> myBalls = new ArrayList<Ball>();
		myBalls.add(new Ball("Ball A", 5, new Vector(0, 0), new Vector(0, 0.6f)));
		myBalls.add(new Ball("Ball B", 3, new Vector(1, -2), new Vector(0.5f, -0.1f)));
		myBalls.add(new Ball("Ball C", 1, new Vector(3, 1), new Vector(-1.5f, 0)));
		
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        SerializationContext context = new SerializationContext(baos, new TypeRegistry());
        context.writeList(Ball.SERIALIZER, myBalls);
        
        StringBuilder stringBuilder = new StringBuilder();
        for(byte b : baos.toByteArray()) {
            if(stringBuilder.length() != 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append(b);
        }
        System.out.println(stringBuilder.toString());
	}
	
	private static class Vector {
		public static final ISerializer<Vector> SERIALIZER = new ISerializer<Vector>() {
			@Override
			public Vector deserialize(IDeserializationContext context) throws IOException {
				float x = context.readFloat();
				float y = context.readFloat();
				return new Vector(x, y);
			}
			
			@Override
			public void serialize(ISerializationContext context, Vector object) throws IOException {
				context.writeFloat(object.x);
				context.writeFloat(object.y);
			}
		};
		
		public float x;
		public float y;
		
		public Vector(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}
	
	private static class Ball {
		public static final ISerializer<Ball> SERIALIZER = new ISerializer<BpsExample.Ball>() {
			@Override
			public Ball deserialize(IDeserializationContext context) throws IOException {
				String name = context.readString();
				float radius = context.readFloat();
				Vector position = context.read(Vector.SERIALIZER);
				Vector velocity = context.read(Vector.SERIALIZER);
				return new Ball(name, radius, position, velocity);
			}
			
			@Override
			public void serialize(ISerializationContext context, Ball object) throws IOException {
				context.writeString(object.name);
				context.writeFloat(object.radius);
				context.write(Vector.SERIALIZER, object.position);
				context.write(Vector.SERIALIZER, object.velocity);
			}
		};
		
		private final String name;
		private final float radius;
		private Vector position;
		private Vector velocity;
		
		public Ball(String name, float radius, Vector position, Vector velocity) {
			this.name = name;
			this.radius = radius;
			this.position = position;
			this.velocity = velocity;
		}
	}
}
```
Outputs:
```
3,0,0,6,66,97,108,108,32,65,64,-96,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,63,25,-103,-102,0,0,6,66,97,108,108,32,66,64,64,0,0,0,63,-128,0,0,-64,0,0,0,0,63,0,0,0,-67,-52,-52,-51,0,0,6,66,97,108,108,32,67,63,-128,0,0,0,64,64,0,0,63,-128,0,0,0,-65,-64,0,0,0,0,0,0
```

Deserialization is left as an excercise for the reader. (Hint: `List<Ball> myRecoveredBalls = deserializationContext.readList(Ball.SERIALIZER, new ArrayList<>());`) 

### Level 4 - References

So far all serialization has been "pure data" so to speak. For example,
```java
SerializationContext context = new SerializationContext(baos, new TypeRegistry());
Ball ball = new Ball("The one and only ball", 999, new Vector(0, 0), new Vector(0, 0));
context.write(Ball.SERIALIZER, ball);
context.write(Ball.SERIALIZER, ball);
```
will save the data contained in `ball`, but when deserialized two java objects with the same data will be created.

To save a reference to the **object** we need to use a `TypeRegistry`.
```java
TypeRegistry typeRegistry = new TypeRegistry();
typeRegistry.register(0, Ball.class, Ball.SERIALIZER);
        
SerializationContext context = new SerializationContext(baos, typeRegistry);
Ball ball = new Ball("The one and only ball", 999, new Vector(0, 0), new Vector(0, 0));
context.writeObject(ball);
context.writeObject(ball);
```
Now two references to the same ball will be stored (including the data of the ball itself).

To deserialize this we use `IDeserializationContext::readObject`. Note however that this does not return a `Ball` object immediately.
This is because it is impossible to guarantee *when* during the deserialization process an object is fully deserialized. Because of this,
a `Consumer<Ball>` can be supplied that will receive the object as soon as it is fully deserialized.
```java
TypeRegistry typeRegistry = new TypeRegistry();
typeRegistry.register(0, Ball.class, Ball.SERIALIZER);
        
byte[] data = new byte[] {0,0,0,0,0,21,84,104,101,32,111,110,101,32,97,110,100,32,111,110,108,121,32,98,97,108,108,68,121,-64,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
IDeserializationContext deserializationContext = new DeserializationContext(new ByteArrayInputStream(data), typeRegistry);
final Ball[] theBalls = new Ball[2];
deserializationContext.readObject(Ball.class, ballA -> theBalls[0] = ballA);
deserializationContext.readObject(Ball.class, ballB -> theBalls[1] = ballB);
Ball ballA = theBalls[0];
Ball ballB = theBalls[1];
System.out.println(ballA.name);
System.out.println(ballB.name);
System.out.println(ballA == ballB ? "It's the same object!" : "DIFFERENT");
```
Outputs:
```
The one and only ball
The one and only ball
It's the same object!
```

Sometimes you need an object that you can pass to other things instead of supplying a `Consumer<T>`. You can then use `deserializationContext.readObject(Ball.class)`.
This returns an `IObjectReference<T>` which can be used to defer the receiving of the object. For example, we can let a `SimplePointer<T>` receive the object:
```java
TypeRegistry typeRegistry = new TypeRegistry();
typeRegistry.register(0, Ball.class, Ball.SERIALIZER);
        
byte[] data = new byte[] {0,0,0,0,0,21,84,104,101,32,111,110,101,32,97,110,100,32,111,110,108,121,32,98,97,108,108,68,121,-64,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
IDeserializationContext deserializationContext = new DeserializationContext(new ByteArrayInputStream(data), typeRegistry);
SimplePointer<Ball> ballA = new SimplePointer<Ball>(deserializationContext.readObject(Ball.class));
SimplePointer<Ball> ballB = new SimplePointer<Ball>(deserializationContext.readObject(Ball.class));
        
System.out.println(ballA.get().name);
System.out.println(ballB.get().name);
System.out.println(ballA.get() == ballB.get() ? "It's the same object!" : "DIFFERENT");
```
Outputs:
```
The one and only ball
The one and only ball
It's the same object!
```

### Level 5 - Polymorphism

```java
public class BpsExample {
	
	public static void main(String[] args) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        TypeRegistry typeRegistry = new TypeRegistry();
        typeRegistry.register(0, Dog.class, Dog.SERIALIZER);
        typeRegistry.register(1, Cat.class, Cat.SERIALIZER);
        typeRegistry.register(2, AnimalOwner.class, AnimalOwner.SERIALIZER);
        
        AnimalOwner animalOwner = new AnimalOwner("Natalie");
        Dog kiara = new Dog();
        Cat tiffany = new Cat(false);
        animalOwner.animals.add(kiara);
        animalOwner.animals.add(tiffany);
        animalOwner.faveAnimal = kiara;
        
        SerializationContext context = new SerializationContext(baos, typeRegistry);
        context.writeObject(animalOwner);
        
        StringBuilder stringBuilder = new StringBuilder();
        for(byte b : baos.toByteArray()) {
            if(stringBuilder.length() != 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append(b);
        }
        System.out.println(stringBuilder.toString());
	}
	
	private static class AnimalOwner {
		public static final ISerializer<AnimalOwner> SERIALIZER = new ISerializer<BpsExample.AnimalOwner>() {
			@Override
			public AnimalOwner deserialize(IDeserializationContext context) throws IOException {
				AnimalOwner object = new AnimalOwner(context.readString());
				context.readObject(IAnimal.class, v -> object.faveAnimal = v);
				context.readObjectList(IAnimal.class, object.animals);
				return object;
			}
			
			@Override
			public void serialize(ISerializationContext context, AnimalOwner object) throws IOException {
				context.writeString(object.name);
				context.writeObject(object.faveAnimal);
				context.writeObjectList(object.animals);
			}
		};
		
		private final String name;
		private IAnimal faveAnimal;
		private final List<IAnimal> animals = new ArrayList<IAnimal>();
		
		public AnimalOwner(String name) {
			this.name = name;
		}
	}
	
	private interface IAnimal {
		String getSound();
	}
	
	private static class Dog implements IAnimal {
		public static final ISerializer<Dog> SERIALIZER = new ISerializer<BpsExample.Dog>() {
			@Override
			public Dog deserialize(IDeserializationContext context) throws IOException {
				return new Dog();
			}
			
			@Override
			public void serialize(ISerializationContext context, Dog object) throws IOException {
			}
		};
		
		@Override
		public String getSound() {
			return "WOOF!";
		}
	}
	
	private static class Cat implements IAnimal {
		public static final ISerializer<Cat> SERIALIZER = new ISerializer<BpsExample.Cat>() {
			@Override
			public Cat deserialize(IDeserializationContext context) throws IOException {
				return new Cat(context.readBoolean());
			}
			
			@Override
			public void serialize(ISerializationContext context, Cat object) throws IOException {
				context.writeBoolean(object.japanese);
			}
		};
		
		private final boolean japanese;
		
		public Cat(boolean japanese) {
			this.japanese = japanese;
		}
		
		@Override
		public String getSound() {
			return japanese ? "Nyan" : "Meow";
		}
	}
}
```
Outputs:
```
0,0,2,0,0,7,78,97,116,97,108,105,101,1,0,0,0,2,1,2,0,1,0,0
```

Let's deserialize the data!
```java
public class BpsExample {
	
	public static void main(String[] args) throws IOException {
        TypeRegistry typeRegistry = new TypeRegistry();
        typeRegistry.register(0, Dog.class, Dog.SERIALIZER);
        typeRegistry.register(1, Cat.class, Cat.SERIALIZER);
        typeRegistry.register(2, AnimalOwner.class, AnimalOwner.SERIALIZER);
        
        byte[] data = new byte[] {0,0,2,0,0,7,78,97,116,97,108,105,101,1,0,0,0,2,1,2,0,1,0,0};
        DeserializationContext context = new DeserializationContext(new ByteArrayInputStream(data), typeRegistry);
        
        context.readObject(AnimalOwner.class, animalOwner -> {
        	System.out.println("My name is " + animalOwner.name + ", and these are the sounds my animals make:");
        	for(IAnimal animal : animalOwner.animals) {
        		System.out.println(animal.getSound() + (animal == animalOwner.faveAnimal ? " <-- (my favorite!)" : ""));
        	}
        });
	}
	
	private static class AnimalOwner {
		public static final ISerializer<AnimalOwner> SERIALIZER = new ISerializer<BpsExample.AnimalOwner>() {
			@Override
			public AnimalOwner deserialize(IDeserializationContext context) throws IOException {
				AnimalOwner object = new AnimalOwner(context.readString());
				context.readObject(IAnimal.class, v -> object.faveAnimal = v);
				context.readObjectList(IAnimal.class, object.animals);
				return object;
			}
			
			@Override
			public void serialize(ISerializationContext context, AnimalOwner object) throws IOException {
				context.writeString(object.name);
				context.writeObject(object.faveAnimal);
				context.writeObjectList(object.animals);
			}
		};
		
		private final String name;
		private IAnimal faveAnimal;
		private final List<IAnimal> animals = new ArrayList<IAnimal>();
		
		public AnimalOwner(String name) {
			this.name = name;
		}
	}
	
	private interface IAnimal {
		String getSound();
	}
	
	private static class Dog implements IAnimal {
		public static final ISerializer<Dog> SERIALIZER = new ISerializer<BpsExample.Dog>() {
			@Override
			public Dog deserialize(IDeserializationContext context) throws IOException {
				return new Dog();
			}
			
			@Override
			public void serialize(ISerializationContext context, Dog object) throws IOException {
			}
		};
		
		@Override
		public String getSound() {
			return "WOOF!";
		}
	}
	
	private static class Cat implements IAnimal {
		public static final ISerializer<Cat> SERIALIZER = new ISerializer<BpsExample.Cat>() {
			@Override
			public Cat deserialize(IDeserializationContext context) throws IOException {
				return new Cat(context.readBoolean());
			}
			
			@Override
			public void serialize(ISerializationContext context, Cat object) throws IOException {
				context.writeBoolean(object.japanese);
			}
		};
		
		private final boolean japanese;
		
		public Cat(boolean japanese) {
			this.japanese = japanese;
		}
		
		@Override
		public String getSound() {
			return japanese ? "Nyan" : "Meow";
		}
	}
}
```
Outputs:
```
My name is Natalie, and these are the sounds my animals make:
WOOF! <-- (my favorite!)
Meow
```

### Level 6 - Updating serializers while keeping backwards compatibility

//TODO

## Cookbook

### You want to register an private inner class but you don't want to expose it.
//TODO