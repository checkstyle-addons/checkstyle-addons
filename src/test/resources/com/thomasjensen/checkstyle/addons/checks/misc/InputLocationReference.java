package com.foo;

import java.util.Comparator;




// @formatter:off
public final class InputLocationReference
{
    private void methodName()   // location = method
    {
        final String checkedVar1 = "methodName";
        final String uncheckedVar1 = "not_checked";
        final Class<?> checkedVar1a = String.class;    // not checked
        checkedCall1("methodName");
        checkedCall1(String.class);  // not checked
    }

    private void classObject()   // location = classobject
    {
        final String checkedVar2 = "foo";    // not checked because of type String
        final String uncheckedVar2 = "bar";
        final Class<?> checkedVar2a = InputLocationReference.class;
        final Class<?> checkedVar2b = String.class;   // REPORT THIS
        checkedCall2("not_checked");
        checkedCall2(InputLocationReference.class);
        checkedCall2(com.foo.InputLocationReference.class);   // REPORT THIS
    }


    private void simpleClass()   // location = simpleclass
    {
        final String checkedVar3 = "InputLocationReference";
        final String uncheckedVar3 = "bar";
        final String checkedVar3a = "error";   // REPORT THIS
        final Class<?> checkedVar3b = InputLocationReference.class;    // not checked because not of type String
        final Class<?> checkedVar3c = String.class;    // not checked because not of type String
        checkedCall3("InputLocationReference");
        checkedCall3a("report_this");   // REPORT THIS
        checkedCall3(InputLocationReference.class);    // not checked because not of type String
        checkedCall3(com.foo.InputLocationReference.class);    // not checked because not of type String
    }


    private void fullClass()   // location = fullclass
    {
        final String checkedVar4 = "com.foo.InputLocationReference";
        final String uncheckedVar4 = "bar";
        final String checkedVar4a = "error";   // REPORT THIS
        final Class<?> checkedVar4b = InputLocationReference.class;    // not checked because not of type String
        final Class<?> checkedVar4c = String.class;    // not checked because not of type String
        checkedCall4("com.foo.InputLocationReference");
        checkedCall4a("report_this");   // REPORT THIS
        checkedCall4(InputLocationReference.class);    // not checked because not of type String
        checkedCall4(com.foo.InputLocationReference.class);    // not checked because not of type String
    }


    public interface Inner1 {

        public void someFoo();

        public static class Inner2 {
            public @interface Inner3 {
                public class Inner4 {

                    public void innerMethod()
                    {
                        final String checkedVar5 = "innerMethod";
                        final String checkedVar5a = "error";  // REPORT THIS
                        checkedCall5("innerMethod");
                        checkedCall5("innerMethod()");  // REPORT THIS
                    }

                    public void innerSimpleClass()
                    {
                        final String checkedVar6 = "Inner4";
                        final String checkedVar6a = "error";  // REPORT THIS
                        checkedCall6("Inner4");
                        checkedCall6("Inner3.Inner4");  // REPORT THIS
                    }

                    public void innerFullClass()
                    {
                        final String checkedVar7 = "com.foo.InputLocationReference.Inner1.Inner2.Inner3.Inner4";
                        final String checkedVar7a = "error";  // REPORT THIS
                        checkedCall7("com.foo.InputLocationReference.Inner1.Inner2.Inner3.Inner4");
                        checkedCall7("com.foo.InputLocationReference$Inner1$Inner2$Inner3$Inner4");  // REPORT THIS
                    }

                    public void innerClassObject()
                    {
                        final Class<Inner4> checkedVar8 = Inner4.class;
                        final Class<String> checkedVar8a = String.class;  // REPORT THIS
                        checkedCall8(Inner4.class);
                        checkedCall8(String.class);  // REPORT THIS
                    }
                }
            }
        }

        public void someFoo2();
    }

    public void anonymous()
    {
        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(final String o1, final String o2)   // location = method
            {
                final String checkedVar9 = "compare";
                final String checkedVar9a = "error";  // REPORT THIS
                checkedCall9("compare");
                checkedCall9("innerMethod()");  // REPORT THIS
                return 0;
            }
        };

