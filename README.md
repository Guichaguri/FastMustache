# FastMustache
An experimental bytecode-powered Mustache compiler.

## WIP
This project is still work-in-progress. It is not ready to use and not fully tested yet.

## What makes it so fast?

There is no overhead at all. There is no reflection, no method handles, no dynamic interpretation, no token iteration.
It's a compiler (pretty much like javac or GCC) that transforms Mustache templates into Java classes.

Mustache variables become fields or getters, Mustache list sections become for loops, Mustache boolean sections become if conditions and so on.

## How does it works?
FastMustache generates Java bytecode from a Mustache template, making it the fastest way to render a template.

**test.mustache** (Your template)
```mustache
Hello {{name}}!!
```

**User.java** (Your POJO class)
```java
public class User {
    public String name;
}
```

**Generated Class** (Decompiled Class, in code form)
```java
import com.guichaguri.fastmustache.template.Template;
import yourpackage.User;

public class TemplateTest implements Template<User> {
    @Override
    public String render(User data) {
        return "Hello " + data.name + "!!";
    }
}
```

Note: The actual class would be generated directly in Java bytecode instructions, but for demonstration purposes the section above shows the equivalent Java code.

## How do I use it?
#### Compiling
You can compile from a string:
```java
Template<User> template = new FastMustache("Hello {{name}}").compile(User.class);
```

You can also compile from a File, a Reader or an InputStream:
```java
Template<User> template = new FastMustache(new File("test.mustache")).compile(User.class);
```

#### Rendering
After compiling it, you can render it:
```java
User user = new User();
user.name = "Rick";
String result = template.render(user);
// Hello Rick!!
```

## Lambdas

The Mustache spec require the compiler, the original template source and the data to be present for lambdas.

Recompiling the section template would be too slow and against the performance goals of the project.

Because of that, I decided to implement two replacements for the original lambdas:

#### Render-time lambdas
Allows you to change the rendered output.
You'll receive the `StringBuilder`, the render function and the data object.

* Allows you to add custom logic before rendering the section
* Allows you to render the section as many times as you want
* Allows you to change the data object before rendering
* Allows you to change the rendered output by using a secondary `StringBuilder`

**Wrapping the rendered output in bold**
```java
MustacheLambda<User> lambda = (builder, section, data) -> {
    builder.append("<b>");
    section.render(builder, data);
    builder.append("</b>");
};

// Template: {{#lambda}}Hello {{name}}{{/lambda}}
// Output: <b>Hello Rick</b>
```

**Swapping the user object**
```java
MustacheLambda<User> lambda = (builder, section, data) -> {
    User customUser = new User();
    customUser.name = "Foo";
    section.render(builder, customUser);
};

// Template: {{#lambda}}Hello {{name}}{{/lambda}}
// Output: Hello Foo
```

**Replacing the rendered output**
```java
MustacheLambda<User> lambda = (builder, section, data) -> {
    StringBuilder lambdaBuilder = new StringBuilder();
    section.render(lambdaBuilder, customUser);
    builder.append(lambdaBuilder.toString().replaceAll("Hello", "Hi"));
};

// Template: {{#lambda}}Hello {{name}}{{/lambda}}
// Output: Hi Rick
```

#### Section transformers
Allows you to change the section tokens in compile-time.
You'll receive the tokens inside the section before they are compiled into bytecode.

* Allows you to insert new tokens
* Allows you to change existing tokens
* Allows you to remove tokens
* Can also transform the whole template, and not only specific sections

**Wrapping the section in bold**
```java
SectionTransformer transformer = (tokens) -> {
    tokens.add(0, new TextToken("<b>"));
    tokens.add(new TextToken("</b>"));
};

// Template: {{#lambda}}Hello {{name}}{{/lambda}}
// Output: <b>Hello Rick</b>
```

**Replacing constant variables with raw text in compile-time**
```java
SectionTransformer transformer = (tokens) -> {
    for(int i = 0; i < tokens.size(); i++) {
        MustacheToken token = tokens.get(i);
        
        if (token instanceof VariableToken && ((VariableToken) token).variable.equals("version")) {
            tokens.set(i, new TextToken("1.0"));
        }
    }
};

// Template: {{#lambda}}Version {{version}}{{/lambda}}
// Output: Version 1.0
```
