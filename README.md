# FastMustache
A bytecode-powered Mustache compiler.

## WIP
This project is still work-in-progress. It is not ready to use and not fully tested yet.

## How does it works?
FastMustache generates Java bytecode from a Mustache template, making it the fastest way to render a template.

**test.mustache** (Your template)
```mustache
Hello {{name}}!
```

**User.java** (Your structure class)
```
public class User {
    public String name;
}
```

**Generated Class**
```java
import com.guichaguri.fastmustache.template.Template;
import yourpackage.User;

public class TemplateTest implements Template<User> {
    @Override
    public String render(User data) {
        return "Hello" + data.name + "!";
    }
}
```

## How do I use it?
#### Compiling
You can compile from a string:
```java
Template<User> template = FastMustache.compile("Hello {{name}}!", User.class);
```

You can also compile from a reader:
```java
Template<User> template = FastMustache.compile(new FileReader("test.mustache"), User.class);
```

#### Rendering
After compiling it, you can render it:
```java
User user = new User("Wilson");
String result = template.render(user);
```