        Comparator<String> comparator2 = new Comparator<String>() {
            @Override
            public int compare(final String o1, final String o2)   // location = simpleclass
            {
                final String checkedVar10 = "InputLocationReference";
                final String checkedVar10a = "Comparator";  // REPORT THIS
                checkedCall10("InputLocationReference");
                checkedCall10("Comparator");  // REPORT THIS
                return 0;
            }
        };
    }


    public void argPositionsMinus1()
    {
        checkedCall11(null, null, null, "argPositionsMinus1");
        checkedCall11(null, null, null, null);   // ignored because of type mismatch
        checkedCall11(null, null, null, "argPositions" + "Minus1");   // ignored because of arithmetic
        checkedCall11(null, "argPositionsMinus1", null, "foo");   // REPORT THIS
    }

    public void argPositionsMinus2()
    {
        checkedCall12(null, null, "argPositionsMinus2", null);
        checkedCall12(null, "argPositionsMinus2", "error", null);   // REPORT THIS
    }

    public void argPositionsPlus1()
    {
        checkedCall13(null, "argPositionsPlus1", null, null);
        checkedCall13("argPositionsPlus1", "error", null, null);   // REPORT THIS
    }


    // fields are ignored for method name checks, but not for class references
    private final String field1 = checkedCall1("unchecked", "unchecked", "unchecked", "unchecked");
    private final String field2 = checkedCall2(String.class, String.class, String.class, String.class);
    private final String field3 = checkedCall3("error", "param1", "param2", "param3");

    public InputLocationReference()
    {
        checkedCall1("<init>");
        checkedCall1("error");
        checkedCall2(InputLocationReference.class);
        checkedCall2(String.class);
    }
    {
        checkedCall1("<init>");
        checkedCall1("error");
        checkedCall2(InputLocationReference.class);
        checkedCall2(String.class);
    }

    static {
        checkedCall1("<clinit>");
        checkedCall1("error");
        checkedCall2(InputLocationReference.class);
        checkedCall2(String.class);
        String checkedVar1;
    }

    public InputLocationReference(final Object pParm1)
    {
        super();
    }

    public InputLocationReference(final Object pParm1, final Object pParm2)
    {
        this("error");
    }

    public InputLocationReference(final Object pParm1, final Object pParm2, final Object pParm3)
    {
        this(String.class);
    }

    private final Class<?> field4 = InputLocationReference.class;

    private static final class Inner5 {
        public static String checkedCall15(Object pObj) { return null; }
    }

    private static final String CONSTANT1 = Inner5.checkedCall15(InputLocationReference.class);
    private static final String CONSTANT2 = Inner5.checkedCall15(String.class);


    /*
     * The called methods for the test methods above; required for compilation.
     */
    private void checkedCall1(Object pObj) { /* empty */ }
    private String checkedCall1(Object pObj, Object a, int b, Object c) { return null; }

    private void checkedCall2(Object pObj) { /* empty */ }
    private String checkedCall2(Object pObj, Object a, int b, Object c) { return null; }

    private void checkedCall3(Object pObj) { /* empty */ }
    private void checkedCall3a(Object pObj) { /* empty */ }
    private String checkedCall3(Object pObj, Object a, int b, Object c) { return null; }

    private void checkedCall4(Object pObj) { /* empty */ }
    private void checkedCall4a(Object pObj) { /* empty */ }
    private void checkedCall4(Object pObj, Object a, int b, Object c) { /* empty */ }

    private static void checkedCall5(Object pObj) { /* empty */ }
    private static void checkedCall6(Object pObj) { /* empty */ }
    private static void checkedCall7(Object pObj) { /* empty */ }
    private static void checkedCall8(Object pObj) { /* empty */ }
    private void checkedCall9(Object pObj) { /* empty */ }
    private void checkedCall10(Object pObj) { /* empty */ }

    private void checkedCall11(Object pObj, Object a, int b, Object c) { /* empty */ }
    private void checkedCall12(Object pObj, Object a, int b, Object c) { /* empty */ }
    private void checkedCall13(Object pObj, Object a, int b, Object c) { /* empty */ }
    private static void checkedCall14(Object pObj) { /* empty */ }

    private void uncheckedCall(Object pObj) { /* empty */ }
    private void uncheckedCall(Object pObj, Object a, int b, Object c) { /* empty */ }
}
