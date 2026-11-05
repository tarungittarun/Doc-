// ============================================================
// HelloWorld.java — Java example with common mistakes
// ============================================================

// MISTAKE 1: Missing package declaration (optional but good practice)
// MISTAKE 2: Wrong class name — must match filename exactly (HelloWorld, not helloworld)
public class HelloWorld {

    // MISTAKE 3: Wrong main method signature
    // public static void main(string[] args)  <- 'string' should be 'String' (case-sensitive)
    // public void main(String[] args)          <- missing 'static'
    // static void main(String[] args)          <- missing 'public'
    // public static void main()                <- missing String[] args parameter

    // CORRECT main method signature:
    public static void main(String[] args) {

        // MISTAKE 4: Missing semicolon at end of statement
        // System.out.println("Hello World")    <- compiles? No — missing ';'

        // CORRECT:
        System.out.println("Hello World");

        // MISTAKE 5: Wrong capitalization in System.out
        // system.out.println("Hello World")    <- 'system' should be 'System'
        // System.Out.println("Hello World")    <- 'Out' should be 'out'

        // MISTAKE 6: Missing quotes around string
        // System.out.println(Hello World)      <- should be "Hello World"

        // MISTAKE 7: Missing closing brace for class or method
    }

    // MISTAKE 8: Extra method outside class (won't compile)
    // public static void anotherMethod() { }
}

// ============================================================
// HOW TO COMPILE AND RUN:
// ============================================================
// 1. Save this file as: HelloWorld.java  (filename must match class name)
// 2. Open terminal/command prompt
// 3. Compile:   javac HelloWorld.java
// 4. Run:       java HelloWorld
// 5. Output:    Hello World
//
// COMMON ERRORS YOU MIGHT SEE:
// - "error: class HelloWorld is public, should be declared in a file named HelloWorld.java"
//   → Rename the file to match the class name exactly
// - "error: cannot find symbol" → Usually a typo (String vs string, System vs system)
// - "error: ';' expected" → Missing semicolon
// - "Exception in thread "main" java.lang.NoSuchMethodError: main"
//   → main method signature is wrong
// ============================================================
