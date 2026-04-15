/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jvnet.jaxb.plugin.commons_lang;

import com.sun.codemodel.*;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.xml.sax.ErrorHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class XjcCommonsLangPluginTest {

    private XjcCommonsLangPlugin plugin;

    @Mock
    private Outline outline;
    @Mock
    private Options options;
    @Mock
    private ErrorHandler errorHandler;
    @Mock
    private CClassInfo cClassInfo;

    private JCodeModel codeModel;
    private JDefinedClass implClass;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        plugin = new XjcCommonsLangPlugin();

        codeModel = new JCodeModel();
        implClass = codeModel._class("TestGeneratedClass");
    }

    @Test
    void testGetOptionName() {
        assertEquals("Xcommons-lang", plugin.getOptionName());
    }

    @Test
    void testGetUsage() {
        String usage = plugin.getUsage();
        assertNotNull(usage);
        assertTrue(usage.contains("-Xcommons-lang"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testRunGeneratesAllMethods() throws Exception {
        ClassOutline classOutline = new TestableClassOutline(cClassInfo, implClass);
        when(outline.getClasses()).thenReturn((Collection) Collections.singletonList(classOutline));

        boolean result = plugin.run(outline, options, errorHandler);

        assertTrue(result);

        assertTrue(hasMethod(implClass, "toString"), "toString() should be generated");
        assertTrue(hasMethod(implClass, "equals"), "equals() should be generated");
        assertTrue(hasMethod(implClass, "hashCode"), "hashCode() should be generated");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testRunSkipsMethodsWhenDisabled() throws Exception {
        ClassOutline classOutline = new TestableClassOutline(cClassInfo, implClass);
        when(outline.getClasses()).thenReturn((Collection) Collections.singletonList(classOutline));

        // Set flags to disable generation
        plugin.parseArgument(options, new String[]{"-Xcommons-lang:addToStringMethod=FALSE"}, 0);
        plugin.parseArgument(options, new String[]{"-Xcommons-lang:addEqualsMethod=FALSE"}, 0);
        plugin.parseArgument(options, new String[]{"-Xcommons-lang:addHashCodeMethod=FALSE"}, 0);

        plugin.run(outline, options, errorHandler);

        // Verify that the methods were NOT added to the real object
        assertFalse(hasMethod(implClass, "toString"), "toString() should not be generated");
        assertFalse(hasMethod(implClass, "equals"), "equals() should not be generated");
        assertFalse(hasMethod(implClass, "hashCode"), "hashCode() should not be generated");
    }


    @Test
    @SuppressWarnings("unchecked")
    void testOnlyToStringGenerated() throws Exception {
        ClassOutline classOutline = new TestableClassOutline(cClassInfo, implClass);
        when(outline.getClasses()).thenReturn((Collection) Collections.singletonList(classOutline));

        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:addToStringMethod=TRUE"}, 0));
        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:addEqualsMethod=FALSE"}, 0));
        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:addHashCodeMethod=FALSE"}, 0));

        boolean result = plugin.run(outline, options, errorHandler);

        assertTrue(result);

        assertTrue(hasMethod(implClass, "toString"), "toString() should be generated");
        assertFalse(hasMethod(implClass, "equals"), "equals() should be generated");
        assertFalse(hasMethod(implClass, "hashCode"), "hashCode() should be generated");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testOnlyEqualsGenerated() throws Exception {
        ClassOutline classOutline = new TestableClassOutline(cClassInfo, implClass);
        when(outline.getClasses()).thenReturn((Collection) Collections.singletonList(classOutline));

        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:addToStringMethod=FALSE"}, 0));
        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:addEqualsMethod=TRUE"}, 0));
        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:addHashCodeMethod=FALSE"}, 0));


        boolean result = plugin.run(outline, options, errorHandler);

        assertTrue(result);

        assertFalse(hasMethod(implClass, "toString"), "toString() should be generated");
        assertTrue(hasMethod(implClass, "equals"), "equals() should be generated");
        JMethod method = getMethod(implClass, "equals").get();
        assertEquals(1, method.listParams().length);
        assertEquals("that", Arrays.stream(method.listParams()).findFirst().get().name());
        assertEquals("java.lang.Object", Arrays.stream(method.listParams()).findFirst().get().type().fullName());
        assertFalse(hasMethod(implClass, "hashCode"), "hashCode() should be generated");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testOnlyEqualsGeneratedWithTestTransients() throws Exception {
        ClassOutline classOutline = new TestableClassOutline(cClassInfo, implClass);
        when(outline.getClasses()).thenReturn((Collection) Collections.singletonList(classOutline));

        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:addToStringMethod=FALSE"}, 0));
        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:addEqualsMethod=TRUE"}, 0));
        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:addHashCodeMethod=FALSE"}, 0));
        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:equalsTestTransients=TRUE"}, 0));


        boolean result = plugin.run(outline, options, errorHandler);

        assertTrue(result);

        assertFalse(hasMethod(implClass, "toString"), "toString() should be generated");
        assertTrue(hasMethod(implClass, "equals"), "equals() should be generated");
        assertFalse(hasMethod(implClass, "hashCode"), "hashCode() should be generated");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testOnlyEqualsGeneratedWithRecursive() throws Exception {
        ClassOutline classOutline = new TestableClassOutline(cClassInfo, implClass);
        when(outline.getClasses()).thenReturn((Collection) Collections.singletonList(classOutline));

        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:addToStringMethod=FALSE"}, 0));
        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:addEqualsMethod=TRUE"}, 0));
        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:addHashCodeMethod=FALSE"}, 0));
        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:equalsTestRecursive=TRUE"}, 0));


        boolean result = plugin.run(outline, options, errorHandler);

        assertTrue(result);

        assertFalse(hasMethod(implClass, "toString"), "toString() should be generated");
        assertTrue(hasMethod(implClass, "equals"), "equals() should be generated");
        assertFalse(hasMethod(implClass, "hashCode"), "hashCode() should be generated");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testOnlyEqualsGeneratedWithTransiantAndRecursive() throws Exception {
        ClassOutline classOutline = new TestableClassOutline(cClassInfo, implClass);
        when(outline.getClasses()).thenReturn((Collection) Collections.singletonList(classOutline));

        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:addToStringMethod=FALSE"}, 0));
        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:addEqualsMethod=TRUE"}, 0));
        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:addHashCodeMethod=FALSE"}, 0));
        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:equalsTestTransients=TRUE"}, 0));
        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:equalsTestRecursive=TRUE"}, 0));


        boolean result = plugin.run(outline, options, errorHandler);

        assertTrue(result);

        assertFalse(hasMethod(implClass, "toString"), "toString() should be generated");
        assertTrue(hasMethod(implClass, "equals"), "equals() should be generated");
        JMethod method = getMethod(implClass, "equals").get();
        assertEquals(1, method.listParams().length);
        assertEquals("that", Arrays.stream(method.listParams()).findFirst().get().name());
        assertEquals("java.lang.Object", Arrays.stream(method.listParams()).findFirst().get().type().fullName());
        assertFalse(hasMethod(implClass, "hashCode"), "hashCode() should be generated");
    }


    @Test
    @SuppressWarnings("unchecked")
    void testOnlyHashCodeGenerated() throws Exception {
        ClassOutline classOutline = new TestableClassOutline(cClassInfo, implClass);
        when(outline.getClasses()).thenReturn((Collection) Collections.singletonList(classOutline));

        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:addToStringMethod=FALSE"}, 0));
        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:addEqualsMethod=FALSE"}, 0));
        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:addHashCodeMethod=TRUE"}, 0));

        boolean result = plugin.run(outline, options, errorHandler);

        assertTrue(result);

        assertFalse(hasMethod(implClass, "toString"), "toString() should be generated");
        assertFalse(hasMethod(implClass, "equals"), "equals() should be generated");
        assertTrue(hasMethod(implClass, "hashCode"), "hashCode() should be generated");
    }


    @Test
    void testParseArgumentToStringStyleStandard() throws BadCommandLineException {
        String arg = "-Xcommons-lang:ToStringStyle=SIMPLE_STYLE";
        assertEquals(1, plugin.parseArgument(options, new String[]{arg}, 0));
    }

    @Test
    void testParseArgumentToStringStyleCustom() throws BadCommandLineException {
        String arg = "-Xcommons-lang:ToStringStyle=java.lang.String";
        assertEquals(1, plugin.parseArgument(options, new String[]{arg}, 0));
    }

    @Test
    void testParseArgumentToStringStyleToClassGeneratorCantSeet() throws BadCommandLineException {
        String arg = "-Xcommons-lang:ToStringStyle=com.non.existent.Style";
        assertEquals(1, plugin.parseArgument(options, new String[]{arg}, 0));
    }

    @Test
    void testParseArgumentDisablingFlags() throws BadCommandLineException {
        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:addToStringMethod=TRUE"}, 0));
        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:addEqualsMethod=TRUE"}, 0));
        assertEquals(1, plugin.parseArgument(options, new String[]{"-Xcommons-lang:addHashCodeMethod=TRUE"}, 0));
    }

    @Test
    void testParseArgumentUnknown() throws BadCommandLineException {
        int result = plugin.parseArgument(options, new String[]{"-Xunknown-param"}, 0);
        assertEquals(0, result);
    }

    /**
     * Branch Coverage: parseArgument catch block (ClassNotFoundException/SecurityException)
     * Triggers when the style name provided is not a valid field in ToStringStyle
     * AND the string itself is not a valid Class name.
     */
    @Test
    void testParseArgumentToStringStyleInvalidClassError() {
        String arg = "-Xcommons-lang:ToStringStyle=NO_FINAL_STATIC_EXISTS_FOR_THIS";
        String[] args = {arg};

        assertThrows(BadCommandLineException.class, () -> {
            plugin.parseArgument(options, args, 0);
        });
    }

    /**
     * Branch Coverage: createToStringMethod JExpr._new branch
     * This triggers when customToStringStyle != null.
     */
    @Test
    void testCreateToStringMethodWithCustomClassBranch() throws Exception {
        String arg = "-Xcommons-lang:ToStringStyle=java.lang.String";
        plugin.parseArgument(options, new String[]{arg}, 0);

        ClassOutline classOutline = new TestableClassOutline(cClassInfo, implClass);
        when(outline.getClasses()).thenReturn((Collection) Collections.singletonList(classOutline));

        boolean result = plugin.run(outline, options, errorHandler);

        assertTrue(result);
        assertTrue(hasMethod(implClass, "toString"), "toString() should be generated using custom class branch");
    }

    private boolean hasMethod(JDefinedClass clazz, String methodName) {
        for (JMethod m : clazz.methods()) {
            if (m.name().equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    private Optional<JMethod> getMethod(JDefinedClass clazz, String methodName) {
        return clazz.methods().stream()
            .filter(anno -> anno.name().equals(methodName))
            .findFirst();
    }

    /**
     * Helper to bypass protected constructor of XJC's ClassOutline.
     */
    public static class TestableClassOutline extends ClassOutline {
        public TestableClassOutline(CClassInfo target, JDefinedClass implClass) {
            super(target, null, null, implClass);
        }

        @Override
        public Outline parent() {
            return null;
        }
    }
}
